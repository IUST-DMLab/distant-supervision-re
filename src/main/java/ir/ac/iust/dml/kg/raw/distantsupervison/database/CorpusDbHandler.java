package ir.ac.iust.dml.kg.raw.distantsupervison.database;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mongodb.*;
import com.mongodb.client.*;
import ir.ac.iust.dml.kg.raw.Normalizer;

import ir.ac.iust.dml.kg.raw.distantsupervison.Constants;
import ir.ac.iust.dml.kg.raw.distantsupervison.CorpusEntryObject;
import ir.ac.iust.dml.kg.raw.distantsupervison.Sentence;
import org.bson.Document;
import org.junit.Test;

import java.io.FileReader;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpusDB;

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
            document.put(Constants.SENTENCE_ATTRIBS.RAW, corpusEntryObject.getOriginalSentence().getRaw());
            document.put(Constants.SENTENCE_ATTRIBS.NORMALIZED, corpusEntryObject.getOriginalSentence().getNormalized());
            document.put(Constants.SENTENCE_ATTRIBS.WORDS, corpusEntryObject.getOriginalSentence().getWords());
            document.put(Constants.SENTENCE_ATTRIBS.POSTAG, corpusEntryObject.getOriginalSentence().getPosTagged());
            document.put(Constants.CORPUS_DB_ENTRY_ATTRIBS.GENERALIZED_SENTENCE, corpusEntryObject.getGeneralizedSentence());
            document.put(Constants.CORPUS_DB_ENTRY_ATTRIBS.SUBJECT, corpusEntryObject.getSubject());
            document.put(Constants.CORPUS_DB_ENTRY_ATTRIBS.OBJECT, corpusEntryObject.getObject());
        document.put(Constants.CORPUS_DB_ENTRY_ATTRIBS.PREDICATE, corpusEntryObject.getPredicate());
        document.put(Constants.CORPUS_DB_ENTRY_ATTRIBS.OCCURRENCE, corpusEntryObject.getOccurrence());

            corpusTable.insertOne(document);
    }

    public void insertAll(List<CorpusEntryObject> corpusEntryObjects){
        for (CorpusEntryObject corpusEntryObject:
             corpusEntryObjects) {
            this.insert(corpusEntryObject);
        }
    }

    public void load(int numberOfEntriesToLoad){

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
                rawString = (String) document.get(Constants.SENTENCE_ATTRIBS.RAW);
                normalized = (String) document.get(Constants.SENTENCE_ATTRIBS.NORMALIZED);
                words = (List<String>) document.get(Constants.SENTENCE_ATTRIBS.WORDS);
                posTags = (List<String>) document.get(Constants.SENTENCE_ATTRIBS.POSTAG);
                generalizedSentence = (String) document.get(Constants.CORPUS_DB_ENTRY_ATTRIBS.GENERALIZED_SENTENCE);
                object = (String) document.get(Constants.CORPUS_DB_ENTRY_ATTRIBS.OBJECT);
                subject = (String) document.get(Constants.CORPUS_DB_ENTRY_ATTRIBS.SUBJECT);
                predicate = (String) document.get(Constants.CORPUS_DB_ENTRY_ATTRIBS.PREDICATE);
                occurrence = (int) document.get(Constants.CORPUS_DB_ENTRY_ATTRIBS.OCCURRENCE);
                //words = convertBasicDBListToJavaListOfStrings(wordsObject);
                //posTags = convertBasicDBListToJavaListOfStrings(postagObject);
                currentSentence = new Sentence(rawString,words,posTags,normalized);
                corpusEntryObject = new CorpusEntryObject(currentSentence, generalizedSentence, object, subject, predicate, occurrence);
                corpusDB.addEntry(corpusEntryObject);
            }

    }

    public void close(){
        mongo.close();
    }


    //TODO: temporarily here. move it
    @Test
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

    public void test(){
        Document firstGroup = new Document("$group",
                new Document("_id",
                        new Document("predicate", "$predicate")
                                .append("subject", "$subject")
                                .append("object", "$object"))
                        );

        Document secondGroup = new Document("$group",
                new Document("_id",
                        new Document("predicate", "$predicate"))
                        .append("count", new Document("$sum", 1)));

        List<Document> pipeline = Arrays.asList(firstGroup, secondGroup);
        AggregateIterable<Document> biadab = corpusTable.aggregate(pipeline).allowDiskUse(true);
        int temp = 0;

        for (Document d:
             biadab) {
            System.out.println(d.toString());
        }
    }

}
