package ir.ac.iust.dml.kg.raw.distantsupervison.database;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import ir.ac.iust.dml.kg.raw.distantsupervison.Constants;
import ir.ac.iust.dml.kg.raw.distantsupervison.Pattern;

import java.net.UnknownHostException;

/**
 * Created by hemmatan on 4/9/2017.
 */
public class PatternsDbHandler extends DbHandler {

    public void addToPatternTable(Pattern pattern) {
        MongoClient mongo = null;
            mongo = new MongoClient(host, port);
            DB distantSupervisionDB = mongo.getDB(distantSupervisionDBName);

            DBCollection patternTable = distantSupervisionDB.getCollection(patternsTableName);

            BasicDBObject dbPattern = new BasicDBObject();

            dbPattern.put(Constants.PATTERN_ATTRIBS.RELATION_NAME, pattern.getRelationName());
            dbPattern.put(Constants.PATTERN_ATTRIBS.FREQUENCY, pattern.getFrequency());
            dbPattern.put(Constants.PATTERN_ATTRIBS.PATTERN, pattern.getPattern());
            dbPattern.put(Constants.PATTERN_ATTRIBS.OBJECT_ENTITY_TYPE, pattern.getObjectEntityType());
            dbPattern.put(Constants.PATTERN_ATTRIBS.SUBJECT_ENTITY_TYPE, pattern.getSubjectEntityType());

            patternTable.insert(dbPattern);


    }


}
