package ir.ac.iust.dml.kg.raw.distantsupervison;

import ir.ac.iust.dml.kg.raw.distantsupervison.database.SentenceDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.models.Classifier;

/**
 * Created by hemmatan on 4/26/2017.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("train")) process(true);
        else process(false);

    }

    private static void process(boolean train) {
        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        sentenceDbHandler.createCorpusTableFromWikiDump();
        sentenceDbHandler.loadCorpusTable();

        Classifier classifier = new Classifier();

        if (train) classifier.train(Configuration.noOfTrainExamples, Configuration.noOfTestExamples);

//        classifier.testForSingleSentenceString("پروین اعتصامی متولد قم است");
        //classifier.testForSingleSentenceString("مولوی متولد قم است");
        //classifier.testForSingleSentenceString("حافظ متولد قم است");
        classifier.testForSingleSentenceString("حسن روحانی متولد قم است");
        classifier.testForSingleSentenceString("محمد اصفهانی متولد قم است");
        classifier.testForSingleSentenceString("علی لاریجانی متولد قم است");
        classifier.testForSingleSentenceString("رفسنجانی متولد قم است");
        classifier.testForSingleSentenceString("سعدی متولد قم است");

    }

//    public void test() {
//        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
//        sentenceDbHandler.loadCorpusTable();
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
