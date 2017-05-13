package ir.ac.iust.dml.kg.raw.distantsupervison;

import ir.ac.iust.dml.kg.raw.WordTokenizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hemmatan on 4/18/2017.
 */
public class CorpusEntryObject {

    private Sentence originalSentence;
    private String generalizedSentence;
    private String object, subject;
    private List<String> objectType, subjectType;
    private String predicate;
    private int occurrence;

    public CorpusEntryObject() {
    }

    public CorpusEntryObject(Sentence originalSentence, String generalizedSentence, String object, String subject, List<String> objectType, List<String> subjectType, String predicate, int occurrence) {
        this.originalSentence = originalSentence;
        this.generalizedSentence = generalizedSentence;
        this.object = object;
        this.subject = subject;
        this.objectType = objectType;
        this.subjectType = subjectType;
        this.predicate = predicate;
        this.occurrence = occurrence;
    }

    public String getGeneralizedSentence() {
        return generalizedSentence;
    }

    public void setGeneralizedSentence(String generalizedSentence) {
        this.generalizedSentence = generalizedSentence;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public Sentence getOriginalSentence() {

        return originalSentence;
    }

    public void setOriginalSentence(Sentence originalSentence) {
        this.originalSentence = originalSentence;
    }

    public int getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(int occurrence) {
        this.occurrence = occurrence;
    }

    public List<String> getObjectType() {
        return objectType;
    }

    public void setObjectType(List<String> objectType) {
        this.objectType = objectType;
    }

    public List<String> getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(List<String> subjectType) {
        this.subjectType = subjectType;
    }

    public String toString() {
        return getOriginalSentence().getNormalized()
                + "\t" + this.getSubject()
                + "\t" + this.getObject()
                + "\t" + "predicate: " + this.getPredicate() + "\n";
    }

    public List<String> getSubjectPrecedingWords() {
        List<String> words = new ArrayList<>();

        List<String> allQueryWords = WordTokenizer.tokenize(getGeneralizedSentence());
        int subjIdx = allQueryWords.indexOf(Constants.sentenceAttribs.SUBJECT_ABV);
        int objIdx = allQueryWords.indexOf(Constants.sentenceAttribs.OBJECT_ABV);
        int startIdx;
        int endIdx = subjIdx;


        if (subjIdx < objIdx) {
            startIdx = (subjIdx - Configuration.maxWindowSize < 0) ? 0 : subjIdx - Configuration.maxWindowSize;
        } else {
            startIdx = objIdx + 1;
        }

        if (startIdx > endIdx)
            words = new ArrayList<>();
        else
            words = allQueryWords.subList(startIdx, endIdx);
        return words;
    }

    public List<String> getSubjectFollowingWords() {
        List<String> words = new ArrayList<>();

        List<String> allQueryWords = WordTokenizer.tokenize(getGeneralizedSentence());
        int subjIdx = allQueryWords.indexOf(Constants.sentenceAttribs.SUBJECT_ABV);
        int objIdx = allQueryWords.indexOf(Constants.sentenceAttribs.OBJECT_ABV);
        int startIdx = subjIdx + 1;
        int endIdx;

        if (subjIdx < objIdx) {
            endIdx = objIdx;
        } else {
            endIdx = (subjIdx + Configuration.maxWindowSize >= allQueryWords.size()) ? allQueryWords.size() - 1 : subjIdx + Configuration.maxWindowSize;
        }

        if (startIdx > endIdx)
            words = new ArrayList<>();
        else
            words = allQueryWords.subList(startIdx, endIdx);

        return words;
    }

    public List<String> getObjectPrecedingWords() {
        List<String> words = new ArrayList<>();

        List<String> allQueryWords = WordTokenizer.tokenize(getGeneralizedSentence());
        int subjIdx = allQueryWords.indexOf(Constants.sentenceAttribs.SUBJECT_ABV);
        int objIdx = allQueryWords.indexOf(Constants.sentenceAttribs.OBJECT_ABV);
        int startIdx;
        int endIdx = objIdx;


        if (objIdx < subjIdx) {
            startIdx = (objIdx - Configuration.maxWindowSize < 0) ? 0 : objIdx - Configuration.maxWindowSize;
        } else {
            startIdx = subjIdx + 1;
        }

        if (startIdx > endIdx)
            words = new ArrayList<>();
        else
            words = allQueryWords.subList(startIdx, endIdx);

        return words;
    }

    public List<String> getObjectFollowingWords() {
        List<String> words = new ArrayList<>();

        List<String> allQueryWords = WordTokenizer.tokenize(getGeneralizedSentence());
        int subjIdx = allQueryWords.indexOf(Constants.sentenceAttribs.SUBJECT_ABV);
        int objIdx = allQueryWords.indexOf(Constants.sentenceAttribs.OBJECT_ABV);
        int startIdx = objIdx + 1;
        int endIdx;

        if (objIdx < subjIdx) {
            endIdx = subjIdx;
        } else {
            endIdx = (objIdx + Configuration.maxWindowSize >= allQueryWords.size()) ? allQueryWords.size() - 1 : objIdx + Configuration.maxWindowSize;
        }

        if (startIdx > endIdx)
            words = new ArrayList<>();
        else
            words = allQueryWords.subList(startIdx, endIdx);


        return words;
    }
}
