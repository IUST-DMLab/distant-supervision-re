package ir.ac.iust.dml.kg.raw.distantsupervison;

import ir.ac.iust.dml.kg.raw.WordTokenizer;
import ir.ac.iust.dml.kg.resource.extractor.client.ExtractorClient;
import ir.ac.iust.dml.kg.resource.extractor.client.MatchedResource;

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
    private List<String> allQueryWords = new ArrayList<>();
    private int subjectIndex = -1;
    private int objectIndex = -1;

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

        allQueryWords = WordTokenizer.tokenize(getGeneralizedSentence());

        this.subjectIndex = allQueryWords.indexOf(Constants.sentenceAttribs.SUBJECT_ABV);
        this.objectIndex = allQueryWords.indexOf(Constants.sentenceAttribs.OBJECT_ABV);
    }

    public CorpusEntryObject(String sentenceString, String subject, String object, String predicate) {
        Sentence test = new Sentence(sentenceString);
        ExtractorClient client = new ExtractorClient(Configuration.extractorClient);
        List<MatchedResource> resultsForSubject = client.match(subject);
        List<MatchedResource> resultsForObject = client.match(object);
        List<String> subjectType = new ArrayList<>();
        if (resultsForSubject != null && !resultsForSubject.isEmpty()
                && resultsForSubject.get(0).getResource() != null)
            subjectType.addAll(resultsForSubject.get(0).getResource().getClassTree());
        List<String> objectType = new ArrayList<>();
        if (resultsForObject != null && !resultsForObject.isEmpty()
                && resultsForObject.get(0).getResource() != null)
            objectType.addAll(resultsForObject.get(0).getResource().getClassTree());

        String generalized = sentenceString.replace(subject, Constants.sentenceAttribs.SUBJECT_ABV);
        generalized = generalized.replace(object, Constants.sentenceAttribs.OBJECT_ABV);

        this.setOriginalSentence(test);
        this.setObject(object);
        this.setSubject(subject);
        this.setGeneralizedSentence(generalized);
        this.setSubjectType(subjectType);
        this.setObjectType(objectType);
        this.setPredicate(predicate);
    }

    public String getGeneralizedSentence() {
        return generalizedSentence;
    }

    public void setGeneralizedSentence(String generalizedSentence) {

        this.generalizedSentence = generalizedSentence;
        allQueryWords = WordTokenizer.tokenize(getGeneralizedSentence());

        this.subjectIndex = allQueryWords.indexOf(Constants.sentenceAttribs.SUBJECT_ABV);
        this.objectIndex = allQueryWords.indexOf(Constants.sentenceAttribs.OBJECT_ABV);

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

        int startIdx;
        int endIdx = this.subjectIndex;


        if (this.subjectIndex < this.objectIndex) {
            startIdx = (this.subjectIndex - Configuration.maxWindowSize < 0) ? 0 : this.subjectIndex - Configuration.maxWindowSize;
        } else {
            startIdx = this.objectIndex + 1;
        }

        if (startIdx > endIdx)
            words = new ArrayList<>();
        else
            words = allQueryWords.subList(startIdx, endIdx);
        return words;
    }

    public List<String> getSubjectFollowingWords() {
        List<String> words = new ArrayList<>();

        int startIdx = this.subjectIndex + 1;
        int endIdx;

        if (this.subjectIndex < this.objectIndex) {
            endIdx = this.objectIndex;
        } else {
            endIdx = (this.subjectIndex + Configuration.maxWindowSize >= allQueryWords.size()) ? allQueryWords.size() - 1 : this.subjectIndex + Configuration.maxWindowSize;
        }

        if (startIdx > endIdx)
            words = new ArrayList<>();
        else
            words = allQueryWords.subList(startIdx, endIdx);

        return words;
    }

    public List<String> getObjectPrecedingWords() {
        List<String> words = new ArrayList<>();

        int startIdx;
        int endIdx = this.objectIndex;


        if (this.objectIndex < this.subjectIndex) {
            startIdx = (this.objectIndex - Configuration.maxWindowSize < 0) ? 0 : this.objectIndex - Configuration.maxWindowSize;
        } else {
            startIdx = this.subjectIndex + 1;
        }

        if (startIdx > endIdx)
            words = new ArrayList<>();
        else
            words = allQueryWords.subList(startIdx, endIdx);

        return words;
    }

    public List<String> getObjectFollowingWords() {
        List<String> words = new ArrayList<>();

        int startIdx = this.objectIndex + 1;
        int endIdx;

        if (this.objectIndex < this.subjectIndex) {
            endIdx = this.subjectIndex;
        } else {
            endIdx = (this.objectIndex + Configuration.maxWindowSize >= allQueryWords.size()) ? allQueryWords.size() - 1 : this.objectIndex + Configuration.maxWindowSize;
        }

        if (startIdx > endIdx)
            words = new ArrayList<>();
        else
            words = allQueryWords.subList(startIdx, endIdx);


        return words;
    }

    public static void setEntityType(List<MatchedResource> result_entity, List<String> entityType) {
        if (result_entity == null || result_entity.size() == 0 || result_entity.get(0).getResource() == null)
            entityType.add("null");
        else if (result_entity.get(0).getResource().getClassTree() == null || result_entity.get(0).getResource().getClassTree().size() == 0)
            entityType.add(result_entity.get(0).getResource().getIri());
        else entityType.addAll(result_entity.get(0).getResource().getClassTree());
    }
}
