package ir.ac.iust.dml.kg.raw.distantsupervison;

import ir.ac.iust.dml.kg.raw.utils.ConfigReader;
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleData;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hemmatan on 4/5/2017.
 */
public class SharedResources {
    public static final Path tuplesPath = ConfigReader.INSTANCE.getPath("tuples.folder", "~/.pkg/data/tuples");
    public static final Path rawTextPath = ConfigReader.INSTANCE.getPath("raw.text.file", "~/.pkg/data/raw.txt");
    public static final Path corpusPath = ConfigReader.INSTANCE.getPath("corpus.file", "~/.pkg/data/corpus.txt");
    public static final Path bagOfWordsModelPath = ConfigReader.INSTANCE.getPath("bagOfWords.model", "~/.pkg/data/bagOfWords");

    public static String rawText = "";
    public static List<String> rawTextLines = new ArrayList<String>();
    public static Corpus corpus = new Corpus();
    public static CorpusDB corpusDB = new CorpusDB();
    public static List<TripleData> tripleDataList = new ArrayList<TripleData>();

}
