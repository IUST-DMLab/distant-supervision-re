package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import de.bwaldvogel.liblinear.*;
import ir.ac.iust.dml.kg.raw.WordTokenizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.*;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.CorpusDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.SentenceDbHandler;
import ir.ac.iust.dml.kg.resource.extractor.client.ExtractorClient;
import ir.ac.iust.dml.kg.resource.extractor.client.MatchedResource;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpus;
import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpusDB;

/**
 * Created by hemmatan on 4/10/2017.
 */
public class Classifier {


    private int defaultMaximumNoOfVocabularyForBOW = Configuration.maximumNoOfVocabularyForBagOfWords;
    private int numberOfFeatures;
    private String modelFilePath = "tempTestModel";
    private String testDataFile = "testData.txt";
    private BagOfWordsModel bagOfWordsModel;
    private HashMap<String, SegmentedBagOfWords> segmentedBagOfWordsHashMap = new HashMap<>();
    private EntityTypeModel entityTypeModel;
    private PartOfSpeechModel partOfSpeechModel;
    private CorpusDB trainData = new CorpusDB();
    private CorpusDB testData = new CorpusDB();

    public Classifier(){
        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        sentenceDbHandler.loadSentenceTable();
    }

    public Classifier(int maximumNoOfVocabularyForBOW){
        entityTypeModel = new EntityTypeModel();
        bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false, maximumNoOfVocabularyForBOW);
    }

    public Classifier(String modelFilePath, BagOfWordsModel bagOfWordsModel) {
        this.modelFilePath = modelFilePath;
        this.bagOfWordsModel = bagOfWordsModel;
        entityTypeModel = new EntityTypeModel();
    }

    public Classifier(boolean createBagOfWordsModel, int maximumNoOfVocabularyForBOW) {
        if (createBagOfWordsModel)
            bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false, maximumNoOfVocabularyForBOW);
        else{
            bagOfWordsModel = new BagOfWordsModel();
            bagOfWordsModel.loadModel();
        }
        entityTypeModel = new EntityTypeModel();
    }

    public String getModelFilePath() {
        return modelFilePath;
    }

    public void setModelFilePath(String modelFilePath) {
        this.modelFilePath = modelFilePath;
    }

    public BagOfWordsModel getBagOfWordsModel() {
        return bagOfWordsModel;
    }

    public void setBagOfWordsModel(BagOfWordsModel bagOfWordsModel) {
        this.bagOfWordsModel = bagOfWordsModel;
    }

    public EntityTypeModel getEntityTypeModel() {
        return entityTypeModel;
    }


    public void train(int numberOfTrainingExamples, int numberOfTestExamples, boolean buildTrainDataFromScratch) {


        Problem problem = new Problem();
        problem.l =  numberOfTrainingExamples; // number of training examples
        FeatureNode[][] featureNodes = new FeatureNode[problem.l][];
        problem.y = new double[problem.l];// target values


        int totalNoOfData = numberOfTestExamples + numberOfTrainingExamples;
        CorpusDbHandler trainDbHandler = new CorpusDbHandler("train");


        if (buildTrainDataFromScratch) {

            trainDbHandler.deleteAll();

            CorpusDbHandler corpusDbHandler = new CorpusDbHandler("corpus");
            corpusDbHandler.loadByMostFrequentPredicates(corpusDB, totalNoOfData);
            corpusDB.shuffle();

            for (int i = 0; i < problem.l; i++) {
                System.out.println(i + "\t" + corpusDB.getShuffledEntries().get(i).toString());
                trainData.addEntry(corpusDB.getShuffledEntries().get(i));
            }
            trainData.shuffle();
            trainDbHandler.insertAll(trainData.getEntries());
        } else {
            trainDbHandler.load(trainData);
        }

        initializeModels(true);


        numberOfFeatures = 4 * this.segmentedBagOfWordsHashMap.get(Constants.segmentedBagOfWordsAttribs.OBJECT_FOLLOWING).getMaximumNoOfVocabulary() +
                + 2 * this.entityTypeModel.getNoOfEntityTypes() + 2 * this.partOfSpeechModel.getNoOfPOS();
        problem.n = this.numberOfFeatures;// number of features

        for (int i = 0; i < problem.l; i++) {
            CorpusEntryObject corpusEntryObject = corpusDB.getShuffledEntries().get(i);
            //featureNodes[i] = FeatureExtractor.createFeatureNode(bagOfWordsModel, entityTypeModel, partOfSpeechModel, corpusEntryObject);
            featureNodes[i] = FeatureExtractor.createFeatureNode(segmentedBagOfWordsHashMap, entityTypeModel, partOfSpeechModel, corpusEntryObject);
            problem.y[i] = corpusDB.getIndices().get(corpusEntryObject.getPredicate());
        }

        createAndSaveTestData(problem, numberOfTestExamples);

        problem.x =  featureNodes;// feature nodes

        SolverType solver = SolverType.L2R_LR; // -s 0
        double C = 1.0;    // cost of constraints violation
        double eps = 0.1; // stopping criteria

        Parameter parameter = new Parameter(solver, C, eps);

        File modelFile = new File(this.modelFilePath);
        Model model = Linear.train(problem, parameter);

        try {
            model.save(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void buildSegmentedBowModel() {
        SegmentedBagOfWords segmentedBagOfWords = new SegmentedBagOfWords(trainData.getShuffledEntries(), Constants.segmentedBagOfWordsAttribs.SUBJECT_PRECEDING,
                false, defaultMaximumNoOfVocabularyForBOW / 4);
        segmentedBagOfWordsHashMap.put(Constants.segmentedBagOfWordsAttribs.SUBJECT_PRECEDING, segmentedBagOfWords);

        new SegmentedBagOfWords(trainData.getShuffledEntries(), Constants.segmentedBagOfWordsAttribs.SUBJECT_FOLLOWING,
                false, defaultMaximumNoOfVocabularyForBOW / 4);
        segmentedBagOfWordsHashMap.put(Constants.segmentedBagOfWordsAttribs.SUBJECT_FOLLOWING, segmentedBagOfWords);

        new SegmentedBagOfWords(trainData.getShuffledEntries(), Constants.segmentedBagOfWordsAttribs.OBJECT_PRECEDING,
                false, defaultMaximumNoOfVocabularyForBOW / 4);
        segmentedBagOfWordsHashMap.put(Constants.segmentedBagOfWordsAttribs.OBJECT_PRECEDING, segmentedBagOfWords);

        new SegmentedBagOfWords(trainData.getShuffledEntries(), Constants.segmentedBagOfWordsAttribs.OBJECT_FOLLOWING,
                false, defaultMaximumNoOfVocabularyForBOW / 4);
        segmentedBagOfWordsHashMap.put(Constants.segmentedBagOfWordsAttribs.OBJECT_FOLLOWING, segmentedBagOfWords);
    }

    private void initializeModels(boolean train) {
        entityTypeModel = new EntityTypeModel();
        partOfSpeechModel = new PartOfSpeechModel();
        for (int i = 0; i < trainData.getShuffledEntries().size(); i++) {
            CorpusEntryObject corpusEntryObject = trainData.getShuffledEntries().get(i);
            partOfSpeechModel.addToModel(corpusEntryObject.getOriginalSentence().getPosTagged());
        }
        partOfSpeechModel.saveModel();
        bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false, defaultMaximumNoOfVocabularyForBOW);
        buildSegmentedBowModel();

    }

    private void createAndSaveTestData(Problem problem, int numberOfTestExamples) {
        try (Writer fileWriter = new FileWriter(this.testDataFile)) {
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
        }
    }


    public void testForWholeTestData(){
        Model model = null;
        File modelFile = new File(this.modelFilePath);
        try {
            model = Linear.loadModel(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (CorpusEntryObject instanceEntryObject:
                this.testData.getShuffledEntries()) {
            Sentence test = new Sentence(instanceEntryObject.getGeneralizedSentence());
            Feature[] instance = bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(test.getWords());//TODO: with window
            double prediction = Linear.predict(model, instance);
            System.out.println("\n"+ corpusDB.getInvertedIndices().get(prediction)+"\t"+instanceEntryObject.getPredicate());
        }

    }

    public void testForSingleSentenceString(String sentenceString){
        initializeModels(false);

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

            if (results.get(i).getResource() == null ||
                    results.get(i).getResource().getClassTree() == null ||
                    results.get(i).getResource().getClassTree().size() == 0)
                continue;

            for (int j = i+1; j<results.size(); j++){

                if (results.get(j) == null ||
                        results.get(j).getResource() == null ||
                        results.get(j).getResource().getClassTree() == null ||
                        results.get(j).getResource().getClassTree().size() == 0)
                    continue;

                CorpusEntryObject corpusEntryObject = new CorpusEntryObject();
                corpusEntryObject.setOriginalSentence(test);


                int subjectStart = results.get(i).getStart();
                int subjectEnd = results.get(i).getEnd();
                String subject = test.getWords().get(subjectStart);

                int objectStart = results.get(j).getStart();
                int objectEnd = results.get(j).getEnd();
                String object = test.getWords().get(objectStart);


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

                corpusEntryObject.setSubjectType(subjectType);
                corpusEntryObject.setObjectType(objectType);
                corpusEntryObject.setSubject(subject);
                corpusEntryObject.setObject(object);

                Feature[] instance = FeatureExtractor.createFeatureNode(bagOfWordsModel, entityTypeModel, partOfSpeechModel, corpusEntryObject);

                double[] probs = new double[model.getNrClass()];
                double prediction = Linear.predictProbability(model, instance, probs);
                List a = Arrays.asList(ArrayUtils.toObject(probs));

                if ((double) Collections.max(a) > Configuration.confidenceThreshold) {
                    System.out.println(subjectType);
                    System.out.println(objectType);
                    System.out.println("\n" + "Subject: " + results.get(i).getResource().getLabel() + "\n" + "Object: " + results.get(j).getResource().getLabel() + "\n" + "Predicate: " + corpusDB.getInvertedIndices().get(prediction));
                    System.out.println("Confidence: " + Collections.max(a));
                }
            }
        }


    }


    /*private void loadTestData(){
        this.testData.clear();
        try (Scanner scanner = new Scanner(new FileInputStream(this.testDataFile))) {
            String[] lineTokens;
            CorpusEntryObject currentTestData;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineTokens = line.split("\t");
                Sentence originalSentence = new Sentence(lineTokens[0]);
                String generalizedSentence = lineTokens[1];
                String subject = lineTokens[2];
                String object = lineTokens[3];
                String predicate =  lineTokens[4];
                Integer occurrence =  Integer.valueOf(lineTokens[5]);
                CorpusEntryObject corpusEntryObject = new CorpusEntryObject(originalSentence, generalizedSentence, object, subject, predicate, occurrence);
                this.testData.add(corpusEntryObject);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }*/
}
