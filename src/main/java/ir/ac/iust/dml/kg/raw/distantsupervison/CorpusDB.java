package ir.ac.iust.dml.kg.raw.distantsupervison;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hemmatan on 4/18/2017.
 */
public class CorpusDB {
    private List<CorpusEntryObject> entries;
    private List<CorpusEntryObject> shuffledEntries;
    private HashMap<String, Double> predicateCounts = new HashMap<>();
    private HashMap<String, Double> indices = new HashMap<>();
    private HashMap<Double, String> invertedIndices = new HashMap<>();
    private Double numberOfClasses = 0.0;

    public CorpusDB() {
        this.entries = new ArrayList<>();
    }

    public CorpusDB(List<CorpusEntryObject> entries) {
        this.entries = entries;
        shuffle();
        index();
    }

    public HashMap<String, Double> getIndices() {
        return indices;
    }

    public void setIndices(HashMap<String, Double> indices) {
        this.indices = indices;
    }

    public HashMap<Double, String> getInvertedIndices() {
        return invertedIndices;
    }

    public void setInvertedIndices(HashMap<Double, String> invertedIndices) {
        this.invertedIndices = invertedIndices;
    }

    public Double getNumberOfClasses() {
        return numberOfClasses;
    }

    public void setNumberOfClasses(Double numberOfClasses) {
        this.numberOfClasses = numberOfClasses;
    }

    public List<CorpusEntryObject> getShuffledEntries() {
        return shuffledEntries;
    }

    public void setShuffledEntries(List<CorpusEntryObject> shuffledEntries) {
        this.shuffledEntries = shuffledEntries;
    }

    public HashMap<String, Double> getPredicateCounts() { return predicateCounts; }

    public void setPredicateCounts(HashMap<String, Double> predicateCounts) { this.predicateCounts = predicateCounts; }

    public void addEntry(CorpusEntryObject corpusEntryObject) {
        this.entries.add(corpusEntryObject);
        if (!indices.containsKey(corpusEntryObject.getPredicate())) {
            indices.put(corpusEntryObject.getPredicate(), ++numberOfClasses);
            invertedIndices.put(numberOfClasses, corpusEntryObject.getPredicate());
            predicateCounts.put(corpusEntryObject.getPredicate(), 1.0);
        }
        else
            predicateCounts.put(corpusEntryObject.getPredicate(),
                    predicateCounts.get(corpusEntryObject.getPredicate())+1);
    }

    public List<CorpusEntryObject> getEntries() {
        return entries;
    }

    public void setEntries(List<CorpusEntryObject> entries) {
        this.entries = entries;
    }

    public void shuffle() {
        this.shuffledEntries = new ArrayList<>(this.entries);
        Collections.shuffle(shuffledEntries);
    }

    public void index() {
        for (CorpusEntryObject corpusEntryObject :
                this.entries) {
            if (!indices.containsKey(corpusEntryObject.getPredicate())) {
                indices.put(corpusEntryObject.getPredicate(), ++numberOfClasses);
                invertedIndices.put(numberOfClasses, corpusEntryObject.getPredicate());
            }
        }
    }

}
