package com.elmakers.mine.bukkit.utility.help;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;

public class ShowTopicsTask implements Runnable {
    private static final int MAX_RESULTS = 10;
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
        }
        mage.sendMessage(messages.get("commands.mhelp.separator"));
    }
}
