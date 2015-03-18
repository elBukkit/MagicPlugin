package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SpellShopAction extends BaseSpellAction implements GUIAction
{
    private String requiredPath = null;
    private String requiresCompletedPath = null;
    private String exactPath = null;
    private boolean showRequired = false;
    private boolean autoUpgrade = false;
    private boolean showFree = false;
    private boolean useXP = false;
    private CastContext context;
    private Wand wand;
    private Map<String, Double> spells = new HashMap<String, Double>();

    @Override
    public void initialize(ConfigurationSection parameters)
    {
        super.initialize(parameters);
        spells.clear();
        if (parameters.contains("spells"))
        {
            ConfigurationSection spellSection = parameters.getConfigurationSection("spells");
            Collection<String> spellKeys = spellSection.getKeys(false);
            for (String spellKey : spellKeys) {
                spells.put(spellKey, spellSection.getDouble(spellKey));
            }
        }
    }

    @Override
    public void deactivated() {

    }

    @Override
    public void clicked(InventoryClickEvent event)
    {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (context != null && wand != null && com.elmakers.mine.bukkit.wand.Wand.isSpell(item))
        {
            Mage mage = context.getMage();
            String spellKey = com.elmakers.mine.bukkit.wand.Wand.getSpell(item);
            SpellTemplate template = context.getController().getSpellTemplate(spellKey);
            boolean isXP = useXP || !VaultController.hasEconomy();
            Double worth = spells.get(spellKey);
            worth = worth == null ? template.getWorth() : worth;
            boolean hasCosts = false;
            if (worth > 0) {
                if (isXP) {
                    hasCosts = mage.getExperience() > (int)(double)worth;
                } else {
                    hasCosts = VaultController.getInstance().has(mage.getPlayer(), worth);
                }
            }

            if (!hasCosts) {
                String costString = context.getMessage("insufficient_resources");
                if (isXP) {
                    String xpAmount = Integer.toString((int)(double)worth);
                    xpAmount = context.getMessage("costs.xp_amount").replace("$amount", xpAmount);
                    costString = costString.replace("$cost", xpAmount);
                } else {
                    costString = costString.replace("$cost", VaultController.getInstance().format(worth));
                }
                context.sendMessage(costString);
            } else {
                String costString = context.getMessage("deducted");
                if (isXP) {
                    String xpAmount = Integer.toString((int)(double)worth);
                    xpAmount = context.getMessage("costs.xp_amount").replace("$amount", xpAmount);
                    costString = costString.replace("$cost", xpAmount);
                } else {
                    costString = costString.replace("$cost", VaultController.getInstance().format(worth));
                }
                costString = costString.replace("$spell", template.getName());
                context.sendMessage(costString);
                if (isXP) {
                    mage.removeExperience((int)(double)worth);
                } else {
                    VaultController.getInstance().withdrawPlayer(mage.getPlayer(), worth);
                }
                wand.addSpell(spellKey);
                com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = wand.getPath();
                WandUpgradePath nextPath = path != null ? path.getUpgrade(): null;
                if (nextPath != null && autoUpgrade && path.checkUpgradeRequirements(wand, null)) {
                    path.upgrade(wand, mage);
                }
            }
            mage.deactivateGUI();
        }
    }

    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        requiredPath = parameters.getString("path", null);
        exactPath = parameters.getString("path_exact", null);
        requiresCompletedPath = parameters.getString("path_end", null);
        showRequired = parameters.getBoolean("show_required", false);
        showFree = parameters.getBoolean("show_free", false);
        autoUpgrade = parameters.getBoolean("auto_upgrade", false);
        useXP = parameters.getBoolean("use_xp", false);
        if (requiresCompletedPath != null) {
            requiredPath = requiresCompletedPath;
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        this.wand = mage.getActiveWand();
        this.context = context;
		Player player = mage.getPlayer();
		if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        if (wand == null) {
            context.sendMessage("no_wand");
            return SpellResult.FAIL;
        }

        WandUpgradePath path = wand.getPath();

        // Check path requirements
        if (requiredPath != null || exactPath != null) {
            if (path == null) {
                context.sendMessage(context.getMessage("no_path").replace("$wand", wand.getName()));
                return SpellResult.FAIL;
            }

            if ((requiredPath != null && !path.hasPath(requiredPath)) || (exactPath != null && !exactPath.equals(path.getKey()))) {
                WandUpgradePath requiresPath = com.elmakers.mine.bukkit.wand.WandUpgradePath.getPath(requiredPath);
                if (requiresPath != null) {
                    context.sendMessage(context.getMessage("no_required_path").replace("$path", requiresPath.getName()));
                } else {
                    context.getLogger().warning("Invalid path specified in AddSpell action: " + requiredPath);
                }
                return SpellResult.FAIL;
            }
            if (requiresCompletedPath != null) {
                WandUpgradePath pathUpgrade = path.getUpgrade();
                if (pathUpgrade == null) {
                    context.sendMessage(context.getMessage("no_upgrade").replace("$wand", wand.getName()));
                    return SpellResult.FAIL;
                }
                if (path.canEnchant(wand)) {
                    context.sendMessage(context.getMessage("no_path_end").replace("$path", pathUpgrade.getName()));
                    return SpellResult.FAIL;
                }
            }
        }

        // Load spells
        Map<String, Double> availableSpells = new TreeMap<String, Double>();
        if (spells.size() > 0)
        {
            availableSpells.putAll(spells);
        }
        else
        {
            if (path == null) {
                context.sendMessage(context.getMessage("no_path").replace("$wand", wand.getName()));
                return SpellResult.FAIL;
            }

            Collection<String> pathSpells = path.getSpells();
            for (String pathSpell : pathSpells) {
                availableSpells.put(pathSpell, null);
            }
            if (showRequired) {
                Collection<String> requiredSpells = path.getRequiredSpells();
                for (String requiredSpell : requiredSpells) {
                    availableSpells.put(requiredSpell, null);
                }
            }
        }

        List<ItemStack> spellItems = new ArrayList<ItemStack>();
        MagicAPI api = MagicPlugin.getAPI();
        boolean isXP = useXP || !VaultController.hasEconomy();
        String costString = context.getMessage("cost_lore");
        for (Map.Entry<String, Double> spellValue : availableSpells.entrySet()) {
            String spellKey = spellValue.getKey();
            if (wand.hasSpell(spellKey)) continue;

            SpellTemplate spell = api.getSpellTemplate(spellKey);
            Double worth = spellValue.getValue();
            if (worth == null) {
                worth = spell.getWorth();
                spells.put(spellKey, worth);
            }
            if (worth <= 0 && !showFree) continue;

            ItemStack spellItem = api.createSpellItem(spellKey);
            ItemMeta meta = spellItem.getItemMeta();
            List<String> lore = meta.getLore();
            String costs;
            if (isXP) {
                String xpAmount = Integer.toString((int)(double)worth);
                xpAmount = context.getMessage("costs.xp_amount").replace("$amount", xpAmount);
                costs = costString.replace("$cost", xpAmount);
            } else {
                costs = costString.replace("$cost", VaultController.getInstance().format(worth));
            }
            lore.add(ChatColor.GOLD + costs);
            meta.setLore(lore);
            spellItem.setItemMeta(meta);
            spellItems.add(spellItem);
        }

        if (spellItems.size() == 0) {
            context.sendMessage("no_spells");
            return SpellResult.FAIL;
        }

        String inventoryTitle = context.getMessage("title", "Spells ($balance)");
        if (isXP) {
            String xpAmount = Integer.toString(mage.getExperience());
            xpAmount = context.getMessage("costs.xp_amount").replace("$amount", xpAmount);
            inventoryTitle = inventoryTitle.replace("$balance", xpAmount);
        } else {
            double balance = VaultController.getInstance().getBalance(player);
            inventoryTitle = inventoryTitle.replace("$balance", VaultController.getInstance().format(balance));
        }

        int invSize = ((spellItems.size() + 9) / 9) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        for (ItemStack item : spellItems)
        {
            displayInventory.addItem(item);
        }
        mage.activateGUI(this);
        mage.getPlayer().openInventory(displayInventory);

        return SpellResult.CAST;
	}

    @Override
    public void getParameterNames(Collection<String> parameters)
    {
        super.getParameterNames(parameters);
        parameters.add("path");
        parameters.add("path_end");
        parameters.add("path_exact");
        parameters.add("show_free");
        parameters.add("show_required");
        parameters.add("auto_upgrade");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        super.getParameterOptions(examples, parameterKey);

        if (parameterKey.equals("path") || parameterKey.equals("path_exact") || parameterKey.equals("path_end")) {
            examples.addAll(com.elmakers.mine.bukkit.wand.WandUpgradePath.getPathKeys());
        } else if (parameterKey.equals("show_free") || parameterKey.equals("show_required") || parameterKey.equals("auto_upgrade")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        }
    }
}
