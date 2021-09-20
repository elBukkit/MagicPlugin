package com.elmakers.mine.bukkit.utility.help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import com.elmakers.mine.bukkit.ChatUtils;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.MacroExpansion;
import com.elmakers.mine.bukkit.utility.Messages;

public class HelpTopic {
    public static final int MIN_WORD_LENGTH = 2;
    public static final int MIN_WHOLE_WORD_LENGTH = 4;
    public static final double COUNT_FACTOR = 0.5;
    public static final double SIMILARITY_FACTOR = 2;
    public static final double CONTENT_WEIGHT = 1;
    public static final double TAG_WEIGHT = 2;
    public static final double TITLE_WEIGHT = 4;
    public static final double TOTAL_WEIGHT = CONTENT_WEIGHT + TAG_WEIGHT + TITLE_WEIGHT;

    private final String key;
    private final String title;
    private final String text;
    private final String searchText;
    private final String tags;
    private final String topicType;
    private final String[] lines;
    private final Map<String, Integer> words;
    private final Set<String> titleWords;
    private final Set<String> tagWords;
    private final int maxCount;

    public HelpTopic(Messages messages, String key, String text, String tags, String topicType) {
        this.key = key;
        this.topicType = topicType;
        MacroExpansion expansion = messages.expandMacros(text);
        text = expansion.getText();
        text = CompatibilityLib.getCompatibilityUtils().translateColors(StringEscapeUtils.unescapeHtml(text));
        this.text = text;
        String simpleText = ChatColor.stripColor(ChatUtils.getSimpleMessage(text, true));
        this.searchText = simpleText.toLowerCase();
        this.title = expansion.getTitle();
        this.tags = tags + " " + expansion.getTags();

        // Pre-split simple description lines, remove title if present
        String[] allLines = StringUtils.split(simpleText, "\n");
        if (!title.isEmpty() && allLines.length > 1) {
            lines = Arrays.copyOfRange(allLines, 1, allLines.length);
        } else {
            lines = allLines;
        }

        // Track uses of individual words
        words = new HashMap<>();
        int maxCount = 0;
        titleWords = new HashSet<>();
        tagWords = new HashSet<>();
        List<String> helpTopicWords = new ArrayList<>();
        helpTopicWords.addAll(Arrays.asList(ChatUtils.getWords(searchText)));
        List titleWordList = Arrays.asList(ChatUtils.getWords(title.toLowerCase()));
        titleWords.addAll(titleWordList);
        helpTopicWords.addAll(titleWordList);
        helpTopicWords.addAll(Arrays.asList(ChatUtils.getWords(key)));
        List tagWordList = Arrays.asList(ChatUtils.getWords(tags.toLowerCase()));
        helpTopicWords.addAll(tagWordList);
        tagWords.addAll(tagWordList);
        helpTopicWords.addAll(Arrays.asList(ChatUtils.getWords(topicType)));
        for (String word : helpTopicWords) {
            word = word.trim();
            if (word.isEmpty()) continue;
            Integer count = words.get(word);
            if (count == null) count = 1;
            else count++;
            words.put(word, count);
            maxCount = Math.max(maxCount, count);
        }
        this.maxCount = maxCount;
    }

    @Nonnull
    public String getTitle() {
        if (title.isEmpty()) {
            String[] pieces = StringUtils.split(key, ".");
            return pieces.length > 1 ? pieces[pieces.length - 1] : key;
        }
        return title;
    }

    @Nonnull
    public String getText() {
        return text;
    }

    public double getRelevance(Help help, String keyword) {
        double wordsRelevance = getWordsRelevance(help, keyword);
        double titleRelevance = getSetRelevance(help, titleWords, keyword);
        double tagRelevance = getSetRelevance(help, tagWords, keyword);
        return (wordsRelevance * CONTENT_WEIGHT + titleRelevance * TITLE_WEIGHT + tagRelevance * TAG_WEIGHT) / TOTAL_WEIGHT;
    }

    private double getSetRelevance(Help help, Set<String> words, String keyword) {
        double relevance = 0;
        if (!isValidWord(keyword)) {
            return relevance;
        }
        keyword = keyword.trim();
        if (words.contains(keyword)) {
            return help.getWeight(keyword);
        }
        double maxSimilarity = 0;
        String bestMatch = null;
        for (String word : words) {
            double similarity = ChatUtils.getSimilarity(keyword, word);
            if (similarity > maxSimilarity) {
                bestMatch = word;
            }
        }
        if (bestMatch != null) {
            relevance = help.getWeight(bestMatch);
            double similarityWeight = Math.pow(maxSimilarity, SIMILARITY_FACTOR);
            relevance *= similarityWeight;
        }

        return relevance;
    }

    private double getWordsRelevance(Help help, String keyword) {
        double relevance = 0;
        if (!isValidWord(keyword)) {
            return relevance;
        }
        keyword = keyword.trim();
        Integer count = words.get(keyword);
        if (count != null) {
            double countWeight = (double)count / maxCount;
            return Math.pow(countWeight, COUNT_FACTOR) * help.getWeight(keyword);
        }
        double maxSimilarity = 0;
        String bestMatch = null;
        for (Map.Entry<String, Integer> entry : words.entrySet()) {
            String word = entry.getKey();
            double similarity = ChatUtils.getSimilarity(keyword, word);
            if (similarity > maxSimilarity) {
                count = entry.getValue();
                bestMatch = word;
            }
        }
        if (bestMatch != null) {
            double countWeight = (double)count / maxCount;
            relevance = Math.pow(countWeight, COUNT_FACTOR) * help.getWeight(bestMatch);
            double similarityWeight = Math.pow(maxSimilarity, SIMILARITY_FACTOR);
            relevance *= similarityWeight;
        }

        return relevance;
    }

    @Nullable
    public HelpTopicMatch match(Help help, Collection<String> keywords) {
        double relevance = 0;
        for (String keyword : keywords) {
            relevance += getRelevance(help, keyword);
        }
        if (relevance <= 0) {
            return null;
        }
        return new HelpTopicMatch(this, relevance / keywords.size());
    }

    public boolean isValidWord(String keyword) {
        if (keyword.length() < MIN_WORD_LENGTH) return false;
        if (keyword.length() < MIN_WHOLE_WORD_LENGTH && !words.containsKey(keyword)) return false;
        return true;
    }

    public Map<String, Integer> getWordCounts() {
        return words;
    }

    public String[] getLines() {
        return lines;
    }

    public String getKey() {
        return key;
    }

    public String getTopicType() {
        return topicType;
    }
}
