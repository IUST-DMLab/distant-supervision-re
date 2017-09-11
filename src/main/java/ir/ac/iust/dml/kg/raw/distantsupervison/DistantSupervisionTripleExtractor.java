package ir.ac.iust.dml.kg.raw.distantsupervison;

import ir.ac.iust.dml.kg.raw.SentenceTokenizer;
import ir.ac.iust.dml.kg.raw.distantsupervison.models.Classifier;
import ir.ac.iust.dml.kg.raw.extractor.ResolvedEntityToken;
import ir.ac.iust.dml.kg.raw.triple.RawTriple;
import ir.ac.iust.dml.kg.raw.triple.RawTripleBuilder;
import ir.ac.iust.dml.kg.raw.triple.RawTripleExtractor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by hemmatan on 9/6/2017.
 */
public class DistantSupervisionTripleExtractor implements RawTripleExtractor {

    @Override
    public List<RawTriple> extract(String source, String version, String text) {
        Date date = new Date();
        List<RawTriple> result = new ArrayList<>();
        Classifier classifier = new Classifier();
        classifier.loadModels();
        final RawTripleBuilder builder = new RawTripleBuilder(Configuration.moduleName, source, date.getTime(), version);
        List<String> sentences = SentenceTokenizer.SentenceSplitterRaw(text);
        for (String sentence :
                sentences) {
            List<TripleGuess> triples = classifier.extractFromSingleSentenceString(sentence);
            for (TripleGuess tripleGuess :
                    triples) {
                RawTriple triple1 = builder.create()
                        .subject(tripleGuess.getSubject()).predicate(tripleGuess.getPredicate())
                        .object(tripleGuess.getObject()).rawText(sentence)
                        .accuracy(tripleGuess.getConfidence()).needsMapping(true);
                result.add(triple1);
            }
        }
        return result;
    }

    @Override
    public List<RawTriple> extract(String source, String version, List<List<ResolvedEntityToken>> text) {
        Date date = new Date();
        List<RawTriple> result = new ArrayList<>();
        Classifier classifier = new Classifier();
        classifier.loadModels();
        final RawTripleBuilder builder = new RawTripleBuilder(Configuration.moduleName, source, date.getTime(), version);


        for (List<ResolvedEntityToken> sentence :
                text) {
            String raw = "";
            for (ResolvedEntityToken token :
                    sentence) {
                raw += token.getWord() + " ";
            }

            List<TripleGuess> triples = classifier.extractFromSingleSentenceString(raw);
            for (TripleGuess tripleGuess :
                    triples) {
                RawTriple triple1 = builder.create()
                        .subject(tripleGuess.getSubject()).predicate(tripleGuess.getPredicate())
                        .object(tripleGuess.getObject()).rawText(raw)
                        .accuracy(tripleGuess.getConfidence()).needsMapping(true);
                result.add(triple1);
            }
        }
        return result;
    }
}
