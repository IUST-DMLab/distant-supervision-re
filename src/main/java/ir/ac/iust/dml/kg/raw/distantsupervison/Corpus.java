/*
 * Farsi Knowledge Graph Project
 *  Iran University of Science and Technology (Year 2017)
 *  Developed by Ensieh Hemmatan.
 */

package ir.ac.iust.dml.kg.raw.distantsupervison;

import java.util.ArrayList;
import java.util.List;

public class Corpus {
    private List<Sentence> sentences;

    public Corpus(){
        sentences = new ArrayList<Sentence>();
    }

    public void addSentence(Sentence sentence){
        sentences.add(sentence);
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

    public void clear() {
        this.sentences.clear();
    }
}
