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
    private final String[] lines;

    public HelpTopic(Messages messages, String key, String text) {
        this.key = key;
        MacroExpansion expansion = messages.expandMacros(text);
        text = expansion.getText();
        text = CompatibilityLib.getCompatibilityUtils().translateColors(StringEscapeUtils.unescapeHtml(text));
        this.text = text;
        String simpleText = ChatColor.stripColor(ChatUtils.getSimpleMessage(text, true));
        this.searchText = simpleText.toLowerCase();
        this.title = expansion.getTitle();

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
        for (String arg : keywords) {
            arg = arg.trim();
            if (arg.length() < 2) continue;
            if (arg.length() < 4) arg = " " + arg + " ";
            if (title.toLowerCase().contains(arg)) {
                relevance += arg.length() * help.getWeight(arg);
            }
            if (key.contains(arg)) {
                relevance += arg.length() * help.getWeight(arg);
            }
            if (searchText.contains(arg)) {
                relevance += arg.length() * help.getWeight(arg);
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
}
