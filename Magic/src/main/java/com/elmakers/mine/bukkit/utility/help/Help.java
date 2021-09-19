package com.elmakers.mine.bukkit.utility.help;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.Messages;

public class Help {
    private static double DEFAULT_WEIGHT = 0.00001;
    private static final int RARITY_FACTOR = 3;
    private static final int TOPIC_RARITY_FACTOR = 5;
    private static final int LENGTH_FACTOR = 2;
    private final Messages messages;
    private final Map<String, HelpTopic> topics = new HashMap<>();
    private final Map<String, HelpTopicWord> words = new HashMap<>();
    private final Map<String, String> metaTemplates = new HashMap<>();
    // We cheat and use one regex for both <li> and <link ...>
    private static final Pattern linkPattern = Pattern.compile("([^`])(<li[^>]*>)([^`])");
    private int maxCount = 0;
    private int maxTopicCount = 0;
    private int maxLength = 0;

    public Help(Messages messages) {
        this.messages = messages;
    }

    public void load(ConfigurationSection helpSection, ConfigurationSection examplesSection, ConfigurationSection metaSection) {
        loadMetaTemplates(metaSection);
        if (helpSection != null) {
            ConfigurationSection helpExamples = helpSection.getConfigurationSection("examples");
            if (helpExamples != null) {
                ConfigurationUtils.addConfigurations(helpExamples, examplesSection);
            } else {
                helpSection.set("examples", examplesSection);
            }
            load(helpSection);
        }
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
        loadTopic(key, contents, "", "");
    }

    public void loadTopic(String key, String contents, String tags, String topicType) {
        HelpTopic helpTopic = new HelpTopic(messages, key, contents, tags, topicType);
        topics.put(key, helpTopic);
        // Index all words
        Map<String, Integer> wordCounts = helpTopic.getWordCounts();
        for (Map.Entry<String, Integer> wordEntry : wordCounts.entrySet()) {
            String word = wordEntry.getKey();
            HelpTopicWord wordCount = words.get(word);
            if (wordCount == null) {
                wordCount = new HelpTopicWord();
                words.put(word, wordCount);
            }
            wordCount.addTopic(wordEntry.getValue());
            maxCount = Math.max(maxCount, wordCount.getCount());
            maxTopicCount = Math.max(maxTopicCount, wordCount.getTopicCount());
            maxLength = Math.max(maxLength, word.length());
        }
    }

    private void loadMetaTemplates(ConfigurationSection metaSection) {
        if (metaSection == null) return;
        for (String key : metaSection.getKeys(true)) {
            metaTemplates.put(key, metaSection.getString(key));
        }
    }

    public void loadMetaActions(Map<String, Map<String, Object>> actions) {
        loadMetaClassed(actions, "action", "action spell", "reference");
    }

    public void loadMetaEffects(Map<String, Map<String, Object>> effects) {
        loadMetaClassed(effects, "effect", "effectlib spell", "reference");
    }

    private String convertMetaDescription(String description) {
        return linkPattern.matcher(description).replaceAll("$1`$2`$3");
    }

    @SuppressWarnings("unchecked")
    private void loadMetaClassed(Map<String, Map<String, Object>> meta, String metaType, String tags, String topicType) {
        String descriptionTemplate = metaTemplates.get(metaType + "_template");
        String defaultDescription = metaTemplates.get("default_description");
        String defaultParameterDescription = metaTemplates.get("default_parameter_description");
        String parameterTemplate = metaTemplates.get("parameter_template");
        String parameterExtraLineTemplate = metaTemplates.get("parameter_extra_line");
        String parametersTemplate = metaTemplates.get("parameters_template");
        String examplesTemplate = metaTemplates.get("examples_template");
        String exampleTemplate = metaTemplates.get("example_template");
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
            Object rawExamples = action.get("examples");
            if (rawExamples != null && rawExamples instanceof List) {
                List<String> exampleList = (List<String>)rawExamples;
                for (int i = 0; i < exampleList.size(); i++) {
                    exampleList.set(i, exampleTemplate.replace("$example", exampleList.get(i)));
                }
                description += "\n" + examplesTemplate.replace("$examples", StringUtils.join(exampleList, " "));
            }
            Object rawParameters = action.get("parameters");
            // The conversion process turns empty maps into empty lists
            if (rawParameters != null && rawParameters instanceof Map) {
                Map<String, Object> parameters = (Map<String, Object>)rawParameters;
                if (parameters != null && !parameters.isEmpty()) {
                    List<String> parameterLines = new ArrayList<>();
                    for (Map.Entry<String, Object> parameterEntry : parameters.entrySet()) {
                        String parameterDescription = parameterEntry.getValue().toString();
                        if (parameterDescription.isEmpty()) {
                            parameterDescription = defaultParameterDescription;
                        }
                        // This is delimited by an escaped \n
                        String[] descriptionLines = parameterDescription.split("\\\\n");
                        parameterLines.add(parameterTemplate
                                .replace("$parameter", parameterEntry.getKey())
                                .replace("$description", descriptionLines[0])
                        );
                        for (int i = 1; i < descriptionLines.length; i++) {
                            parameterLines.add(parameterExtraLineTemplate
                                    .replace("$parameter", parameterEntry.getKey())
                                    .replace("$description", descriptionLines[i])
                            );
                        }
                    }
                    description += "\n" + parametersTemplate.replace("$parameters", StringUtils.join(parameterLines, "\n"));
                }
            }

            description = convertMetaDescription(description);
            // hacky plural here, be warned
            loadTopic("reference." + metaType + "s." + key, description, tags, topicType);
        }
    }

    public Set<String> getWords() {
        return words.keySet();
    }

    public boolean isWord(String word) {
        return words.containsKey(word);
    }

    public double getWeight(String word) {
        if (maxCount == 0 || maxLength == 0 || maxTopicCount == 0) return DEFAULT_WEIGHT;
        HelpTopicWord wordCount = words.get(word);
        // TODO: find partial match?
        if (wordCount == null) return DEFAULT_WEIGHT;
        double rarityWeight = 1.0 - ((double)wordCount.getCount() / (maxCount + 1));
        double topicRarityWeight = 1.0 - ((double)wordCount.getTopicCount() / (maxTopicCount + 1));
        return Math.pow(rarityWeight, RARITY_FACTOR) * Math.pow(topicRarityWeight, TOPIC_RARITY_FACTOR) * Math.pow((double)word.length() / maxLength, LENGTH_FACTOR);
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

        // Group by topic type, make sure to show at least one topic of each type
        Map<String, Queue<HelpTopicMatch>> grouped = new HashMap<>();
        for (HelpTopicMatch match : matches) {
            String topicType = match.getTopic().getTopicType();
            Queue<HelpTopicMatch> groupedList = grouped.get(topicType);
            if (groupedList == null) {
                groupedList = new PriorityQueue<>();
                grouped.put(topicType, groupedList);
            }
            groupedList.add(match);
        }

        // Merge each list in
        matches.clear();
        List<HelpTopicMatch> batch = new ArrayList<>();
        while (!grouped.isEmpty()) {
            Iterator<Queue<HelpTopicMatch>> it = grouped.values().iterator();
            if (grouped.size() == 1) {
                matches.addAll(it.next());
                break;
            }

            while (it.hasNext()) {
                Queue<HelpTopicMatch> typeMatches = it.next();
                batch.add(typeMatches.remove());
                if (typeMatches.isEmpty()) {
                    it.remove();
                }
            }
            Collections.sort(batch);
            matches.addAll(batch);
            batch.clear();
        }

        return matches;
    }

    public void search(Mage mage, String[] args) {
        List<String> keywords = new ArrayList<>();
        for (String arg : args) {
            keywords.add(arg.toLowerCase());
        }
        List<HelpTopicMatch> matches = findMatches(keywords);

        // This is called async, move back to the main thread to do messaging
        ShowTopicsTask showTask = new ShowTopicsTask(this, mage, keywords, matches);
        Plugin plugin = mage.getController().getPlugin();
        plugin.getServer().getScheduler().runTask(plugin, showTask);
    }
}
