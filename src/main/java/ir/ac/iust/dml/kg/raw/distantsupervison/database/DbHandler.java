package ir.ac.iust.dml.kg.raw.distantsupervison.database;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mongodb.BasicDBList;
import ir.ac.iust.dml.kg.raw.Normalizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.Configuration;
import ir.ac.iust.dml.kg.raw.distantsupervison.Constants;
import ir.ac.iust.dml.kg.raw.distantsupervison.CorpusEntryObject;
import ir.ac.iust.dml.kg.raw.distantsupervison.Sentence;
import ir.ac.iust.dml.kg.resource.extractor.client.ExtractorClient;
import ir.ac.iust.dml.kg.resource.extractor.client.MatchedResource;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hemmatan on 4/9/2017.
 */
public class DbHandler {
    protected static final String host = "localhost";
    protected static final int port = 27017;
    protected static final String distantSupervisionDBName = "DistantSupervision";
    protected static final String patternsTableName = "patterns";
    protected static final String sentencesTableName = "sentences";
    public static final String corpusTableName = "corpus_lt20";
    public static final String trainTableName = "train";
    public static final String testTableName = "test";



    public List<String> convertBasicDBListToJavaListOfStrings(BasicDBList basicDBList){
        List<String> result = new ArrayList<>();
        for (Object obj:
             basicDBList) {
            result.add((String) obj);
        }
        return result;
    }


    //TODO: :)
    @org.junit.Test
    public void saveCorpusJasonToDB() {

        String tempCorpusJasonPath = "C:\\Users\\hemmatan\\IdeaProjects\\RE\\Corpus5.json";
        try (JsonReader reader = new JsonReader(new FileReader(tempCorpusJasonPath));
        ) {
            reader.beginArray();
            int cnt = 0;
            JsonToken nextToken = reader.peek();

            CorpusDbHandler corpusDbHandler = new CorpusDbHandler(corpusTableName);
            ExtractorClient client = new ExtractorClient(Configuration.extractorClient);

            while (reader.hasNext()) {
                if (JsonToken.BEGIN_OBJECT.equals(nextToken)) {
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
                    while (!JsonToken.END_OBJECT.equals(nextToken2)) {
                        String newSentence = reader.nextName();
                        int occur = Integer.valueOf(reader.nextString());

                        String originalSentence = newSentence.replace(Constants.sentenceAttribs.SUBJECT_ABV, subject);
                        originalSentence = originalSentence.replace(Constants.sentenceAttribs.OBJECT_ABV, object);


                        List<String> objectType = new ArrayList<>();
                        List<String> subjectType = new ArrayList<>();

                        final List<MatchedResource> result_object = client.match(object);
                        final List<MatchedResource> result_subject = client.match(subject);

                        if (result_object == null || result_object.size() == 0 || result_object.get(0).getResource() == null)
                            objectType.add("null");
                        else if (result_object.get(0).getResource().getClassTree() == null || result_object.get(0).getResource().getClassTree().size() == 0)
                            objectType.add(result_object.get(0).getResource().getIri());
                        else objectType.addAll(result_object.get(0).getResource().getClassTree());

                        if (result_subject == null || result_subject.size() == 0 || result_subject.get(0).getResource() == null)
                            subjectType.add("null");
                        else if (result_subject.get(0).getResource().getClassTree() == null || result_subject.get(0).getResource().getClassTree().size() == 0)
                            subjectType.add(result_subject.get(0).getResource().getIri());
                        else subjectType.addAll(result_subject.get(0).getResource().getClassTree());


                        Sentence sentence = new Sentence(originalSentence);
                        //String generalizedNormalizedSentence = sentence.getNormalized().replace(subject, Constants.sentenceAttribs.SUBJECT_ABV);
                        //generalizedNormalizedSentence = generalizedNormalizedSentence.replace(object, Constants.sentenceAttribs.OBJECT_ABV);
                        String generalizedNormalizedSentence = Normalizer.normalize(newSentence);
                        CorpusEntryObject corpusEntryObject = new CorpusEntryObject(sentence, generalizedNormalizedSentence, object, subject, objectType, subjectType, predicate, occur);
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
