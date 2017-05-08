package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import de.bwaldvogel.liblinear.*;
import ir.ac.iust.dml.kg.raw.WordTokenizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.Configuration;
import ir.ac.iust.dml.kg.raw.distantsupervison.Constants;
import ir.ac.iust.dml.kg.raw.distantsupervison.CorpusEntryObject;
import ir.ac.iust.dml.kg.raw.distantsupervison.Sentence;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.CorpusDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.SentenceDbHandler;
import ir.ac.iust.dml.kg.resource.extractor.client.ExtractorClient;
import ir.ac.iust.dml.kg.resource.extractor.client.MatchedResource;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpus;
import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpusDB;

/**
 * Created by hemmatan on 4/10/2017.
 */
public class Classifier {

    private String modelFilePath = "tempTestModel";
    private String testDataFile = "testData.txt";
    private BagOfWordsModel bagOfWordsModel;
    private EntityTypeModel entityTypeModel;
    private List<CorpusEntryObject> trainData = new ArrayList<>();
    private List<CorpusEntryObject> testData = new ArrayList<>();

    public Classifier(){
        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        sentenceDbHandler.loadCorpusTable();

        int totalNoOfData = Configuration.noOfTotalExamples;

        CorpusDbHandler corpusDbHandler = new CorpusDbHandler();
        corpusDbHandler.loadByMostFrequentPredicates(corpusDB, totalNoOfData);
        corpusDB.shuffle();

        int defaultMaximumNoOfVocabularyForBOW = Configuration.maximumNoOfVocabularyForBagOfWords;
        entityTypeModel = new EntityTypeModel();
        bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false, defaultMaximumNoOfVocabularyForBOW);
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

    public void train(int numberOfTrainingExamples, int numberOfTestExamples){
        int totalNoOfData = numberOfTestExamples + numberOfTrainingExamples;

        Problem problem = new Problem();
        problem.l =  numberOfTrainingExamples; // number of training examples
        problem.n =  this.bagOfWordsModel.getMaximumNoOfVocabulary()
                + 2*this.entityTypeModel.getNoOfEntityTypes();// number of features

        FeatureNode[][] featureNodes = new FeatureNode[problem.l][];
        problem.y = new double[problem.l];// target values
        for (int i = 0 ; i<problem.l; i++){
            trainData.add(corpusDB.getShuffledEntries().get(i));
            System.out.println(i + "\t" + corpusDB.getShuffledEntries().get(i).getOriginalSentence().getNormalized()
                    + "\t" + corpusDB.getShuffledEntries().get(i).getSubject()
                    + "\t" + corpusDB.getShuffledEntries().get(i).getObject()
                    + "\t" + "predicate: " + corpusDB.getShuffledEntries().get(i).getPredicate() + "\n");
            CorpusEntryObject corpusEntryObject = corpusDB.getShuffledEntries().get(i);
            featureNodes[i] = FeatureExtractor.createFeatureNode(bagOfWordsModel, entityTypeModel, corpusEntryObject);
            problem.y[i] = corpusDB.getIndices().get(corpusEntryObject.getPredicate());
        }

        createAndSaveTestData(problem, numberOfTestExamples);

        //System.out.println(corpusDB.getIndices().keySet());

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

    private void createAndSaveTestData(Problem problem, int numberOfTestExamples) {
        try (Writer fileWriter = new FileWriter(this.testDataFile)) {
            CorpusEntryObject currentTestData;
            for (int i = problem.l; i<problem.l+numberOfTestExamples; i++){
                currentTestData = corpusDB.getShuffledEntries().get(i);
                testData.add(currentTestData);
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

    public void testForWholeTestData(){
        Model model = null;
        File modelFile = new File(this.modelFilePath);
        try {
            model = Linear.loadModel(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (CorpusEntryObject instanceEntryObject:
             this.testData) {
            Sentence test = new Sentence(instanceEntryObject.getGeneralizedSentence());
            Feature[] instance = bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(test.getWords());//TODO: with window
            double prediction = Linear.predict(model, instance);
            System.out.println("\n"+ corpusDB.getInvertedIndices().get(prediction)+"\t"+instanceEntryObject.getPredicate());
        }

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

                int objectStart = results.get(j).getStart();
                int objectEnd = results.get(j).getEnd();

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

                Feature[] instance = FeatureExtractor.createFeatureNode(bagOfWordsModel, entityTypeModel, corpusEntryObject);

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


    @Test
    public void temp(){
        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        //sentenceDbHandler.createCorpusTableFromWikiDump();
        sentenceDbHandler.loadCorpusTable();
        BagOfWordsModel bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false, 10000);

        Sentence tavallod1 = new Sentence("قلی در ایران چشم به جهان گشود.");
        Sentence tavallod2 = new Sentence("گلی در سمنان چشم به جهان باز کرد.");
        Sentence tavallod3 = new Sentence("سارا در دامغان زاده شد.");
        Sentence tavallod4 = new Sentence("ایکس در مشهد به دنیا آمد.");
        Sentence tavallod5 = new Sentence("سعید در همدان پا به جهان گذاشت.");

        Sentence marg1 = new Sentence("سهراب در تهران چشم از جهان فرو بست.");
        Sentence marg2 = new Sentence("متین در بیمارستان مهر مرد.");
        Sentence marg3 = new Sentence("مینا در دامغان از دنیا رفت.");
        Sentence marg4 = new Sentence("مجید در مشهد دار فانی را وداع گفت.");
        Sentence marg5 = new Sentence("مرتضی در همدان فوت کرد.");

        Problem problem = new Problem();
        problem.l =  10; // number of training examples
        problem.n =  bagOfWordsModel.getMaximumNoOfVocabulary();// number of features

        FeatureNode[][] f = {bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(tavallod1.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(tavallod2.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(tavallod3.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(tavallod4.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(tavallod5.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(marg1.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(marg2.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(marg3.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(marg4.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(marg5.getWords())
        };

        problem.x =  f;// feature nodes
        problem.y = new double[]{1, 1, 1, 1, 1, -1, -1, -1, -1, -1};// target values

        SolverType solver = SolverType.L2R_LR; // -s 0
        double C = 1.0;    // cost of constraints violation
        double eps = 0.01; // stopping criteria

        Parameter parameter = new Parameter(solver, C, eps);
        Model model = Linear.train(problem, parameter);
        File modelFile = new File("testmodel");
        try {
            model.save(modelFile);
            // load model or use it directly
            model = Model.load(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Sentence test = new Sentence("او در تهران چشم از جهان فرو بست.");

        Feature[] instance = bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(test.getWords());
        double prediction = Linear.predict(model, instance);

        System.out.println("\n"+prediction);
    }



    @Test
    public void test2(){
        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        sentenceDbHandler.createCorpusTableFromWikiDump();
        //sentenceDbHandler.loadCorpusTable();
        //BagOfWordsModel bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false);
    }


    @Test
    public void dbTest(){
        CorpusDbHandler corpusDbHandler = new CorpusDbHandler();
        corpusDbHandler.loadByMostFrequentPredicates(corpusDB, 1000);
        corpusDB.shuffle();
        //corpusDbHandler.test();
    }

}
