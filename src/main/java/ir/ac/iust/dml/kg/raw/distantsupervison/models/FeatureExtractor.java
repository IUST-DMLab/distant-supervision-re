package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import de.bwaldvogel.liblinear.FeatureNode;
import ir.ac.iust.dml.kg.raw.WordTokenizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.CorpusEntryObject;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * Created by hemmatan on 4/5/2017.
 */
public class FeatureExtractor {
    public static int maxWindowSize = 2;


    public static FeatureNode[] createFeatureNode(BagOfWordsModel bagOfWordsModel, CorpusEntryObject corpusEntryObject) {
        String jomle = corpusEntryObject.getGeneralizedSentence();
        List<String> tokenized = WordTokenizer.tokenize(jomle);
        FeatureNode[] bagOfWordsFeatureNodes = bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(tokenized);
        FeatureNode[] entityTypesFeatureNodes;
        FeatureNode[] featureNodes = ArrayUtils.addAll(bagOfWordsFeatureNodes);
        return featureNodes;
    }
}
