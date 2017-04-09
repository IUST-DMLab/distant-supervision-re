package ir.ac.iust.dml.kg.raw.distantsupervison.database;

import com.mongodb.*;
import ir.ac.iust.dml.kg.raw.distantsupervison.Constants;
import ir.ac.iust.dml.kg.raw.distantsupervison.Pattern;
import ir.ac.iust.dml.kg.raw.distantsupervison.RawTextHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.Sentence;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources.corpus;

/**
 * Created by hemmatan on 4/9/2017.
 */
public class DbHandler {
    protected static final String host = "localhost";
    protected static final int port = 27017;
    protected static final String distantSupervisionDBName = "DistantSupervision";
    protected static final String patternsTableName = "patterns";
    protected static final String corpusTableName = "sentences";


    public List<String> convertBasicDBListToJavaListOfStrings(BasicDBList basicDBList){
        List<String> result = new ArrayList<>();
        for (Object obj:
             basicDBList) {
            result.add((String) obj);
        }
        return result;
    }



}
