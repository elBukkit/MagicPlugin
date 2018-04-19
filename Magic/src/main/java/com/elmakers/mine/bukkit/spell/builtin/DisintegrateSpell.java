package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.Target;

@Deprecated
public class DisintegrateSpell extends BlockSpell
{
    private static final int             DEFAULT_PLAYER_DAMAGE = 1;
    private static final int             DEFAULT_ENTITY_DAMAGE = 100;

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Target target = getTarget();

        int playerDamage = parameters.getInt("player_damage", DEFAULT_PLAYER_DAMAGE);
        int entityDamage = parameters.getInt("entity_damage", DEFAULT_ENTITY_DAMAGE);

        if (target.hasEntity())
        {
            Entity targetEntity = target.getEntity();
            if (controller.isElemental(targetEntity))
            {
                int elementalDamage = parameters.getInt("elemental_damage", DEFAULT_ENTITY_DAMAGE);
                controller.damageElemental(targetEntity, elementalDamage, 0, mage.getCommandSender());
                return SpellResult.CAST;
            }
            else
            {
                registerModified(targetEntity);
                if (targetEntity instanceof Player)
                {
                    Player player = (Player)targetEntity;
                    CompatibilityUtils.magicDamage(player, mage.getDamageMultiplier() * playerDamage, mage.getEntity());
                }
                else  if (targetEntity instanceof LivingEntity)
                {
                    LivingEntity li = (LivingEntity)targetEntity;
                    CompatibilityUtils.magicDamage(li, mage.getDamageMultiplier() * entityDamage, mage.getEntity());
                }
                else
                {
                    targetEntity.remove();
                }
                registerForUndo();
                return SpellResult.CAST;
            }
        }

        if (!target.hasTarget())
        {
            return SpellResult.NO_TARGET;
        }

        Block targetBlock = target.getBlock();
        if (!hasBreakPermission(targetBlock))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (!isDestructible(targetBlock))
        {
            return SpellResult.NO_TARGET;
        }

        registerForUndo(targetBlock);

        // This makes $target messaging work properly, otherwise
        // it always displays air or water
        MaterialAndData targetMaterial = new MaterialAndData(targetBlock);
        getCurrentCast().setTargetName(targetMaterial.getName());

        if (isUnderwater())
        {
            targetBlock.setType(Material.WATER);
        }
        else
        {
            targetBlock.setType(Material.AIR);
        }

        registerForUndo();
        return SpellResult.CAST;
    }
}
