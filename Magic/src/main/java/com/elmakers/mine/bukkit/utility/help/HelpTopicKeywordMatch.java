package com.elmakers.mine.bukkit.utility.help;

public class HelpTopicKeywordMatch {
    private final String keyword;
    private final String word;
    private final double relevance;

    public HelpTopicKeywordMatch(String keyword, String word, double relevance) {
        this.keyword = keyword.trim();
        this.word = word;
        this.relevance = relevance;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getWord() {
        return word;
    }

    public double getRelevance() {
        return relevance;
    }
}
