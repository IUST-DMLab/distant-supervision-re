package ir.ac.iust.dml.kg.raw.distantsupervison.database;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import ir.ac.iust.dml.kg.raw.Normalizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.*;
import ir.ac.iust.dml.kg.resource.extractor.client.ExtractorClient;
import ir.ac.iust.dml.kg.resource.extractor.client.MatchedResource;
import org.bson.Document;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hemmatan on 4/18/2017.
 */
public class CorpusDbHandler extends DbHandler {

    private MongoClient mongo = null;
    private MongoDatabase distantSupervisionDB;
    private MongoCollection<Document> corpusTable;

    public CorpusDbHandler() {
        mongo = new MongoClient(host, port);
        distantSupervisionDB = mongo.getDatabase(distantSupervisionDBName);
        corpusTable = distantSupervisionDB.getCollection(corpusTableName);
    }

    public void insert(CorpusEntryObject corpusEntryObject) {

        Document document = new Document();
        document.put(Constants.sentenceAttribs.RAW, corpusEntryObject.getOriginalSentence().getRaw());
        document.put(Constants.sentenceAttribs.NORMALIZED, corpusEntryObject.getOriginalSentence().getNormalized());
        document.put(Constants.sentenceAttribs.WORDS, corpusEntryObject.getOriginalSentence().getWords());
        document.put(Constants.sentenceAttribs.POSTAG, corpusEntryObject.getOriginalSentence().getPosTagged());
        document.put(Constants.corpusDbEntryAttribs.GENERALIZED_SENTENCE, corpusEntryObject.getGeneralizedSentence());
        document.put(Constants.corpusDbEntryAttribs.SUBJECT, corpusEntryObject.getSubject());
        document.put(Constants.corpusDbEntryAttribs.OBJECT, corpusEntryObject.getObject());
        document.put(Constants.corpusDbEntryAttribs.SUBJECT_TYPE, corpusEntryObject.getSubjectType());
        document.put(Constants.corpusDbEntryAttribs.OBJECT_TYPE, corpusEntryObject.getObjectType());
        document.put(Constants.corpusDbEntryAttribs.PREDICATE, corpusEntryObject.getPredicate());
        document.put(Constants.corpusDbEntryAttribs.OCCURRENCE, corpusEntryObject.getOccurrence());

        corpusTable.insertOne(document);
    }

    public void insertAll(List<CorpusEntryObject> corpusEntryObjects){
        for (CorpusEntryObject corpusEntryObject:
             corpusEntryObjects) {
            this.insert(corpusEntryObject);
        }
    }

    public void loadByMostFrequentPredicates(CorpusDB destinationCorpusDB,
                                             int numberOfEntriesToLoad){
        List<String> predicates = getMostFrequentPredicates(Configuration.numberOfPredicatesToLoad);
        MongoCursor cursor = corpusTable.find().iterator();
        Document document;
        String rawString;
        String normalized;
        Object wordsObject;
        BasicDBList postagObject;
        String generalizedSentence;
        String object;
        String subject;
        List<String> objectType;
        List<String> subjectType;
        String predicate;
        int occurrence;
        List<String> words;
        List<String> posTags;
        Sentence currentSentence;
        CorpusEntryObject corpusEntryObject;
        while (cursor.hasNext() && destinationCorpusDB.getEntries().size()<numberOfEntriesToLoad){
            document = (Document) cursor.next();
            rawString = (String) document.get(Constants.sentenceAttribs.RAW);
            normalized = (String) document.get(Constants.sentenceAttribs.NORMALIZED);
            words = (List<String>) document.get(Constants.sentenceAttribs.WORDS);
            posTags = (List<String>) document.get(Constants.sentenceAttribs.POSTAG);
            generalizedSentence = (String) document.get(Constants.corpusDbEntryAttribs.GENERALIZED_SENTENCE);
            object = (String) document.get(Constants.corpusDbEntryAttribs.OBJECT);
            subject = (String) document.get(Constants.corpusDbEntryAttribs.SUBJECT);
            objectType = (List<String>) document.get(Constants.corpusDbEntryAttribs.OBJECT_TYPE);
            subjectType = (List<String>) document.get(Constants.corpusDbEntryAttribs.SUBJECT_TYPE);
            predicate = (String) document.get(Constants.corpusDbEntryAttribs.PREDICATE);
            occurrence = (int) document.get(Constants.corpusDbEntryAttribs.OCCURRENCE);
            if (destinationCorpusDB.getPredicateCounts().containsKey(predicate) &&
                destinationCorpusDB.getPredicateCounts().get(predicate)>=Configuration.maximumNoOfInstancesForEachPredicate)
                    continue;
            //words = convertBasicDBListToJavaListOfStrings(wordsObject);
            //posTags = convertBasicDBListToJavaListOfStrings(postagObject);
            currentSentence = new Sentence(rawString,words,posTags,normalized);
            corpusEntryObject = new CorpusEntryObject(currentSentence, generalizedSentence, object, subject, objectType, subjectType, predicate, occurrence);
            if (predicates.contains(corpusEntryObject.getPredicate()))
                destinationCorpusDB.addEntry(corpusEntryObject);
        }
    }

    public void load(CorpusDB destinationCorpusDB){
        load(destinationCorpusDB, Integer.MAX_VALUE);
    }

    public void load(CorpusDB destinationCorpusDB, int numberOfEntriesToLoad){

        MongoCursor cursor = corpusTable.find().iterator();
        int cnt = 0;
        Document document;
        String rawString;
        String normalized;
        Object wordsObject;
        BasicDBList postagObject;
        String generalizedSentence;
        String object;
        String subject;
        List<String> objectType;
        List<String> subjectType;
        String predicate;
        int occurrence;
        List<String> words;
        List<String> posTags;
        Sentence currentSentence;
        CorpusEntryObject corpusEntryObject;
            while (cursor.hasNext() && cnt<numberOfEntriesToLoad){
                cnt+=1;
                document = (Document) cursor.next();
                rawString = (String) document.get(Constants.sentenceAttribs.RAW);
                normalized = (String) document.get(Constants.sentenceAttribs.NORMALIZED);
                words = (List<String>) document.get(Constants.sentenceAttribs.WORDS);
                posTags = (List<String>) document.get(Constants.sentenceAttribs.POSTAG);
                generalizedSentence = (String) document.get(Constants.corpusDbEntryAttribs.GENERALIZED_SENTENCE);
                object = (String) document.get(Constants.corpusDbEntryAttribs.OBJECT);
                subject = (String) document.get(Constants.corpusDbEntryAttribs.SUBJECT);
                objectType = (List<String>) document.get(Constants.corpusDbEntryAttribs.OBJECT_TYPE);
                subjectType = (List<String>) document.get(Constants.corpusDbEntryAttribs.SUBJECT_TYPE);
                predicate = (String) document.get(Constants.corpusDbEntryAttribs.PREDICATE);
                occurrence = (int) document.get(Constants.corpusDbEntryAttribs.OCCURRENCE);
                //words = convertBasicDBListToJavaListOfStrings(wordsObject);
                //posTags = convertBasicDBListToJavaListOfStrings(postagObject);
                currentSentence = new Sentence(rawString,words,posTags,normalized);
                corpusEntryObject = new CorpusEntryObject(currentSentence, generalizedSentence, object, subject,  objectType, subjectType, predicate, occurrence);
                destinationCorpusDB.addEntry(corpusEntryObject);
            }
    }


    //TODO: :)
    public void saveCorpusJasonToDB(){

        String tempCorpusJasonPath = "Corpus.json";
        try (JsonReader reader = new JsonReader(new FileReader(tempCorpusJasonPath));
        ){
            reader.beginArray();
            int cnt = 0;
            JsonToken nextToken = reader.peek();

            CorpusDbHandler corpusDbHandler = new CorpusDbHandler();
            ExtractorClient client = new ExtractorClient(Configuration.extractorClient);

            while(reader.hasNext()) {
                if (JsonToken.BEGIN_OBJECT.equals(nextToken)){
                    reader.beginObject();
                    String name = reader.nextName();
                    String subject = Normalizer.normalize(reader.nextString());
                    name = reader.nextName();
                    String object = Normalizer.normalize(reader.nextString());
                    name = reader.nextName();
                    String predicate = reader.nextString();
                    name = reader.nextName();
                    reader.beginObject();
                    JsonToken nextToken2 = reader.peek();
                    while (!JsonToken.END_OBJECT.equals(nextToken2)){
                        String newSentence = reader.nextName();
                        int occur = Integer.valueOf(reader.nextString());

                        String originalSentence = newSentence.replace(Constants.sentenceAttribs.SUBJECT_ABV, subject);
                        originalSentence = originalSentence.replace(Constants.sentenceAttribs.OBJECT_ABV, object);


                        List<String> objectType = new ArrayList<>();
                        List<String> subjectType = new ArrayList<>();

                        final List<MatchedResource> result_object = client.match(object);
                        final List<MatchedResource> result_subject = client.match(subject);

                        if (result_object == null || result_object.size()==0 || result_object.get(0).getResource()==null)
                            objectType.add("null");
                        else if (result_object.get(0).getResource().getClassTree() == null || result_object.get(0).getResource().getClassTree().size()==0)
                            objectType.add(result_object.get(0).getResource().getIri());
                        else objectType.addAll(result_object.get(0).getResource().getClassTree());

                        if (result_subject == null || result_subject.size()==0 || result_subject.get(0).getResource()==null)
                            subjectType.add("null");
                        else if (result_subject.get(0).getResource().getClassTree() == null || result_subject.get(0).getResource().getClassTree().size()==0)
                            subjectType.add(result_subject.get(0).getResource().getIri());
                        else subjectType.addAll(result_subject.get(0).getResource().getClassTree());


                        Sentence sentence = new Sentence(originalSentence);
                        String generalizedNormalizedSentence = sentence.getNormalized().replace(subject, Constants.sentenceAttribs.SUBJECT_ABV);
                        generalizedNormalizedSentence = generalizedNormalizedSentence.replace(object, Constants.sentenceAttribs.OBJECT_ABV);
                        CorpusEntryObject corpusEntryObject = new CorpusEntryObject(sentence,generalizedNormalizedSentence,object,subject,objectType,subjectType,predicate,occur);
                        corpusDbHandler.insert(corpusEntryObject);
                        nextToken2 = reader.peek();
                    }
                    reader.endObject();
                    reader.endObject();
                    nextToken = reader.peek();
                    cnt++;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<String> getMostFrequentPredicates(int numberOfEntriesToLoad){
        Document firstGroup = new Document("$group",
                new Document("_id",
                        new Document("predicate", "$predicate")
                                .append("subject", "$subject")
                                .append("object", "$object")));

        Document secondGroup = new Document("$group",
                new Document("_id",
                        new Document("predicate", "$predicate"))
                        .append("count", new Document("$sum", 1)));

        Document sort = new Document("$sort", new Document("count", -1));

        List<Document> pipeline = new ArrayList<Document>(Arrays.asList(secondGroup));
        pipeline.add(sort);
        AggregateIterable<Document> biadab = corpusTable.aggregate(pipeline).allowDiskUse(true);


        int cnt = 0;
        List<String> result = new ArrayList<>();
        for (Document d:
             biadab) {
            if (cnt++>numberOfEntriesToLoad)
                break;
            String predicate = ((Document) d.get("_id")).get("predicate").toString();
            result.add(predicate);
            System.out.println(d.get("_id")+" "+d.get("count"));
        }

        return result;
    }

    public void close(){
        mongo.close();
    }


}
