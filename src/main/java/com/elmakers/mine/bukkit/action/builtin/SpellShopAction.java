package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseShopAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellShopAction extends BaseShopAction
{
    private String requiredPath = null;
    private String requiresCompletedPath = null;
    private String exactPath = null;
    private boolean showRequired = false;
    private boolean autoUpgrade = false;
    private boolean showFree = false;
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
    protected void onPurchase(CastContext context) {
        Mage mage = context.getMage();
        Wand wand = mage.getActiveWand();
        if (wand == null) return;

        com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = wand.getPath();
        WandUpgradePath nextPath = path != null ? path.getUpgrade(): null;
        if (nextPath != null && autoUpgrade && path.checkUpgradeRequirements(wand, null) && !path.canEnchant(wand)) {
            path.upgrade(wand, mage);
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
        if (requiresCompletedPath != null) {
            requiredPath = requiresCompletedPath;
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
		Player player = mage.getPlayer();
		if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        Wand wand = mage.getActiveWand();
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
        Map<String, Double> spellPrices = new HashMap<String, Double>();
        if (spells.size() > 0)
        {
            spellPrices.putAll(spells);
        }
        else
        {
            if (path == null) {
                context.sendMessage(context.getMessage("no_path").replace("$wand", wand.getName()));
                return SpellResult.FAIL;
            }

            Collection<String> pathSpells = path.getSpells();
            for (String pathSpell : pathSpells) {
                spellPrices.put(pathSpell, null);
            }
            if (showRequired) {
                Collection<String> requiredSpells = path.getRequiredSpells();
                for (String requiredSpell : requiredSpells) {
                    spellPrices.put(requiredSpell, null);
                }
            }
        }

        List<ShopItem> shopItems = new ArrayList<ShopItem>();
        for (Map.Entry<String, Double> spellValue : spellPrices.entrySet()) {
            String spellKey = spellValue.getKey();
            if (wand.hasSpell(spellKey)) continue;

            SpellTemplate spell = controller.getSpellTemplate(spellKey);
            Double worth = spellValue.getValue();
            if (worth == null) {
                worth = spell.getWorth();
                spellPrices.put(spellKey, worth);
            }
            if (worth <= 0 && !showFree) continue;

            ItemStack spellItem = controller.createSpellItem(spellKey);
            shopItems.add(new ShopItem(spellItem, worth));
        }

        return showItems(context, shopItems);
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
        if (parameterKey.equals("path") || parameterKey.equals("path_exact") || parameterKey.equals("path_end")) {
            examples.addAll(com.elmakers.mine.bukkit.wand.WandUpgradePath.getPathKeys());
        } else if (parameterKey.equals("show_free") || parameterKey.equals("show_required")
                || parameterKey.equals("auto_upgrade")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }
}
