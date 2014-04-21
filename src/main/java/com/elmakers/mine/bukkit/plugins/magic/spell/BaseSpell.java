package com.elmakers.mine.bukkit.plugins.magic.spell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public abstract class BaseSpell extends Spell {
	protected static final double VIEW_HEIGHT = 1.65;
	protected static final double LOOK_THRESHOLD_RADIANS = 0.8;
	
	private long 			lastMessageSent 			= 0;
	private Set<Material>	preventPassThroughMaterials = null;
	
	public Player getPlayer()
	{
		return mage.getPlayer();
	}

	public CommandSender getCommandSender()
	{
		return mage.getCommandSender();
	}

	public boolean allowPassThrough(Material mat)
	{
		if (mage != null && mage.isSuperPowered()) {
			return true;
		}
		return preventPassThroughMaterials == null || !preventPassThroughMaterials.contains(mat);
	}
	
	/*
	 * Ground / location search and test function functions
	 * TODO: Config-drive this.
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
		Block blockOneDown = block.getRelative(BlockFace.DOWN);
		Player player = mage.getPlayer();
		return (
				(isOkToStandOn(blockOneDown.getType()) || (player != null && player.isFlying()))
				&&	isOkToStandIn(blockOneUp.getType())
				&& 	isOkToStandIn(block.getType())
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
			// First look down just a little bit
			int y = targetLoc.getBlockY();
			int testMinY = Math.max(minY,  y - 4);
			location = findPlaceToStand(targetLoc, false, testMinY, maxY);
			
			// Then look up
			if (location == null) {
				location = findPlaceToStand(targetLoc, true, minY, maxY);
			}
			
			// Then look allll the way down.
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
				return targetLocation;
			}
			
			if (!allowPassThrough(block.getType())) {
				return null;
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
		return getLocation().getBlock().getRelative(BlockFace.DOWN);
	}

	/**
	 * Get the direction the player is facing as a BlockFace.
	 * 
	 * @return a BlockFace representing the direction the player is facing
	 */
	public BlockFace getPlayerFacing()
	{
		float playerRot = getLocation().getYaw();
		while (playerRot < 0)
			playerRot += 360;
		while (playerRot > 360)
			playerRot -= 360;

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

	/*
	 * Functions to send text to player- use these to respect "quiet" and "silent" modes.
	 */

	/**
	 * Send a message to a player when a spell is cast.
	 * 
	 * @param message The message to send
	 */
	public void castMessage(String message)
	{
		if (canSendMessage() && message != null && message.length() > 0)
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
	 * @param message The message to send
	 */
	public void sendMessage(String message)
	{
		if (canSendMessage() && message != null && message.length() > 0)
		{
			mage.sendMessage(message);
			lastMessageSent = System.currentTimeMillis();
		}
	}

	public Location getLocation()
	{
		if (location != null) return location.clone();
		if (mage != null) {
			return mage.getLocation();
		}
		return null;
	}

	public Location getEyeLocation()
	{
		Location location = getLocation();
		if (location == null) return null;
		location.setY(location.getY() + 1.5);
		return location;
	}
	
	public Vector getDirection()
	{
		if (location == null) {
			return mage.getDirection();
		}
		return location.getDirection();
	}
	
	public boolean isLookingUp()
	{
		Vector direction = getDirection();
		if (direction == null) return false;
		return direction.getY() > LOOK_THRESHOLD_RADIANS;
	}

	public boolean isLookingDown()
	{
		Vector direction = getDirection();
		if (direction == null) return false;
		return direction.getY() < -LOOK_THRESHOLD_RADIANS;
	}

	public World getWorld()
	{
		Location location = getLocation();
		if (location != null) return location.getWorld();
		return null;
	}

	/**
	 * Check to see if the player is underwater
	 * 
	 * @return true if the player is underwater
	 */
	public boolean isUnderwater()
	{
		Block playerBlock = getPlayerBlock();
		if (playerBlock == null) return false;
		playerBlock = playerBlock.getRelative(BlockFace.UP);
		return (playerBlock.getType() == Material.WATER || playerBlock.getType() == Material.STATIONARY_WATER);
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
	
	protected static Collection<PotionEffect> getPotionEffects(ConfigurationSection parameters)
	{		
		List<PotionEffect> effects = new ArrayList<PotionEffect>();
		PotionEffectType[] effectTypes = PotionEffectType.values();
		for (PotionEffectType effectType : effectTypes) {
			// Why is there a null entry in this list? Maybe a 1.7 bug?
			if (effectType == null) continue;
			
			String parameterName = "effect_" + effectType.getName().toLowerCase();
			if (parameters.contains(parameterName)) {
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
					Bukkit.getLogger().warning("Error parsing potion effect for " + effectType + ": " + value);
				}
			}
		}
		return effects;
	}

	public boolean isInCircle(int x, int z, int R)
	{
		return ((x * x) +  (z * z) - (R * R)) <= 0;
	}
	
	private boolean canSendMessage()
	{
		if (lastMessageSent == 0) return true;
		int throttle = controller.getMessageThrottle();
		long now = System.currentTimeMillis();
		return (lastMessageSent < now - throttle);
	}
	
	@Override
	protected Location getEffectLocation()
	{
		return getEyeLocation();
	}

	protected void processParameters(ConfigurationSection parameters) {
		super.processParameters(parameters);

		if (parameters.contains("prevent_passthrough")) {
			preventPassThroughMaterials = controller.getMaterialSet(parameters.getString("prevent_passthrough"));
		} else if (parameters.contains("indestructible")) {
			preventPassThroughMaterials = controller.getMaterialSet(parameters.getString("indestructible"));
		} else {
			preventPassThroughMaterials = controller.getMaterialSet("indestructible");
		}
	}
}
