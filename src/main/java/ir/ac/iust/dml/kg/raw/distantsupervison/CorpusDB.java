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
    private HashMap<String, Integer> indices = new HashMap<>();
    private HashMap<Integer, String> invertedIndices = new HashMap<>();
    private int numberOfClasses = 0;

    public CorpusDB() {
        this.entries = new ArrayList<>();
    }

    public CorpusDB(List<CorpusEntryObject> entries) {
        this.entries = entries;
        shuffle();
        index();
    }

    public HashMap<String, Integer> getIndices() {
        return indices;
    }

    public void setIndices(HashMap<String, Integer> indices) {
        this.indices = indices;
    }

    public HashMap<Integer, String> getInvertedIndices() {
        return invertedIndices;
    }

    public void setInvertedIndices(HashMap<Integer, String> invertedIndices) {
        this.invertedIndices = invertedIndices;
    }

    public int getNumberOfClasses() {
        return numberOfClasses;
    }

    public void setNumberOfClasses(int numberOfClasses) {
        this.numberOfClasses = numberOfClasses;
    }

    public List<CorpusEntryObject> getShuffledEntries() {
        return shuffledEntries;
    }

    public void setShuffledEntries(List<CorpusEntryObject> shuffledEntries) {
        this.shuffledEntries = shuffledEntries;
    }

    public void addEntry(CorpusEntryObject corpusEntryObject) {
        this.entries.add(corpusEntryObject);
        if (!indices.containsKey(corpusEntryObject.getPredicate())) {
            indices.put(corpusEntryObject.getPredicate(), ++numberOfClasses);
            invertedIndices.put(numberOfClasses, corpusEntryObject.getPredicate());
        }
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
