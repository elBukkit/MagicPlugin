package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;

@Deprecated
public class StashSpell extends TargetingSpell
{
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Target target = getTarget();
        if (!target.hasEntity()) return SpellResult.NO_TARGET;
        Entity targetEntity = target.getEntity();
        if (!(targetEntity instanceof HumanEntity)) return SpellResult.NO_TARGET;

        Player showPlayer = mage.getPlayer();
        if (showPlayer == null) return SpellResult.PLAYER_REQUIRED;
        String typeString = parameters.getString("type", "ender");

        // Special case for wands
        if (targetEntity instanceof Player && targetEntity != showPlayer) {
            Player targetPlayer = (Player)targetEntity;
            Mage targetMage = controller.getMage(targetPlayer);

            if (!mage.isSuperPowered() && isSuperProtected(targetEntity)) {
                return SpellResult.NO_TARGET;
            }

            if (targetMage.getActiveWand() != null && typeString.equalsIgnoreCase("inventory")) {
                targetMage.getActiveWand().closeInventory();
            }
        }

        // Make sure to close the player's wand
        Wand activeWand = mage.getActiveWand();
        if (activeWand != null) {
            activeWand.closeInventory();
        }

        HumanEntity humanTarget = (HumanEntity)targetEntity;

        if (typeString.equalsIgnoreCase("inventory")) {
            Inventory inventory = humanTarget.getInventory();
            showPlayer.openInventory(inventory);
        } else {
            Inventory enderInventory = humanTarget.getEnderChest();
            showPlayer.openInventory(enderInventory);
        }

        return SpellResult.CAST;
    }
}
