package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseShopAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.TextUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpellShopAction extends BaseShopAction
{
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
        showRequired = parameters.getBoolean("show_required", false);
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
        MageController controller = context.getController();
        SpellResult contextResult = checkContext(context);
        if (!contextResult.isSuccess()) {
            return contextResult;
        }
        Wand wand = context.getWand();
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

            Collection<String> pathSpells = currentPath.getSpells();
            for (String pathSpell : pathSpells) {
                spellPrices.put(pathSpell, null);
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

        List<ShopItem> shopItems = new ArrayList<ShopItem>();
        for (Map.Entry<String, Double> spellValue : spellPrices.entrySet()) {
            String key = spellValue.getKey();
            key = context.parameterize(key);
            String spellKey = key.split(" ", 2)[0];

            if (!castsSpells && wand.hasSpell(spellKey)) continue;

            SpellTemplate spell = controller.getSpellTemplate(spellKey);
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
                if (spell.getSpellKey().getLevel() > 1
                        && (requiredPathKey != null && !currentPath.hasPath(requiredPathKey)
                        || (requiresCastCounts && requiredCastCount > 0 && castCount < requiredCastCount)
                        || (requiredPathTags != null && !currentPath.hasAllTags(requiredPathTags))))
                {
                    ItemMeta meta = spellItem.getItemMeta();
                    List<String> itemLore = meta.getLore();
                    List<String> lore = new ArrayList<String>();
                    if (itemLore.size() > 0) {
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
                            lore.add(message);
                        }
                    } else if (requiredPathTags != null && !requiredPathTags.isEmpty() && !currentPath.hasAllTags(requiredPathTags)) {
                        Set<String> tags = currentPath.getMissingTags(requiredPathTags);
                        message = context.getMessage("tags_requirement").replace("$tags", TextUtils.formatTags(tags));
                        lore.add(message);
                    }

                    if (requiresCastCounts && requiredCastCount > 0 && castCount < requiredCastCount) {
                        message = ChatColor.RED + context.getMessage("cast_requirement", "&r&cCasts: &6$current&f/&e$required")
                                .replace("$current", Long.toString(castCount))
                                .replace("$required", Long.toString(requiredCastCount));
                        lore.add(message);
                    }

                    for (int i = 1; i < itemLore.size(); i++) {
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
        return showItems(context, shopItems);
	}

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("show_free");
        parameters.add("show_required");
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
