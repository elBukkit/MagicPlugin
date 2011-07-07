package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.HashMap;

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

import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.persistence.dao.BoundingBox;
import com.elmakers.mine.bukkit.persistence.dao.MaterialList;
import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.SpellEventType;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;
import com.elmakers.mine.bukkit.utilities.CSVParser;

public class PortalSpell extends Spell
{
    private int             defaultSearchDistance   = 255;
    protected static final String DEFAULT_DESTRUCTIBLES = "0,1,2,3,4,10,11,12,13,14,15,16,21,51,56,78,79,82,87,88,89";
    public static MaterialList destructible          = null;
  
    protected HashMap<String, PlayerPortals>            playerPortals = new HashMap<String, PlayerPortals>();
    
    public class TeleportPlayerTask implements Runnable
    {
        protected Location  targetLocation;
        protected Player    player;
        
        public TeleportPlayerTask(Location target, Player player)
        {
            this.targetLocation = target;
            this.player = player;
        }
        
        public void run()
        {
            player.teleport(targetLocation);
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
        }
        
        protected void buildPortalBlocks(Location centerBlock, BlockFace facing, BlockList blockList)
        {
            BoundingBox container = new BoundingBox(centerBlock.getBlockX() - 1, centerBlock.getBlockY(), centerBlock.getBlockZ() - 1, centerBlock.getBlockX() + 1, centerBlock.getBlockY() + 3, centerBlock.getBlockZ());
            container.fill(centerBlock.getWorld(), Material.PORTAL, destructible, blockList);
        }
        
        public void remove()
        {
            portalBlocks.undo();
            portalBlocks = null;
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
        
        public void teleport(Player player, Plugin plugin)
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
            sched.scheduleSyncDelayedTask(plugin, new TeleportPlayerTask(destination, player), 0);
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
    
    public class PlayerPortals
    {
        PlayerPortal    a;
        PlayerPortal    b;
        BlockVector     lastLocation;
        boolean         portalling;
        
        public PlayerPortals()
        {
            a = null;
            b = null;
            lastLocation = null;
            portalling = false;
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
    }
    
	@Override
	public boolean onCast(String[] parameters)
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
		
		String playerName = player.getName();
		PlayerPortals portals = playerPortals.get(playerName);
		if (portals == null)
		{
		    portals = new PlayerPortals();
		    playerPortals.put(playerName, portals);
		}
		
        spells.disablePhysics(10000);
		portals.create(player.getLocation(), portalBase.getLocation());
        
        if (!portals.hasPortals())
        {
            playerPortals.remove(playerName);
        }
        
        checkListener();
		
		return true;
	}

	@Override
	public String getName()
	{
		return "portal";
	}

	@Override
	public String getCategory()
	{
		return "nether";
	}

	@Override
	public String getDescription()
	{
		return "Create a temporary portal";
	}

	@Override
	public Material getMaterial()
	{
		return Material.PORTAL;
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
	    PlayerPortals portals = playerPortals.get(event.getPlayer().getName());
	    if (portals == null) return;
	    
        Location playerLocation = player.getLocation();
        BlockVector lastLoc = portals.getLastLocation();
        BlockVector currentLoc = new BlockVector(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ());
        if (lastLoc != null && currentLoc.getBlockX() == lastLoc.getBlockX() && currentLoc.getBlockY() == lastLoc.getBlockY() && currentLoc.getBlockZ() == lastLoc.getBlockZ())
        {
            return;
        }
        
        portals.setLastLocation(currentLoc);
        playerLocation.setY(playerLocation.getY() + 2);
        Block locationBlock = playerLocation.getBlock();
        if (locationBlock.getType() == Material.PORTAL)
        {
            if (!portals.isPortalling())
            {
                portals.setPortalling(true);
                PlayerPortal portal = findPortal(playerLocation);
                if (portal != null)
                {
                    portal.teleport(player, spells.getPlugin());
                }
            }
        }
        else
        {
            portals.setPortalling(false);
        }
    }
	
	protected PlayerPortal findPortal(Location targetLocation)
	{
	    for (PlayerPortals portals : playerPortals.values())
	    {
	        PlayerPortal portal = portals.getPortal(targetLocation);
	        if (portal != null) return portal;
	    }
	    return null;
	}
	
	protected void checkListener()
    {
        if (playerPortals.size() == 0)
        {
            spells.unregisterEvent(SpellEventType.PLAYER_MOVE, this);
        }
        else
        {
            spells.registerEvent(SpellEventType.PLAYER_MOVE, this);
        }
    }
}
