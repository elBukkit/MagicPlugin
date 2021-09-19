package com.elmakers.mine.bukkit.utility.help;

import com.elmakers.mine.bukkit.api.magic.Mage;

public class SearchHelpTask implements Runnable {
    private final Help help;
    private final Mage mage;
    private final String[] args;
    private final int maxResults;

    public SearchHelpTask(Help help, Mage mage, String[] args, int maxResults) {
        this.help = help;
        this.mage = mage;
        this.args = args;
        this.maxResults = maxResults;
    }

    @Override
    public void run() {
        help.search(mage, args, maxResults);
    }
}
