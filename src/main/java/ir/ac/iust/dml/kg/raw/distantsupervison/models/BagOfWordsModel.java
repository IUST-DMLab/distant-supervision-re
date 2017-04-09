package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import ir.ac.iust.dml.kg.raw.distantsupervison.Sentence;

import java.util.*;

import static java.lang.Math.log10;

/**
 * Created by hemmatan on 4/9/2017.
 */
public class BagOfWordsModel {

    private List<Sentence> corpusOfBOW = new ArrayList<>();
    private Set<String> vocabulary = new HashSet<>();
    private int vocabularySize;
    private double numberOfSentences;
    private HashMap<String, Double> idf = new HashMap<>();
    private HashMap<String, Double> tfInCorpus = new HashMap<>();
    private Boolean doLemmatize = false;

    public BagOfWordsModel(List<Sentence> corpusOfBOW, Boolean doLemmatize){
        this.doLemmatize = doLemmatize;
        this.numberOfSentences = corpusOfBOW.size();
        if (!doLemmatize){
            extractVocabulary(corpusOfBOW);
        }
    }

    private void extractVocabulary(List<Sentence> corpusOfBOW) {
        HashMap<String, Double> df = new HashMap<>();
        for (Sentence sentence:
             corpusOfBOW) {
            List<String> words = sentence.getWords();
            Set<String> uniqueWords = new HashSet<>();

            for (String queryWord:
                 words) {
                vocabulary.add(queryWord);
                uniqueWords.add(queryWord);
                if (!tfInCorpus.containsKey(queryWord))
                    tfInCorpus.put(queryWord, 1.0);
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
        this.vocabularySize = vocabulary.size();
        Iterator it = df.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            idf.put((String) pair.getKey(), log10(this.numberOfSentences/(Double) pair.getValue()));
        }
    }

    public HashMap<String, Double> compute_tf_idf(ArrayList<String> queryWords){
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

    public List<Sentence> getCorpusOfBOW() {
        return corpusOfBOW;
    }

    public Set<String> getVocabulary() {
        return vocabulary;
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
}
