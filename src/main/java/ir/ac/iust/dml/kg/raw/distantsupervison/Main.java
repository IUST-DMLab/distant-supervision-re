/*
 * Farsi Knowledge Graph Project
 *  Iran University of Science and Technology (Year 2017)
 *  Developed by Ensieh Hemmatan.
 */

package ir.ac.iust.dml.kg.raw.distantsupervison;

import ir.ac.iust.dml.kg.raw.distantsupervison.database.DbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.models.Classifier;

import java.io.*;
import java.nio.file.Files;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("train")) process(true);
        else if (args.length > 0 && args[0].equals("makeDB")) DbHandler.saveCorpusJasonToDB();
        else process(false);

    }

    private static void process(boolean train) {
        //TODO: these two lines should be removed because the corpus table loads in Classifier()!
        //SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        //sentenceDbHandler.loadSentenceTable();
        Date date = new Date();
        String dateString = date.toString().replaceAll("[: ]", "-");

        Classifier classifier = new Classifier();

        if (train) classifier.train(Configuration.maximumNumberOfTrainExamples, true);
        else classifier.loadModels();


        //classifier.initializeModels(false);

        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream("testResults-" + dateString + ".txt"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setOut(out);

        classifier.testOnGoldJson();

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
