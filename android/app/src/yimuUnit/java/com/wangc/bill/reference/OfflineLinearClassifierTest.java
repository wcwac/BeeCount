package com.wangc.bill.reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public final class OfflineLinearClassifierTest {
    private static String[] labels() {
        String[] labels = new String[OfflineLinearClassifier.CLASS_COUNT];
        for (int i = 0; i < labels.length; i++) labels[i] = "category_" + i;
        return labels;
    }

    private static OfflineLinearClassifier.Model model(int numDocs, int[] df, double[] weights) {
        return new OfflineLinearClassifier.Model(numDocs, df, labels(), weights);
    }

    @Test public void noKnownFeaturesProduceUniformProbabilitiesBelowTheOriginalThreshold() {
        OfflineLinearClassifier classifier = new OfflineLinearClassifier(model(99, new int[]{1}, new double[70]));
        OfflineLinearClassifier.Prediction prediction = classifier.predict(Collections.emptyMap());
        assertEquals(70, prediction.probabilities().length);
        for (double probability : prediction.probabilities()) assertEquals(1.0d / 70.0d, probability, 0.0d);
        assertNull(prediction.selectedLabel());
        assertEquals(-1, prediction.selectedClassIndex());
        assertTrue(Double.isNaN(prediction.selectedProbability()));
    }

    @Test public void oneFeatureUsesNormalizedSigmoidsInsteadOfSoftmax() {
        double[] weights = new double[70]; weights[0] = Math.log(3.0d);
        OfflineLinearClassifier classifier = new OfflineLinearClassifier(model(99, new int[]{1}, weights));
        OfflineLinearClassifier.Prediction prediction = classifier.predict(Collections.singletonMap(0, 1));
        // sigmoid(log(3))=3/4; all other 69 classes contribute 1/2.
        assertEquals(1.0d / 47.0d, prediction.probability(0), 1e-15d);
        assertEquals(2.0d / 141.0d, prediction.probability(1), 1e-15d);
        assertEquals("category_0", prediction.selectedLabel());
        assertEquals(prediction.probability(0), prediction.selectedProbability(), 0.0d);
        assertEquals(prediction.probability(0), prediction.probabilitiesByLabel().get("category_0"), 0.0d);
    }

    @Test public void termFrequencyDocumentFrequencyAndL2NormKeepTheirRecoveredMeaning() {
        double[] weights = new double[2 * 70];
        weights[0] = 2.0d; weights[70] = -1.0d;
        weights[1] = -1.0d; weights[71] = 2.0d;
        OfflineLinearClassifier classifier = new OfflineLinearClassifier(model(99, new int[]{1, 99}, weights));
        Map<Integer, Integer> frequencies = new LinkedHashMap<>(); frequencies.put(0, 1); frequencies.put(1, 9);
        OfflineLinearClassifier.Prediction prediction = classifier.predict(frequencies);
        // TF-IDF weights reduce algebraically to [2*log10(2), log10(2)].
        // Their normalized vector is [2/sqrt(5),1/sqrt(5)], giving logits
        // [3/sqrt(5),0,...,0] for the chosen feature-major coefficients.
        double sigmoid = 1.0d / (Math.exp(-3.0d / Math.sqrt(5.0d)) + 1.0d);
        double sum = sigmoid + 69.0d * 0.5d;
        assertEquals(sigmoid / sum, prediction.probability(0), 1e-15d);
        assertEquals(0.5d / sum, prediction.probability(1), 1e-15d);
    }

    @Test public void featureIterationFollowsNumericIdsRegardlessOfMapInsertionOrder() {
        double[] weights = new double[3 * 70];
        weights[0] = 1e16d; weights[70] = -1e16d; weights[140] = 1.0d;
        OfflineLinearClassifier classifier = new OfflineLinearClassifier(model(99, new int[]{1, 1, 1}, weights));
        Map<Integer, Integer> forward = new LinkedHashMap<>(), reverse = new LinkedHashMap<>();
        forward.put(0, 1); forward.put(1, 1); forward.put(2, 1);
        reverse.put(2, 1); reverse.put(1, 1); reverse.put(0, 1);
        assertArrayEquals(classifier.predict(forward).probabilities(), classifier.predict(reverse).probabilities(), 0.0d);
    }

    @Test public void equalWinningProbabilitiesUseLexicographicLabelOrder() {
        String[] catalog = labels(); catalog[0] = "zeta"; catalog[1] = "alpha";
        double[] weights = new double[70]; weights[0] = weights[1] = Math.log(3.0d);
        OfflineLinearClassifier classifier = new OfflineLinearClassifier(new OfflineLinearClassifier.Model(99, new int[]{1}, catalog, weights));
        OfflineLinearClassifier.Prediction prediction = classifier.predict(Collections.singletonMap(0, 1));
        assertEquals(prediction.probability(0), prediction.probability(1), 0.0d);
        assertEquals("alpha", prediction.selectedLabel());
        assertEquals(1, prediction.selectedClassIndex());
    }

    @Test public void aProbabilityEqualToTheOriginalThresholdDoesNotSelectALabel() {
        OfflineLinearClassifier.Model model = model(99, new int[]{1}, new double[70]);
        double[] probabilities = new double[70];
        Arrays.fill(probabilities, (1.0d - 0.015d) / 69.0d);
        probabilities[0] = 0.015d;
        assertEquals(-1, OfflineLinearClassifier.selectOriginalClass(model, probabilities));
        probabilities[0] = Math.nextUp(0.015d);
        assertEquals(0, OfflineLinearClassifier.selectOriginalClass(model, probabilities));
    }

    @Test public void allSigmoidsUnderflowingToZeroPreservesTheOriginalNaNResult() {
        double[] weights = new double[70]; Arrays.fill(weights, -1000.0d);
        OfflineLinearClassifier classifier = new OfflineLinearClassifier(model(99, new int[]{1}, weights));
        OfflineLinearClassifier.Prediction prediction = classifier.predict(Collections.singletonMap(0, 1));
        for (double probability : prediction.probabilities()) assertTrue(Double.isNaN(probability));
        assertNull(prediction.selectedLabel());
    }

    @Test public void termFrequencyAdditionKeepsTheOriginalJavaIntOverflowBoundary() {
        OfflineLinearClassifier classifier = new OfflineLinearClassifier(model(99, new int[]{1}, new double[70]));
        OfflineLinearClassifier.Prediction prediction = classifier.predict(Collections.singletonMap(0, Integer.MAX_VALUE));
        for (double probability : prediction.probabilities()) assertTrue(Double.isNaN(probability));
        assertNull(prediction.selectedLabel());
    }

    @Test public void numericModelsAndPredictionsDoNotExposeMutableArraysOrMaps() {
        int[] df = {1}; String[] labels = labels(); double[] weights = new double[70];
        weights[0] = Math.log(3.0d);
        OfflineLinearClassifier.Model model = new OfflineLinearClassifier.Model(99, df, labels, weights);
        df[0] = 0; labels[0] = "mutated"; weights[0] = Double.NaN;
        assertEquals(99, model.numDocs()); assertEquals(1, model.featureCount()); assertEquals("category_0", model.label(0));
        OfflineLinearClassifier.Prediction prediction = new OfflineLinearClassifier(model).predict(Collections.singletonMap(0, 1));
        double original = prediction.probability(0); double[] copy = prediction.probabilities(); copy[0] = -1.0d;
        assertEquals(original, prediction.probability(0), 0.0d);
        assertThrows(UnsupportedOperationException.class, () -> prediction.probabilitiesByLabel().put("category_0", 0.0d));
    }

    @Test public void numericModelValidationRejectsWrongShapeLabelsAndNonfiniteWeights() {
        assertThrows(IllegalArgumentException.class, () -> model(0, new int[]{1}, new double[70]));
        assertThrows(IllegalArgumentException.class, () -> model(99, new int[]{0}, new double[70]));
        assertThrows(IllegalArgumentException.class, () -> model(99, new int[]{100}, new double[70]));
        assertThrows(IllegalArgumentException.class, () -> model(99, new int[]{1}, new double[69]));
        assertThrows(IllegalArgumentException.class, () -> new OfflineLinearClassifier.Model(99, new int[]{1}, new String[]{"only_one"}, new double[70]));
        String[] duplicate = labels(); duplicate[1] = duplicate[0];
        assertThrows(IllegalArgumentException.class, () -> new OfflineLinearClassifier.Model(99, new int[]{1}, duplicate, new double[70]));
        double[] nonfinite = new double[70]; nonfinite[4] = Double.NaN;
        assertThrows(IllegalArgumentException.class, () -> model(99, new int[]{1}, nonfinite));
        nonfinite[4] = Double.POSITIVE_INFINITY;
        assertThrows(IllegalArgumentException.class, () -> model(99, new int[]{1}, nonfinite));
    }

    @Test public void featureInputRejectsInvalidIdsCountsAndNullsInsteadOfGuessingTokens() {
        OfflineLinearClassifier classifier = new OfflineLinearClassifier(model(99, new int[]{1}, new double[70]));
        assertThrows(IllegalArgumentException.class, () -> classifier.predict(null));
        assertThrows(IllegalArgumentException.class, () -> classifier.predict(Collections.singletonMap(-1, 1)));
        assertThrows(IllegalArgumentException.class, () -> classifier.predict(Collections.singletonMap(1, 1)));
        assertThrows(IllegalArgumentException.class, () -> classifier.predict(Collections.singletonMap(0, 0)));
        assertThrows(IllegalArgumentException.class, () -> classifier.predict(Collections.singletonMap(0, -1)));
        assertThrows(IllegalArgumentException.class, () -> classifier.predict(Collections.singletonMap(null, 1)));
        assertThrows(IllegalArgumentException.class, () -> classifier.predict(Collections.singletonMap(0, null)));
    }
}
