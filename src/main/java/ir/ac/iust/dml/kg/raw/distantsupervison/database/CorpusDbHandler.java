package ir.ac.iust.dml.kg.raw.distantsupervison.database;

import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import ir.ac.iust.dml.kg.raw.distantsupervison.*;
import org.bson.Document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by hemmatan on 4/18/2017.
 */
public class CorpusDbHandler extends DbHandler {

    private MongoClient mongo = null;
    private MongoDatabase distantSupervisionDB;
    private MongoCollection<Document> corpusTable;

    public CorpusDbHandler(String tableName) {
        mongo = new MongoClient(host, port);
        distantSupervisionDB = mongo.getDatabase(distantSupervisionDBName);
        corpusTable = distantSupervisionDB.getCollection(tableName);
    }

    public void deleteAll() {
        corpusTable.deleteMany(new Document());
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
        List<String> predicates = getMostFrequentPredicates(Configuration.maximumNumberOfPredicatesToLoad);

        loadByPredicates(destinationCorpusDB, numberOfEntriesToLoad, predicates, new HashSet<>());
    }

    private void loadByPredicates(CorpusDB destinationCorpusDB, int numberOfEntriesToLoad, List<String> predicates, Set<String> testIDs) {
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
            if ((destinationCorpusDB.getPredicateCounts().containsKey(predicate) &&
                    destinationCorpusDB.getPredicateCounts().get(predicate) >= Configuration.maximumNoOfInstancesForEachPredicate)
                    || testIDs.contains(document.get("_id").toString())) {
                if (testIDs.contains(document.get("_id").toString()))
                    System.out.println(document.get("_id").toString());
                continue;
            }
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

        Configuration.noOfTrainExamples = destinationCorpusDB.getEntries().size();
    }


    public List<String> getMostFrequentPredicates(int numberOfEntriesToLoad){

        Document secondGroup = new Document("$group",
                new Document("_id",
                        new Document("predicate", "$predicate"))
                        .append("count", new Document("$sum", 1)));

        Document sort = new Document("$sort", new Document("count", -1));

        List<Document> pipeline = new ArrayList<Document>(Arrays.asList(secondGroup));
        pipeline.add(sort);
        AggregateIterable<Document> documents = corpusTable.aggregate(pipeline).allowDiskUse(true);


        int cnt = 0;
        List<String> result = new ArrayList<>();
        for (Document d:
                documents) {
            if (cnt++ >= numberOfEntriesToLoad)
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


    public void loadByReadingPedicatesFromFile(CorpusDB destinationCorpusDB, int numberOfEntriesToLoad, Set<String> testIDs, String predicatesFile) {
        List<String> predicates = readPredicatesFromFile(Configuration.maximumNumberOfPredicatesToLoad, predicatesFile);
        loadByPredicates(destinationCorpusDB, numberOfEntriesToLoad, predicates, testIDs);
    }

    public static List<String> readPredicatesFromFile(int numberOfPredicatesToLoad, String predicatesFile) {
        List<String> predicates = new ArrayList<>();
        int cnt = 0;
        try (Scanner scanner = new Scanner(new FileInputStream(predicatesFile))) {
            while (scanner.hasNextLine() && cnt < numberOfPredicatesToLoad) {
                String line = scanner.nextLine();
                predicates.add(line);
                cnt++;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return predicates;
    }


}
