package com.elmakers.mine.bukkit.utility.help;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.elmakers.mine.bukkit.ChatUtils;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;

public class ShowTopicsTask implements Runnable {
    private static final int MAX_RESULTS = 10;
    private static final int DEBUG_PADDING = 1;
    private static final int DEBUG_KEY_WIDTH = 8;
    private static final int DEBUG_NUMERIC_WIDTH = 8;
    private static final String DEBUG_NUMERIC_FORMAT = "%5.2e";
    private final List<HelpTopicMatch> matches;
    private final Mage mage;
    private final Help help;
    private final List<String> keywords;

    public ShowTopicsTask(Help help, Mage mage, List<String> keywords, List<HelpTopicMatch> matches) {
        this.help = help;
        this.keywords = keywords;
        this.mage = mage;
        this.matches = matches;
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
            if (size > MAX_RESULTS) {
                String foundMessage = messages.get("commands.mhelp.found_limit");
                mage.sendMessage(foundMessage
                    .replace("$count", Integer.toString(size))
                    .replace("$limit", Integer.toString(MAX_RESULTS)));
            } else {
                String foundMessage = messages.get("commands.mhelp.found");
                mage.sendMessage(foundMessage.replace("$count", Integer.toString(size)));
            }

            int shown = 0;
            String template = messages.get("commands.mhelp.match");
            for (HelpTopicMatch topicMatch : matches) {
                String title = topicMatch.getTopic().getTitle();
                String summary = topicMatch.getSummary(help, keywords, title);
                String message = template
                        .replace("$title", title)
                        .replace("$topic", topicMatch.getTopic().getKey())
                        .replace("$summary", summary);
                mage.sendMessage(message);
                shown++;
                if (shown >= MAX_RESULTS) break;
            }

            if (mage.getDebugLevel() >= 1000) {
                mage.sendMessage(messages.get("commands.mhelp.separator"));
                int matchCount = Math.min(MAX_RESULTS, matches.size());
                String header = ChatUtils.getFixedWidth("", DEBUG_KEY_WIDTH) + StringUtils.repeat(" ", DEBUG_PADDING);
                header += ChatUtils.getFixedWidth("*", DEBUG_NUMERIC_WIDTH);
                header += StringUtils.repeat(" ", DEBUG_PADDING);
                for (int i = 0; i < matchCount; i++) {
                    header += ChatUtils.getFixedWidth(Integer.toString(i + 1), DEBUG_NUMERIC_WIDTH);
                    header += StringUtils.repeat(" ", DEBUG_PADDING);
                }
                mage.sendMessage(header);
                mage.sendMessage(StringUtils.repeat("_", DEBUG_KEY_WIDTH + DEBUG_PADDING + matchCount * (DEBUG_NUMERIC_WIDTH + DEBUG_PADDING)));

                String row = ChatUtils.getFixedWidth("*", DEBUG_KEY_WIDTH) + StringUtils.repeat(" ", DEBUG_PADDING);
                row += ChatUtils.getFixedWidth("", DEBUG_NUMERIC_WIDTH) + StringUtils.repeat(" ", DEBUG_PADDING);
                for (int i = 0; i < matchCount; i++) {
                    HelpTopicMatch topicMatch = matches.get(i);
                    double relevance = topicMatch.getRelevance();
                    String value = relevance > 0 ? String.format(DEBUG_NUMERIC_FORMAT, relevance) : "";
                    row += ChatUtils.getFixedWidth(value, DEBUG_NUMERIC_WIDTH);
                    row += StringUtils.repeat(" ", DEBUG_PADDING);
                }
                mage.sendMessage(row);

                for (String keyword : keywords) {
                    row = ChatUtils.getFixedWidth(keyword, DEBUG_KEY_WIDTH) + StringUtils.repeat(" ", DEBUG_PADDING);
                    row += ChatUtils.getFixedWidth(String.format(DEBUG_NUMERIC_FORMAT, help.getWeight(keyword)), DEBUG_NUMERIC_WIDTH) + StringUtils.repeat(" ", DEBUG_PADDING);
                    for (int i = 0; i < matchCount; i++) {
                        HelpTopicMatch topicMatch = matches.get(i);
                        double relevance = topicMatch.getTopic().getRelevance(help, keyword);
                        String value = relevance > 0 ? String.format(DEBUG_NUMERIC_FORMAT, relevance) : "";
                        row += ChatUtils.getFixedWidth(value, DEBUG_NUMERIC_WIDTH);
                        row += StringUtils.repeat(" ", DEBUG_PADDING);
                    }
                    mage.sendMessage(row);
                }
            }
        }
        mage.sendMessage(messages.get("commands.mhelp.separator"));
    }
}
