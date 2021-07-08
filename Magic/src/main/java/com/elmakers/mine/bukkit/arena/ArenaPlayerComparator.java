package com.elmakers.mine.bukkit.arena;

import java.util.Comparator;

/**
 * Sort players based on the left confidence interval of their true win ratio.
 *
 * @see ArenaPlayer#getWinConfidence()
 */
public class ArenaPlayerComparator implements Comparator<ArenaPlayer> {
    @Override
    public int compare(ArenaPlayer a, ArenaPlayer b) {
        return -Double.compare(a.getWinConfidence(), b.getWinConfidence());
    }
}
