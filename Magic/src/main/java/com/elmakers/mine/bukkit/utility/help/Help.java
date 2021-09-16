package com.elmakers.mine.bukkit.utility.help;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.Messages;

public class Help {
    private final Messages messages;
    private final Map<String, HelpTopic> topics = new HashMap<>();
    private final Map<String, Integer> words = new HashMap<>();
    private final Map<String, String> metaTemplates = new HashMap<>();
    private int maxCount = 0;

    public Help(Messages messages) {
        this.messages = messages;
    }

    public void load(ConfigurationSection helpSection, ConfigurationSection examplesSection, ConfigurationSection metaSection) {
        loadMetaTemplates(metaSection);
        ConfigurationSection helpExamples = helpSection.getConfigurationSection("examples");
        if (helpExamples != null) {
            ConfigurationUtils.addConfigurations(helpExamples, examplesSection);
        } else {
            helpSection.set("examples", examplesSection);
        }
        load(helpSection);
    }

    public void load(ConfigurationSection helpSection) {
        Collection<String> keys = helpSection.getKeys(true);
        for (String key : keys) {
            if (helpSection.isConfigurationSection(key)) continue;
            String value = helpSection.getString(key);
            loadTopic(key, value);
        }
    }

    public void loadTopic(String key, String contents) {
        loadTopic(key, contents, "");
    }

    public void loadTopic(String key, String contents, String tags) {
        HelpTopic helpTopic = new HelpTopic(messages, key, contents, tags);
        topics.put(key, helpTopic);
        for (String word : helpTopic.getWords()) {
            if (word.length() > 1) {
                Integer count = words.get(word);
                if (count == null) count = 1;
                else count++;
                words.put(word, count);
                maxCount = Math.max(maxCount, count);
            }
        }
    }

    private void loadMetaTemplates(ConfigurationSection metaSection) {
        for (String key : metaSection.getKeys(true)) {
            metaTemplates.put(key, metaSection.getString(key));
        }
    }

    public void loadMetaActions(Map<String, Map<String, Object>> actions) {
        loadMetaClassed(actions, "action", "action spell reference");
    }

    public void loadMetaEffects(Map<String, Map<String, Object>> effects) {
        loadMetaClassed(effects, "effect", "effectlib spell reference");
    }

    @SuppressWarnings("unchecked")
    private void loadMetaClassed(Map<String, Map<String, Object>> meta, String metaType, String tags) {
        String descriptionTemplate = metaTemplates.get(metaType + "_template");
        String defaultDescription = metaTemplates.get("default_description");
        for (Map.Entry<String, Map<String, Object>> entry : meta.entrySet()) {
            Map<String, Object> action = entry.getValue();
            String key = entry.getKey();
            String shortClass = (String)action.get("short_class");
            if (shortClass == null) continue;
            List<String> descriptionList = (List<String>)action.get("description");
            String description = StringUtils.join(descriptionList, "\n");
            if (description.isEmpty()) {
                description = defaultDescription;
            }
            description = descriptionTemplate.replace("$class", shortClass)
                .replace("$description", description)
                .replace("$key", key);
            String metaCategory = (String)action.get("category");
            if (metaCategory != null && !metaCategory.isEmpty()) {
                tags += " " + metaCategory;
            }
            // hacky plural here, be warned
            loadTopic("reference." + metaType + "s." + key, description, tags);
        }
    }

    public Set<String> getWords() {
        return words.keySet();
    }

    public double getWeight(String word) {
        if (maxCount == 0) return 1;
        Integer count = words.get(word);
        if (count == null) return 0;
        return 1.0 - ((double)count / (maxCount + 1));
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

    public HelpTopic getTopic(String key) {
        return topics.get(key);
    }

    @Nonnull
    public List<HelpTopicMatch> findMatches(List<String> keywords) {
        List<HelpTopicMatch> matches = new ArrayList<>();
        for (HelpTopic topic : topics.values()) {
            double relevance = topic.match(this, keywords);
            if (relevance > 0) {
                matches.add(new HelpTopicMatch(topic, relevance));
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
        ShowTopicsTask showTask = new ShowTopicsTask(this, mage, keywords, matches);
        Plugin plugin = mage.getController().getPlugin();
        plugin.getServer().getScheduler().runTask(plugin, showTask);
    }
}
