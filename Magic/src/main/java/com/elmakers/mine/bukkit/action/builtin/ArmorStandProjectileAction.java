package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;

import de.slikey.effectlib.math.VectorTransform;

public class ArmorStandProjectileAction extends EntityProjectileAction {
    public enum Slot {
        HELMET, RIGHT_ARM, CHESTPLATE, LEGGINGS, BOOTS
    }

    private boolean smallArmorStand = false;
    private boolean armorStandMarker = false;
    private boolean armorStandInvisible = false;
    private boolean armorStandGravity = false;
    private boolean adjustArmPitch = false;
    private boolean adjustHeadPitch = false;
    private boolean showArmorStandArms = true;
    private boolean showArmorStandBaseplate = false;
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
    private int visibleDelayTicks = 1;

    private Slot wandSlot;
    private boolean useWand;
    private ItemStack wandItem;
    private int slotNumber;

    private int stepCount = 0;

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
        parameters.set("type", "armor_stand");
        super.prepare(context, parameters);

        armorStandMarker = parameters.getBoolean("armor_stand_marker", true);
        armorStandInvisible = parameters.getBoolean("armor_stand_invisible", true);
        armorStandGravity = parameters.getBoolean("armor_stand_gravity", false);
        showArmorStandArms = parameters.getBoolean("armor_stand_arms", true);
        showArmorStandBaseplate = parameters.getBoolean("armor_stand_baseplate", false);
        smallArmorStand = parameters.getBoolean("armor_stand_small", false);
        adjustHeadPitch = parameters.getBoolean("orient_head", false);
        adjustArmPitch = parameters.getBoolean("orient_right_arm", false);
        unbreakableItems = parameters.getBoolean("unbreakable_items", false);
        visibleDelayTicks = parameters.getInt("visible_delay_ticks", 1);
        useWand = parameters.getBoolean("mount_wand", false);

        String wandSlotString = parameters.getString("wand_slot", "HELMET");
        try {
            wandSlot = Slot.valueOf(wandSlotString.toUpperCase());
        } catch (Exception ex) {
            context.getLogger().warning("Invalid wand slot: " + wandSlotString);
        }

        MageController controller = context.getController();
        ItemData itemType = controller.getOrCreateItemOrWand(parameters.getString("right_arm_item"));
        if (itemType != null) {
            rightArmItem = itemType.getItemStack(1);
            if (rightArmItem != null && unbreakableItems) {
                InventoryUtils.makeUnbreakable(rightArmItem);
            }
        }
        itemType = controller.getOrCreateItemOrWand(parameters.getString("helmet_item"));
        if (itemType != null) {
            helmetItem = itemType.getItemStack(1);
            if (helmetItem != null && unbreakableItems) {
                InventoryUtils.makeUnbreakable(InventoryUtils.makeReal(helmetItem));
            }
        }
        itemType = controller.getOrCreateItemOrWand(parameters.getString("chestplate_item"));
        if (itemType != null) {
            chestplateItem = itemType.getItemStack(1);
            if (chestplateItem != null && unbreakableItems) {
                InventoryUtils.makeUnbreakable(InventoryUtils.makeReal(chestplateItem));
            }
        }
        itemType = controller.getOrCreateItemOrWand(parameters.getString("leggings_item"));
        if (itemType != null) {
            leggingsItem = itemType.getItemStack(1);
            if (leggingsItem != null && unbreakableItems) {
                InventoryUtils.makeUnbreakable(InventoryUtils.makeReal(leggingsItem));
            }
        }
        itemType = controller.getOrCreateItemOrWand(parameters.getString("boots_item"));
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
        Location location = adjustLocation(sourceLocation.getLocation(context));
        ArmorStand armorStand = (ArmorStand)setEntity(controller, CompatibilityUtils.createArmorStand(location));
        armorStand.setMarker(armorStandMarker);
        armorStand.setVisible(!armorStandInvisible);
        armorStand.setBasePlate(showArmorStandBaseplate);
        armorStand.setGravity(armorStandGravity);
        armorStand.setSmall(smallArmorStand);
        armorStand.setArms(showArmorStandArms);
        CompatibilityUtils.setSilent(armorStand, true);
        CompatibilityUtils.setDisabledSlots(armorStand, 2039552);
        update(armorStand);
        CompatibilityUtils.addToWorld(location.getWorld(), armorStand, spawnReason);

        return super.start(context);
    }

    protected void update(ArmorStand armorStand) {
        double t = (double)flightTime / 1000;
        Location currentLocation = armorStand.getLocation();

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
            headPose = headPose.setX(Math.toRadians(currentLocation.getPitch()));
            armorStand.setHeadPose(headPose);
        }
    }

    @Override
    public SpellResult step(CastContext context) {
        SpellResult result = super.step(context);
        if (entity == null) {
            return SpellResult.FAIL;
        }
        Player player = context.getMage().getPlayer();
        if (player == null && useWand) {
            return SpellResult.PLAYER_REQUIRED;
        }
        ArmorStand armorStand = (ArmorStand)entity;
        update(armorStand);
        if (useWand && wandItem == null) {
            Wand wand = context.getWand();
            if (wand == null) {
                return SpellResult.NO_TARGET;
            }
            wand.deactivate();

            wandItem = wand.getItem();
            if (wandItem == null || wandItem.getType() == Material.AIR) {
                return SpellResult.FAIL;
            }
            slotNumber = wand.getHeldSlot();
            player.getInventory().setItem(slotNumber, new ItemStack(Material.AIR));
        }
        if (stepCount == visibleDelayTicks) {
            if (wandItem != null && wandSlot == Slot.HELMET) {
                armorStand.setHelmet(wandItem);
            } else {
                armorStand.setHelmet(helmetItem);
            }
            if (wandItem != null && wandSlot == Slot.RIGHT_ARM) {
                armorStand.setItemInHand(wandItem);
            } else {
                armorStand.setItemInHand(rightArmItem);
            }
            if (wandItem != null && wandSlot == Slot.CHESTPLATE) {
                armorStand.setChestplate(wandItem);
            } else {
                armorStand.setChestplate(chestplateItem);
            }
            if (wandItem != null && wandSlot == Slot.LEGGINGS) {
                armorStand.setLeggings(wandItem);
            } else {
                armorStand.setLeggings(leggingsItem);
            }
            if (wandItem != null && wandSlot == Slot.BOOTS) {
                armorStand.setBoots(wandItem);
            } else {
                armorStand.setBoots(bootsItem);
            }
        }
        stepCount++;
        return result;
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);

        Mage mage = context.getMage();
        Player player = mage.getPlayer();
        if (player == null || wandItem == null) return;

        ItemStack currentItem = player.getInventory().getItem(slotNumber);
        if (currentItem != null || mage.hasStoredInventory()) {
            mage.giveItem(wandItem);
        } else {
            player.getInventory().setItem(slotNumber, wandItem);
        }
        mage.checkWand();

        wandItem = null;
    }

    @Override
    public void reset(CastContext context)
    {
        super.reset(context);
        stepCount = 0;
    }
}
