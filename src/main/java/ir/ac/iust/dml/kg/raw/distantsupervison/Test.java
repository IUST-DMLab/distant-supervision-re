package ir.ac.iust.dml.kg.raw.distantsupervison;

import ir.ac.iust.dml.kg.raw.POSTagger;
import ir.ac.iust.dml.kg.raw.WordTokenizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.SentenceDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.models.BagOfWordsModel;
import ir.ac.iust.dml.kg.raw.distantsupervison.models.Classifier;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpus;

/**
 * Created by hemmatan on 4/4/2017.
 */
public class Test {

    @org.junit.Test
    public void postest() {
        System.out.print(POSTagger.tag(WordTokenizer.tokenize("۱۳۳۰")));
    }


    @org.junit.Test
    public void test() {
        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream("consoleOutput.txt"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setOut(out);

        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        sentenceDbHandler.loadSentenceTable();

        Classifier classifier = new Classifier();

        classifier.train(Configuration.noOfTrainExamples, Configuration.noOfTestExamples, true);

        classifier.testForSingleSentenceString("پروین اعتصامی متولد قم است");
        classifier.testForSingleSentenceString("مولوی متولد قم است");
        classifier.testForSingleSentenceString("حافظ متولد قم است");
        classifier.testForSingleSentenceString("حسن روحانی متولد قم است");
        classifier.testForSingleSentenceString("محمد اصفهانی متولد قم است");
        classifier.testForSingleSentenceString("علی لاریجانی متولد قم است");
        classifier.testForSingleSentenceString("رفسنجانی متولد قم است");
        classifier.testForSingleSentenceString("سعدی متولد قم است");

    }


    public static void main(String[] args) {
        //RawTextHandler.loadRawText();
        //RawTextHandler.buildCorpus();
        //RawTextHandler.saveCorpus();
        //RawTextHandler.loadCorpus();


        /*final Path directory = ConfigReader.INSTANCE.getPath("tuples.folder", "~/.pkg/data/tuples");

        LanguageChecker.INSTANCE.isEnglish("Test");
        final List<Path> files = PathWalker.INSTANCE.getPath(directory, new Regex("\\d-infoboxes.json"));
        final TripleJsonFileReader reader = new TripleJsonFileReader(files.get(0));
        while(reader.hasNext()) {
            final TripleData triple = reader.next();
            tripleDataList.add(triple);
        }*/

        /*Pattern pattern = new Pattern();
        String temp = "حسن روحانی در سال ۱۳۲۷ در شهرستان سرخه در استان سمنان زاده شد.";
        String subject = "حسن روحانی";
        String object = "سمنان";
        String relation = "محل تولد";
        pattern.extractPattern(temp, subject, object, relation);
        PatternsDbHandler dbHandler = new PatternsDbHandler();
        dbHandler.addToPatternTable(pattern);*/

        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        //sentenceDbHandler.createCorpusTableFromWikiDump();
        sentenceDbHandler.loadSentenceTable();
        BagOfWordsModel bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false, 10000);


        int tempp = 0;
    }
}
