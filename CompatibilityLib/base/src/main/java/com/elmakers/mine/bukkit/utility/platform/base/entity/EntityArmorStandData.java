package com.elmakers.mine.bukkit.utility.platform.base.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class EntityArmorStandData extends EntityExtraData {
    public ItemStack itemInHand;
    public ItemStack boots;
    public ItemStack leggings;
    public ItemStack chestplate;
    public ItemStack helmet;
    public EulerAngle bodyPose;
    public EulerAngle leftArmPose;
    public EulerAngle rightArmPose;
    public EulerAngle leftLegPose;
    public EulerAngle rightLegPose;
    public EulerAngle headPose;
    public Boolean isMarker;
    public Boolean hasGravity;
    public Boolean isInvisible;
    public Boolean hasArms;
    public Boolean isSmall;
    public Boolean hasBasePlate;
    public Integer disabledSlots;

    public EntityArmorStandData(ArmorStand armorStand) {
        EntityEquipment equipment = armorStand.getEquipment();
        itemInHand = equipment == null ? null : equipment.getItemInMainHand();
        if (itemInHand != null) itemInHand = itemInHand.clone();
        boots = equipment == null ? null : equipment.getBoots();
        if (boots != null) boots = boots.clone();
        leggings = equipment == null ? null : equipment.getLeggings();
        if (leggings != null) leggings = leggings.clone();
        chestplate = equipment == null ? null : equipment.getChestplate();
        if (chestplate != null) chestplate = chestplate.clone();
        helmet = equipment == null ? null : equipment.getHelmet();
        if (helmet != null) helmet = helmet.clone();
        bodyPose = armorStand.getBodyPose();
        leftArmPose = armorStand.getLeftArmPose();
        rightArmPose = armorStand.getRightArmPose();
        leftLegPose = armorStand.getLeftLegPose();
        rightLegPose = armorStand.getRightLegPose();
        headPose = armorStand.getHeadPose();
        hasGravity = armorStand.hasGravity();
        isInvisible = !armorStand.isVisible();
        hasArms = armorStand.hasArms();
        isSmall = armorStand.isSmall();
        hasBasePlate = armorStand.hasBasePlate();
        isMarker = armorStand.isMarker();
        disabledSlots = getPlatform().getCompatibilityUtils().getDisabledSlots(armorStand);
    }

    public EntityArmorStandData(ConfigurationSection parameters) {
        isSmall = ConfigUtils.getOptionalBoolean(parameters, "small");
        isInvisible = ConfigUtils.getOptionalBoolean(parameters, "invisible");
        hasBasePlate = ConfigUtils.getOptionalBoolean(parameters, "baseplate");
        hasGravity = ConfigUtils.getOptionalBoolean(parameters, "gravity");
        isMarker = ConfigUtils.getOptionalBoolean(parameters, "marker");
        hasArms = ConfigUtils.getOptionalBoolean(parameters, "arms");
        disabledSlots = ConfigUtils.getOptionalInteger(parameters, "disabled_slots");

        bodyPose = ConfigUtils.getEulerAngle(parameters, "body_pose");
        leftArmPose = ConfigUtils.getEulerAngle(parameters, "left_arm_pose");
        rightArmPose = ConfigUtils.getEulerAngle(parameters, "right_arm_pose");
        leftLegPose = ConfigUtils.getEulerAngle(parameters, "left_leg_pose");
        rightLegPose = ConfigUtils.getEulerAngle(parameters, "right_leg_pose");
        headPose = ConfigUtils.getEulerAngle(parameters, "head_pose");
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof ArmorStand)) return;
        ArmorStand armorStand = (ArmorStand)entity;
        EntityEquipment equipment = armorStand.getEquipment();

        if (equipment != null) {
            if (itemInHand != null) equipment.setItemInMainHand(itemInHand);
            if (boots != null) equipment.setBoots(boots);
            if (leggings != null) equipment.setLeggings(leggings);
            if (chestplate != null) equipment.setChestplate(chestplate);
            if (helmet != null) equipment.setHelmet(helmet);
        }
        if (bodyPose != null) armorStand.setBodyPose(bodyPose);
        if (leftArmPose != null) armorStand.setLeftArmPose(leftArmPose);
        if (rightArmPose != null) armorStand.setRightArmPose(rightArmPose);
        if (leftLegPose != null) armorStand.setLeftLegPose(leftLegPose);
        if (rightLegPose != null) armorStand.setRightLegPose(rightLegPose);
        if (headPose != null) armorStand.setHeadPose(headPose);
        if (hasGravity != null) armorStand.setGravity(hasGravity);
        if (isInvisible != null) armorStand.setVisible(isInvisible);
        if (hasArms != null) armorStand.setArms(hasArms);
        if (isSmall != null) armorStand.setSmall(isSmall);
        if (hasBasePlate != null) armorStand.setBasePlate(hasBasePlate);
        if (isMarker != null) armorStand.setMarker(isMarker);
        if (disabledSlots != null) getPlatform().getCompatibilityUtils().setDisabledSlots(armorStand, disabledSlots);
    }
}
