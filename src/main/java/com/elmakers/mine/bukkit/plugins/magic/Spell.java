package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.blocks.BlockAction;
import com.elmakers.mine.bukkit.blocks.MaterialAndData;
import com.elmakers.mine.bukkit.effects.SpellEffect;
import com.elmakers.mine.bukkit.utilities.CSVParser;
import com.elmakers.mine.bukkit.utilities.Messages;
import com.elmakers.mine.bukkit.utilities.Target;
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
	protected MagicController				controller;
	protected Mage 							mage;
	protected static CSVParser              csv = new CSVParser();

	/*
	 * Variant properties
	 */
	private String key;
	private String name;
	private String description;
	private String usage;
	private String category;
	private MaterialAndData icon = new MaterialAndData(Material.AIR);
	private List<CastingCost> costs = null;
	private List<CastingCost> activeCosts = null;

	protected ConfigurationNode parameters = new ConfigurationNode();

	/*
	 * private data
	 */

	private boolean                             allowMaxRange           = false;
	private boolean                             pvpRestricted           = false;
	private int                                 range                   = 200;
	private static int                          maxRange                = 511;
	private double                              viewHeight              = 1.65;
	private double                              step                    = 0.2;

	private int                                 cooldown                = 0;
	private int                                 duration                = 0;
	private long                                lastCast                = 0;
	private long								castCount				= 0;
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
	private Set<Material>                       targetThroughMaterials  = new HashSet<Material>();
	private boolean                             reverseTargeting        = false;
	private boolean								isActive				= false;
	
	private Target								target					= null;

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
	}

	protected static String getBuiltinClasspath()
	{
		String baseClass = Spell.class.getName();
		return baseClass.substring(0, baseClass.lastIndexOf('.'));
	}

	public static Spell loadSpell(String name, ConfigurationNode node, MagicController controller)
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
		catch (Throwable ex)
		{
			controller.getLogger().warning("Error loading spell: " + name);
			ex.printStackTrace();
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
		newSpell.initialize(controller);
		newSpell.loadTemplate(name, node);

		return newSpell;
	}
	
	public void checkActiveCosts() {
		if (activeCosts == null) return;
		
		for (CastingCost cost : activeCosts)
		{
			if (!cost.has(mage))
			{
				deactivate();
				return;
			}
			
			cost.use(mage);
		}
	}
	
	public void checkActiveDuration() {
		if (duration > 0 && lastCast < System.currentTimeMillis() - duration) {
			deactivate();
		}
	}
	
	protected void activate() {
		onActivate();
		
		mage.activateSpell(this);
		isActive = true;
	}
	
	protected void deactivate() {
		onDeactivate();
		
		mage.deactivateSpell(this);
		isActive = false;
	}
	
	protected List<CastingCost> parseCosts(ConfigurationNode node) {
		if (node == null) {
			return null;
		}
		List<CastingCost> castingCosts = new ArrayList<CastingCost>();
		List<String> costKeys = node.getKeys();
		for (String key : costKeys)
		{
			castingCosts.add(new CastingCost(key, node.getDouble(key, 1)));
		}
		
		return castingCosts;
	}
	
	public void configure(ConfigurationNode node) {
		icon = node.getMaterialAndData("icon", icon.getMaterial());
		category = node.getString("category", category);
		parameters = node.getNode("parameters", parameters);
		pvpRestricted = node.getBoolean("pvp_restricted", pvpRestricted);
		
		cooldown = parameters.getInt("cooldown", cooldown);
		duration = parameters.getInt("duration", duration);
		range = parameters.getInteger("range", range);
		allowMaxRange = parameters.getBoolean("allow_max_range", allowMaxRange);
		
		if (parameters.containsKey("target_through")) {
			targetThroughMaterials = parameters.getMaterials("target_through");
		} else if (parameters.containsKey("transparent")) {
			targetThroughMaterials.clear();
			targetThroughMaterials.addAll(controller.getMaterialSet(parameters.getString("transparent")));
		} else {
			targetThroughMaterials.clear();
			targetThroughMaterials.addAll(controller.getMaterialSet("transparent"));			
		}

		costs = parseCosts(node.getNode("costs"));
		activeCosts = parseCosts(node.getNode("active_costs"));
	}

	protected void loadTemplate(String key, ConfigurationNode node)
	{
		this.key = key;
		this.name = key;
		
		name = Messages.get("spells." + key + ".name", name);
		description = Messages.get("spells." + key + ".description", description);
		usage = Messages.get("spells." + key + ".usage", usage);
		
		configure(node);
	}

	public void setMage(Mage mage)
	{
		this.mage = mage;
	}

	public final String getKey()
	{
		return key;
	}

	public final String getName()
	{
		return name;
	}

	public final MaterialAndData getIcon()
	{
		return icon;
	}

	public final String getDescription()
	{
		return description;
	}

	public final String getUsage()
	{
		return usage;
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
		target = null;
		
		if (pvpRestricted && !controller.isPVPAllowed(mage.getLocation())) {
			return false;
		}
		
		ConfigurationNode parameters = new ConfigurationNode(this.parameters);
		addParameters(extraParameters, parameters);

		long currentTime = System.currentTimeMillis();
		float cooldownReduction = mage.getCooldownReduction();
		if (cooldownReduction < 1 && !isActive && cooldown > 0) {
			int reducedCooldown = (int)Math.ceil((1.0f - cooldownReduction) * cooldown);
			if (lastCast != 0 && lastCast > currentTime - reducedCooldown)
			{
				long seconds = (lastCast - (currentTime - reducedCooldown)) / 1000;
				if (seconds > 1) {
					sendMessage(Messages.get("cooldown.wait_seconds").replace("$seconds", ((Long)seconds).toString()));
				} else {
					sendMessage(Messages.get("cooldown.wait_moment"));
				}
				mage.onCast(this, SpellResult.COOLDOWN);
				return false;
			}
		}

		if (costs != null && !isActive)
		{
			for (CastingCost cost : costs)
			{
				if (!cost.has(mage))
				{
					sendMessage(Messages.get("costs.insufficient_resources").replace("$cost", cost.getDescription(mage)));
					mage.onCast(this, SpellResult.INSUFFICIENT_RESOURCES);
					return false;
				}
			}
		}

		lastCast = currentTime;
		initializeTargeting(getPlayer());

		processParameters(parameters);
		SpellResult result = onCast(parameters);
		mage.onCast(this, result);
		
		if (result == SpellResult.SUCCESS) {
			if (costs != null) {
				for (CastingCost cost : costs)
				{
					cost.use(mage);
				}
			}
			castCount++;
		} else if (result == SpellResult.INSUFFICIENT_PERMISSION) {
			sendMessage(Messages.get("costs.insufficient_permissions"));
		}
		
		return result == SpellResult.SUCCESS;
	}
	
	protected void processParameters(ConfigurationNode parameters) {
		
	}

	public String getPermissionNode()
	{
		return "Magic.cast." + key;
	}

	public boolean hasSpellPermission(Player player)
	{
		if (player == null) return true;

		return controller.hasPermission(player, getPermissionNode(), true);
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
	 * Called when a material selection spell is cancelled mid-selection.
	 */
	public boolean onCancel()
	{
		return false;
	}

	/**
	 * Listener method, called on player quit for registered spells.
	 * 
	 * @param event The player who just quit
	 * @see MagicController#registerEvent(SpellEventType, Spell)
	 */
	public void onPlayerQuit(PlayerQuitEvent event)
	{

	}

	/**
	 * Listener method, called on player move for registered spells.
	 * 
	 * @param player The player that died
	 * @param event The original entity death event
	 * @see MagicController#registerEvent(SpellEventType, Spell)
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
		return mage.getPlayer();
	}

	public CommandSender getCommandSender()
	{
		return mage.getCommandSender();
	}
	
	public boolean hasBuildPermission(Block block)
	{
		return mage.hasBuildPermission(block);
	}

	public void targetEntity(Class<? extends Entity> typeOf)
	{
		targetEntityType = typeOf;
	}

	public void targetThrough(Material mat)
	{
		targetThroughMaterials.add(mat);
	}

	public void targetThrough(Set<Material> mat)
	{
		targetThroughMaterials.clear();
		targetThroughMaterials.addAll(mat);
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
		return (mat != Material.AIR && mat != Material.LAVA && mat != Material.STATIONARY_LAVA);
	}
	
	public boolean isSafeLocation(Block block)
	{
		if (!block.getChunk().isLoaded()) {
			block.getChunk().load(true);
			return false;
		}

		if (block.getY() > 255) {
			return false;
		}
		
		Block blockOneUp = block.getRelative(BlockFace.UP);
		Block blockTwoUp = blockOneUp.getRelative(BlockFace.UP);
		return (
				isOkToStandOn(block.getType())
				&&	isOkToStandIn(blockOneUp.getType())
				&& 	isOkToStandIn(blockTwoUp.getType())
		);
	}
	
	public boolean isSafeLocation(Location loc)
	{
		return isSafeLocation(loc.getBlock());
	}
	
	public Location tryFindPlaceToStand(Location targetLoc)
	{
		return tryFindPlaceToStand(targetLoc, 4, 253);
	}
	
	public Location tryFindPlaceToStand(Location targetLoc, int minY, int maxY)
	{
		Location location = findPlaceToStand(targetLoc, minY, maxY);
		return location == null ? targetLoc : location;
	}
	
	public Location findPlaceToStand(Location targetLoc, int minY, int maxY)
	{
		if (!targetLoc.getBlock().getChunk().isLoaded()) return null;
		
		int targetY = targetLoc.getBlockY();
		if (targetY >= minY && targetY <= maxY && isSafeLocation(targetLoc)) return targetLoc;
		Location location = null;
		if (targetY < minY) {
			location = targetLoc.clone();
			location.setY(minY);
			location = findPlaceToStand(location, true, minY, maxY);
		} else if (targetY > maxY) {
			location = targetLoc.clone();
			location.setY(maxY);
			location = findPlaceToStand(location, false, minY, maxY);
		} else {
			location = findPlaceToStand(targetLoc, true, minY, maxY);
			
			if (location == null) {
				location = findPlaceToStand(targetLoc, false, minY, maxY);
			}
		}
		return location;
	}
	
	public Location findPlaceToStand(Location target, boolean goUp)
	{
		return findPlaceToStand(target, goUp, 4, 253);
	}
	
	public Location findPlaceToStand(Location target, boolean goUp, int minY, int maxY)
	{
		int direction = goUp ? 1 : -1;
		
		// search for a spot to stand
		Location targetLocation = target.clone();
		while (minY <= targetLocation.getY() && targetLocation.getY() <= maxY)
		{
			Block block = targetLocation.getBlock();
			if 
			(
				isSafeLocation(block)
			&&   !(goUp && isUnderwater() && isWater(block.getType())) // rise to surface of water
			)
			{
				// spot found - return location
				targetLocation.setY(targetLocation.getY() + 1);
				return targetLocation;
			}
			
			targetLocation.setY(targetLocation.getY() + direction);
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
		Location playerLoc = getPlayer().getLocation();
		int x = (int) Math.round(playerLoc.getX() - 0.5);
		int y = (int) Math.round(playerLoc.getY() - 0.5);
		int z = (int) Math.round(playerLoc.getZ() - 0.5);
		int dy = 0;
		while (dy > -3 && (playerBlock == null || isOkToStandIn(playerBlock.getType())))
		{
			playerBlock = getPlayer().getWorld().getBlockAt(x, y + dy, z);
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

		Location location = new Location(getPlayer().getWorld(), spawnBlock.getX() + aimVector.getX(), spawnBlock.getY()
				+ aimVector.getY(), spawnBlock.getZ() + aimVector.getZ(), getPlayer().getLocation().getYaw(), getPlayer()
				.getLocation().getPitch());

		return location;
	}

	protected Location getLocation()
	{
		Player player = getPlayer();
		if (player != null)
		{
			return player.getLocation();
		}
		
		// TODO: Configurable locations
		return Bukkit.getWorlds().get(0).getSpawnLocation();
	}

	protected Location getEyeLocation()
	{
		Player player = getPlayer();
		if (player != null)
		{
			return player.getEyeLocation();
		}
		
		// TODO: Configurable locations
		Location location = getLocation();
		location.setY(location.getY() + 1.5);
		return location;
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
		float playerRot = getPlayer().getLocation().getYaw();
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
		Target targetBlock = new Target(getPlayer(), block);
		Target targetEntity = getTargetEntity();
		if (targetEntity == null || targetBlock.getDistance() < targetEntity.getDistance())
		{
			target = targetBlock;
		} 
		else 
		{
			// Don't allow targeting entities in no-PVP areas.
			if (!pvpRestricted || controller.isPVPAllowed(targetEntity.getLocation())) 
			{
				target = targetEntity;
			}
			else 
			{
				// Don't let the target the block, either.
				target = new Target(getPlayer(), null);
			}
		}
		
		return target;
	}
	
	public Target getCurrentTarget()
	{
		return target;
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
		List<Entity> entities = getPlayer().getWorld().getEntities();
		List<Target> scored = new ArrayList<Target>();
		for (Entity entity : entities)
		{
			if (entity == getPlayer()) continue;
			if (targetEntityType != null && !(targetEntityType.isAssignableFrom(entity.getClass()))) continue;

			Target newScore = new Target(getPlayer(), entity, getMaxRange());
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
		World world = getPlayer().getWorld();
		return world.getBlockAt(x, y, z);
	}	

	/*
	 * Functions to send text to player- use these to respect "quiet" and "silent" modes.
	 */

	/**
	 * Send a message to a player when a spell is cast.
	 * 
	 * @param player The player to send a message to 
	 * @param message The message to send
	 */
	public void castMessage(String message)
	{
		if (canSendMessage())
		{
			mage.castMessage(message);
			lastMessageSent = System.currentTimeMillis();
		}
	}

	/**
	 * Send a message to a player. 
	 * 
	 * Use this to send messages to the player that are important.
	 * 
	 * @param player The player to send the message to
	 * @param message The message to send
	 */
	public void sendMessage(String message)
	{
		if (canSendMessage())
		{
			mage.sendMessage(message);
			lastMessageSent = System.currentTimeMillis();
		}
	}
	
	private boolean canSendMessage()
	{
		if (lastMessageSent == 0) return true;
		int throttle = controller.getMessageThrottle();
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
		getPlayer().getWorld().setTime(time);
	}

	/**
	 * Return the in-game server time.
	 * 
	 * @return server time
	 */
	public long getTime()
	{
		return getPlayer().getWorld().getTime();
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
	public void initialize(MagicController instance)
	{
		this.controller = instance;
		
		targetThroughMaterials.clear();
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

	public long getCastCount()
	{
		return castCount;
	}
	
	protected int getMaxRange()
	{
		return Math.min(maxRange, (int)(mage.getRangeMultiplier() * range));
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
			ItemStack current = getPlayer().getInventory().getItem(i);
			if (current == null || current.getType() == Material.AIR)
			{
				getPlayer().getInventory().setItem(i, itemStack);
				active = true;
				break;
			}
		}

		if (!active)
		{
			getPlayer().getInventory().addItem(itemStack);
		}

		return true;
	}
	
	public List<CastingCost> getCosts() {
		return costs;
	}
	
	public List<CastingCost> getActiveCosts() {
		return activeCosts;
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
					Block block = getPlayer().getWorld().getBlockAt(x, y, z);
					int depth = 0;

					if (targetThroughMaterials.contains(block.getType()))
					{
						while (depth < verticalSearchDistance && targetThroughMaterials.contains(block.getType()))
						{
							depth++;
							block = block.getRelative(BlockFace.DOWN);
						}   
					}
					else
					{
						while (depth < verticalSearchDistance && !targetThroughMaterials.contains(block.getType()))
						{
							depth++;
							block = block.getRelative(BlockFace.UP);
						}
						block = block.getRelative(BlockFace.DOWN);
					}
					Block coveringBlock = block.getRelative(BlockFace.UP);
					if (!targetThroughMaterials.contains(block.getType()) && targetThroughMaterials.contains(coveringBlock.getType()))
					{
						action.perform(block);
					}  
				} 
			}
		}
	}
	
	protected boolean isTransparent(Material material)
	{
		return targetThroughMaterials.contains(material);
	}
	
	public void onActivate() {
		
	}
	
	public void onDeactivate() {

	}
	
	public Mage getMage() {
		return mage;
	}
	
	// Effects
	
	protected SpellEffect getEffect(String effectName) {
		// TODO: config-driven
		SpellEffect effect = new SpellEffect();
		return effect;
	}
	
	public void load(ConfigurationNode node) {
		try {
			castCount = node.getLong("cast_count", 0);
			lastCast = node.getLong("last_cast", 0);
			onLoad(node);
		} catch (Exception ex) {
			controller.getPlugin().getLogger().warning("Failed to load data for spell " + name + ": " + ex.getMessage());
		}
	}
	
	public void save(ConfigurationNode node) {
		try {
			node.setProperty("cast_count", castCount);
			node.setProperty("last_cast", lastCast);
			onSave(node);
		} catch (Exception ex) {
			controller.getPlugin().getLogger().warning("Failed to save data for spell " + name + ": " + ex.getMessage());
		}
	}

	/**
	 * Called on player data load.
	 */
	public void onLoad(ConfigurationNode node)
	{
		
	}

	/**
	 * Called on player data save.
	 * 
	 * @param node The configuration node to load data from.
	 */
	public void onSave(ConfigurationNode node)
	{

	}
	
	protected static Collection<PotionEffect> getPotionEffects(ConfigurationNode parameters)
	{		
		List<PotionEffect> effects = new ArrayList<PotionEffect>();
		PotionEffectType[] effectTypes = PotionEffectType.values();
		for (PotionEffectType effectType : effectTypes) {
			// Why is there a null entry in this list? Maybe a 1.7 bug?
			if (effectType == null) continue;
			
			String parameterName = "effect_" + effectType.getName().toLowerCase();
			if (parameters.containsKey(parameterName)) {
				String value = parameters.getString(parameterName);
				String[] pieces = value.split(",");
				try {
					Integer ticks = Integer.parseInt(pieces[0]);
					Integer power = 1;
					if (pieces.length > 0) {
						power = Integer.parseInt(pieces[1]);
					}
					PotionEffect effect = new PotionEffect(effectType, ticks, power, true);
					effects.add(effect);
				} catch (Exception ex) {
					Bukkit.getLogger().warning("Error parsing potion effect for " + effectType + ": " + value + ": " + parameters.getKeys());
				}
			}
		}
		return effects;
	}
	
	protected void applyPotionEffects(Location location, int radius, Collection<PotionEffect> potionEffects) {
		if (potionEffects == null || radius <= 0 || potionEffects.size() == 0) return;
		
		int radiusSquared = radius * 2;
		List<Entity> entities = location.getWorld().getEntities();
		for (Entity entity : entities) {
			if (entity instanceof LivingEntity) {
				if (entity instanceof Player) {
					if (((Player)entity).getName().equals(mage.getName())) {
						continue;
					}
				}
				
				if (entity.getLocation().distanceSquared(location) < radiusSquared) {
					LivingEntity living = (LivingEntity)entity;
					living.addPotionEffects(potionEffects);
				}
			}
		}
	}
	
	protected String getBlockSkin(Material blockType) {
		String skinName = null;
		switch (blockType) {
		case CACTUS:
			skinName = "MHF_Cactus";
			break;
		case CHEST:
			skinName = "MHF_Chest";
			break;
		case MELON_BLOCK:
			skinName = "MHF_Melon";
			break;
		case TNT:
			if (Math.random() > 0.5) {
				skinName = "MHF_TNT";
			} else {
				skinName = "MHF_TNT2";
			}
			break;
		case LOG:
			skinName = "MHF_OakLog";
			break;
		case PUMPKIN:
			skinName = "MHF_Pumpkin";
			break;
		default:
			// TODO .. ?
			/*
			 * Blocks:
				Bonus:
				MHF_ArrowUp
				MHF_ArrowDown
				MHF_ArrowLeft
				MHF_ArrowRight
				MHF_Exclamation
				MHF_Question
			 */
		}
		
		return skinName;
	}
	
	protected String getMobSkin(EntityType mobType)
	{
		String mobSkin = null;
		switch (mobType) {
			case BLAZE:
				mobSkin = "MHF_Blaze";
				break;
			case CAVE_SPIDER:
				mobSkin = "MHF_CaveSpider";
				break;
			case CHICKEN:
				mobSkin = "MHF_Chicken";
				break;
			case COW:
				mobSkin = "MHF_Cow";
				break;
			case ENDERMAN:
				mobSkin = "MHF_Enderman";
				break;
			case GHAST:
				mobSkin = "MHF_Ghast";
				break;
			case IRON_GOLEM:
				mobSkin = "MHF_Golem";
				break;
			case MAGMA_CUBE:
				mobSkin = "MHF_LavaSlime";
				break;
			case MUSHROOM_COW:
				mobSkin = "MHF_MushroomCow";
				break;
			case OCELOT:
				mobSkin = "MHF_Ocelot";
				break;
			case PIG:
				mobSkin = "MHF_Pig";
				break;
			case PIG_ZOMBIE:
				mobSkin = "MHF_PigZombie";
				break;
			case SHEEP:
				mobSkin = "MHF_Sheep";
				break;
			case SLIME:
				mobSkin = "MHF_Slime";
				break;
			case SPIDER:
				mobSkin = "MHF_Spider";
				break;
			case SQUID:
				mobSkin = "MHF_Squid";
				break;
			case VILLAGER:
				mobSkin = "MHF_Villager";
			default:
				// TODO: Find skins for SKELETON, CREEPER and ZOMBIE .. ?
		}
		
		return mobSkin;
	}
}
