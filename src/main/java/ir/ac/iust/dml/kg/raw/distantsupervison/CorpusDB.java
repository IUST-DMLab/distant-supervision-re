/*
 * Farsi Knowledge Graph Project
 *  Iran University of Science and Technology (Year 2017)
 *  Developed by Ensieh Hemmatan.
 */

package ir.ac.iust.dml.kg.raw.distantsupervison;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class CorpusDB {
    private List<CorpusEntryObject> entries = new ArrayList<>();
    private List<CorpusEntryObject> shuffledEntries = new ArrayList<>();
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
        } else
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

    public void deleteAll() {
        this.entries.clear();
        this.shuffledEntries.clear();
    }


    public void savePredicateIndices(String predicatesIndexFile) {
        try (Writer fileWriter = new FileWriter(predicatesIndexFile)) {
            Set<String> pos = this.getIndices().keySet();
            for (String s :
                pos) {
                fileWriter.write(s + "\t" + getIndices().get(s) + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPredicateIndices(String predicatesIndexFile) {
        this.indices.clear();
        this.invertedIndices.clear();
        try (Scanner scanner = new Scanner(this.getClass().getClassLoader().getResourceAsStream(predicatesIndexFile))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String pos = line.split("\t")[0];
                Double lastIdx = Double.parseDouble(line.split("\t")[1]);
                this.indices.put(pos, lastIdx);
                this.invertedIndices.put(lastIdx, pos);
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
