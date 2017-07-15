package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import de.bwaldvogel.liblinear.*;
import edu.stanford.nlp.ling.TaggedWord;
import ir.ac.iust.dml.kg.raw.POSTagger;
import ir.ac.iust.dml.kg.raw.WordTokenizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.*;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.CorpusDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.DbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.SentenceDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.reUtils.JSONHandler;
import ir.ac.iust.dml.kg.resource.extractor.client.ExtractorClient;
import ir.ac.iust.dml.kg.resource.extractor.client.MatchedResource;
import org.apache.commons.lang3.ArrayUtils;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.json.JSONArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;


import java.io.*;
import java.util.*;

import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpusDB;
import static ir.ac.iust.dml.kg.raw.distantsupervison.database.DbHandler.corpusTableName;

/**
 * Created by hemmatan on 4/10/2017.
 */
public class Classifier {

    protected Problem problem = new Problem();
    protected int defaultMaximumNoOfVocabularyForBOW = Configuration.maximumNoOfVocabularyForBagOfWords;
    protected int numberOfFeatures;
    protected DataSet trainingData;

    public int getNumberOfClasses() {
        return numberOfClasses;
    }

    protected int numberOfClasses;
    protected String predicatesIndexFile = "predicates.txt";
    protected String goldJsonFilePath = Configuration.exportURL;
    protected HashMap<String, SegmentedBagOfWords> segmentedBagOfWordsHashMap = new HashMap<>();
    protected EntityTypeModel entityTypeModel;
    protected PartOfSpeechModel partOfSpeechModel;
    protected CorpusDB trainData = new CorpusDB();
    protected CorpusDB testData = new CorpusDB();
    protected Set<String> testIDs = new HashSet<>();

    public Classifier() {
        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        sentenceDbHandler.loadSentenceTable();

        CorpusDbHandler trainDbHandler = new CorpusDbHandler(DbHandler.trainTableName);
        trainDbHandler.load(trainData);
    }

    private static DataSet readCSVDataset(
            String csvFileClasspath, int batchSize, int labelIndex, int numClasses)
            throws IOException, InterruptedException {

        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new ClassPathResource(csvFileClasspath).getFile()));
        DataSetIterator iterator = new RecordReaderDataSetIterator(rr, batchSize, labelIndex, numClasses);
        return iterator.next();
    }

    public HashMap<String, SegmentedBagOfWords> getSegmentedBagOfWordsHashMap() {
        return segmentedBagOfWordsHashMap;
    }

    public EntityTypeModel getEntityTypeModel() {
        return entityTypeModel;
    }

    public PartOfSpeechModel getPartOfSpeechModel() {
        return partOfSpeechModel;
    }

    public CorpusDB getTrainData() {
        return trainData;
    }

    public void createTrainData(int maximumNumberOfTrainingExamples,
                                CorpusDbHandler trainDbHandler) {
        readTestAndExtractItsPredicates();

        trainDbHandler.deleteAll();
        trainData.deleteAll();

        CorpusDbHandler corpusDbHandler = new CorpusDbHandler(corpusTableName);
        if (Configuration.trainingSetMode.equalsIgnoreCase(Constants.trainingSetModes.LOAD_DATA_FROM_CSV)) {
            this.trainingData = loadTrainDataFromCSV();
        } else {
            if (Configuration.trainingSetMode.equalsIgnoreCase(Constants.trainingSetModes.LOAD_PREDICATES_FROM_FILE))
                corpusDbHandler.loadByReadingPedicatesFromFile(corpusDB, maximumNumberOfTrainingExamples, testIDs, SharedResources.predicatesToLoadFile);
            else if (Configuration.trainingSetMode.equalsIgnoreCase(Constants.trainingSetModes.USE_ALL_PREDICATES_IN_EXPORTS_JSON))
                corpusDbHandler.loadByReadingPedicatesFromFile(corpusDB, maximumNumberOfTrainingExamples, testIDs, SharedResources.predicatesInExportsJsonFile);
            else // Configuration.trainingSetMode.equalsIgnoreCase(Constants.trainingSetModes.LOAD_CORPUS_FREQUENT_PREDICATES)
                corpusDbHandler.loadByMostFrequentPredicates(corpusDB, maximumNumberOfTrainingExamples);

            corpusDB.shuffle();
            int numberOfTrainingExamples = corpusDB.getEntries().size();
            Configuration.noOfTrainExamples = numberOfTrainingExamples;

            for (int i = 0; i < numberOfTrainingExamples; i++) {
                System.out.println(i + "\t" + corpusDB.getShuffledEntries().get(i).toString());
                trainData.addEntry(corpusDB.getShuffledEntries().get(i));
            }
            trainDbHandler.insertAll(trainData.getEntries());
        }
    }

    protected DataSet loadTrainDataFromCSV() {
        System.out.println("loadTrainDataFromCSV");
        DataSet trainingData = null;
        loadLatestCSVReaderParams();
        try {
            trainingData = readCSVDataset(
                    SharedResources.trainCSV,
                    Configuration.dl4jParams.batchSizeTraining,
                    numberOfFeatures, numberOfClasses);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        loadModels();
        return trainingData;
    }


    public int getNumberOfFeatures() {
        return numberOfFeatures;
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
        System.out.println("loadModels");
        entityTypeModel = new EntityTypeModel(true);
        partOfSpeechModel = new PartOfSpeechModel();
        partOfSpeechModel.loadModel();
        loadSegmentedBowModel();
        trainData.loadPredicateIndices(this.predicatesIndexFile);
    }

    public void initializeModels(boolean train) {
        System.out.println("initializeModels");
        entityTypeModel = new EntityTypeModel(false);
        partOfSpeechModel = new PartOfSpeechModel();
        for (int i = 0; i < trainData.getEntries().size(); i++) {
            CorpusEntryObject corpusEntryObject = trainData.getEntries().get(i);
            partOfSpeechModel.addToModel(corpusEntryObject.getOriginalSentence().getPosTagged());
        }
        partOfSpeechModel.saveModel();
        buildSegmentedBowModel();
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


    public void readTestAndExtractItsPredicates() {
        Set<String> predicates = new HashSet<>();
        JSONArray jsonArray = JSONHandler.getJsonArrayFromURL(goldJsonFilePath);
        Configuration.noOfTestExamples = jsonArray.length();
        for (int i = 0; i < Configuration.noOfTestExamples; i++) {
            String id = jsonArray.getJSONObject(i).getString("id");
            String predicate = jsonArray.getJSONObject(i).getString("predicate");
            testIDs.add(id);
            predicates.add(predicate);
        }
        try (Writer fileWriter = new FileWriter(SharedResources.predicatesInExportsJsonFile)) {
            for (String predicate : predicates) {
                fileWriter.write(predicate + "\r\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void extractProblemParamsFromDBData() {
        problem.l = Configuration.noOfTrainExamples; // number of training examples
        FeatureNode[][] featureNodes = new FeatureNode[problem.l][];
        problem.y = new double[problem.l];// target values
        numberOfFeatures = 4 * this.segmentedBagOfWordsHashMap.get(Constants.segmentedBagOfWordsAttribs.OBJECT_FOLLOWING).getMaximumNoOfVocabulary() +
                +2 * this.entityTypeModel.getNoOfEntityTypes() + 2 * this.partOfSpeechModel.getNoOfPOS();
        problem.n = this.numberOfFeatures;// number of features

        for (int i = 0; i < problem.l; i++) {
            CorpusEntryObject corpusEntryObject = trainData.getEntries().get(i);
            featureNodes[i] = FeatureExtractor.createFeatureNode(segmentedBagOfWordsHashMap, entityTypeModel, partOfSpeechModel, corpusEntryObject);
            problem.y[i] = trainData.getIndices().get(corpusEntryObject.getPredicate());
        }

        System.out.print(trainData.getIndices());
        System.out.print(numberOfFeatures);

        problem.x = featureNodes;// feature nodes
        trainData.savePredicateIndices(this.predicatesIndexFile);
    }

    private void extractProblemParamsFromCSV(String csvFile) {
        int noOfFeatures = 0;
        Set<String> classes = new HashSet<>();
        List<String[]> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(new FileInputStream(csvFile))) {
            String[] lineTokens;
            while (scanner.hasNextLine()){
                lineTokens = scanner.nextLine().split(",");
                lines.add(lineTokens);
                classes.add(lineTokens[lineTokens.length-1]);
                noOfFeatures = lineTokens.length-1;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.problem.l =  lines.size();
        FeatureNode[][] featureNodes = new FeatureNode[problem.l][];
        FeatureExtractor.convertCSVRowsToFeatureNode(lines, featureNodes, problem.y, noOfFeatures);

        this.numberOfClasses =  classes.size();
        this.problem.n = noOfFeatures;
        this.problem.x = featureNodes;
    }

    private void loadLatestCSVReaderParams(){
        String csvReaderParamsFile = "C:\\Users\\hemmatan\\Desktop\\KG\\RE deep-learning\\lastDeepModel\\csvParams.txt";
        try (Scanner scanner = new Scanner(new FileInputStream(csvReaderParamsFile))) {
            problem.l = Integer.parseInt(scanner.nextLine());
            this.numberOfClasses = Integer.parseInt(scanner.nextLine());
            this.numberOfFeatures =  Integer.parseInt(scanner.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
    }


}
