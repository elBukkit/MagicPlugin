package com.elmakers.mine.bukkit.tasks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;

import com.elmakers.mine.bukkit.kit.MagicKit;
import com.elmakers.mine.bukkit.magic.Mage;

public class ProcessKitsTask implements Runnable {
    private final Mage mage;
    private final World fromWorld;
    private final World toWorld;
    private final List<MagicKit> kits = new ArrayList<>();

    public ProcessKitsTask(Mage mage, World fromWorld, World toWorld) {
        this.mage = mage;
        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
    }

    public void addKit(MagicKit kit) {
        kits.add(kit);
    }

    public boolean isEmpty() {
        return kits.isEmpty();
    }

    @Override
    public void run() {
        for (MagicKit joinKit : kits) {
            if (joinKit.isStarter()) {
                joinKit.checkGive(mage);
            }
            if (joinKit.isRemove()) {
                joinKit.checkRemoveFrom(mage);
            }
            if (joinKit.isKeep()) {
                joinKit.giveMissing(mage);
            }
        }
    }
}
