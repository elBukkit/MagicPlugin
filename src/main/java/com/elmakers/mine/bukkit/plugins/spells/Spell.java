package com.elmakers.mine.bukkit.plugins.spells;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.persisted.Persistence;
import com.elmakers.mine.bukkit.persistence.dao.PlayerData;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;
import com.elmakers.mine.bukkit.utilities.BlockAction;
import com.elmakers.mine.bukkit.utilities.PluginUtilities;

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
	/*
	 * protected members that are helpful to use
	 */
	protected Player						player;
	protected Spells						spells;

	/**
	 * Called when this spell is cast.
	 * 
	 * This is where you do your work!
	 * 
	 * If parameters were passed to this spell, either via a variant or the command line,
	 * they will be passed in here.
	 * 
	 * @param parameters Any parameters that were passed to this spell
	 * @return true if the spell worked, false if it failed
	 */
	public abstract boolean onCast(String[] parameters);

	/**
	 * You must specify a unique name (id) for your spell.
	 * 
	 * This is also the name of the default variant, used for casting this spell's default behavior.
	 * 
	 * @return The name of this spell
	 */
	public abstract String getName();

	/**
	 * You must specify a category for this spell.
	 * 
	 * This is used for grouping spells when displaying the in-game spell explorer, and can
	 * be used for permissions as well.
	 * 
	 * Check the builtins spells for examples of common categories.
	 * 
	 * @return This spell's category.
	 */
	public abstract String getCategory();

	/**
	 * A brief description of this spell.
	 * 
	 * This is displayed in the in-game help screen, so keep it short.
	 * 
	 * @return This spells' description.
	 */
	public abstract String getDescription();
	
	/**
	 * The material used to represent this spell.
	 * 
	 * This will probably be dropped from this interface and managed by Wand in the future.
	 * 
	 * @return The material used to represent this spell's icon in the Wand UI.
	 */
	public abstract Material getMaterial();


	/**
	 * Called on load, you can load data here and set defaults.
	 * 
	 * This will be modified to use Persistence for loading in the future.
	 * 
	 * @param properties The spells properties file.
	 */
	public void onLoad(PluginProperties properties)
	{

	}

	/**
	 * Called when a material selection spell is cancelled mid-selection.
	 */
	public void onCancel()
	{

	}

	/**
	 * Listener method, called on player move for registered spells.
	 * 
	 * @param event The original player move event
	 * @see Spells#registerEvent(SpellEventType, Spell)
	 */
	public void onPlayerMove(PlayerMoveEvent event)
	{
		
	}
	
	/**
	 * Listener method, called on player maerial selection for registered spells.
	 * 
	 * @param player The player that has chosen a material
	 * @see Spells#registerEvent(SpellEventType, Spell)
	 */
	public void onMaterialChoose(Player player)
	{
		
	}
	
	/**
	 * Listener method, called on player quit for registered spells.
	 * 
	 * @param event The player who just quit
	 * @see Spells#registerEvent(SpellEventType, Spell)
	 */
	public void onPlayerQuit(PlayerQuitEvent event)
	{

	}
	
	/**
	 * Listener method, called on player move for registered spells.
	 * 
	 * @param player The player that died
	 * @param event The original entity death event
	 * @see Spells#registerEvent(SpellEventType, Spell)
	 */
	public void onPlayerDeath(Player player, EntityDeathEvent event)
	{

	}
	

	/**
	 * Default constructor, used to register spells.
	 * 
	 * Override this constructor to add new default variants.
	 */
	public Spell()
	{
		variants.add(new SpellVariant(this));
	}
	
	public static byte getItemData(ItemStack stack)
	{
		if (stack == null) return 0;
		return (byte)stack.getDurability();
	}
	
	/*
	 * General helper functions
	 */
	public ItemStack getBuildingMaterial()
	{
		ItemStack result = null;
		List<Material> buildingMaterials = spells.getBuildingMaterials();
		Inventory inventory = player.getInventory();
		ItemStack[] contents = inventory.getContents();
		
		result = contents[8];
		boolean isAir = result == null || result.getType() == Material.AIR;
		if (!isAir && buildingMaterials.contains(result.getType()))
		{
			return result;
		}
		
		// Should be air now
		result = null;
		
		// Check for other building materials- if the second-to-last material is a building
		// material, then return air- else return null.
		if (contents[7] == null || contents[7].getType() == Material.AIR) return null;
		
		return new ItemStack(Material.AIR);
	}
	
	public void targetEntity(Class<? extends Entity> typeOf)
	{
	    targetEntityType = typeOf;
	}

	public void targetThrough(Material mat)
	{
		targetThroughMaterials.put(mat, true);
	}

	public void noTargetThrough(Material mat)
	{
		targetThroughMaterials.put(mat, false);
	}
	
	public boolean isTargetable(Material mat)
	{
		Boolean checkMat = targetThroughMaterials.get(mat);
		if (reverseTargeting)
		{
			return(checkMat != null && checkMat);
		}
		return (checkMat == null || !checkMat);
	}

	public void setReverseTargeting(boolean reverse)
	{
		reverseTargeting = reverse;
	}
	
	public boolean isReverseTargeting()
	{
		return reverseTargeting;
	}
	
	public void setTargetHeightRequired(int height)
	{
		targetHeightRequired = height;
	}
	
	public int getTargetHeightRequired()
	{
		return targetHeightRequired;
	}
	
	/*
	 * Ground / location search and test function functions
	 */
	public boolean isOkToStandIn(Material mat)
	{
		return 
		(
		        mat == Material.AIR 
		||    mat == Material.WATER 
		||    mat == Material.STATIONARY_WATER 
		||    mat == Material.SNOW
		||    mat == Material.TORCH
		||    mat == Material.SIGN_POST
		||    mat == Material.REDSTONE_TORCH_ON
        ||    mat == Material.REDSTONE_TORCH_OFF
        ||    mat == Material.YELLOW_FLOWER
        ||    mat == Material.RED_ROSE
        ||    mat == Material.RED_MUSHROOM
        ||    mat == Material.BROWN_MUSHROOM
        ||    mat == Material.LONG_GRASS
		);
	}
	
	public boolean isWater(Material mat)
	{
	    return (mat == Material.WATER || mat == Material.STATIONARY_WATER);
	}

	public boolean isOkToStandOn(Material mat)
	{
		// Added snow here to avoid blink bounciness
		return (mat != Material.AIR && mat != Material.LAVA && mat != Material.STATIONARY_LAVA);
	}
	
	public Location findPlaceToStand(Location playerLoc, boolean goUp)
	{
		int step;
		if (goUp)
		{
			step = 1;
		}
		else
		{
			step = -1;
		}

		// get player position
		int x = (int) Math.round(playerLoc.getX() - 0.5);
		int y = (int) Math.round(playerLoc.getY() + step + step);
		int z = (int) Math.round(playerLoc.getZ() - 0.5);

		World world = player.getWorld();

		// search for a spot to stand
		while (4 < y && y < 125)
		{
			Block block = world.getBlockAt(x, y, z);
			Block blockOneUp = world.getBlockAt(x, y + 1, z);
			Block blockTwoUp = world.getBlockAt(x, y + 2, z);
			if 
			(
				isOkToStandOn(block.getType())
			&&	isOkToStandIn(blockOneUp.getType())
			&& 	isOkToStandIn(blockTwoUp.getType())
			&&   (!goUp || !isUnderwater() || !isWater(blockOneUp.getType())) // rise to surface of water
			)
			{
				// spot found - return location
				return new Location(world, (double) x + 0.5, (double) y + 1, (double) z + 0.5, playerLoc.getYaw(),
						playerLoc.getPitch());
		    
			}
			y += step;
		}

		// no spot found
		return null;
	}
	
	public static double getDistance(Location source, Location target)
	{
		return Math.sqrt
		(
			Math.pow(source.getX() - target.getX(), 2) 
		+ 	Math.pow(source.getY() - target.getY(), 2)
		+ 	Math.pow(source.getZ() - target.getZ(), 2)
		);
	}
	
	public static double getDistance(Player player, Block target)
	{
		Location loc = player.getLocation();
		return Math.sqrt
		(
			Math.pow(loc.getX() - target.getX(), 2) 
		+ 	Math.pow(loc.getY() - target.getY(), 2)
		+ 	Math.pow(loc.getZ() - target.getZ(), 2)
		);
	}

	/**
	 * Get the block the player is standing on.
	 * 
	 * @return The Block the player is standing on
	 */
	public Block getPlayerBlock()
	{
		Block playerBlock = null;
		Location playerLoc = player.getLocation();
		int x = (int) Math.round(playerLoc.getX() - 0.5);
		int y = (int) Math.round(playerLoc.getY() - 0.5);
		int z = (int) Math.round(playerLoc.getZ() - 0.5);
		int dy = 0;
		while (dy > -3 && (playerBlock == null || isOkToStandIn(playerBlock.getType())))
		{
			playerBlock = player.getWorld().getBlockAt(x, y + dy, z);
			dy--;
		}
		return playerBlock;
	}

	
	/**
	 * Get the direction the player is facing as a BlockFace.
	 * 
	 * @return a BlockFace representing the direction the player is facing
	 */
	public BlockFace getPlayerFacing()
	{
		float playerRot = getPlayerRotation();

		BlockFace direction = BlockFace.NORTH;
		if (playerRot <= 45 || playerRot > 315)
		{
			direction = BlockFace.WEST;
		}
		else if (playerRot > 45 && playerRot <= 135)
		{
			direction = BlockFace.NORTH;
		}
		else if (playerRot > 135 && playerRot <= 225)
		{
			direction = BlockFace.EAST;
		}
		else if (playerRot > 225 && playerRot <= 315)
		{
			direction = BlockFace.SOUTH;
		}

		return direction;
	}
	

	/**
	 * A helper function to go change a given direction to the direction "to the right".
	 * 
	 * There's probably some better matrix-y, math-y way to do this.
	 * It'd be nice if this was in BlockFace.
	 * 
	 * @param direction The current direction
	 * @return The direction to the left
	 */
	public static BlockFace goLeft(BlockFace direction)
	{
		switch (direction)
		{
			case EAST:
				return BlockFace.NORTH;
			case NORTH:
				return BlockFace.WEST;
			case WEST:
				return BlockFace.SOUTH;
			case SOUTH:
				return BlockFace.EAST;
		}
		return direction;
	}

	/**
	 * A helper function to go change a given direction to the direction "to the right".
	 * 
	 * There's probably some better matrix-y, math-y way to do this.
	 * It'd be nice if this was in BlockFace.
	 * 
	 * @param direction The current direction
	 * @return The direction to the right
	 */
	public static BlockFace goRight(BlockFace direction)
	{
		switch (direction)
		{
			case EAST:
				return BlockFace.SOUTH;
			case SOUTH:
				return BlockFace.WEST;
			case WEST:
				return BlockFace.NORTH;
			case NORTH:
				return BlockFace.EAST;
		}
		return direction;
	}
	

	/**
	 * Find a good location to spawn a projectile, such as a fireball.
	 * 
	 * @return The projectile spawn location
	 */
	protected Location getProjectileSpawnLocation()
	{
		Block spawnBlock = getPlayerBlock();

		int height = 2;
		double hLength = 2;
		double xOffset = (hLength * Math.cos(Math.toRadians(xRotation)));
		double zOffset = (hLength * Math.sin(Math.toRadians(xRotation)));

		Vector aimVector = new Vector(xOffset + 0.5, height + 0.5, zOffset + 0.5);

		Location location = new Location(player.getWorld(), spawnBlock.getX() + aimVector.getX(), spawnBlock.getY()
				+ aimVector.getY(), spawnBlock.getZ() + aimVector.getZ(), player.getLocation().getYaw(), player
				.getLocation().getPitch());

		return location;
	}

	/**
	 * Get a Vector reprsenting the current aim direction
	 * 
	 * @return The player's aim vector
	 */
	public Vector getAimVector()
	{
		return new Vector
		(
	        (0 - Math.sin(Math.toRadians(playerLocation.getYaw()))), 
	        (0 - Math.sin(Math.toRadians(playerLocation.getPitch()))), 
	        Math.cos(Math.toRadians(playerLocation.getYaw()))
		);
	}

	
	/**
	 * Get the (simplified) player pitch.
	 * 
	 * @return Player Y-axis rotation (pitch)
	 */
	public double getYRotation()
	{
		return yRotation;
	}
	
	/**
	 * Get the (simplified) player yaw.
	 * @return Player X-axis rotation (yaw)
	 */
	public double getXRotation()
	{
		return xRotation;
	}
	
	/**
	 * Gets the normal player rotation.
	 * 
	 * This differs from xRotation by 90 degrees. xRotation is ported from 
	 * HitBlox, I really need to get rid of or refactor all that code, but it may be
	 * worth just waiting for the Bukkit targeting implementation at this point.
	 * 
	 * @return The player X-rotation (yaw)
	 */
	public float getPlayerRotation()
	{
		float playerRot = player.getLocation().getYaw();
		while (playerRot < 0)
			playerRot += 360;
		while (playerRot > 360)
			playerRot -= 360;
		return playerRot;
	}
	
	/*
	 * HitBlox-ported code
	 */

	/**
	 * Returns the block at the cursor, or null if out of range
	 * 
	 * @return The target block
	 */
	public Target getTarget()
	{
	    Block block = getTargetBlock();
	    Target targetBlock = new Target(player, block);
	    Target targetEntity = getTargetEntity();
	    if (targetEntity == null || targetBlock.getDistance() < targetEntity.getDistance())
	    {
	        return targetBlock;
	    }
	    
	    return targetEntity;
	}
	
	public Block getTargetBlock()
	{
		findTargetBlock();
		return getCurBlock();
	}

	protected Target getTargetEntity()
	{
		List<Entity> entities = player.getWorld().getEntities();
		List<Target> scored = new ArrayList<Target>();
		for (Entity entity : entities)
		{
            if (entity == player) continue;
			if (targetEntityType != null && !(targetEntityType.isAssignableFrom(entity.getClass()))) continue;
			
			Target newScore = new Target(player, entity);
			if (newScore.getScore() > 0)
			{
			    scored.add(newScore);
			}
		}
		
		if (scored.size() <= 0) return null;
		Collections.sort(scored);
		
		return scored.get(0);
	}

	/**
	 * Returns the block attached to the face at the cursor, or null if out of
	 * range
	 * 
	 * @return The face block
	 */
	public Block getFaceBlock()
	{
		findTargetBlock();
		if (getCurBlock() != null)
		{
			return getLastBlock();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Move "steps" forward along line of vision and returns the block there
	 * 
	 * @return The block at the new location
	 */
	public Block getNextBlock()
	{
		lastX = targetX;
		lastY = targetY;
		lastZ = targetZ;

		do
		{
			length += step;

			hLength = (length * Math.cos(Math.toRadians(yRotation)));
			yOffset = (length * Math.sin(Math.toRadians(yRotation)));
			xOffset = (hLength * Math.cos(Math.toRadians(xRotation)));
			zOffset = (hLength * Math.sin(Math.toRadians(xRotation)));

			targetX = (int) Math.floor(xOffset + playerLocation.getX());
			targetY = (int) Math.floor(yOffset + playerLocation.getY() + viewHeight);
			targetZ = (int) Math.floor(zOffset + playerLocation.getZ());

		}
		while ((length <= range) && ((targetX == lastX) && (targetY == lastY) && (targetZ == lastZ)));

		if (length > range || targetY > 127)
		{
			if (allowMaxRange)
			{
				return getBlockAt(targetX, targetY, targetZ);
			}
			else
			{
				return null;
			}
		}

		return getBlockAt(targetX, targetY, targetZ);
	}

	/**
	 * Returns the current block along the line of vision
	 * 
	 * @return The block
	 */
	public Block getCurBlock()
	{
		if (length > range && !allowMaxRange)
		{
			return null;
		}
		else
		{
			return getBlockAt(targetX, targetY, targetZ);
		}
	}

	/**
	 * Returns the previous block along the line of vision
	 * 
	 * @return The block
	 */
	public Block getLastBlock()
	{
		return getBlockAt(lastX, lastY, lastZ);
	}

	/**
	 * Returns the block at the specified location
	 * 
	 * Just a wrapper for world.getBlock at this point.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return block The block at the specified coordinates
	 */
	public Block getBlockAt(int x, int y, int z)
	{
		World world = player.getWorld();
		return world.getBlockAt(x, y, z);
	}	
	
	/*
	 * Functions to send text to player- use these to respect "quiet" and "silent" modes.
	 */
	
	/**
	 * Send a message to a player when a spell is cast.
	 * 
	 * Will be replaced with the Message interface from Persistence soon.
	 * 
	 * Respects the "quiet" and "silent" properties settings.
	 * 
	 * @param player The player to send a message to 
	 * @param message The message to send
	 */
	public void castMessage(Player player, String message)
	{
		if (!spells.isQuiet() && !spells.isSilent())
		{
			player.sendMessage(message);
		}
	}

	/**
	 * Send a message to a player. 
	 * 
	 * Will be replaced with the Message interface from Persistence soon.
	 * 
	 * Use this to send messages to the player that are important.
	 * 
	 * Only respects the "silent" properties setting.
	 * 
	 * @param player The player to send the message to
	 * @param message The message to send
	 */
	public void sendMessage(Player player, String message)
	{
		if (!spells.isSilent())
		{
			player.sendMessage(message);
		}
	}

	/*
	 * Time functions
	 */

	/**
	 * Sets the current server time
	 * 
	 * @param time specified server time (0-24000)
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

	/**
	 * Return the in-game server time.
	 * 
	 * @return server time
	 */
	public long getTime()
	{
		return player.getWorld().getTime();
	}
	
	/**
	 * Check to see if the player is underwater
	 * 
	 * @return true if the player is underwater
	 */
	public boolean isUnderwater()
	{
		Block playerBlock = getPlayerBlock();
		playerBlock = playerBlock.getFace(BlockFace.UP);
		return (playerBlock.getType() == Material.WATER || playerBlock.getType() == Material.STATIONARY_WATER);
	}
	
	/**
	 * Get all the registered variants of this spell.
	 * 
	 * @return This spells' variants
	 */
	public List<SpellVariant> getVariants()
	{
		return variants;
	}
	
	/**
	 * Register a variant of this spell.
	 * 
	 * @param name
	 * @param material
	 * @param category
	 * @param description
	 * @param parameters
	 */
	protected void addVariant(String name, Material material, String category, String description, String[] parameters)
	{
		variants.add(new SpellVariant(this, name, material, category, description, parameters));
	}
	
	/**
	 * Register a variant of this spell.
	 * 
	 * @param name
	 * @param material
	 * @param category
	 * @param description
	 * @param parameter
	 */
	protected void addVariant(String name, Material material, String category, String description, String parameter)
	{
		String[] parameters = parameter.split(" ");
		variants.add(new SpellVariant(this, name, material, category, description, parameters));
	}
	
	/**
	 * Used internally to initialize the Spell, do not call.
	 * 
	 * @param instance The spells instance
	 */
	public void initialize(Spells instance, PluginUtilities utilities, Persistence persistence)
	{
		this.utilities = utilities;
		this.spells = instance;
		this.persistence = persistence;
	}
	
	/**
	 * Called by Spells to cast this spell, do not call.
	 * 
	 * @param parameters
	 * @param player
	 * @return true if the spell succeed, false if failed
	 * @see Spells#castSpell(SpellVariant, Player)
	 */
	public boolean cast(String[] parameters, Player player)
	{
		this.player = player;
		
		long currentTime = System.currentTimeMillis();
		if (lastCast != 0 && lastCast > currentTime - cooldown)
		{
		    return false;
		}
		
		lastCast = currentTime;

		targetThrough(Material.AIR);
		targetThrough(Material.WATER);
		targetThrough(Material.STATIONARY_WATER);
        targetThrough(Material.SNOW);

		initializeTargeting(player);

		return onCast(parameters);
	}

	/**
	 * Called by the Spells plugin to cancel this spell, do not call.
	 * 
	 * @param plugin The Spells plugin instance
	 * @param player The player cancelling selection
	 */
	public void cancel(Spells plugin, Player player)
	{
		this.player = player;
		this.spells = plugin;

		onCancel();
	}

	protected void initializeTargeting(Player player)
	{
		playerLocation = player.getLocation();
		length = 0;
		targetHeightRequired = 1;
		targetEntityType = LivingEntity.class;
		xRotation = (playerLocation.getYaw() + 90) % 360;
		yRotation = playerLocation.getPitch() * -1;
		reverseTargeting = false;

		targetX = (int) Math.floor(playerLocation.getX());
		targetY = (int) Math.floor(playerLocation.getY() + viewHeight);
		targetZ = (int) Math.floor(playerLocation.getZ());
		lastX = targetX;
		lastY = targetY;
		lastZ = targetZ;
		targetingComplete = false;
	}

	protected void findTargetBlock()
	{
		if (targetingComplete)
		{
			return;
		}

		while (getNextBlock() != null && length <= range)
		{
			Block block = getCurBlock();
			if (isTargetable(block.getType()))
			{
				boolean enoughSpace = true;
				for (int i = 1; i < targetHeightRequired; i++)
				{
					block = block.getFace(BlockFace.UP);
					if (!isTargetable(block.getType()))
					{
						enoughSpace = false;
						break;
					}
				}
				if (enoughSpace) break;
			}
		}
		targetingComplete = true;
	}
	
	public String getPermissionNode()
	{
		return "SpellsPlugin.spells." + getName();
	}
	
	public boolean hasSpellPermission()
	{
		return hasSpellPermission(player);
	}
	
	public boolean hasSpellPermission(Player player)
	{
		if (player == null) return false;
		PlayerData playerData = persistence.get(player.getName(), PlayerData.class);
		if (playerData == null) return false;
		return playerData.isSet(getPermissionNode());
	}
	
	public boolean otherSpellHasPermission(String spellName)
	{
		SpellVariant spell = spells.getSpell(spellName, player);
		if (spell == null) return false;
		
		return spell.hasSpellPermission(player);
	}
	
	/* Used for sorting spells
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Spell other)
	{
		return getName().compareTo(other.getName());
	}
	
	protected void setMaxRange(int range, boolean allow)
	{
		this.range = range;
		this.allowMaxRange = allow;
	}
	
	protected Material getMaterial(String matName, List<Material> materials)
	{
		Material material = Material.AIR;
		StringBuffer simplify = new StringBuffer ("_");
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
	
	public Persistence getPersistence()
	{
		return persistence;
	}

    public boolean isInCircle(int x, int z, int R)
    {
        return ((x * x) +  (z * z) - (R * R)) <= 0;
    }
    
    public void coverSurface(Location center, int radius, BlockAction action)
    {   
        int y = center.getBlockY();
        for (int dx = -radius; dx < radius; ++dx)
        {
            for (int dz = -radius; dz < radius; ++dz)
            {
                if (isInCircle(dx, dz, radius))
                {
                    int x = center.getBlockX() + dx;
                    int z = center.getBlockZ() + dz;
                    Block block = player.getWorld().getBlockAt(x, y, z);
                    int depth = 0;
                    
                    if (block.getType() == Material.AIR)
                    {
                        while (depth < verticalSearchDistance && block.getType() == Material.AIR)
                        {
                            depth++;
                            block = block.getFace(BlockFace.DOWN);
                        }   
                    }
                    else
                    {
                        while (depth < verticalSearchDistance && block.getType() != Material.AIR)
                        {
                            depth++;
                            block = block.getFace(BlockFace.UP);
                        }
                        block = block.getFace(BlockFace.DOWN);
                    }

                    Block coveringBlock = block.getFace(BlockFace.UP);
                    if (block.getType() != Material.AIR && coveringBlock.getType() == Material.AIR)
                    {
                        action.perform(block);
                    }  
                } 
            }
        }
    }
    
    protected void setCooldown(int ms)
    {
        cooldown = ms;
    }
	
	/*
	 * private data
	 */

	private boolean								allowMaxRange			= false;
	private int									range					= 200;
	private double								viewHeight				= 1.65;
	private double								step					= 0.2;
	
	private int                                 cooldown                = 0;
	private long                                lastCast                = 0;

	private int                                 verticalSearchDistance  = 8;
	private boolean								targetingComplete;
	private int									targetHeightRequired	= 1;
	private Class<? extends Entity>             targetEntityType        = null;
	private Location							playerLocation;
	private double								xRotation, yRotation;
	private double								length, hLength;
	private double								xOffset, yOffset, zOffset;
	private int									lastX, lastY, lastZ;
	private int									targetX, targetY, targetZ;
	private final HashMap<Material, Boolean>	targetThroughMaterials	= new HashMap<Material, Boolean>();
	private boolean								reverseTargeting		= false;
	private final List<SpellVariant>			variants				= new ArrayList<SpellVariant>();

	protected PluginUtilities					utilities				= null;
	protected Persistence						persistence				= null;
}
