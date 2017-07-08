package ir.ac.iust.dml.kg.raw.distantsupervison;

import de.bwaldvogel.liblinear.FeatureNode;
import ir.ac.iust.dml.kg.raw.POSTagger;
import ir.ac.iust.dml.kg.raw.WordTokenizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.CorpusDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.SentenceDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.models.BagOfWordsModel;
import ir.ac.iust.dml.kg.raw.distantsupervison.models.Classifier;
import ir.ac.iust.dml.kg.raw.distantsupervison.models.FeatureExtractor;
import ir.ac.iust.dml.kg.raw.distantsupervison.reUtils.JSONHandler;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.json.JSONArray;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.*;
import java.util.*;

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
        Date date = new Date();
        String dateString = date.toString().replaceAll("[: ]", "-");

        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream("consoleOutput-" + dateString + ".txt"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setOut(out);

        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        sentenceDbHandler.loadSentenceTable();

        Classifier classifier = new Classifier();

        classifier.train(Configuration.maximumNumberOfTrainExamples,
                Constants.Classifiers.DEEP,
                true);

        JSONArray jsonArray = JSONHandler.getJsonArrayFromURL(Configuration.exportURL);
        FeatureNode[][] featureNodes = new FeatureNode[5560][];
        double y[] = new double[5560];
        for (int i = 0; i < jsonArray.length(); i++) {
            String sentenceString = jsonArray.getJSONObject(i).getString("raw");
            String subject = jsonArray.getJSONObject(i).getString("subject");
            String object = jsonArray.getJSONObject(i).getString("object");
            String predicate = jsonArray.getJSONObject(i).getString("predicate");
            int predNum = classifier.getTrainData().getIndices().get(predicate).intValue();
            y[i] = predNum;
            CorpusEntryObject corpusEntryObject = new CorpusEntryObject(sentenceString, subject, object, predicate);
            featureNodes[i] = FeatureExtractor.createFeatureNode(classifier.getSegmentedBagOfWordsHashMap(), classifier.getEntityTypeModel(), classifier.getPartOfSpeechModel(), corpusEntryObject);
        }
        FeatureExtractor.convertFeatureNodesToCSVandSave(featureNodes, y, SharedResources.testCSV);
        int batchSizeTest = 44;
        DataSet testData = null;
        try {
            testData = readCSVDataset(SharedResources.testCSV,
                    batchSizeTest, classifier.getNumberOfFeatures(), classifier.getTrainData().getNumberOfClasses().intValue());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        classifier.getNormalizer().transform(testData);
        Evaluation eval = new Evaluation(classifier.getTrainData().getNumberOfClasses().intValue());
        INDArray output = classifier.getModel_deep().output(testData.getFeatureMatrix());

        eval.eval(testData.getLabels(), output);
        System.out.println(eval.stats());

        classifier.testForSingleSentenceString("پروین اعتصامی متولد قم است");
        classifier.testForSingleSentenceString("مولوی متولد قم است");
        classifier.testForSingleSentenceString("حافظ متولد قم است");
        classifier.testForSingleSentenceString("حسن روحانی متولد قم است");
        classifier.testForSingleSentenceString("محمد اصفهانی متولد قم است");
        classifier.testForSingleSentenceString("علی لاریجانی متولد قم است");
        classifier.testForSingleSentenceString("رفسنجانی متولد قم است");
        classifier.testForSingleSentenceString("سعدی متولد قم است");
    }

    private static DataSet readCSVDataset(
            String csvFileClasspath, int batchSize, int labelIndex, int numClasses)
            throws IOException, InterruptedException{

        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new ClassPathResource(csvFileClasspath).getFile()));
        DataSetIterator iterator = new RecordReaderDataSetIterator(rr,batchSize,labelIndex,numClasses);
        return iterator.next();
    }


    @org.junit.Test
    public void evaluateTest() {
        evaluate(SharedResources.LastTestResultsFile);
    }

    public void evaluate(String testResultsFile) {

        String predicatesFile = SharedResources.predicatesToLoadFile;
        if (Configuration.trainingSetMode.equalsIgnoreCase(Constants.trainingSetModes.LOAD_PREDICATES_FROM_FILE))
            predicatesFile = SharedResources.predicatesToLoadFile;
        else if (Configuration.trainingSetMode.equalsIgnoreCase(Constants.trainingSetModes.USE_ALL_PREDICATES_IN_EXPORTS_JSON))
            predicatesFile = SharedResources.predicatesInExportsJsonFile;

        List<String> predicatesToLoad = CorpusDbHandler.readPredicatesFromFile(Configuration.maximumNumberOfPredicatesToLoad, predicatesFile);
        HashMap<String, Integer> mapping = new HashMap<>();
        int currentIndex = 0;
        try (Scanner scanner = new Scanner(new FileInputStream(SharedResources.mappingsFile))) {
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
        int rec = 0;

        try (Scanner scanner = new Scanner(new FileInputStream(testResultsFile))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("[main]"))
                    rec++;
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
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        rec /= 4;
        System.out.println((correct * 100) / total);
        System.out.println(correct + " " + rec);

        System.out.println((correct * 100) / rec);

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
