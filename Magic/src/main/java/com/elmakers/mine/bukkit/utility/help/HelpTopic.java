package com.elmakers.mine.bukkit.utility.help;

import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.elmakers.mine.bukkit.ChatUtils;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.MacroExpansion;
import com.elmakers.mine.bukkit.utility.Messages;

public class HelpTopic {
    private final String key;
    private final String title;
    private final String text;
    private final String simpleText;
    private final String searchText;

    public HelpTopic(Messages messages, String key, String text) {
        this.key = key;
        MacroExpansion expansion = messages.expandMacros(text);
        text = expansion.getText();
        text = CompatibilityLib.getCompatibilityUtils().translateColors(StringEscapeUtils.unescapeHtml(text));
        this.text = text;
        this.simpleText = ChatUtils.getSimpleMessage(text);
        this.searchText = simpleText.toLowerCase();
        this.title = expansion.getTitle();
    }

    @Nonnull
    public String getTitle() {
        if (title.isEmpty()) {
            return key;
        }
        return title;
    }

    @Nonnull
    public String getText() {
        return text;
    }

    public int match(Collection<String> keywords) {
        int matchCount = 0;
        for (String arg : keywords) {
            if (key.contains(arg) || searchText.contains(arg) || title.contains(arg)) {
                matchCount++;
            }
        }
        return matchCount;
    }

    public String[] getLines() {
        String[] lines = StringUtils.split(simpleText, "\n");
        if (!title.isEmpty() && lines.length > 1) {
            lines = Arrays.copyOfRange(lines, 1, lines.length);
        }
        return lines;
    }

    public String getKey() {
        return key;
    }
}
