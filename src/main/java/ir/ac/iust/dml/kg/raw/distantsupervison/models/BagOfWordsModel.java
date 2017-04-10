package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import de.bwaldvogel.liblinear.FeatureNode;
import ir.ac.iust.dml.kg.raw.distantsupervison.Sentence;

import java.io.*;
import java.util.*;

import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.bagOfWordsModelPath;
import static java.lang.Math.log10;

/**
 * Created by hemmatan on 4/9/2017.
 */
public class BagOfWordsModel {

    private List<Sentence> corpusOfBOW = new ArrayList<>();
    private Set<String> vocabularySet = new HashSet<>();
    private int vocabularySize;
    private int maximumNoOfVocabulary = 1000;
    private double numberOfSentences;
    private HashMap<String, Double> idf = new HashMap<>();
    private HashMap<String, Double> tfInCorpus = new HashMap<>();
    private HashMap<String, Double> df = new HashMap<>();
    private HashMap<String, Double> tfIdfInCorpus = new HashMap<>();
    private Boolean doLemmatize = false;
    private HashMap<String, Integer> indices = new HashMap<>();
    private List<String> sortedByTf = new ArrayList<>();


    public BagOfWordsModel(List<Sentence> corpusOfBOW, Boolean doLemmatize){
        this.doLemmatize = doLemmatize;
        this.numberOfSentences = corpusOfBOW.size();
        if (!doLemmatize){
            extractVocabulary(corpusOfBOW);
        }
        saveModel();
    }

    private void extractVocabulary(List<Sentence> corpusOfBOW) {
        this.corpusOfBOW = corpusOfBOW;
        for (Sentence sentence:
             corpusOfBOW) {
            List<String> words = sentence.getWords();
            Set<String> uniqueWords = new HashSet<>();

            for (String queryWord:
                 words) {
                vocabularySet.add(queryWord);
                uniqueWords.add(queryWord);
                if (!tfInCorpus.containsKey(queryWord)) {
                    tfInCorpus.put(queryWord, 1.0);
                }
                else
                    tfInCorpus.put(queryWord, tfInCorpus.get(queryWord)+1);
            }

            for (String word:
                    uniqueWords) {
                if (!df.containsKey(word))
                    df.put(word, 1.0);
                else
                    df.put(word, df.get(word)+1);
            }
        }
        this.vocabularySize = vocabularySet.size();

        // compute idf and tf_idf for vocabulary
        Iterator it = df.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            idf.put((String) pair.getKey(), log10(this.numberOfSentences/(Double) pair.getValue()));
            Double currentTfIdf = tfInCorpus.get((String) pair.getKey())*idf.get((String) pair.getKey());
            tfIdfInCorpus.put((String) pair.getKey(), currentTfIdf);
        }

        // sort the vocabulary by term frequency
        Comparator<Map.Entry> hashmapValueComparator = new HashmapValueComparator();
        PriorityQueue<Map.Entry> priorityQueue =  new PriorityQueue<>(this.vocabularySize, hashmapValueComparator);
        Iterator tfInCorpusIterator = tfInCorpus.entrySet().iterator();
        while (tfInCorpusIterator.hasNext()){
            Map.Entry pair = (Map.Entry) tfInCorpusIterator.next();
            priorityQueue.add(pair);
        }

        Integer currentIndex = 0;
        while (!priorityQueue.isEmpty() && currentIndex<this.maximumNoOfVocabulary){
            Map.Entry pair = priorityQueue.poll();
            sortedByTf.add((String) pair.getKey());
            this.indices.put((String) pair.getKey(), currentIndex++);
        }

    }

    public HashMap<String, Double> computeTfIdfForQuery(List<String> queryWords){
        HashMap<String, Integer> tf = new HashMap<>();
        HashMap<String, Double> tf_idf = new HashMap<>();
        for (String queryWord:
             queryWords) {
            if (!tf.containsKey(queryWord))
                tf.put(queryWord, 1);
            else
                tf.put(queryWord, tf.get(queryWord)+1);
        }
        for (String queryWord:
             queryWords) {
            tf_idf.put(queryWord, tf.get(queryWord)*idf.get(queryWord));
        }
        return tf_idf;
    }

    public List<Double> createBowFeatureVectorForQuery(List<String> queryWords){
        List<Double> featureVector  = new ArrayList<>();
        for (int i = 0; i<this.maximumNoOfVocabulary; i++)
            featureVector.add(0.0);
        HashMap<String, Double> tf_idf = computeTfIdfForQuery(queryWords);
        for (String queryWord:
             queryWords) {
            // TODO: this is temp! did not have time to add OOV
            if (this.indices.containsKey(queryWord))
                featureVector.set(this.indices.get(queryWord), tf_idf.get(queryWord));
        }
        return featureVector;
    }

    public FeatureNode[] createBowLibLinearFeatureNodeForQuery(List<String> queryWords){
        FeatureNode[] featureNodes = new FeatureNode[this.maximumNoOfVocabulary];
        List<Double> featureVector = this.createBowFeatureVectorForQuery(queryWords);
        for (int i = 0; i<featureVector.size(); i++){
            featureNodes[i] = new FeatureNode(i+1, featureVector.get(i));
        }
        return featureNodes;
    }

    public void saveModel(){
        try (Writer fileWriter = new FileWriter("C:\\Users\\hemmatan\\Desktop\\KG\\distant-supervision-re\\src\\main\\java\\ir\\ac\\iust\\dml\\kg\\raw\\distantsupervison\\models\\bagOfWords")) {
            for (String token:
                    this.sortedByTf) {
                fileWriter.write(token+"\t");
                fileWriter.write(this.tfInCorpus.get(token)+"\t");
                fileWriter.write(this.df.get(token)+"\t");
                fileWriter.write(this.idf.get(token)+"\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadModel(){
        int currentIndex = 0;
        try (Scanner scanner = new Scanner(new FileInputStream(bagOfWordsModelPath.toString()))) {
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] tokens = line.split("\t");
                this.sortedByTf.add(tokens[0]);
                this.vocabularySet.add(tokens[0]);
                this.tfIdfInCorpus.put(tokens[0], Double.valueOf(tokens[1]));
                this.df.put(tokens[0], Double.valueOf(tokens[2]));
                this.idf.put(tokens[0], Double.valueOf(tokens[3]));
                this.indices.put(tokens[0], currentIndex++);
            }
            this.vocabularySize = currentIndex;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public List<Sentence> getCorpusOfBOW() {
        return corpusOfBOW;
    }

    public Set<String> getVocabularySet() {
        return vocabularySet;
    }

    public HashMap<String, Double> getIdf() {
        return idf;
    }

    public HashMap<String, Double> getTfInCorpus() {
        return tfInCorpus;
    }

    public Boolean getDoLemmatize() {
        return doLemmatize;
    }

    public int getVocabularySize() {
        return vocabularySize;
    }

    public int getMaximumNoOfVocabulary() {
        return maximumNoOfVocabulary;
    }
}



