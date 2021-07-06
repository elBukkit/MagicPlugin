package com.elmakers.mine.bukkit.tasks;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.wand.Wand;

public class ActivateIconTask extends CloseInventoryTask {
    private final Mage mage;
    private final Wand activeWand;
    private final ItemStack clickedItem;

    public ActivateIconTask(Mage mage, Wand activeWand, ItemStack clickedItem) {
        super(mage.getPlayer());
        this.mage = mage;
        this.activeWand = activeWand;
        this.clickedItem = clickedItem;
    }

    @Override
    public void run() {
        super.run();
        mage.activateIcon(activeWand, clickedItem);
    }
}
