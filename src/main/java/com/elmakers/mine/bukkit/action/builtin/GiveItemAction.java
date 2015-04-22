package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class GiveItemAction extends BaseSpellAction
{
    private ItemStack item = null;
    private ItemStack requireItem = null;
    private String permissionNode = null;

    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        MageController controller = context.getController();

        permissionNode = parameters.getString("permission", null);
        String itemKey = parameters.getString("item");
        item = controller.createItem(itemKey);
        if (item == null) {
            context.getLogger().warning("Invalid item: " + itemKey);
        }
        String costKey = parameters.getString("requires");
        if (costKey != null && !costKey.isEmpty())
        {
            requireItem = controller.createItem(costKey);
            if (requireItem == null) {
                context.getLogger().warning("Invalid required item: " + costKey);
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (item == null) {
            return SpellResult.FAIL;
        }

        Mage mage = context.getMage();
        MageController controller = context.getController();
		Player player = mage.getPlayer();
		if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        if (permissionNode != null && !player.hasPermission(permissionNode)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (requireItem != null) {
            boolean foundItem = false;
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (controller.itemsAreEqual(item, requireItem)) {
                    player.getInventory().setItem(i, null);
                    foundItem = true;
                    break;
                }
            }
            if (!foundItem) {
                context.sendMessage("insufficient_resources");
                return SpellResult.INSUFFICIENT_RESOURCES;
            }
        }

        mage.giveItem(InventoryUtils.getCopy(item));
        mage.getPlayer().updateInventory();
        return SpellResult.CAST;
	}

    @Override
    public String transformMessage(String message) {
        MagicAPI api = MagicPlugin.getAPI();
        if (this.requireItem != null) {
            message = message.replace("$requires", api.describeItem(requireItem));
        }
        if (item != null) {
            message = message.replace("$item", api.describeItem(item));
        }
        return message;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("item");
        parameters.add("require");
        parameters.add("permission");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("item") || parameterKey.equals("require")) {
            MagicAPI api = MagicPlugin.getAPI();
            Collection<SpellTemplate> spellList = api.getSpellTemplates();
            for (SpellTemplate spellTemplate : spellList) {
                examples.add(spellTemplate.getKey());
            }
            Collection<String> allWands = api.getWandKeys();
            for (String wandKey : allWands) {
                examples.add(wandKey);
            }
            examples.addAll(api.getBrushes());
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
