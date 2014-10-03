package ch.ethz.nlp.headline.generators;

import ch.ethz.nlp.headline.cache.AnnotationCache;
import ch.ethz.nlp.headline.util.CoreNLPUtil;
import ch.ethz.nlp.headline.selection.ScoredSentencesSelector;
import ch.ethz.nlp.headline.selection.TfIdfProvider;
import edu.stanford.nlp.pipeline.Annotation;

/**
 * Uses the sentence whose words have the highest average tf-idf score.
 */
public class CombinedSentenceGenerator extends CoreNLPGenerator {

    private final ScoredSentencesSelector sentencesSelector;

    public CombinedSentenceGenerator(AnnotationCache cache,
                                     TfIdfProvider tfIdfProvider) {
        super(cache);

        this.sentencesSelector = new ScoredSentencesSelector(tfIdfProvider);
    }

    @Override
    public String getId() {
        return "COMBINED-SENTENCE";
    }

    @Override
    protected Annotation generate(Annotation annotation) {
        annotation = sentencesSelector.select(annotation);
        return annotation;
    }

}
