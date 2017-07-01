package ir.ac.iust.dml.kg.raw.distantsupervison;

import ir.ac.iust.dml.kg.raw.distantsupervison.models.Classifier;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created by hemmatan on 4/26/2017.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("train")) process(true);
        else process(false);

    }

    private static void process(boolean train) {
        //TODO: these two lines should be remove because the corpus table loads in Classifier()!
        //SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        //sentenceDbHandler.loadSentenceTable();

        Classifier classifier = new Classifier();

        if (train) classifier.train(Configuration.maximumNumberOfTrainExamples, true);
        else classifier.loadModels();


        //classifier.initializeModels(false);
        String sentenceString = "حسن روحانی ، رییس\\u200Cجمهور ایران، در سال ۱۳۳۰ در تهران به دنیا آمد.";
        String subject = "حسن روحانی";
        String object = "تهران";

        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream("testResults.txt"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setOut(out);

        classifier.testOnGoldJson();
    }

}
