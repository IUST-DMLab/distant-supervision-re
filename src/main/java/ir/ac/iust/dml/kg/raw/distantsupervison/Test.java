package ir.ac.iust.dml.kg.raw.distantsupervison;

import ir.ac.iust.dml.kg.raw.POSTagger;
import ir.ac.iust.dml.kg.raw.WordTokenizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.CorpusDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.SentenceDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.models.BagOfWordsModel;
import ir.ac.iust.dml.kg.raw.distantsupervison.models.Classifier;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

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

    @org.junit.Test
    public void evaluate() {
        List<String> predicatesToLoad = CorpusDbHandler.readPredicatesFromFile(Configuration.numberOfPredicatesToLoad);
        HashMap<String, Integer> mapping = new HashMap<>();
        int currentIndex = 0;
        try (Scanner scanner = new Scanner(new FileInputStream("mappings.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split("\t");
                for (int i = 0; i < tokens.length; i++) {
                    mapping.put(tokens[i], currentIndex);
                }
                currentIndex++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int total = 0;
        int correct = 0;

        try (Scanner scanner = new Scanner(new FileInputStream("testResults.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("Predicate")) {
                    String predicate = line.split(": ")[1];
                    String nextLine = scanner.nextLine();
                    String correctPredicate = nextLine.split(": ")[1];
                    String templine = scanner.nextLine();
                    String confidence = scanner.nextLine().split(": ")[1];
                    Double conf = Double.parseDouble(confidence);
                    if (conf > 0.4 && predicatesToLoad.contains(correctPredicate)) {
                        total++;
                        if (Objects.equals(mapping.get(predicate), mapping.get(correctPredicate)))
                            correct++;
                        //System.out.println(predicate + " " + correctPredicate);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println((correct * 100) / total);

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


        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        //sentenceDbHandler.createCorpusTableFromWikiDump();
        sentenceDbHandler.loadSentenceTable();
        BagOfWordsModel bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false, 10000);


        int tempp = 0;
    }


}
