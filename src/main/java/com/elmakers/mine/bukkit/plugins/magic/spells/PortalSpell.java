package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.dao.BoundingBox;
import com.elmakers.mine.bukkit.dao.MaterialList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;
import com.elmakers.mine.bukkit.utilities.CSVParser;
import com.elmakers.mine.bukkit.utilities.PluginProperties;

public class PortalSpell extends Spell
{
    private int             defaultSearchDistance   = 255;
    protected static final String DEFAULT_DESTRUCTIBLES = "0,1,2,3,4,10,11,12,13,14,15,16,21,51,56,78,79,82,87,88,89";
    public static MaterialList destructible          = null;
    public static int                               teleportCooldown = 500;
    
    PlayerPortal    a;
    PlayerPortal    b;
    BlockVector     lastLocation;
    boolean         portalling = false;
    long            lastTeleport = 0;
    
    protected static List<PlayerPortal>    allPortals = new ArrayList<PlayerPortal>();
    
    public class TeleportPlayerTask implements Runnable
    {
        protected Location  targetLocation;
        protected PortalSpell   spell;
        
        public TeleportPlayerTask(Location target, PortalSpell spell)
        {
            this.targetLocation = target;
            this.spell = spell;
        }
        
        public void run()
        {
            spell.startTeleporting();
            spell.getPlayer().teleport(targetLocation);
        }
    }
    
    public class PlayerPortal
    {
        protected Location base;
        protected BlockList portalBlocks;
        protected PlayerPortal target;
        
        public PlayerPortal(Location targetLocation)
        {
            base = targetLocation;
            portalBlocks = new BlockList();
            buildPortalBlocks(base, BlockFace.NORTH, portalBlocks);
            PortalSpell.allPortals.add(this);
        }
        
        protected void buildPortalBlocks(Location centerBlock, BlockFace facing, BlockList blockList)
        {
            BoundingBox container = new BoundingBox(centerBlock.getBlockX(), centerBlock.getBlockY(), centerBlock.getBlockZ(), centerBlock.getBlockX() + 2, centerBlock.getBlockY() + 3, centerBlock.getBlockZ() + 1);
            container.fill(centerBlock.getWorld(), Material.PORTAL, destructible, blockList);
        }
        
        public void remove()
        {
            portalBlocks.undo();
            portalBlocks = null;
            PortalSpell.allPortals.remove(this);
        }
        
        public Location getLocation()
        {
            return base;
        }
        
        public boolean contains(Location location)
        {
            int lx = location.getBlockX();
            int ly = location.getBlockY();
            int lz = location.getBlockZ();
            int bx = base.getBlockX();
            int by = base.getBlockY();
            int bz = base.getBlockZ();
            return lx >= bx - 3 && lx <= bx + 3 && ly >= by && ly <= by + 4 && lz >= bz - 3 && lz <= bz + 3;
        }
        
        public void teleport(Player player, Plugin plugin, PortalSpell spell)
        {
            if (target == null) return;
            
            Location targetLocation = target.getLocation();
            Location playerLocation = player.getLocation();
            Location destination = new Location
            (
                    targetLocation.getWorld(),
                    targetLocation.getX(),
                    targetLocation.getY() + 1,
                    targetLocation.getZ(),
                    playerLocation.getYaw(),
                    playerLocation.getPitch()
            );
            
            Server server = plugin.getServer();
            BukkitScheduler sched = server.getScheduler();
            sched.scheduleSyncDelayedTask(plugin, new TeleportPlayerTask(destination, spell), 0);
        }
        
        public void link(PlayerPortal target)
        {
            this.target = target;
        }
        
        public void unlink()
        {
            this.target = null;
        }
    }

    public void create(Location fromLocation, Location targetLocation)
    {
        if (a == null)
        {
            a = new PlayerPortal(targetLocation);
            relink();
            return;
        }
        if (b == null)
        {
            b = new PlayerPortal(targetLocation);
            relink();
            return;
        }
        
        double dist_a = Spell.getDistance(a.getLocation(), fromLocation);
        double dist_b = Spell.getDistance(b.getLocation(), fromLocation);
        
        if (dist_a < dist_b)
        {
            b.remove();
            b = new PlayerPortal(targetLocation);
        }
        else
        {        
            a.remove();
            a = new PlayerPortal(targetLocation);   
        }
        
        relink();
    }
    
    public void relink()
    {
        if (a != null)
        {
            a.link(b);
        }
        
        if (b != null)
        {
            b.link(a);
        }
    }
    
    public boolean hasPortals()
    {
        return a != null || b != null;
    }
    
    public BlockVector getLastLocation()
    {
        return lastLocation;
    }
    
    public void setLastLocation(BlockVector location)
    {
        lastLocation = location;
    }
    
    public void setPortalling(boolean p)
    {
        portalling = p;
    }
    
    public void startTeleporting()
    {
        portalling = true;
        lastTeleport = System.currentTimeMillis();
    }
    
    public boolean readyForTeleport()
    {
        return !portalling && (lastTeleport == 0 || lastTeleport + teleportCooldown < System.currentTimeMillis());
    }
    
    public boolean isPortalling()
    {
        return portalling;
    }
    
    public PlayerPortal getPortal(Location target)
    {
        if (a != null && a.contains(target)) return a;
        if (b != null && b.contains(target)) return b;
        return null;
    }

	@Override
	public boolean onCast(Map<String, Object> parameters)
	{
	    targetThrough(Material.GLASS);
	    
		Block target = getTargetBlock();
		if (target == null)
		{
			castMessage(player, "No target");
			return false;
		}
		if (defaultSearchDistance > 0 && getDistance(player, target) > defaultSearchDistance)
		{
			castMessage(player, "Can't create a portal that far away");
			return false;
		}
		
		Material blockType = target.getType();
		Block portalBase = target.getFace(BlockFace.UP);
		blockType = portalBase.getType();
		if (blockType != Material.AIR)
		{
			portalBase = getFaceBlock();
		}
		
		blockType = portalBase.getType();
		if (blockType != Material.AIR && blockType != Material.SNOW)
		{
			castMessage(player, "Can't create a portal there");
			return false;		
		}
		
        spells.disablePhysics(10000);
		create(player.getLocation(), portalBase.getLocation());
        
        checkListener();
		
		return true;
	}
	
	public void onLoad(PluginProperties properties)
    {
    	 if (destructible == null)
         {
             destructible = new MaterialList();
             CSVParser csv = new CSVParser();
             destructible = csv.parseMaterials(DEFAULT_DESTRUCTIBLES);
         }
    }
	
	@Override
    public void onPlayerMove(PlayerMoveEvent event)
    {
	    Location playerLocation = player.getLocation();
        BlockVector lastLoc = getLastLocation();
        BlockVector currentLoc = new BlockVector(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ());
        if (lastLoc != null && currentLoc.getBlockX() == lastLoc.getBlockX() && currentLoc.getBlockY() == lastLoc.getBlockY() && currentLoc.getBlockZ() == lastLoc.getBlockZ())
        {
            return;
        }
        
        setLastLocation(currentLoc);
        playerLocation.setY(playerLocation.getY() + 2);
        Block locationBlock = playerLocation.getBlock();
        if (locationBlock.getType() == Material.PORTAL)
        {
            if (!isPortalling())
            {
                if (readyForTeleport())
                {
                    PlayerPortal portal = findPortal(playerLocation);
                    if (portal != null)
                    {
                        portal.teleport(player, spells.getPlugin(), this);
                    }
                }
                setPortalling(true);
             }
        }
        else
        {
            setPortalling(false);
        }
    }
	
	protected PlayerPortal findPortal(Location targetLocation)
	{
	    for (PlayerPortal portal : allPortals)
	    {
	        if (portal.contains(targetLocation)) return portal;
	    }
	    return null;
	}
	
	protected void checkListener()
    {
        if (allPortals.size() == 0)
        {
            spells.unregisterEvent(SpellEventType.PLAYER_MOVE, this);
        }
        else
        {
            spells.registerEvent(SpellEventType.PLAYER_MOVE, this);
        }
    }
}
