package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import de.bwaldvogel.liblinear.FeatureNode;
import ir.ac.iust.dml.kg.raw.WordTokenizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.Constants;
import ir.ac.iust.dml.kg.raw.distantsupervison.CorpusEntryObject;
import ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources;
import org.apache.commons.lang3.ArrayUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hemmatan on 4/5/2017.
 */
public class FeatureExtractor {
    public static int maxWindowSize = 2;


    public static FeatureNode[] createFeatureNode(BagOfWordsModel bagOfWordsModel, EntityTypeModel entityTypeModel, PartOfSpeechModel partOfSpeechModel, CorpusEntryObject corpusEntryObject) {
        String jomle = corpusEntryObject.getGeneralizedSentence();
        List<String> tokenized = WordTokenizer.tokenize(jomle);
        FeatureNode[] bagOfWordsFeatureNodes = bagOfWordsModel.createBowLibLinearFeatureNodeForQueryWithWindow(corpusEntryObject);
        FeatureNode[] entityTypesFeatureNodes = createNamedEntityFeature(entityTypeModel, corpusEntryObject, bagOfWordsFeatureNodes.length);
        FeatureNode[] posFeatureNodes = createPosFeature(partOfSpeechModel, corpusEntryObject, bagOfWordsFeatureNodes.length + entityTypesFeatureNodes.length);
        FeatureNode[] featureNodes = ArrayUtils.addAll(ArrayUtils.addAll(bagOfWordsFeatureNodes, entityTypesFeatureNodes), posFeatureNodes);
        return featureNodes;
    }

    public static FeatureNode[] createFeatureNode(HashMap<String, SegmentedBagOfWords> segmentedBagOfWordsHashMap,
                                                  EntityTypeModel entityTypeModel, PartOfSpeechModel partOfSpeechModel, CorpusEntryObject corpusEntryObject) {
        //String jomle = corpusEntryObject.getGeneralizedSentence();
        //List<String> tokenized = WordTokenizer.tokenize(jomle);
        FeatureNode[] bagOfWordsFeatureNodes1 = segmentedBagOfWordsHashMap.get(Constants.segmentedBagOfWordsAttribs.SUBJECT_PRECEDING)
                .createBowLibLinearFeatureNodeForQuery(corpusEntryObject, 0);
        FeatureNode[] bagOfWordsFeatureNodes2 = segmentedBagOfWordsHashMap.get(Constants.segmentedBagOfWordsAttribs.SUBJECT_FOLLOWING)
                .createBowLibLinearFeatureNodeForQuery(corpusEntryObject, bagOfWordsFeatureNodes1.length);
        FeatureNode[] bagOfWordsFeatureNodes3 = segmentedBagOfWordsHashMap.get(Constants.segmentedBagOfWordsAttribs.OBJECT_PRECEDING)
                .createBowLibLinearFeatureNodeForQuery(corpusEntryObject, 2 * bagOfWordsFeatureNodes1.length);
        FeatureNode[] bagOfWordsFeatureNodes4 = segmentedBagOfWordsHashMap.get(Constants.segmentedBagOfWordsAttribs.OBJECT_FOLLOWING)
                .createBowLibLinearFeatureNodeForQuery(corpusEntryObject, 3 * bagOfWordsFeatureNodes1.length);
        FeatureNode[] bagOfWordsFeatureNodes = ArrayUtils.addAll(
                ArrayUtils.addAll(bagOfWordsFeatureNodes1, bagOfWordsFeatureNodes2),
                ArrayUtils.addAll(bagOfWordsFeatureNodes3, bagOfWordsFeatureNodes4));
        FeatureNode[] entityTypesFeatureNodes = createNamedEntityFeature(entityTypeModel, corpusEntryObject, bagOfWordsFeatureNodes.length);
        FeatureNode[] posFeatureNodes = createPosFeature(partOfSpeechModel, corpusEntryObject, bagOfWordsFeatureNodes.length + entityTypesFeatureNodes.length);
        FeatureNode[] featureNodes = ArrayUtils.addAll(ArrayUtils.addAll(bagOfWordsFeatureNodes, entityTypesFeatureNodes), posFeatureNodes);


        return featureNodes;
    }


    public static FeatureNode[] createNamedEntityFeature(EntityTypeModel entityTypeModel, CorpusEntryObject corpusEntryObject, int lastIdx) {
        FeatureNode[] featureNodes = new FeatureNode[2 * entityTypeModel.getNoOfEntityTypes()];
        List<String> subjectType = corpusEntryObject.getSubjectType();//.subList(0, Math.min(2, corpusEntryObject.getSubjectType().size()));
        List<String> objectType = corpusEntryObject.getObjectType();//.subList(0, Math.min(2, corpusEntryObject.getObjectType().size()));
        for (int i = 0; i < entityTypeModel.getNoOfEntityTypes(); i++) {
            if (subjectType.contains(entityTypeModel.getEntityInvertedIndex().get(i)))
                featureNodes[i] = new FeatureNode(lastIdx + 1 + i, 1);
            else
                featureNodes[i] = new FeatureNode(lastIdx + 1 + i, 0);
        }

        for (int i = 0; i < entityTypeModel.getNoOfEntityTypes(); i++) {
            if (objectType.contains(entityTypeModel.getEntityInvertedIndex().get(i)))
                featureNodes[entityTypeModel.getNoOfEntityTypes() + i] = new FeatureNode(lastIdx + 1 + entityTypeModel.getNoOfEntityTypes() + i, 1);
            else
                featureNodes[entityTypeModel.getNoOfEntityTypes() + i] = new FeatureNode(lastIdx + 1 + entityTypeModel.getNoOfEntityTypes() + i, 0);
        }
        return featureNodes;
    }


    private static FeatureNode[] createPosFeature(PartOfSpeechModel partOfSpeechModel, CorpusEntryObject corpusEntryObject, int lastIdx) {
        FeatureNode[] featureNodes = new FeatureNode[2 * partOfSpeechModel.getNoOfPOS()];
        String subject = corpusEntryObject.getSubject().split(" ")[0];
        int subjectIdx = corpusEntryObject.getOriginalSentence().getWords().indexOf(subject);
        if (subjectIdx == -1) {
            for (int i = 0; i < corpusEntryObject.getOriginalSentence().getWords().size(); i++) {
                if (corpusEntryObject.getOriginalSentence().getWords().get(i).contains(subject)) {
                    subjectIdx = i;
                    break;
                }
            }
        }
        if (subjectIdx != -1) {
            String subjectPOS = corpusEntryObject.getOriginalSentence().getPosTagged().get(subjectIdx);


            for (int i = 0; i < partOfSpeechModel.getNoOfPOS(); i++) {
                if (subjectPOS.equalsIgnoreCase(partOfSpeechModel.getPosInvertedIndex().get(i)))
                    featureNodes[i] = new FeatureNode(lastIdx + 1 + i, 1);
                else
                    featureNodes[i] = new FeatureNode(lastIdx + 1 + i, 0);
            }
        } else {
            for (int i = 0; i < partOfSpeechModel.getNoOfPOS(); i++)
                featureNodes[i] = new FeatureNode(lastIdx + 1 + i, 0);
        }


        String object = corpusEntryObject.getObject().split(" ")[0];
        int objectIdx = corpusEntryObject.getOriginalSentence().getWords().indexOf(object);
        if (objectIdx == -1) {
            for (int i = 0; i < corpusEntryObject.getOriginalSentence().getWords().size(); i++) {
                if (corpusEntryObject.getOriginalSentence().getWords().get(i).contains(object)) {
                    objectIdx = i;
                    break;
                }
            }
        }
        if (objectIdx != -1) {
            String objectPOS = corpusEntryObject.getOriginalSentence().getPosTagged().get(objectIdx);
            for (int i = 0; i < partOfSpeechModel.getNoOfPOS(); i++) {
                if (objectPOS.equalsIgnoreCase(partOfSpeechModel.getPosInvertedIndex().get(i)))
                    featureNodes[partOfSpeechModel.getNoOfPOS() + i] = new FeatureNode(lastIdx + 1 + partOfSpeechModel.getNoOfPOS() + i, 1);
                else
                    featureNodes[partOfSpeechModel.getNoOfPOS() + i] = new FeatureNode(lastIdx + 1 + partOfSpeechModel.getNoOfPOS() + i, 0);
            }
        } else {
            for (int i = 0; i < partOfSpeechModel.getNoOfPOS(); i++)
                featureNodes[partOfSpeechModel.getNoOfPOS() + i] = new FeatureNode(lastIdx + 1 + partOfSpeechModel.getNoOfPOS() + i, 0);
        }
        return featureNodes;
    }

    public static void convertFeatureNodesToCSVandSave(FeatureNode[][] featureNodes, double[] y, String csvFile) {
        try (Writer fileWriter = new FileWriter(csvFile)) {
            for (int i = 0; i < featureNodes.length; i++) {
                String dataRow = "";
                for (int j = 0; j < featureNodes[i].length; j++) {
                    dataRow += featureNodes[i][j].getValue() + ",";
                }
                dataRow += ((int) y[i]) + "\r\n";
                fileWriter.write(dataRow);
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void convertCSVRowsToFeatureNode(List<String[]> rows, FeatureNode[][] featureNodes, double[] y, int noOfFeatures) {
        for (int i = 0; i < rows.size(); i++) {
            y[i] = Double.valueOf(rows.get(i)[noOfFeatures]);
            for (int j = 0; j < rows.get(i).length; j++)
                featureNodes[i][j] = new FeatureNode(j + 1, Double.valueOf(rows.get(i)[j]));
        }
    }
}