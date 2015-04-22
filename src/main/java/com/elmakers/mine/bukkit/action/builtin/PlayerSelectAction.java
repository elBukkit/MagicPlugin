package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
    private Map<Integer, WeakReference<Player>> players = new HashMap<Integer, WeakReference<Player>>();
    private CastContext context;
    private boolean allowCrossWorld;
    private boolean targetSelf;

    @Override
    public void deactivated() {

    }

    @Override
    public void dragged(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void clicked(InventoryClickEvent event)
    {
        if (context == null) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }
        int slot = event.getSlot();
        event.setCancelled(true);
        if (event.getSlotType() == InventoryType.SlotType.CONTAINER)
        {
            WeakReference<Player> playerReference = players.get(slot);
            Player player = playerReference != null ? playerReference.get() : null;
            if (player != null)
            {
                Mage mage = context.getMage();
                mage.deactivateGUI();
                createActionContext(context);
                actionContext.setTargetEntity(player);
                actionContext.setTargetLocation(player.getLocation());
                super.perform(actionContext);
                actionContext.playEffects("player_selected");
            }

            players.clear();
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        allowCrossWorld = parameters.getBoolean("cross_world", true);
        targetSelf = parameters.getBoolean("target_self", true);
    }

    @Override
    public SpellResult perform(CastContext context) {
		players.clear();
        this.context = context;
        Mage mage = context.getMage();
        MageController controller = context.getController();
		Player player = mage.getPlayer();
		if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }

        List<Player> allPlayers = null;

        if (allowCrossWorld) {
            allPlayers = new ArrayList<Player>(CompatibilityUtils.getOnlinePlayers(controller.getPlugin().getServer()));
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
            if (!targetSelf && targetPlayer == player) continue;
            if (targetPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;
            if (!context.canTarget(targetPlayer)) continue;
            players.put(index++, new WeakReference<Player>(targetPlayer));
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
            ItemStack playerItem = InventoryUtils.getPlayerSkull(targetPlayer, displayName);
            if (!name.equals(displayName))
            {
                ItemMeta meta = playerItem.getItemMeta();
                List<String> lore = new ArrayList<String>();
                lore.add(name);
                meta.setLore(lore);
                playerItem.setItemMeta(meta);
            }
            displayInventory.setItem(entry.getKey(), playerItem);
        }
        mage.activateGUI(this);
        mage.getPlayer().openInventory(displayInventory);

        return SpellResult.CAST;
	}

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        parameters.add("target_self");
        parameters.add("cross_world");
        super.getParameterNames(spell, parameters);
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("target_self") || parameterKey.equals("cross_world")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
