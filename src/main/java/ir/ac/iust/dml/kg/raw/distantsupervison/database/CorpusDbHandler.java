package ir.ac.iust.dml.kg.raw.distantsupervison.database;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mongodb.*;
import ir.ac.iust.dml.kg.raw.Normalizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.Constants;
import ir.ac.iust.dml.kg.raw.distantsupervison.CorpusEntryObject;
import ir.ac.iust.dml.kg.raw.distantsupervison.RawTextHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.Sentence;

import java.io.FileReader;
import java.net.UnknownHostException;
import java.util.List;

import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpus;
import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpusDB;

/**
 * Created by hemmatan on 4/18/2017.
 */
public class CorpusDbHandler extends DbHandler {

    private MongoClient mongo = null;
    private DB distantSupervisionDB;
    private DBCollection corpusTable;

    public CorpusDbHandler() {
        try {
            mongo = new MongoClient(host, port);
        }
        catch(UnknownHostException e){
            e.printStackTrace();
        }

        distantSupervisionDB = mongo.getDB(distantSupervisionDBName);
        corpusTable = distantSupervisionDB.getCollection(corpusTableName);
    }

    public void insert(CorpusEntryObject corpusEntryObject) {

            BasicDBObject basicDBObject = new BasicDBObject();
            basicDBObject.put(Constants.SENTENCE_ATTRIBS.RAW, corpusEntryObject.getOriginalSentence().getRaw());
            basicDBObject.put(Constants.SENTENCE_ATTRIBS.NORMALIZED, corpusEntryObject.getOriginalSentence().getNormalized());
            basicDBObject.put(Constants.SENTENCE_ATTRIBS.WORDS, corpusEntryObject.getOriginalSentence().getWords());
            basicDBObject.put(Constants.SENTENCE_ATTRIBS.POSTAG, corpusEntryObject.getOriginalSentence().getPosTagged());
            basicDBObject.put(Constants.CORPUS_DB_ENTRY_ATTRIBS.GENERALIZED_SENTENCE, corpusEntryObject.getGeneralizedSentence());
            basicDBObject.put(Constants.CORPUS_DB_ENTRY_ATTRIBS.SUBJECT, corpusEntryObject.getSubject());
            basicDBObject.put(Constants.CORPUS_DB_ENTRY_ATTRIBS.OBJECT, corpusEntryObject.getObject());
        basicDBObject.put(Constants.CORPUS_DB_ENTRY_ATTRIBS.PREDICATE, corpusEntryObject.getPredicate());
        basicDBObject.put(Constants.CORPUS_DB_ENTRY_ATTRIBS.OCCURRENCE, corpusEntryObject.getOccurrence());

            corpusTable.insert(basicDBObject);
    }

    public void load(int numberOfEntriesToLoad){

        DBCursor cursor = corpusTable.find();
        int cnt = 0;
        DBObject dbObject;
        String rawString;
        String normalized;
        BasicDBList wordsObject;
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
                dbObject = cursor.next();
                rawString = (String) dbObject.get(Constants.SENTENCE_ATTRIBS.RAW);
                normalized = (String) dbObject.get(Constants.SENTENCE_ATTRIBS.NORMALIZED);
                wordsObject = (BasicDBList) dbObject.get(Constants.SENTENCE_ATTRIBS.WORDS);
                postagObject = (BasicDBList) dbObject.get(Constants.SENTENCE_ATTRIBS.POSTAG);
                generalizedSentence = (String) dbObject.get(Constants.CORPUS_DB_ENTRY_ATTRIBS.GENERALIZED_SENTENCE);
                object = (String) dbObject.get(Constants.CORPUS_DB_ENTRY_ATTRIBS.OBJECT);
                subject = (String) dbObject.get(Constants.CORPUS_DB_ENTRY_ATTRIBS.SUBJECT);
                predicate = (String) dbObject.get(Constants.CORPUS_DB_ENTRY_ATTRIBS.PREDICATE);
                occurrence = (int) dbObject.get(Constants.CORPUS_DB_ENTRY_ATTRIBS.OCCURRENCE);
                words = convertBasicDBListToJavaListOfStrings(wordsObject);
                posTags = convertBasicDBListToJavaListOfStrings(postagObject);
                currentSentence = new Sentence(rawString,words,posTags,normalized);
                corpusEntryObject = new CorpusEntryObject(currentSentence, generalizedSentence, object, subject, predicate, occurrence);
                corpusDB.addEntry(corpusEntryObject);
            }

    }

    public void close(){
        mongo.close();
    }


    //TODO: temporarily here. move it later
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
}
