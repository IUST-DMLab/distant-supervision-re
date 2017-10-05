package ir.ac.iust.dml.kg.raw.distantsupervison;

import ir.ac.iust.dml.kg.raw.distantsupervison.models.Classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static com.google.common.io.Files.isDirectory;

/**
 * Created by hemmatan on 9/19/2017.
 */
public class ModelHandler {
    public static String decide(CorpusEntryObject corpusEntryObject){
        String[] names = Configuration.classifierTypes;
        for (String name :
                names){
            File curFile = new File(SharedResources.logitDirectory + name);
            if (curFile.isDirectory()){
                try (Scanner scanner = new Scanner(new FileInputStream(curFile + "\\AllowedEntityTypes.txt"))) {
                    String subjectType = scanner.nextLine();
                    String objectType = scanner.nextLine();
                    if (corpusEntryObject.getSubjectType().contains(subjectType) &&
                            corpusEntryObject.getObjectType().contains(objectType))
                        return name;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return Constants.classifierTypes.GENERAL;
    }

    public static void trainAllModels(){
        String[] names = Configuration.classifierTypes;
        for (String name :
                names){
            File curFile = new File(SharedResources.logitDirectory + name);
            if (curFile.isDirectory()){
                Classifier classifier = new Classifier(name);
                classifier.train(Configuration.maximumNumberOfTrainExamples, true);
            }
        }
    }
}
