package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import de.bwaldvogel.liblinear.*;
import ir.ac.iust.dml.kg.raw.WordTokenizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.CorpusEntryObject;
import ir.ac.iust.dml.kg.raw.distantsupervison.Sentence;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.CorpusDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.SentenceDbHandler;
import org.apache.commons.lang.ObjectUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpus;
import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpusDB;

/**
 * Created by hemmatan on 4/10/2017.
 */
public class Classifier {

    @Test
    public void test2(){
        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        sentenceDbHandler.createCorpusTableFromWikiDump();
        //sentenceDbHandler.loadCorpusTable();
        //BagOfWordsModel bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false);
    }


    @Test
    public void dbTest(){
        CorpusDbHandler corpusDbHandler = new CorpusDbHandler();
        corpusDbHandler.loadByMostFrequentPredicates(corpusDB, 1000);
        corpusDB.shuffle();
        //corpusDbHandler.test();
    }

    @Test
    public void train(){
        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        //sentenceDbHandler.createCorpusTableFromWikiDump();
        sentenceDbHandler.loadCorpusTable();
        BagOfWordsModel bagOfWordsModel = new BagOfWordsModel(corpus.getSentences(), false, 5000);

        CorpusDbHandler corpusDbHandler = new CorpusDbHandler();
        corpusDbHandler.loadByMostFrequentPredicates(corpusDB, 1000);
        corpusDB.shuffle();

       Problem problem = new Problem();
        problem.l =  1000; // number of training examples
        problem.n =  bagOfWordsModel.getMaximumNoOfVocabulary();// number of features

        FeatureNode[][] featureNodes = new FeatureNode[problem.l][];
        problem.y = new double[problem.l];// target values
        for (int i = 0 ; i<problem.l; i++){
            CorpusEntryObject temp = corpusDB.getShuffledEntries().get(i);
            String jomle = temp.getGeneralizedSentence();
            List<String> tokenized = WordTokenizer.tokenize(jomle);
            featureNodes[i] = bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(tokenized);
            problem.y[i] = corpusDB.getIndices().get(temp.getPredicate());
        }

        problem.x =  featureNodes;// feature nodes

        SolverType solver = SolverType.L2R_LR; // -s 0
        double C = 1.0;    // cost of constraints violation
        double eps = 1; // stopping criteria

        Parameter parameter = new Parameter(solver, C, eps);

        File modelFile = new File("temptestmodel2");
        Model model = null;//Linear.train(problem, parameter);
       try {
            model = Linear.loadModel(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*try {
            model.save(modelFile);
            // load model or use it directly
            model = Model.load(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Sentence test = new Sentence("$SUBJ، به آلمانی $OBJ");

        Feature[] instance = bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(test.getWords());
        double prediction = Linear.predict(model, instance);

        System.out.println("\n"+ corpusDB.getInvertedIndices().get(prediction));
    }


    @Test
    public void test(){
        SentenceDbHandler sentenceDbHandler = new SentenceDbHandler();
        //sentenceDbHandler.createCorpusTableFromWikiDump();
        sentenceDbHandler.loadCorpusTable();
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
