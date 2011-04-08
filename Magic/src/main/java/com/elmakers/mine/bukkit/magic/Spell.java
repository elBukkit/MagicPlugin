package com.elmakers.mine.bukkit.magic;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.magic.dao.SpellVariant;
import com.elmakers.mine.bukkit.persistence.Persistence;
import com.elmakers.mine.bukkit.persistence.dao.MaterialData;
import com.elmakers.mine.bukkit.persistence.dao.MaterialList;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;
import com.elmakers.mine.bukkit.persistence.dao.PlayerData;
import com.elmakers.mine.bukkit.utilities.CSVParser;
import com.elmakers.mine.bukkit.utilities.PluginUtilities;
import com.elmakers.mine.bukkit.utilities.Targeting;

/**
 * 
 * Base class for spells. Handles finding player location, targeting, and other
 * common spell activities.
 * 
 * Original targeting code ported from: HitBlox.java, Ho0ber@gmail.com
 * 
 */
public abstract class Spell implements Comparable<Spell>
{
    static protected CSVParser               csvParser            = new CSVParser();

    protected static HashMap<String, String> defaultMaterialLists = null;
    protected Magic                          magic                = null;

    protected Persistence                    persistence          = null;
    /*
     * The player instance is set prior to onCast being called, so you can use
     * it to reference the current player.
     */
    protected Player                         player               = null;
    protected Targeting                      targeting            = null;

    protected PluginUtilities                utilities            = null;

    /**
     * Used internally to initialize the Spell, do not call.
     * 
     * @param instance
     *            The spells instance
     */
    public void initialize(Player player, Magic magic, PluginUtilities utilities, Persistence persistence)
    {
        this.targeting = new Targeting(player);
        targeting.targetThrough(Material.AIR);
        targeting.targetThrough(Material.WATER);
        targeting.targetThrough(Material.STATIONARY_WATER);

        this.persistence = persistence;
        this.utilities = utilities;
        this.magic = magic;
        this.player = player;
        onLoad();
    }
    
    /**
     * A brief description of this spell.
     * 
     * This is displayed in the in-game help screen, so keep it short.
     * 
     * @return This spells' description.
     */
    public abstract String getDescription();

    /**
     * You must specify a unique name (id) for your spell.
     * 
     * This is also the name of the default variant, used for casting this
     * spell's default behavior.
     * 
     * @return The name of this spell
     */
    public abstract String getName();
    

    /**
     * Called when a material selection spell is cancelled mid-selection.
     */
    public void onCancel()
    {

    }

    /**
     * Called when this spell is cast.
     * 
     * This is where you do your work!
     * 
     * If parameters were passed to this spell, either via a variant or the
     * command line, they will be passed in here.
     * 
     * @param parameters
     *            Any parameters that were passed to this spell
     * @return true if the spell worked, false if it failed
     */
    public abstract boolean onCast(ParameterMap parameters);

    /**
     * Called when a spell is first loaded.
     */
    public void onLoad()
    {

    }
    
    /**
     * Called by the Spells plugin to cancel this spell, do not call.
     */
    public void cancel()
    {
        onCancel();
    }

    /**
     * Called by Spells to cast this spell, do not call.
     * 
     * @param parameters
     * @param player
     * @return true if the spell succeed, false if failed
     * @see Magic#castSpell(SpellVariant, Player)
     */
    public boolean cast(ParameterMap parameters)
    {
        // TODO : Check reagents, cooldown, casting cost, etc
        return onCast(parameters);
    }

    /**
     * 
     * TODO: Integrate with Message, messaging
     * 
     * Send a message to a player when a spell is cast.
     * 
     * Will be replaced with the Message interface from Persistence soon.
     * 
     * Respects the "quiet" and "silent" properties settings.
     * 
     * @param player
     *            The player to send a message to
     * @param message
     *            The message to send
     */
    public void castMessage(Player player, String message)
    {
        // TODO: check for player logging privs
        player.sendMessage(message);
    }

    /*
     * General helper functions
     */

    /*
     * Used for sorting spells
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Spell other)
    {
        return getName().compareTo(other.getName());
    }

    public MaterialData getBuildingMaterial(ParameterMap parameters, Block target)
    {
        ParameterData withParam = parameters.get("with");
        if (withParam != null)
        {
            return new MaterialData(withParam.getMaterial());
        }
        
        ItemStack selected = getSelectedMaterial();
        if (selected != null)
        {
            return new MaterialData(selected);
        }
        
        if (target == null)
        {
            return null;
        }
        
        return new MaterialData(target.getType(), target.getData());
    }
    
    protected ItemStack getSelectedMaterial()
    {
        ItemStack result = null;
        List<Material> buildingMaterials = magic.getBuildingMaterials();
        Inventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();

        result = contents[8];
        boolean isAir = result == null || result.getType() == Material.AIR;
        if (!isAir && buildingMaterials.contains(result.getType()))
        {
            return result;
        }

        if (!isAir && !buildingMaterials.contains(result.getType()))
        {
            return null;
        }

        // Should be air now
        result = null;

        for (int i = 8; i >= 0; i--)
        {
            if (contents[i] == null)
            {
                break;
            }
            Material candidate = contents[i].getType();
            if (buildingMaterials.contains(candidate))
            {
                result = new ItemStack(Material.AIR);
                break;
            }
        }

        return result;
    }

    protected Material getMaterial(String matName, List<Material> materials)
    {
        Material material = Material.AIR;
        StringBuffer simplify = new StringBuffer("_");
        matName = matName.replace(simplify, new StringBuffer(""));
        for (Material checkMat : materials)
        {
            String checkName = checkMat.name().replace(simplify, new StringBuffer(""));
            if (checkName.equalsIgnoreCase(matName))
            {
                material = checkMat;
                break;
            }
        }
        return material;
    }

    public void createDefaultMaterialLists()
    {
        defaultMaterialLists = new HashMap<String, String>();
        
        defaultMaterialLists.put("common", "0,1,2,3,10,11,12,13,87,88");
  }
    
    public MaterialList getMaterialList(String listName, String defaultCSV)
    {       
        MaterialList list = utilities.getMaterialList(listName);
        if (list.size() == 0 && defaultCSV != null && defaultCSV.length() > 0)
        {
            list = csvParser.parseMaterials(listName, defaultCSV);
            persistence.put(list);
        }
        
       return list;
    }
    
    public MaterialList getMaterialList(String listName)
    {       
        if (defaultMaterialLists == null)
        {
            createDefaultMaterialLists();
        }

        String defaultCSV = defaultMaterialLists.get(listName);
        return getMaterialList(listName, defaultCSV);
    }

    /*
     * Time functions
     */

    public String getPermissionNode()
    {
        return "Magic.spells." + getName();
    }

    public Persistence getPersistence()
    {
        return persistence;
    }

    /**
     * Return the in-game server time.
     * 
     * @return server time
     */
    public long getTime()
    {
        return player.getWorld().getTime();
    }

    protected boolean giveMaterial(Material materialType, int amount, short damage, byte data)
    {
        ItemStack itemStack = new ItemStack(materialType, amount, damage, data);
        boolean active = false;
        for (int i = 8; i >= 0; i--)
        {
            ItemStack current = player.getInventory().getItem(i);
            if (current == null || current.getType() == Material.AIR)
            {
                player.getInventory().setItem(i, itemStack);
                active = true;
                break;
            }
        }

        if (!active)
        {
            player.getInventory().addItem(itemStack);
        }

        return true;
    }

    public boolean hasSpellPermission()
    {
        return hasSpellPermission(player);
    }

    public boolean hasSpellPermission(Player player)
    {
        if (player == null)
        {
            return false;
        }
        PlayerData playerData = persistence.get(player.getName(), PlayerData.class);
        if (playerData == null)
        {
            return false;
        }
        return playerData.isSet(getPermissionNode());
    }

    /**
     * Check to see if the player is underwater
     * 
     * @return true if the player is underwater
     */
    public boolean isUnderwater()
    {
        Block playerBlock = targeting.getPlayerBlock();
        playerBlock = playerBlock.getFace(BlockFace.UP);
        return playerBlock.getType() == Material.WATER || playerBlock.getType() == Material.STATIONARY_WATER;
    }

    /**
     * Listener method, called on player maerial selection for registered
     * spells.
     * 
     * @param player
     *            The player that has chosen a material
     * @see Magic#registerEvent(SpellEventType, Spell)
     */
    public void onMaterialChoose(Player player)
    {

    }

    /**
     * Listener method, called on player death for registered spells.
     * 
     * @param event
     *            The original entity death event
     * @see Magic#registerEvent(SpellEventType, Spell)
     */
    public void onPlayerDeath(EntityDeathEvent event)
    {

    }

    /**
     * Listener method, called on player damage for registered spells.
     * 
     * @param event
     *            The original entity damage event
     * @see Magic#registerEvent(SpellEventType, Spell)
     */
    public void onPlayerDamage(EntityDamageEvent event)
    {

    }
    
    /**
     * Listener method, called on player move for registered spells.
     * 
     * @param event
     *            The original player move event
     * @see Magic#registerEvent(SpellEventType, Spell)
     */
    public void onPlayerMove(PlayerMoveEvent event)
    {

    }

    /**
     * Listener method, called on player quit for registered spells.
     * 
     * @param event
     *            The player who just quit
     * @see Magic#registerEvent(SpellEventType, Spell)
     */
    public void onPlayerQuit(PlayerEvent event)
    {

    }

    /**
     * 
     * TODO: Integrate with Message, messaging
     * 
     * Send a message to a player.
     * 
     * Will be replaced with the Message interface from Persistence soon.
     * 
     * Use this to send messages to the player that are important.
     * 
     * Only respects the "silent" properties setting.
     * 
     * @param player
     *            The player to send the message to
     * @param message
     *            The message to send
     */
    public void sendMessage(Player player, String message)
    {
        player.sendMessage(message);
    }

    /**
     * Sets the current server time
     * 
     * @param time
     *            specified server time (0-24000)
     */
    public void setRelativeTime(long time)
    {
        long margin = (time - getTime()) % 24000;

        if (margin < 0)
        {
            margin += 24000;
        }
        player.getWorld().setTime(getTime() + margin);
    }
}
