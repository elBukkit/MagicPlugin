package com.elmakers.mine.bukkit.action.builtin;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class ItemProjectileAction extends EntityProjectileAction {
    private boolean useWand;
    private boolean activeWandIcon;
    private boolean reactivateWand;
    private boolean useBrushItem;
    private double scale;
    private boolean unbreakableItems = false;

    private int visibleDelayTicks = 1;
    private int stepCount = 0;

    private boolean openSpellInventory;
    private ItemStack wandItem;
    private ItemStack useItem;
    private int slotNumber;

    private ItemData item;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);
    }

    @Override
    protected boolean teleportByDefault() {
        // Velocity doesn't work on display entities
        return true;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        parameters.set("type", "armor_stand");
        super.prepare(context, parameters);

        useWand = parameters.getBoolean("mount_wand", parameters.getBoolean("use_wand", false));
        activeWandIcon = parameters.getBoolean("active_wand", false);
        reactivateWand = parameters.getBoolean("reactivate_wand", false);
        useBrushItem = parameters.getBoolean("use_brush", false);
        unbreakableItems = parameters.getBoolean("unbreakable_items", false);
        visibleDelayTicks = parameters.getInt("visible_delay_ticks", 0);
        scale = parameters.getDouble("scale", 1);

        MageController controller = context.getController();
        item = controller.getOrCreateItem(parameters.getString("item"));
    }

    @Override
    public SpellResult start(CastContext context) {
        MageController controller = context.getController();
        if (!checkItems(context)) {
            return SpellResult.FAIL;
        }
        ItemStack item = visibleDelayTicks > 0 ? new ItemStack(Material.AIR) : getItem(this.item);
        Location location = adjustStartLocation(sourceLocation.getLocation(context));
        Entity entity = CompatibilityLib.getCompatibilityUtils().createItemDisplayEntity(location, item, scale);
        if (entity == null) {
            return SpellResult.FAIL;
        }
        setEntity(controller, entity);
        return super.start(context);
    }

    @Override
    public SpellResult step(CastContext context) {
        SpellResult result = super.step(context);
        if (entity == null) {
            return SpellResult.FAIL;
        }
        if (visibleDelayTicks > 0 && stepCount == visibleDelayTicks) {
            ItemDisplay itemDisplay = (ItemDisplay)entity;
            itemDisplay.setItemStack(getItem(item));
        }
        stepCount++;
        return result;
    }

    private boolean checkItems(CastContext context) {
        Player player = context.getMage().getPlayer();
        if (useBrushItem) {
            MaterialBrush brush = context.getBrush();
            if (brush == null || !brush.isValid()) {
                return false;
            }
            ItemData brushItem = context.getController().getOrCreateItem(brush.getKey());
            if (brushItem == null) {
                return false;
            }
            item = brushItem;
        } else if (useWand && wandItem == null && player != null) {
            Wand wand = context.getWand();
            if (wand == null) {
                return false;
            }
            openSpellInventory = reactivateWand && wand.isInventoryOpen();
            wand.deactivate();

            wandItem = wand.getItem();
            if (wandItem == null || wandItem.getType() == Material.AIR) {
                return false;
            }
            useItem = wandItem;
            if (activeWandIcon) {
                useItem = wand.getIcon().getItemStack(1);
            }
            slotNumber = wand.getHeldSlot();
            player.getInventory().setItem(slotNumber, new ItemStack(Material.AIR));
        }
        return true;
    }

    @Nullable
    private ItemStack getItem(ItemData itemData) {
        ItemStack itemStack = null;
        if (itemData != null) {
            itemStack = itemData.getItemStack(1);
            if (itemStack != null && unbreakableItems) {
                CompatibilityLib.getItemUtils().makeUnbreakable(CompatibilityLib.getItemUtils().makeReal(itemStack));
            }
        }
        return itemStack;
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
        Wand wand = context.checkWand();
        if (openSpellInventory && wand != null) {
            wand.openInventory();
        }

        wandItem = null;
    }
}
