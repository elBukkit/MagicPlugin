package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;

@Deprecated
public class HatSpell extends TargetingSpell
{
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        Block target = getTargetBlock();

        if (target == null)
        {
            return SpellResult.NO_TARGET;
        }

        MaterialAndData material = new MaterialAndData(target);
        if (material.getMaterial() == Material.AIR)
        {
            return SpellResult.NO_TARGET;
        }
        ItemStack hatItem = material.getItemStack(1);
        ItemStack itemStack = player.getInventory().getHelmet();
        ItemMeta meta = hatItem.getItemMeta();
        meta.setDisplayName(getMessage("hat_name").replace("$material", material.getName()));
        List<String> lore = new ArrayList<>();
        lore.add(getMessage("hat_lore"));
        meta.setLore(lore);
        hatItem.setItemMeta(meta);
        hatItem = ItemUtils.makeReal(hatItem);
        ItemUtils.makeTemporary(hatItem, getMessage("removed").replace("$material", material.getName()));
        player.getInventory().setHelmet(hatItem);
        if (itemStack != null && itemStack.getType() != Material.AIR && !ItemUtils.isTemporary(itemStack)) {
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
        }
        return SpellResult.CAST;
    }
}
