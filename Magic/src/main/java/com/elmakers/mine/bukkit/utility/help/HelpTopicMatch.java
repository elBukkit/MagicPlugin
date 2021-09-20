package com.elmakers.mine.bukkit.utility.help;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import org.geysermc.connector.common.ChatColor;

import com.elmakers.mine.bukkit.ChatUtils;

public class HelpTopicMatch implements Comparable<HelpTopicMatch> {
    public static final double COUNT_FACTOR = 0.5;
    public static final double SIMILARITY_FACTOR = 2;
    public static final double CONTENT_WEIGHT = 1;
    public static final double TAG_WEIGHT = 2;
    public static final double TITLE_WEIGHT = 4;
    public static final double TOTAL_WEIGHT = CONTENT_WEIGHT + TAG_WEIGHT + TITLE_WEIGHT;
    private static final int MAX_WIDTH = 50;
    private final double relevance;
    private final HelpTopic topic;
    private final Map<String, HelpTopicKeywordMatch> wordMatches = new HashMap<>();

    public HelpTopicMatch(Help help, HelpTopic topic, Collection<String> keywords) {
        this.topic = topic;

        double relevance = 0;
        for (String keyword : keywords) {
            relevance += computeRelevance(help, keyword);
        }
        relevance = relevance / keywords.size();
        this.relevance = relevance;
    }

    private double computeRelevance(Help help, String keyword) {
        double wordsRelevance = computeWordsRelevance(help, keyword);
        double titleRelevance = computeSetRelevance(help, topic.titleWords, keyword);
        double tagRelevance = computeSetRelevance(help, topic.tagWords, keyword);
        return (wordsRelevance * CONTENT_WEIGHT + titleRelevance * TITLE_WEIGHT + tagRelevance * TAG_WEIGHT) / TOTAL_WEIGHT;
    }

    private double computeSetRelevance(Help help, Set<String> words, String keyword) {
        double relevance = 0;
        if (!topic.isValidWord(keyword)) {
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
                maxSimilarity = similarity;
            }
        }
        if (bestMatch != null) {
            relevance = help.getWeight(bestMatch);
            double similarityWeight = Math.pow(maxSimilarity, SIMILARITY_FACTOR);
            relevance *= similarityWeight;
        }

        return relevance;
    }

    private double computeWordsRelevance(Help help, String keyword) {
        double relevance = 0;
        if (!topic.isValidWord(keyword)) {
            return relevance;
        }
        keyword = keyword.trim();
        Integer count = topic.words.get(keyword);
        if (count != null) {
            double countWeight = (double)count / topic.maxCount;
            return Math.pow(countWeight, COUNT_FACTOR) * help.getWeight(keyword);
        }
        double maxSimilarity = 0;
        String bestMatch = null;
        for (Map.Entry<String, Integer> entry : topic.words.entrySet()) {
            String word = entry.getKey();
            double similarity = ChatUtils.getSimilarity(keyword, word);
            if (similarity > maxSimilarity) {
                count = entry.getValue();
                bestMatch = word;
                maxSimilarity = similarity;
            }
        }
        if (bestMatch != null) {
            double countWeight = (double)count / topic.maxCount;
            relevance = Math.pow(countWeight, COUNT_FACTOR) * help.getWeight(bestMatch);
            double similarityWeight = Math.pow(maxSimilarity, SIMILARITY_FACTOR);
            relevance *= similarityWeight;
            HelpTopicKeywordMatch match = new HelpTopicKeywordMatch(keyword, bestMatch, relevance);
            wordMatches.put(bestMatch, match);
        }

        return relevance;
    }

    @Override
    public int compareTo(HelpTopicMatch o) {
        return o.relevance > relevance ? 1 : (o.relevance < relevance ? -1 : 0);
    }

    @Nonnull
    public HelpTopic getTopic() {
        return topic;
    }

    public String getSummary() {
        return getSummary(MAX_WIDTH, ChatColor.AQUA, ChatColor.RESET);
    }

    public String getSummary(int maxWidth, String matchPrefix, String matchSuffix) {
        String title = getTopic().getTitle();
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
        double mostRelevant = 0;
        String summary = null;
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            // Case-insensitive searching
            String matchLine = line.toLowerCase();
            // Match against each search keyword, keeping a range of text between them
            int firstMatchIndex = -1;
            int lastMatchEnd = -1;
            double relevance = 0;
            int matchCount = 0;
            for (HelpTopicKeywordMatch match : wordMatches.values()) {
                String keyword = match.getWord();
                int startIndex = matchLine.indexOf(keyword);
                if (startIndex >= 0) {
                    // Track match count and relevance
                    double matchRelevance = match.getRelevance();
                    if (matchRelevance > 0) {
                        relevance += matchRelevance;
                        matchCount++;
                    }
                    // Track range of all keywords
                    int endIndex = startIndex + keyword.length();
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
            if (relevance > 0 && relevance > mostRelevant) {
                boolean fitAllMatches = true;
                mostRelevant = relevance;
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
                if (fitAllMatches && matchCount == wordMatches.size()) break;
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
        // Highlight matches
        for (HelpTopicKeywordMatch match : wordMatches.values()) {
            String keyword = match.getWord();
            summary = summary.replaceAll("((?i)" + Pattern.quote(keyword) + ")", matchPrefix + "$1" + matchSuffix);
        }
        return summary;
    }

    public double getKeywordRelevance(String keyword) {
        HelpTopicKeywordMatch match = wordMatches.get(keyword);
        return match == null ? 0 : match.getRelevance();
    }

    public double getRelevance() {
        return relevance;
    }

    public boolean isRelevant() {
        return relevance > 0;
    }
}
