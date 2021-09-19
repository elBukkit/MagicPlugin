package com.elmakers.mine.bukkit.utility.help;

public class HelpTopicWord {
    private int count;
    private int topicCount;

    public int getCount() {
        return count;
    }

    public int getTopicCount() {
        return topicCount;
    }

    public void addTopic(int count) {
        this.topicCount++;
        this.count += count;
    }
}
