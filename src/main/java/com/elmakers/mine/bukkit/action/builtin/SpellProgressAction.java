package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.action.GeneralAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpellProgressAction extends BaseSpellAction implements GeneralAction, GUIAction
{
    @Override
    public void deactivated() {

    }

    @Override
    public void clicked(InventoryClickEvent event)
    {
        int slot = event.getSlot();
        event.setCancelled(true);
    }

    @Override
    public SpellResult perform(ConfigurationSection parameters) {
        Mage mage = getMage();
		Player player = mage.getPlayer();
		if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        Wand wand = mage.getActiveWand();
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
                        lore.add(upgradeDescription);
                    }
                    String requiredPathKey = spell.getRequiredUpgradePath();
                    WandUpgradePath currentPath = wand.getPath();
                    if (requiredPathKey != null && !currentPath.hasPath(requiredPathKey))
                    {
                        com.elmakers.mine.bukkit.wand.WandUpgradePath upgradePath = com.elmakers.mine.bukkit.wand.WandUpgradePath.getPath(requiredPathKey);
                        if (upgradePath == null) continue;
                        lore.add(getMessage("level_requirement").replace("$path", upgradePath.getName()));
                    }
                    long castCount = Math.min(spell.getCastCount(), requiredCastCount);
                    if (castCount == requiredCastCount) {
                        lore.add(ChatColor.GREEN + getMessage("cast_requirement")
                                .replace("$current", Long.toString(castCount))
                                .replace("$required", Long.toString(requiredCastCount)));
                    } else {
                        lore.add(ChatColor.RED + getMessage("cast_requirement")
                                .replace("$current", Long.toString(castCount))
                                .replace("$required", Long.toString(requiredCastCount)));
                    }

                    meta.setLore(lore);
                    spellItem.setItemMeta(meta);
                    upgrades.add(spellItem);
                }
            }
        }

        String inventoryTitle = getMessage("title", "Spell Upgrades");
        int invSize = ((upgrades.size() + 9) / 9) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        for (ItemStack item : upgrades)
        {
            displayInventory.addItem(item);
        }
        mage.activateGUI(this);
        mage.getPlayer().openInventory(displayInventory);

        return SpellResult.CAST;
	}
}
