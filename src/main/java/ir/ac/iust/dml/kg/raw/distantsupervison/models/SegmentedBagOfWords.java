package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import de.bwaldvogel.liblinear.FeatureNode;
import ir.ac.iust.dml.kg.raw.distantsupervison.Configuration;
import ir.ac.iust.dml.kg.raw.distantsupervison.Constants;
import ir.ac.iust.dml.kg.raw.distantsupervison.CorpusEntryObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static java.lang.Math.log10;

/**
 * Created by hemmatan on 5/9/2017.
 */
public class SegmentedBagOfWords {
    private String segment;
    private String bowFile;
    private List<CorpusEntryObject> corpusOfBOW = new ArrayList<>();
    private Set<String> vocabularySet = new HashSet<>();
    private List<Set<String>> sentencesUniqueWords = new ArrayList<>();
    private int vocabularySize;
    private int maximumNoOfVocabulary = Configuration.maximumNoOfVocabularyForBagOfWords / 4;
    private double numberOfSentences;
    private HashMap<String, Double> idf = new HashMap<>();
    private HashMap<String, Double> tfInCorpus = new HashMap<>();
    private HashMap<String, Double> df = new HashMap<>();
    private HashMap<String, Double> tfIdfInCorpus = new HashMap<>();
    private Boolean doLemmatize = false;
    private HashMap<String, Integer> indices = new HashMap<>();
    private List<String> sortedByTf = new ArrayList<>();

    public SegmentedBagOfWords() {
    }

    public SegmentedBagOfWords(List<CorpusEntryObject> corpusOfBOW, String segment, Boolean doLemmatize, int maximumNoOfVocabulary) {
        this.doLemmatize = doLemmatize;
        this.numberOfSentences = corpusOfBOW.size();
        this.maximumNoOfVocabulary = maximumNoOfVocabulary;
        this.segment = segment;
        this.bowFile = segment;
        if (!doLemmatize) {
            extractVocabulary(corpusOfBOW);
        }
        saveModel();
    }


    public List<String> getSegmentWords(CorpusEntryObject corpusEntryObject) {
        List<String> words = new ArrayList<>();
        if (segment.equalsIgnoreCase(Constants.segmentedBagOfWordsAttribs.SUBJECT_PRECEDING))
            words = corpusEntryObject.getSubjectPrecedingWords();
        else if (segment.equalsIgnoreCase(Constants.segmentedBagOfWordsAttribs.SUBJECT_FOLLOWING))
            words = corpusEntryObject.getSubjectFollowingWords();
        else if (segment.equalsIgnoreCase(Constants.segmentedBagOfWordsAttribs.OBJECT_PRECEDING))
            words = corpusEntryObject.getObjectPrecedingWords();
        else words = corpusEntryObject.getObjectFollowingWords();
        return words;
    }

    private void extractVocabulary(List<CorpusEntryObject> corpusOfBOW) {
        this.corpusOfBOW = corpusOfBOW;
        for (CorpusEntryObject corpusEntryObject :
                corpusOfBOW) {
            List<String> words = getSegmentWords(corpusEntryObject);


            Set<String> currentSentenceUniqueWords = new HashSet<>();

            for (String queryWord :
                    words) {
                vocabularySet.add(queryWord);
                currentSentenceUniqueWords.add(queryWord);
                if (!tfInCorpus.containsKey(queryWord)) {
                    tfInCorpus.put(queryWord, 1.0);
                } else
                    tfInCorpus.put(queryWord, tfInCorpus.get(queryWord) + 1);
            }

            for (String word :
                    currentSentenceUniqueWords) {
                if (!df.containsKey(word))
                    df.put(word, 2.0);
                else
                    df.put(word, df.get(word) + 1);
            }
        }
        this.vocabularySize = vocabularySet.size();

        // compute idf and tf_idf for vocabulary
        Iterator it = df.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            idf.put((String) pair.getKey(), log10(this.numberOfSentences / (Double) pair.getValue()));
            Double currentTfIdf = tfInCorpus.get((String) pair.getKey()) * idf.get((String) pair.getKey());
            tfIdfInCorpus.put((String) pair.getKey(), currentTfIdf);
        }

        // sort the vocabulary by term frequency
        Comparator<Map.Entry> hashmapValueComparator = new HashmapValueComparator();
        PriorityQueue<Map.Entry> priorityQueue = new PriorityQueue<>(this.vocabularySize, hashmapValueComparator);
        Iterator tfInCorpusIterator = tfInCorpus.entrySet().iterator();
        while (tfInCorpusIterator.hasNext()) {
            Map.Entry pair = (Map.Entry) tfInCorpusIterator.next();
            priorityQueue.add(pair);
        }

        Integer currentIndex = 0;
        while (!priorityQueue.isEmpty() && currentIndex < this.maximumNoOfVocabulary) {
            Map.Entry pair = priorityQueue.poll();
            sortedByTf.add((String) pair.getKey());
            this.indices.put((String) pair.getKey(), currentIndex++);
        }
    }


    public HashMap<String, Double> computeTfIdfForQuery(CorpusEntryObject corpusEntryObject) {
        List<String> queryWords = getSegmentWords(corpusEntryObject);
        HashMap<String, Integer> tf = new HashMap<>();
        HashMap<String, Double> tf_idf = new HashMap<>();
        for (String queryWord :
                queryWords) {
            if (!tf.containsKey(queryWord))
                tf.put(queryWord, 1);
            else
                tf.put(queryWord, tf.get(queryWord) + 1);
        }
        for (String queryWord :
                queryWords) {
            if (idf.containsKey(queryWord))
                tf_idf.put(queryWord, tf.get(queryWord) * idf.get(queryWord));
            else tf_idf.put(queryWord, log10(this.numberOfSentences));
        }
        return tf_idf;
    }

    public List<Double> createBowFeatureVectorForQuery(CorpusEntryObject corpusEntryObject) {
        List<Double> featureVector = new ArrayList<>();
        for (int i = 0; i < this.maximumNoOfVocabulary; i++)
            featureVector.add(0.0);
        HashMap<String, Double> tf_idf = computeTfIdfForQuery(corpusEntryObject);
        List<String> queryWords = getSegmentWords(corpusEntryObject);
        for (String queryWord :
                queryWords) {
            // TODO: this is temp! did not have time to add OOV
            if (this.indices.containsKey(queryWord))
                featureVector.set(this.indices.get(queryWord), tf_idf.get(queryWord));
        }
        return featureVector;
    }

    public FeatureNode[] createBowLibLinearFeatureNodeForQuery(CorpusEntryObject corpusEntryObject) {
        List<String> queryWords = getSegmentWords(corpusEntryObject);
        FeatureNode[] featureNodes = new FeatureNode[this.maximumNoOfVocabulary];
        List<Double> featureVector = this.createBowFeatureVectorForQuery(corpusEntryObject);
        for (int i = 0; i < featureVector.size(); i++) {
            featureNodes[i] = new FeatureNode(i + 1, featureVector.get(i));
        }
        return featureNodes;
    }

    public void saveModel() {
        HashMap<String, Integer> temp = new HashMap<>();
        temp.put("tfInCorpus", 0);
        temp.put("df", 1);
        temp.put("idf", 2);
        String[] parameters = {"tfInCorpus", "df", "idf"};
        System.out.println(this.bowFile);
        try (Writer fileWriter = new FileWriter(this.bowFile)) {
            fileWriter.write("maximumNoOfVocabulary" + "\t" + this.maximumNoOfVocabulary + "\n");
            for (String token :
                    this.sortedByTf) {
                fileWriter.write(token + "\t");
                fileWriter.write(this.tfInCorpus.get(token) + "\t");
                fileWriter.write(this.df.get(token) + "\t");
                fileWriter.write(this.idf.get(token) + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int getMaximumNoOfVocabulary() {
        return maximumNoOfVocabulary;
    }
}
