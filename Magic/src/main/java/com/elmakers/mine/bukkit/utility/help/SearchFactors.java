package com.elmakers.mine.bukkit.utility.help;

public class SearchFactors {
    // HelpTopicMatch
    public static double CONTENT_FACTOR = 1.5;
    public static double TAG_FACTOR = 0.75;
    public static double EXTRA_FACTOR = 0.5;
    public static double TITLE_FACTOR = 1.2;
    public static double CONTENT_WEIGHT = 1;
    public static double TAG_WEIGHT = 1.1;
    public static double TITLE_WEIGHT = 1.3;
    public static double EXTRA_WEIGHT = 0.25;

    // HelpTopicWord
    public static double RARITY_FACTOR = 0.4;
    public static double TOPIC_RARITY_FACTOR = 1.5;
    public static double LENGTH_FACTOR = 0.5;
    public static double RARITY_WEIGHT = 1.2;
    public static double TOPIC_RARITY_WEIGHT = 1.0;
    public static double LENGTH_WEIGHT = 1.1;

    // HelpTopicKeywordMatch
    public static double COUNT_FACTOR = 0.4;
    public static double WORD_FACTOR = 0.5;
    public static double SIMILARITY_FACTOR = 1.2;
    public static double COUNT_WEIGHT = 1;
    public static double WORD_WEIGHT = 0.8;
    public static double COUNT_MAX = 3;
    public static double MIN_SIMILARITY = 0.6;
    public static double MIN_TITLE_SIMILARITY = 0.9;
    public static double MIN_TAG_SIMILARITY = 0.8;
    public static double MIN_EXTRA_SIMILARITY = 0.95;

    // Computed values last
    public static double MAX_WEIGHT = Math.max(SearchFactors.EXTRA_WEIGHT, Math.max(Math.max(SearchFactors.CONTENT_WEIGHT, SearchFactors.TITLE_WEIGHT), SearchFactors.TAG_WEIGHT));
}
