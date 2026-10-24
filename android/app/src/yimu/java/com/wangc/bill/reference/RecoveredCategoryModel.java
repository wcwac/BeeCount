package com.wangc.bill.reference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Recovered numeric model and inert dictionary data. No serialized object is loaded.
 * Tokenization is an explicit host approximation: frequency-path core segmentation
 * plus longest custom-word merging. HanLP bigram, atoms and person NER are not ported.
 */
public final class RecoveredCategoryModel {
    public static final String TOKENIZER="RECOVERED_DICTIONARY_APPROXIMATION";
    public static final String MODEL_SHA256="91e47b80c6c68fa95ea601f2da383bccec831e3b6f3d1dbb461ad60d452d7ccc";
    public static final String DICTIONARY_SHA256="40b5c731bd4d0d3a02dae275989de3b1ab17d9a05aed95ce579a72c7542132e2";
    private static final int FEATURES=5706,CLASSES=70,MAX_TEXT=2000;
    // Actual utils.h2.T punctuation/space removal; case and other whitespace are preserved.
    private static final Pattern CLEAN=Pattern.compile("[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]");
    private final OfflineLinearClassifier classifier;
    private final Map<String,Integer> vocabulary;
    private final String[] coreWords,customWords;
    private final int[] coreFrequencies;
    private final int totalFrequency,coreMaxLength,customMaxLength;
    private final Map<String,Result> cache=new LinkedHashMap<String,Result>(128,.75f,true){
        @Override protected boolean removeEldestEntry(Map.Entry<String,Result> entry){return size()>128;}
    };

    public static final class Result {
        public final String label;
        public final double score;
        public final int knownTerms;
        private Result(String label,double score,int knownTerms){this.label=label;this.score=score;this.knownTerms=knownTerms;}
    }
    private RecoveredCategoryModel(OfflineLinearClassifier classifier,Map<String,Integer> vocabulary,
            String[] coreWords,int[] coreFrequencies,int totalFrequency,String[] customWords){
        this.classifier=classifier;this.vocabulary=vocabulary;this.coreWords=coreWords;
        this.coreFrequencies=coreFrequencies;this.totalFrequency=totalFrequency;this.customWords=customWords;
        int coreMax=0,customMax=0;for(String word:coreWords)coreMax=Math.max(coreMax,word.length());
        for(String word:customWords)customMax=Math.max(customMax,word.length());
        coreMaxLength=coreMax;customMaxLength=customMax;
    }

    /** Call on a worker. SHA and exact dimensions bind both resources to the reviewed data. */
    public static RecoveredCategoryModel read(InputStream modelResource,InputStream dictionaryResource)throws IOException{
        try(InputStream modelInput=modelResource;InputStream dictionaryInput=dictionaryResource;
                DataInputStream in=data(modelInput,MODEL_SHA256)){
            if(in.readInt()!=0x59434d31||in.readInt()!=1||in.readInt()!=FEATURES||in.readInt()!=CLASSES||in.readInt()!=40040)
                throw new IOException("Invalid category model header");
            String[] labels=new String[CLASSES];for(int i=0;i<CLASSES;i++)labels[i]=text(in);
            Map<String,Integer> vocabulary=new HashMap<>();int[] frequencies=new int[FEATURES];
            for(int i=0;i<FEATURES;i++){
                if(vocabulary.put(text(in),i)!=null)throw new IOException("Duplicate model word");
                frequencies[i]=in.readInt();if(frequencies[i]<=0||frequencies[i]>40040)throw new IOException("Invalid document frequency");
            }
            double[] weights=new double[FEATURES*CLASSES];
            for(int i=0;i<weights.length;i++){weights[i]=in.readDouble();if(!Double.isFinite(weights[i]))throw new IOException("Invalid model weight");}
            if(in.read()!=-1)throw new IOException("Trailing category model data");
            OfflineLinearClassifier classifier=new OfflineLinearClassifier(new OfflineLinearClassifier.Model(40040,frequencies,labels,weights));
            try(DataInputStream dictionary=data(dictionaryInput,DICTIONARY_SHA256)){
                if(dictionary.readInt()!=0x59434431||dictionary.readInt()!=1||dictionary.readInt()!=9727730||dictionary.readInt()!=85585)
                    throw new IOException("Invalid category dictionary header");
                String[] core=new String[85585];int[] counts=new int[core.length];
                for(int i=0;i<core.length;i++){
                    core[i]=text(dictionary);counts[i]=dictionary.readInt();
                    if(counts[i]<=0||counts[i]>9727730||i>0&&core[i-1].compareTo(core[i])>=0)throw new IOException("Invalid core dictionary entry");
                }
                if(dictionary.readInt()!=83911)throw new IOException("Invalid custom dictionary count");
                String[] custom=new String[83911];
                for(int i=0;i<custom.length;i++){
                    custom[i]=text(dictionary);
                    if(i>0&&custom[i-1].compareTo(custom[i])>=0)throw new IOException("Invalid custom dictionary order");
                }
                if(dictionary.read()!=-1)throw new IOException("Trailing category dictionary data");
                return new RecoveredCategoryModel(classifier,vocabulary,core,counts,9727730,custom);
            }
        }
    }

    public Result predict(String input){
        if(input==null||input.isEmpty())return new Result(null,Double.NaN,0);
        String normalized=CLEAN.matcher(input.substring(0,Math.min(MAX_TEXT,input.length()))).replaceAll("");
        if(normalized.isEmpty())return new Result(null,Double.NaN,0);
        synchronized(cache){Result old=cache.get(normalized);if(old!=null)return old;}
        Map<Integer,Integer> counts=new HashMap<>();int known=0;
        for(String token:segment(normalized)){
            if(token.indexOf(0)>=0)continue;
            Integer feature=vocabulary.get(token);if(feature==null)continue;
            counts.put(feature,counts.getOrDefault(feature,0)+1);known++;
        }
        Result result;
        if(known==0)result=new Result(null,Double.NaN,0);
        else{
            OfflineLinearClassifier.Prediction prediction=classifier.predict(counts);
            result=new Result(prediction.selectedLabel(),prediction.selectedProbability(),known);
        }
        synchronized(cache){cache.put(normalized,result);}return result;
    }

    private List<String> segment(String text){
        int length=text.length();double[] costs=new double[length+1];int[] next=new int[length];
        costs[length]=0;double unknown=Math.log(totalFrequency+1d);
        for(int start=length-1;start>=0;start--){
            int fallback=start+1;
            if(ascii(text.charAt(start)))while(fallback<length&&fallback-start<128&&ascii(text.charAt(fallback)))fallback++;
            next[start]=fallback;costs[start]=unknown+costs[fallback];
            int maximum=Math.min(length,start+coreMaxLength);
            for(int end=start+1;end<=maximum;end++){
                int word=Arrays.binarySearch(coreWords,text.substring(start,end));if(word<0)continue;
                double cost=Math.log((totalFrequency+1d)/(coreFrequencies[word]+1d))+costs[end];
                if(cost<costs[start]||cost==costs[start]&&end>next[start]){costs[start]=cost;next[start]=end;}
            }
        }
        List<String> base=new ArrayList<>();for(int at=0;at<length;at=next[at])base.add(text.substring(at,next[at]));
        List<String> merged=new ArrayList<>();
        for(int start=0;start<base.size();){
            StringBuilder joined=new StringBuilder();String chosen=base.get(start);int last=start+1;
            for(int end=start;end<base.size();end++){
                if(joined.length()+base.get(end).length()>customMaxLength)break;
                joined.append(base.get(end));String candidate=joined.toString();
                if(Arrays.binarySearch(customWords,candidate)>=0){chosen=candidate;last=end+1;}
            }
            merged.add(chosen);start=last;
        }
        return merged;
    }
    private static boolean ascii(char c){return c>='0'&&c<='9'||c>='a'&&c<='z'||c>='A'&&c<='Z';}
    private static DataInputStream data(InputStream resource,String hash)throws IOException{
        byte[] compressed;
        try(ByteArrayOutputStream output=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];int size;
            while((size=resource.read(buffer))!=-1){if(output.size()+size>4*1024*1024)throw new IOException("Category data too large");output.write(buffer,0,size);}
            compressed=output.toByteArray();
        }
        try{
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(compressed);StringBuilder actual=new StringBuilder(64);
            for(byte value:digest)actual.append(Character.forDigit((value>>>4)&15,16)).append(Character.forDigit(value&15,16));
            if(!actual.toString().equals(hash))throw new IOException("Category data hash mismatch");
        }catch(NoSuchAlgorithmException unavailable){throw new IOException("SHA-256 unavailable",unavailable);}
        return new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(compressed)));
    }
    private static String text(DataInputStream input)throws IOException{
        int size=input.readInt();if(size<=0||size>4096)throw new IOException("Invalid category string length");
        byte[] value=new byte[size];input.readFully(value);
        // The exact SHA-256 above authenticates data extracted as valid UTF-8;
        // no per-word decoder/Trie allocation is required for the large dictionary.
        return new String(value,StandardCharsets.UTF_8);
    }
}
