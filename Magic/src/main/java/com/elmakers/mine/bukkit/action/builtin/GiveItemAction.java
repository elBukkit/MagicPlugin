package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;

public class GiveItemAction extends BaseSpellAction
{
    private ItemStack item = null;
    private int itemCount = 0;
    private int maxItemCount = 0;
    private ItemStack requireItem = null;
    private String permissionNode = null;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        MageController controller = context.getController();

        permissionNode = parameters.getString("permission", null);
        String itemKey = parameters.getString("item");
        itemCount = parameters.getInt("item_count", 0);
        maxItemCount = parameters.getInt("max_item_count", 0);
        item = controller.createItem(itemKey);
        if (item == null) {
            context.getLogger().warning("Invalid item: " + itemKey);
        }
        else
        {
            String name = parameters.getString("name", null);
            List<String> lore = ConfigurationUtils.getStringList(parameters, "lore");
            if ((name != null && !name.isEmpty()) || (lore != null && !lore.isEmpty()))
            {
                ItemMeta meta = item.getItemMeta();
                if (name != null && !name.isEmpty())
                {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                }
                if (lore != null && !lore.isEmpty())
                {
                    for (int i = 0; i < lore.size(); i++)
                    {
                        lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                    }
                    meta.setLore(lore);
                }
                item.setItemMeta(meta);
            }
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

        Entity targetEntity = context.getTargetEntity();
        if (targetEntity == null) {
            return SpellResult.NO_TARGET;
        }
        if (!(targetEntity instanceof Player)) {
            return SpellResult.PLAYER_REQUIRED;
        }

        MageController controller = context.getController();
        Player player = (Player)targetEntity;
        if (permissionNode != null && !player.hasPermission(permissionNode)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (requireItem != null && !controller.takeItem(player, requireItem)) {
            context.showMessage("insufficient_resources", "You must have a $requires");
            return SpellResult.INSUFFICIENT_RESOURCES;
        }

        Mage mage = controller.getMage(player);
        ItemStack itemCopy = InventoryUtils.getCopy(item);
        int setAmount = itemCount;
        if (maxItemCount > 0) {
            int currentCount = mage.getItemCount(item);
            int maxAmount = maxItemCount - currentCount;;
            if (setAmount > 0) {
                setAmount = Math.min(setAmount, maxAmount);
            } else {
                setAmount = maxAmount;
            }

            if (setAmount <= 0) {
                return SpellResult.NO_TARGET;
            }
        }

        if (setAmount > 0) {
            itemCopy.setAmount(setAmount);
        }
        mage.giveItem(itemCopy);
        DeprecatedUtils.updateInventory(player);
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
