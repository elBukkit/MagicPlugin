package com.elmakers.mine.bukkit.spell.builtin;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.item.ItemUpdatedCallback;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.Target;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;

@Deprecated
public class ShrinkSpell extends BlockSpell
{
    private static final int             DEFAULT_PLAYER_DAMAGE = 1;
    private static final int             DEFAULT_ENTITY_DAMAGE = 100;

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        String giveName = parameters.getString("name");
        if (giveName != null) {
            String itemName = giveName + "'s Head";
            dropHead(controller, getLocation(), giveName, itemName);
            return SpellResult.CAST;
        }

        Target target = getTarget();

        if (target.hasEntity()) {
            Entity targetEntity = target.getEntity();
            if (controller.isElemental(targetEntity))
            {
                double elementalSize = controller.getElementalScale(targetEntity);
                if (elementalSize < 0.1) {
                    int elementalDamage = parameters.getInt("elemental_damage", DEFAULT_ENTITY_DAMAGE);
                    controller.damageElemental(targetEntity, elementalDamage, 0, mage.getCommandSender());
                } else {
                    elementalSize /= 2;
                    controller.setElementalScale(targetEntity, elementalSize);
                }

                return SpellResult.CAST;
            }

            if (!(targetEntity instanceof LivingEntity)) return SpellResult.NO_TARGET;

            // Register for undo in advance to catch entity death.
            registerForUndo();

            int damage =  parameters.getInt("entity_damage", DEFAULT_ENTITY_DAMAGE);

            LivingEntity li = (LivingEntity)targetEntity;
            boolean alreadyDead = li.isDead() || li.getHealth() <= 0;
            String itemName = CompatibilityLib.getInstance().getDisplayName(li) + " Head";;

            if (li instanceof Player)
            {
                damage = parameters.getInt("player_damage", DEFAULT_PLAYER_DAMAGE);
            }

            Location targetLocation = targetEntity.getLocation();
            if (li instanceof Player) {
                CompatibilityLib.getCompatibilityUtils().magicDamage(li, damage, mage.getEntity());
                if (li.isDead() && !alreadyDead) {
                    dropHead(controller, targetEntity, itemName);
                }
            }
            else if (li.getType() == EntityType.GIANT) {
                UndoList spawnedList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(li);
                registerModified(li);
                li.remove();
                Entity zombie = targetLocation.getWorld().spawnEntity(targetLocation, EntityType.ZOMBIE);
                if (zombie instanceof Zombie) {
                    ((Zombie)zombie).setBaby(false);
                }
                registerForUndo(zombie);
                if (spawnedList != null) {
                    spawnedList.add(zombie);
                }
            }
            else if (li instanceof Ageable && ((Ageable)li).isAdult() && !(li instanceof Player)) {
                registerModified(li);
                ((Ageable)li).setBaby();
            } else  if (li instanceof Zombie && !((Zombie)li).isBaby()) {
                registerModified(li);
                ((Zombie)li).setBaby(true);
            } else  if (li instanceof PigZombie && !((PigZombie)li).isBaby()) {
                registerModified(li);
                ((PigZombie)li).setBaby(true);
            } else  if (li instanceof Slime && ((Slime)li).getSize() > 1) {
                registerModified(li);
                Slime slime = (Slime)li;
                slime.setSize(slime.getSize() - 1);
            } else {
                CompatibilityLib.getCompatibilityUtils().magicDamage(li, damage, mage.getEntity());
                if ((li.isDead() || li.getHealth() == 0) && !alreadyDead) {
                    dropHead(controller, targetEntity, itemName);
                }
            }
        } else {
            Block targetBlock = target.getBlock();
            if (targetBlock == null) {
                return SpellResult.NO_TARGET;
            }
            String blockSkin = getBlockSkin(targetBlock.getType());
            if (blockSkin == null) return SpellResult.NO_TARGET;

            if (!hasBreakPermission(targetBlock))
            {
                return SpellResult.INSUFFICIENT_PERMISSION;
            }
            if (mage.isIndestructible(targetBlock))
            {
                return SpellResult.NO_TARGET;
            }

            registerForUndo(targetBlock);
            registerForUndo();

            dropHead(controller, targetBlock.getLocation(), blockSkin, targetBlock.getType().name());
            targetBlock.setType(Material.AIR);
        }

        return SpellResult.CAST;
    }


    protected void dropHead(MageController controller, Entity entity, String itemName) {
        controller.getSkull(entity, itemName, new ItemUpdatedCallback() {
            @Override
            public void updated(@Nullable ItemStack itemStack) {
                if (!ItemUtils.isEmpty(itemStack)) {
                    Location location = entity instanceof LivingEntity ? ((LivingEntity)entity).getEyeLocation() : entity.getLocation();
                    location.getWorld().dropItemNaturally(location, itemStack);
                }
            }
        });
    }

    protected void dropHead(MageController controller, Location location, String ownerName, String itemName) {
        controller.getSkull(ownerName, itemName, new ItemUpdatedCallback() {
            @Override
            public void updated(@Nullable ItemStack itemStack) {
                if (!ItemUtils.isEmpty(itemStack)) {
                    location.setX(location.getX() + 0.5);
                    location.setY(location.getY() + 0.5);
                    location.setZ(location.getZ() + 0.5);
                    location.getWorld().dropItemNaturally(location, itemStack);
                }
            }
        });
    }
}
