package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import ir.ac.iust.dml.kg.ontology.tree.client.OntologyClass;
import ir.ac.iust.dml.kg.ontology.tree.client.OntologyClient;
import ir.ac.iust.dml.kg.ontology.tree.client.PagedData;
import ir.ac.iust.dml.kg.raw.distantsupervison.Configuration;
import ir.ac.iust.dml.kg.raw.distantsupervison.Constants;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by hemmatan on 4/29/2017.
 */
public class EntityTypeModel {
    private String entityModelFile = "entity.txt";
    private int noOfEntityTypes;
    private List<OntologyClass> entities;
    private HashMap<String, Integer> entityIndex = new HashMap<>();
    private HashMap<Integer, String> entityInvertedIndex = new HashMap<>();

    public EntityTypeModel(boolean load) {
        if (load)
            this.loadModel();
        else {
            OntologyClient client = new OntologyClient(Configuration.ontologyClient);
            final PagedData<OntologyClass> result = client.search(0, 1000, null, "Thing", false);
            this.noOfEntityTypes = result.getRowCount();
            this.entities = result.getData();
            int lastIdx = 0;
            for (OntologyClass ontologyClass :
                    this.entities) {
                String temp = Constants.entityModelAttribs.PREFIX + ontologyClass.getOntologyClass();
                if (!entityIndex.containsKey((temp))) {
                    entityIndex.put(temp, lastIdx);
                    entityInvertedIndex.put(lastIdx, temp);
                    lastIdx++;
                }
            }
            saveModel();
        }
    }


    public void saveModel() {
        try (Writer fileWriter = new FileWriter(this.entityModelFile)) {
            Set<String> entities = this.entityIndex.keySet();
            for (String s :
                    entities) {
                fileWriter.write(s + "\t" + entityIndex.get(s) + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadModel() {
        try (Scanner scanner = new Scanner(new FileInputStream(this.entityModelFile))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String pos = line.split("\t")[0];
                int lastIdx = Integer.parseInt(line.split("\t")[1]);
                entityIndex.put(pos, lastIdx);
                entityInvertedIndex.put(lastIdx, pos);
            }
            noOfEntityTypes = entityIndex.keySet().size();
            //entities = entityIndex.keySet();
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getNoOfEntityTypes() {
        return noOfEntityTypes;
    }

    public void setNoOfEntityTypes(int noOfEntityTypes) {
        this.noOfEntityTypes = noOfEntityTypes;
    }

    public HashMap<String, Integer> getEntityIndex() {
        return entityIndex;
    }

    public void setEntityIndex(HashMap<String, Integer> entityIndex) {
        this.entityIndex = entityIndex;
    }

    public HashMap<Integer, String> getEntityInvertedIndex() {
        return entityInvertedIndex;
    }

    public void setEntityInvertedIndex(HashMap<Integer, String> entityInvertedIndex) {
        this.entityInvertedIndex = entityInvertedIndex;
    }
}
