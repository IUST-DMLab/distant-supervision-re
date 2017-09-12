package ir.ac.iust.dml.kg.raw.distantsupervison;

/**
 * Created by hemmatan on 4/26/2017.
 */
public class Configuration {
    public static final String moduleName = "DistantSupervision";
    public static final String trainingSetMode = Constants.trainingSetModes.USE_ALL_PREDICATES_IN_EXPORTS_JSON;
    //public static final String trainCorpusName


    public static final int maximumNoOfVocabularyForBagOfWords = 3600;
    public static final int maximumNumberOfTrainExamples = 36000;
    public static final int maximumNoOfInstancesForEachPredicate = 700;
    public static int noOfTrainExamples;// = 33000;//21000;
    public static int noOfTestExamples;
    public static final int maximumNumberOfPredicatesToLoad = 50;
    //(Integer) noOfTotalExamples/maximumNoOfInstancesForEachPredicate +1;

    public static final String extractorClient = "http://194.225.227.161:8094";
    public static final String ontologyClient = "http://194.225.227.161:8090";
    public static final String exportURL = "http://dmls.iust.ac.ir:8100/rest/v1/raw/export";

    public static final int maxWindowSize = 5;
    public static final double confidenceThreshold = 0.5;
    public static final int maxLengthForRawString = 2000;
    public static final float contextDisambiguationThreshold = 0.001f;
    public static class libLinearParams {
        public static final double costOfConstraintsViolation = 1.0;
        public static final double epsStoppingCriteria = 0.05;
    }

    public static final String distantSupervisionDBName = "DistantSupervision";
    public static final String sentencesTableName = "sentences";
    public static final String corpusTableName = Constants.trainCorpusNames.WIKI_PLUS_DEPENDENCY_MINUS_FREQUENT_TRIPLES;
    public static final String trainTableName = "train";




}
