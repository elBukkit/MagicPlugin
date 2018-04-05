package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;

public class CaptureAction extends BaseSpellAction {
    private double minHealth;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        minHealth = parameters.getDouble("min_health", -1);
    }

    @Override
    public SpellResult perform(CastContext context) {

        Entity targetEntity = context.getTargetEntity();
        if (targetEntity instanceof Player) {
            return SpellResult.NO_TARGET;
        }
        if (minHealth >= 0) {
            if (!(targetEntity instanceof LivingEntity)) {
                return SpellResult.NO_TARGET;
            }
            LivingEntity li = (LivingEntity)targetEntity;
            if (li.getHealth() > minHealth) {
                return SpellResult.NO_TARGET;
            }
        }
        String entityTypeString = CompatibilityUtils.getEntityType(targetEntity);
        if (entityTypeString == null) {
            return SpellResult.FAIL;
        }
        Object savedEntity = CompatibilityUtils.getEntityData(targetEntity);
        if (savedEntity == null) {
            return SpellResult.FAIL;
        }
        if (targetEntity instanceof LivingEntity) {
            LivingEntity li = (LivingEntity)targetEntity;
            li.setHealth(DeprecatedUtils.getMaxHealth(li));
        }
        targetEntity.remove();
        ItemStack spawnEgg = new ItemStack(Material.VILLAGER_SPAWN_EGG);

        String entityName = targetEntity.getCustomName();
        if (entityName != null && !entityName.isEmpty()) {
            ItemMeta meta = spawnEgg.getItemMeta();
            meta.setDisplayName("Spawn " + entityName);
            spawnEgg.setItemMeta(meta);
        }
        spawnEgg = InventoryUtils.makeReal(spawnEgg);

        // Add entity type attribute
        CompatibilityUtils.setMeta(savedEntity, "id", entityTypeString);

        // Remove instance-specific attributes
        CompatibilityUtils.removeMeta(savedEntity, "Pos");
        CompatibilityUtils.removeMeta(savedEntity, "Motion");
        CompatibilityUtils.removeMeta(savedEntity, "Rotation");
        CompatibilityUtils.removeMeta(savedEntity, "FallDistance");
        CompatibilityUtils.removeMeta(savedEntity, "Dimension");
        CompatibilityUtils.removeMeta(savedEntity, "UUID");
        CompatibilityUtils.removeMeta(savedEntity, "PortalCooldown");

        if (!CompatibilityUtils.setMetaNode(spawnEgg, "EntityTag", savedEntity)) {
            return SpellResult.FAIL;
        }
        targetEntity.getWorld().dropItemNaturally(targetEntity.getLocation(), spawnEgg);

        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable() {
        return false;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
