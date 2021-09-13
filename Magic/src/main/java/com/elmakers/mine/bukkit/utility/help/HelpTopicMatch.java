package com.elmakers.mine.bukkit.utility.help;

import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import org.geysermc.connector.common.ChatColor;

public class HelpTopicMatch implements Comparable<HelpTopicMatch> {
    private static final int MAX_WIDTH = 50;
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

    public String getSummary(List<String> keywords, String title) {
        return getSummary(keywords, title, MAX_WIDTH);
    }

    public String getSummary(List<String> keywords, String title, int maxWidth) {
        int titleLength = title.length();
        if (titleLength > maxWidth - 4) {
            return "";
        }
        int remainingLength = maxWidth - titleLength;
        String[] lines = topic.getLines();
        if (lines.length == 0) {
            return "";
        }

        // Look for matches on each line separately, tracking the number of matches
        int summaryMatchCount = 0;
        String summary = null;
        for (String line : lines) {
            // Case-insensitive searching
            String matchLine = line.toLowerCase();
            // Match against each search keyword, keeping a range of text between them
            int firstMatchIndex = -1;
            int lastMatchEnd = -1;
            int matchCount = 0;
            for (String arg : keywords) {
                int startIndex = matchLine.indexOf(arg);
                if (startIndex >= 0) {
                    // Track match count
                    matchCount++;
                    // Track range of all keywords
                    int endIndex = startIndex + arg.length();
                    if (firstMatchIndex == -1) {
                        firstMatchIndex = startIndex;
                        lastMatchEnd = endIndex;
                    } else {
                        firstMatchIndex = Math.min(firstMatchIndex, startIndex);
                        lastMatchEnd = Math.max(lastMatchEnd, endIndex);
                    }
                }
            }

            // If there are more matches than we currently have, use this line
            if (matchCount > 0 && matchCount > summaryMatchCount) {
                boolean fitAllMatches = true;
                summaryMatchCount = matchCount;
                // Trim this line if it is too long
                if (line.length() > remainingLength) {
                    // Trim from the start if we can fit everything
                    if (lastMatchEnd < remainingLength) {
                        line = line.substring(0, remainingLength) + ChatColor.GRAY + "...";
                    } else if (line.length() - firstMatchIndex < remainingLength) {
                        // Get everything from the end
                        line =  ChatColor.GRAY + "..."  + ChatColor.RESET + line.substring(line.length() - remainingLength);
                    } else if (lastMatchEnd - firstMatchIndex < remainingLength) {
                        // If the whole segment can fit, center it
                        int padding = remainingLength - (lastMatchEnd - firstMatchIndex);
                        int startIndex = firstMatchIndex - padding / 2;
                        line =  ChatColor.GRAY + "..."  + ChatColor.RESET + line.substring(startIndex, startIndex + remainingLength) + ChatColor.GRAY + "...";
                    } else {
                        // Just start at the first match
                        line = ChatColor.GRAY + "..." + line.substring(firstMatchIndex, firstMatchIndex + remainingLength) + ChatColor.GRAY + "...";
                        fitAllMatches = false;
                    }
                }
                summary = line;
                if (fitAllMatches && summaryMatchCount == keywords.size()) break;
            }
        }
        // Fall back to first line
        if (summary == null) {
            summary = lines[0];

            // Trim if needed
            if (summary.length() > remainingLength) {
                summary = summary.substring(0, remainingLength) + ChatColor.GRAY + "...";
            }
        }
        // Color matches
        for (String keyword : keywords) {
            summary = summary.replaceAll("((?i)" + Pattern.quote(keyword) + ")", ChatColor.AQUA + "$1" + ChatColor.RESET);
        }
        return summary;
    }
}
