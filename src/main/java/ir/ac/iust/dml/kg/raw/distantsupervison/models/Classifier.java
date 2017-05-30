package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import de.bwaldvogel.liblinear.*;
import edu.stanford.nlp.ling.TaggedWord;
import ir.ac.iust.dml.kg.raw.POSTagger;
import ir.ac.iust.dml.kg.raw.WordTokenizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.*;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.CorpusDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.DbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.SentenceDbHandler;
import ir.ac.iust.dml.kg.resource.extractor.client.ExtractorClient;
import ir.ac.iust.dml.kg.resource.extractor.client.MatchedResource;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpusDB;

/**
 * Created by hemmatan on 4/10/2017.
 */
public class Classifier {


    private int defaultMaximumNoOfVocabularyForBOW = Configuration.maximumNoOfVocabularyForBagOfWords;
    private int numberOfFeatures;
    private String modelFilePath = "tempTestModel";
    private String predicatesIndexFile = "predicates.txt";
    private BagOfWordsModel bagOfWordsModel;
    private HashMap<String, SegmentedBagOfWords> segmentedBagOfWordsHashMap = new HashMap<>();
    private EntityTypeModel entityTypeModel;
    private PartOfSpeechModel partOfSpeechModel;
    private CorpusDB trainData = new CorpusDB();
    private CorpusDB testData = new CorpusDB();

    public Classifier(){
        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        sentenceDbHandler.loadSentenceTable();

        CorpusDbHandler trainDbHandler = new CorpusDbHandler(DbHandler.trainTableName);
        trainDbHandler.load(trainData);
    }


    public void createTrainData(int numberOfTrainingExamples, int numberOfTestExamples,
                                CorpusDbHandler trainDbHandler) {
        int totalNoOfData = numberOfTestExamples + numberOfTrainingExamples;

        trainDbHandler.deleteAll();
        trainData.deleteAll();

        CorpusDbHandler corpusDbHandler = new CorpusDbHandler(DbHandler.corpusTableName);
        if (Configuration.getPredicatesFromFile)
            corpusDbHandler.loadByReadingPedicatesFromFile(corpusDB, totalNoOfData);
        else
            corpusDbHandler.loadByMostFrequentPredicates(corpusDB, totalNoOfData);
        corpusDB.shuffle();

        for (int i = 0; i < numberOfTrainingExamples; i++) {
            System.out.println(i + "\t" + corpusDB.getShuffledEntries().get(i).toString());
            trainData.addEntry(corpusDB.getShuffledEntries().get(i));
        }
        trainDbHandler.insertAll(trainData.getEntries());
    }


    public void train(int numberOfTrainingExamples, int numberOfTestExamples, boolean buildTrainDataFromScratch) {


        Problem problem = new Problem();
        problem.l = numberOfTrainingExamples; // number of training examples
        FeatureNode[][] featureNodes = new FeatureNode[problem.l][];
        problem.y = new double[problem.l];// target values


        CorpusDbHandler trainDbHandler = new CorpusDbHandler(DbHandler.trainTableName);

        if (buildTrainDataFromScratch) {
            createTrainData(numberOfTrainingExamples, numberOfTestExamples, trainDbHandler);
            createAndSaveTestData(problem, numberOfTestExamples);

        } else {
            trainDbHandler.load(trainData);
        }

        trainDbHandler.close();

        initializeModels(true);
        numberOfFeatures = 4 * this.segmentedBagOfWordsHashMap.get(Constants.segmentedBagOfWordsAttribs.OBJECT_FOLLOWING).getMaximumNoOfVocabulary() +
                + 2 * this.entityTypeModel.getNoOfEntityTypes() + 2 * this.partOfSpeechModel.getNoOfPOS();
        problem.n = this.numberOfFeatures;// number of features

        for (int i = 0; i < problem.l; i++) {
            CorpusEntryObject corpusEntryObject = trainData.getEntries().get(i);
            //featureNodes[i] = FeatureExtractor.createFeatureNode(bagOfWordsModel, entityTypeModel, partOfSpeechModel, corpusEntryObject);
            featureNodes[i] = FeatureExtractor.createFeatureNode(segmentedBagOfWordsHashMap, entityTypeModel, partOfSpeechModel, corpusEntryObject);
            problem.y[i] = trainData.getIndices().get(corpusEntryObject.getPredicate());
        }


        System.out.print(trainData.getIndices());

        System.out.print(numberOfFeatures);

        problem.x =  featureNodes;// feature nodes

        SolverType solver = SolverType.L2R_LR; // -s 0
        double C = 1.0;    // cost of constraints violation
        double eps = 0.1; // stopping criteria

        Parameter parameter = new Parameter(solver, C, eps);

        File modelFile = new File(this.modelFilePath);


        trainData.savePredicateIndices(this.predicatesIndexFile);
        Model model = Linear.train(problem, parameter);

        try {
            model.save(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void buildSegmentedBowModel() {
        SegmentedBagOfWords segmentedBagOfWords = new SegmentedBagOfWords(trainData.getEntries(), Constants.segmentedBagOfWordsAttribs.SUBJECT_PRECEDING,
                false, defaultMaximumNoOfVocabularyForBOW / 4);
        segmentedBagOfWordsHashMap.put(Constants.segmentedBagOfWordsAttribs.SUBJECT_PRECEDING, segmentedBagOfWords);

        segmentedBagOfWords = new SegmentedBagOfWords(trainData.getEntries(), Constants.segmentedBagOfWordsAttribs.SUBJECT_FOLLOWING,
                false, defaultMaximumNoOfVocabularyForBOW / 4);
        segmentedBagOfWordsHashMap.put(Constants.segmentedBagOfWordsAttribs.SUBJECT_FOLLOWING, segmentedBagOfWords);

        segmentedBagOfWords = new SegmentedBagOfWords(trainData.getEntries(), Constants.segmentedBagOfWordsAttribs.OBJECT_PRECEDING,
                false, defaultMaximumNoOfVocabularyForBOW / 4);
        segmentedBagOfWordsHashMap.put(Constants.segmentedBagOfWordsAttribs.OBJECT_PRECEDING, segmentedBagOfWords);

        segmentedBagOfWords = new SegmentedBagOfWords(trainData.getEntries(), Constants.segmentedBagOfWordsAttribs.OBJECT_FOLLOWING,
                false, defaultMaximumNoOfVocabularyForBOW / 4);
        segmentedBagOfWordsHashMap.put(Constants.segmentedBagOfWordsAttribs.OBJECT_FOLLOWING, segmentedBagOfWords);
    }

    private void loadSegmentedBowModel() {
        SegmentedBagOfWords segmentedBagOfWords = new SegmentedBagOfWords(Constants.segmentedBagOfWordsAttribs.SUBJECT_PRECEDING);
        segmentedBagOfWords.loadModel();
        segmentedBagOfWordsHashMap.put(Constants.segmentedBagOfWordsAttribs.SUBJECT_PRECEDING, segmentedBagOfWords);

        segmentedBagOfWords = new SegmentedBagOfWords(Constants.segmentedBagOfWordsAttribs.SUBJECT_FOLLOWING);
        segmentedBagOfWords.loadModel();
        segmentedBagOfWordsHashMap.put(Constants.segmentedBagOfWordsAttribs.SUBJECT_FOLLOWING, segmentedBagOfWords);

        segmentedBagOfWords = new SegmentedBagOfWords(Constants.segmentedBagOfWordsAttribs.OBJECT_PRECEDING);
        segmentedBagOfWords.loadModel();
        segmentedBagOfWordsHashMap.put(Constants.segmentedBagOfWordsAttribs.OBJECT_PRECEDING, segmentedBagOfWords);

        segmentedBagOfWords = new SegmentedBagOfWords(Constants.segmentedBagOfWordsAttribs.OBJECT_FOLLOWING);
        segmentedBagOfWords.loadModel();
        segmentedBagOfWordsHashMap.put(Constants.segmentedBagOfWordsAttribs.OBJECT_FOLLOWING, segmentedBagOfWords);
    }

    public void loadModels() {
        entityTypeModel = new EntityTypeModel(true);
        partOfSpeechModel = new PartOfSpeechModel();
        partOfSpeechModel.loadModel();
        loadSegmentedBowModel();
        trainData.loadPredicateIndices(this.predicatesIndexFile);
    }

    public void initializeModels(boolean train) {
        entityTypeModel = new EntityTypeModel(false);
        partOfSpeechModel = new PartOfSpeechModel();
        for (int i = 0; i < trainData.getEntries().size(); i++) {
            CorpusEntryObject corpusEntryObject = trainData.getEntries().get(i);
            partOfSpeechModel.addToModel(corpusEntryObject.getOriginalSentence().getPosTagged());
        }
        partOfSpeechModel.saveModel();
        bagOfWordsModel = new BagOfWordsModel();//(corpus.getSentences(), false, defaultMaximumNoOfVocabularyForBOW);
        buildSegmentedBowModel();

        System.out.println(segmentedBagOfWordsHashMap.get(Constants.segmentedBagOfWordsAttribs.OBJECT_FOLLOWING).getIndices());

    }

    private void createAndSaveTestData(Problem problem, int numberOfTestExamples) {
        CorpusDbHandler testDbHandler = new CorpusDbHandler(DbHandler.testTableName);
        testDbHandler.deleteAll();

        for (int i = problem.l; i < corpusDB.getShuffledEntries().size(); i++) {
            System.out.println(i + "\t" + "test: " + corpusDB.getShuffledEntries().get(i).toString());
            testData.addEntry(corpusDB.getShuffledEntries().get(i));
        }
        testData.shuffle();
        testDbHandler.insertAll(testData.getEntries());

        /*try (Writer fileWriter = new FileWriter(this.testDataFile)) {
            CorpusEntryObject currentTestData;
            for (int i = problem.l; i<problem.l+numberOfTestExamples; i++){
                currentTestData = corpusDB.getShuffledEntries().get(i);
                testData.addEntry(currentTestData);
                fileWriter.write(currentTestData.getOriginalSentence().getRaw()+"\t");
                fileWriter.write(currentTestData.getGeneralizedSentence()+"\t");
                fileWriter.write(currentTestData.getSubject()+"\t");
                fileWriter.write(currentTestData.getObject()+"\t");
                fileWriter.write(currentTestData.getPredicate()+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public boolean ignoreEntity(MatchedResource matchedResource, Sentence test) {
        boolean sw = false;
        TaggedWord pos = POSTagger.tag(Arrays.asList(test.getRaw().split(" ")[matchedResource.getStart()])).get(0);


        sw = (matchedResource.getResource() == null ||
                matchedResource.getResource().getClassTree() == null ||
                matchedResource.getResource().getClassTree().size() == 0 ||
                (matchedResource.getResource().getType() != null &&
                        matchedResource.getResource().getType().equalsIgnoreCase("property")) ||
                (matchedResource.getEnd() == matchedResource.getStart() &&
                        pos.tag().equalsIgnoreCase("P"))
        );


        return sw;
    }

    public void testForSingleSentenceStringAndTriple(String sentenceString, String subject, String object) {
        Sentence test = new Sentence(sentenceString);
        ExtractorClient client = new ExtractorClient(Configuration.extractorClient);
        List<MatchedResource> resultsForSubject = client.match(subject);
        List<MatchedResource> resultsForObject = client.match(object);


        //Feature[] instance = bagOfWordsModel.createBowLibLinearFeatureNodeForQueryWithWindow(test.getWords());
        Model model = null;
        File modelFile = new File(this.modelFilePath);
        try {
            model = Linear.loadModel(modelFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> subjectType = new ArrayList<>();
        if (resultsForSubject != null && !resultsForSubject.isEmpty())
            subjectType.addAll(resultsForSubject.get(0).getResource().getClassTree());
        List<String> objectType = new ArrayList<>();
        if (resultsForObject != null && !resultsForObject.isEmpty())
            objectType.addAll(resultsForObject.get(0).getResource().getClassTree());

        String generalized = sentenceString.replace(subject, Constants.sentenceAttribs.SUBJECT_ABV);
        generalized = generalized.replace(object, Constants.sentenceAttribs.OBJECT_ABV);

        CorpusEntryObject corpusEntryObject = new CorpusEntryObject();
        corpusEntryObject.setOriginalSentence(test);
        corpusEntryObject.setObject(object);
        corpusEntryObject.setSubject(subject);
        corpusEntryObject.setGeneralizedSentence(generalized);
        corpusEntryObject.setSubjectType(subjectType);
        corpusEntryObject.setObjectType(objectType);

        Feature[] instance = FeatureExtractor.createFeatureNode(segmentedBagOfWordsHashMap, entityTypeModel, partOfSpeechModel, corpusEntryObject);

        double[] probs = new double[model.getNrClass()];
        double prediction = Linear.predictProbability(model, instance, probs);
        List a = Arrays.asList(ArrayUtils.toObject(probs));

        if ((double) Collections.max(a) > Configuration.confidenceThreshold) {
            System.out.println(subjectType);
            System.out.println(objectType);
            System.out.println("\n" + "Subject: " + subject + " " + "\n" + "Object: " + object + " " + "\n" + "Predicate: " + trainData.getInvertedIndices().get(prediction));
            System.out.println("Prediction number: " + prediction);
            System.out.println("Confidence: " + Collections.max(a));
        }

        System.out.print(trainData.getIndices());

    }

    public void testForSingleSentenceString(String sentenceString){

        Sentence test = new Sentence(sentenceString);
        ExtractorClient client = new ExtractorClient(Configuration.extractorClient);
        List<MatchedResource> results = client.match(sentenceString);


        //Feature[] instance = bagOfWordsModel.createBowLibLinearFeatureNodeForQueryWithWindow(test.getWords());
        Model model = null;
        File modelFile = new File(this.modelFilePath);
        try {
            model = Linear.loadModel(modelFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i<results.size(); i++) {

            if (ignoreEntity(results.get(i), test))
                continue;

            for (int j = i+1; j<results.size(); j++){

                if (ignoreEntity(results.get(j), test))
                    continue;

                CorpusEntryObject corpusEntryObject = new CorpusEntryObject();
                corpusEntryObject.setOriginalSentence(test);


                int subjectStart = results.get(i).getStart();
                int subjectEnd = results.get(i).getEnd();
                //String subject = test.getWords().get(subjectStart);
                String subject = test.getRaw().split(" ")[subjectStart];

                int objectStart = results.get(j).getStart();
                int objectEnd = results.get(j).getEnd();
                String object = test.getRaw().split(" ")[objectStart];
                //String object = test.getWords().get(objectStart);


                String jomle = new Sentence(sentenceString).getNormalized();
                jomle = jomle.replace(test.getWords().get(objectStart), Constants.sentenceAttribs.OBJECT_ABV);
                jomle = jomle.replace(test.getWords().get(subjectStart), Constants.sentenceAttribs.SUBJECT_ABV);
                List<String> words = WordTokenizer.tokenize(jomle);
                for (int o = objectStart+1; o<=objectEnd; o++){
                    jomle = jomle.replace(test.getWords().get(o), "");
                    words.remove(o);
                }
                for (int s = subjectStart+1; s<=subjectEnd; s++){
                    jomle = jomle.replace(test.getWords().get(s), "");
                    words.remove(s);
                }

                corpusEntryObject.setGeneralizedSentence(jomle);

                List<String> subjectType = new ArrayList<>();
                subjectType.addAll(results.get(i).getResource().getClassTree());
                List<String> objectType = new ArrayList<>();
                objectType.addAll(results.get(j).getResource().getClassTree());

                /*boolean cntsw1 = false;
                for (String type:
                     subjectType) {
                    if (entityTypeModel.getEntityIndex().keySet().contains(type))
                        cntsw1 = true;
                }
                boolean cntsw2 = false;
                for (String type:
                        objectType) {
                    if (entityTypeModel.getEntityIndex().keySet().contains(type))
                        cntsw2 = true;
                }

                if (!cntsw1 || !cntsw2)
                    continue;*/


                corpusEntryObject.setSubjectType(subjectType);
                corpusEntryObject.setObjectType(objectType);
                corpusEntryObject.setSubject(subject);
                corpusEntryObject.setObject(object);

                Feature[] instance = FeatureExtractor.createFeatureNode(segmentedBagOfWordsHashMap, entityTypeModel, partOfSpeechModel, corpusEntryObject);

                double[] probs = new double[model.getNrClass()];
                double prediction = Linear.predictProbability(model, instance, probs);
                List a = Arrays.asList(ArrayUtils.toObject(probs));

                if ((double) Collections.max(a) > Configuration.confidenceThreshold) {
                    System.out.println(subjectType);
                    System.out.println(objectType);
                    System.out.println("\n" + "Subject: " + subject + " " + results.get(i).getResource().getLabel() + "\n" + "Object: " + object + " " + results.get(j).getResource().getLabel() + "\n" + "Predicate: " + trainData.getInvertedIndices().get(prediction));
                    System.out.println("Prediction number: " + prediction);
                    System.out.println("Confidence: " + Collections.max(a));
                }

                System.out.print(trainData.getIndices());

            }
        }


    }

}
