package com.elmakers.mine.bukkit.utility.help;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.utility.Messages;

public class Help {
    private final Messages messages;
    private final Map<String, HelpTopic> topics = new HashMap<>();
    private final Set<String> words = new HashSet<>();

    public Help(Messages messages) {
        this.messages = messages;
    }

    public void load(ConfigurationSection helpSection) {
        Collection<String> keys = helpSection.getKeys(true);
        for (String key : keys) {
            if (helpSection.isConfigurationSection(key)) continue;
            String value = helpSection.getString(key);
            HelpTopic helpTopic = new HelpTopic(messages, key, value);
            topics.put(key, helpTopic);
            for (String word : helpTopic.getWords()) {
                if (word.length() > 1) {
                    words.add(word);
                }
            }
        }
    }

    public Set<String> getWords() {
        return words;
    }

    public Set<String> getTopicKeys() {
        return topics.keySet();
    }

    public boolean showTopic(Mage mage, String key) {
        HelpTopic topic = topics.get(key);
        if (topic == null) {
            return false;
        }
        mage.sendMessage(topic.getText());
        return true;
    }

    @Nonnull
    public List<HelpTopicMatch> findMatches(List<String> keywords) {
        List<HelpTopicMatch> matches = new ArrayList<>();
        for (HelpTopic topic : topics.values()) {
            int matchCount = topic.match(keywords);
            if (matchCount > 0) {
                matches.add(new HelpTopicMatch(topic, matchCount));
            }
        }

        return matches;
    }

    public void search(Mage mage, String[] args) {
        List<String> keywords = new ArrayList<>();
        for (String arg : args) {
            keywords.add(arg.toLowerCase());
        }
        List<HelpTopicMatch> matches = findMatches(keywords);
        Collections.sort(matches);

        // This is called async, move back to the main thread to do messaging
        ShowTopicsTask showTask = new ShowTopicsTask(mage, keywords, matches);
        Plugin plugin = mage.getController().getPlugin();
        plugin.getServer().getScheduler().runTask(plugin, showTask);
    }
}
