package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClassTemplate;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.item.Cost;
import de.slikey.effectlib.math.EquationStore;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import de.slikey.effectlib.math.EquationTransform;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectorAction extends BaseSpellAction implements GUIAction, CostReducer
{
    protected double costScale = 1;
    protected boolean autoClose = true;
    protected SelectorConfiguration defaultConfiguration;
    protected MaterialAndData confirmFillMaterial;
    protected CastContext context;
    private Map<Integer, SelectorOption> showingItems;
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
    }

    protected enum ModifierType { WAND, MAGE, CLASS, ATTRIBUTE };

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
        protected @Nullable String castSpell = null;
        protected @Nullable String unlockClass = null;
        protected @Nullable String selectedMessage = null;
        protected @Nullable String unlockKey = null;
        protected @Nonnull String unlockSection = "unlocked";
        protected @Nullable Collection<Requirement> requirements;
        protected @Nullable List<String> commands;
        protected @Nullable List<CostModifier> costModifiers;
        protected boolean applyToWand = false;
        protected boolean showConfirmation = false;
        protected boolean showUnavailable = false;
        protected int experience;
        protected int sp;
        protected int currency = 0;
        protected int limit = 0;

        public SelectorConfiguration(ConfigurationSection configuration) {
            parse(configuration);
        }

        protected SelectorConfiguration() {
        }

        protected void parse(ConfigurationSection configuration) {
            applyToWand = configuration.getBoolean("apply_to_wand", applyToWand);
            castSpell = configuration.getString("cast_spell", castSpell);
            unlockClass = configuration.getString("unlock_class", unlockClass);
            currency = configuration.getInt("currency", currency);
            experience = configuration.getInt("experience", experience);
            sp = configuration.getInt("sp", sp);
            limit = configuration.getInt("limit", limit);
            unlockKey = configuration.getString("unlock", unlockKey);
            unlockSection = configuration.getString("unlock_section", unlockSection);
            showConfirmation = configuration.getBoolean("confirm", showConfirmation);
            costType = configuration.getString("cost_type", costType);
            showUnavailable = configuration.getBoolean("show_unavailable", showUnavailable);
            commands = ConfigurationUtils.getStringList(configuration, "commands");

            selectedMessage = configuration.getString("selected", selectedMessage);
            if (selectedMessage == null) {
               selectedMessage = getMessage("selected");
            }

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
            icon = parseItem(configuration.getString("icon"));
            costModifiers = parseCostModifiers(configuration, "cost_modifiers");
            costs = parseCosts(configuration.getConfigurationSection("costs"));
            int cost = configuration.getInt("cost");
            if (cost > 0) {
                if (costs == null) {
                    costs = new ArrayList<>();
                }
                costs.add(new Cost(context.getController(), costType, cost));
            }
        }

        @Nullable protected List<Cost> parseCosts(ConfigurationSection node) {
            if (node == null) {
                return null;
            }
            List<Cost> costs = new ArrayList<>();
            Collection<String> costKeys = node.getKeys(false);
            for (String key : costKeys) {
                costs.add(new Cost(context.getController(), key, node.getInt(key, 1)));
            }

            return costs;
        }

        @Nullable protected List<CostModifier> parseCostModifiers(ConfigurationSection configuration, String section) {
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
            this.items = defaults.items;
            this.costs = defaults.costs;
            this.castSpell = defaults.castSpell;
            this.applyToWand = defaults.applyToWand;
            this.unlockClass = defaults.unlockClass;
            this.currency = defaults.currency;
            this.sp = defaults.sp;
            this.experience = defaults.experience;
            this.limit = defaults.limit;
            this.unlockKey = defaults.unlockKey;
            this.unlockSection = defaults.unlockSection;
            this.showConfirmation = defaults.showConfirmation;
            this.costType = defaults.costType;
            this.showUnavailable = defaults.showUnavailable;
            this.commands = defaults.commands;
            this.lore = configuration.contains("lore") ? configuration.getStringList("lore") : new ArrayList<String>();

            placeholder = configuration.getBoolean("placeholder") || configuration.getString("item", "").equals("none");
            if (placeholder) {
                this.icon = new ItemStack(Material.AIR);
                return;
            }

            parse(configuration);

            if (defaults.requirements != null) {
                if (requirements == null ) {
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
            if (name.isEmpty() && items != null) {
                name = controller.describeItem(items.get(0));
            }
            if (name.isEmpty() && castSpell != null && !castSpell.isEmpty()) {
                SpellTemplate spell = controller.getSpellTemplate(castSpell);
                name = getMessage("cast_spell");
                if (spell != null) {
                    name = name.replace("$spell", spell.getName());
                } else {
                    controller.getLogger().warning("Unknown spell in selector config: " + castSpell);
                }
            }

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
                if (!check.result.isSuccess()) {
                    unavailable = true;
                    unavailableMessage = check.message;
                    if (unavailableMessage != null && !unavailableMessage.isEmpty()) {
                        InventoryUtils.wrapText(check.message, lore);
                    }
                }
            }

            // Don't show costs if unavailable
            if (costs != null && !unavailable) {
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
            }

            // Choose icon if none was set in config
            if (icon == null && items != null) {
                icon = InventoryUtils.getCopy(items.get(0));
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
                }
            }

            if (icon == null && defaults.icon != null) {
                this.icon = InventoryUtils.getCopy(defaults.icon);
            }
            if (icon == null) {
                // Show a question mark if nothing else worked
                this.icon = InventoryUtils.getURLSkull("http://textures.minecraft.net/texture/1adaf6e6e387bc18567671bb82e948488bbacff97763ee5985442814989f5d");
            }
            
            // Prepare icon
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
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

        @Nullable protected Cost takeCosts(CostReducer reducer, CastContext context) {
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

        @Nullable public Cost getRequiredCost(CostReducer reducer, CastContext context) {
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
                mage.unlockClass(unlockClass);
            }

            if (applyToWand && items != null) {
                if (wand == null) {
                    context.showMessage("no_wand", getDefaultMessage(context, "no_wand"));
                    return SpellResult.NO_TARGET;
                }
                boolean anyApplied = false;
                for (ItemStack item : items) {
                    anyApplied = wand.addItem(item) || anyApplied;
                }
                if (!anyApplied) {
                    String inapplicable = getMessage("not_applicable").replace("$item", name);
                    context.showMessage(inapplicable);
                    return SpellResult.NO_TARGET;
                }
            }

            MageController controller = context.getController();
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

            if (sp != 0) {
                if (mage.isAtMaxSkillPoints()) {
                    return SpellResult.NO_TARGET;
                }
                mage.addSkillPoints(sp);
            }

            if (items != null && !applyToWand) {
                for (ItemStack item : items) {
                    ItemStack copy = InventoryUtils.getCopy(item);
                    mage.giveItem(copy);
                }
            }
            
            if (commands != null && !commands.isEmpty()) {
                for (String command : commands) {
                    String execute = context.parameterize(command);
                    controller.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), execute);
                }
            }

            if (experience != 0) {
                mage.giveExperience(experience);
            }

            if (currency != 0) {
                VaultController.getInstance().depositPlayer(mage.getPlayer(), currency);
            }

            Cost required = takeCosts(reducer, context);
            if (required != null) {
                String baseMessage = getMessage("insufficient");
                String costDescription = required.getFullDescription(controller.getMessages(), reducer);
                costDescription = baseMessage.replace("$cost", costDescription);
                context.showMessage(costDescription);
                return SpellResult.INSUFFICIENT_RESOURCES;
            }

            return SpellResult.CAST;
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

        @Nullable public ItemStack getIcon() {
            return icon;
        }

        public String getName() {
            return name;
        }

        public String getSelectedMessage(CostReducer reducer) {
            return getCostsMessage(reducer, selectedMessage);
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
            return baseMessage.replace("$item", name).replace("$name", name).replace("$cost", costString);
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
        super.prepare(context, parameters);
        this.context = context;

        defaultConfiguration = new SelectorConfiguration(parameters);
        confirmFillMaterial = ConfigurationUtils.getMaterialAndData(parameters, "confirm_filler", new MaterialAndData(Material.AIR));
        autoClose = parameters.getBoolean("auto_close", true);
        costScale = parameters.getDouble("scale", 1);
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
                showingItems.put(slot, option);
                numSlots = Math.max(slot + 1, numSlots);
            }
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

        return SpellResult.PENDING;
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
        if (option.isUnlock() ) {
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
        Player player = mage.getPlayer();
        Messages messages = context.getController().getMessages();
        String description = "";
        switch (defaultConfiguration.getCostType()) {
            case "xp":
                String xpAmount = Integer.toString(mage.getExperience());
                description = messages.get("costs.xp_amount").replace("$amount", xpAmount);
                break;
            case "sp":
                String spAmount = Integer.toString(mage.getSkillPoints());
                description = messages.get("costs.sp_amount").replace("$amount", spAmount);
                break;
            case "levels":
                int levels = player == null ? 0 : player.getLevel();
                String levelAmount = Integer.toString(levels);
                description = messages.get("costs.levels_amount").replace("$amount", levelAmount);
                break;
            default:
                double balance = VaultController.getInstance().getBalance(mage.getPlayer());
                description = VaultController.getInstance().format(balance);
        }

        return description;
    }

    protected Inventory getInventory(CastContext context)
    {
        String inventoryTitle = getInventoryTitle();
        String balanceDescription = getBalanceDescription(context);
        inventoryTitle = inventoryTitle.replace("$balance", balanceDescription);

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
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public void finish(CastContext context) {
        isActive = false;
        finalResult = null;
    }

    public RequirementsResult checkDefaultRequirements(CastContext context) {
        return defaultConfiguration.checkRequirements(context);
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (isActive) {
            return SpellResult.PENDING;
        }
        if (finalResult != null) {
            return finalResult;
        }
        RequirementsResult check = checkDefaultRequirements(context);
        if (!check.result.isSuccess()) {
            context.sendMessage(check.message);
            return check.result;
        }

        if (showingItems.isEmpty()) {
            context.showMessage("no_items", getDefaultMessage(context, "no_items"));
            return SpellResult.NO_ACTION;
        }
        return showItems(context);
    }

    @Nullable protected ItemStack parseItem(String itemKey) {
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
}
