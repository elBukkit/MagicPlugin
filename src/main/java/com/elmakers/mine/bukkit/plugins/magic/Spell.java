package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.dao.MaterialList;
import com.elmakers.mine.bukkit.utilities.BlockAction;
import com.elmakers.mine.bukkit.utilities.CSVParser;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

/**
 * 
 * Base class for spells. Handles finding player location, targeting, and other
 * common spell activities.
 * 
 * Original targeting code ported from: HitBlox.java, Ho0ber@gmail.com 
 *
 */
public abstract class Spell implements Comparable<Spell>, Cloneable
{	
	/*
	 * protected members that are helpful to use
	 */
	protected Player						player;
	protected Spells						spells;
	protected PlayerSpells					playerSpells;
	protected static CSVParser              csv = new CSVParser();

	/*
	 * Variant properties
	 */
	private String key;
	private String name;
	private String description;
	private String category;
	private ConfigurationNode parameters = new ConfigurationNode();
	private Material material;
	private Material materialOverride;
	private List<CastingCost> costs = null;
	private List<CastingCost> activeCosts = null;

	/*
	 * private data
	 */

	private boolean                             allowMaxRange           = false;
	private int                                 range                   = 200;
	private static int                          maxRange                = 511;
	private double                              viewHeight              = 1.65;
	private double                              step                    = 0.2;

	private int                                 cooldown                = 0;
	private int                                 duration                = 0;
	private long                                lastCast                = 0;
	private long 								lastMessageSent 		= 0;

	private int                                 verticalSearchDistance  = 8;
	private boolean                             targetingComplete;
	private int                                 targetHeightRequired    = 1;
	private Class<? extends Entity>             targetEntityType        = null;
	private Location                            playerLocation;
	private double                              xRotation, yRotation;
	private double                              length, hLength;
	private double                              xOffset, yOffset, zOffset;
	private int                                 lastX, lastY, lastZ;
	private int                                 targetX, targetY, targetZ;
	private MaterialList                        targetThroughMaterials  = new MaterialList();
	private boolean                             reverseTargeting        = false;
	private boolean                             usesTargeting           = true;

	protected Object clone()
	{
		try
		{
			return super.clone();
		}
		catch (CloneNotSupportedException ex)
		{
			return null;
		}
	}

	/**
	 * Default constructor, used to register spells.
	 * 
	 */
	public Spell()
	{
		this.player = null;
	}

	protected static String getBuiltinClasspath()
	{
		String baseClass = Spell.class.getName();
		return baseClass.substring(0, baseClass.lastIndexOf('.'));
	}

	public static Spell loadSpell(String name, ConfigurationNode node, Spells spells)
	{
		String builtinClassPath = getBuiltinClasspath();

		String className = node.getString("class");
		if (className == null) return null;

		if (className.indexOf('.') <= 0)
		{
			className = builtinClassPath + ".spells." + className;
		}

		Class<?> spellClass = null;
		try
		{
			spellClass = Class.forName(className);
		}
		catch(ClassNotFoundException ex)
		{
			// TODO Log errors
			return null;
		}

		Object newObject;
		try
		{
			newObject = spellClass.newInstance();
		}
		catch (InstantiationException e)
		{
			// TODO Log errors
			return null;
		}
		catch (IllegalAccessException e)
		{
			// TODO Log errors
			return null;
		}

		if (newObject == null || !(newObject instanceof Spell))
		{
			// TODO Log errors
			return null;
		}

		Spell newSpell = (Spell)newObject;
		newSpell.initialize(spells);
		newSpell.load(name, node);

		return newSpell;
	}
	
	public void checkActiveCosts() {
		if (activeCosts == null) return;
		
		for (CastingCost cost : activeCosts)
		{
			if (!cost.has(playerSpells))
			{
				deactivate();
				return;
			}
			
			cost.use(playerSpells);
		}
	}
	
	public void checkActiveDuration() {
		if (duration > 0 && lastCast < System.currentTimeMillis() - duration) {
			deactivate();
		}
	}
	
	protected void activate() {
		onActivate();
		
		playerSpells.activateSpell(this);
	}
	
	protected void deactivate() {
		onDeactivate();
		
		playerSpells.deactivateSpell(this);
	}

	protected void loadAndSave(ConfigurationNode node)
	{
		name = node.getString("name", name);
		description = node.getString("description", description);
		material = node.getMaterial("icon", material);
		category = node.getString("category", category);
		parameters = node.getNode("parameters", parameters);
		ConfigurationNode properties = node.getNode("properties");
		if (properties == null) properties = node.createChild("properties");
		cooldown = properties.getInt("cooldown", cooldown);
		duration = properties.getInt("duration", duration);
		materialOverride = properties.getMaterial("material", materialOverride);

		this.onLoad(properties);

		if (usesTargeting)
		{            
			range = properties.getInteger("range", range);
			allowMaxRange = properties.getBoolean("allow_max_range", allowMaxRange);
			targetThroughMaterials = new MaterialList(properties.getMaterials("target_through", targetThroughMaterials));
		}
	}
	
	protected List<CastingCost> parseCosts(ConfigurationNode node, String nodeName) {
		List<Object> costNodes = node.getList(nodeName);
		List<CastingCost> castingCosts = null;
		
		if (costNodes != null) 
		{
			castingCosts = new ArrayList<CastingCost>();
			for (Object o : costNodes)
			{
				if (o instanceof Map)
				{
					@SuppressWarnings("unchecked")
					Map<String, Object> nodeValues = (Map<String, Object>)o;
					CastingCost cost = new CastingCost(new ConfigurationNode(nodeValues));
					castingCosts.add(cost);
				}
			}
		}
		
		return castingCosts;
	}

	protected void load(String key, ConfigurationNode node)
	{
		this.key = key;
		this.name = key;
		loadAndSave(node);

		costs = parseCosts(node, "costs");
		activeCosts = parseCosts(node, "active_costs");

		Set<Material> defaultTargetThrough = spells.getTargetThroughMaterials();
		for (Material defMat : defaultTargetThrough) {
			targetThrough(defMat);
		}
	}

	public void save(ConfigurationNode node)
	{
		String className = this.getClass().getName();

		String builtinClassPath = getBuiltinClasspath();

		if (className.contains(builtinClassPath))
		{
			className = className.substring(className.lastIndexOf('.') + 1);
		}
		node.setProperty("class", className);

		loadAndSave(node);

		if (costs != null)
		{
			List< Map<String, Object> > costList = new ArrayList< Map<String, Object> >();
			for (CastingCost cost : costs)
			{
				costList.add(cost.export());
			}

			node.setProperty("costs", costList);
		}
		this.onSave(node);
	}

	public void onSave(ConfigurationNode node)
	{

	}

	public void setPlayer(Player player)
	{
		this.player = player;
		playerSpells = spells.getPlayerSpells(player);
	}

	public final String getKey()
	{
		return key;
	}

	public final String getName()
	{
		return name;
	}

	public final Material getMaterial()
	{
		return material;
	}

	public final String getDescription()
	{
		return description;
	}

	public final String getCategory()
	{
		return category;
	}

	public boolean isMatch(String spell, String[] params)
	{
		if (params == null) params = new String[0];
		return (key.equalsIgnoreCase(spell) && parameters.equals(params));
	}

	public int compareTo(Spell other)
	{
		return name.compareTo(other.name);
	}

	public boolean cast()
	{
		return cast(new String[0]);
	}

	static public void addParameters(String[] extraParameters, ConfigurationNode parameters)
	{
		if (extraParameters != null)
		{
			for (int i = 0; i < extraParameters.length - 1; i += 2)
			{
				parameters.setProperty(extraParameters[i], extraParameters[i + 1]);
			}
		}
	}

	public boolean cast(String[] extraParameters)
	{
		ConfigurationNode parameters = new ConfigurationNode(this.parameters);
		addParameters(extraParameters, parameters);

		long currentTime = System.currentTimeMillis();
		float cooldownReduction = playerSpells.getCooldownReduction();
		int reducedCooldown = cooldownReduction >= 1 ? 0 : (int)Math.ceil((1.0f - cooldownReduction) * cooldown);
		if (lastCast != 0 && lastCast > currentTime - reducedCooldown)
		{
			long seconds = (lastCast - (currentTime - reducedCooldown)) / 1000;
			if (seconds > 1) {
				sendMessage("You must wait another " + seconds + " seconds.");
			} else {
				sendMessage("You must wait a moment.");
			}
			playerSpells.onCast(SpellResult.COOLDOWN);
			return false;
		}

		if (costs != null)
		{
			for (CastingCost cost : costs)
			{
				if (!cost.has(playerSpells))
				{
					sendMessage("Not enough " + cost.getDescription());
					playerSpells.onCast(SpellResult.INSUFFICIENT_RESOURCES);
					return false;
				}
			}
		}

		lastCast = currentTime;
		initializeTargeting(player);

		SpellResult result = onCast(parameters);
		playerSpells.onCast(result);
		
		if (result == SpellResult.SUCCESS && costs != null) {
			for (CastingCost cost : costs)
			{
				cost.use(playerSpells);
			}
		}
		
		return result == SpellResult.SUCCESS;
	}

	public String getPermissionNode()
	{
		return "Magic.cast." + key;
	}

	public boolean hasSpellPermission(Player player)
	{
		if (player == null) return false;

		return spells.hasPermission(player, getPermissionNode(), true);
	}

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
	public abstract SpellResult onCast(ConfigurationNode parameters);

	/**
	 * Called on load, you can load data here and set defaults.
	 * 
	 * @param node The configuration node to load data from.
	 */
	public void onLoad(ConfigurationNode node)
	{

	}

	/**
	 * Called when a material selection spell is cancelled mid-selection.
	 */
	public boolean onCancel()
	{
		return false;
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
	public void onPlayerDeath(EntityDeathEvent event)
	{

	}

	public void onPlayerDamage(EntityDamageEvent event)
	{

	}

	public static byte getItemData(ItemStack stack)
	{
		if (stack == null) return 0;
		return (byte)stack.getDurability();
	}

	public Player getPlayer()
	{
		return player;
	}

	/*
	 * General helper functions
	 */
	public ItemStack getBuildingMaterial()
	{
		if (materialOverride != null)
		{
			return new ItemStack(materialOverride, 1);
		}
		
		return playerSpells.getBuildingMaterial();
	}
	
	public boolean hasBuildPermission(Location location)
	{
		return playerSpells.hasBuildPermission(location);
	}
	
	public boolean hasBuildPermission(Block block)
	{
		return playerSpells.hasBuildPermission(block);
	}

	public void targetEntity(Class<? extends Entity> typeOf)
	{
		targetEntityType = typeOf;
	}

	public void targetThrough(Material mat)
	{
		targetThroughMaterials.add(mat);
	}

	public void noTargetThrough(Material mat)
	{
		targetThroughMaterials.remove(mat);
	}

	public boolean isTargetable(Material mat)
	{
		boolean targetThrough = targetThroughMaterials.contains(mat);
		if (reverseTargeting)
		{
			return(targetThrough);
		}
		return !targetThrough;
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
		while (4 < y && y < 253)
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
			direction = BlockFace.SOUTH;
		}
		else if (playerRot > 45 && playerRot <= 135)
		{
			direction = BlockFace.WEST;
		}
		else if (playerRot > 135 && playerRot <= 225)
		{
			direction = BlockFace.NORTH;
		}
		else if (playerRot > 225 && playerRot <= 315)
		{
			direction = BlockFace.EAST;
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
		default:
			return direction;
		}
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
		default:
			return direction;
		}
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
	 * Get a Vector representing the current aim direction
	 * 
	 * @return The player's aim vector
	 */
	public Vector getAimVector()
	{
		double yaw = Math.toRadians(playerLocation.getYaw());
		double pitch = Math.toRadians(playerLocation.getPitch());
		Vector aimVector = new Vector
				(
						(0 - Math.sin(yaw)), 
						(0 - Math.sin(pitch)), 
						Math.cos(yaw)
						);
		// kind of a hack, but i can't seem to get the matrix math right :P
		double y = aimVector.getY();
		if  (y >= 1 || y <= -1)
		{
			aimVector.setX(0);
			aimVector.setZ(0);
		}
		else
		{
			aimVector.normalize();
		}
		return aimVector;
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
		List<Target> scored = getAllTargetEntities();
		if (scored.size() <= 0) return null;
		return scored.get(0);
	}
	
	protected List<Target> getAllTargetEntities() {
		List<Entity> entities = player.getWorld().getEntities();
		List<Target> scored = new ArrayList<Target>();
		for (Entity entity : entities)
		{
			if (entity == player) continue;
			if (targetEntityType != null && !(targetEntityType.isAssignableFrom(entity.getClass()))) continue;

			Target newScore = new Target(player, entity, getMaxRange());
			if (newScore.getScore() > 0)
			{
				scored.add(newScore);
			}
		}

		Collections.sort(scored);
		return scored;
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
		int scaledRange = getMaxRange();

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
		while ((length <= scaledRange) && ((targetX == lastX) && (targetY == lastY) && (targetZ == lastZ)));

		if (length > scaledRange || targetY > 255)
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
		int scaledRange = getMaxRange();
		
		if (length > scaledRange && !allowMaxRange)
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
	 * Respects the "quiet" and "silent" properties settings.
	 * 
	 * @param player The player to send a message to 
	 * @param message The message to send
	 */
	public void castMessage(String message)
	{
		if (!spells.isQuiet() && !spells.isSilent() && canSendMessage())
		{
			player.sendMessage(message);
			lastMessageSent = System.currentTimeMillis();
		}
	}

	/**
	 * Send a message to a player. 
	 * 
	 * Use this to send messages to the player that are important.
	 * 
	 * Only respects the "silent" properties setting.
	 * 
	 * @param player The player to send the message to
	 * @param message The message to send
	 */
	public void sendMessage(String message)
	{
		if (!spells.isSilent() && canSendMessage())
		{
			player.sendMessage(message);
			lastMessageSent = System.currentTimeMillis();
		}
	}
	
	private boolean canSendMessage()
	{
		if (lastMessageSent == 0) return true;
		int throttle = spells.getMessageThrottle();
		long now = System.currentTimeMillis();
		return (lastMessageSent < now - throttle);
	}

	/*
	 * Time functions
	 */

	/**
	 * Sets the current server time
	 * 
	 * @param time specified server time (0-24000)
	 */
	public void setTime(long time)
	{
		player.getWorld().setTime(time);
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
		playerBlock = playerBlock.getRelative(BlockFace.UP);
		return (playerBlock.getType() == Material.WATER || playerBlock.getType() == Material.STATIONARY_WATER);
	}

	/**
	 * Used internally to initialize the Spell, do not call.
	 * 
	 * @param instance The spells instance
	 */
	public void initialize(Spells instance)
	{
		this.spells = instance;
	}

	/**
	 * Called by the Spells plugin to cancel this spell, do not call.
	 * 
	 * @param plugin The Spells plugin instance
	 * @param player The player cancelling selection
	 */
	public boolean cancel()
	{
		return onCancel();
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
		int scaledRange = getMaxRange();
		while (getNextBlock() != null && length <= scaledRange)
		{
			Block block = getCurBlock();
			if (isTargetable(block.getType()))
			{
				boolean enoughSpace = true;
				for (int i = 1; i < targetHeightRequired; i++)
				{
					block = block.getRelative(BlockFace.UP);
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

	protected int getMaxRange()
	{
		return Math.min(maxRange, (int)(playerSpells.getPowerMultiplier() * range));
	}

	protected int getMaxRangeSquared()
	{
		int maxRange = getMaxRange();
		return maxRange * maxRange;
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
		@SuppressWarnings("deprecation")
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

	protected void disableTargeting()
	{
		usesTargeting = false;
	}
	
	public List<CastingCost> getCosts() {
		return costs;
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
							block = block.getRelative(BlockFace.DOWN);
						}   
					}
					else
					{
						while (depth < verticalSearchDistance && block.getType() != Material.AIR)
						{
							depth++;
							block = block.getRelative(BlockFace.UP);
						}
						block = block.getRelative(BlockFace.DOWN);
					}

					Block coveringBlock = block.getRelative(BlockFace.UP);
					if (block.getType() != Material.AIR && coveringBlock.getType() == Material.AIR)
					{
						action.perform(block);
					}  
				} 
			}
		}
	}
	
	public boolean usesMaterial() {
		return false;
	}
	
	public boolean hasMaterialOverride() {
		return materialOverride != null;
	}
	
	public void onActivate() {
		
	}
	
	public void onDeactivate() {

	}
	
	public PlayerSpells getPlayerSpells() {
		return playerSpells;
	}
}
