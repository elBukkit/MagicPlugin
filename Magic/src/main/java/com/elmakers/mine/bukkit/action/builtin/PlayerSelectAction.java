package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffectType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerSelectAction extends CompoundAction implements GUIAction
{
    private Map<Integer, WeakReference<Player>> players;
    private boolean allowCrossWorld;
    private boolean active = false;
    private WeakReference<Player> target = null;

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
            target = players.get(slot);
            Player player = target != null ? target.get() : null;
            if (player != null)
            {
                Mage mage = actionContext.getMage();
                mage.deactivateGUI();
                actionContext.setTargetEntity(player);
                actionContext.setTargetLocation(player.getLocation());
                actionContext.playEffects("player_selected");
            }

            players.clear();
            active = false;
        }
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        players = new HashMap<>();
        active = false;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        allowCrossWorld = parameters.getBoolean("cross_world", true);
    }

    @Override
    public SpellResult step(CastContext context) {
        if (active) {
            return SpellResult.PENDING;
        }

        Player targetEntity = target == null ? null : target.get();
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

        List<Player> allPlayers = null;
        players.clear();

        if (allowCrossWorld) {
            allPlayers = new ArrayList<>(controller.getPlugin().getServer().getOnlinePlayers());
        } else {
            allPlayers = context.getLocation().getWorld().getPlayers();
        }

        Collections.sort(allPlayers, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                return p1.getDisplayName().compareTo(p2.getDisplayName());
            }
        });

        int index = 0;
        for (Player targetPlayer : allPlayers) {
            if (!context.getTargetsCaster() && targetPlayer == player) continue;
            if (targetPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;
            if (!context.canTarget(targetPlayer)) continue;
            players.put(index++, new WeakReference<>(targetPlayer));
        }
        if (players.size() == 0) return SpellResult.NO_TARGET;

        String inventoryTitle = context.getMessage("title", "Select Player");
        int invSize = ((players.size() + 9) / 9) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        for (Map.Entry<Integer, WeakReference<Player>> entry : players.entrySet())
        {
            Player targetPlayer = entry.getValue().get();
            if (targetPlayer == null) continue;

            String name = targetPlayer.getName();
            String displayName = targetPlayer.getDisplayName();

            @SuppressWarnings("deprecation") 
            ItemStack playerItem = new ItemStack(Material.SKULL_ITEM, 1, (short)0, (byte)3);
            ItemMeta meta = playerItem.getItemMeta();
            meta.setDisplayName(displayName);
            if (meta instanceof SkullMeta) {
                SkullMeta skullData = (SkullMeta)meta;
                skullData.setOwner(name);
            }
            if (!name.equals(displayName))
            {
                List<String> lore = new ArrayList<>();
                lore.add(name);
                meta.setLore(lore);
            }
            playerItem.setItemMeta(meta);
            displayInventory.setItem(entry.getKey(), playerItem);
        }
        active = true;
        mage.activateGUI(this, displayInventory);

        return SpellResult.NO_ACTION;
	}

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        parameters.add("cross_world");
        super.getParameterNames(spell, parameters);
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("cross_world")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
