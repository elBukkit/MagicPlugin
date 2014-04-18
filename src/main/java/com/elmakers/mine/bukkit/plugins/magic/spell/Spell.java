package com.elmakers.mine.bukkit.plugins.magic.spell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.effects.EffectPlayer;
import com.elmakers.mine.bukkit.effects.EffectSingle;
import com.elmakers.mine.bukkit.effects.EffectTrail;
import com.elmakers.mine.bukkit.effects.ParticleType;
import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.MagicController;
import com.elmakers.mine.bukkit.utilities.ConfigurationUtils;
import com.elmakers.mine.bukkit.utilities.Messages;

/**
 * 
 * Base class for spells. Handles finding player location, targeting, and other
 * common spell activities.
 * 
 * Original targeting code ported from: HitBlox.java, Ho0ber@gmail.com 
 *
 */
public abstract class Spell implements Comparable<com.elmakers.mine.bukkit.api.spell.SpellTemplate>, Cloneable, CostReducer, com.elmakers.mine.bukkit.api.spell.Spell
{
	// TODO: Configurable default? this does look cool, though.
	protected final static Material DEFAULT_EFFECT_MATERIAL = Material.STATIONARY_WATER;
	
	public final static int MAX_Y = 255;
	
	public final static String[] EXAMPLE_VECTOR_COMPONENTS = {"-1", "-0.5", "0", "0.5", "1", "~-1", "~-0.5", "~0", "~0.5", "~1"};
	public final static String[] EXAMPLE_SIZES = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "12", "16", "32", "64"};
	public final static String[] EXAMPLE_BOOLEANS = {"true", "false"};
	public final static String[] EXAMPLE_DURATIONS = {"500", "1000", "2000", "5000", "10000", "60000", "120000"};
	public final static String[] EXAMPLE_PERCENTAGES = {"0", "0.1", "0.25", "0.5", "0.75", "1"};
	
	public final static String[] OTHER_PARAMETERS = {
		"transparent", "target", "target_type", "range", "duration", "player"
	};
	
	public final static String[] WORLD_PARAMETERS = {
		"pworld", "tworld", "otworld", "t2world"
	};
	
	protected final static Set<String> worldParameterMap = new HashSet<String>(Arrays.asList(WORLD_PARAMETERS));
	
	public final static String[] VECTOR_PARAMETERS = {
		"px", "py", "pz", "pdx", "pdy", "pdz", "tx", "ty", "tz", "otx", "oty", "otz", "t2x", "t2y", "t2z"
	};

	protected final static Set<String> vectorParameterMap = new HashSet<String>(Arrays.asList(VECTOR_PARAMETERS));
	
	public final static String[] BOOLEAN_PARAMETERS = {
		"allow_max_range", "prevent_passthrough", "bypass_build", "bypass_pvp", "target_npc"
	};

	protected final static Set<String> booleanParameterMap = new HashSet<String>(Arrays.asList(BOOLEAN_PARAMETERS));
	
	public final static String[] PERCENTAGE_PARAMETERS = {
		"fizzle_chance", "backfire_chance", "cooldown_reduction"
	};
	
	protected final static Set<String> percentageParameterMap = new HashSet<String>(Arrays.asList(PERCENTAGE_PARAMETERS));
	
	private static final String BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.plugins.magic.spell.builtin";
	
	public final static String[] COMMON_PARAMETERS = (String[])
		ArrayUtils.addAll(
			ArrayUtils.addAll(
					ArrayUtils.addAll(
							ArrayUtils.addAll(VECTOR_PARAMETERS, BOOLEAN_PARAMETERS), 
							OTHER_PARAMETERS
					),
					WORLD_PARAMETERS
			), 
			PERCENTAGE_PARAMETERS
		);
	
	/*
	 * protected members that are helpful to use
	 */
	protected MagicController				controller;
	protected Mage 							mage;
	protected Location    					location;

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

	protected ConfigurationSection parameters = null;

	/*
	 * private data
	 */

	private float                               cooldownReduction       = 0;
	private float                               costReduction           = 0;
	
	private int                                 cooldown                = 0;
	private int                                 duration                = 0;
	private long                                lastCast                = 0;
	private long								castCount				= 0;

	private boolean								isActive				= false;
	
	private Map<SpellResult, List<EffectPlayer>> effects				= new HashMap<SpellResult, List<EffectPlayer>>();
	
	private float								fizzleChance			= 0.0f;
	private float								backfireChance			= 0.0f;

	/**
	 * Default constructor, used to register spells.
	 * 
	 */
	public Spell()
	{
	}

	public static Spell loadSpell(String name, ConfigurationSection node, MagicController controller)
	{
		String className = node.getString("class");
		if (className == null) return null;

		if (className.indexOf('.') <= 0)
		{
			className = BUILTIN_CLASSPATH + "." + className;
		}

		Class<?> spellClass = null;
		try
		{
			spellClass = Class.forName(className);
		}
		catch (Throwable ex)
		{
			controller.getLogger().warning("Error loading spell: " + className);
			ex.printStackTrace();
			return null;
		}

		Object newObject;
		try
		{
			newObject = spellClass.newInstance();
		}
		catch (Throwable ex)
		{

			controller.getLogger().warning("Error loading spell: " + className);
			ex.printStackTrace();
			return null;
		}

		if (newObject == null || !(newObject instanceof Spell))
		{
			controller.getLogger().warning("Error loading spell: " + className + ", does it extend Spell?");
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
			if (!cost.has(this))
			{
				deactivate();
				return;
			}
			
			cost.use(this);
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
	
	public void deactivate() {
		onDeactivate();
		
		mage.deactivateSpell(this);
		isActive = false;
	}
	
	protected List<CastingCost> parseCosts(ConfigurationSection node) {
		if (node == null) {
			return null;
		}
		List<CastingCost> castingCosts = new ArrayList<CastingCost>();
		Set<String> costKeys = node.getKeys(false);
		for (String key : costKeys)
		{
			castingCosts.add(new CastingCost(key, node.getDouble(key, 1)));
		}
		
		return castingCosts;
	}

	// Override to load custom non-parameter data.
	public void configure(ConfigurationSection node) {
	}

	public void loadTemplate(String key, ConfigurationSection node)
	{
		this.key = key;
		this.loadTemplate(node);
	}

	@SuppressWarnings("unchecked")
	protected void loadTemplate(ConfigurationSection node)
	{
		// Get localizations
		name = this.key;
		name = node.getString("name", name);
		name = Messages.get("spells." + key + ".name", name);
		description = node.getString("description", "");
		description = Messages.get("spells." + key + ".description", description);
		usage = Messages.get("spells." + key + ".usage", usage);

		// Load basic properties
		icon = ConfigurationUtils.getMaterialAndData(node, "icon", icon);
		category = node.getString("category", category);
		parameters = node.getConfigurationSection("parameters");
		costs = parseCosts(node.getConfigurationSection("costs"));
		activeCosts = parseCosts(node.getConfigurationSection("active_costs"));
		
		// Load effects ... Config API is kind of ugly here, and I'm not actually
		// sure this is valid YML... :\
		effects.clear();
		if (node.contains("effects")) {
			ConfigurationSection effectsNode = node.getConfigurationSection("effects");
			for (SpellResult resultType : SpellResult.values()) {
				String typeName = resultType.name().toLowerCase();
				if (effectsNode.contains(typeName)) {
					Collection<ConfigurationSection> effectNodes = ConfigurationUtils.getNodeList(effectsNode, typeName);
			        if (effectNodes != null) 
			        {
			        	List<EffectPlayer> players = new ArrayList<EffectPlayer>();
			            for (ConfigurationSection effectValues : effectNodes)
			            {
		                    if (effectValues.contains("class")) {
		                    	String effectClass = effectValues.getString("class");
			                    try {
			                    	Class<?> genericClass = Class.forName("com.elmakers.mine.bukkit.effects." + effectClass);
			                    	if (!EffectPlayer.class.isAssignableFrom(genericClass)) {
			                    		throw new Exception("Must extend EffectPlayer");
			                    	}
			                    	
									Class<? extends EffectPlayer> playerClass = (Class<? extends EffectPlayer>)genericClass;
				                    EffectPlayer player = playerClass.newInstance();
				                    player.load(controller.getPlugin(), effectValues);
				                    players.add(player);
			                    } catch (Exception ex) {
			                    	ex.printStackTrace();
			                    	controller.getLogger().info("Error creating effect class: " + effectClass + " " + ex.getMessage());
			                    }
		                    }
			            }
			            
			            effects.put(resultType, players);
			        }
				}
			}
		}
		
		// Populate default effects
		initializeDefaultSound(SpellResult.FAIL, Sound.NOTE_BASS_DRUM, 0.9f, 1.2f);
		initializeDefaultSound(SpellResult.INSUFFICIENT_RESOURCES, Sound.NOTE_BASS, 1.0f, 1.2f);
		initializeDefaultSound(SpellResult.INSUFFICIENT_PERMISSION, Sound.NOTE_BASS, 1.1f, 1.5f);
		initializeDefaultSound(SpellResult.COOLDOWN, Sound.NOTE_SNARE_DRUM, 1.1f, 0.9f);
		initializeDefaultSound(SpellResult.NO_TARGET, Sound.NOTE_STICKS, 1.1f, 0.9f);
		
		if (!effects.containsKey(SpellResult.TARGET_SELECTED)) {
			List<EffectPlayer> effectList = new ArrayList<EffectPlayer>();
			EffectPlayer targetHighlight = new EffectSingle(controller.getPlugin());
			targetHighlight.setSound(Sound.ANVIL_USE);
			targetHighlight.setParticleType(ParticleType.HAPPY_VILLAGER);
			targetHighlight.setLocationType("target");
			targetHighlight.setOffset(0.5f, 0.5f, 0.5f);
			effectList.add(targetHighlight);
			EffectPlayer trail = new EffectTrail(controller.getPlugin());
			trail.setParticleType(ParticleType.WATER_DRIPPING);
			effectList.add(trail);
			effects.put(SpellResult.TARGET_SELECTED, effectList);
		}
		
		if (!effects.containsKey(SpellResult.COST_FREE) && effects.containsKey(SpellResult.CAST)) {
			effects.put(SpellResult.COST_FREE, effects.get(SpellResult.CAST));
		}
	}
	
	protected void initializeDefaultSound(SpellResult result, Sound sound, float volume, float pitch) {
		if (effects.containsKey(result)) return;
		
		EffectPlayer defaultEffect = new EffectSingle(controller.getPlugin());
		defaultEffect.setSound(sound, volume, pitch);
		List<EffectPlayer> effectList = new ArrayList<EffectPlayer>();
		effectList.add(defaultEffect);
		effects.put(result, effectList);
	}

	public void setMage(Mage mage)
	{
		this.mage = mage;
	}

	public boolean isMatch(String spell, String[] params)
	{
		if (params == null) params = new String[0];
		return (key.equalsIgnoreCase(spell) && parameters.equals(params));
	}

	public int compareTo(com.elmakers.mine.bukkit.api.spell.SpellTemplate other)
	{
		return name.compareTo(other.getName());
	}

	static public void addParameters(String[] extraParameters, ConfigurationSection parameters)
	{
		if (extraParameters != null)
		{
			for (int i = 0; i < extraParameters.length - 1; i += 2)
			{
				ConfigurationUtils.set(parameters, extraParameters[i], extraParameters[i + 1]);
			}
		}
	}
	
	protected void preCast()
	{
		
	}

	protected void reset()
	{
		Location mageLocation = mage != null ? mage.getLocation() : null;
		
		// Kind of a hack, but assume the default location has no direction.
		if (this.location != null && mageLocation != null) {
			this.location.setPitch(mageLocation.getPitch());
			this.location.setYaw(mageLocation.getYaw());
		}
	}

	public boolean cast(String[] extraParameters, Location defaultLocation)
	{
		this.location = defaultLocation;
		this.reset();
		
		if (this.parameters == null) {
			this.parameters = new MemoryConfiguration();
		}
		final ConfigurationSection parameters = new MemoryConfiguration();
		ConfigurationUtils.addConfigurations(parameters, this.parameters);
		addParameters(extraParameters, parameters);
		processParameters(parameters);
		
		this.preCast();
		
		// Check cooldowns
		cooldown = parameters.getInt("cooldown", cooldown);
		cooldown = parameters.getInt("cool", cooldown);
		
		long currentTime = System.currentTimeMillis();
		double cooldownReduction = mage.getCooldownReduction() + this.cooldownReduction;
		if (cooldownReduction < 1 && !isActive && cooldown > 0) {
			int reducedCooldown = (int)Math.ceil((1.0f - cooldownReduction) * cooldown);
			if (lastCast != 0 && lastCast > currentTime - reducedCooldown)
			{
				long seconds = (lastCast - (currentTime - reducedCooldown)) / 1000;
				if (seconds > 60 * 60 ) {
					long hours = seconds / (60 * 60);
					sendMessage(Messages.get("cooldown.wait_hours").replace("$hours", ((Long)hours).toString()));					
				} else if (seconds > 60) {
					long minutes = seconds / 60;
					sendMessage(Messages.get("cooldown.wait_minutes").replace("$minutes", ((Long)minutes).toString()));					
				} else if (seconds > 1) {
					sendMessage(Messages.get("cooldown.wait_seconds").replace("$seconds", ((Long)seconds).toString()));
				} else {
					sendMessage(Messages.get("cooldown.wait_moment"));
				}
				processResult(SpellResult.COOLDOWN);
				return false;
			}
		}

		if (costs != null && !isActive)
		{
			for (CastingCost cost : costs)
			{
				if (!cost.has(this))
				{
					String baseMessage = Messages.get("costs.insufficient_resources");
					String costDescription = cost.getDescription(mage);
					sendMessage(baseMessage.replace("$cost", costDescription));
					processResult(SpellResult.INSUFFICIENT_RESOURCES);
					return false;
				}
			}
		}
		
		if (!canCast()) {
			processResult(SpellResult.INSUFFICIENT_PERMISSION);
			return false;
		}
		
		return finalizeCast(parameters);
	}
	
	protected boolean canCast() {
		return true;
	}
	
	protected void onBackfire() {
		
	}
	
	protected boolean finalizeCast(ConfigurationSection parameters) {
		SpellResult result = null;
		if (!mage.isSuperPowered()) {
			if (backfireChance > 0 && Math.random() < backfireChance) {
				onBackfire();
				onCast(parameters);
				result = SpellResult.BACKFIRE;
			} else if (fizzleChance > 0 && Math.random() < fizzleChance) {
				result = SpellResult.FIZZLE;
			}
		}
		
		if (result == null) {
			result = onCast(parameters);
		}
		processResult(result);
		
		if (result.isSuccess()) {
			lastCast = System.currentTimeMillis();
			if (costs != null) {
				for (CastingCost cost : costs)
				{
					cost.use(this);
				}
			}
			castCount++;
		}
		
		return result.isSuccess();
	}
	
	public String getMessage(String messageKey) {
		return getMessage(messageKey, "");
	}
	
	public String getMessage(String messageKey, String def) {
		String message = Messages.get("spells.default." + messageKey, def);
		message = Messages.get("spells." + key + "." + messageKey, message);
		if (message == null) message = "";
		
		// Escape some common parameters
		String playerName = mage.getName();
		message = message.replace("$player", playerName);
		
		String materialName = getDisplayMaterialName();
		message = message.replace("$material", materialName);
		
		return message;
	}

	protected String getDisplayMaterialName()
	{
		return "None";
	}
	
	protected void processResult(SpellResult result) {
		if (mage != null) {
			mage.processResult(result);
		}
		if (result.isSuccess()) {
			// Notify controller of successful casts,
			// this if for dynmap display or other global-level processing.
			controller.onCast(mage, this, result);
		}
		
		// Show messaging
		if (result == SpellResult.CAST) {
			String message = getMessage(result.name().toLowerCase());
			Player player = mage.getPlayer();
			Entity targetEntity = getTargetEntity();
			if (targetEntity == player) {
				message = getMessage("cast_self", message);
			} else if (targetEntity instanceof Player) {
				message = getMessage("cast_player", message);
				String playerMessage = getMessage("cast_player_message");
				if (playerMessage.length() > 0) {
					playerMessage = playerMessage.replace("$spell", getName());
					Player targetPlayer = (Player)targetEntity;
					Mage targetMage = controller.getMage(targetPlayer);
					targetMage.sendMessage(playerMessage);
				}
			} else if (targetEntity instanceof LivingEntity) {
				message = getMessage("cast_livingentity", message);
			} else if (targetEntity instanceof Entity) {
				message = getMessage("cast_entity", message);
			}
			castMessage(message);
		} else {
			sendMessage(getMessage(result.name().toLowerCase()));
		}
		
		// Play effects
		Location mageLocation = getEffectLocation();
		if (effects.containsKey(result) && mageLocation != null) {
			Location targetLocation = getTargetLocation();
			List<EffectPlayer> resultEffects = effects.get(result);
			for (EffectPlayer player : resultEffects) {
				// Set material and color
				player.setMaterial(getEffectMaterial());
				player.setColor(mage.getEffectColor());
				player.start(mageLocation, targetLocation);
			}
		}
	}
	
	public Location getTargetLocation() {
		return null;
	}
	
	public Entity getTargetEntity() {
		return null;
	}
	
	public MaterialAndData getEffectMaterial()
	{
		return new MaterialAndData(DEFAULT_EFFECT_MATERIAL);
	}

	protected void processParameters(ConfigurationSection parameters) {
		duration = parameters.getInt("duration", duration);
		
		fizzleChance = (float)parameters.getDouble("fizzle_chance", fizzleChance);
		backfireChance = (float)parameters.getDouble("backfire_chance", backfireChance);
	
		Location defaultLocation = location == null ? mage.getLocation() : location;
		Location locationOverride = ConfigurationUtils.getLocationOverride(parameters, "p", defaultLocation);
		if (locationOverride != null) {
			location = locationOverride;
		}
		costReduction = (float)parameters.getDouble("cost_reduction", 0);
		cooldownReduction = (float)parameters.getDouble("cooldown_reduction", 0);
	}

	public String getPermissionNode()
	{
		return "Magic.cast." + key;
	}

	public boolean hasSpellPermission(CommandSender sender)
	{
		if (sender == null) return true;

		return controller.hasPermission(sender, getPermissionNode(), true);
	}
	
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
	
	/**
	 * Used internally to initialize the Spell, do not call.
	 * 
	 * @param instance The spells instance
	 */
	public void initialize(MagicController instance)
	{
		this.controller = instance;
	}

	/**
	 * Called by the Spells plugin to cancel this spell, do not call.
	 * 
	 * @param plugin The Spells plugin instance
	 * @param player The player cancelling selection
	 */
	public boolean cancel()
	{
		boolean cancelled = onCancel();
		if (cancelled) {
			sendMessage(getMessage("cancel"));
		}
		return cancelled;
	}
	
	public long getCastCount()
	{
		return castCount;
	}
	
	public void onActivate() {
		
	}
	
	public void onDeactivate() {

	}
	
	public Mage getMage() {
		return mage;
	}
	
	public void load(ConfigurationSection node) {
		try {
			castCount = node.getLong("cast_count", 0);
			lastCast = node.getLong("last_cast", 0);
			onLoad(node);
		} catch (Exception ex) {
			controller.getPlugin().getLogger().warning("Failed to load data for spell " + name + ": " + ex.getMessage());
		}
	}
	
	public void save(ConfigurationSection node) {
		try {
			node.set("cast_count", castCount);
			node.set("last_cast", lastCast);
			onSave(node);
		} catch (Exception ex) {
			controller.getPlugin().getLogger().warning("Failed to save data for spell " + name);
			ex.printStackTrace();
		}
	}

	/**
	 * Called on player data load.
	 */
	public void onLoad(ConfigurationSection node)
	{
		
	}

	/**
	 * Called on player data save.
	 * 
	 * @param node The configuration node to load data from.
	 */
	public void onSave(ConfigurationSection node)
	{

	}
	
	//
	// Cloneable implementation
	//
	
	public Object clone()
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
	
	//
	// CostReducer Implementation
	//
	
	public float getCostReduction()
	{
		return costReduction + mage.getCostReduction();
	}
	
	public boolean usesMana() 
	{
		return mage.usesMana();
	}
	
	//
	// Public API Implementation
	//
	
	public boolean cast()
	{
		return cast(new String[0], null);
	}
	
	public boolean cast(String[] extraParameters)
	{
		return cast(extraParameters, null);
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
	
	public Collection<com.elmakers.mine.bukkit.api.spell.CastingCost> getCosts() {
		if (costs == null) return null;
		List<com.elmakers.mine.bukkit.api.spell.CastingCost> copy = new ArrayList<com.elmakers.mine.bukkit.api.spell.CastingCost>();
		copy.addAll(costs);
		return copy;
	}
	
	public Collection<com.elmakers.mine.bukkit.api.spell.CastingCost> getActiveCosts() {
		if (activeCosts == null) return null;
		List<com.elmakers.mine.bukkit.api.spell.CastingCost> copy = new ArrayList<com.elmakers.mine.bukkit.api.spell.CastingCost>();
		copy.addAll(activeCosts);
		return copy;
	}
	
	public void getParameters(Collection<String> parameters)
	{
		parameters.addAll(Arrays.asList(COMMON_PARAMETERS));
	}
	
	public void getParameterOptions(Collection<String> examples, String parameterKey)
	{
		if (parameterKey.equals("duration")) {
			examples.addAll(Arrays.asList(EXAMPLE_DURATIONS));
		} else if (parameterKey.equals("range")) {
			examples.addAll(Arrays.asList(EXAMPLE_SIZES));
		} else if (parameterKey.equals("transparent")) {
			examples.addAll(controller.getMaterialSets());
		} else if (parameterKey.equals("player")) {
			examples.addAll(controller.getPlugin().getPlayerNames());
		} else if (parameterKey.equals("target")) {
			TargetType[] targetTypes = TargetType.values();
			for (TargetType targetType : targetTypes) {
				examples.add(targetType.name().toLowerCase());
			}
		} else if (parameterKey.equals("target")) {
			TargetType[] targetTypes = TargetType.values();
			for (TargetType targetType : targetTypes) {
				examples.add(targetType.name().toLowerCase());
			}
		} else if (parameterKey.equals("target_type")) {
			EntityType[] entityTypes = EntityType.values();
			for (EntityType entityType : entityTypes) {
				examples.add(entityType.name().toLowerCase());
			}
		} else if (booleanParameterMap.contains(parameterKey)) {
			examples.addAll(Arrays.asList(EXAMPLE_BOOLEANS));
		} else if (vectorParameterMap.contains(parameterKey)) {
			examples.addAll(Arrays.asList(EXAMPLE_VECTOR_COMPONENTS));
		} else if (worldParameterMap.contains(parameterKey)) {
			List<World> worlds = Bukkit.getWorlds();
			for (World world : worlds) {
				examples.add(world.getName());
			}
		} else if (percentageParameterMap.contains(parameterKey)) {
			examples.addAll(Arrays.asList(EXAMPLE_PERCENTAGES));
		} 
	}
	
	//
	// Spell abstract interface
	//


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
	public abstract SpellResult onCast(ConfigurationSection parameters);

	protected abstract Location getEffectLocation();
	public abstract void sendMessage(String message);
	public abstract void castMessage(String message);
}
