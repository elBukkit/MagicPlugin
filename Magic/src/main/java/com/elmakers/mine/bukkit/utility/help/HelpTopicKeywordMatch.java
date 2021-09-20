package com.elmakers.mine.bukkit.utility.help;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.ChatUtils;

public class HelpTopicKeywordMatch {
    public static final int MIN_WHOLE_WORD_LENGTH = 4;
    public static final double COUNT_FACTOR = 0.5;
    public static final double WORD_FACTOR = 1.5;
    public static final double SIMILARITY_FACTOR = 2;
    public static final double HIGHLIGHT_CUTOFF = 0.6;

    public static final double COUNT_WEIGHT = 0.5;
    public static final double SIMILARITY_WEIGHT = 4;
    public static final double WORD_WEIGHT = 2;
    public static final double TOTAL_WEIGHT = COUNT_WEIGHT + SIMILARITY_WEIGHT + WORD_WEIGHT;

    private final String keyword;
    private final String word;
    private final double relevance;
    private final double similarity;

    private HelpTopicKeywordMatch(Help help, String keyword, String word, double countRatio, double similarity) {
        double wordValue = help.getWeight(keyword);

        this.keyword = keyword.trim();
        this.word = word;
        this.similarity = similarity;

        this.relevance = (Math.pow(countRatio, COUNT_FACTOR) * COUNT_WEIGHT
            + Math.pow(wordValue, WORD_FACTOR) * WORD_WEIGHT
            + Math.pow(similarity, SIMILARITY_FACTOR) * SIMILARITY_WEIGHT
        ) / TOTAL_WEIGHT;
    }

    @Nullable
    public static HelpTopicKeywordMatch match(String keyword, Set<String> words, HelpTopic topic, Help help) {
        if (!topic.isValidWord(keyword)) {
            return null;
        }
        keyword = keyword.trim();
        if (words.contains(keyword)) {
            return new HelpTopicKeywordMatch(help, keyword, keyword, 1, 1);
        }

        double maxSimilarity = 0;
        String bestMatch = null;
        for (String word : words) {
            double similarity = ChatUtils.getSimilarity(keyword, word);
            if (similarity > maxSimilarity) {
                bestMatch = word;
                maxSimilarity = similarity;
            }
        }
        if (bestMatch == null) {
            return null;
        }
        return new HelpTopicKeywordMatch(help, keyword, bestMatch, 1, maxSimilarity);
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
            return new HelpTopicKeywordMatch(help, keyword, keyword, countWeight, 1);
        }

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
        if (bestMatch == null) {
            return null;
        }
        double countWeight = (double)count / topic.maxCount;
        return new HelpTopicKeywordMatch(help, keyword, bestMatch, countWeight, maxSimilarity);
    }

    public String getWord() {
        return word;
    }

    public String getKeyword() {
        return keyword;
    }

    public double getRelevance() {
        return relevance;
    }

    public double getSimilarity() {
        return similarity;
    }

    public boolean allowHighlight(HelpTopic topic) {
        if (similarity < HIGHLIGHT_CUTOFF) return false;
        // TODO: Remove this hack eventually?
        // This prevents a lot of false-positives in identifying where a matched word is
        // It might be better to divide up topics into word sets per line?
        if (keyword.length() < MIN_WHOLE_WORD_LENGTH && !topic.words.containsKey(keyword)) return false;
        return true;
    }
}
