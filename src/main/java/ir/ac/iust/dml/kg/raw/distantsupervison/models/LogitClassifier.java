package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import de.bwaldvogel.liblinear.*;
import ir.ac.iust.dml.kg.raw.WordTokenizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.Configuration;
import ir.ac.iust.dml.kg.raw.distantsupervison.Constants;
import ir.ac.iust.dml.kg.raw.distantsupervison.CorpusEntryObject;
import ir.ac.iust.dml.kg.raw.distantsupervison.Sentence;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.CorpusDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.DbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.reUtils.JSONHandler;
import ir.ac.iust.dml.kg.resource.extractor.client.ExtractorClient;
import ir.ac.iust.dml.kg.resource.extractor.client.MatchedResource;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by hemmatan on 7/8/2017.
 */
public class LogitClassifier extends Classifier {

    private String modelFilePath = "logitModel.model";
    private Parameter parameter;

    public LogitClassifier() {
        super();
    }

    public void train(int maximumNumberOfTrainingExamples) {

        CorpusDbHandler trainDbHandler = new CorpusDbHandler(DbHandler.trainTableName);

        if (Configuration.trainingSetMode.equalsIgnoreCase(Constants.trainingSetModes.LOAD_DATA_FROM_DB)) {
            trainDbHandler.load(trainData);
        } else {
            createTrainData(maximumNumberOfTrainingExamples, trainDbHandler);
        }
        trainDbHandler.close();

        if (!Configuration.trainingSetMode.equalsIgnoreCase(Constants.trainingSetModes.LOAD_DATA_FROM_CSV)) {
            initializeModels(true);
            extractProblemParamsFromDBData();//problem.x , problem.y , problem.n , problem.l
        }

        setLogitParams();//Parameter: eps , solverType, costOfConstraintViolation

        File modelFile = new File(this.modelFilePath);
        Model model = Linear.train(problem, parameter);

        try {
            model.save(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setLogitParams() {
        SolverType solver = SolverType.L2R_LR; // -s 0
        double costOfConstraintsViolation = Configuration.libLinearParams.costOfConstraintsViolation;
        double eps = Configuration.libLinearParams.epsStoppingCriteria;
        parameter = new Parameter(solver, costOfConstraintsViolation, eps);
    }


    public void testForSingleSentenceStringAndTriple(String sentenceString, String subject, String object, String predicate) {
        Model model = null;
        File modelFile = new File(String.valueOf(this.modelFilePath));
        try {
            model = Linear.loadModel(modelFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        CorpusEntryObject corpusEntryObject = new CorpusEntryObject(sentenceString, subject, object, predicate);
        Feature[] instance = FeatureExtractor.createFeatureNode(segmentedBagOfWordsHashMap, entityTypeModel, partOfSpeechModel, corpusEntryObject);

        double[] probs = new double[model.getNrClass()];
        double prediction = Linear.predictProbability(model, instance, probs);
        List a = Arrays.asList(ArrayUtils.toObject(probs));

        if ((double) Collections.max(a) > 0) {
            System.out.println(corpusEntryObject.getSubjectType());
            System.out.println(corpusEntryObject.getObjectType());
            System.out.println("\n" + "Subject: " + subject + " " + "\n" + "Object: " + object + " " + "\n" + "Predicate: " + trainData.getInvertedIndices().get(prediction));
            System.out.println("Correct Predicate: " + predicate);
            System.out.println("Prediction number: " + prediction);
            System.out.println("Confidence: " + Collections.max(a));
        }
        //System.out.print(trainData.getIndices());
    }

    public void testForSingleSentenceString(String sentenceString) {

        Sentence test = new Sentence(sentenceString);
        ExtractorClient client = new ExtractorClient(Configuration.extractorClient);
        List<MatchedResource> results = client.match(sentenceString);


        //Feature[] instance = bagOfWordsModel.createBowLibLinearFeatureNodeForQueryWithWindow(test.getWords());
        Model model = null;
        File modelFile = new File(String.valueOf(this.modelFilePath));
        try {
            model = Linear.loadModel(modelFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < results.size(); i++) {

            if (ignoreEntity(results.get(i), test))
                continue;

            for (int j = i + 1; j < results.size(); j++) {

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
                for (int o = objectStart + 1; o <= objectEnd; o++) {
                    jomle = jomle.replace(test.getWords().get(o), "");
                    words.remove(o);
                }
                for (int s = subjectStart + 1; s <= subjectEnd; s++) {
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

    public void testOnGoldJson() {
        JSONArray jsonArray = JSONHandler.getJsonArrayFromURL(goldJsonFilePath);
        for (int i = 0; i < jsonArray.length(); i++) {
            String sentenceString = jsonArray.getJSONObject(i).getString("raw");
            String subject = jsonArray.getJSONObject(i).getString("subject");
            String object = jsonArray.getJSONObject(i).getString("object");
            String predicate = jsonArray.getJSONObject(i).getString("predicate");
            testForSingleSentenceStringAndTriple(sentenceString, subject, object, predicate);
        }
    }
}
