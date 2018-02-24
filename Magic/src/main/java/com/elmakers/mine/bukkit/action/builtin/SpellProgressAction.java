package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
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
import java.util.Set;

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
            Wand wand = context.getWand();
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
        CasterProperties casterProperties = context.getActiveProperties();
        this.context = context;
		Player player = mage.getPlayer();
		if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        Collection<String> spells = casterProperties.getSpells();
        Collection<ItemStack> upgrades = new ArrayList<>();
        Messages messages = context.getController().getMessages();
        for (String spellKey : spells) {
            MageSpell spell = mage.getSpell(spellKey);
            SpellTemplate upgradeSpell = spell.getUpgrade();
            if (upgradeSpell != null) {
                ItemStack spellItem = MagicPlugin.getAPI().createSpellItem(upgradeSpell.getKey());
                if (spellItem != null) {
                    long requiredCastCount = spell.getRequiredUpgradeCasts();
                    String requiredPathKey = spell.getRequiredUpgradePath();

                    //ToDo:for skillAPI
                    String requiredSkillapiClass = spell.getRequiredSkillapiClass();
                    String requiredSkillapiSkill = spell.getRequiredSkillapiSkill();

                    Set<String> requiredPathTags = spell.getRequiredUpgradeTags();
                    ItemMeta meta = spellItem.getItemMeta();
                    List<String> lore = new ArrayList<>();

                    String levelDescription = upgradeSpell.getLevelDescription();
                    if (levelDescription == null || levelDescription.isEmpty()) {
                        levelDescription = upgradeSpell.getName();
                    }

                    lore.add(levelDescription);
                    String upgradeDescription = upgradeSpell.getUpgradeDescription();
                    if (upgradeDescription != null && !upgradeDescription.isEmpty()) {
                        InventoryUtils.wrapText(upgradeDescription, lore);
                    }
                    ProgressionPath currentPath = casterProperties.getPath();
                    if (requiredPathKey != null && currentPath == null) {
                        continue;
                    }

                    //SkillAPI
                    if (requiredSkillapiClass != null && !mage.hasSkillAPIClass(requiredSkillapiClass)) {
                        lore.add(ChatColor.RED + context.getMessage("skillapi_class_requirement").replace("$skillapiclass", requiredSkillapiClass));
                    }
                    if (requiredSkillapiSkill != null && !mage.hasSkillAPISkill(requiredSkillapiSkill)) {
                        lore.add(ChatColor.RED + context.getMessage("skillapi_skill_requirement").replace("$skillapiskill", requiredSkillapiSkill));
                    }

                    if (!upgradeSpell.getName().equals(spell.getName())) {
                        lore.add(context.getMessage("upgrade_name_change", "&r&4Upgrades: &r$name").replace("$name", spell.getName()));
                    }
                    if (requiredPathKey != null && !currentPath.hasPath(requiredPathKey))
                    {
                        requiredPathKey = currentPath.translatePath(requiredPathKey);
                        com.elmakers.mine.bukkit.wand.WandUpgradePath upgradePath = com.elmakers.mine.bukkit.wand.WandUpgradePath.getPath(requiredPathKey);
                        if (upgradePath == null) continue;
                        lore.add(context.getMessage("level_requirement").replace("$path", upgradePath.getName()));
                    }
                    if (requiredPathTags != null && !requiredPathTags.isEmpty() && !currentPath.hasAllTags(requiredPathTags)) {
                        Set<String> tags = currentPath.getMissingTags(requiredPathTags);
                        lore.add(context.getMessage("tags_requirement").replace("$tags", messages.formatList("tags", tags, "name")));
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
