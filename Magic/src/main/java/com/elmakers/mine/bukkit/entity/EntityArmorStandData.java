package com.elmakers.mine.bukkit.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;

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
    public boolean isMarker;
    public boolean hasGravity;
    public boolean isVisible;
    public boolean hasArms;
    public boolean isSmall;
    public boolean hasBasePlate;
    public int disabledSlots;

    public EntityArmorStandData() {

    }

    public EntityArmorStandData(ArmorStand armorStand) {
        itemInHand = armorStand.getItemInHand();
        if (itemInHand != null) itemInHand = itemInHand.clone();
        boots = armorStand.getBoots();
        if (boots != null) boots = boots.clone();
        leggings = armorStand.getLeggings();
        if (leggings != null) leggings = leggings.clone();
        chestplate = armorStand.getChestplate();
        if (chestplate != null) chestplate = chestplate.clone();
        helmet = armorStand.getHelmet();
        if (helmet != null) helmet = helmet.clone();
        bodyPose = armorStand.getBodyPose();
        leftArmPose = armorStand.getLeftArmPose();
        rightArmPose = armorStand.getRightArmPose();
        leftLegPose = armorStand.getLeftLegPose();
        rightLegPose = armorStand.getRightLegPose();
        headPose = armorStand.getHeadPose();
        hasGravity = armorStand.hasGravity();
        isVisible = armorStand.isVisible();
        hasArms = armorStand.hasArms();
        isSmall = armorStand.isSmall();
        hasBasePlate = armorStand.hasBasePlate();
        isMarker = armorStand.isMarker();
        disabledSlots = CompatibilityUtils.getDisabledSlots(armorStand);
    }

    public EntityArmorStandData(ConfigurationSection parameters) {
        isSmall = parameters.getBoolean("small", false);
        isVisible = !parameters.getBoolean("invisible", false);
        hasBasePlate = !parameters.getBoolean("baseplate", true);
        hasGravity = parameters.getBoolean("gravity", true);
        isMarker = parameters.getBoolean("marker", false);
        hasArms = parameters.getBoolean("arms", false);
        disabledSlots = parameters.getInt("disabled_slots", 0);
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof ArmorStand)) return;
        ArmorStand armorStand = (ArmorStand)entity;

        armorStand.setItemInHand(itemInHand);
        armorStand.setBoots(boots);
        armorStand.setLeggings(leggings);
        armorStand.setChestplate(chestplate);
        armorStand.setHelmet(helmet);
        if (bodyPose != null) armorStand.setBodyPose(bodyPose);
        if (leftArmPose != null) armorStand.setLeftArmPose(leftArmPose);
        if (rightArmPose != null) armorStand.setRightArmPose(rightArmPose);
        if (leftLegPose != null) armorStand.setLeftLegPose(leftLegPose);
        if (rightLegPose != null) armorStand.setRightLegPose(rightLegPose);
        if (headPose != null) armorStand.setHeadPose(headPose);
        armorStand.setGravity(hasGravity);
        armorStand.setVisible(isVisible);
        armorStand.setArms(hasArms);
        armorStand.setSmall(isSmall);
        armorStand.setBasePlate(hasBasePlate);
        armorStand.setMarker(isMarker);
        CompatibilityUtils.setDisabledSlots(armorStand, disabledSlots);
    }
}
