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

        if (train) classifier.train(Configuration.noOfTrainExamples, Configuration.noOfTestExamples, true);
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

        classifier.testJson();
        //classifier.testForSingleSentenceStringAndTriple(sentenceString, subject, object);
        //classifier.testForSingleSentenceString("حسن روحانی ، رییس\u200Cجمهور ایران، در سال ۱۳۳۰ در تهران به دنیا آمد.");

        //classifier.testForSingleSentenceString("مولوی متولد قم است");

        //classifier.testForSingleSentenceString("حافظ متولد قم است");
        /*
        classifier.testForSingleSentenceString("حسن روحانی متولد قم است");
        classifier.testForSingleSentenceString("محمد اصفهانی متولد قم است");
        classifier.testForSingleSentenceString("علی لاریجانی متولد قم است");
        classifier.testForSingleSentenceString("رفسنجانی متولد قم است");
        classifier.testForSingleSentenceString("سعدی متولد قم است");


        classifier.testForSingleSentenceString("پروین اعتصامی در آبادان چشم به جهان گشود");
        classifier.testForSingleSentenceString("محل تولد شاعر بزرگ، مولوی ساوه میباشد.");
        classifier.testForSingleSentenceString("حافظ در شیراز به دنیا آمد");
        classifier.testForSingleSentenceString("حسن روحانی در خانواده تهیدست در  مشهد متولد گشت");
        classifier.testForSingleSentenceString("محمد اصفهانی در سال ۱۳۳۰ در تهران به دنیا آمد");
        classifier.testForSingleSentenceString("علی لاریجانی در بهمن ۱۳۵۷ در  گرگان متولد شد");*/
    }

//    public void test() {
//        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
//        sentenceDbHandler.loadSentenceTable();
//
//        CorpusDbHandler corpusDbHandler = new CorpusDbHandler();
//        corpusDbHandler.loadByMostFrequentPredicates(corpusDB, 1000);
//        corpusDB.shuffle();
//
//
//        for (int i = 0 ; i<1000; i++){
//            CorpusEntryObject corpusEntryObject = corpusDB.getShuffledEntries().get(i);
//        }
//        System.out.println("**"+corpusDB.getIndices().keySet());
//
//    }
}
