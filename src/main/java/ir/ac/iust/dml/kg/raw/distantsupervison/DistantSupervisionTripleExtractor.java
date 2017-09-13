package ir.ac.iust.dml.kg.raw.distantsupervison;

import ir.ac.iust.dml.kg.raw.distantsupervison.models.Classifier;
import ir.ac.iust.dml.kg.raw.extractor.EnhancedEntityExtractor;
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

    private final Classifier classifier;

    public DistantSupervisionTripleExtractor() {
        classifier = new Classifier();
        classifier.loadModels();
    }

    @Override
    public List<RawTriple> extract(String source, String version, String text) {
        List<RawTriple> result;
        EnhancedEntityExtractor enhancedEntityExtractor = new EnhancedEntityExtractor();
        List<List<ResolvedEntityToken>> sentences = enhancedEntityExtractor.extract(text);
        enhancedEntityExtractor.disambiguateByContext(sentences, Configuration.contextDisambiguationThreshold);
        result = extract(source, version, sentences);
        return result;
    }

    @Override
    public List<RawTriple> extract(String source, String version, List<List<ResolvedEntityToken>> text) {
        Date date = new Date();
        List<RawTriple> result = new ArrayList<>();
        final RawTripleBuilder builder = new RawTripleBuilder(Configuration.moduleName, source, date.getTime(), version);
        List<TripleGuess> triples = classifier.extractFromSingleSentenceString(text);
        for (TripleGuess tripleGuess :
                    triples) {
                RawTriple triple1 = builder.create()
                        .subject(tripleGuess.getSubject()).predicate(tripleGuess.getPredicate())
                        .object(tripleGuess.getObject()).rawText(tripleGuess.getOriginSentence())
                        .accuracy(tripleGuess.getConfidence()).needsMapping(true);
            if (triple1.getAccuracy() > 0.9)
                result.add(triple1);
        }
        return result;
    }
}
