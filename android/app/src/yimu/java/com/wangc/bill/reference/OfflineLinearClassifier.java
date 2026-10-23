package com.wangc.bill.reference;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The recovered classifier's numeric path, independent of Android, tokenization and model I/O.
 * Labels are the original catalog in weight-column order. The actual serialized liblinear
 * label array is 0..69; AbstractClassifier.predict maps probability columns to that catalog.
 * This class does not load objects or tokenize text, and is not connected to capture or UI.
 */
public final class OfflineLinearClassifier {
    public static final int CLASS_COUNT = 70;
    public static final double ORIGINAL_THRESHOLD = 0.015d;

    private final Model model;

    /** Immutable numeric data supplied by a separately reviewed, bounded data reader. */
    public static final class Model {
        private final int numDocs;
        private final int[] documentFrequencies;
        private final String[] labels;
        private final double[] featureMajorWeights;
        private final int[] lexicographicClassIndices;

        /**
         * weights[featureId * 70 + classIndex] is the original liblinear layout.
         * The recovered model has bias=-1, so this interface intentionally has no bias feature.
         */
        public Model(int numDocs, int[] documentFrequencies, String[] labels,
                double[] featureMajorWeights) {
            if (numDocs <= 0 || documentFrequencies == null || labels == null
                    || featureMajorWeights == null || documentFrequencies.length == 0
                    || labels.length != CLASS_COUNT
                    || documentFrequencies.length > Integer.MAX_VALUE / CLASS_COUNT
                    || featureMajorWeights.length != documentFrequencies.length * CLASS_COUNT) {
                throw new IllegalArgumentException("Invalid numeric model dimensions");
            }
            this.numDocs = numDocs;
            this.documentFrequencies = documentFrequencies.clone();
            this.labels = labels.clone();
            this.featureMajorWeights = featureMajorWeights.clone();
            for (int frequency : this.documentFrequencies) {
                if (frequency <= 0 || frequency > numDocs) {
                    throw new IllegalArgumentException("Invalid document frequency");
                }
            }
            for (double weight : this.featureMajorWeights) {
                if (!Double.isFinite(weight)) {
                    throw new IllegalArgumentException("Nonfinite model weight");
                }
            }
            TreeMap<String, Integer> ordered = new TreeMap<>();
            for (int i = 0; i < CLASS_COUNT; i++) {
                String label = this.labels[i];
                if (label == null || label.isEmpty() || ordered.put(label, i) != null) {
                    throw new IllegalArgumentException("Missing or duplicate class label");
                }
            }
            lexicographicClassIndices = new int[CLASS_COUNT];
            int index = 0;
            for (int classIndex : ordered.values()) lexicographicClassIndices[index++] = classIndex;
        }

        public int numDocs() { return numDocs; }
        public int featureCount() { return documentFrequencies.length; }
        public String label(int classIndex) { return labels[classIndex]; }
    }

    /** All probability columns plus the u4.l/f choice strictly above the original threshold. */
    public static final class Prediction {
        private final double[] probabilities;
        private final SortedMap<String, Double> probabilitiesByLabel;
        private final String selectedLabel;
        private final int selectedClassIndex;

        private Prediction(Model model, double[] probabilities) {
            this.probabilities = probabilities;
            TreeMap<String, Double> named = new TreeMap<>();
            for (int i = 0; i < CLASS_COUNT; i++) named.put(model.labels[i], probabilities[i]);
            probabilitiesByLabel = Collections.unmodifiableSortedMap(named);
            selectedClassIndex = selectOriginalClass(model, probabilities);
            selectedLabel = selectedClassIndex < 0 ? null : model.labels[selectedClassIndex];
        }

        public double[] probabilities() { return probabilities.clone(); }
        public double probability(int classIndex) { return probabilities[classIndex]; }
        public SortedMap<String, Double> probabilitiesByLabel() { return probabilitiesByLabel; }
        public String selectedLabel() { return selectedLabel; }
        public int selectedClassIndex() { return selectedClassIndex; }
        /** NaN denotes no label passing the threshold; the 70 raw columns remain available. */
        public double selectedProbability() {
            return selectedClassIndex < 0 ? Double.NaN : probabilities[selectedClassIndex];
        }
    }

    public OfflineLinearClassifier(Model model) {
        if (model == null) throw new IllegalArgumentException("Numeric model is required");
        this.model = model;
    }

    /**
     * Counts must already refer to known vocabulary IDs and be positive Java ints, as in
     * Document/FrequencyMap. Unseen text tokens belong in the tokenizer/vocabulary stage.
     * The input map is snapshotted and sorted by ID to preserve the original summation order.
     */
    public Prediction predict(Map<Integer, Integer> termFrequencies) {
        if (termFrequencies == null) throw new IllegalArgumentException("Feature counts are required");
        TreeMap<Integer, Integer> ordered = new TreeMap<>();
        for (Map.Entry<Integer, Integer> entry : termFrequencies.entrySet()) {
            Integer featureId = entry.getKey(), count = entry.getValue();
            if (featureId == null || count == null || featureId < 0
                    || featureId >= model.documentFrequencies.length || count <= 0) {
                throw new IllegalArgumentException("Invalid known feature count");
            }
            ordered.put(featureId, count);
        }

        int[] featureIds = new int[ordered.size()];
        double[] features = new double[ordered.size()];
        int at = 0;
        for (Map.Entry<Integer, Integer> entry : ordered.entrySet()) {
            int featureId = entry.getKey();
            int termFrequency = entry.getValue();
            featureIds[at] = featureId;
            // TfIdfFeatureWeighter.weight: keep the original Java int addition.
            // Integer.MAX_VALUE + 1 overflows before log10; do not silently widen it.
            features[at++] = Math.log10(termFrequency + 1)
                * Math.log10(((double) model.numDocs / model.documentFrequencies[featureId]) + 1.0d);
        }
        double squaredNorm = 0.0d;
        for (double value : features) squaredNorm += value * value;
        double norm = Math.sqrt(squaredNorm);
        for (int i = 0; i < features.length; i++) features[i] = features[i] / norm;

        double[] probabilities = new double[CLASS_COUNT];
        for (int i = 0; i < features.length; i++) {
            int offset = featureIds[i] * CLASS_COUNT;
            for (int category = 0; category < CLASS_COUNT; category++) {
                probabilities[category] = probabilities[category]
                    + (model.featureMajorWeights[offset + category] * features[i]);
            }
        }
        // Linear.predictProbability uses normalized sigmoids, not softmax.
        for (int category = 0; category < CLASS_COUNT; category++) {
            probabilities[category] = 1.0d / (Math.exp(-probabilities[category]) + 1.0d);
        }
        double sum = 0.0d;
        for (double probability : probabilities) sum += probability;
        for (int category = 0; category < CLASS_COUNT; category++) {
            probabilities[category] = probabilities[category] / sum;
        }
        // Preserve original IEEE results, including NaN after overflow or an all-zero
        // sigmoid sum. The original strict comparison selects no label in that case.
        return new Prediction(model, probabilities);
    }

    /** Package-visible so exact threshold/tie boundaries can be checked independently. */
    static int selectOriginalClass(Model model, double[] probabilities) {
        double best = ORIGINAL_THRESHOLD;
        int selected = -1;
        for (int classIndex : model.lexicographicClassIndices) {
            if (probabilities[classIndex] > best) {
                best = probabilities[classIndex];
                selected = classIndex;
            }
        }
        return selected;
    }
}
