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

import org.bukkit.ChatColor;

import com.elmakers.mine.bukkit.utility.ChatUtils;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.MacroExpansion;
import com.elmakers.mine.bukkit.utility.Messages;
import com.elmakers.mine.bukkit.utility.StringEscapeUtils;
import com.elmakers.mine.bukkit.utility.StringUtils;

public class HelpTopic {
    private final String key;
    private final String title;
    private final String text;
    private final String extraText;
    private final String topicType;
    private final String[] lines;
    private final double weight;
    protected final Map<String, Integer> words;
    protected final Set<String> titleWords;
    protected final Set<String> tagWords;
    protected final Set<String> extraWords;
    protected final int maxCount;

    public HelpTopic(Messages messages, String key, String text, String extraText, String tags, String topicType, double weight) {
        this.key = key;
        this.topicType = topicType;
        this.weight = weight;
        MacroExpansion expansion = messages.expandMacros(text);
        text = expansion.getText();
        text = CompatibilityLib.getCompatibilityUtils().translateColors(StringEscapeUtils.unescapeHtml(text));
        this.text = text;
        String simpleText = ChatColor.stripColor(ChatUtils.getSimpleMessage(text, true));
        String searchText = simpleText.toLowerCase();
        this.title = expansion.getTitle();

        MacroExpansion extraExpansion = messages.expandMacros(extraText);
        extraText = extraExpansion.getText();
        extraText = CompatibilityLib.getCompatibilityUtils().translateColors(StringEscapeUtils.unescapeHtml(extraText));
        this.extraText = extraText;

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
        this.extraWords = new HashSet<>();
        List<String> otherWordList = Arrays.asList(ChatUtils.getWords(extraText.toLowerCase()));
        for (String otherWord : otherWordList) {
            if (otherWord.length() < Help.MIN_WORD_LENGTH) continue;
            this.extraWords.add(otherWord);
        }
        List<String> helpTopicWords = new ArrayList<>();
        helpTopicWords.addAll(Arrays.asList(ChatUtils.getWords(searchText)));
        List<String> titleWordList = Arrays.asList(ChatUtils.getWords(title.toLowerCase()));
        for (String titleWord : titleWordList) {
            if (titleWord.length() < Help.MIN_WORD_LENGTH) continue;
            titleWords.add(titleWord);
        }
        helpTopicWords.addAll(titleWordList);
        List<String> keyWordList = Arrays.asList(ChatUtils.getWords(key));
        for (String keyWord : keyWordList) {
            if (keyWord.length() < Help.MIN_WORD_LENGTH) continue;
            titleWords.add(keyWord);
        }
        helpTopicWords.addAll(keyWordList);
        tags = tags + " " + expansion.getTags();
        List<String> tagWordList = Arrays.asList(ChatUtils.getWords(tags.toLowerCase()));
        helpTopicWords.addAll(tagWordList);
        for (String tagWord : tagWordList) {
            if (tagWord.length() < Help.MIN_WORD_LENGTH) continue;
            tagWords.add(tagWord);
        }
        List<String> topicTypeList = Arrays.asList(ChatUtils.getWords(topicType.toLowerCase()));
        for (String topicWord : topicTypeList) {
            if (topicWord.length() < Help.MIN_WORD_LENGTH) continue;
            tagWords.add(topicWord);
        }
        helpTopicWords.addAll(topicTypeList);
        for (String word : helpTopicWords) {
            word = word.trim();
            if (word.length() < Help.MIN_WORD_LENGTH) continue;
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
        return text + extraText;
    }

    @Nonnull
    public HelpTopicMatch match(Help help, Collection<String> keywords) {
        return HelpTopicMatch.match(help, this, keywords);
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

    public double getWeight() {
        return weight;
    }
}
