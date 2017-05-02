package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import de.bwaldvogel.liblinear.FeatureNode;
import ir.ac.iust.dml.kg.ontology.tree.client.OntologyClass;
import ir.ac.iust.dml.kg.ontology.tree.client.OntologyClient;
import ir.ac.iust.dml.kg.ontology.tree.client.PagedData;
import ir.ac.iust.dml.kg.raw.WordTokenizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.CorpusEntryObject;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.util.List;

/**
 * Created by hemmatan on 4/5/2017.
 */
public class FeatureExtractor {
    public static int maxWindowSize = 2;


    public static FeatureNode[] createFeatureNode(BagOfWordsModel bagOfWordsModel, EntityTypeModel entityTypeModel, CorpusEntryObject corpusEntryObject) {
        String jomle = corpusEntryObject.getGeneralizedSentence();
        List<String> tokenized = WordTokenizer.tokenize(jomle);
        FeatureNode[] bagOfWordsFeatureNodes = bagOfWordsModel.createBowLibLinearFeatureNodeForQuery(tokenized);
        FeatureNode[] entityTypesFeatureNodes = createNamedEntityFeature(bagOfWordsModel, entityTypeModel, corpusEntryObject);
        FeatureNode[] featureNodes = ArrayUtils.addAll(bagOfWordsFeatureNodes, entityTypesFeatureNodes);
        return featureNodes;
    }

    public static FeatureNode[] createNamedEntityFeature(BagOfWordsModel bagOfWordsModel, EntityTypeModel entityTypeModel, CorpusEntryObject corpusEntryObject){
        FeatureNode[] featureNodes = new FeatureNode[2*entityTypeModel.getNoOfEntityTypes()];
        List<String> subjectType = corpusEntryObject.getSubjectType();
        List<String> objectType = corpusEntryObject.getObjectType();
        for (int i = 0; i<entityTypeModel.getNoOfEntityTypes(); i++){
            if (subjectType.contains(entityTypeModel.getEntityInvertedIndex().get(i)))
                featureNodes[i] = new FeatureNode(bagOfWordsModel.getMaximumNoOfVocabulary()+1+i, 1);
            else
                featureNodes[i] = new FeatureNode(bagOfWordsModel.getMaximumNoOfVocabulary()+1+i, 0);
        }

        for (int i = entityTypeModel.getNoOfEntityTypes(); i<2*entityTypeModel.getNoOfEntityTypes(); i++){
            if (objectType.contains(entityTypeModel.getEntityInvertedIndex().get(i)))
                featureNodes[i] = new FeatureNode(bagOfWordsModel.getMaximumNoOfVocabulary()+1+i, 1);
            else
                featureNodes[i] = new FeatureNode(bagOfWordsModel.getMaximumNoOfVocabulary()+1+i, 0);
        }
        return featureNodes;
    }
}
