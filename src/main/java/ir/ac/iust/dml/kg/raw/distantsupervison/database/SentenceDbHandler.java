package ir.ac.iust.dml.kg.raw.distantsupervison.database;

import com.mongodb.*;
import ir.ac.iust.dml.kg.raw.distantsupervison.Constants;
import ir.ac.iust.dml.kg.raw.distantsupervison.RawTextHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.Sentence;

import java.net.UnknownHostException;
import java.util.List;

import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpus;

/**
 * Created by hemmatan on 4/9/2017.
 */
public class SentenceDbHandler extends DbHandler {

    public void createCorpusTableFromWikiDump() {
        MongoClient mongo = null;
        try {
            mongo = new MongoClient(host, port);
            DB distantSupervisionDB = mongo.getDB(distantSupervisionDBName);

            DBCollection corpusTable = distantSupervisionDB.getCollection(sentencesTableName);

            RawTextHandler.loadRawText();
            RawTextHandler.buildCorpus();

            List<Sentence> sentenceList = corpus.getSentences();

            for (Sentence sentence:
                    sentenceList) {
                BasicDBObject dbSentence = new BasicDBObject();
                dbSentence.put(Constants.SENTENCE_ATTRIBS.RAW, sentence.getRaw());
                dbSentence.put(Constants.SENTENCE_ATTRIBS.WORDS, sentence.getWords());
                dbSentence.put(Constants.SENTENCE_ATTRIBS.POSTAG, sentence.getPosTagged());

                corpusTable.insert(dbSentence);
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void loadCorpusTable(){
        MongoClient mongo = null;
        try {
            mongo = new MongoClient(host, port);
            DB distantSupervisionDB = mongo.getDB(distantSupervisionDBName);

            DBCollection corpusTable = distantSupervisionDB.getCollection(sentencesTableName);
            DBCursor cursor = corpusTable.find();

            int cnt = 0;

            while (cursor.hasNext()){
                cnt+=1;
                DBObject dbSentence = cursor.next();
                String rawString = (String) dbSentence.get(Constants.SENTENCE_ATTRIBS.RAW);
                BasicDBList wordsObject = (BasicDBList) dbSentence.get(Constants.SENTENCE_ATTRIBS.WORDS);
                BasicDBList postagObject = (BasicDBList) dbSentence.get(Constants.SENTENCE_ATTRIBS.POSTAG);
                List<String> words = convertBasicDBListToJavaListOfStrings(wordsObject);
                List<String> posTags = convertBasicDBListToJavaListOfStrings(postagObject);
                Sentence currentSentence = new Sentence(rawString,words,posTags);
                corpus.addSentence(currentSentence);
            }

        } catch (UnknownHostException e){
            e.printStackTrace();
        }
    }
}
