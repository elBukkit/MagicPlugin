package com.elmakers.mine.bukkit.utility.help;

import java.util.Map;
import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.ChatUtils;

public class HelpTopicKeywordMatch {
    public static final int MIN_WHOLE_WORD_LENGTH = 4;
    public static final double COUNT_FACTOR = 0.5;
    public static final double SIMILARITY_FACTOR = 2;

    private final String keyword;
    private final String word;
    private final double relevance;

    private HelpTopicKeywordMatch(String keyword, String word, double relevance) {
        this.keyword = keyword.trim();
        this.word = word;
        this.relevance = relevance;
    }

    @Nullable
    public static HelpTopicKeywordMatch match(String keyword, HelpTopic topic, Help help) {
        if (!topic.isValidWord(keyword)) {
            return null;
        }
        keyword = keyword.trim();
        Integer count = topic.words.get(keyword);
        if (count != null) {
            double countWeight = (double)count / topic.maxCount;
            return new HelpTopicKeywordMatch(keyword, keyword, Math.pow(countWeight, COUNT_FACTOR) * help.getWeight(keyword));
        }

        double relevance = 0;
        double maxSimilarity = 0;
        String bestMatch = null;
        for (Map.Entry<String, Integer> entry : topic.words.entrySet()) {
            String word = entry.getKey();
            double similarity = ChatUtils.getSimilarity(keyword, word);
            if (similarity > maxSimilarity) {
                count = entry.getValue();
                bestMatch = word;
                maxSimilarity = similarity;
            }
        }
        if (bestMatch != null) {
            double countWeight = (double)count / topic.maxCount;
            relevance = Math.pow(countWeight, COUNT_FACTOR) * help.getWeight(bestMatch);
            double similarityWeight = Math.pow(maxSimilarity, SIMILARITY_FACTOR);
            relevance *= similarityWeight;
        }

        return new HelpTopicKeywordMatch(keyword, bestMatch, relevance);
    }

    public String getWord() {
        return word;
    }

    public double getRelevance() {
        return relevance;
    }

    public boolean allowHighlight(HelpTopic topic) {
        // TODO: Remove this hack eventually?
        // This prevents a lot of false-positives in identifying where a matched word is
        // It might be better to divide up topics into word sets per line?
        if (keyword.length() < MIN_WHOLE_WORD_LENGTH && !topic.words.containsKey(keyword)) return false;
        return true;
    }
}
