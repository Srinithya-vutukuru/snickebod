package org.ethz.nlp.headline.generators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.ethz.nlp.headline.Document;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

public class PosFilteredGenerator implements Generator {

	private final StanfordCoreNLP pipeline;
	private final Set<String> openTags;

	public PosFilteredGenerator() throws ClassNotFoundException, IOException {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos");
		pipeline = new StanfordCoreNLP(props);

		// Temporarily create a tagger to gain access to the list of open tags
		MaxentTagger tagger = new MaxentTagger(MaxentTagger.DEFAULT_JAR_PATH);
		openTags = tagger.getTags().getOpenTags();
	}

	@Override
	public String getId() {
		return "POS-F";
	}

	@Override
	public String generate(Document document) throws IOException {
		String content = document.load();
		Annotation annotation = new Annotation(content);
		pipeline.annotate(annotation);

		CoreMap sentenceMap = annotation.get(SentencesAnnotation.class).get(0);
		List<CoreLabel> labels = sentenceMap.get(TokensAnnotation.class);
		List<String> wordsWithOpenTag = new ArrayList<>();

		for (CoreLabel label : labels) {
			String tag = label.get(PartOfSpeechAnnotation.class);
			if (openTags.contains(tag)) {
				wordsWithOpenTag.add(label.word());
			}
		}

		return StringUtils.join(wordsWithOpenTag);
	}

}
