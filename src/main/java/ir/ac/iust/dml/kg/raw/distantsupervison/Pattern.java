package ir.ac.iust.dml.kg.raw.distantsupervison;

import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleData;

/**
 * Created by hemmatan on 4/9/2017.
 */

public class Pattern {
    private String relationName;
    private int frequency;
    private String pattern;
    private String objectEntityType;
    private String subjectEntityType;

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getObjectEntityType() {
        return objectEntityType;
    }

    public void setObjectEntityType(String objectEntityType) {
        this.objectEntityType = objectEntityType;
    }

    public String getSubjectEntityType() {
        return subjectEntityType;
    }

    public void setSubjectEntityType(String subjectEntityType) {
        this.subjectEntityType = subjectEntityType;
    }

    public void extractPattern(String string, String subject, String object, String relationName){
        this.setRelationName(relationName);
        String curPattern = string.replaceAll(subject, "SUBJ");
        curPattern = curPattern.replaceAll(object, "OBJ");
        this.setPattern(curPattern);
    }

}
