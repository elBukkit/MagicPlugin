package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MountArmorStandAction extends RideEntityAction
{
    private boolean armorStandInvisible;
    private boolean armorStandInvulnerable;
    private boolean armorStandSmall;
    private boolean armorStandMarker;
    private boolean armorStandGravity;
    private boolean mountWand;
    private boolean findWand;
    private double armorStandPitch = 0;
    private double armorStandRoll = 0;
    private double armorStandHealth = 0;
    private ItemStack helmetItem;
    private CreatureSpawnEvent.SpawnReason armorStandSpawnReason = CreatureSpawnEvent.SpawnReason.CUSTOM;

    private ItemStack item;
    private ItemStack replacementItem;
    private int slotNumber;
    private boolean mountTarget = false;
    private String mountName;

    @Override
    public void reset(CastContext context)
    {
        Entity mount = this.mount;
        super.reset(context);
        item = null;
        if (mount != null && !mountTarget) {
            mount.remove();
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        MageController controller = context.getController();
        noTarget = parameters.getBoolean("mount_untargetable", true);
        mountTarget = parameters.getBoolean("mount_target", false);
        armorStandInvisible = parameters.getBoolean("armor_stand_invisible", true);
        armorStandInvulnerable = parameters.getBoolean("armor_stand_invulnerable", true);
        armorStandSmall = parameters.getBoolean("armor_stand_small", false);
        armorStandMarker = parameters.getBoolean("armor_stand_marker", true);
        armorStandGravity = parameters.getBoolean("armor_stand_gravity", true);
        armorStandPitch = parameters.getDouble("armor_stand_pitch", 0.0);
        armorStandRoll = parameters.getDouble("armor_stand_roll", 0.0);
        armorStandHealth = parameters.getDouble("armor_stand_health", 0.1);
        mountWand = parameters.getBoolean("mount_wand", false);
        findWand = parameters.getBoolean("find_wand", false);
        mountName = parameters.getString("mount_name", null);
        ItemData replacementType = controller.getOrCreateItem(parameters.getString("replacement_item"));
        if (replacementType != null) {
            replacementItem = replacementType.getItemStack(1);
        }
        if (parameters.contains("armor_stand_reason")) {
            String reasonText = parameters.getString("armor_stand_reason").toUpperCase();
            try {
                armorStandSpawnReason = CreatureSpawnEvent.SpawnReason.valueOf(reasonText);
            } catch (Exception ex) {
                context.getMage().sendMessage("Unknown spawn reason: " + reasonText);
            }
        }

        ItemData itemType = controller.getOrCreateItem(parameters.getString("helmet_item"));
        if (itemType != null) {
            helmetItem = itemType.getItemStack(1);
            if (helmetItem != null) {
                CompatibilityLib.getItemUtils().makeUnbreakable(CompatibilityLib.getItemUtils().makeReal(helmetItem));
            }
        }
    }

    @Override
    protected void remount(CastContext context) {
        if (mountTarget) {
            return;
        }

        // This seems to happen occasionally... guess we'll work around it for now.
        // TODO: Remove this, was an issue with how I was spawning armor stands and should no
        // longer be a problem.
        if (mount != null) {
            mount.remove();
        }
        if (mountNewArmorStand(context)) {
            mount = context.getTargetEntity();
        }
    }

    @Override
    protected void adjustHeading(CastContext context) {
        super.adjustHeading(context);

        float targetPitch = targetLocation.getPitch();
        if (armorStandPitch != 0 || armorStandRoll != 0) {
            double pitch = armorStandPitch * targetPitch / 180 * Math.PI;
            double roll = 0;
            if (armorStandRoll != 0) {
                double strafeDirection = context.getMage().getVehicleStrafeDirection();
                roll = armorStandRoll * strafeDirection;
            }

            ArmorStand armorStand = (ArmorStand)mount;
            armorStand.setHeadPose(new EulerAngle(pitch, 0, roll));
        }
    }

    @Override
    protected SpellResult mount(CastContext context) {
        Mage mage = context.getMage();
        Player player = mage.getPlayer();
        if (player == null && mountWand)
        {
            return SpellResult.PLAYER_REQUIRED;
        }

        item = null;
        if (mountWand) {
            Wand wand = context.getWand();
            if (findWand) {
                wand = null;
                MageController controller = context.getController();
                ItemStack[] items = player.getInventory().getContents();
                for (int i = 0; i < items.length; i++) {
                    ItemStack candidateItem = items[i];
                    if (controller.isWand(candidateItem)) {
                        Wand candidateWand = controller.getWand(candidateItem);
                        String spellKey = candidateWand.getActiveSpellKey();
                        if (spellKey != null && spellKey.equals(context.getSpell().getSpellKey().getBaseKey())) {
                            wand = candidateWand;
                            wand.setHeldSlot(i);
                            break;
                        }
                    }
                }
            }
            if (wand == null) {
                return SpellResult.NO_TARGET;
            }
            wand.deactivate();

            item = wand.getItem();
            if (item == null || item.getType() == Material.AIR)
            {
                return SpellResult.FAIL;
            }
            slotNumber = wand.getHeldSlot();
        }

        if (!mountTarget && !mountNewArmorStand(context)) {
            return SpellResult.FAIL;
        }
        if (mountWand) {
            ItemStack replacement = replacementItem == null ? new ItemStack(Material.AIR) : replacementItem;
            player.getInventory().setItem(slotNumber, replacement);
        }

        SpellResult result = super.mount(context);;
        if (mount == null || !(mount instanceof ArmorStand)) {
            result = SpellResult.FAIL;
        }
        return result;
    }

    protected boolean mountNewArmorStand(CastContext context) {
        Mage mage = context.getMage();
        Entity entity = context.getEntity();
        ArmorStand armorStand = CompatibilityLib.getCompatibilityUtils().createArmorStand(mage.getLocation());

        armorStand.setHealth(armorStandHealth);
        CompatibilityLib.getCompatibilityUtils().setMaxHealth(armorStand, armorStandHealth);
        if (armorStandInvisible) {
            CompatibilityLib.getCompatibilityUtils().setInvisible(armorStand, true);
        }
        if (armorStandInvulnerable) {
            CompatibilityLib.getCompatibilityUtils().setInvulnerable(armorStand, true);
        }
        if (armorStandMarker) {
            armorStand.setMarker(true);
        }
        if (!armorStandGravity) {
            armorStand.setGravity(false);
        }
        CompatibilityLib.getCompatibilityUtils().setDisabledSlots(armorStand, 2039552);
        if (armorStandSmall) {
            armorStand.setSmall(true);
        }
        CompatibilityLib.getCompatibilityUtils().setPersist(armorStand, false);

        MageController controller = context.getController();
        controller.setForceSpawn(true);
        try {
            CompatibilityLib.getCompatibilityUtils().addToWorld(entity.getWorld(), armorStand, armorStandSpawnReason);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        controller.setForceSpawn(false);

        if (mountWand) {
            armorStand.getEquipment().setHelmet(item);
        } else if (helmetItem != null) {
            armorStand.getEquipment().setHelmet(helmetItem);
        }
        if (mountName != null && !mountName.isEmpty()) {
            armorStand.setCustomName(mountName);
        }
        context.setTargetEntity(armorStand);

        return true;
    }

    @Override
    public void finish(CastContext context) {
        if (!mountTarget && mount != null) {
            mount.remove();
        } else if (mount != null && (armorStandPitch != 0 || armorStandRoll != 0)) {
            ArmorStand armorStand = (ArmorStand)mount;
            armorStand.setHeadPose(new EulerAngle(0, 0, 0));
        }

        super.finish(context);

        Mage mage = context.getMage();
        Player player = mage.getPlayer();
        if (player == null || item == null) return;

        ItemStack currentItem = mage.getItem(slotNumber);
        if (replacementItem != null && context.getController().itemsAreEqual(currentItem, replacementItem)) {
            mage.setItem(slotNumber, item);
        } else if (currentItem != null || mage.hasStoredInventory() || player.isDead()) {
            mage.giveItem(item);
        } else {
            player.getInventory().setItem(slotNumber, item);
        }
        context.checkWand();

        item = null;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("armor_stand_invisible");
        parameters.add("armor_stand_invulnerable");
        parameters.add("armor_stand_small");
        parameters.add("armor_stand_marker");
        parameters.add("armor_stand_gravity");
        parameters.add("armor_stand_reason");
        parameters.add("armor_stand_pitch");
        parameters.add("mount_wand");
        parameters.add("mount_target");
        parameters.add("replacement_item");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("armor_stand_invisible")
                || parameterKey.equals("armor_stand_marker")
                || parameterKey.equals("armor_stand_small")
                || parameterKey.equals("armor_stand_gravity")
                || parameterKey.equals("armor_stand_invulnerable")
                || parameterKey.equals("armor_stand_invisible")
                || parameterKey.equals("mount_target")
                || parameterKey.equals("mount_wand")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else if (parameterKey.equals("armor_stand_pitch")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_VECTOR_COMPONENTS));
        } else if (parameterKey.equals("armor_stand_reason")) {
            for (CreatureSpawnEvent.SpawnReason reason : CreatureSpawnEvent.SpawnReason.values()) {
                examples.add(reason.name().toLowerCase());
            }
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
