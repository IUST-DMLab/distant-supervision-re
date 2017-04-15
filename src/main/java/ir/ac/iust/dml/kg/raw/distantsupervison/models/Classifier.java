package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import de.bwaldvogel.liblinear.*;
import ir.ac.iust.dml.kg.raw.distantsupervison.Corpus;
import ir.ac.iust.dml.kg.raw.distantsupervison.Sentence;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.CorpusDbHandler;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static ir.ac.iust.dml.kg.raw.distantsupervison.RawTextHandler.stringToList;
import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpus;
import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpusPath;

/**
 * Created by hemmatan on 4/10/2017.
 */
public class Classifier {

    @Test
    public void test2(){
        CorpusDbHandler corpusDbHandler  = new CorpusDbHandler();
        corpusDbHandler.createCorpusTable();
        //corpusDbHandler.loadCorpusTable();
        //BagOfWordsModel bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false);
    }


    @Test
    public void test3(){
        CorpusDbHandler corpusDbHandler  = new CorpusDbHandler();
        //corpusDbHandler.createCorpusTable();
        corpusDbHandler.loadCorpusTable();
        BagOfWordsModel bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false, 5000);

    }

    @Test
    public void loadCorpusJason(){
        String tempCorpusJasonPath = "Corpus.json";
        try {
            JsonReader reader = new JsonReader(new FileReader(tempCorpusJasonPath));
            reader.beginArray();
            int cnt = 0;
            JsonToken nextToken = reader.peek();
            while(reader.hasNext() && cnt<=3) {
                if (JsonToken.BEGIN_OBJECT.equals(nextToken)){
                    reader.beginObject();
                    String name = reader.nextName();
                    String subject = reader.nextString();
                    name = reader.nextName();
                    String object = reader.nextString();
                    name = reader.nextName();
                    String predicate = reader.nextString();
                    name = reader.nextName();
                    reader.beginObject();
                    JsonToken nextToken2 = reader.peek();
                    while (!JsonToken.END_OBJECT.equals(nextToken2)){
                        String n = reader.nextName();
                        String occur = reader.nextString();
                        nextToken2 = reader.peek();
                    }
                    reader.endObject();
                    reader.endObject();
                    nextToken = reader.peek();
                    cnt++;
                }
            }
            //reader.endArray();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test(){
        CorpusDbHandler corpusDbHandler  = new CorpusDbHandler();
        //corpusDbHandler.createCorpusTable();
        corpusDbHandler.loadCorpusTable();
        BagOfWordsModel bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false, 10000);

        Sentence tavallod1 = new Sentence("قلی در ایران چشم به جهان گشود.");
        Sentence tavallod2 = new Sentence("گلی در سمنان چشم به جهان باز کرد.");
        Sentence tavallod3 = new Sentence("سارا در دامغان زاده شد.");
        Sentence tavallod4 = new Sentence("ایکس در مشهد به دنیا آمد.");
        Sentence tavallod5 = new Sentence("سعید در همدان پا به جهان گذاشت.");

        Sentence marg1 = new Sentence("سهراب در تهران چشم از جهان فرو بست.");
        Sentence marg2 = new Sentence("متین در بیمارستان مهر مرد.");
        Sentence marg3 = new Sentence("مینا در دامغان از دنیا رفت.");
        Sentence marg4 = new Sentence("مجید در مشهد دار فانی را وداع گفت.");
        Sentence marg5 = new Sentence("مرتضی در همدان فوت کرد.");

        Problem problem = new Problem();
        problem.l =  10; // number of training examples
        problem.n =  bagOfWordsModel.getMaximumNoOfVocabulary();// number of features

        FeatureNode[][] f = {bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(tavallod1.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(tavallod2.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(tavallod3.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(tavallod4.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(tavallod5.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(marg1.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(marg2.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(marg3.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(marg4.getWords()),
                bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(marg5.getWords())
        };

        problem.x =  f;// feature nodes
        problem.y = new double[]{1, 1, 1, 1, 1, -1, -1, -1, -1, -1};// target values

        SolverType solver = SolverType.L2R_LR; // -s 0
        double C = 1.0;    // cost of constraints violation
        double eps = 0.01; // stopping criteria

        Parameter parameter = new Parameter(solver, C, eps);
        Model model = Linear.train(problem, parameter);
        File modelFile = new File("testmodel");
        try {
            model.save(modelFile);
            // load model or use it directly
            model = Model.load(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Sentence test = new Sentence("او در تهران چشم از جهان فرو بست.");

        Feature[] instance = bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(test.getWords());
        double prediction = Linear.predict(model, instance);

        System.out.println("\n"+prediction);
    }
}
