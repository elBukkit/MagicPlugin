package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.SafetyUtils;
import com.elmakers.mine.bukkit.utility.Target;
import com.elmakers.mine.bukkit.wand.Wand;

@Deprecated
public class DisarmSpell extends TargetingSpell
{
    private Random random = new Random();

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Target target = getTarget();
        if (!target.hasEntity() || !(target.getEntity() instanceof LivingEntity)) {
            return SpellResult.NO_TARGET;
        }
        LivingEntity entity = (LivingEntity)target.getEntity();

        EntityEquipment equipment = entity.getEquipment();
        ItemStack stack = equipment.getItemInHand();

        if (stack == null || stack.getType() == Material.AIR)
        {
            return SpellResult.NO_TARGET;
        }

        // Special case for wands
        if (Wand.isWand(stack) && controller.isMage(entity)) {
            Mage targetMage = controller.getMage(entity);

            // Check for protected players (admins, generally...)
            // This gets overridden by superpower...
            if (!mage.isSuperPowered() && isSuperProtected(entity)) {
                return SpellResult.NO_TARGET;
            }

            if (targetMage.getActiveWand() != null) {
                targetMage.getActiveWand().deactivate();
            }
        }

        Integer targetSlot = null;
        PlayerInventory targetInventory = null;
        ItemStack swapItem = null;
        if (entity instanceof Player && parameters.getBoolean("keep_in_inventory", false)) {
            Player targetPlayer = (Player)entity;
            targetInventory = targetPlayer.getInventory();
            List<Integer> validSlots = new ArrayList<>();
            ItemStack[] contents = targetInventory.getContents();
            int minSlot = parameters.getInt("min_slot", Wand.HOTBAR_SIZE);
            int maxSlot = parameters.getInt("max_slot", contents.length - 1);

            for (int i = minSlot; i <= maxSlot; i++) {
                if (contents[i] == null || contents[i].getType() == Material.AIR) {
                    validSlots.add(i);
                }
            }

            // Randomly choose a slot if no empty one was found
            if (validSlots.size() == 0) {
                int swapSlot = random.nextInt(maxSlot - minSlot) + minSlot;
                swapItem = targetInventory.getItem(swapSlot);
                validSlots.add(swapSlot);
            }

            int chosen = random.nextInt(validSlots.size());
            targetSlot = validSlots.get(chosen);
        }

        equipment.setItemInHand(swapItem);
        if (targetSlot != null && targetInventory != null) {
            targetInventory.setItem(targetSlot, stack);
        } else {
            Location location = entity.getLocation();
            location.setY(location.getY() + 1);
            Item item = entity.getWorld().dropItemNaturally(location, stack);
            Vector velocity = item.getVelocity();
            velocity.setY(velocity.getY() * 5);
            SafetyUtils.setVelocity(item, velocity);
        }

        return SpellResult.CAST;

    }
}
