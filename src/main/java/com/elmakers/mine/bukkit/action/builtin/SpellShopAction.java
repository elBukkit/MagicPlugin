package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseShopAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellShopAction extends BaseShopAction
{
    private boolean showRequired = false;
    private boolean showFree = false;
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
        Wand wand = mage.getActiveWand();
        WandUpgradePath path = wand == null ? null : wand.getPath();

        if (!castsSpells && wand.isLocked()) {
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
            if (path == null) {
                context.showMessage(context.getMessage("no_path", "You may not learn with that $wand.").replace("$wand", wand.getName()));
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
            String key = spellValue.getKey();
            key = context.parameterize(key);
            String spellKey = key.split(" ", 2)[0];

            if (!castsSpells && wand.hasSpell(spellKey)) continue;

            SpellTemplate spell = controller.getSpellTemplate(spellKey);
            Double worth = spellValue.getValue();
            if (worth == null) {
                worth = spell.getWorth();
            } else if (costScale > 0) {
                // See note in ItemShopAction about how ugly this is.
                worth /= costScale;
            }
            if (worth <= 0 && !showFree) continue;
            if (!spell.hasCastPermission(mage.getCommandSender())) continue;

            ItemStack spellItem = controller.createSpellItem(key, castsSpells);
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
