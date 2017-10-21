package ir.ac.iust.dml.kg.raw.distantsupervison;

/**
 * Created by hemmatan on 4/26/2017.
 */
public class Configuration {
    public static final String moduleName = "DistantSupervision";
    public static final String trainingSetMode = Constants.trainingSetModes.LOAD_PREDICATES_FROM_FILE;
    public static final boolean omitGoldDataFromTrainData = false;
    //public static final String trainCorpusName


    public static final int maximumNoOfVocabularyForBagOfWords = 500;
    public static long maximumNoOfInstancesForEachPredicate = 1000;
    public static final long initialMaximumNoOfInstancesForEachPredicate = 1000;
    public static int noOfTrainExamples;// = 33000;//21000;
    public static int noOfTestExamples;
    public static final int maximumNumberOfPredicatesToLoad = 60;
    public static final long maximumNumberOfTrainExamples = (maximumNumberOfPredicatesToLoad+1)*maximumNoOfInstancesForEachPredicate;
    //+1 is for negatives

    //(Integer) noOfTotalExamples/maximumNoOfInstancesForEachPredicate +1;

    public static final String extractorClient = "http://194.225.227.161:8094";
    public static final String ontologyClient = "http://194.225.227.161:8090";
    public static final String exportURL = "http://dmls.iust.ac.ir:8100/rest/v1/raw/export";

    public static final int maxWindowSize = 4;
    public static final double confidenceThreshold = 0.95;
    public static final int maxLengthForRawString = 2000;
    public static final float contextDisambiguationThreshold = 0.001f;


    public static class libLinearParams {
        public static final double costOfConstraintsViolation = 1.0;
        public static final double epsStoppingCriteria = 0.0001;
    }

    public static final String distantSupervisionDBName = "DistantSupervision";
    public static final String sentencesTableName = "sentences";
    public static final String corpusTableName = Constants.trainCorpusNames.WIKI_PLUS_DEPENDENCY_FEATURES;
    public static final String negativesTableName = "NegativeSentence";
    public static final String trainTableName = "train";

    public static final String[] classifierTypes = new String[]{
            Constants.classifierTypes.SPECIES_SPECIES,
            Constants.classifierTypes.ATHLETE_SPORTSTEAM,
            Constants.classifierTypes.PERSON_PERSONFUNCTION,
            Constants.classifierTypes.PERSON_PERSON,
            Constants.classifierTypes.PERSON_PLACE,
            Constants.classifierTypes.WORK_AGENT,
            Constants.classifierTypes.THING_PLACE,
            Constants.classifierTypes.THING_PERSON,
            Constants.classifierTypes.THING_AGENT,
            Constants.classifierTypes.GENERAL
    };




}
