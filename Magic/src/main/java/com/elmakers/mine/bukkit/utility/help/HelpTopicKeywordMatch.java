package com.elmakers.mine.bukkit.utility.help;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.utility.ChatUtils;

public class HelpTopicKeywordMatch {
    public static final int MIN_WHOLE_WORD_LENGTH = 4;
    public static final double HIGHLIGHT_CUTOFF = 0.6;

    private final String keyword;
    private final String word;
    private final double relevance;
    private final double similarity;
    private final double countWeight;

    private HelpTopicKeywordMatch(Help help, String keyword, String word, double countRatio, double similarity) {
        this.keyword = keyword.trim();
        this.word = word;
        this.similarity = Math.pow(similarity, SearchFactors.SIMILARITY_FACTOR);
        this.countWeight = Math.pow(countRatio, SearchFactors.COUNT_FACTOR);

        double wordWeight = help.getWeight(word);
        wordWeight = wordWeight * Math.pow(wordWeight, SearchFactors.WORD_FACTOR);
        double totalWeight = SearchFactors.COUNT_WEIGHT + SearchFactors.WORD_WEIGHT;
        double relevance = (countWeight * SearchFactors.COUNT_WEIGHT + wordWeight * SearchFactors.WORD_WEIGHT) / totalWeight;
        this.relevance = relevance * similarity;
    }

    public String getDebugText(Help help) {
        double wordWeight = help.getWeight(word);
        return "Matched " + word + ": "
                + ChatUtils.printPercentage(similarity)
                + " x ("
                + "Count: "
                + ChatUtils.printPercentage(countWeight)
                + "x" + (int) SearchFactors.COUNT_WEIGHT
                + " + Word: "
                + ChatUtils.printPercentage(Math.pow(wordWeight, SearchFactors.WORD_FACTOR))
                + "x" + (int) SearchFactors.WORD_WEIGHT
                + ") ["
                + help.getDebugText(word) + "]";
    }

    @Nullable
    public static HelpTopicKeywordMatch match(String keyword, Set<String> words, Help help, double minSimilarity) {
        if (!help.isValidWord(keyword)) {
            return null;
        }
        keyword = keyword.trim();
        if (words.contains(keyword)) {
            return new HelpTopicKeywordMatch(help, keyword, keyword, 1 / SearchFactors.COUNT_MAX, 1);
        }

        double maxSimilarity = 0;
        String bestMatch = null;
        for (String word : words) {
            double similarity = ChatUtils.getSimilarity(keyword, word);
            if (similarity > maxSimilarity && similarity >= minSimilarity) {
                bestMatch = word;
                maxSimilarity = similarity;
            }
        }
        if (bestMatch == null || !help.isValidWord(bestMatch)) {
            return null;
        }
        return new HelpTopicKeywordMatch(help, keyword, bestMatch, 1 / SearchFactors.COUNT_MAX, maxSimilarity);
    }

    @Nullable
    public static HelpTopicKeywordMatch match(String keyword, HelpTopic topic, Help help) {
        if (!help.isValidWord(keyword)) {
            return null;
        }
        keyword = keyword.trim();
        Integer count = topic.words.get(keyword);
        if (count != null) {
            double countWeight = Math.min(count, SearchFactors.COUNT_MAX) / SearchFactors.COUNT_MAX;
            return new HelpTopicKeywordMatch(help, keyword, keyword, countWeight, 1);
        }

        double maxSimilarity = 0;
        String bestMatch = null;
        for (Map.Entry<String, Integer> entry : topic.words.entrySet()) {
            String word = entry.getKey();
            double similarity = ChatUtils.getSimilarity(keyword, word);
            if (similarity > maxSimilarity && similarity >= SearchFactors.MIN_SIMILARITY) {
                count = entry.getValue();
                bestMatch = word;
                maxSimilarity = similarity;
            }
        }
        if (bestMatch == null || !help.isValidWord(bestMatch)) {
            return null;
        }
        double countWeight = Math.min(count, SearchFactors.COUNT_MAX) / SearchFactors.COUNT_MAX;
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
