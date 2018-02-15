package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageClassTemplate;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.item.Cost;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
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
    protected boolean showConfirmation = true;
    protected SelectorConfiguration defaultConfiguration;
    protected MaterialAndData confirmFillMaterial;
    protected CastContext context;
    private Map<Integer, SelectorOption> showingItems;
    private int numSlots;
    private int has = 0;
    private String title;
    private String confirmTitle;

    // State
    private boolean isActive = false;
    private SpellResult finalResult = null;

    protected class SelectorConfiguration {
        protected @Nonnull ItemStack icon;
        protected @Nullable List<ItemStack> items;
        protected @Nullable List<Cost> costs = null;
        protected @Nullable String permissionNode = null;
        protected @Nullable String requiredPath = null;
        protected @Nullable String requiredTemplate = null;
        protected @Nullable String requiresCompletedPath = null;
        protected @Nullable String exactPath = null;
        protected @Nullable String castSpell = null;
        protected @Nullable String unlockClass = null;
        protected @Nullable String selectedMessage = null;
        protected boolean requireWand = false;
        protected boolean applyToWand = false;
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
            permissionNode = configuration.getString("permission", permissionNode);
            requiredPath = configuration.getString("path", exactPath);
            exactPath = configuration.getString("path_exact", exactPath);
            requiresCompletedPath = configuration.getString("path_end", requiresCompletedPath);
            requiredTemplate = configuration.getString("require_template", requiredTemplate);
            requireWand = configuration.getBoolean("require_wand", requireWand);
            applyToWand = configuration.getBoolean("apply_to_wand", requireWand);
            castSpell = configuration.getString("cast_spell", castSpell);
            unlockClass = configuration.getString("unlock_class", unlockClass);
            currency = configuration.getInt("currency", currency);
            experience = configuration.getInt("experience", experience);
            sp = configuration.getInt("sp", sp);
            limit = configuration.getInt("limit", limit);
            selectedMessage = configuration.getString("selected_message", selectedMessage);
            if (selectedMessage == null) {
               selectedMessage = context.getMessage("deducted", getDefaultMessage(context, "deducted"));
            }
            if (configuration.contains("item")) {
                items = new ArrayList<>();
                ItemStack item = parseItem(configuration.getString("item"));
                items.add(item);
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
            if (icon == null && items != null) {
                icon = InventoryUtils.getCopy(items.get(0));
            }
            if (icon == null && castSpell != null && !castSpell.isEmpty()) {
                SpellTemplate spellTemplate = context.getController().getSpellTemplate(castSpell);
                if (spellTemplate != null && spellTemplate.getIcon() != null) {
                    icon = spellTemplate.getIcon().getItemStack(1);
                }
            }
            costs = parseCosts(configuration.getConfigurationSection("costs"));
            int currencyCost = configuration.getInt("cost");
            if (currencyCost > 0) {
                if (costs == null) {
                    costs = new ArrayList<>();
                }
                costs.add(new Cost(context.getController(), "currency", currencyCost));
            }

            if (requiresCompletedPath != null) {
                requiredPath = requiresCompletedPath;
                exactPath = requiresCompletedPath;
            }
            if (requiredPath != null || exactPath != null || requiredTemplate != null) {
                requireWand = true;
            }
        }

        protected List<Cost> parseCosts(ConfigurationSection node) {
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

        public boolean hasLimit() {
            return limit > 0;
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

        public SpellResult checkContext(CastContext context, boolean quiet) {
            Mage mage = context.getMage();
            MageController controller = mage.getController();
            Player player = mage.getPlayer();
            if (player == null) {
                return SpellResult.PLAYER_REQUIRED;
            }
            if (permissionNode != null && !player.hasPermission(permissionNode)) {
                return SpellResult.INSUFFICIENT_PERMISSION;
            }
            Wand wand = mage.getActiveWand();
            if (wand == null && requireWand) {
                if (!quiet) context.showMessage("no_wand", getDefaultMessage(context,"no_wand"));
                return SpellResult.FAIL;
            }

            if (requiredTemplate != null) {
                String template = wand.getTemplateKey();
                if (template == null || !template.equals(requiredTemplate)) {
                    if (!quiet) context.showMessage(context.getMessage("no_template", getDefaultMessage(context, "no_template")).replace("$wand", wand.getName()));
                    return SpellResult.FAIL;
                }
            }

            CasterProperties checkProperties = null;
            MageClass activeClass = mage.getActiveClass();
            ProgressionPath path = null;
            if (wand !=  null) {
                path = wand.getPath();
                checkProperties = wand;
            } else if (activeClass != null) {
                path = activeClass.getPath();
                checkProperties = activeClass;
            }

            if (unlockClass != null && !unlockClass.isEmpty()) {
                if (mage.hasClassUnlocked(unlockClass)) {
                    String hasClassMessage = context.getMessage("has_class", getDefaultMessage(context, "has_class")).replace("$class", unlockClass);
                    if (!quiet) context.showMessage(hasClassMessage);
                    return SpellResult.NO_TARGET;
                }
            }

            // Check path requirements
            if (requiredPath != null || exactPath != null) {
                if (path == null) {
                    if (!quiet) context.showMessage(context.getMessage("no_path", getDefaultMessage(context, "no_path")));
                    return SpellResult.FAIL;
                }

                if (requiredPath != null && !path.hasPath(requiredPath)) {
                    WandUpgradePath requiresPath = controller.getPath(requiredPath);
                    if (!quiet)  {
                        if (requiresPath != null) {
                            context.showMessage(context.getMessage("no_required_path", getDefaultMessage(context, "no_required_path")).replace("$path", requiresPath.getName()));
                        } else {
                            context.getLogger().warning("Invalid path specified in Shop action: " + requiredPath);
                        }
                    }
                    return SpellResult.FAIL;
                }
                if (exactPath != null && !exactPath.equals(path.getKey())) {
                    WandUpgradePath requiresPath = controller.getPath(exactPath);
                    if (!quiet)  {
                        if (requiresPath != null) {
                            context.showMessage(context.getMessage("no_path_exact", getDefaultMessage(context, "no_path_exact")).replace("$path", requiresPath.getName()));
                        } else {
                            context.getLogger().warning("Invalid path specified in Shop action: " + exactPath);
                        }
                    }
                    return SpellResult.FAIL;
                }
                if (requiresCompletedPath != null) {
                    if (path.canProgress(checkProperties)) {
                        if (!quiet) context.showMessage(context.getMessage("no_path_end", getDefaultMessage(context, "no_path_end")).replace("$path", path.getName()));
                        return SpellResult.FAIL;
                    }
                }
            }

            if (limit > 0 && has >= limit) {
                if (!quiet) {
                    String limitMessage = context.getMessage("at_limit", getDefaultMessage(context, "at_limit")).replace("$limit", Integer.toString(limit));
                    context.showMessage(limitMessage);
                }
                return SpellResult.NO_TARGET;
            }

            return SpellResult.CAST;
        }
    }

    protected class SelectorOption extends SelectorConfiguration {
        protected Integer slot = null;
        protected String name = null;
        protected List<String> lore = null;

        public SelectorOption(SelectorConfiguration defaults, ConfigurationSection configuration, CostReducer reducer) {
            super();

            this.selectedMessage = defaults.selectedMessage;
            this.items = defaults.items;
            this.permissionNode = defaults.permissionNode;
            this.costs = defaults.costs;
            this.requiredPath = defaults.requiredPath;
            this.requiredTemplate = defaults.requiredTemplate;
            this.requiresCompletedPath = defaults.requiresCompletedPath;
            this.exactPath = defaults.exactPath;
            this.castSpell = defaults.castSpell;
            this.requireWand = defaults.requireWand;
            this.applyToWand = defaults.applyToWand;
            this.unlockClass = defaults.unlockClass;
            this.currency = defaults.currency;
            this.sp = defaults.sp;
            this.experience = defaults.experience;
            this.limit = defaults.limit;
            this.lore = configuration.contains("lore") ? configuration.getStringList("lore") : null;

            parse(configuration);

            if (icon == null && defaults.icon != null) {
                this.icon = InventoryUtils.getCopy(defaults.icon);
            }
            if (icon == null) {
                // Show a question mark
                this.icon = InventoryUtils.getURLSkull("http://textures.minecraft.net/texture/1adaf6e6e387bc18567671bb82e948488bbacff97763ee5985442814989f5d");
            }

            if (configuration.contains("slot")) {
                slot = configuration.getInt("slot");
            }

            MageController controller = context.getController();
            name = configuration.getString("name", "");
            if (name.isEmpty() && unlockClass != null && !unlockClass.isEmpty()) {
                MageClassTemplate mageClass = controller.getMageClassTemplate(unlockClass);
                name = context.getMessage("unlock_class", getDefaultMessage(context, "unlock_class"));
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
                name = context.getMessage("cast_spell", getDefaultMessage(context, "cast_spell"));
                if (spell != null) {
                    name = name.replace("$spell", spell.getName());
                } else {
                    controller.getLogger().warning("Unknown spell in selector config: " + castSpell);
                }
            }

            if (lore == null) {
                if (unlockClass != null && !unlockClass.isEmpty()) {
                    MageClassTemplate mageClass = controller.getMageClassTemplate(unlockClass);
                    String description = mageClass.getDescription();
                    if (description != null && !description.isEmpty()) {
                        lore = new ArrayList<>();
                        lore.add(description);
                    }
                } else if (castSpell != null && !castSpell.isEmpty()) {
                    SpellTemplate spell = controller.getSpellTemplate(castSpell);
                    String description = spell.getDescription();
                    if (description != null && !description.isEmpty()) {
                        lore = new ArrayList<>();
                        lore.add(description);
                    }
                }
            }

            if (costs != null) {
                String costString = context.getMessage("cost_lore", getDefaultMessage(context, "cost_lore"));
                if (lore == null) {
                    lore = new ArrayList<>();
                }
                for (Cost cost : costs) {
                    lore.add(costString.replace("$cost", cost.getFullDescription(context.getController().getMessages(), reducer)));
                }
            }

            String description = configuration.getString("description");
            if (description != null && !description.isEmpty()) {
                if (lore == null) {
                    lore = new ArrayList<>();
                }
                lore.add(0, description);
            }

            // Prepare icon
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (lore != null) {
                for (int i = 0; i < lore.size(); i++) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                }
                meta.setLore(lore);
            }
            icon.setItemMeta(meta);
            icon = InventoryUtils.makeReal(icon);

            InventoryUtils.makeUnbreakable(icon);
            InventoryUtils.hideFlags(icon, (byte)63);
        }

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

            if (unlockClass != null && !unlockClass.isEmpty()) {
                if (mage.hasClassUnlocked(unlockClass)) {
                    String hasClassMessage = context.getMessage("has_class", getDefaultMessage(context, "has_class")).replace("$class", name);
                    context.showMessage(hasClassMessage);
                    return SpellResult.NO_TARGET;
                }
                mage.unlockClass(unlockClass);
            }

            if (requireWand) {
                if (wand == null) {
                    context.showMessage("no_wand", getDefaultMessage(context, "no_wand"));
                    return SpellResult.NO_TARGET;
                }
                if (applyToWand && items != null) {
                    boolean anyApplied = false;
                    for (ItemStack item : items) {
                        anyApplied = wand.addItem(item) || anyApplied;
                    }
                    if (!anyApplied) {
                        String inapplicable = context.getMessage("not_applicable", getDefaultMessage(context, "not_applicable")).replace("$item", name);
                        context.showMessage(inapplicable);
                        return SpellResult.NO_TARGET;
                    }
                }
            }

            MageController controller = context.getController();
            if (castSpell != null && !castSpell.isEmpty()) {
                Spell spell = null;
                spell = mage.getSpell(castSpell);
                // TODO: Arg support? Target clicker?
                if (spell == null || !spell.cast()) {
                    context.showMessage("cast_fail", getDefaultMessage(context, "cast_fail"));
                    return SpellResult.NO_TARGET;
                }
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

            if (experience != 0) {
                mage.giveExperience(experience);
            }

            if (currency != 0) {
                VaultController.getInstance().depositPlayer(mage.getPlayer(), currency);
            }

            Cost required = takeCosts(reducer, context);
            if (required != null) {
                String baseMessage = context.getMessage("insufficient", getDefaultMessage(context, "insufficient"));
                String costDescription = required.getFullDescription(controller.getMessages(), mage);
                costDescription = baseMessage.replace("$cost", costDescription);
                context.showMessage(costDescription);
                return SpellResult.INSUFFICIENT_RESOURCES;
            }

            return SpellResult.CAST;
        }

        public Integer getSlot() {
            return slot;
        }

        public ItemStack getIcon() {
            return icon;
        }

        public String getName() {
            return name;
        }

        public String getSelectedMessage(CostReducer reducer) {
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
                costString = context.getMessage("shops.nothing");
            }
            return selectedMessage.replace("$name", name).replace("$cost", costString);
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

    protected String getDefaultMessage(CastContext context, String key) {
        return context.getController().getMessages().get("shops." + key);
    }

    protected String getDefaultMessage(CastContext context, String key, String defaultValue) {
        return context.getController().getMessages().get("shops." + key, defaultValue);
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
        if (option == null) {
            return;
        }

        Cost required = option.getRequiredCost(this, context);
        if (required != null) {
            String baseMessage = context.getMessage("insufficient", getDefaultMessage(context, "insufficient"));
            String costDescription = required.getFullDescription(controller.getMessages(), mage);
            costDescription = baseMessage.replace("$cost", costDescription);
            context.showMessage(costDescription);
        } else {
            String itemName = option.getName();
            if (InventoryUtils.hasMeta(item, "confirm")) {
                String inventoryTitle = context.getMessage("confirm_title", getDefaultMessage(context, "confirm_title", confirmTitle)).replace("$item", itemName);
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
            if (finalResult.isSuccess()) {
                context.showMessage(option.getSelectedMessage(this));
            }
        }
        if (autoClose || finalResult != SpellResult.CAST) {
            mage.deactivateGUI();
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
        showConfirmation = parameters.getBoolean("confirm", true);
        confirmFillMaterial = ConfigurationUtils.getMaterialAndData(parameters, "confirm_filler", new MaterialAndData(Material.AIR));
        autoClose = parameters.getBoolean("auto_close", true);
        costScale = parameters.getDouble("scale", 1);
        title = parameters.getString("title");
        confirmTitle = parameters.getString("confirm_title");
        if (!autoClose) {
            showConfirmation = false;
        }
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
                SelectorOption newOption = new SelectorOption(defaultConfiguration, option, this);
                if (newOption.hasLimit() && newOption.has(context)) {
                    has++;
                }
                options.add(newOption);
            }
            for (SelectorOption option : options) {
                if (!option.checkContext(context, true).isSuccess()) {
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

    protected String getInventoryTitle(CastContext context)
    {
        return context.getMessage("title", getDefaultMessage(context, "title", title));
    }

    protected String getBalanceDescription(CastContext context) {
        Mage mage = context.getMage();
        double balance = VaultController.getInstance().getBalance(mage.getPlayer());
        return VaultController.getInstance().format(balance);
    }

    protected Inventory getInventory(CastContext context)
    {
        String inventoryTitle = getInventoryTitle(context);
        String balanceDescription = getBalanceDescription(context);
        inventoryTitle = inventoryTitle.replace("$balance", balanceDescription);

        int invSize = (int)Math.ceil(numSlots / 9.0f) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        for (Map.Entry<Integer, SelectorOption> entry : showingItems.entrySet()) {
            ItemStack icon = entry.getValue().getIcon();
            InventoryUtils.setMeta(icon, "slot", Integer.toString(entry.getKey()));
            if (showConfirmation) {
                InventoryUtils.setMeta(icon, "confirm", "true");
            }
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

    public SpellResult checkContext(CastContext context) {
        return defaultConfiguration.checkContext(context, false);
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (isActive) {
            return SpellResult.PENDING;
        }
        if (finalResult != null) {
            return finalResult;
        }
        SpellResult contextResult = checkContext(context);
        if (!contextResult.isSuccess()) {
            return contextResult;
        }

        if (showingItems.isEmpty()) {
            context.showMessage("no_items", getDefaultMessage(context, "no_items"));
            return SpellResult.NO_ACTION;
        }
        return showItems(context);
    }

    protected ItemStack parseItem(String itemKey) {
        if (itemKey == null || itemKey.isEmpty() || itemKey.equalsIgnoreCase("none"))
        {
            return null;
        }

        return context.getController().createItem(itemKey);
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
