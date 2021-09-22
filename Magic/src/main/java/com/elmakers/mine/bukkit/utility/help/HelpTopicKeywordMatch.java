package com.elmakers.mine.bukkit.utility.help;

import java.util.Map;
import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.ChatUtils;

public class HelpTopicKeywordMatch {
    public static final int MIN_WHOLE_WORD_LENGTH = 4;
    public static final double HIGHLIGHT_CUTOFF = 0.6;

    public static double COUNT_FACTOR = 0.8;
    public static double WORD_FACTOR = 1.0;
    public static double SIMILARITY_FACTOR = 0.2;
    public static double COUNT_WEIGHT = 1;
    public static double WORD_WEIGHT = 10;

    private final String keyword;
    private final String word;
    private final double relevance;
    private final double similarity;
    private final double countWeight;

    private HelpTopicKeywordMatch(Help help, String keyword, String word, double countRatio, double similarity) {
        this.keyword = keyword.trim();
        this.word = word;
        this.similarity = Math.pow(similarity, SIMILARITY_FACTOR);
        this.countWeight = Math.pow(countRatio, COUNT_FACTOR);

        double wordWeight = help.getWeight(word);
        wordWeight = wordWeight * Math.pow(wordWeight, WORD_FACTOR);
        double totalWeight = COUNT_WEIGHT + WORD_WEIGHT;
        double relevance = (countWeight * COUNT_WEIGHT + wordWeight * WORD_WEIGHT) / totalWeight;
        this.relevance = relevance * similarity;
    }

    public String getDebugText(Help help) {
        double wordWeight = help.getWeight(word);
        return "Matched " + word + ": "
                + ChatUtils.printPercentage(similarity)
                + " x ("
                + "Count: "
                + ChatUtils.printPercentage(countWeight)
                + "x" + (int)COUNT_WEIGHT
                + " + Word: "
                + ChatUtils.printPercentage(Math.pow(wordWeight, WORD_FACTOR))
                + "x" + (int)WORD_WEIGHT
                + ") ["
                + help.getDebugText(word) + "]";
    }

    @Nullable
    public static HelpTopicKeywordMatch match(String keyword, HelpTopic topic, Help help) {
        if (!help.isValidWord(keyword)) {
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
        if (bestMatch == null || !help.isValidWord(bestMatch)) {
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
