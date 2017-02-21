package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import de.slikey.effectlib.math.VectorTransform;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class ArmorStandProjectileAction extends EntityProjectileAction {
    private boolean smallArmorStand = false;
    private boolean armorStandMarker = false;
    private boolean armorStandInvisible = false;
    private boolean armorStandGravity = false;
    private boolean adjustArmPitch = false;
    private boolean adjustHeadPitch = false;
    private boolean showArmorStandArms = true;
    private boolean showArmorStandBaseplate = true;
    private boolean unbreakableItems = false;
    private ItemStack rightArmItem = null;
    private ItemStack helmetItem = null;
    private ItemStack chestplateItem = null;
    private ItemStack leggingsItem = null;
    private ItemStack bootsItem = null;
    private VectorTransform leftArmTransform;
    private VectorTransform rightArmTransform;
    private VectorTransform leftLegTransform;
    private VectorTransform rightLegTransform;
    private VectorTransform bodyTransform;
    private VectorTransform headTransform;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);

        if (parameters.isConfigurationSection("left_arm_transform")) {
            leftArmTransform = new VectorTransform(ConfigurationUtils.getConfigurationSection(parameters, "right_arm_transform"));
        }
        if (parameters.isConfigurationSection("right_arm_transform")) {
            rightArmTransform = new VectorTransform(ConfigurationUtils.getConfigurationSection(parameters, "right_arm_transform"));
        }
        if (parameters.isConfigurationSection("right_leg_transform")) {
            rightLegTransform = new VectorTransform(ConfigurationUtils.getConfigurationSection(parameters, "right_leg_transform"));
        }
        if (parameters.isConfigurationSection("left_leg_transform")) {
            leftLegTransform = new VectorTransform(ConfigurationUtils.getConfigurationSection(parameters, "left_leg_transform"));
        }
        if (parameters.isConfigurationSection("head_transform")) {
            headTransform = new VectorTransform(ConfigurationUtils.getConfigurationSection(parameters, "head_transform"));
        }
        if (parameters.isConfigurationSection("body_transform")) {
            bodyTransform = new VectorTransform(ConfigurationUtils.getConfigurationSection(parameters, "body_transform"));
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        armorStandMarker = parameters.getBoolean("armor_stand_marker", true);
        armorStandInvisible = parameters.getBoolean("armor_stand_invisible", true);
        armorStandGravity = parameters.getBoolean("armor_stand_gravity", true);
        showArmorStandArms = parameters.getBoolean("armor_stand_arms", true);
        showArmorStandBaseplate = parameters.getBoolean("armor_stand_baseplate", true);
        smallArmorStand = parameters.getBoolean("armor_stand_small", false);
        adjustHeadPitch = parameters.getBoolean("orient_head", false);
        adjustArmPitch = parameters.getBoolean("orient_right_arm", false);
        unbreakableItems = parameters.getBoolean("unbreakable_items", false);
        
        MageController controller = context.getController();
        ItemData itemType = controller.getOrCreateItem(parameters.getString("right_arm_item"));
        if (itemType != null) {
            rightArmItem = itemType.getItemStack(1);
            if (rightArmItem != null && unbreakableItems) {
                InventoryUtils.makeUnbreakable(rightArmItem);
            }
        }
        itemType = controller.getOrCreateItem(parameters.getString("helmet_item"));
        if (itemType != null) {
            helmetItem = itemType.getItemStack(1);
            if (helmetItem != null && unbreakableItems) {
                InventoryUtils.makeUnbreakable(InventoryUtils.makeReal(helmetItem));
            }
        }
        itemType = controller.getOrCreateItem(parameters.getString("chestplate_item"));
        if (itemType != null) {
            chestplateItem = itemType.getItemStack(1);
            if (chestplateItem != null && unbreakableItems) {
                InventoryUtils.makeUnbreakable(InventoryUtils.makeReal(chestplateItem));
            }
        }
        itemType = controller.getOrCreateItem(parameters.getString("leggings_item"));
        if (itemType != null) {
            leggingsItem = itemType.getItemStack(1);
            if (leggingsItem != null && unbreakableItems) {
                InventoryUtils.makeUnbreakable(InventoryUtils.makeReal(leggingsItem));
            }
        }
        itemType = controller.getOrCreateItem(parameters.getString("boots_item"));
        if (itemType != null) {
            bootsItem = itemType.getItemStack(1);
            if (bootsItem != null && unbreakableItems) {
                InventoryUtils.makeUnbreakable(InventoryUtils.makeReal(bootsItem));
            }
        }
    }

    @Override
    public SpellResult start(CastContext context) {
        MageController controller = context.getController();
        Location location = context.getEyeLocation();
        ArmorStand armorStand = (ArmorStand)setEntity(controller, CompatibilityUtils.spawnArmorStand(location));
        CompatibilityUtils.setYawPitch(armorStand, location.getYaw(), location.getPitch());
        armorStand.setItemInHand(rightArmItem);
        armorStand.setHelmet(helmetItem);
        armorStand.setChestplate(chestplateItem);
        armorStand.setLeggings(leggingsItem);
        armorStand.setBoots(bootsItem);
        armorStand.setMarker(armorStandMarker);
        armorStand.setVisible(!armorStandInvisible);
        armorStand.setBasePlate(showArmorStandBaseplate);
        CompatibilityUtils.setGravity(armorStand, armorStandGravity);
        armorStand.setSmall(smallArmorStand);
        armorStand.setArms(showArmorStandArms);
        CompatibilityUtils.setDisabledSlots(armorStand, 2039552);
        CompatibilityUtils.addToWorld(location.getWorld(), armorStand, spawnReason);

        return super.start(context);
    }

    @Override
    public SpellResult step(CastContext context) {
        SpellResult result = super.step(context);
        ArmorStand armorStand = (ArmorStand)entity;
        double t = (double)flightTime / 1000;
        Location currentLocation = entity.getLocation();

        if (leftArmTransform != null) {
            Vector direction = leftArmTransform.get(launchLocation, t);
            armorStand.setLeftArmPose(new EulerAngle(direction.getX(), direction.getY(), direction.getZ()));
        }
        if (rightArmTransform != null) {
            Vector direction = rightArmTransform.get(launchLocation, t);
            double pitchOffset = adjustArmPitch ? Math.toRadians(currentLocation.getPitch()) : 0;
            armorStand.setRightArmPose(new EulerAngle(direction.getX(), direction.getY() + pitchOffset, direction.getZ()));
        } else if (adjustArmPitch) {
            EulerAngle armPose = armorStand.getRightArmPose();
            armPose = armPose.setY(Math.toRadians(-currentLocation.getPitch()));
            armorStand.setRightArmPose(armPose);
        }
        if (leftLegTransform != null) {
            Vector direction = leftLegTransform.get(launchLocation, t);
            armorStand.setLeftLegPose(new EulerAngle(direction.getX(), direction.getY(), direction.getZ()));
        }
        if (rightLegTransform != null) {
            Vector direction = rightLegTransform.get(launchLocation, t);
            armorStand.setRightLegPose(new EulerAngle(direction.getX(), direction.getY(), direction.getZ()));
        }
        if (bodyTransform != null) {
            Vector direction = bodyTransform.get(launchLocation, t);
            armorStand.setBodyPose(new EulerAngle(direction.getX(), direction.getY(), direction.getZ()));
        }
        if (headTransform != null) {
            Vector direction = headTransform.get(launchLocation, t);
            double pitchOffset = adjustHeadPitch ? Math.toRadians(currentLocation.getPitch()) : 0;
            armorStand.setHeadPose(new EulerAngle(direction.getX(), direction.getY() + pitchOffset, direction.getZ()));
        } else if (adjustHeadPitch) {
            EulerAngle headPose = armorStand.getHeadPose();
            headPose = headPose.setY(Math.toRadians(-currentLocation.getPitch()));
            armorStand.setRightArmPose(headPose);
        }
        return result;
    }
}
