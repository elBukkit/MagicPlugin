package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.PrerequisiteSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;

@Deprecated
public class SpellShopAction extends com.elmakers.mine.bukkit.action.BaseShopAction
{
    private boolean showPath = true;
    private boolean showExtra = true;
    private boolean showRequired = false;
    private boolean showFree = false;
    private boolean showUpgrades = false;
    private boolean allowLocked = false;
    protected boolean requiresCastCounts = false;
    private Map<String, Double> spells = new HashMap<>();

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
            if (requireWand && !applyToCaster) {
                applyToWand = false;
            } else {
                applyToCaster = true;
            }
        }
    }

    @Nullable
    @Override
    public List<ShopItem> getItems(CastContext context) {
        Mage mage = context.getMage();
        Wand wand = mage.getActiveWand();
        CasterProperties caster = getCaster(context);

        // Check for auto upgrade, if the player can no longer progress on their current path and is
        // eligible for an upgrade, we will upgrade them and skip showing the spell shop so the player
        // can see their upgrade rewards.
        boolean canProgress = false;

        // TODO: Make this work on CasterProperties
        if (autoUpgrade && wand != null) {
            canProgress = caster.canProgress();
            if (!canProgress && wand.checkUpgrade(true) && wand.upgrade(false)) {
                return null;
            }
        }

        ProgressionPath currentPath = caster.getPath();

        if (!castsSpells && !allowLocked && wand != null && wand.isLocked()) {
            context.showMessage(context.getMessage("no_path", getDefaultMessage(context, "no_path")).replace("$wand", wand.getName()));
            return null;
        }

        // Load spells
        Map<String, Double> spellPrices = new HashMap<>();
        if (spells.size() > 0)
        {
            spellPrices.putAll(spells);
        }
        else
        {
            if (currentPath == null) {
                String name = wand == null ? "" : wand.getName();
                context.showMessage(context.getMessage("no_path", getDefaultMessage(context, "no_path")).replace("$wand", name));
                return null;
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
                Collection<String> spells = caster.getSpells();
                for (String spellKey : spells) {
                    MageSpell spell = mage.getSpell(spellKey);
                    SpellTemplate upgradeSpell = spell.getUpgrade();
                    if (upgradeSpell != null) {
                        spellPrices.put(upgradeSpell.getKey(), null);
                    }
                }
            }
        }

        List<ShopItem> shopItems = new ArrayList<>();
        for (Map.Entry<String, Double> spellValue : spellPrices.entrySet()) {
            ShopItem shopItem = createShopItem(spellValue.getKey(), spellValue.getValue(), context);
            if (shopItem != null) {
                shopItems.add(shopItem);
            }
        }

        Collections.sort(shopItems);

        if (spells.size() == 0 && showExtra && !castsSpells && currentPath != null) {
            Collection<String> extraSpells = currentPath.getExtraSpells();
            List<ShopItem> extraItems = new ArrayList<>();
            for (String spellKey : extraSpells) {
                ShopItem shopItem = createShopItem(spellKey, null, context);
                if (shopItem != null) {
                    ItemStack spellItem = shopItem.getItem();
                    ItemMeta meta = spellItem.getItemMeta();
                    List<String> itemLore = meta.getLore();
                    itemLore.add(context.getMessage("extra_spell", getDefaultMessage(context, "extra_spell")));
                    meta.setLore(itemLore);
                    spellItem.setItemMeta(meta);
                    extraItems.add(shopItem);
                }
            }
            Collections.sort(extraItems);
            shopItems.addAll(extraItems);
        }

        // If there is nothing here for us to do, check for upgrades being blocked
        // we will upgrade the wand here, but in theory that should never happen since we
        // checked for upgrades above.
        // TODO: Make this work for caster...
        mage.sendDebugMessage(ChatColor.GOLD + "Spells to buy: " + shopItems.size(), 2);
        if (wand != null && shopItems.size() == 0) {
            boolean canUpgrade = autoUpgrade && wand.checkUpgrade(false);
            boolean hasUpgrade = autoUpgrade && wand.hasUpgrade();
            if (!canProgress && !hasUpgrade) {
                context.showMessage(context.getMessage("no_upgrade", getDefaultMessage(context, "no_upgrade")).replace("$wand", wand.getName()));
                return null;
            } else if (canUpgrade) {
                wand.upgrade(false);
                return null;
            } else {
                return null;
            }
        }

        return shopItems;
    }

    @Nullable
    private ShopItem createShopItem(String key, Double worth, CastContext context) {
        CasterProperties caster = getCaster(context);
        Mage mage = context.getMage();
        MageController controller = context.getController();
        ProgressionPath currentPath = caster.getPath();

        key = context.parameterize(key);
        String spellKey = key.split(" ", 2)[0];

        if (!castsSpells && caster.hasSpell(spellKey)) {
            mage.sendDebugMessage(ChatColor.GRAY + " Skipping " + spellKey + ", already have it", 3);
            return null;
        }

        SpellTemplate spell = controller.getSpellTemplate(spellKey);
        if (spell == null) {
            mage.sendDebugMessage(ChatColor.RED + " Skipping " + spellKey + ", invalid spell", 0);
            return null;
        }

        if (worth == null) {
            worth = spell.getWorth();
        }
        if (worth <= 0 && !showFree) {
            mage.sendDebugMessage(ChatColor.YELLOW + " Skipping " + spellKey + ", is free (worth " + worth + ")", 3);
            return null;
        }
        if (!spell.hasCastPermission(mage.getCommandSender())) {
            mage.sendDebugMessage(ChatColor.YELLOW + " Skipping " + spellKey + ", no permission", 3);
            return null;
        }

        ItemStack spellItem = controller.createSpellItem(key, castsSpells);
        if (!castsSpells)
        {
            Spell mageSpell = caster.getSpell(spellKey);
            String requiredPathKey = mageSpell != null ? mageSpell.getRequiredUpgradePath() : null;
            Set<String> requiredPathTags = mageSpell != null ? mageSpell.getRequiredUpgradeTags() : null;
            long requiredCastCount = mageSpell != null ? mageSpell.getRequiredUpgradeCasts() : 0;
            long castCount = mageSpell != null ? Math.min(mageSpell.getCastCount(), requiredCastCount) : 0;
            Collection<PrerequisiteSpell> missingSpells = PrerequisiteSpell.getMissingRequirements(caster, spell);

            ItemMeta meta = spellItem.getItemMeta();
            List<String> itemLore = meta.getLore();
            if (itemLore == null) {
                mage.sendDebugMessage(ChatColor.RED + " Skipping " + spellKey + ", spell item is invalid", 0);
                return null;
            }
            List<String> lore = new ArrayList<>();
            if (spell.getSpellKey().getLevel() > 1 && itemLore.size() > 0) {
                lore.add(itemLore.get(0));
            }
            String upgradeDescription = spell.getUpgradeDescription();
            if (showUpgrades && upgradeDescription != null && !upgradeDescription.isEmpty()) {
                InventoryUtils.wrapText(upgradeDescription, controller.getMessages().get("spell.upgrade_description_prefix"), lore);
            }

            String unpurchasableMessage = null;

            Collection<Requirement> requirements = spell.getRequirements();
            String requirementMissing = controller.checkRequirements(context, requirements);
            if (requirementMissing != null) {
                unpurchasableMessage = requirementMissing;
                InventoryUtils.wrapText(unpurchasableMessage, lore);
            }

            if ((requiredPathKey != null && !currentPath.hasPath(requiredPathKey))
                    || (requiresCastCounts && requiredCastCount > 0 && castCount < requiredCastCount)
                    || (requiredPathTags != null && !currentPath.hasAllTags(requiredPathTags))
                    || !missingSpells.isEmpty()) {

                if (mageSpell != null && !spell.getName().equals(mageSpell.getName())) {
                    lore.add(context.getMessage("upgrade_name_change", getDefaultMessage(context, "upgrade_name_change")).replace("$name", mageSpell.getName()));
                }

                if (requiredPathKey != null && !currentPath.hasPath(requiredPathKey)) {
                    requiredPathKey = currentPath.translatePath(requiredPathKey);
                    com.elmakers.mine.bukkit.wand.WandUpgradePath upgradePath = com.elmakers.mine.bukkit.wand.WandUpgradePath.getPath(requiredPathKey);
                    if (upgradePath != null) {
                        unpurchasableMessage = context.getMessage("level_requirement", getDefaultMessage(context, "level_requirement")).replace("$path", upgradePath.getName());
                        InventoryUtils.wrapText(unpurchasableMessage, lore);
                    }
                } else if (requiredPathTags != null && !requiredPathTags.isEmpty() && !currentPath.hasAllTags(requiredPathTags)) {
                    Set<String> tags = currentPath.getMissingTags(requiredPathTags);
                    unpurchasableMessage = context.getMessage("tags_requirement", getDefaultMessage(context, "tags_requirement")).replace("$tags", controller.getMessages().formatList("tags", tags, "name"));
                    InventoryUtils.wrapText(unpurchasableMessage, lore);
                }

                if (requiresCastCounts && requiredCastCount > 0 && castCount < requiredCastCount) {
                    unpurchasableMessage = ChatColor.RED + context.getMessage("cast_requirement", getDefaultMessage(context, "cast_requirement"))
                            .replace("$current", Long.toString(castCount))
                            .replace("$required", Long.toString(requiredCastCount));
                    InventoryUtils.wrapText(unpurchasableMessage, lore);
                }

                if (!missingSpells.isEmpty()) {
                    List<String> spells = new ArrayList<>(missingSpells.size());
                    for (PrerequisiteSpell s : missingSpells) {
                        SpellTemplate template = context.getController().getSpellTemplate(s.getSpellKey().getKey());
                        String spellMessage = context.getMessage("prerequisite_spell_level", getDefaultMessage(context, "prerequisite_spell_level"))
                                .replace("$name", template.getName());
                        if (s.getProgressLevel() > 1) {
                            spellMessage += context.getMessage("prerequisite_spell_progress_level", getDefaultMessage(context, "prerequisite_spell_progress_level"))
                                    .replace("$level", Long.toString(s.getProgressLevel()))
                                    // This max level should never return 0 here but just in case we'll make the min 1.
                                    .replace("$max_level", Long.toString(Math.max(1, template.getMaxProgressLevel())));
                        }
                        spells.add(spellMessage);
                    }
                    unpurchasableMessage = ChatColor.RED + context.getMessage("required_spells", getDefaultMessage(context, "required_spells"))
                            .replace("$spells", StringUtils.join(spells, ", "));
                    InventoryUtils.wrapText(ChatColor.GOLD + unpurchasableMessage, lore);
                }
            }

            for (int i = (spell.getSpellKey().getLevel() > 1 ? 1 : 0); i < itemLore.size(); i++) {
                lore.add(itemLore.get(i));
            }
            meta.setLore(lore);
            spellItem.setItemMeta(meta);

            if (unpurchasableMessage != null) InventoryUtils.setMeta(spellItem, "unpurchasable", unpurchasableMessage);
        }

        return new ShopItem(spellItem, worth);
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
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
