package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseShopAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.PrerequisiteSpell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpellShopAction extends BaseShopAction
{
    private boolean showPath = true;
    private boolean showExtra = true;
    private boolean showRequired = false;
    private boolean showFree = false;
    private boolean showUpgrades = false;
    private boolean allowLocked = false;
    protected boolean requiresCastCounts = false;
    private Map<String, Double> spells = new HashMap<String, Double>();

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        spells.clear();
        if (parameters.contains("spells"))
        {
            if (parameters.isConfigurationSection("spells"))
            {
                ConfigurationSection spellSection = ConfigurationUtils.getConfigurationSection(parameters, "spells");
                Collection<String> spellKeys = spellSection.getKeys(false);
                for (String spellKey : spellKeys) {
                    spells.put(spellKey, spellSection.getDouble(spellKey));
                }
            }
            else
            {
                Collection<String> spellList = ConfigurationUtils.getStringList(parameters, "spells");
                if (spellList != null)
                {
                    for (String spellKey : spellList)
                    {
                        spells.put(spellKey, null);
                    }
                }
            }
        }
    }

    @Override
    public void deactivated() {

    }

    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        showPath = parameters.getBoolean("show_path_spells", true);
        showExtra = parameters.getBoolean("show_extra_spells", true);
        showRequired = parameters.getBoolean("show_required_spells", false);
        showFree = parameters.getBoolean("show_free", false);
        showUpgrades = parameters.getBoolean("show_upgrades", false);
        allowLocked = parameters.getBoolean("allow_locked", false);
        requiresCastCounts = parameters.getBoolean("upgrade_requires_casts", false);
        if (!castsSpells) {
            requireWand = true;
            applyToWand = true;
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        Wand wand = mage.getActiveWand();
        if (wand != null && autoUpgrade && spells.size() == 0) {
            if (!wand.checkAndUpgrade(false) && !showUpgrades) {
                return SpellResult.FAIL;
            }
        }

        MageController controller = context.getController();
        SpellResult contextResult = checkContext(context);
        if (!contextResult.isSuccess()) {
            return contextResult;
        }
        WandUpgradePath currentPath = wand == null ? null : wand.getPath();

        if (!castsSpells && !allowLocked && wand.isLocked()) {
            context.showMessage(context.getMessage("no_path", "You may not learn with that $wand.").replace("$wand", wand.getName()));
            return SpellResult.FAIL;
        }

        // Load spells
        Map<String, Double> spellPrices = new HashMap<String, Double>();
        if (spells.size() > 0)
        {
            spellPrices.putAll(spells);
        }
        else
        {
            if (currentPath == null) {
                context.showMessage(context.getMessage("no_path", "You may not learn with that $wand.").replace("$wand", wand.getName()));
                return SpellResult.FAIL;
            }

            if (showPath) {
                Collection<String> pathSpells = currentPath.getSpells();
                for (String pathSpell : pathSpells) {
                    spellPrices.put(pathSpell, null);
                }
            }
            if (showRequired) {
                Collection<String> requiredSpells = currentPath.getRequiredSpells();
                for (String requiredSpell : requiredSpells) {
                    spellPrices.put(requiredSpell, null);
                }
            }
            if (showUpgrades) {
                Collection<String> spells = wand.getSpells();
                for (String spellKey : spells) {
                    MageSpell spell = mage.getSpell(spellKey);
                    SpellTemplate upgradeSpell = spell.getUpgrade();
                    if (upgradeSpell != null) {
                        spellPrices.put(upgradeSpell.getKey(), null);
                    }
                }
            }
        }

        Messages messages = context.getController().getMessages();
        List<ShopItem> shopItems = new ArrayList<ShopItem>();
        for (Map.Entry<String, Double> spellValue : spellPrices.entrySet()) {
            String key = spellValue.getKey();
            key = context.parameterize(key);
            String spellKey = key.split(" ", 2)[0];

            if (!castsSpells && wand.hasSpell(spellKey)) continue;

            SpellTemplate spell = controller.getSpellTemplate(spellKey);
            if (spell == null) continue;
            
            Double worth = spellValue.getValue();
            if (worth == null) {
                worth = spell.getWorth();
            }
            if (worth <= 0 && !showFree) continue;
            if (!spell.hasCastPermission(mage.getCommandSender())) continue;

            ItemStack spellItem = controller.createSpellItem(key, castsSpells);
            if (!castsSpells)
            {
                Spell mageSpell = wand != null ? wand.getSpell(spellKey) : null;
                String requiredPathKey = mageSpell != null ? mageSpell.getRequiredUpgradePath() : null;
                Set<String> requiredPathTags = mageSpell != null ? mageSpell.getRequiredUpgradeTags() : null;
                long requiredCastCount = mageSpell != null ? mageSpell.getRequiredUpgradeCasts() : 0;
                long castCount = mageSpell != null ? Math.min(mageSpell.getCastCount(), requiredCastCount) : 0;
                Collection<PrerequisiteSpell> missingSpells = PrerequisiteSpell.getMissingRequirements(wand, spell);

                if (requiredPathKey != null && !currentPath.hasPath(requiredPathKey)
                        || (requiresCastCounts && requiredCastCount > 0 && castCount < requiredCastCount)
                        || (requiredPathTags != null && !currentPath.hasAllTags(requiredPathTags))
                        || !missingSpells.isEmpty()) {
                    ItemMeta meta = spellItem.getItemMeta();
                    List<String> itemLore = meta.getLore();
                    List<String> lore = new ArrayList<String>();
                    if (spell.getSpellKey().getLevel() > 1 && itemLore.size() > 0) {
                        lore.add(itemLore.get(0));
                    }
                    String upgradeDescription = spell.getUpgradeDescription();
                    if (upgradeDescription != null && !upgradeDescription.isEmpty()) {
                        InventoryUtils.wrapText(upgradeDescription, BaseSpell.MAX_LORE_LENGTH, lore);
                    }

                    if (mageSpell != null && !spell.getName().equals(mageSpell.getName())) {
                        lore.add(context.getMessage("upgrade_name_change", "&r&4Upgrades: &r$name").replace("$name", mageSpell.getName()));
                    }

                    String message = null;
                    if (requiredPathKey != null && !currentPath.hasPath(requiredPathKey)) {
                        requiredPathKey = currentPath.translatePath(requiredPathKey);
                        com.elmakers.mine.bukkit.wand.WandUpgradePath upgradePath = com.elmakers.mine.bukkit.wand.WandUpgradePath.getPath(requiredPathKey);
                        if (upgradePath != null) {
                            message = context.getMessage("level_requirement", "&r&cRequires: &6$path").replace("$path", upgradePath.getName());
                            InventoryUtils.wrapText(message, BaseSpell.MAX_LORE_LENGTH, lore);
                        }
                    } else if (requiredPathTags != null && !requiredPathTags.isEmpty() && !currentPath.hasAllTags(requiredPathTags)) {
                        Set<String> tags = currentPath.getMissingTags(requiredPathTags);
                        message = context.getMessage("tags_requirement", "&r&cRequires: &6$tags").replace("$tags", messages.formatList("tags", tags, "name"));
                        InventoryUtils.wrapText(message, BaseSpell.MAX_LORE_LENGTH, lore);
                    }

                    if (requiresCastCounts && requiredCastCount > 0 && castCount < requiredCastCount) {
                        message = ChatColor.RED + context.getMessage("cast_requirement", "&r&cCasts: &6$current&f/&e$required")
                                .replace("$current", Long.toString(castCount))
                                .replace("$required", Long.toString(requiredCastCount));
                        lore.add(message);
                    }

                    if (!missingSpells.isEmpty()) {
                        List<String> spells = new ArrayList<String>(missingSpells.size());
                        for (PrerequisiteSpell s : missingSpells) {
                            SpellTemplate template = context.getController().getSpellTemplate(s.getSpellKey().getKey());
                            String spellMessage = context.getMessage("prerequisite_spell_level", "&6Level $level $name")
                                    .replace("$name", template.getName())
                                    .replace("$level", Integer.toString(s.getSpellKey().getLevel()));
                            if (s.getProgressLevel() > 1) {
                                spellMessage += context.getMessage("prerequisite_spell_progress_level", " (Progress $level/$max_level)")
                                        .replace("$level", Long.toString(s.getProgressLevel()))
                                        // This max level should never return 0 here but just in case we'll make the min 1.
                                        .replace("$max_level", Long.toString(Math.max(1, template.getMaxProgressLevel())));
                            }
                            spells.add(spellMessage);
                        }
                        message = ChatColor.RED + context.getMessage("required_spells", "&r&cRequires: $spells")
                                .replace("$spells", StringUtils.join(spells, ", "));
                        InventoryUtils.wrapText(ChatColor.GOLD.toString(), message, BaseSpell.MAX_LORE_LENGTH, lore);
                    }

                    for (int i = (spell.getSpellKey().getLevel() > 1 ? 1 : 0); i < itemLore.size(); i++) {
                        lore.add(itemLore.get(i));
                    }
                    meta.setLore(lore);
                    spellItem.setItemMeta(meta);
                    if (message != null) InventoryUtils.setMeta(spellItem, "unpurchasable", message);
                }
            }
            
            shopItems.add(new ShopItem(spellItem, worth));
        }

        Collections.sort(shopItems);
        
        if (spells.size() == 0 && showExtra && !castsSpells) {
            Collection<String> extraSpells = currentPath.getExtraSpells();
            List<ShopItem> extraItems = new ArrayList<ShopItem>();
            for (String spellKey : extraSpells) {
                if (wand.hasSpell(spellKey)) continue;

                SpellTemplate spell = controller.getSpellTemplate(spellKey);
                if (spell == null) continue;
                double worth = spell.getWorth();
                if (worth <= 0 && !showFree) continue;
                if (!spell.hasCastPermission(mage.getCommandSender())) continue;

                ItemStack spellItem = controller.createSpellItem(spellKey, castsSpells);
                ItemMeta meta = spellItem.getItemMeta();
                List<String> itemLore = meta.getLore();
                itemLore.add(context.getMessage("extra_spell", "&aNot Required"));
                meta.setLore(itemLore);
                spellItem.setItemMeta(meta);
                extraItems.add(new ShopItem(spellItem, worth));
            }
            Collections.sort(extraItems);
            shopItems.addAll(extraItems);
        }
        return showItems(context, shopItems);
	}

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("show_free");
        parameters.add("show_path_spells");
        parameters.add("show_extra_spells");
        parameters.add("show_required_spells");
        parameters.add("show_upgrades");
        parameters.add("allow_locked");
        parameters.add("upgrade_requires_casts");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("show_free") || parameterKey.equals("show_required")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
