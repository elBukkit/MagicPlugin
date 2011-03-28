package com.elmakers.mine.bukkit.wands;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.elmakers.mine.bukkit.magic.Magic;
import com.elmakers.mine.bukkit.magic.dao.SpellVariant;

public class Wands
{
    private Magic        magic  = null;

    private final Server server = null;

    public boolean cycleMaterials(Player player)
    {
        List<Material> buildingMaterials = magic.getBuildingMaterials();
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        int firstMaterialSlot = 8;
        boolean foundAir = false;

        for (int i = 8; i >= 0; i--)
        {
            Material mat = contents[i].getType();
            if (mat == Material.AIR)
            {
                if (foundAir)
                {
                    break;
                }
                else
                {
                    foundAir = true;
                    firstMaterialSlot = i;
                    continue;
                }
            }
            else
            {
                if (buildingMaterials.contains(mat))
                {
                    firstMaterialSlot = i;
                    continue;
                }
                else
                {
                    break;
                }
            }
        }

        if (firstMaterialSlot == 8)
        {
            return false;
        }

        ItemStack lastSlot = contents[8];
        for (int i = 7; i >= firstMaterialSlot; i--)
        {
            contents[i + 1] = contents[i];
        }
        contents[firstMaterialSlot] = lastSlot;

        inventory.setContents(contents);
        /*
         * CraftPlayer cPlayer = ((CraftPlayer)player); cPlayer.getHandle().l();
         */
        return true;
    }

    public void cycleSpells(Player player)
    {
        Inventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        ItemStack[] active = new ItemStack[9];

        for (int i = 0; i < 9; i++)
        {
            active[i] = contents[i];
        }

        int maxSpellSlot = 0;
        int firstSpellSlot = -1;
        for (int i = 0; i < 9; i++)
        {
            boolean isWand = active[i].getType() == getWandType();
            boolean isSpell = false;
            if (active[i].getType() != Material.AIR)
            {
                SpellVariant spell = null;// magic.getSpell(active[i].getType(),
                                          // player);
                isSpell = spell != null;
            }

            if (isSpell)
            {
                if (firstSpellSlot < 0)
                {
                    firstSpellSlot = i;
                }
                maxSpellSlot = i;
            }
            else
            {
                if (!isWand && firstSpellSlot >= 0)
                {
                    break;
                }
            }

        }

        int numSpellSlots = firstSpellSlot < 0 ? 0 : maxSpellSlot - firstSpellSlot + 1;

        if (numSpellSlots < 2)
        {
            return;
        }

        for (int ddi = 0; ddi < numSpellSlots; ddi++)
        {
            int i = ddi + firstSpellSlot;
            if (contents[i].getType() != getWandType())
            {
                for (int di = 1; di < numSpellSlots; di++)
                {
                    int dni = (ddi + di) % numSpellSlots;
                    int ni = dni + firstSpellSlot;
                    if (active[ni].getType() != getWandType())
                    {
                        contents[i] = active[ni];
                        break;
                    }
                }
            }
        }

        inventory.setContents(contents);
        /*
         * CraftPlayer cPlayer = ((CraftPlayer)player); cPlayer.getHandle().l();
         */
    }

    protected Material getWandType()
    {
        return Material.STICK;
    }

    /**
     * Called when a player plays an animation, such as an arm swing
     * 
     * @param event
     *            Relevant event details
     */
    public void onPlayerAnimation(PlayerAnimationEvent event)
    {
        /*
         * Player player = event.getPlayer(); if (event.getAnimationType() ==
         * PlayerAnimationType.ARM_SWING) { if
         * (event.getPlayer().getInventory().getItemInHand().getType() ==
         * getWandType()) { Inventory inventory = player.getInventory();
         * ItemStack[] contents = inventory.getContents();
         * 
         * SpellVariant spell = null; for (int i = 0; i < 9; i++) { if
         * (contents[i].getType() == Material.AIR || contents[i].getType() ==
         * getWandType()) { continue; }
         * 
         * spell = magic.getSpell(contents[i].getType(), player); if (spell !=
         * null) { break; } }
         * 
         * if (spell != null) { spell.cast(player)!
         * wands.getSpells().castSpell(spell, player); }
         * 
         * } }
         */
    }

    /**
     * Called when a player uses an item
     * 
     * @param event
     *            Relevant event details
     */
    public void onPlayerItem(PlayerItemEvent event)
    {
        Material equippedItem = event.getPlayer().getInventory().getItemInHand().getType();
        Player player = event.getPlayer();

        boolean cycleSpells = false;

        cycleSpells = player.isSneaking();
        if (equippedItem == getWandType())
        {
            if (cycleSpells)
            {
                if (!cycleMaterials(event.getPlayer()))
                {
                    cycleSpells(event.getPlayer());
                }
            }
            else
            {
                cycleSpells(event.getPlayer());
            }
        }
        else
        {
            spellHelp(event.getPlayer());
        }
    }

    /*
     * Private data
     */

    public void setMagic(Server server, Magic magic)
    {
        this.magic = magic;
    }

    public void spellHelp(Player player)
    {
        // Check for magic item
        /*
         * Inventory inventory = player.getInventory(); ItemStack[] contents =
         * inventory.getContents();
         * 
         * boolean inInventory = false; boolean foundInventory = false;
         * SpellVariant spell = null; boolean hasWand = false;
         * 
         * for (int i = 0; i < 9; i++) { if (contents[i].getType() ==
         * getWandType()) { hasWand = true; continue; }
         * 
         * if (contents[i].getType() != Material.AIR) { SpellVariant ispell =
         * magic.getSpell(contents[i].getType(), player);
         * 
         * if (!foundInventory) { if (!inInventory) { if (ispell != null) {
         * inInventory = true; } } else { if (ispell == null) { inInventory =
         * false; foundInventory = true; } } }
         * 
         * if (inInventory && i == player.getInventory().getHeldItemSlot()) {
         * spell = ispell; } } }
         * 
         * if (hasWand && spell != null) { player.sendMessage(spell.getName() +
         * " : " + spell.getDescription()); }
         */
    }
}
