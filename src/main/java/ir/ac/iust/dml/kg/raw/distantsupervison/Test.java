package ir.ac.iust.dml.kg.raw.distantsupervison;

import com.google.gson.internal.bind.JsonTreeReader;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.CorpusDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.PatternsDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.models.BagOfWordsModel;
import ir.ac.iust.dml.kg.raw.utils.ConfigReader;
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker;
import ir.ac.iust.dml.kg.raw.utils.PathWalker;
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleData;
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleJsonFileReader;
import kotlin.text.Regex;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpus;
import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.tripleDataList;

/**
 * Created by hemmatan on 4/4/2017.
 */
public class Test {
    public static void main(String[] args) {
        //RawTextHandler.loadRawText();
        //RawTextHandler.buildCorpus();
        //RawTextHandler.saveCorpus();
        /*RawTextHandler.loadCorpus();


        final Path directory = ConfigReader.INSTANCE.getPath("tuples.folder", "~/.pkg/data/tuples");

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

        CorpusDbHandler corpusDbHandler  = new CorpusDbHandler();
        corpusDbHandler.loadCorpusTable();
        BagOfWordsModel bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false);
        int tempp = 0;
    }
}
