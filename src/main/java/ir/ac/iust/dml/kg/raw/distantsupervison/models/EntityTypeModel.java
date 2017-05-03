package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import ir.ac.iust.dml.kg.ontology.tree.client.OntologyClass;
import ir.ac.iust.dml.kg.ontology.tree.client.OntologyClient;
import ir.ac.iust.dml.kg.ontology.tree.client.PagedData;
import ir.ac.iust.dml.kg.raw.distantsupervison.Configuration;

import java.util.HashMap;
import java.util.List;

/**
 * Created by hemmatan on 4/29/2017.
 */
public class EntityTypeModel {
    private int noOfEntityTypes;
    private List<OntologyClass> entities;
    private HashMap<String, Integer> entityIndex = new HashMap<>();
    private HashMap<Integer, String> entityInvertedIndex = new HashMap<>();

    public EntityTypeModel() {
        OntologyClient client = new OntologyClient(Configuration.ontologyClient);
        final PagedData<OntologyClass> result = client.search(0, 1000, null, null, false);
        this.noOfEntityTypes = result.getRowCount();
        this.entities = result.getData();
        int lastIdx = 0;
        for (OntologyClass ontologyClass:
             this.entities) {
            String temp = "http://fkg.iust.ac.ir/ontology/" + ontologyClass.getOntologyClass();
            if (!entityIndex.containsKey((temp))) {
                entityIndex.put(temp, lastIdx);
                entityInvertedIndex.put(lastIdx, temp);
                lastIdx++;
            }
        }
    }

    public int getNoOfEntityTypes() {
        return noOfEntityTypes;
    }

    public void setNoOfEntityTypes(int noOfEntityTypes) {
        this.noOfEntityTypes = noOfEntityTypes;
    }

    public List<OntologyClass> getEntities() {
        return entities;
    }

    public void setEntities(List<OntologyClass> entities) {
        this.entities = entities;
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
