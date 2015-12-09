package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class ArmorStandProjectileAction extends CustomProjectileAction {
    private boolean smallArmorStand = false;
    private boolean useArmorStand = false;
    private boolean armorStandMarker = false;
    private boolean armorStandInvisible = false;
    private boolean useHelmet = false;
    private ItemStack heldItem = null;
    private ArmorStand armorStand = null;
    private CreatureSpawnEvent.SpawnReason armorStandSpawnReason = CreatureSpawnEvent.SpawnReason.CUSTOM;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        armorStandMarker = parameters.getBoolean("armor_stand_marker", true);
        armorStandInvisible = parameters.getBoolean("armor_stand_invisible", true);
        smallArmorStand = parameters.getBoolean("armor_stand_small", false);
        MaterialAndData itemType = ConfigurationUtils.getMaterialAndData(parameters, "held_item");
        if (itemType != null) {
            heldItem = itemType.getItemStack(1);
        }
        useHelmet = parameters.getBoolean("armor_stand_helmet", false);
        if (parameters.contains("spawn_reason")) {
            String reasonText = parameters.getString("spawn_reason").toUpperCase();
            try {
                armorStandSpawnReason = CreatureSpawnEvent.SpawnReason.valueOf(reasonText);
            } catch (Exception ex) {
                context.getMage().sendMessage("Unknown spawn reason: " + reasonText);
            }
        }
    }

    @Override
    public SpellResult start(CastContext context) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        armorStand = CompatibilityUtils.spawnArmorStand(mage.getEyeLocation());
        if (useHelmet) {
            armorStand.setHelmet(heldItem);
        } else {
            armorStand.setItemInHand(heldItem);
        }
        armorStand.setMetadata("notarget", new FixedMetadataValue(controller.getPlugin(), true));
        armorStand.setMetadata("broom", new FixedMetadataValue(controller.getPlugin(), true));
        if (armorStandInvisible) {
            CompatibilityUtils.setInvisible(armorStand, true);
        }
        if (armorStandMarker) {
            CompatibilityUtils.setMarker(armorStand, true);
        }
        CompatibilityUtils.setGravity(armorStand, false);
        CompatibilityUtils.setDisabledSlots(armorStand, 2039552);
        if (smallArmorStand) {
            CompatibilityUtils.setSmall(armorStand, true);
        }
        armorStand.setMetadata("notarget", new FixedMetadataValue(controller.getPlugin(), true));
        CompatibilityUtils.addToWorld(mage.getLocation().getWorld(), armorStand, armorStandSpawnReason);

        return super.start(context);
    }

    @Override
    public SpellResult step(CastContext context) {
        SpellResult result = super.step(context);
        armorStand.teleport(actionContext.getTargetLocation());
        return result;
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        armorStand.remove();
    }
}
