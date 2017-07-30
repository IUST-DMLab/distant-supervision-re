package ir.ac.iust.dml.kg.raw.distantsupervison;

import ir.ac.iust.dml.kg.raw.distantsupervison.models.Classifier;
import ir.ac.iust.dml.kg.raw.distantsupervison.models.DeepClassifier;
import ir.ac.iust.dml.kg.raw.distantsupervison.models.LogitClassifier;

import java.io.*;
import java.nio.file.Files;
import java.util.Date;

/**
 * Created by hemmatan on 4/26/2017.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("train")) process(true);
        else process(false);

    }

    private static void process(boolean train) {
        //TODO: these two lines should be removed because the corpus table loads in Classifier()!
        //SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        //sentenceDbHandler.loadSentenceTable();
        Date date = new Date();
        String dateString = date.toString().replaceAll("[: ]", "-");

        DeepClassifier classifier = new DeepClassifier();

        if (train) classifier.train(Configuration.maximumNumberOfTrainExamples);
        else {
            classifier.loadModels();
            classifier.loadNetworkAndNormalizer();
        }



        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream("testResults-" + dateString + ".txt"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setOut(out);

        classifier.testOnGoldCSV();

        try {
            Files.deleteIfExists(new File(SharedResources.LastTestResultsFile).toPath());
            Files.copy(new File("testResults-" + dateString + ".txt").toPath(), new File(SharedResources.LastTestResultsFile).toPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
