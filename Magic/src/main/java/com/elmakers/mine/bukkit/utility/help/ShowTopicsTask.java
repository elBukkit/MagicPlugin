package com.elmakers.mine.bukkit.utility.help;

import java.util.List;

import org.bukkit.ChatColor;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.utility.ChatUtils;
import com.elmakers.mine.bukkit.utility.StringUtils;

public class ShowTopicsTask implements Runnable {
    private static final int DEBUG_PADDING = 2;
    private static final int DEBUG_KEY_WIDTH = 8;
    private static final int DEBUG_NUMERIC_WIDTH = 6;
    private static final String DEBUG_NUMERIC_FORMAT = "%.2f";
    private final List<HelpTopicMatch> matches;
    private final Mage mage;
    private final Help help;
    private final List<String> keywords;
    private final int maxTopics;

    public ShowTopicsTask(Help help, Mage mage, List<String> keywords, List<HelpTopicMatch> matches, int maxTopics) {
        this.help = help;
        this.keywords = keywords;
        this.mage = mage;
        this.matches = matches;
        this.maxTopics = maxTopics;
    }

    @Override
    public void run() {
        Messages messages = mage.getController().getMessages();
        if (matches.isEmpty()) {
            String topic = StringUtils.join(keywords, " ");
            String unknownMessage = messages.get("commands.mhelp.unknown");
            mage.sendMessage(unknownMessage.replace("$topic", topic));
        } else {
            int size = matches.size();
            if (size == 1) {
                String foundMessage = messages.get("commands.mhelp.found_single");
                mage.sendMessage(foundMessage);
                HelpTopic topic = matches.get(0).getTopic();
                mage.sendMessage(topic.getText());
                return;
            }
            if (size > maxTopics) {
                String foundMessage = messages.get("commands.mhelp.found_limit");
                mage.sendMessage(foundMessage
                    .replace("$count", Integer.toString(size))
                    .replace("$limit", Integer.toString(maxTopics)));
            } else {
                String foundMessage = messages.get("commands.mhelp.found");
                mage.sendMessage(foundMessage.replace("$count", Integer.toString(size)));
            }

            int shown = 0;
            String template = messages.get("commands.mhelp.match");
            for (HelpTopicMatch topicMatch : matches) {
                String title = topicMatch.getTopic().getTitle();
                String summary = topicMatch.getSummary(!mage.isPlayer());
                String message = template
                        .replace("$title", title)
                        .replace("$topic", topicMatch.getTopic().getKey())
                        .replace("$summary", summary);
                mage.sendMessage(message);
                shown++;
                if (shown >= maxTopics) break;
            }

            if (mage.getDebugLevel() >= 1000) {
                String debugTooltip = messages.get("commands.mhelp.debug_tooltip");
                mage.sendMessage(messages.get("commands.mhelp.separator"));
                int matchCount = Math.min(maxTopics, matches.size());
                if (mage.isPlayer()) {
                    matchCount = Math.min(matchCount, 5);
                }
                String header = ChatColor.GRAY + ChatUtils.getFixedWidth("", DEBUG_KEY_WIDTH) + StringUtils.repeat(" ", DEBUG_PADDING);
                header += ChatUtils.getFixedWidth("*", DEBUG_NUMERIC_WIDTH) + StringUtils.repeat(" ", DEBUG_PADDING);

                for (int i = 0; i < matchCount; i++) {
                    HelpTopicMatch topicMatch = matches.get(i);
                    String topicHeader = debugTooltip.replace("$debug", topicMatch.getTopic().getKey()).replace("$text", Integer.toString(i + 1));
                    header += ChatUtils.getFixedWidth(topicHeader, DEBUG_NUMERIC_WIDTH);
                    header += StringUtils.repeat(" ", DEBUG_PADDING);
                }
                mage.sendMessage(header);
                int totalWidth = DEBUG_KEY_WIDTH + DEBUG_PADDING + (matchCount + 1) * (DEBUG_NUMERIC_WIDTH + DEBUG_PADDING);
                mage.sendMessage(StringUtils.repeat("_", totalWidth));

                String row = ChatColor.GRAY + ChatUtils.getFixedWidth("*", DEBUG_KEY_WIDTH) + StringUtils.repeat(" ", DEBUG_PADDING);
                row += ChatUtils.getFixedWidth("", DEBUG_NUMERIC_WIDTH) + StringUtils.repeat(" ", DEBUG_PADDING);

                for (int i = 0; i < matchCount; i++) {
                    HelpTopicMatch topicMatch = matches.get(i);
                    double relevance = topicMatch.getRelevance() * 100;
                    String value = relevance > 0 ? String.format(DEBUG_NUMERIC_FORMAT, relevance) : "";
                    value = debugTooltip.replace("$text", value);
                    value = value.replace("$debug", topicMatch.getDebugText());
                    row += ChatColor.DARK_AQUA + ChatUtils.getFixedWidth(value, DEBUG_NUMERIC_WIDTH);
                    row += StringUtils.repeat(" ", DEBUG_PADDING);
                }
                mage.sendMessage(row);

                for (String keyword : keywords) {
                    row = ChatColor.WHITE + ChatUtils.getFixedWidth(keyword, DEBUG_KEY_WIDTH) + StringUtils.repeat(" ", DEBUG_PADDING);
                    String overallWeight = String.format(DEBUG_NUMERIC_FORMAT, 100 * help.getWeight(keyword));
                    overallWeight = debugTooltip.replace("$text", overallWeight);
                    overallWeight = overallWeight.replace("$debug", help.getDebugText(keyword));
                    row += ChatColor.BLUE + ChatUtils.getFixedWidth(overallWeight, DEBUG_NUMERIC_WIDTH) + StringUtils.repeat(" ", DEBUG_PADDING);

                    for (int i = 0; i < matchCount; i++) {
                        HelpTopicMatch topicMatch = matches.get(i);
                        HelpTopicKeywordMatch keyWordMatch = topicMatch.getKeywordMatch(keyword);
                        double relevance = keyWordMatch == null ? 0 : keyWordMatch.getRelevance();
                        String value = relevance > 0 ? String.format(DEBUG_NUMERIC_FORMAT, 100 * relevance) : "";
                        if (keyWordMatch != null) {
                            value = debugTooltip.replace("$text", value);
                            value = value.replace("$debug", keyWordMatch.getDebugText(help));
                        }
                        row += ChatColor.AQUA + ChatUtils.getFixedWidth(value, DEBUG_NUMERIC_WIDTH);
                        row += StringUtils.repeat(" ", DEBUG_PADDING);
                    }
                    mage.sendMessage(row);
                }
            }
        }
        mage.sendMessage(messages.get("commands.mhelp.separator"));
    }
}
