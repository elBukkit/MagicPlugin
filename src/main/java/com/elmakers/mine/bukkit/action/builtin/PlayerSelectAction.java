package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.action.GeneralAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.CompoundAction;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerSelectAction extends CompoundAction implements GeneralAction, GUIAction
{
    private Map<Integer, WeakReference<Player>> players = new HashMap<Integer, WeakReference<Player>>();
    private Inventory inventory = null;
    private ConfigurationSection castParameters = null;

    @Override
    public void deactivated() {

    }

    @Override
    public void clicked(InventoryClickEvent event)
    {
        int slot = event.getSlot();
        event.setCancelled(true);
        org.bukkit.Bukkit.getLogger().info("CLick: " + slot + " out of " + players.size());
        if (event.getSlotType() == InventoryType.SlotType.CONTAINER)
        {
            WeakReference<Player> playerReference = players.get(slot);
            Player player = playerReference != null ? playerReference.get() : null;
            Spell baseSpell = getSpell();

            org.bukkit.Bukkit.getLogger().info("player: " + player);

            if (player != null)
            {
                Mage mage = getMage();
                mage.deactivateGUI();

                org.bukkit.Bukkit.getLogger().info("player: " + player);

                super.perform(castParameters, player);
                playEffects("player_selected");
            }

            players.clear();
        }
    }

    @Override
    public SpellResult perform(ConfigurationSection parameters) {
		players.clear();
        castParameters = parameters;
        Mage mage = getMage();
        MageController controller = getController();
		Player player = mage.getPlayer();
		if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }

        boolean allowCrossWorld = parameters.getBoolean("cross_world", true);
        boolean targetSelf = parameters.getBoolean("target_self", true);

        List<Player> allPlayers = allowCrossWorld
                ? Arrays.asList(controller.getPlugin().getServer().getOnlinePlayers())
                : getLocation().getWorld().getPlayers();

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
            if (!canTarget(targetPlayer)) continue;
            players.put(index++, new WeakReference<Player>(targetPlayer));
        }
        if (players.size() == 0) return SpellResult.NO_TARGET;

        String inventoryTitle = getMessage("title", "Select Player");
        int invSize = ((players.size() + 9) / 9) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        for (Map.Entry<Integer, WeakReference<Player>> entry : players.entrySet())
        {
            Player targetPlayer = entry.getValue().get();
            if (targetPlayer == null) continue;

            String name = targetPlayer.getName();
            ItemStack playerItem = InventoryUtils.getPlayerSkull(targetPlayer, name);
            String displayName = targetPlayer.getDisplayName();
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
}
