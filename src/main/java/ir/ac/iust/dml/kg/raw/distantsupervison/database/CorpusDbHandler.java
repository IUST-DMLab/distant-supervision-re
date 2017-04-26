package ir.ac.iust.dml.kg.raw.distantsupervison.database;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mongodb.*;
import com.mongodb.client.*;
import ir.ac.iust.dml.kg.raw.Normalizer;

import ir.ac.iust.dml.kg.raw.distantsupervison.*;
import org.bson.Document;

import java.io.FileReader;
import java.util.*;

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
        int numberOfPredicatesToLoad = (Integer) numberOfEntriesToLoad/Configuration.maximumNoOfInstancesForEachPredicate +1;
        List<String> predicates = getMostFrequentPredicates(numberOfPredicatesToLoad);
        MongoCursor cursor = corpusTable.find().iterator();
        Document document;
        String rawString;
        String normalized;
        Object wordsObject;
        BasicDBList postagObject;
        String generalizedSentence;
        String object;
        String subject;
        String predicate;
        int occurrence;
        List<String> words;
        List<String> posTags;
        Sentence currentSentence;
        CorpusEntryObject corpusEntryObject;
        while (cursor.hasNext() && destinationCorpusDB.getEntries().size()<=numberOfEntriesToLoad){
            document = (Document) cursor.next();
            rawString = (String) document.get(Constants.sentenceAttribs.RAW);
            normalized = (String) document.get(Constants.sentenceAttribs.NORMALIZED);
            words = (List<String>) document.get(Constants.sentenceAttribs.WORDS);
            posTags = (List<String>) document.get(Constants.sentenceAttribs.POSTAG);
            generalizedSentence = (String) document.get(Constants.corpusDbEntryAttribs.GENERALIZED_SENTENCE);
            object = (String) document.get(Constants.corpusDbEntryAttribs.OBJECT);
            subject = (String) document.get(Constants.corpusDbEntryAttribs.SUBJECT);
            predicate = (String) document.get(Constants.corpusDbEntryAttribs.PREDICATE);
            occurrence = (int) document.get(Constants.corpusDbEntryAttribs.OCCURRENCE);
            if (destinationCorpusDB.getPredicateCounts().containsKey(predicate) &&
                destinationCorpusDB.getPredicateCounts().get(predicate)>=Configuration.maximumNoOfInstancesForEachPredicate)
                    continue;
            //words = convertBasicDBListToJavaListOfStrings(wordsObject);
            //posTags = convertBasicDBListToJavaListOfStrings(postagObject);
            currentSentence = new Sentence(rawString,words,posTags,normalized);
            corpusEntryObject = new CorpusEntryObject(currentSentence, generalizedSentence, object, subject, predicate, occurrence);
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
                predicate = (String) document.get(Constants.corpusDbEntryAttribs.PREDICATE);
                occurrence = (int) document.get(Constants.corpusDbEntryAttribs.OCCURRENCE);
                //words = convertBasicDBListToJavaListOfStrings(wordsObject);
                //posTags = convertBasicDBListToJavaListOfStrings(postagObject);
                currentSentence = new Sentence(rawString,words,posTags,normalized);
                corpusEntryObject = new CorpusEntryObject(currentSentence, generalizedSentence, object, subject, predicate, occurrence);
                destinationCorpusDB.addEntry(corpusEntryObject);
            }
    }

    public void saveCorpusJasonToDB(){
        String tempCorpusJasonPath = "Corpus.json";
        try (JsonReader reader = new JsonReader(new FileReader(tempCorpusJasonPath));
        ){
            reader.beginArray();
            int cnt = 0;
            JsonToken nextToken = reader.peek();

            CorpusDbHandler corpusDbHandler = new CorpusDbHandler();
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

                        String originalSentence = newSentence.replace("$SUBJ",subject);
                        originalSentence = originalSentence.replace("$OBJ", object);
                        Sentence sentence = new Sentence(originalSentence);
                        String generalizedNormalizedSentence = sentence.getNormalized().replace(subject, "$SUBJ");
                        generalizedNormalizedSentence = generalizedNormalizedSentence.replace(object, "$OBJ");
                        CorpusEntryObject corpusEntryObject = new CorpusEntryObject(sentence,generalizedNormalizedSentence,object,subject,predicate,occur);
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
