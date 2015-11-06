package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpellProgressAction extends BaseSpellAction implements GUIAction
{
    private CastContext context;

    @Override
    public void deactivated() {

    }

    @Override
    public void dragged(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void clicked(InventoryClickEvent event)
    {
        event.setCancelled(true);
        if (context != null)
        {
            Mage mage = context.getMage();
            Wand wand = mage.getActiveWand();
            ItemStack item = event.getCurrentItem();
            if (wand != null && com.elmakers.mine.bukkit.wand.Wand.isSpell(item))
            {
                String spellKey = com.elmakers.mine.bukkit.wand.Wand.getSpell(item);
                SpellKey upgradeKey = new SpellKey(spellKey);
                wand.setActiveSpell(upgradeKey.getBaseKey());
            }
            mage.deactivateGUI();
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        Wand wand = mage.getActiveWand();
        this.context = context;
		Player player = mage.getPlayer();
		if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        if (wand == null) {
            return SpellResult.FAIL;
        }
        Collection<String> spells = wand.getSpells();
        Collection<ItemStack> upgrades = new ArrayList<ItemStack>();
        for (String spellKey : spells) {
            MageSpell spell = mage.getSpell(spellKey);
            long requiredCastCount = spell.getRequiredUpgradeCasts();
            MageSpell upgradeSpell = spell.getUpgrade();
            if (requiredCastCount > 0 && upgradeSpell != null) {
                ItemStack spellItem = MagicPlugin.getAPI().createSpellItem(spellKey);
                if (spellItem != null) {
                    ItemMeta meta = spellItem.getItemMeta();
                    List<String> lore = new ArrayList<String>();

                    String levelDescription = upgradeSpell.getLevelDescription();
                    if (levelDescription == null || levelDescription.isEmpty()) {
                        levelDescription = upgradeSpell.getName();
                    }

                    lore.add(levelDescription);
                    String upgradeDescription = upgradeSpell.getUpgradeDescription();
                    if (upgradeDescription != null && !upgradeDescription.isEmpty()) {
                        InventoryUtils.wrapText(upgradeDescription, BaseSpell.MAX_LORE_LENGTH, lore);
                    }
                    String requiredPathKey = spell.getRequiredUpgradePath();
                    WandUpgradePath currentPath = wand.getPath();
                    if (requiredPathKey != null && !currentPath.hasPath(requiredPathKey))
                    {
                        com.elmakers.mine.bukkit.wand.WandUpgradePath upgradePath = com.elmakers.mine.bukkit.wand.WandUpgradePath.getPath(requiredPathKey);
                        if (upgradePath == null) continue;
                        lore.add(context.getMessage("level_requirement").replace("$path", upgradePath.getName()));
                    }
                    long castCount = Math.min(spell.getCastCount(), requiredCastCount);
                    if (castCount == requiredCastCount) {
                        lore.add(ChatColor.GREEN + context.getMessage("cast_requirement")
                                .replace("$current", Long.toString(castCount))
                                .replace("$required", Long.toString(requiredCastCount)));
                    } else {
                        lore.add(ChatColor.RED + context.getMessage("cast_requirement")
                                .replace("$current", Long.toString(castCount))
                                .replace("$required", Long.toString(requiredCastCount)));
                    }

                    meta.setLore(lore);
                    spellItem.setItemMeta(meta);
                    upgrades.add(spellItem);
                }
            }
        }

        String inventoryTitle = context.getMessage("title", "Spell Upgrades");
        int invSize = ((upgrades.size() + 9) / 9) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        for (ItemStack item : upgrades)
        {
            displayInventory.addItem(item);
        }
        mage.activateGUI(this, displayInventory);

        return SpellResult.CAST;
	}
}
