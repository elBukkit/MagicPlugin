package com.elmakers.mine.bukkit.utility.help;

import com.elmakers.mine.bukkit.api.magic.Mage;

public class SearchHelpTask implements Runnable {
    private final Help help;
    private final Mage mage;
    private final String[] args;

    public SearchHelpTask(Help help, Mage mage, String[] args) {
        this.help = help;
        this.mage = mage;
        this.args = args;
    }

    @Override
    public void run() {
        help.search(mage, args);
    }
}
