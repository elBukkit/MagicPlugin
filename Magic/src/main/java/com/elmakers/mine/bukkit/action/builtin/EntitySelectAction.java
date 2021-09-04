package com.elmakers.mine.bukkit.action.builtin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.item.ItemUpdatedCallback;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class EntitySelectAction extends CompoundAction implements GUIAction
{
    private Map<Integer, WeakReference<Entity>> entities;
    private double radius;
    private int limit = 64;
    private boolean active = false;
    private WeakReference<Entity> target = null;

    @Override
    public void deactivated() {
        active = false;
    }

    @Override
    public void dragged(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void clicked(InventoryClickEvent event)
    {
        int slot = event.getRawSlot();
        event.setCancelled(true);
        if (event.getSlotType() == InventoryType.SlotType.CONTAINER)
        {
            target = entities.get(slot);
            Entity entity = target != null ? target.get() : null;
            if (entity != null)
            {
                Mage mage = actionContext.getMage();
                mage.deactivateGUI();
                actionContext.setTargetEntity(entity);
                actionContext.setTargetLocation(entity.getLocation());
                actionContext.playEffects("entity_selected");
            }

            entities.clear();
            active = false;
        }
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        entities = new HashMap<>();
        active = false;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        radius = parameters.getDouble("radius", 32);
        limit = parameters.getInt("limit", 64);
    }

    @Override
    public SpellResult step(CastContext context) {
        if (active) {
            return SpellResult.PENDING;
        }

        Entity targetEntity = target == null ? null : target.get();
        if (targetEntity == null) {
            return SpellResult.NO_TARGET;
        }

        return startActions();
    }

    @Override
    public SpellResult start(CastContext context) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }

        final Location location = context.getLocation();
        List<Entity> allEntities = new ArrayList<>(location.getWorld().getNearbyEntities(location, radius, radius, radius));
        entities.clear();

        Collections.sort(allEntities, new Comparator<Entity>() {
            @Override
            public int compare(Entity entity1, Entity entity2) {
                return Double.compare(location.distanceSquared(entity1.getLocation()), location.distanceSquared(entity2.getLocation()));
            }
        });

        int index = 0;
        for (Entity targetEntity : allEntities) {
            if (targetEntity instanceof LivingEntity && ((LivingEntity)targetEntity).hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;
            if (!context.canTarget(targetEntity)) continue;
            entities.put(index++, new WeakReference<>(targetEntity));
            if (entities.size() >= limit) break;
        }
        if (entities.size() == 0) return SpellResult.NO_TARGET;

        String inventoryTitle = context.getMessage("title", "Select Target");
        int invSize = ((entities.size() + 9) / 9) * 9;
        Inventory displayInventory = CompatibilityLib.getCompatibilityUtils().createInventory(null, invSize, inventoryTitle);
        for (Map.Entry<Integer, WeakReference<Entity>> entry : entities.entrySet())
        {
            Entity targetEntity = entry.getValue().get();
            if (targetEntity == null) continue;

            String displayName = CompatibilityLib.getDeprecatedUtils().getDisplayName(targetEntity);
            final Integer slot = entry.getKey();
            if (slot >= displayInventory.getSize()) continue;
            controller.getSkull(targetEntity, displayName, new ItemUpdatedCallback() {
                @Override
                public void updated(@Nullable ItemStack itemStack) {
                    displayInventory.setItem(slot, itemStack);
                }
            });
        }
        active = true;
        mage.activateGUI(this, displayInventory);

        return SpellResult.NO_ACTION;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        parameters.add("radius");
        parameters.add("limit");
        super.getParameterNames(spell, parameters);
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("radius") || parameterKey.equals("limit")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
