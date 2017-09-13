package ir.ac.iust.dml.kg.raw.distantsupervison;

import ir.ac.iust.dml.kg.raw.DependencyParser;
import ir.ac.iust.dml.kg.raw.POSTagger;
import ir.ac.iust.dml.kg.raw.WordTokenizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.CorpusDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.ExtractedTriplesDBHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.SentenceDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.models.Classifier;
import ir.ac.iust.dml.kg.raw.distantsupervison.reUtils.JSONHandler;
import ir.ac.iust.dml.kg.raw.triple.RawTriple;
import org.json.JSONArray;
import org.maltparser.concurrent.graph.ConcurrentDependencyGraph;
import org.maltparser.concurrent.graph.ConcurrentDependencyNode;

import java.io.*;
import java.util.*;

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

        classifier.train(Configuration.maximumNumberOfTrainExamples, true);

        /*classifier.extractFromSingleSentenceString("پروین اعتصامی متولد قم است");
        classifier.extractFromSingleSentenceString("مولوی متولد قم است");
        classifier.extractFromSingleSentenceString("حافظ متولد قم است");
        classifier.extractFromSingleSentenceString("حسن روحانی متولد قم است");
        classifier.extractFromSingleSentenceString("محمد اصفهانی متولد قم است");
        classifier.extractFromSingleSentenceString("علی لاریجانی متولد قم است");
        classifier.extractFromSingleSentenceString("رفسنجانی متولد قم است");
        classifier.extractFromSingleSentenceString("سعدی متولد قم است");*/
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


        // SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        ////sentenceDbHandler.createCorpusTableFromWikiDump();
        //sentenceDbHandler.loadSentenceTable();
        //BagOfWordsModel bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false, 10000);
    }

    @org.junit.Test
    public void extract() {
        /*Classifier classifier = new Classifier();
        classifier.loadModels();*/
        String text;
        DistantSupervisionTripleExtractor distantSupervisionTripleExtractor = new DistantSupervisionTripleExtractor();
        //String text = "زاگرس در ایران واقعا است";
        ExtractedTriplesDBHandler extractedTriplesDBHandler = new ExtractedTriplesDBHandler("extracted_triples");
        try {
            try (Scanner scanner = new Scanner(new FileInputStream("/home/asgari/test.txt"))) {
                while (scanner.hasNextLine()) {
                    text = scanner.nextLine();
                    //text = "زاگرس در ایران واقعا است";
                    List<RawTriple> triples = distantSupervisionTripleExtractor.extract("wiki", "2", text);
                    for (RawTriple tripleGuess :
                            triples) {
                        if (tripleGuess.getAccuracy() > 0.4)
                            extractedTriplesDBHandler.insert(tripleGuess);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

 /*   public void extract(String raw, Classifier classifier){
        List<String> sentences = SentenceTokenizer.SentenceSplitterRaw(raw);
        for (String sentence :
                sentences) {
            classifier.extractFromSingleSentenceString(sentence);
        }
    }*/

    @org.junit.Test
    public void statsForExport() {
        try (Writer fileWriter = new FileWriter("stats2.txt")) {
            fileWriter.write("subject" + "\t" + "object" + "\t" + "predicate" + "\t" + "depTree.getVerb(subject)" + "\t" + "depTree.getVerb(object)" +
                    "\t"+
                    "depTree.getHead(subject)" + "\t" + "depTree.getHead(object)" + "\t" + "sentence"+ "\r\n");
            JSONArray jsonArray = JSONHandler.getJsonArrayFromURL(Configuration.exportURL);
            for (int i = 0; i < jsonArray.length(); i++) {
                Object depTreeHash = jsonArray.getJSONObject(i).get("depTreeHash");
                if (depTreeHash.equals(null) || ((String) depTreeHash).equalsIgnoreCase("error"))
                    continue;

                String sentenceString = jsonArray.getJSONObject(i).getString("normalized");
                String subject = jsonArray.getJSONObject(i).getString("subject").split(" ")[0];
                String object = jsonArray.getJSONObject(i).getString("object").split(" ")[0];
                String predicate = jsonArray.getJSONObject(i).getString("predicate");
                String[] sentenceTokens = sentenceString.split(" ");
                DepTree depTree = new DepTree((String) depTreeHash, sentenceString);
                fileWriter.write(subject + "\t" + object + "\t" + predicate + "\t" + testGetHeadVerb(sentenceString, subject) + "\t" + testGetHeadVerb(sentenceString, object) +
                        "\t"+
                        depTree.getHead(subject) + "\t" + depTree.getHead(object) + "\t"+ sentenceString+"\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @org.junit.Test
    public void testDepTree() {
        String depTreeHash = "";
        ConcurrentDependencyGraph result = DependencyParser.parseRaw("مجموعه\u200Cهای جدید : Containment Crazy Ex-Girlfriend افسانه\u200Cهای فردا مجموعه\u200Cهای سال ۲۰۱۴-۲۰۱۵ که در این سال بازنمی\u200Cگردند : هارت آو دیکسی پیام\u200Cآوران ( مجموعه تلویزیونی ) شبکه رسانه\u200Cای فاکس مجموعه\u200Cهای بازگشته : امریکن آیدل برگری باب استخوان\u200Cها ( مجموعه تلویزیونی ) بروکلین نه-نه امپراتوری ( مجموعه تلویزیونی ) مرد خانواده Fox College Football گاتهام ( مجموعه تلویزیونی ) Hell's Kitchen آخرین مرد روی زمین ( مجموعه تلویزیونی ) MasterChef MasterChef Junior دختر جدید ( مجموعه تلویزیونی ) فرار از زندان ( مجموعه تلویزیونی ) سیمپسون\u200Cها Sleepy Hollow World's Funniest پرونده\u200Cهای اکس مجموعه\u200Cهای جدید : Bordertown Cooper Barret's Guide to Surviving Life Grandfathered The Grinder Houdini and Doyle Lookinglass Lucifer Minority Report Rosewood ملکه\u200Cهای جیغ مجموعه\u200Cهای سال ۲۰۱۴-۲۰۱۵ که در این سال بازنمی\u200Cگردند : Backstrom پیرو ( مجموعه تلویزیونی ) گلی ( مجموعه تلویزیونی ) Knock Knock Live The Mindy Project ( moves to هیولو ( وب\u200Cگاه ) ) Mulaney Red Band Society Utopia Weird Loners ان\u200Cبی\u200Cسی مجموعه\u200Cهای بازگشته : American Ninja Warrior آمریکا استعداد دارد کارآموز ( برنامه تلویزیونی ) Aquarius The Biggest Loser لیست سیاه ( مجموعه تلویزیونی ) The Carmichael Show Chicago Fire Chicago P . ").get(0);
        for (int i = 1; i < result.nTokenNodes() + 1; i++) {
            ConcurrentDependencyNode node = result.getDependencyNode(i);
            String postag = node.getLabel("POSTAG");


            String headid = "0";
            if (!node.getHead().getLabel("ID").equalsIgnoreCase(""))
                headid = node.getHead().getLabel("ID");
            String deprel = node.getLabel("DEPREL");
            depTreeHash += "[" + postag + "," + headid + "," + deprel + "]";
        }
        System.out.println(depTreeHash);
        int temp = 0;//:))))

    }

    //@org.junit.Test
    public String testGetHeadVerb(String sentence, String entity) {

        // sentence = "او که به هنر علاقه داشت در تهران زاده شد.";
        //entity = "هنر";
        if (DependencyParser.parseRaw(sentence) == null || DependencyParser.parseRaw(sentence).isEmpty())
            return "null";
        ConcurrentDependencyGraph result = DependencyParser.parseRaw(sentence).get(0);
        int idx = -1;
        for (int i = 1; i < result.nTokenNodes() + 1; i++) {
            ConcurrentDependencyNode node = result.getDependencyNode(i);
            if (node.getLabel("FORM").equalsIgnoreCase(entity))
                idx = i;
        }
        if (idx == -1) {
            System.out.println("entity not found");
            return "entity not found";
        }

        ConcurrentDependencyNode node = result.getDependencyNode(idx);
        String res = "nullll";
        int idxx = -1;
        while (true) {
            ConcurrentDependencyNode parent = node.getHead();
            if (parent == null) return "null parent";
            if (parent.getLabel("POSTAG").equalsIgnoreCase("V")) {
                res = parent.getLabel("FORM");
                idxx = parent.getIndex();
                System.out.println(parent.getLabel("FORM"));
                break;
            }
            node = parent;

        }

        int ind = -1;
        for (int i = 1; i < result.nTokenNodes() + 1; i++) {
            ConcurrentDependencyNode node1 = result.getDependencyNode(i);
            if (node.getLabel("DEPREL").equalsIgnoreCase("NVE"))
                ind = i;
        }
        if (ind != -1)
            res = result.getDependencyNode(ind).getLabel("FORM") + " " + res;
        return res;
    }

    @org.junit.Test
    public void update() {
        CorpusDbHandler corpus = new CorpusDbHandler(Configuration.corpusTableName);
        corpus.updateEntityTypes();
    }
}
