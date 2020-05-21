package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageClassTemplate;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAttribute;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import com.elmakers.mine.bukkit.api.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.item.Cost;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;

import de.slikey.effectlib.math.EquationStore;
import de.slikey.effectlib.math.EquationTransform;

public class SelectorAction extends CompoundAction implements GUIAction, CostReducer
{
    private static final int MAX_INVENTORY_SLOTS = 6 * 9;
    protected double costScale = 1;
    protected double earnScale = 1;
    protected boolean autoClose = true;
    protected SelectorConfiguration defaultConfiguration;
    protected MaterialAndData confirmFillMaterial;
    protected CastContext context;
    private Map<Integer, SelectorOption> showingItems;
    private int itemCount;
    private int numSlots;
    private int has = 0;
    private String title;
    private String confirmTitle;
    private String confirmUnlockTitle;

    // State
    private boolean isActive = false;
    private SpellResult finalResult = null;

    protected class RequirementsResult {
        public final SpellResult result;
        public final String message;

        public RequirementsResult(SpellResult result, String message) {
            this.result = result;
            this.message = message;
        }

        public RequirementsResult(SpellResult result) {
            this(result, context.getMessage(result.name().toLowerCase()));
        }

        @Override
        public String toString() {
            return result.toString() + " " + message;
        }
    }

    protected enum ModifierType { WAND, MAGE, CLASS, ATTRIBUTE }

    protected class CostModifier {
        private ModifierType type;
        private String equation;
        private String property;
        private double defaultValue;

        public CostModifier(ConfigurationSection configuration) {
            String typeString = configuration.getString("type");
            try {
                type = ModifierType.valueOf(typeString.toUpperCase());
            } catch (Exception ex) {
                context.getLogger().warning("Invalid modifier type in selector config: " + typeString);
                type = null;
                return;
            }

            defaultValue = configuration.getDouble("default");
            equation = configuration.getString("scale");
            if (type == ModifierType.ATTRIBUTE) {
                property = configuration.getString("attribute");
            } else {
                property = configuration.getString("property");
            }
        }

        public void modify(Cost cost) {
            if (type == null) return;

            Mage mage = context.getMage();
            double value = defaultValue;
            switch (type) {
                case MAGE:
                    value = mage.getProperties().getProperty(property, value);
                    break;
                case ATTRIBUTE:
                    Double attribute = mage.getAttribute(property);
                    if (attribute != null) {
                        value = attribute;
                    }
                    break;
                case CLASS:
                    CasterProperties activeClass = mage.getActiveClass();
                    if (activeClass != null) {
                        value = activeClass.getProperty(property, value);
                    }
                    break;
                case WAND:
                    Wand wand = mage.getActiveWand();
                    if (wand != null) {
                        value = wand.getProperty(property, value);
                    }
                    break;
            }
            EquationTransform transform = EquationStore.getInstance().getTransform(equation);
            transform.setVariable("x", value);
            double scale = transform.get();
            cost.scale(scale);
        }
    }

    protected class SelectorConfiguration {
        protected @Nullable ItemStack icon;
        protected @Nullable List<ItemStack> items;
        protected @Nullable List<Cost> costs = null;
        protected @Nonnull String costType = "currency";
        protected @Nonnull String earnType = "currency";
        protected @Nullable String costOverride = null;
        protected @Nonnull String costTypeFallback = "item";
        protected @Nullable String castSpell = null;
        protected @Nullable String unlockClass = null;
        protected @Nullable String selectedMessage = null;
        protected @Nullable String selectedFreeMessage = null;
        protected @Nullable String unlockKey = null;
        protected @Nullable String actions = null;
        protected @Nonnull String unlockSection = "unlocked";
        protected @Nullable Collection<Requirement> requirements;
        protected @Nullable List<String> commands;
        protected @Nullable List<CostModifier> costModifiers;
        protected @Nullable List<Cost> earns = null;
        protected @Nullable Map<String,String> alternateSpellTags;
        protected @Nonnull String effects = "selected";
        protected @Nullable String attributeKey = null;
        protected int attributeAmount = 0;
        protected boolean applyToWand = false;
        protected boolean applyToCaster = false;
        protected MagicPropertyType applyTo = null;
        protected @Nullable String applyToClass = null;
        protected boolean showConfirmation = false;
        protected boolean showUnavailable = false;
        protected boolean switchClass = false;
        protected boolean putInHand = false;
        protected boolean free = false;
        protected boolean applyLoreToItem = false;
        protected boolean applyNameToItem = false;

        protected int limit = 0;

        public SelectorConfiguration(ConfigurationSection configuration) {
            parse(configuration);
        }

        protected SelectorConfiguration() {
        }

        protected void parse(ConfigurationSection configuration) {
            applyToWand = configuration.getBoolean("apply_to_wand", applyToWand);
            applyToCaster = configuration.getBoolean("apply_to_caster", applyToCaster);
            applyToClass = configuration.getString("apply_to_class", applyToClass);
            putInHand = configuration.getBoolean("put_in_hand", putInHand);
            castSpell = configuration.getString("cast_spell", castSpell);
            unlockClass = configuration.getString("unlock_class", unlockClass);
            if (configuration.contains("switch_class")) {
                switchClass = true;
                unlockClass = configuration.getString("switch_class");
            }
            String applyToString = configuration.getString("apply_to", applyTo == null ? null : applyTo.name());
            if (applyToString != null && !applyToString.isEmpty()) {
                try {
                    applyTo = MagicPropertyType.valueOf(applyToString.toUpperCase());
                } catch (Exception ex) {
                    context.getLogger().warning("Invalid apply_to: " + applyToString);
                }
            } else {
                applyTo = null;
            }
            attributeAmount = configuration.getInt("attribute_amount", attributeAmount);
            attributeKey = configuration.getString("attribute", attributeKey);
            limit = configuration.getInt("limit", limit);
            unlockKey = configuration.getString("unlock", unlockKey);
            unlockSection = configuration.getString("unlock_section", unlockSection);
            showConfirmation = configuration.getBoolean("confirm", showConfirmation);
            costType = configuration.getString("cost_type", costType);
            costOverride = configuration.getString("cost_override", costOverride);
            earnType = configuration.getString("earn_type", earnType);
            costTypeFallback = configuration.getString("cost_type_fallback", costTypeFallback);
            actions = configuration.getString("actions", actions);
            showUnavailable = configuration.getBoolean("show_unavailable", showUnavailable);
            commands = ConfigurationUtils.getStringList(configuration, "commands");
            free = configuration.getBoolean("free", free);
            effects = configuration.getString("effects", effects);
            applyLoreToItem = configuration.getBoolean("apply_lore_to_item", applyLoreToItem);
            applyNameToItem = configuration.getBoolean("apply_name_to_item", applyNameToItem);

            if (costType.isEmpty() || costType.equalsIgnoreCase("none")) {
                free = true;
            }

            selectedMessage = configuration.getString("selected", selectedMessage);
            selectedFreeMessage = configuration.getString("selected_free", selectedFreeMessage);

            Collection<ConfigurationSection> requirementConfigurations = ConfigurationUtils.getNodeList(configuration, "requirements");
            if (requirementConfigurations != null) {
                requirements = new ArrayList<>();
                for (ConfigurationSection requirementConfiguration : requirementConfigurations) {
                    requirements.add(new Requirement(requirementConfiguration));
                }
            }

            if (configuration.contains("item")) {
                items = new ArrayList<>();
                ItemStack item = parseItem(configuration.getString("item"));
                if (item != null) {
                    items.add(item);
                }
            }
            if (configuration.contains("items")) {
                List<String> itemList = configuration.getStringList("items");
                if (itemList.size() > 0) {
                    items = new ArrayList<>();
                    for (String itemKey : itemList) {
                        items.add(parseItem(itemKey));
                    }
                }
            }

            ConfigurationSection altTags = ConfigurationUtils.getConfigurationSection(configuration, "cast_for_tags");
            if (altTags != null) {
                alternateSpellTags = new HashMap<>();
                for (String key : altTags.getKeys(false)) {
                    alternateSpellTags.put(key, altTags.getString(key));
                }
            }

            if (actions != null && !actions.isEmpty()) {
                addHandler(context.getSpell(), actions);
            }

            // Prevent out of bounds exceptions later
            if (items != null && items.isEmpty()) {
                items = null;
            }

            MageController controller = context.getController();
            icon = parseItem(configuration.getString("icon"));
            if (icon != null && icon.hasItemMeta()) {
                ItemMeta meta = icon.getItemMeta();
                meta.setLore(null);
                icon.setItemMeta(meta);
            }
            costModifiers = parseCostModifiers(configuration, "cost_modifiers");
            if (!free) {
                costs = parseCosts(ConfigurationUtils.getConfigurationSection(configuration, "costs"));
                double cost = configuration.getDouble("cost");
                if (cost > 0) {
                    if (costs == null) {
                        costs = new ArrayList<>();
                    }
                    Cost optionCost = new com.elmakers.mine.bukkit.item.Cost(context.getController(), costType, cost);
                    if (costOverride != null) {
                        optionCost.convert(controller, costOverride);
                    }
                    optionCost.checkSupported(controller, costTypeFallback);
                    optionCost.scale(controller.getWorthBase());
                    costs.add(optionCost);
                } else if (configuration.isString("cost")) {
                    if (costs == null) {
                        costs = new ArrayList<>();
                    }
                    costs.add(new com.elmakers.mine.bukkit.item.Cost(context.getController(), configuration.getString("cost"), 1));
                }

                if (costs == null && items != null) {
                    costs = new ArrayList<>();
                    for (ItemStack item : items) {
                        Cost itemCost = null;
                        String spellKey = controller.getSpell(item);
                        if (spellKey == null) {
                            Double worth = controller.getWorth(item);
                            if (worth != null && worth > 0) {
                                itemCost = new com.elmakers.mine.bukkit.item.Cost(context.getController(), costType, worth);
                            }
                        } else {
                            SpellTemplate spell = controller.getSpellTemplate(spellKey);
                            itemCost = (Cost)spell.getCost();
                        }
                        if (itemCost != null) {
                            if (costOverride != null) {
                                itemCost.convert(controller, costOverride);
                                itemCost.checkSupported(controller, costTypeFallback);
                            } else {
                                itemCost.checkSupported(controller, costType, costTypeFallback);
                            }
                            itemCost.scale(controller.getWorthBase());
                            costs.add(itemCost);
                        }
                    }
                }
            }

            if ((applyNameToItem || applyLoreToItem) && items != null) {
                for (ItemStack item : items) {
                    ItemMeta meta = item.getItemMeta();
                    String customName = configuration.getString("name");
                    if (applyNameToItem && customName != null  && !customName.isEmpty()) {
                        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
                    }
                    List<String> lore = configuration.contains("lore") ? configuration.getStringList("lore") : null;
                    if (applyLoreToItem && lore != null) {
                        List<String> translated = new ArrayList<>();
                        for (String line : lore) {
                            translated.add(ChatColor.translateAlternateColorCodes('&', line));
                        }
                        meta.setLore(translated);
                    }
                    item.setItemMeta(meta);
                }
            }

            earns = parseCosts(ConfigurationUtils.getConfigurationSection(configuration, "earns"));
            double earn = configuration.getDouble("earn");
            if (earn > 0) {
                if (earns == null) {
                    earns = new ArrayList<>();
                }

                Cost earnCost = new com.elmakers.mine.bukkit.item.Cost(context.getController(), earnType, earn);
                earnCost.checkSupported(controller, costType, costTypeFallback);
                earnCost.scale(controller.getWorthBase());
                earnCost.scale(earnScale);
                earns.add(earnCost);
            }
        }

        @Nullable
        protected List<Cost> parseCosts(ConfigurationSection node) {
            return Cost.parseCosts(node, context.getController());
        }

        @Nullable
        protected List<CostModifier> parseCostModifiers(ConfigurationSection configuration, String section) {
            Collection<ConfigurationSection> modifierConfigs = ConfigurationUtils.getNodeList(configuration, section);
            if (modifierConfigs == null) {
                return null;
            }
            List<CostModifier> modifiers = new ArrayList<>();
            for (ConfigurationSection modifierConfig : modifierConfigs) {
                modifiers.add(new CostModifier(modifierConfig));
            }

            return modifiers;
        }

        public boolean hasLimit() {
            return limit > 0;
        }

        public String getCostType() {
            return costType;
        }

        public String getCostTypeFallback() {
            return costTypeFallback;
        }

        public boolean has(CastContext context) {
            Mage mage = context.getMage();
            if (unlockClass != null && !unlockClass.isEmpty()) {
                if (mage.hasClassUnlocked(unlockClass)) {
                    return true;
                }
            }

            return false;
        }

        public RequirementsResult checkRequirements(CastContext context) {
            MageController controller = context.getController();
            Mage mage = context.getMage();
            Player player = mage.getPlayer();
            if (player == null) {
                return new SelectorAction.RequirementsResult(SpellResult.PLAYER_REQUIRED);
            }

            if (limit > 0 && has >= limit) {
                return new RequirementsResult(SpellResult.NO_TARGET, getMessage("at_limit").replace("$limit", Integer.toString(limit)));
            }

            if (unlockClass != null && !unlockClass.isEmpty()) {
                if (mage.hasClassUnlocked(unlockClass)) {
                    return new RequirementsResult(SpellResult.NO_TARGET, getMessage("has_class").replace("$class", unlockClass));
                }
            }

            if (requirements != null) {
                String message = controller.checkRequirements(context, requirements);
                if (message != null) {
                    return new RequirementsResult(SpellResult.NO_TARGET, message);
                }
            }

            return new RequirementsResult(SpellResult.CAST);
        }

        public boolean isUnlock() {
            return unlockKey != null && !unlockKey.isEmpty();
        }

        public boolean showIfUnavailable() {
            return showUnavailable;
        }
    }

    protected class SelectorOption extends SelectorConfiguration {
        protected Integer slot = null;
        protected String name = null;
        protected List<String> lore = null;
        protected String unavailableMessage;
        protected boolean placeholder;
        protected boolean unavailable;

        public SelectorOption(SelectorConfiguration defaults, ConfigurationSection configuration, CastContext context, CostReducer reducer) {
            super();

            this.selectedMessage = defaults.selectedMessage;
            this.selectedFreeMessage = defaults.selectedFreeMessage;
            this.items = defaults.items;
            this.costs = defaults.costs;
            this.castSpell = defaults.castSpell;
            this.applyToWand = defaults.applyToWand;
            this.applyToCaster = defaults.applyToCaster;
            this.applyTo = defaults.applyTo;
            this.applyToClass = defaults.applyToClass;
            this.attributeKey = defaults.attributeKey;
            this.attributeAmount = defaults.attributeAmount;
            this.unlockClass = defaults.unlockClass;
            this.switchClass = defaults.switchClass;
            this.putInHand = defaults.putInHand;
            this.limit = defaults.limit;
            this.unlockKey = defaults.unlockKey;
            this.unlockSection = defaults.unlockSection;
            this.showConfirmation = defaults.showConfirmation;
            this.costType = defaults.costType;
            this.costTypeFallback = defaults.costTypeFallback;
            this.earnType = defaults.earnType;
            this.showUnavailable = defaults.showUnavailable;
            this.commands = defaults.commands;
            this.actions = defaults.actions;
            this.free = defaults.free;
            this.costOverride = defaults.costOverride;
            this.effects = defaults.effects;
            this.applyLoreToItem = defaults.applyLoreToItem;
            this.applyNameToItem = defaults.applyNameToItem;
            this.lore = configuration.contains("lore") ? configuration.getStringList("lore") : new ArrayList<>();

            placeholder = configuration.getBoolean("placeholder") || configuration.getString("item", "").equals("none");
            if (placeholder) {
                this.icon = new ItemStack(Material.AIR);
                return;
            }

            parse(configuration);

            if (defaults.requirements != null) {
                if (requirements == null) {
                    requirements = defaults.requirements;
                } else {
                    requirements.addAll(defaults.requirements);
                }
            }

            if (configuration.contains("slot")) {
                slot = configuration.getInt("slot");
            }

            MageController controller = context.getController();
            name = configuration.getString("name", "");
            if (name.isEmpty() && unlockClass != null && !unlockClass.isEmpty()) {
                MageClassTemplate mageClass = controller.getMageClassTemplate(unlockClass);
                name = getMessage("unlock_class");
                if (mageClass != null) {
                    name = name.replace("$class", mageClass.getName());
                } else {
                    controller.getLogger().warning("Unknown class in selector config: " + unlockClass);
                }
            }
            String castSpell = getCastSpell(context.getWand());
            if (name.isEmpty() && castSpell != null && !castSpell.isEmpty()) {
                SpellTemplate spell = controller.getSpellTemplate(castSpell);
                name = getMessage("cast_spell");
                if (spell != null) {
                    name = name.replace("$spell", spell.getName());
                } else {
                    controller.getLogger().warning("Unknown spell in selector config: " + castSpell);
                }
            }

            MagicAttribute attribute = attributeKey == null ? null : controller.getAttribute(attributeKey);
            if (name.isEmpty() && attribute != null) {
                name = attribute.getName(controller.getMessages());
                if (attributeAmount != 0) {
                    String template = attributeAmount < 0 ? getMessage("decrease_attribute") : getMessage("increase_attribute");
                    name = template.replace("$attribute", name)
                        .replace("$amount", Integer.toString(Math.abs(attributeAmount)));
                }
            }

            if (name.isEmpty() && items != null) {
                ItemStack item = items.get(0);
                name = controller.describeItem(item);
                if (item.getAmount() > 1) {
                    String template = getMessage("item_amount");
                    name = template.replace("$name", name).replace("$amount", Integer.toString(item.getAmount()));
                }
            }

            if (name.isEmpty() && icon != null) {
                name = controller.describeItem(icon);
                if (icon.getAmount() > 1) {
                    String template = getMessage("item_amount");
                    name = template.replace("$name", name).replace("$amount", Integer.toString(icon.getAmount()));
                }
            }

            name = ChatColor.translateAlternateColorCodes('&', name);

            String description = configuration.getString("description");
            if (description == null) {
                if (unlockClass != null && !unlockClass.isEmpty()) {
                    MageClassTemplate mageClass = controller.getMageClassTemplate(unlockClass);
                    description = mageClass.getDescription();
                } else if (castSpell != null && !castSpell.isEmpty()) {
                    SpellTemplate spell = controller.getSpellTemplate(castSpell);
                    if (spell == null) {
                        controller.getLogger().warning("Unknown spell in selector config: " + castSpell);
                    } else {
                        description = spell.getDescription();
                    }
                } else if (attribute != null && attributeAmount == 0) {
                    description = attribute.getDescription(controller.getMessages());
                }
            }

            if (description != null && !description.isEmpty()) {
                InventoryUtils.wrapText(description, lore);
            }

            boolean unlocked = false;
            if (unlockKey != null && !unlockKey.isEmpty()) {
                Mage mage = context.getMage();
                ConfigurationSection unlocks = mage.getData().getConfigurationSection(unlockSection);
                if (unlocks != null && unlocks.getBoolean(unlockKey, false)) {
                    unlocked = true;
                    costs = null;
                    showConfirmation = false;
                    String unlockedMessage = getMessage("unlocked_lore");
                    InventoryUtils.wrapText(unlockedMessage, lore);
                }
            }

            // Unlocked options skip requirements and costs
            if (!unlocked) {
                RequirementsResult check = checkRequirements(context);
                if (!check.result.isSuccess() && !hasAltTags(context.getWand())) {
                    unavailable = true;
                    unavailableMessage = check.message;
                    if (unavailableMessage != null && !unavailableMessage.isEmpty()) {
                        InventoryUtils.wrapText(check.message, lore);
                    }
                }
            }

            // Don't show costs if unavailable
            if (costs != null && !unavailable) {
                String costHeading = getMessage("cost_heading");
                if (!costHeading.isEmpty()) {
                    InventoryUtils.wrapText(costHeading, lore);
                }

                String costKey = unlockKey != null && !unlockKey.isEmpty() ? "unlock_cost_lore" : "cost_lore";
                String requiredKey = unlockKey != null && !unlockKey.isEmpty() ? "required_unlock_cost_lore" : "required_cost_lore";
                String costString = getMessage(costKey);
                String requiredCostString = getMessage(requiredKey);
                for (Cost cost : costs) {
                    if (costModifiers != null) {
                        for (CostModifier modifier : costModifiers) {
                            modifier.modify(cost);
                        }
                    }

                    String costDescription = cost.has(context.getMage(), context.getWand(), reducer) ? costString : requiredCostString;
                    costDescription = costDescription.replace("$cost", cost.getFullDescription(context.getController().getMessages(), reducer));
                    InventoryUtils.wrapText(costDescription, lore);
                }
            } else if (unlockKey != null && !unlockKey.isEmpty() && !unlocked) {
                unavailable = true;
                String lockedMessage = getMessage("locked");
                if (!lockedMessage.isEmpty()) {
                    InventoryUtils.wrapText(lockedMessage, lore);
                    if (unavailableMessage == null) {
                        unavailableMessage = lockedMessage;
                    }
                }
            }

            // Add earn lore
            if (earns != null) {
                String costHeading = getMessage("earn_heading");
                if (!costHeading.isEmpty()) {
                    InventoryUtils.wrapText(costHeading, lore);
                }

                String earnString = getMessage("earn_lore");
                for (Cost earn : earns) {
                    earnString = earnString.replace("$earn", earn.getFullDescription(context.getController().getMessages(), reducer));
                    InventoryUtils.wrapText(earnString, lore);
                }
            }

            // Choose icon if none was set in config
            if (icon == null && items != null) {
                icon = InventoryUtils.getCopy(items.get(0));
                // This prevents getting two copies of the lore
                // Only do this if lore was actually provided, since this setting is on by default for the Shop action
                if (applyLoreToItem && configuration.contains("lore")) {
                    ItemMeta meta = icon.getItemMeta();
                    meta.setLore(null);
                    icon.setItemMeta(meta);
                }
            }

            if (icon == null && castSpell != null && !castSpell.isEmpty()) {
                SpellTemplate spellTemplate = context.getController().getSpellTemplate(castSpell);
                if (spellTemplate != null) {
                    if (unavailable && spellTemplate.getDisabledIcon() != null) {
                        icon = spellTemplate.getDisabledIcon().getItemStack(1);
                    }
                    if (icon == null && spellTemplate.getIcon() != null) {
                        icon = spellTemplate.getIcon().getItemStack(1);
                    }
                    if (icon == null && unavailable && spellTemplate.getDisabledIconURL() != null) {
                        icon = controller.getURLSkull(spellTemplate.getDisabledIconURL());
                    }
                    if (icon == null && spellTemplate.getIconURL() != null) {
                        icon = controller.getURLSkull(spellTemplate.getIconURL());
                    }
                }
            }

            if (icon == null && attribute != null) {
                String iconKey = attribute.getIconKey();
                if (iconKey != null && !iconKey.isEmpty()) {
                    ItemData iconData = controller.getOrCreateItem(iconKey);
                    if (iconData != null) {
                        int amount = 1;
                        if (attributeAmount == 0) {
                            CasterProperties caster = getCaster(context);
                            Double attributeAmount = caster.getAttribute(attributeKey);
                            if (attributeAmount != null) {
                                amount = (int)Math.floor(attributeAmount);
                            }
                        }
                        icon = iconData.getItemStack(Math.max(1, amount));
                    }
                }
            }

            if (icon == null && defaults.icon != null) {
                this.icon = InventoryUtils.getCopy(defaults.icon);
            }
            ItemMeta meta = icon == null ? null : icon.getItemMeta();
            if (icon == null || meta == null) {
                // Show a question mark if nothing else worked
                this.icon = controller.getURLSkull("http://textures.minecraft.net/texture/1adaf6e6e387bc18567671bb82e948488bbacff97763ee5985442814989f5d");
                meta = icon.getItemMeta();
                if (meta == null) {
                    this.icon = new ItemStack(com.elmakers.mine.bukkit.wand.Wand.DefaultUpgradeMaterial);
                    meta = this.icon.getItemMeta();
                }
            }

            // Prepare icon
            meta.setDisplayName(name);
            if (!lore.isEmpty()) {
                List<String> itemLore = meta.getLore();
                if (itemLore == null) {
                    itemLore = new ArrayList<>();
                }
                for (String line : lore) {
                    itemLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(itemLore);
            }
            icon.setItemMeta(meta);
            icon = InventoryUtils.makeReal(icon);

            InventoryUtils.makeUnbreakable(icon);
            InventoryUtils.hideFlags(icon, (byte)63);

            if (unavailable) {
                if (unavailableMessage != null && !unavailableMessage.isEmpty()) {
                    InventoryUtils.setMeta(icon, "unpurchasable", unavailableMessage);
                } else {
                    // We're not going to show unavailable items without a reason.
                    showUnavailable = false;
                }
            }

            if (showConfirmation) {
                InventoryUtils.setMeta(icon, "confirm", "true");
            }
        }

        @Nullable
        protected String getCastSpell(Wand wand) {
            if (alternateSpellTags != null && wand != null) {
                for (String key : alternateSpellTags.keySet()) {
                    if (wand.hasTag(key)) {
                        return alternateSpellTags.get(key);
                    }
                }
            }
            return castSpell;
        }

        protected boolean hasAltTags(Wand wand) {
            if (alternateSpellTags == null || wand == null) return false;
            for (String key : alternateSpellTags.keySet()) {
                if (wand.hasTag(key)) return true;
            }
            return false;
        }

        @Nullable
        protected Cost takeCosts(CostReducer reducer, CastContext context) {
            Cost required = getRequiredCost(reducer, context);
            if (required != null) {
                return required;
            }
            if (costs != null) {
                for (Cost cost : costs) {
                    cost.deduct(context.getMage(), context.getWand(), reducer);
                }
            }

            return null;
        }

        @Nullable
        public Cost getRequiredCost(CostReducer reducer, CastContext context) {
            if (costs != null) {
                for (Cost cost : costs) {
                    if (!cost.has(context.getMage(), context.getWand(), reducer)) {
                        return cost;
                    }
                }
            }

            return null;
        }

        public SpellResult give(CostReducer reducer, CastContext context) {
            Mage mage = context.getMage();
            Wand wand = context.getWand();

            if (placeholder) {
                return SpellResult.NO_ACTION;
            }

            if (unlockClass != null && !unlockClass.isEmpty()) {
                if (mage.hasClassUnlocked(unlockClass)) {
                    String hasClassMessage = getMessage("has_class").replace("$class", name);
                    context.showMessage(hasClassMessage);
                    return SpellResult.NO_TARGET;
                }
                MageClass activeClass = mage.getActiveClass();
                if (switchClass && activeClass != null) {
                    mage.lockClass(activeClass.getKey());
                }
                mage.unlockClass(unlockClass);
                if (switchClass) {
                    mage.setActiveClass(unlockClass);

                    // This is here to force reload any changes made to wands
                    // If this becomes an issue, maybe make it optional
                    wand = actionContext.checkWand();
                }
            }

            CasterProperties caster = getCaster(context);
            if (applyToWand && caster == null) {
                context.showMessage("no_wand", getDefaultMessage(context, "no_wand"));
                return SpellResult.NO_TARGET;
            }

            if (caster != null && items != null) {
                boolean anyApplied = false;
                for (ItemStack item : items) {
                    anyApplied = caster.addItem(item) || anyApplied;
                }
                if (!anyApplied) {
                    String inapplicable = getMessage("not_applicable").replace("$item", name);
                    context.showMessage(inapplicable);
                    return SpellResult.NO_TARGET;
                }
            }

            if (caster != null && attributeKey != null && !attributeKey.isEmpty()) {
                MagicAttribute attributeDefinition = context.getController().getAttribute(attributeKey);
                Double amount = caster.getAttribute(attributeKey);
                if (amount != null && attributeDefinition != null) {
                    double newValue = amount + attributeAmount;
                    if (!attributeDefinition.inRange(newValue)) {
                        return SpellResult.NO_TARGET;
                    }
                    caster.setAttribute(attributeKey, newValue);
                } else {
                    context.getLogger().warning("Invalid attribute: " + attributeKey);
                }
            }

            MageController controller = context.getController();
            String castSpell = getCastSpell(context.getWand());
            if (castSpell != null && !castSpell.isEmpty()) {
                Spell spell = null;
                spell = mage.getSpell(castSpell);

                // Close before casting, to support sub-menus
                if (autoClose) {
                    mage.deactivateGUI();
                }

                if (spell == null || !spell.cast()) {
                    context.showMessage("cast_fail", getDefaultMessage(context, "cast_fail"));
                    return SpellResult.NO_TARGET;
                }
            }

            if (unlockKey != null && !unlockKey.isEmpty()) {
                ConfigurationSection unlocks = mage.getData().getConfigurationSection(unlockSection);
                if (unlocks != null && !unlocks.getBoolean(unlockKey)) {
                    String unlockMessage = getMessage("unlocked");
                    context.showMessage(getCostsMessage(reducer, unlockMessage));
                }
                if (unlocks == null) {
                    unlocks = mage.getData().createSection(unlockSection);
                }
                unlocks.set(unlockKey, true);
            }

            if (items != null && caster == null) {
                for (ItemStack item : items) {
                    ItemStack copy = InventoryUtils.getCopy(item);
                    mage.giveItem(copy, putInHand);
                }
            }

            if (commands != null && !commands.isEmpty()) {
                for (String command : commands) {
                    String execute = context.parameterize(command);
                    controller.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), execute);
                }
            }

            if (earns != null) {
                boolean givenAny = false;
                for (Cost cost : earns) {
                    givenAny = cost.give(mage, wand) || givenAny;
                }
                if (!givenAny) {
                    return SpellResult.NO_TARGET;
                }
            }

            if (actions != null) {
                startActions(actions);
            }

            Cost required = takeCosts(reducer, context);
            if (required != null) {
                String baseMessage = getMessage("insufficient");
                String costDescription = required.getFullDescription(controller.getMessages(), reducer);
                costDescription = baseMessage.replace("$cost", costDescription);
                context.showMessage(costDescription);
                return SpellResult.INSUFFICIENT_RESOURCES;
            }

            if (!effects.isEmpty()) {
                context.playEffects(effects);
            }

            return SpellResult.CAST;
        }

        @Nullable
        public CasterProperties getCaster(CastContext context) {
            Mage mage = context.getMage();
            Wand wand = context.getWand();
            CasterProperties caster = null;
            if (applyTo != null) {
                MagicProperties properties = mage.getProperties().getStorage(applyTo);
                if (properties instanceof CasterProperties) {
                    caster = (CasterProperties)properties;
                }
            } else if (applyToClass != null && !applyToClass.isEmpty()) {
                caster = mage.getClass(applyToClass);
            } else if (applyToWand) {
                caster = wand;
            } else if (applyToCaster) {
                caster = mage.getActiveProperties();
            }
            return caster;
        }

        public Integer getSlot() {
            return slot;
        }

        public boolean isPlaceholder() {
            return placeholder;
        }

        public boolean isUnavailable() {
            return unavailable;
        }

        @Nullable
        public ItemStack getIcon() {
            return icon;
        }

        public String getName() {
            return name;
        }

        public String getSelectedMessage(CostReducer reducer) {
            String message = selectedMessage;
            if (costs == null) {
                if (selectedFreeMessage != null) {
                    message = selectedFreeMessage;
                } else if (message == null) {
                    message = getMessage("selected_free");
                }
            } else if (message == null) {
                message = getMessage("selected");
            }
            return getCostsMessage(reducer, message);
        }

        public String getCostsMessage(CostReducer reducer, String baseMessage) {
            String costString = "";

            if (costs != null) {
                for (Cost cost : costs) {
                    if (!costString.isEmpty()) {
                        costString += ", ";
                    }

                    costString += cost.getFullDescription(context.getController().getMessages(), reducer);
                }
            }

            if (costString.isEmpty()) {
                costString = getMessage("nothing");
            }

            String earnString = "";
            if (earns != null) {
                for (Cost earn : earns) {
                    if (!earnString.isEmpty()) {
                        earnString += ", ";
                    }

                    earnString += earn.getFullDescription(context.getController().getMessages());
                }
            }

            if (earnString.isEmpty()) {
                earnString = getMessage("nothing");
            }
            return baseMessage.replace("$item", name)
                .replace("$name", name)
                .replace("$cost", costString)
                .replace("$earn", earnString);
        }
    }

    @Override
    public void deactivated() {
        // Check for shop items glitched into the player's inventory
        if (context != null) {
            context.getMage().removeItemsWithTag("slot");
        }
        isActive = false;
    }

    @Override
    public void dragged(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    protected String getMessage(String key) {
        return context.getMessage(key, getDefaultMessage(context, key));
    }

    protected String getDefaultMessage(CastContext context, String key) {
        return context.getController().getMessages().get("shops." + key);
    }

    @Override
    public void clicked(InventoryClickEvent event)
    {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        Mage mage = context.getMage();
        if (item == null || !InventoryUtils.hasMeta(item, "slot")) {
            if (!autoClose) {
                mage.deactivateGUI();
            }
            return;
        }

        int slotIndex = Integer.parseInt(InventoryUtils.getMetaString(item, "slot"));
        MageController controller = context.getController();

        SelectorOption option = showingItems.get(slotIndex);
        if (option == null || option.isPlaceholder()) {
            return;
        }

        String unpurchasableMessage = InventoryUtils.getMetaString(item, "unpurchasable");
        if (unpurchasableMessage != null && !unpurchasableMessage.isEmpty()) {
            context.showMessage(unpurchasableMessage);
            mage.deactivateGUI();
            return;
        }

        Cost required = option.getRequiredCost(this, context);
        if (required != null) {
            String baseMessage = getMessage("insufficient");
            String costDescription = required.getFullDescription(controller.getMessages(), this);
            costDescription = baseMessage.replace("$cost", costDescription);
            context.showMessage(costDescription);
        } else {
            String itemName = option.getName();
            if (InventoryUtils.hasMeta(item, "confirm")) {
                String inventoryTitle = getConfirmTitle(option).replace("$item", itemName);
                Inventory confirmInventory = CompatibilityUtils.createInventory(null, 9, inventoryTitle);
                InventoryUtils.removeMeta(item, "confirm");
                for (int i = 0; i < 9; i++)
                {
                    if (i != 4) {
                        ItemStack filler = confirmFillMaterial.getItemStack(1);
                        ItemMeta meta = filler.getItemMeta();
                        if (meta != null)
                        {
                            meta.setDisplayName(ChatColor.DARK_GRAY + (i < 4 ? "-->" : "<--"));
                            filler.setItemMeta(meta);
                        }
                        confirmInventory.setItem(i, filler);
                    } else {
                        confirmInventory.setItem(i, item);
                    }
                }
                mage.deactivateGUI();
                isActive = true;
                mage.activateGUI(this, confirmInventory);
                return;
            }

            finalResult = option.give(this, context);
            if (finalResult.isSuccess() && finalResult != SpellResult.NO_TARGET) {
                context.showMessage(option.getSelectedMessage(this));
            }
        }
        if (autoClose || finalResult != SpellResult.CAST) {
            if (isActive) {
                mage.deactivateGUI();
            }
        } else {
            // update title
            mage.continueGUI(this, getInventory(context));
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        this.context = context;

        defaultConfiguration = new SelectorConfiguration(parameters);
        confirmFillMaterial = ConfigurationUtils.getMaterialAndData(parameters, "confirm_filler", new MaterialAndData(Material.AIR));
        autoClose = parameters.getBoolean("auto_close", true);
        costScale = parameters.getDouble("scale", 1);
        earnScale = parameters.getDouble("earn_scale", costScale);
        title = parameters.getString("title");
        confirmTitle = parameters.getString("confirm_title");
        confirmUnlockTitle = parameters.getString("unlock_confirm_title");
        finalResult = null;
        isActive = false;

        numSlots = 0;
        showingItems = new HashMap<>();
        has = 0;
        Collection<ConfigurationSection> optionConfigs = ConfigurationUtils.getNodeList(parameters, "options");
        if (optionConfigs != null) {
            loadOptions(optionConfigs);
        }

        // Have to do this after adding options since options may register action handlers
        super.prepare(context, parameters);
    }

    protected void loadOptions(Collection<ConfigurationSection> optionConfigs) {
        // Gather list of selector options first, to compute limits
        List<SelectorOption> options = new ArrayList<>();

        for (ConfigurationSection option : optionConfigs) {
            SelectorOption newOption = new SelectorOption(defaultConfiguration, option, context, this);
            if (newOption.hasLimit() && newOption.has(context)) {
                has++;
            }
            options.add(newOption);
        }
        for (SelectorOption option : options) {
            if (option.isUnavailable() && !option.showIfUnavailable()) {
                continue;
            }

            Integer targetSlot = option.getSlot();
            int slot = targetSlot == null ? numSlots : targetSlot;
            if (slot >= MAX_INVENTORY_SLOTS) continue;
            if (!option.isPlaceholder()) itemCount++;
            showingItems.put(slot, option);
            numSlots = Math.max(slot + 1, numSlots);
        }
    }

    public SpellResult showItems(CastContext context) {
        Mage mage = context.getMage();
        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }

        isActive = true;
        finalResult = SpellResult.NO_ACTION;
        Inventory displayInventory = getInventory(context);
        mage.activateGUI(this, displayInventory);

        return SpellResult.CAST;
    }

    protected String getInventoryTitle()
    {
        if (title != null && !title.isEmpty()) {
            return title;
        }
        return getMessage("title");
    }

    protected String getConfirmTitle(SelectorOption option)
    {
        if (option.isUnlock()) {
            if (confirmUnlockTitle != null && !confirmUnlockTitle.isEmpty()) {
                return confirmUnlockTitle;
            }
            return getMessage("unlock_confirm_title");
        }
        if (confirmTitle != null && !confirmTitle.isEmpty()) {
            return confirmTitle;
        }
        return getMessage("confirm_title");
    }

    protected String getBalanceDescription(CastContext context) {
        Mage mage = context.getMage();
        if (defaultConfiguration.free) {
            return "";
        }
        String costType = defaultConfiguration.getCostType();
        com.elmakers.mine.bukkit.item.Cost cost = new com.elmakers.mine.bukkit.item.Cost(context.getController(), costType, 1);
        cost.checkSupported(context.getController(), defaultConfiguration.getCostTypeFallback());
        cost.setAmount(cost.getBalance(mage, context.getWand()));
        return cost.getFullDescription(context.getController().getMessages());
    }

    protected Inventory getInventory(CastContext context)
    {
        String inventoryTitle = getInventoryTitle();
        String balanceDescription = getBalanceDescription(context);
        inventoryTitle = inventoryTitle.replace("$balance", balanceDescription);

        ProgressionPath path = context.getMage().getActiveProperties().getPath();
        String pathName = (path == null ? null : path.getName());
        if (pathName == null) {
            pathName = "";
        }
        inventoryTitle = inventoryTitle.replace("$path", pathName);


        int invSize = (int)Math.ceil(numSlots / 9.0f) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        for (Map.Entry<Integer, SelectorOption> entry : showingItems.entrySet()) {
            ItemStack icon = entry.getValue().getIcon();
            InventoryUtils.setMeta(icon, "slot", Integer.toString(entry.getKey()));
            displayInventory.setItem(entry.getKey(), icon);
        }

        return displayInventory;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("confirm");
        parameters.add("path");
        parameters.add("path_end");
        parameters.add("path_exact");
        parameters.add("auto_upgrade");
        parameters.add("require_wand");
        parameters.add("permission");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        MageController controller = spell.getController();
        if (parameterKey.equals("path") || parameterKey.equals("path_exact") || parameterKey.equals("path_end")) {
            examples.addAll(controller.getWandPathKeys());
        } else if (parameterKey.equals("require_wand") || parameterKey.equals("confirm") || parameterKey.equals("auto_upgrade")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public void finish(CastContext context) {
        isActive = false;
        finalResult = null;
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        isActive = false;
        finalResult = null;
    }

    public RequirementsResult checkDefaultRequirements(CastContext context) {
        return defaultConfiguration.checkRequirements(context);
    }

    @Override
    public SpellResult start(CastContext context) {
        RequirementsResult check = checkDefaultRequirements(context);
        if (!check.result.isSuccess()) {
            context.sendMessage(check.message);
            return check.result;
        }

        if (itemCount == 0) {
            context.showMessage("no_items", getDefaultMessage(context, "no_items"));
            return SpellResult.NO_ACTION;
        }
        return showItems(context);
    }

    @Override
    public SpellResult step(CastContext context) {
        if (isActive) {
            return SpellResult.PENDING;
        }

        return finalResult == null ? SpellResult.NO_ACTION : finalResult;
    }

    @Nullable
    protected ItemStack parseItem(String itemKey) {
        if (itemKey == null || itemKey.isEmpty() || itemKey.equalsIgnoreCase("none"))
        {
            return null;
        }

        ItemStack item = context.getController().createItem(itemKey);
        if (item == null) {
           context.getLogger().warning("Failed to create item in selector: " + itemKey);
        }
        return item;
    }

    @Override
    public float getCostReduction() {
        return 0;
    }

    @Override
    public float getConsumeReduction() {
        return 0;
    }

    @Override
    public float getCostScale() {
        return (float)costScale;
    }

    protected int getNumSlots() {
        return numSlots;
    }

    @Override
    protected void addHandlers(Spell spell, ConfigurationSection parameters) {
        // We've done something weird with the Selector action, making "actions" not an action handler
        // nor a reference to one, but a default value for options to use for referencing an action
        // handler.
        // This means it's important we don't remove it from the configuration, and so should not
        // try to process it like a default action handler.
        // This is why addHandlers is overridden and does not call the super method.
    }
}
