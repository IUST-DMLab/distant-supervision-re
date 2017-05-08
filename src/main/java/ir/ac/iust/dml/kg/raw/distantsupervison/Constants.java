package ir.ac.iust.dml.kg.raw.distantsupervison;

/**
 * Created by hemmatan on 4/5/2017.
 */
public class Constants {

    public static class sentenceAttribs {
        public static final String RAW = "raw";
        public static final String NORMALIZED = "normalized";
        public static final String WORDS = "words";
        public static final String POSTAG = "postag";
        public static final String OBJECT_ABV = "$OBJ";
        public static final String SUBJECT_ABV = "$SUBJ";
    }

    public static class patternAttribs {
        public static final String RELATION_NAME = "relation_name";
        public static final String FREQUENCY = "frequency";
        public static final String PATTERN = "pattern";
        public static final String OBJECT_ENTITY_TYPE = "object_entity_type";
        public static final String SUBJECT_ENTITY_TYPE = "subject_entity_type";
    }

    public static class bagOfWordsAttribs {
        public static final String TF = "tf";
        public static final String IDF = "idf";
        public static final String TF_IDF = "tf_idf";
        public static final String TOKEN = "token";
    }

    public static class corpusDbEntryAttribs {
        public static final String GENERALIZED_SENTENCE = "generalized_sentence";
        public static final String OCCURRENCE = "occurrence";
        public static final String SUBJECT = "subject";
        public static final String OBJECT = "object";
        public static final String SUBJECT_TYPE = "subject_type";
        public static final String OBJECT_TYPE = "object_type";
        public static final String PREDICATE = "predicate";
    }
}
