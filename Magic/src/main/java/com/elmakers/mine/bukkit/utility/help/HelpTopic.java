package com.elmakers.mine.bukkit.utility.help;

import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import com.elmakers.mine.bukkit.ChatUtils;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.MacroExpansion;
import com.elmakers.mine.bukkit.utility.Messages;

public class HelpTopic {
    private final String key;
    private final String title;
    private final String text;
    private final String searchText;
    private final String tags;
    private final String topicType;
    private final String[] lines;

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

    public double match(Help help, Collection<String> keywords) {
        double relevance = 0;
        for (String keyword : keywords) {
            keyword = keyword.trim();
            if (keyword.length() < 2) continue;
            String search = keyword;
            if (search.length() < 4 && !help.isWord(keyword)) continue;
            if (title.toLowerCase().contains(search)) {
                relevance += help.getWeight(keyword);
            }
            if (key.contains(search)) {
                relevance += help.getWeight(keyword);
            }
            if (searchText.contains(search)) {
                relevance += help.getWeight(keyword);
            }
            if (tags.contains(search)) {
                relevance += help.getWeight(keyword);
            }
            if (topicType.contains(search)) {
                relevance += help.getWeight(keyword);
            }
        }
        return relevance;
    }

    public String[] getWords() {
        return ChatUtils.getWords(searchText);
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
