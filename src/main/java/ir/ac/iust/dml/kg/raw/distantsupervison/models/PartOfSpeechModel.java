package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import java.io.*;
import java.util.*;

/**
 * Created by hemmatan on 5/8/2017.
 */
public class PartOfSpeechModel {
    private String posFile = "posModel.txt";
    private int noOfPOS = 0;
    private Set<String> partsOfSpeech = new HashSet<>();
    private HashMap<String, Integer> posIndex = new HashMap<>();
    private HashMap<Integer, String> posInvertedIndex = new HashMap<>();

    public PartOfSpeechModel() {

    }

    public void addToModel(List<String> posTagged) {
        int lastIdx = noOfPOS;
        for (String pos :
                posTagged) {
            if (!posIndex.containsKey((pos))) {
                posIndex.put(pos, lastIdx);
                posInvertedIndex.put(lastIdx, pos);
                lastIdx++;
            }
        }
        noOfPOS = posIndex.keySet().size();
        partsOfSpeech = posIndex.keySet();
    }

    public void saveModel() {
        try (Writer fileWriter = new FileWriter(this.posFile)) {
            Set<String> pos = this.posIndex.keySet();
            for (String s :
                    pos) {
                fileWriter.write(s + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadModel() {
        try (Scanner scanner = new Scanner(new FileInputStream(this.posFile))) {
            int lastIdx = 0;
            while (scanner.hasNextLine()) {
                String pos = scanner.nextLine();
                posIndex.put(pos, lastIdx);
                posInvertedIndex.put(lastIdx, pos);
                lastIdx++;
            }
            noOfPOS = posIndex.keySet().size();
            partsOfSpeech = posIndex.keySet();
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getNoOfPOS() {
        return noOfPOS;
    }

    public void setNoOfPOS(int noOfPOS) {
        this.noOfPOS = noOfPOS;
    }

    public Set<String> getPartsOfSpeech() {
        return partsOfSpeech;
    }

    public void setPartsOfSpeech(Set<String> partsOfSpeech) {
        this.partsOfSpeech = partsOfSpeech;
    }

    public HashMap<String, Integer> getPosIndex() {
        return posIndex;
    }

    public void setPosIndex(HashMap<String, Integer> posIndex) {
        this.posIndex = posIndex;
    }

    public HashMap<Integer, String> getPosInvertedIndex() {
        return posInvertedIndex;
    }

    public void setPosInvertedIndex(HashMap<Integer, String> posInvertedIndex) {
        this.posInvertedIndex = posInvertedIndex;
    }
}
