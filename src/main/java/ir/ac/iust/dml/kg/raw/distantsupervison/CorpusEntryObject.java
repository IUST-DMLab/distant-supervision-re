package ir.ac.iust.dml.kg.raw.distantsupervison;

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
}
