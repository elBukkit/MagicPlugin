package com.elmakers.mine.bukkit.utility.help;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;

public class ShowTopicsTask implements Runnable {
    private final List<HelpTopicMatch> matches;
    private final Mage mage;
    private final List<String> keywords;

    public ShowTopicsTask(Mage mage, List<String> keywords, List<HelpTopicMatch> matches) {
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
            String foundMessage = messages.get("commands.mhelp.found");
            mage.sendMessage(foundMessage.replace("$count", Integer.toString(matches.size())));
            String template = messages.get("commands.mhelp.match");
            for (HelpTopicMatch topicMatch : matches) {
                String summary = topicMatch.getSummary(keywords);
                String message = template
                        .replace("$title", topicMatch.getTopic().getTitle())
                        .replace("$topic", topicMatch.getTopic().getKey())
                        .replace("$summary", summary);
                mage.sendMessage(message);
            }
        }
        mage.sendMessage(messages.get("commands.mhelp.separator"));
    }
}
