package com.elmakers.mine.bukkit.utility.help;

import java.util.List;
import javax.annotation.Nonnull;

public class HelpTopicMatch implements Comparable<HelpTopicMatch> {
    private final int matchCount;
    private final HelpTopic topic;

    public HelpTopicMatch(HelpTopic topic, int matchCount) {
        this.topic = topic;
        this.matchCount = matchCount;
    }

    @Override
    public int compareTo(HelpTopicMatch o) {
        return o.matchCount > matchCount ? 1 : (o.matchCount < matchCount ? -1 : 0);
    }

    @Nonnull
    public HelpTopic getTopic() {
        return topic;
    }

    public String getSummary(List<String> keywords) {
        String[] lines = topic.getLines();
        if (lines.length == 0) {
            return "";
        }
        String summary = null;
        for (String line : lines) {
            for (String arg : keywords) {
                if (line.contains(arg)) {
                    summary = line;
                    break;
                }
            }
            if (summary != null) break;
        }
        if (summary == null) {
            summary = lines[0];
        }
        return summary;
    }
}
