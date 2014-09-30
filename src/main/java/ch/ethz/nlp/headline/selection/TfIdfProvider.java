package ch.ethz.nlp.headline.selection;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.cache.AnnotationProvider;
import ch.ethz.nlp.headline.util.CoreNLPUtil;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.BinaryHeapPriorityQueue;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PriorityQueue;

public class TfIdfProvider {

    private final int numDocuments;

    /**
     * For each lemma, stores the number of documents in which it occurs.
     */
    private final Map<String, Integer> documentFrequencies;

    private TfIdfProvider(int numDocs) {
        this.documentFrequencies = new HashMap<>();
        this.numDocuments = numDocs;
    }

    public static TfIdfProvider of(AnnotationProvider annotationProvider,
                                   Dataset dataset) {
        int counter = 0;
        TfIdfProvider result = new TfIdfProvider(dataset.getDocuments().size());

        System.out.println("total size: " + dataset.getDocuments().size());

        for (Document document : dataset.getDocuments()) {
            System.out.println("Parsed " + counter);
            ++counter;
            String content = document.getContent();
            Annotation annotation = annotationProvider.getAnnotation(content);
            Multiset<String> lemmaFreqs = getLemmaFreqs(annotation);
            for (String lemma : lemmaFreqs.elementSet()) {
                int newFrequency = result.getDocumentFrequency(lemma) + 1;
                result.documentFrequencies.put(lemma, newFrequency);
            }
        }

        System.out.println("DONE");

        return result;
    }

    /**
     * Compute the frequency of each term in the given annotation.
     */
    protected static Multiset<String> getLemmaFreqs(Annotation annotation) {
        CoreNLPUtil.ensureLemmaAnnotation(annotation);
        Multiset<String> termFreqs = HashMultiset.create();

        for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
            for (CoreLabel label : sentence.get(TokensAnnotation.class)) {
                termFreqs.add(label.lemma());
            }
        }

        return termFreqs;
    }

    public int getNumDocuments() {
        return numDocuments;
    }

    public int getDocumentFrequency(String lemma) {
        Integer result = documentFrequencies.get(lemma);
        return (result == null) ? 0 : result;
    }

    /**
     * Compute the tf-idf score for each lemma in the given annotation.
     */
    public PriorityQueue<String> getTfIdfMap(Annotation annotation) {
        Multiset<String> lemmaFreqs = getLemmaFreqs(annotation);
        PriorityQueue<String> tfIdfMap = new BinaryHeapPriorityQueue<>();

        for (String lemma : lemmaFreqs.elementSet()) {
            double lemmaFreq = lemmaFreqs.count(lemma);
            double docFreq = getDocumentFrequency(lemma);
            if (docFreq > 0) {
                double inverseDocFreq = Math.log(getNumDocuments() / docFreq);
                double tfIdf = lemmaFreq * inverseDocFreq;
                tfIdfMap.add(lemma, tfIdf);
            }
        }

        return tfIdfMap;
    }

}
