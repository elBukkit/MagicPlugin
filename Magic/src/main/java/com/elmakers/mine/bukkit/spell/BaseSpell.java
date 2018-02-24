package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.action.CastContext;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.batch.SpellBatch;
import com.elmakers.mine.bukkit.api.data.SpellData;
import com.elmakers.mine.bukkit.api.event.CastEvent;
import com.elmakers.mine.bukkit.api.event.PreCastEvent;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.*;
import com.elmakers.mine.bukkit.api.spell.SpellCategory;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.effect.EffectPlayer;
import com.elmakers.mine.bukkit.magic.ParameterizedConfiguration;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import de.slikey.effectlib.math.EquationTransform;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.BlockCommandSender;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

public abstract class BaseSpell implements MageSpell, Cloneable {
    public static String DEFAULT_DISABLED_ICON_URL = "";

    protected static final double LOOK_THRESHOLD_RADIANS = 0.9;

    // TODO: Config-drive
    protected static final int MIN_Y = 1;

    // TODO: Configurable default? this does look cool, though.
    protected final static Material DEFAULT_EFFECT_MATERIAL = Material.WATER;

    public final static String[] EXAMPLE_VECTOR_COMPONENTS = {"-1", "-0.5", "0", "0.5", "1", "~-1", "~-0.5", "~0", "~0.5", "*1", "*-1", "*-0.5", "*0.5", "*1"};
    public final static String[] EXAMPLE_SIZES = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "12", "16", "32", "64"};
    public final static String[] EXAMPLE_INTEGERS = {"-10", "-5", "-1", "1", "5", "10"};
    public final static String[] EXAMPLE_BOOLEANS = {"true", "false"};
    public final static String[] EXAMPLE_DURATIONS = {"500", "1000", "2000", "5000", "10000", "60000", "120000"};
    public final static String[] EXAMPLE_PERCENTAGES = {"0", "0.1", "0.25", "0.5", "0.75", "1"};

    public final static String[] OTHER_PARAMETERS = {
        "transparent", "target", "target_type", "range", "duration", "player"
    };

    public final static String[] WORLD_PARAMETERS = {
        "pworld", "tworld", "otworld", "t2world"
    };

    protected final static Set<String> worldParameterMap = new HashSet<>(Arrays.asList(WORLD_PARAMETERS));

    public final static String[] VECTOR_PARAMETERS = {
        "px", "py", "pz", "pdx", "pdy", "pdz", "tx", "ty", "tz", "otx", "oty", "otz", "t2x", "t2y", "t2z",
        "otdx", "otdy", "otdz"
    };

    protected final static Set<String> vectorParameterMap = new HashSet<>(Arrays.asList(VECTOR_PARAMETERS));

    public final static String[] BOOLEAN_PARAMETERS = {
        "allow_max_range", "prevent_passthrough", "reverse_targeting", "passthrough", "bypass_protection",
        "bypass", "bypass_build", "bypass_break", "bypass_pvp", "target_npc", "ignore_blocks", "target_self"
    };

    protected final static Set<String> booleanParameterMap = new HashSet<>(Arrays.asList(BOOLEAN_PARAMETERS));

    public final static String[] PERCENTAGE_PARAMETERS = {
        "fizzle_chance", "backfire_chance", "cooldown_reduction"
    };

    protected final static Set<String> percentageParameterMap = new HashSet<>(Arrays.asList(PERCENTAGE_PARAMETERS));

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
    
    private static final DecimalFormat RANGE_FORMATTER = new DecimalFormat("0.#");
    private static final DecimalFormat SECONDS_FORMATTER = new DecimalFormat("0.##");

    /*
     * protected members that are helpful to use
     */
    protected MageController				controller;
    protected Mage 							mage;
    protected Location    					location;
    protected CastContext currentCast;

    /*
     * Variant properties
     */
    private SpellKey spellKey;
    private SpellData spellData;
    private String inheritKey;
    private String name;
    private String alias;
    private String description;
    private String extendedDescription;
    private String levelDescription;
    private String progressDescription;
    private String upgradeDescription;
    private String usage;
    private double worth;
    private int earns;
    private Color color;
    private String particle;
    private SpellCategory category;
    private Set<String> tags;
    private BaseSpell template;
    private long requiredUpgradeCasts;
    private String requiredUpgradePath;
    private String requiredSkillapiClass;
    private String requiredSkillapiSkill;
    private Set<String> requiredUpgradeTags;
    private Collection<PrerequisiteSpell> requiredSpells;
    private List<SpellKey> removesSpells;
    private MaterialAndData icon = new MaterialAndData(Material.AIR);
    private MaterialAndData disabledIcon = null;
    private String iconURL = null;
    private String iconDisabledURL = null;
    private List<CastingCost> costs = null;
    private List<CastingCost> activeCosts = null;

    protected double cancelOnDamage             = 0;
    protected boolean cancelOnCastOther         = false;
    protected boolean cancelOnNoPermission      = false;
    protected boolean pvpRestricted           	= false;
    protected boolean disguiseRestricted        = false;
    protected boolean worldBorderRestricted     = true;
    protected boolean glideRestricted           = false;
    protected boolean glideExclusive            = false;
    protected boolean usesBrushSelection        = false;
    protected boolean bypassFriendlyFire    	= false;
    protected boolean onlyFriendlyFire    	    = false;
    protected boolean bypassPvpRestriction    	= false;
    protected boolean bypassBuildRestriction    = false;
    protected boolean bypassBreakRestriction    = false;
    protected boolean bypassProtection          = false;
    protected boolean bypassConfusion             = true;
    protected boolean bypassWeakness              = true;
    protected boolean bypassPermissions           = false;
    protected boolean bypassRegionPermission      = false;
    protected boolean castOnNoTarget              = true;
    protected boolean bypassDeactivate            = false;
    protected boolean bypassAll                   = false;
    protected boolean quiet                       = false;
    protected boolean loud                        = false;
    protected boolean messageTargets              = true;
    protected boolean targetSelf                  = false;
    protected boolean showUndoable              = true;
    protected boolean cancellable               = true;
    protected boolean quickCast                 = false;
    protected boolean cancelEffects = false;
    protected boolean commandBlockAllowed       = true;
    protected int                               verticalSearchDistance  = 8;

    private boolean backfired                   = false;
    private boolean hidden                      = false;
    private boolean passive                     = false;

    protected ConfigurationSection progressLevels = null;
    protected ConfigurationSection progressLevelParameters = null;
    protected ParameterizedConfiguration parameters = new ParameterizedConfiguration();
    protected ConfigurationSection workingParameters = null;
    protected ConfigurationSection configuration = null;

    protected static Random random            = new Random();

    /*
     * private data
     */

    private long                                requiredCastsPerLevel   = 0;
    private long                                maxLevels               = 0;
    private Map<String, EquationTransform>      progressLevelEquations  = new HashMap<>();

    private float                               cooldownReduction       = 0;
    private float                               costReduction           = 0;
    private float                               consumeReduction        = 0;

    private boolean                             bypassMageCooldown      = false;
    private int                                 mageCooldown            = 0;
    private int                                 cooldown                = 0;
    private int                                 displayCooldown         = -1;
    private int                                 warmup                  = 0;
    private int                                 earnCooldown            = 0;
    private int                                 duration                = 0;
    private int                                 totalDuration           = -1;
    private long                                lastActiveCost          = 0;
    private float                               activeCostScale         = 1;

    private Map<String, Collection<EffectPlayer>>     effects				= new HashMap<>();

    private float								fizzleChance			= 0.0f;
    private float								backfireChance			= 0.0f;

    private long 								lastMessageSent 			= 0;
    private Set<Material>						preventPassThroughMaterials = null;
    private Set<Material>                       passthroughMaterials = null;
    private Set<Material>						unsafeMaterials = null;

    public boolean allowPassThrough(Material mat)
    {
        if (mage != null && mage.isSuperPowered()) {
            return true;
        }
        if (passthroughMaterials != null && passthroughMaterials.contains(mat)) {
            return true;
        }
        return preventPassThroughMaterials == null || !preventPassThroughMaterials.contains(mat);
    }

    public boolean isPassthrough(Material mat)
    {
        return passthroughMaterials != null && passthroughMaterials.contains(mat);
    }

    /*
     * Ground / location search and test functions
     */
    public boolean isOkToStandIn(Material mat)
    {
        if (isHalfBlock(mat)) {
            return false;
        }
        return passthroughMaterials.contains(mat) && !unsafeMaterials.contains(mat);
    }

    public boolean isWater(Material mat)
    {
        return (mat == Material.WATER || mat == Material.STATIONARY_WATER);
    }

    public boolean isOkToStandOn(Block block)
    {
        return isOkToStandOn(block.getType());
    }

    protected boolean isHalfBlock(Material mat) {

        // TODO: Data-driven half-block list
        // Don't put carpet and snow in here, acts weird. Not sure why though.
        return (mat == Material.STEP || mat == Material.WOOD_STEP);
    }

    public boolean isOkToStandOn(Material mat)
    {
        if (isHalfBlock(mat)) {
            return true;
        }
        return (mat != Material.AIR && !unsafeMaterials.contains(mat) && !passthroughMaterials.contains(mat));
    }

    public boolean isSafeLocation(Block block)
    {
        if (!block.getChunk().isLoaded()) {
            block.getChunk().load(true);
            return false;
        }

        if (block.getY() > CompatibilityUtils.getMaxHeight(block.getWorld())) {
            return false;
        }

        Block blockOneUp = block.getRelative(BlockFace.UP);
        Block blockOneDown = block.getRelative(BlockFace.DOWN);

        // Ascend to top of water
        if (isUnderwater() && (blockOneDown.getType() == Material.STATIONARY_WATER || blockOneDown.getType() == Material.WATER)
            && blockOneUp.getType() == Material.AIR && block.getType() == Material.AIR) {
            return true;
        }

        Player player = mage.getPlayer();
        return (
                (isOkToStandOn(blockOneDown) || (player != null && player.isFlying()))
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
        int maxHeight = CompatibilityUtils.getMaxHeight(targetLoc.getWorld());
        return tryFindPlaceToStand(targetLoc, maxHeight, maxHeight);
    }

    public Location findPlaceToStand(Location targetLoc)
    {
        return findPlaceToStand(targetLoc, verticalSearchDistance, verticalSearchDistance);
    }

    public Location tryFindPlaceToStand(Location targetLoc, int maxDownDelta, int maxUpDelta)
    {
        Location location = findPlaceToStand(targetLoc, maxDownDelta, maxUpDelta);
        return location == null ? targetLoc : location;
    }

    public Location findPlaceToStand(Location targetLoc, int maxDownDelta, int maxUpDelta)
    {
        if (!targetLoc.getBlock().getChunk().isLoaded()) return null;
        int minY = MIN_Y;
        int maxY = CompatibilityUtils.getMaxHeight(targetLoc.getWorld());
        int targetY = targetLoc.getBlockY();
        if (targetY >= minY && targetY <= maxY && isSafeLocation(targetLoc)) {
            return checkForHalfBlock(targetLoc);
        }

        Location location = null;
        if (targetY < minY) {
            location = targetLoc.clone();
            location.setY(minY);
            location = findPlaceToStand(location, true, maxUpDelta);
        } else if (targetY > maxY) {
            location = targetLoc.clone();
            location.setY(maxY);
            location = findPlaceToStand(location, false, maxDownDelta);
        } else {
            // First look up just a little bit
            int testMaxY = Math.min(maxUpDelta, 3);
            location = findPlaceToStand(targetLoc, true, testMaxY);

            // Then  look down just a little bit
            if (location == null) {
                int testMinY = Math.min(maxDownDelta, 4);
                location = findPlaceToStand(targetLoc, false, testMinY);
            }

            // Then look all the way up
            if (location == null) {
                location = findPlaceToStand(targetLoc, true, maxUpDelta);
            }

            // Then look allll the way down.
            if (location == null) {
                location = findPlaceToStand(targetLoc, false, maxDownDelta);
            }
        }
        return location;
    }

    public Location findPlaceToStand(Location target, boolean goUp)
    {
        return findPlaceToStand(target, goUp, verticalSearchDistance);
    }

    public Location findPlaceToStand(Location target, boolean goUp, int maxDelta)
    {
        int direction = goUp ? 1 : -1;

        // search for a spot to stand
        Location targetLocation = target.clone();
        int yDelta = 0;
        int minY = MIN_Y;
        int maxY = CompatibilityUtils.getMaxHeight(targetLocation.getWorld());

        while (minY <= targetLocation.getY() && targetLocation.getY() <= maxY && yDelta < maxDelta)
        {
            Block block = targetLocation.getBlock();
            if (isSafeLocation(block))
            {
                // spot found - return location
                return checkForHalfBlock(targetLocation);
            }

            if (!allowPassThrough(block.getType())) {
                return null;
            }

            yDelta++;
            targetLocation.setY(targetLocation.getY() + direction);
        }

        // no spot found
        return null;
    }

    protected Location checkForHalfBlock(Location location) {
        // This is a hack, but data-driving would be a pain.
        boolean isHalfBlock = false;
        Block downBlock = location.getBlock().getRelative(BlockFace.DOWN);
        Material material = downBlock.getType();
        if (material == Material.STEP || material == Material.WOOD_STEP) {
            // Drop down to half-steps
            isHalfBlock = (DeprecatedUtils.getData(downBlock) < 8);
        } else {
            isHalfBlock = isHalfBlock(material);
        }
        if (isHalfBlock) {
            location.setY(location.getY() - 0.5);
        }

        return location;
    }

    /**
     * Get the block the player is standing on.
     *
     * @return The Block the player is standing on
     */
    public Block getPlayerBlock()
    {
        Location location = getLocation();
        if (location == null) return null;
        return location.getBlock().getRelative(BlockFace.DOWN);
    }

    /**
     * Get the direction the player is facing as a BlockFace.
     *
     * @return a BlockFace representing the direction the player is facing
     */
    public BlockFace getPlayerFacing()
    {
        return getFacing(getLocation());
    }

    public static BlockFace getFacing(Location location)
    {
        float playerRot = location.getYaw();
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
    @Override
    public void castMessage(String message)
    {
        Wand activeWand = mage.getActiveWand();
        // First check wand
        if (!loud && activeWand != null && !activeWand.showCastMessages()) return;

        if (!quiet && canSendMessage() && message != null && message.length() > 0)
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
    @Override
    public void sendMessage(String message)
    {
        Wand activeWand = mage.getActiveWand();

        // First check wand
        if (!loud && activeWand != null && !activeWand.showMessages()) return;

        if (!quiet && message != null && message.length() > 0)
        {
            mage.sendMessage(message);
            lastMessageSent = System.currentTimeMillis();
        }
    }

    @Override
    public Location getLocation()
    {
        if (location != null) return location.clone();
        if (mage != null) {
            return mage.getLocation();
        }
        return null;
    }

    public Location getWandLocation()
    {
        if (this.location != null)
        {
            return location.clone();
        }
        return mage.getWandLocation();
    }

    public Location getCastLocation()
    {
        if (this.location != null)
        {
            return location.clone();
        }
        return mage.getCastLocation();
    }

    @Override
    public Location getEyeLocation()
    {
        if (this.location != null)
        {
            return location.clone();
        }
        return mage.getEyeLocation();
    }

    @Override
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

    protected String getBlockSkin(Material blockType) 
    {
        return controller.getBlockSkin(blockType);
    }

    protected String getMobSkin(EntityType mobType)
    {
        return controller.getMobSkin(mobType);
    }

    public static Collection<PotionEffect> getPotionEffects(ConfigurationSection parameters)
    {
        return getPotionEffects(parameters, null);
    }

    public static Collection<PotionEffect> getPotionEffects(ConfigurationSection parameters, Integer duration)
    {
        return getPotionEffects(parameters, duration, true, true);
    }

    public static Collection<PotionEffect> getPotionEffects(ConfigurationSection parameters, Integer duration, boolean ambient, boolean particles)
    {
        List<PotionEffect> effects = new ArrayList<>();
        PotionEffectType[] effectTypes = PotionEffectType.values();
        for (PotionEffectType effectType : effectTypes) {
            // Why is there a null entry in this list? Maybe a 1.7 bug?
            if (effectType == null) continue;

            String parameterName = "effect_" + effectType.getName().toLowerCase();
            if (parameters.contains(parameterName)) {
                String value = parameters.getString(parameterName);

                int ticks = 10;
                int power = 1;
                try {
                    if (value.contains(",")) {
                        String[] pieces = StringUtils.split(value, ',');
                        ticks = (int)Float.parseFloat(pieces[0]);
                        power = (int)Float.parseFloat(pieces[1]);
                    } else {
                        power = (int)Float.parseFloat(value);
                        if (duration != null) {
                            ticks = duration / 50;
                        }
                    }

                } catch (Exception ex) {
                    Bukkit.getLogger().warning("Error parsing potion effect for " + effectType + ": " + value);
                }
                PotionEffect effect = new PotionEffect(effectType, ticks, power, true);
                effects.add(effect);
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

    protected Location getEffectLocation()
    {
        return getEyeLocation();
    }

    @Override
    public boolean hasBrushOverride()
    {
        return false;
    }

    @Override
    public boolean usesBrush()
    {
        return false;
    }

    @Override
    public boolean usesBrushSelection() {
        return (usesBrushSelection || usesBrush()) && !hasBrushOverride();
    }

    @Override
    public boolean isUndoable()
    {
        return false;
    }

    public void checkActiveCosts() {
        if (activeCosts == null) return;

        long now = System.currentTimeMillis();
        activeCostScale = (float)((double)(now - lastActiveCost) / 1000);
        lastActiveCost = now;

        for (CastingCost cost : activeCosts)
        {
            if (!cost.has(this))
            {
                deactivate();
                break;
            }

            cost.use(this);
        }

        activeCostScale = 1;
    }

    public void checkActiveDuration() {
        if (duration > 0 && spellData.getLastCast() < System.currentTimeMillis() - duration) {
            deactivate();
        }
    }

    protected List<CastingCost> parseCosts(ConfigurationSection node) {
        if (node == null) {
            return null;
        }
        List<CastingCost> castingCosts = new ArrayList<>();
        Set<String> costKeys = node.getKeys(false);
        for (String key : costKeys)
        {
            castingCosts.add(new com.elmakers.mine.bukkit.spell.CastingCost(controller, key, node.getInt(key, 1)));
        }

        return castingCosts;
    }

    protected void loadTemplate(ConfigurationSection node)
    {
        // Get localizations
        String baseKey = spellKey.getBaseKey();

        // Message defaults come from the messages.yml file
        name = controller.getMessages().get("spells." + baseKey + ".name", baseKey);
        description = controller.getMessages().get("spells." + baseKey + ".description", "");
        extendedDescription = controller.getMessages().get("spells." + baseKey + ".extended_description", "");
        usage = controller.getMessages().get("spells." + baseKey + ".usage", "");

        // Upgrade path information
        // The actual upgrade spell will be set externally.
        requiredUpgradePath = node.getString("upgrade_required_path");
        if (requiredUpgradePath != null && requiredUpgradePath.isEmpty()) {
            requiredUpgradePath = null;
        }
        requiredUpgradeCasts = node.getLong("upgrade_required_casts");
        List<String> pathTags = ConfigurationUtils.getStringList(node, "upgrade_required_path_tags");
        if (pathTags == null || pathTags.isEmpty()) {
            requiredUpgradeTags = null;
        } else {
            requiredUpgradeTags = new HashSet<>(pathTags);
        }

        requiredSpells = new ArrayList<>();

        List<String> removesSpellKeys = ConfigurationUtils.getStringList(node, "removes_spells");
        if (removesSpellKeys != null) {
            removesSpells = new ArrayList<>(removesSpellKeys.size());
            for (String key : removesSpellKeys) {
                removesSpells.add(new SpellKey(key));
            }
        } else {
            removesSpells = new ArrayList<>(0);
        }

        //required skillAPI stuff
        requiredSkillapiClass = node.getString( "upgrade_required_skillapi_class", "");
        requiredSkillapiSkill = node.getString( "upgrade_required_skillapi_skill", "");

        // Inheritance, currently only used to look up messages, and only goes one level deep
        inheritKey = node.getString("inherit");

        progressDescription = controller.getMessages().get("spell.progress_description", progressDescription);

        // Can be overridden by the base spell, or the variant spell
        levelDescription = controller.getMessages().get("spells." + baseKey + ".level_description", levelDescription);
        progressDescription = controller.getMessages().get("spells." + baseKey + ".progress_description", progressDescription);
        upgradeDescription = controller.getMessages().get("spells." + baseKey + ".upgrade_description", upgradeDescription);

        // Spell level variants can override
        if (spellKey.isVariant()) {
            // Level description defaults to pre-formatted text
            levelDescription = controller.getMessages().get("spell.level_description", levelDescription);

            String variantKey = spellKey.getKey();
            name = controller.getMessages().get("spells." + variantKey + ".name", name);
            description = controller.getMessages().get("spells." + variantKey + ".description", description);
            extendedDescription = controller.getMessages().get("spells." + variantKey + ".extended_description", extendedDescription);
            usage = controller.getMessages().get("spells." + variantKey + ".usage", usage);

            // Any spell may have a level description, including base spells if chosen.
            // Base spells must specify their own level in each spell config though,
            // they don't get an auto-generated one.
            levelDescription = controller.getMessages().get("spells." + variantKey + ".level_description", levelDescription);
            progressDescription = controller.getMessages().get("spells." + variantKey + ".progress_description", progressDescription);
            upgradeDescription = controller.getMessages().get("spells." + variantKey + ".upgrade_description", upgradeDescription);
        }

        // Individual spell configuration overrides all
        name = node.getString("name", name);
        alias = node.getString("alias", "");
        extendedDescription = node.getString("extended_description", extendedDescription);
        description = node.getString("description", description);
        levelDescription = node.getString("level_description", levelDescription);
        progressDescription = node.getString("progress_description", progressDescription);

        // Parameterize level description
        if (levelDescription != null && !levelDescription.isEmpty()) {
            levelDescription = levelDescription.replace("$level", Integer.toString(spellKey.getLevel()));
        }

        // Load basic properties
        icon = ConfigurationUtils.getMaterialAndData(node, "icon", icon);
        disabledIcon = ConfigurationUtils.getMaterialAndData(node, "icon_disabled", null);
        iconURL = node.getString("icon_url");
        iconDisabledURL = node.getString("icon_disabled_url");
        color = ConfigurationUtils.getColor(node, "color", null);
        worth = node.getDouble("worth", 0);
        if (node.contains("worth_sp")) {
            worth = node.getDouble("worth_sp", 0) * controller.getWorthSkillPoints();
        }
        earns = node.getInt("earns_sp", 0);
        earnCooldown = node.getInt("earns_cooldown", 0);
        category = controller.getCategory(node.getString("category"));
        Collection<String> tagList = ConfigurationUtils.getStringList(node, "tags");
        if (tagList != null) {
            tags = new HashSet<>(tagList);
        } else {
            tags = null;
        }

        costs = parseCosts(node.getConfigurationSection("costs"));
        activeCosts = parseCosts(node.getConfigurationSection("active_costs"));
        pvpRestricted = node.getBoolean("pvp_restricted", false);
        quickCast = node.getBoolean("quick_cast", false);
        disguiseRestricted = node.getBoolean("disguise_restricted", false);
        glideRestricted = node.getBoolean("glide_restricted", false);
        glideExclusive = node.getBoolean("glide_exclusive", false);
        worldBorderRestricted = node.getBoolean("world_border_restricted", false);
        usesBrushSelection = node.getBoolean("brush_selection", false);
        castOnNoTarget = node.getBoolean("cast_on_no_target", true);
        hidden = node.getBoolean("hidden", false);
        showUndoable = node.getBoolean("show_undoable", true);
        cancellable = node.getBoolean("cancellable", true);
        cancelEffects = node.getBoolean("cancel_effects", false);

        progressLevels = node.getConfigurationSection("progress_levels");
        if (progressLevels != null) {
            requiredCastsPerLevel = progressLevels.getLong("required_casts_per_level");
            maxLevels = progressLevels.getLong("max_levels");
            if (requiredCastsPerLevel <= 0 && maxLevels > 0) {
                if (requiredUpgradeCasts <= 0) {
                    maxLevels = 0;
                } else {
                    // auto determine casts per level
                    requiredCastsPerLevel = requiredUpgradeCasts / maxLevels;
                }
            }



            progressLevelParameters = progressLevels.getConfigurationSection("parameters");
            if (progressLevelParameters != null) {
                Set<String> keys = progressLevelParameters.getKeys(true);
                progressLevelEquations = new HashMap<>(keys.size());
                for (String key : keys) {
                    if (progressLevelParameters.isString(key)) {
                        String value = progressLevelParameters.getString(key, "");
                        progressLevelEquations.put(key, new EquationTransform(value, "x"));
                    }
                }
            }
        }

        // Preload some parameters
        parameters.setMage(mage);
        parameters.wrap(node.getConfigurationSection("parameters"));
        bypassMageCooldown = parameters.getBoolean("bypass_mage_cooldown", false);
        warmup = parameters.getInt("warmup", 0);
        cooldown = parameters.getInt("cooldown", 0);
        cooldown = parameters.getInt("cool", cooldown);
        mageCooldown = parameters.getInt("cooldown_mage", 0);
        displayCooldown = parameters.getInt("display_cooldown", -1);
        bypassPvpRestriction = parameters.getBoolean("bypass_pvp", false);
        bypassPvpRestriction = parameters.getBoolean("bp", bypassPvpRestriction);
        bypassPermissions = parameters.getBoolean("bypass_permissions", false);
        bypassBuildRestriction = parameters.getBoolean("bypass_build", false);
        bypassBuildRestriction = parameters.getBoolean("bb", bypassBuildRestriction);
        bypassBreakRestriction = parameters.getBoolean("bypass_break", false);
        bypassProtection = parameters.getBoolean("bypass_protection", false);
        bypassProtection = parameters.getBoolean("bp", bypassProtection);
        bypassAll = parameters.getBoolean("bypass", false);
        duration = parameters.getInt("duration", 0);
        totalDuration = parameters.getInt("total_duration", -1);

        effects.clear();
        if (node.contains("effects")) {
            ConfigurationSection effectsNode = node.getConfigurationSection("effects");
            Collection<String> effectKeys = effectsNode.getKeys(false);
            for (String effectKey : effectKeys) {
                if (effectsNode.isString(effectKey)) {
                    String referenceKey = effectsNode.getString(effectKey);
                    if (effects.containsKey(referenceKey)) {
                        effects.put(effectKey, new ArrayList<>(effects.get(referenceKey)));
                    }
                }
                else
                {
                    effects.put(effectKey, EffectPlayer.loadEffects(controller.getPlugin(), effectsNode, effectKey));
                }
            }
        }
    }
    
    @Override
    public void loadPrerequisites(ConfigurationSection node)
    {
        requiredSpells.addAll(ConfigurationUtils.getPrerequisiteSpells(controller, node, "required_spells", "spell " + getKey(), true));
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

        backfired = false;

        if (!this.spellData.isActive())
        {
            this.currentCast = null;
        }
    }

    @Override
    public boolean cast(String[] extraParameters, Location defaultLocation) {
        ConfigurationSection parameters = null;
        if (extraParameters != null && extraParameters.length > 0) {
            parameters = new MemoryConfiguration();
            ConfigurationUtils.addParameters(extraParameters, parameters);
        }
        return cast(parameters, defaultLocation);
    }

    @Override
    public boolean cast(ConfigurationSection parameters)
    {
        return cast(parameters, null);
    }

    @Override
    public boolean cast(ConfigurationSection extraParameters, Location defaultLocation)
    {
        if (mage.isPlayer() && mage.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            if (mage.getDebugLevel() > 0 && extraParameters != null) {
                mage.sendDebugMessage("Cannot cast in spectator mode.");
            }
            return false;
        }
        if (mage.getDebugLevel() > 5 && extraParameters != null) {
            Collection<String> keys = extraParameters.getKeys(false);
            if (keys.size() > 0) {
                mage.sendDebugMessage(ChatColor.BLUE + "Cast " + ChatColor.GOLD + getName() + " " + ChatColor.GREEN + ConfigurationUtils.getParameters(extraParameters));
            }
        }
        
        this.reset();

        Location location = mage.getLocation();
        if (location != null) {
            Set<String> overrides = controller.getSpellOverrides(mage, location);
            if (overrides != null && !overrides.isEmpty())
            {
                if (extraParameters == null) {
                    extraParameters = new MemoryConfiguration();
                }
                for (String entry : overrides) {
                    String[] pieces = StringUtils.split(entry, ' ');
                    if (pieces.length < 2) continue;

                    String fullKey = pieces[0];
                    String[] key = StringUtils.split(fullKey, '.');
                    if (key.length == 0) continue;
                    if (key.length == 2 && !key[0].equals("default") && !key[0].equals(spellKey.getBaseKey()) && !key[0].equals(spellKey.getKey())) {
                        continue;
                    }

                    fullKey = key.length == 2 ? key[1] : key[0];
                    ConfigurationUtils.set(extraParameters, fullKey, pieces[1]);
                }
            }
        }

        if (this.currentCast == null)
        {
            this.currentCast = new CastContext();
            this.currentCast.setSpell(this);
        }

        this.location = defaultLocation;

        workingParameters = new ParameterizedConfiguration(mage);
        ConfigurationUtils.addConfigurations(workingParameters, this.parameters);
        ConfigurationUtils.addConfigurations(workingParameters, extraParameters);
        processParameters(workingParameters);

        // Check to see if this is allowed to be cast by a command block
        if (!commandBlockAllowed) {
            CommandSender sender = mage.getCommandSender();
            if (sender != null && sender instanceof BlockCommandSender) {
                Block block = mage.getLocation().getBlock();
                if (block.getType() == Material.COMMAND) {
                    block.setType(Material.AIR);
                }
                return false;
            }
        }

        // Allow other plugins to cancel this cast
        PreCastEvent preCast = new PreCastEvent(mage, this);
        Bukkit.getPluginManager().callEvent(preCast);

        if (preCast.isCancelled()) {
            processResult(SpellResult.CANCELLED, workingParameters);
            sendCastMessage(SpellResult.CANCELLED, " (no cast)");
            return false;
        }

        // Don't allow casting if the player is confused or weakened
        bypassConfusion = workingParameters.getBoolean("bypass_confusion", bypassConfusion);
        bypassWeakness = workingParameters.getBoolean("bypass_weakness", bypassWeakness);
        LivingEntity livingEntity = mage.getLivingEntity();
        if (livingEntity != null && !mage.isSuperPowered()) {
            if (!bypassConfusion && livingEntity.hasPotionEffect(PotionEffectType.CONFUSION)) {
                processResult(SpellResult.CURSED, workingParameters);
                sendCastMessage(SpellResult.CURSED, " (no cast)");
                return false;
            }

            // Don't allow casting if the player is weakened
            if (!bypassWeakness && livingEntity.hasPotionEffect(PotionEffectType.WEAKNESS)) {
                processResult(SpellResult.CURSED, workingParameters);
                sendCastMessage(SpellResult.CURSED, " (no cast)");
                return false;
            }
        }

        // Don't perform permission check until after processing parameters, in case of overrides
        if (!canCast(getLocation())) {
            processResult(SpellResult.INSUFFICIENT_PERMISSION, workingParameters);
            sendCastMessage(SpellResult.INSUFFICIENT_PERMISSION, " (no cast)");
            if (mage.getDebugLevel() > 1) {
                CommandSender messageTarget = mage.getDebugger();
                if (messageTarget == null) {
                    messageTarget = mage.getCommandSender();
                }
                if (messageTarget != null) {
                    mage.debugPermissions(messageTarget, this);
                }
            }
            return false;
        }

        this.preCast();

        // PVP override settings
        bypassPvpRestriction = workingParameters.getBoolean("bypass_pvp", false);
        bypassPvpRestriction = workingParameters.getBoolean("bp", bypassPvpRestriction);
        bypassPermissions = workingParameters.getBoolean("bypass_permissions", bypassPermissions);
        bypassFriendlyFire = workingParameters.getBoolean("bypass_friendly_fire", false);
        onlyFriendlyFire = workingParameters.getBoolean("only_friendly", false);

        // Check cooldowns
        cooldown = workingParameters.getInt("cooldown", cooldown);
        cooldown = workingParameters.getInt("cool", cooldown);
        mageCooldown = workingParameters.getInt("cooldown_mage", mageCooldown);

        // Color override
        color = ConfigurationUtils.getColor(workingParameters, "color", color);
        particle = workingParameters.getString("particle", null);

        double cooldownRemaining = getRemainingCooldown() / 1000.0;
        String timeDescription = "";
        if (cooldownRemaining > 0) {
            if (cooldownRemaining > 60 * 60 ) {
                long hours = (long)Math.ceil(cooldownRemaining / (60 * 60));
                if (hours == 1) {
                    timeDescription = controller.getMessages().get("cooldown.wait_hour");
                } else {
                    timeDescription = controller.getMessages().get("cooldown.wait_hours").replace("$hours", Long.toString(hours));
                }
            } else if (cooldownRemaining > 60) {
                long minutes = (long)Math.ceil(cooldownRemaining / 60);
                if (minutes == 1) {
                    timeDescription = controller.getMessages().get("cooldown.wait_minute");
                } else {
                    timeDescription = controller.getMessages().get("cooldown.wait_minutes").replace("$minutes", Long.toString(minutes));
                }
            } else if (cooldownRemaining >= 1) {
                long seconds = (long)Math.ceil(cooldownRemaining);
                if (seconds == 1) {
                    timeDescription = controller.getMessages().get("cooldown.wait_second");
                } else {
                    timeDescription = controller.getMessages().get("cooldown.wait_seconds").replace("$seconds", Long.toString(seconds));
                }
            } else {
                timeDescription = controller.getMessages().get("cooldown.wait_moment");
                if (timeDescription.contains("$seconds")) {
                    timeDescription = timeDescription.replace("$seconds", SECONDS_FORMATTER.format(cooldownRemaining));
                }
            }
            castMessage(getMessage("cooldown").replace("$time", timeDescription));
            processResult(SpellResult.COOLDOWN, workingParameters);
            sendCastMessage(SpellResult.COOLDOWN, " (no cast)");
            return false;
        }

        CastingCost required = getRequiredCost();
        if (required != null) {
            String baseMessage = getMessage("insufficient_resources");
            String costDescription = required.getDescription(controller.getMessages(), mage);
            // Send loud messages when items are required.
            if (required.isItem()) {
                sendMessage(baseMessage.replace("$cost", costDescription));
            } else {
                castMessage(baseMessage.replace("$cost", costDescription));
            }
            processResult(SpellResult.INSUFFICIENT_RESOURCES, workingParameters);
            sendCastMessage(SpellResult.INSUFFICIENT_RESOURCES, " (no cast)");
            return false;
        }

        if (controller.isSpellProgressionEnabled()) {
            long progressLevel = getProgressLevel();
            for (Entry<String, EquationTransform> entry : progressLevelEquations.entrySet()) {
                workingParameters.set(entry.getKey(), entry.getValue().get(progressLevel));
            }
        }

        // Check for cancel-on-cast-other spells, after we have determined that this spell can really be cast.
        if (!passive) {
            for (Iterator<Batch> iterator = mage.getPendingBatches().iterator(); iterator.hasNext();) {
                Batch batch = iterator.next();
                if (!(batch instanceof SpellBatch)) continue;
                SpellBatch spellBatch = (SpellBatch)batch;
                Spell spell = spellBatch.getSpell();
                if (spell.cancelOnCastOther()) {
                    spell.cancel();
                    batch.finish();
                    iterator.remove();
                }
            }
        }

        return finalizeCast(workingParameters);
    }

    @Override
    public long getProgressLevel() {
        if (requiredCastsPerLevel == 0) {
            return 1;
        }
        return Math.min(getCastCount() / requiredCastsPerLevel + 1, maxLevels);
    }

    @Override
    public long getMaxProgressLevel() {
        return maxLevels;
    }

    private long getPreviousCastProgressLevel() {
        return Math.min(Math.max((getCastCount() - 1), 0) / requiredCastsPerLevel + 1, maxLevels);
    }

    @Override
    public boolean canCast(Location location) {
        if (bypassAll) return true;
        if (!hasCastPermission(mage.getCommandSender())) return false;
        Entity entity = mage.getEntity();
        if (disguiseRestricted && entity != null && entity instanceof Player && controller.isDisguised(entity)) return false;
        if (glideRestricted && entity != null && entity instanceof LivingEntity && ((LivingEntity)entity).isGliding()) return false;
        if (glideExclusive && entity != null && entity instanceof LivingEntity && !((LivingEntity)entity).isGliding()) return false;

        if (location == null) return true;
        Boolean regionPermission = bypassRegionPermission ? null : controller.getRegionCastPermission(mage.getPlayer(), this, location);
        if (regionPermission != null && regionPermission == true) return true;
        Boolean personalPermission = bypassRegionPermission ? null : controller.getPersonalCastPermission(mage.getPlayer(), this, location);
        if (personalPermission != null && personalPermission == true) return true;
        if (regionPermission != null && regionPermission == false) return false;
        if (requiresBuildPermission() && !hasBuildPermission(location.getBlock())) return false;
        if (requiresBreakPermission() && !hasBreakPermission(location.getBlock())) return false;
        if (worldBorderRestricted)
        {
            WorldBorder border = location.getWorld().getWorldBorder();
            double borderSize = border.getSize() / 2 - border.getWarningDistance();
            Location offset = location.clone().subtract(border.getCenter());
            if (offset.getX() < -borderSize || offset.getX() > borderSize || offset.getZ() < -borderSize || offset.getZ() > borderSize) return false;
        }
        return !pvpRestricted || bypassPvpRestriction || mage.isPVPAllowed(location);
    }

    @Override
    public boolean isPvpRestricted() {
        return pvpRestricted && !bypassPvpRestriction;
    }

    @Override
    public boolean isDisguiseRestricted() {
        return disguiseRestricted;
    }

    @Override
    public boolean requiresBuildPermission() {
        return false;
    }

    @Override
    public boolean requiresBreakPermission() {
        return false;
    }

    public boolean hasBreakPermission(Location location) {
        if (location == null) return true;
        return hasBreakPermission(location.getBlock());
    }

    public boolean hasBreakPermission(Block block) {
        // Cast permissions bypass
        if (bypassBreakRestriction || bypassAll) return true;
        Boolean castPermission = controller.getRegionCastPermission(mage.getPlayer(), this, block.getLocation());
        if (castPermission != null && castPermission == true) return true;
        if (castPermission != null && castPermission == false) return false;
        return mage.hasBreakPermission(block);
    }

    public boolean hasBuildPermission(Location location) {
        if (location == null) return true;
        return hasBuildPermission(location.getBlock());
    }

    public boolean hasBuildPermission(Block block) {
        // Cast permissions bypass
        if (bypassBuildRestriction || bypassRegionPermission || bypassAll) return true;
        Boolean castPermission = controller.getRegionCastPermission(mage.getPlayer(), this, block.getLocation());
        if (castPermission != null && castPermission == true) return true;
        if (castPermission != null && castPermission == false) return false;
        return mage.hasBuildPermission(block);
    }

    protected void onBackfire() {

    }

    protected void backfire() {
        if (!backfired) {
            onBackfire();
        }
        backfired = true;
    }

    protected boolean finalizeCast(ConfigurationSection parameters) {
        SpellResult result = null;

        // Global parameters
        controller.disablePhysics(parameters.getInt("disable_physics", 0));

        if (!mage.isSuperPowered()) {
            if (backfireChance > 0 && random.nextDouble() < backfireChance) {
                backfire();
            } else if (fizzleChance > 0 && random.nextDouble() < fizzleChance) {
                result = SpellResult.FIZZLE;
            }
        }

        if (result == null) {
            result = onCast(parameters);
        }
        if (backfired) {
            result = SpellResult.BACKFIRE;
        }
        if (result == SpellResult.CAST) {
            LivingEntity sourceEntity = mage.getLivingEntity();
            Entity targetEntity = getTargetEntity();
            if (sourceEntity == targetEntity) {
                result = SpellResult.CAST_SELF;
            }
        }
        processResult(result, parameters);

        boolean success = result.isSuccess();
        boolean free = result.isFree(castOnNoTarget);
        if (!free) {
            if (costs != null && !mage.isCostFree()) {
                for (CastingCost cost : costs)
                {
                    if (cost.isItem() && currentCast != null) {
                        currentCast.getUndoList().setConsumed(true);
                    }
                    cost.use(this);
                }
            }
            updateCooldown();
        }

        sendCastMessage(result, " (" + success + ")");
        return success;
    }

    protected void updateCooldown() {
        Wand wand = currentCast != null ? currentCast.getWand() : null;
        boolean isCooldownFree = wand != null ? wand.isCooldownFree() : mage.isCooldownFree();
        double cooldownReduction = wand != null ? wand.getCooldownReduction() : mage.getCooldownReduction();
        cooldownReduction += this.cooldownReduction;
        spellData.setLastCast(System.currentTimeMillis());
        if (!isCooldownFree && cooldown > 0) {
            if (cooldownReduction < 1) {
                int reducedCooldown = (int)Math.ceil((1.0f - cooldownReduction) * cooldown);
                spellData.setCooldownExpiration(Math.max(spellData.getCooldownExpiration(), System.currentTimeMillis() + reducedCooldown));
            }
        }
        if (!isCooldownFree && mageCooldown > 0) {
            if (cooldownReduction < 1) {
                int reducedCooldown = (int)Math.ceil((1.0f - cooldownReduction) * mageCooldown);
                mage.setRemainingCooldown(reducedCooldown);
            }
        }
    }

    protected void sendCastMessage(SpellResult result, String message)
    {
        Location source = getEyeLocation();
        if (mage == null || source == null) return;

        mage.sendDebugMessage(ChatColor.WHITE + "Cast " + ChatColor.GOLD + getName() + ChatColor.WHITE + " from " +
                ChatColor.GRAY + source.getBlockX() +
                ChatColor.DARK_GRAY + ","  + ChatColor.GRAY + source.getBlockY() +
                ChatColor.DARK_GRAY + "," + ChatColor.GRAY + source.getBlockZ() +
                ChatColor.WHITE  + ": " + ChatColor.AQUA + result.name().toLowerCase() +
                ChatColor.DARK_AQUA + message);
    }

    @Override
    public String getMessage(String messageKey) {
        return getMessage(messageKey, "");
    }

    public String getMessage(String messageKey, String def) {
        String message = controller.getMessages().get("spells.default." + messageKey, def);
        if (inheritKey != null && !inheritKey.isEmpty()) {
            message = controller.getMessages().get("spells." + inheritKey + "." + messageKey, message);
        }
        message = controller.getMessages().get("spells." + spellKey.getBaseKey() + "." + messageKey, message);
        if (spellKey.isVariant()) {
            message = controller.getMessages().get("spells." + spellKey.getKey() + "." + messageKey, message);
        }
        if (message == null) message = "";
        else if (!message.isEmpty()) {
            // Escape some common parameters
            String playerName = mage.getName();
            message = message.replace("$player", playerName);

            if (message.contains("$material"))
            {
                String materialName = getDisplayMaterialName();

                // TODO: Localize "None", provide static getter
                materialName = materialName == null ? "None" : materialName;
                message = message.replace("$material", materialName);
            }
        }
        return message;
    }

    protected String getDisplayMaterialName()
    {
        return "None";
    }

    protected void processResult(SpellResult result, ConfigurationSection parameters) {
        mage.onCast(this, result);

        // Show messaging
        String resultName = result.name().toLowerCase();
        if (!mage.isQuiet())
        {
            if (result.isSuccess()) {
                String message = null;
                if (result != SpellResult.CAST) {
                    message = getMessage("cast");
                }
                if (result.isAlternate() && result != SpellResult.ALTERNATE) {
                    message = getMessage("alternate", message);
                }
                message = getMessage(resultName, message);
                LivingEntity sourceEntity = mage.getLivingEntity();
                Entity targetEntity = getTargetEntity();
                if (targetEntity == sourceEntity) {
                    message = getMessage("cast_self", message);
                } else if (targetEntity instanceof Player) {
                    message = getMessage("cast_player", message);
                } else if (targetEntity instanceof LivingEntity) {
                    message = getMessage("cast_livingentity", message);
                } else {
                    message = getMessage("cast_entity", message);
                }
                if (loud) {
                    sendMessage(message);
                } else {
                    castMessage(message);
                }
            } else
            // Special cases where messaging is handled elsewhere
            if (result != SpellResult.INSUFFICIENT_RESOURCES && result != SpellResult.COOLDOWN)
            {
                String message = null;
                if (result.isFailure() && result != SpellResult.FAIL) {
                    message = getMessage("fail");
                }

                if (result.isFailure()) {
                    sendMessage(getMessage(resultName, message));
                } else {
                    castMessage(getMessage(resultName, message));
                }
            }
        }

        // Play effects
        playEffects(resultName);

        // Check for finalization
        if (currentCast != null) {
            // Legacy spells never update the final context result.
            if (isLegacy()) currentCast.addResult(result);
            // Batched spells will call finish() on completion
            if (!isBatched()) currentCast.finish();
        }
    }

    protected boolean isBatched() {
        return false;
    }

    protected boolean isLegacy() {
        return true;
    }

    @Override
    public void messageTargets(String messageKey)
    {
        if (messageTargets && currentCast != null)
        {
            currentCast.messageTargets(messageKey);
        }
    }

    public void playEffects(String effectName, float scale, Block sourceBlock) {
        playEffects(effectName, getCurrentCast(), scale, sourceBlock);
    }

    public void playEffects(String effectName, float scale) {
        playEffects(effectName, getCurrentCast(), scale, null);
    }

    @Override
    public void playEffects(String effectName)
    {
        playEffects(effectName, 1);
    }

    @Override
    public void playEffects(String effectName, com.elmakers.mine.bukkit.api.action.CastContext context)
    {
        playEffects(effectName, context, 1);
    }

    public void playEffects(String effectName, com.elmakers.mine.bukkit.api.action.CastContext context, float scale, Block block) {
        context.playEffects(effectName, scale, block);
    }

    @Override
    public void playEffects(String effectName, com.elmakers.mine.bukkit.api.action.CastContext context, float scale) {
        playEffects(effectName, context, scale, null);
    }

    @Override
    public void target() {

    }

    @Override
    public Location getTargetLocation() {
        return null;
    }

    @Override
    public boolean canTarget(Entity entity) {
        if (bypassAll) return true;
        if (!bypassPvpRestriction && entity instanceof Player)
        {
            Player magePlayer = mage.getPlayer();
            if (magePlayer != null && !magePlayer.hasPermission("Magic.bypass_pvp"))
            {
                // Check that the other player does not have PVP disabled for fairness
                if (!controller.isPVPAllowed((Player)entity, entity.getLocation())) return false;
                if (!controller.isPVPAllowed(magePlayer, entity.getLocation())) return false;
                if (!controller.isPVPAllowed(magePlayer, mage.getLocation())) return false;
            }
        }
        if (onlyFriendlyFire)
        {
            return controller.isFriendly(mage.getEntity(), entity);
        }
        if (!bypassProtection && !bypassFriendlyFire)
        {
            return controller.canTarget(mage.getEntity(), entity);
        }
        return true;
    }

    @Override
    public Entity getTargetEntity() {
        return null;
    }

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getEffectMaterial()
    {
        return new MaterialAndData(DEFAULT_EFFECT_MATERIAL);
    }

    public void processParameters(ConfigurationSection parameters) {
        fizzleChance = (float)parameters.getDouble("fizzle_chance", 0);
        backfireChance = (float)parameters.getDouble("backfire_chance", 0);

        Location defaultLocation = location == null ? mage.getLocation() : location;
        Location locationOverride = ConfigurationUtils.overrideLocation(parameters, "p", defaultLocation, controller.canCreateWorlds());
        if (locationOverride != null) {
            location = locationOverride;
        }
        costReduction = (float)parameters.getDouble("cost_reduction", 0);
        consumeReduction = (float)parameters.getDouble("consume_reduction", 0);
        cooldownReduction = (float)parameters.getDouble("cooldown_reduction", 0);
        if (parameters.getBoolean("free", false)) {
            costReduction = 2;
            consumeReduction = 2;
        }
        bypassMageCooldown = parameters.getBoolean("bypass_mage_cooldown", false);
        cancelOnDamage = parameters.getDouble("cancel_on_damage", 0);
        cancelOnCastOther = parameters.getBoolean("cancel_on_cast_other", false);
        cancelOnNoPermission = parameters.getBoolean("cancel_on_no_permission", false);
        commandBlockAllowed = parameters.getBoolean("command_block_allowed", true);

        if (parameters.contains("prevent_passthrough")) {
            preventPassThroughMaterials = controller.getMaterialSet(parameters.getString("prevent_passthrough"));
        } else {
            preventPassThroughMaterials = controller.getMaterialSet("indestructible");
        }

        if (parameters.contains("passthrough")) {
            passthroughMaterials = controller.getMaterialSet(parameters.getString("passthrough"));
        } else {
            passthroughMaterials = controller.getMaterialSet("passthrough");
        }

        if (parameters.contains("unsafe")) {
            unsafeMaterials = controller.getMaterialSet(parameters.getString("unsafe"));
        } else {
            unsafeMaterials = controller.getMaterialSet("unsafe");
        }

        bypassDeactivate = parameters.getBoolean("bypass_deactivate", false);
        quiet = parameters.getBoolean("quiet", false);
        loud = parameters.getBoolean("loud", false);
        targetSelf = parameters.getBoolean("target_self", false);
        messageTargets = parameters.getBoolean("message_targets", true);
        verticalSearchDistance = parameters.getInt("vertical_range", 8);
        passive = parameters.getBoolean("passive", false);

        cooldown = parameters.getInt("cooldown", 0);
        cooldown = parameters.getInt("cool", cooldown);
        displayCooldown = parameters.getInt("display_cooldown", -1);
        warmup = parameters.getInt("warmup", 0);
        bypassPvpRestriction = parameters.getBoolean("bypass_pvp", false);
        bypassPvpRestriction = parameters.getBoolean("bp", bypassPvpRestriction);
        bypassPermissions = parameters.getBoolean("bypass_permissions", false);
        bypassBuildRestriction = parameters.getBoolean("bypass_build", false);
        bypassBuildRestriction = parameters.getBoolean("bb", bypassBuildRestriction);
        bypassBreakRestriction = parameters.getBoolean("bypass_break", false);
        bypassProtection = parameters.getBoolean("bypass_protection", false);
        bypassProtection = parameters.getBoolean("bp", bypassProtection);
        bypassAll = parameters.getBoolean("bypass", false);
        duration = parameters.getInt("duration", 0);
        totalDuration = parameters.getInt("total_duration", 0);
    }

    @Override
    public boolean brushIsErase() {
        return false;
    }

    @Override
    public String getPermissionNode()
    {
        return "Magic.cast." + spellKey.getBaseKey();
    }

    /**
     * Called when a material selection spell is cancelled mid-selection.
     */
    public boolean onCancelSelection()
    {
        return false;
    }

    /**
     * Listener method, called on player quit for registered spells.
     *
     * @param event The player who just quit
     */
    public void onPlayerQuit(PlayerQuitEvent event)
    {

    }

    /**
     * Listener method, called on player move for registered spells.
     *
     * @param event The original entity death event
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
    @Override
    public void initialize(MageController instance)
    {
        this.controller = instance;
    }

    @Override
    public long getCastCount()
    {
        return spellData.getCastCount();
    }

    @Override
    public void setCastCount(long count) {
        spellData.setCastCount(count);
    }

    public void onActivate() {

    }

    public void onDeactivate() {

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

    @Override
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

    @Override
    public float getConsumeReduction()
    {
        CostReducer reducer = currentCast != null ? currentCast.getWand() : mage;
        if (reducer == null) {
            reducer = mage;
        }
        if (reducer == null) {
            return consumeReduction;
        }
        return consumeReduction + reducer.getConsumeReduction();
    }

    @Override
    public float getCostReduction()
    {
        CostReducer reducer = currentCast != null ? currentCast.getWand() : mage;
        if (reducer == null) {
            reducer = mage;
        }
        if (reducer == null) {
            return costReduction;
        }
        return costReduction + reducer.getCostReduction();
    }

    @Override
    public float getCostScale()
    {
        return activeCostScale;
    }

    //
    // Public API Implementation
    //

    @Override
    public com.elmakers.mine.bukkit.api.spell.Spell createSpell()
    {
        BaseSpell spell = null;
        try {
            spell = this.getClass().newInstance();
            spell.initialize(controller);
            spell.loadTemplate(spellKey.getKey(), configuration);
            spell.loadPrerequisites(configuration);
            spell.template = this;
        } catch (Throwable ex) {
            controller.getLogger().log(Level.WARNING, "Error creating spell " + spellKey.getKey(), ex);
        }
        return spell;
    }

    @Override
    public boolean cast()
    {
        return cast((ConfigurationSection)null, null);
    }

    @Override
    public boolean cast(String[] extraParameters)
    {
        return cast(extraParameters, null);
    }

    @Override
    public final String getKey()
    {
        return spellKey.getKey();
    }

    @Override
    public final String getName()
    {
        return name;
    }

    @Override
    public final String getAlias()
    {
        return alias;
    }

    @Override
    public boolean isQuickCast() {
        return quickCast;
    }

    @Override
    public final com.elmakers.mine.bukkit.api.block.MaterialAndData getIcon()
    {
        return icon;
    }

    @Override
    public final com.elmakers.mine.bukkit.api.block.MaterialAndData getDisabledIcon()
    {
        return disabledIcon;
    }

    @Override
    public boolean hasIcon() {
        return icon != null && icon.getMaterial() != Material.AIR;
    }

    @Override
    public final String getDescription()
    {
        return description;
    }

    @Override
    public final String getExtendedDescription()
    {
        return extendedDescription;
    }

    @Override
    public final String getLevelDescription()
    {
        return levelDescription;
    }

    public final String getProgressDescription()
    {
        return progressDescription;
    }

    @Override
    public final SpellKey getSpellKey()
    {
        return spellKey;
    }

    @Override
    public final String getUsage()
    {
        return usage;
    }

    @Override
    public final double getWorth()
    {
        return worth;
    }

    @Override
    public final double getEarns()
    {
        return earns;
    }

    @Override
    public final SpellCategory getCategory()
    {
        return category;
    }

    @Override
    public boolean hasTag(String tag) {
        if (category != null && category.getKey().equals(tag)) return true;
        return tags != null && tags.contains(tag);
    }

    @Override
    public boolean hasAnyTag(Collection<String> tagSet) {
        if (category != null && tagSet.contains(category.getKey())) return true;
        return tags != null && !Collections.disjoint(tagSet, tags);
    }

    @Override
    public Collection<com.elmakers.mine.bukkit.api.effect.EffectPlayer> getEffects(SpellResult result) {
        return getEffects(result.name().toLowerCase());
    }

    public boolean hasEffects(String key) {
        Collection<EffectPlayer> effectList = effects.get(key);
        return effectList != null && effectList.size() > 0;
    }

    @Override
    public Collection<com.elmakers.mine.bukkit.api.effect.EffectPlayer> getEffects(String key) {
        Collection<EffectPlayer> effectList = effects.get(key);
        if (effectList == null) {
            return new ArrayList<>();
        }
        return new ArrayList<com.elmakers.mine.bukkit.api.effect.EffectPlayer>(effectList);
    }

    @Override
    public Collection<CastingCost> getCosts() {
        if (costs == null) return null;
        List<CastingCost> copy = new ArrayList<>();
        copy.addAll(costs);
        return copy;
    }

    @Override
    public Collection<CastingCost> getActiveCosts() {
        if (activeCosts == null) return null;
        List<CastingCost> copy = new ArrayList<>();
        copy.addAll(activeCosts);
        return copy;
    }

    @Override
    public ConfigurationSection getWorkingParameters() {
        return workingParameters;
    }

    @Override
    public void getParameters(Collection<String> parameters)
    {
        parameters.addAll(Arrays.asList(COMMON_PARAMETERS));
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        if (parameterKey.equals("duration")) {
            examples.addAll(Arrays.asList(EXAMPLE_DURATIONS));
        } else if (parameterKey.equals("range")) {
            examples.addAll(Arrays.asList(EXAMPLE_SIZES));
        } else if (parameterKey.equals("transparent")) {
            examples.addAll(controller.getMaterialSets());
        } else if (parameterKey.equals("player")) {
            examples.addAll(controller.getPlayerNames());
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

    @Override
    public String getMageCooldownDescription() {
        return getCooldownDescription(controller.getMessages(), mageCooldown, null);
    }

    public String getMageCooldownDescription(com.elmakers.mine.bukkit.api.wand.Wand wand) {
        return getCooldownDescription(controller.getMessages(), mageCooldown, wand);
    }

    @Override
    public String getCooldownDescription() {
        return getCooldownDescription(
                controller.getMessages(), getDisplayCooldown(),  null);
    }

    public String getWarmupDescription() {
        return getTimeDescription(controller.getMessages(), warmup);
    }

    public String getCooldownDescription(com.elmakers.mine.bukkit.api.wand.Wand wand) {
        return getCooldownDescription(
                controller.getMessages(), getDisplayCooldown(), wand);
    }

    /**
     * @return The cooldown to show in UI. Spells can manually set their
     *         "display_cooldown" if they apply cooldown via an action.
     */
    private int getDisplayCooldown() {
        return displayCooldown != -1 ? displayCooldown : cooldown;
    }

    private String getTimeDescription(Messages messages, int time) {
        if (time > 0) {
            int timeInSeconds = time / 1000;
            if (timeInSeconds > 60 * 60 ) {
                int hours = timeInSeconds / (60 * 60);
                if (hours == 1) {
                    return messages.get("cooldown.description_hour");
                }
                return messages.get("cooldown.description_hours").replace("$hours", Integer.toString(hours));
            } else if (timeInSeconds > 60) {
                int minutes = timeInSeconds / 60;
                if (minutes == 1) {
                    return messages.get("cooldown.description_minute");
                }
                return messages.get("cooldown.description_minutes").replace("$minutes", Integer.toString(minutes));
            } else if (timeInSeconds > 1) {
                return messages.get("cooldown.description_seconds").replace("$seconds", Integer.toString(timeInSeconds));
            } else if (timeInSeconds == 1) {
                return messages.get("cooldown.description_second");
            } else {
                String timeDescription = controller.getMessages().get("cooldown.description_moment");
                if (timeDescription.contains("$seconds")) {
                    timeDescription = timeDescription.replace("$seconds", SECONDS_FORMATTER.format(time / 1000.0D));
                }
                return timeDescription;
            }
        }
        return null;
    }

    protected String getCooldownDescription(Messages messages, int cooldown, com.elmakers.mine.bukkit.api.wand.Wand wand) {
        if (wand != null) {
            if (wand.isCooldownFree()) {
                cooldown = 0;
            }
            double cooldownReduction = wand.getCooldownReduction();
            cooldownReduction += this.cooldownReduction;
            if (cooldown > 0 && cooldownReduction < 1) {
                cooldown = (int)Math.ceil((1.0f - cooldownReduction) * cooldown);
            }
        }

        return getTimeDescription(messages, cooldown);
    }

    @Override
    public long getCooldown()
    {
        return cooldown;
    }

    @Override
    public CastingCost getRequiredCost() {
        if (!mage.isCostFree())
        {
            if (costs != null && !spellData.isActive())
            {
                for (CastingCost cost : costs)
                {
                    if (!cost.has(this))
                    {
                        return cost;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void clearCooldown() {
        spellData.setCooldownExpiration(0);
    }

    @Override
    public void setRemainingCooldown(long ms) {
        spellData.setCooldownExpiration(Math.max(ms + System.currentTimeMillis(), spellData.getCooldownExpiration()));
    }

    @Override
    public long getRemainingCooldown() {
        long remaining = 0;
        if (mage.isCooldownFree()) return 0;
        if (spellData.getCooldownExpiration() > 0)
        {
            long now = System.currentTimeMillis();
            if (spellData.getCooldownExpiration() > now) {
                remaining = spellData.getCooldownExpiration() - now;
            } else {
                spellData.setCooldownExpiration(0);
            }
        }

        return bypassMageCooldown ? remaining : Math.max(mage.getRemainingCooldown(), remaining);
    }

    @Override
    public long getDuration()
    {
        if (totalDuration >= 0) {
            return totalDuration;
        }
        return duration;
    }

    @Override
    public double getRange()
    {
        return 0;
    }

    @Override
    public void setMage(Mage mage)
    {
        this.mage = mage;
    }

    @Override
    public boolean cancelSelection()
    {
        boolean cancelled = onCancelSelection();
        if (cancelled) {
            cancel();
        }
        return cancelled;
    }

    @Override
    public boolean cancel()
    {
        if (isActive()) {
            sendMessage(getMessage("cancel"));
        }
        if (currentCast != null) {
            currentCast.cancelEffects();
        }
        return true;
    }

    @Override
    public void setActive(boolean active) {
        if (active && !spellData.isActive()) {
            onActivate();
        } else if (!active && spellData.isActive()) {
            onDeactivate();
        }
        spellData.setIsActive(active);
        lastActiveCost = System.currentTimeMillis();
    }

    @Override
    public void activate() {
        if (!spellData.isActive()) {
            mage.activateSpell(this);
        }
    }

    @Override
    public boolean deactivate() {
        updateCooldown();
        return deactivate(false, false);
    }

    @Override
    public boolean deactivate(boolean force, boolean quiet) {
        if (!force && bypassDeactivate) {
            return false;
        }
        if (spellData.isActive()) {
            spellData.setIsActive(false);
            onDeactivate();

            mage.deactivateSpell(this);
            if (!quiet) {
                sendMessage(getMessage("deactivate"));
            }
            if (currentCast != null) {
                currentCast.cancelEffects();
            }
        }

        return true;
    }

    @Override
    public Mage getMage() {
        return mage;
    }

    @Override
    public void load(SpellData spellData) {
        if (spellData == null) {
            throw new IllegalArgumentException("SpellData may not be null");
        }
        try {
            this.spellData = spellData;
            onLoad(spellData.getExtraData());
        } catch (Exception ex) {
            controller.getPlugin().getLogger().warning("Failed to load data for spell " + name + ": " + ex.getMessage());
        }
    }

    @Override
    public void save(SpellData spellData) {
        try {
            onSave(this.spellData.getExtraData());
        } catch (Exception ex) {
            controller.getPlugin().getLogger().warning("Failed to save data for spell " + name);
            ex.printStackTrace();
        }
    }

    @Override
    public SpellData getSpellData() {
        return spellData;
    }

    @Override
    public void setSpellData(SpellData data) {
        this.spellData = data;
    }

    @Override
    public void loadTemplate(String key, ConfigurationSection node)
    {
        spellKey = new SpellKey(key);
        this.configuration = node;
        this.loadTemplate(node);
    }

    @Override
    public void tick()
    {
        checkActiveDuration();
        checkActiveCosts();
    }

    @Override
    public boolean isActive()
    {
         return spellData.isActive();
    }

    @Override
    public int compareTo(com.elmakers.mine.bukkit.api.spell.SpellTemplate other)
    {
        return name.compareTo(other.getName());
    }

    @Override
    public boolean hasCastPermission(CommandSender sender)
    {
        if (sender == null || bypassPermissions) return true;

        return controller.hasCastPermission(sender, this);
    }

    @Override
    public Color getColor()
    {
        if (color != null) return color;
        if (category != null) return category.getColor();
        return null;
    }

    @Override
    public boolean isHidden()
    {
        return hidden;
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

    @Override
    public MageController getController() {
        return controller;
    }

    @Override
    public String getIconURL() {
        return iconURL;
    }

    @Override
    public String getDisabledIconURL() {
        return iconDisabledURL == null ? DEFAULT_DISABLED_ICON_URL : iconDisabledURL;
    }

    @Override
    public String getRequiredUpgradePath() {
        return requiredUpgradePath;
    }

    @Override
    public Set<String> getRequiredUpgradeTags() {
        return requiredUpgradeTags;
    }

    @Override
    public long getRequiredUpgradeCasts() {
        return requiredUpgradeCasts;
    }

    @Override
    public Collection<PrerequisiteSpell> getPrerequisiteSpells() {
        return requiredSpells;
    }

    @Override
    public Collection<SpellKey> getSpellsToRemove() {
        return removesSpells;
    }

    @Override
    public String getUpgradeDescription() {
        return upgradeDescription == null ? "" : upgradeDescription;
    }

    @Override
    public SpellTemplate getUpgrade() {
        if (requiredUpgradeCasts <= 0
                && ((requiredUpgradePath == null || requiredUpgradePath.isEmpty())
                && (requiredUpgradeTags == null || requiredUpgradeTags.isEmpty()))) {
            return null;
        }
        SpellKey upgradeKey = new SpellKey(spellKey.getBaseKey(), spellKey.getLevel() + 1);
        return controller.getSpellTemplate(upgradeKey.getKey());
    }

    @Override
    public ConfigurationSection getConfiguration() {
        return this.configuration;
    }

    @Override
    public com.elmakers.mine.bukkit.api.action.CastContext getCurrentCast() {
        if (currentCast == null) {
            currentCast = new CastContext();
            this.currentCast.setSpell(this);
        }
        return currentCast;
    }

    @Override
    public Entity getEntity() {
        return mage.getEntity();
    }

    @Override
    public String getEffectParticle() {
        if (particle == null) {
            return mage.getEffectParticleName();
        }
        return particle;
    }

    @Override
    public Color getEffectColor() {
        if (color == null) {
            return mage.getEffectColor();
        }

        return color;
    }

    @Override
    public boolean showUndoable() {
        return showUndoable;
    }

    public int getVerticalSearchDistance() {
        return verticalSearchDistance;
    }

    @Override
    public void addLore(Messages messages, Mage mage, Wand wand, List<String> lore) {
        CostReducer reducer = wand == null ? mage : wand;
        if (levelDescription != null && levelDescription.length() > 0) {
            String descriptionTemplate = messages.get("spell.level_lore", "");
            if (!descriptionTemplate.isEmpty()) {
                InventoryUtils.wrapText(descriptionTemplate.replace("$level", levelDescription), lore);
            }
        }
        if (description != null && description.length() > 0) {
            String descriptionTemplate = messages.get("spell.description_lore", "");
            if (!descriptionTemplate.isEmpty()) {
                InventoryUtils.wrapText(descriptionTemplate.replace("$description", description), lore);
            }
        }
        if (usage != null && usage.length() > 0) {
            InventoryUtils.wrapText(usage, lore);
        }
        if (category != null) {
            String categoryLore = messages.get("spell.category", "");
            String categoryName = category.getName();
            if (!categoryLore.isEmpty() && !categoryName.isEmpty()) {
                lore.add(categoryLore.replace("$category", categoryName));
            }
        }
        if (quickCast && wand != null && !wand.isQuickCastDisabled() && wand.hasInventory()) {
            String quickCastText = messages.get("spell.quick_cast", "");
            if (!quickCastText.isEmpty()) {
                lore.add(quickCastText);
            }
        }
        String warmupDescription = getWarmupDescription();
        if (warmupDescription != null && !warmupDescription.isEmpty()) {
            lore.add(messages.get("warmup.description").replace("$time", warmupDescription));
        }
        String cooldownDescription = getCooldownDescription(wand);
        if (cooldownDescription != null && !cooldownDescription.isEmpty()) {
            lore.add(messages.get("cooldown.description").replace("$time", cooldownDescription));
        }
        String mageCooldownDescription = getMageCooldownDescription(wand);
        if (mageCooldownDescription != null && !mageCooldownDescription.isEmpty()) {
            lore.add(messages.get("cooldown.mage_description").replace("$time", mageCooldownDescription));
        }
        if (costs != null) {
            for (CastingCost cost : costs) {
                if (!cost.isEmpty(reducer)) {
                    lore.add(ChatColor.YELLOW + messages.get("wand.costs_description").replace("$description", cost.getFullDescription(messages, reducer)));
                }
            }
        }
        if (activeCosts != null) {
            for (CastingCost cost : activeCosts) {
                if (!cost.isEmpty(reducer)) {
                    lore.add(ChatColor.YELLOW + messages.get("wand.active_costs_description").replace("$description", cost.getFullDescription(messages, reducer)));
                }
            }
        }

        double range = getRange();
        if (range > 0) {
            lore.add(ChatColor.GRAY + messages.get("wand.range_description").replace("$range", RANGE_FORMATTER.format(range)));
        }

        long effectiveDuration = this.getDuration();
        if (effectiveDuration > 0) {
            long seconds = effectiveDuration / 1000;
            if (seconds > 60 * 60 ) {
                long hours = seconds / (60 * 60);
                lore.add(ChatColor.GRAY + messages.get("duration.lasts_hours").replace("$hours", ((Long)hours).toString()));
            } else if (seconds > 60) {
                long minutes = seconds / 60;
                lore.add(ChatColor.GRAY + messages.get("duration.lasts_minutes").replace("$minutes", ((Long)minutes).toString()));
            } else {
                lore.add(ChatColor.GRAY + messages.get("duration.lasts_seconds").replace("$seconds", ((Long)seconds).toString()));
            }
        }
        else if (showUndoable()) {
            if (isUndoable()) {
                String undoableText = messages.get("spell.undoable", "");
                if (!undoableText.isEmpty()) {
                    lore.add(undoableText);
                }
            } else {
                String undoableText = messages.get("spell.not_undoable", "");
                if (!undoableText.isEmpty()) {
                    lore.add(undoableText);
                }
            }
        }

        if (usesBrush()) {
            String brushText = messages.get("spell.brush");
            if (!brushText.isEmpty()) {
                lore.add(ChatColor.GOLD + brushText);
            }
        }

        if (earns > 0 && controller.isSPEnabled() && controller.isSPEarnEnabled()) {
            int scaledEarn = earns;
            if (mage != null) {
                scaledEarn = (int)Math.floor(mage.getSPMultiplier() * scaledEarn);
            }
            if (scaledEarn > 0) {
                String earnsText = messages.get("spell.earns").replace("$earns", Integer.toString(scaledEarn));
                if (!earnsText.isEmpty()) {
                    lore.add(earnsText);
                }
            }
        }
        if (controller.isSpellProgressionEnabled() && progressDescription != null
                && progressDescription.length() > 0 && maxLevels > 0 && template != null) {
            InventoryUtils.wrapText(progressDescription
                    .replace("$level", Long.toString(Math.max(0, getProgressLevel())))
                    .replace("$max_level", Long.toString(maxLevels)),
                    lore);
        }
    }

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialBrush getBrush()
    {
        if (mage == null) {
            return null;
        }
        return mage.getBrush();
    }

    @Override
    public boolean isCancellable() {
        return cancellable;
    }

    @Override
    public void finish(com.elmakers.mine.bukkit.api.action.CastContext context) {
        SpellResult result = context.getResult();

        // Notify other plugins of this spell cast
        CastEvent castEvent = new CastEvent(mage, this, result);
        Bukkit.getPluginManager().callEvent(castEvent);

        // Message targets
        if (result.isSuccess() && (loud || (!mage.isQuiet() && !quiet))) {
            messageTargets("cast_player_message");
        }
        
        // Clear cooldown on miss
        if (result.shouldRefundCooldown(castOnNoTarget)) {
            clearCooldown();
        }
        
        if (cancelEffects) {
            context.cancelEffects();
        }

        // Track cast counts
        if (result.isSuccess() && !passive) {
            spellData.addCast();
            if (template != null && template.spellData != null) {
                template.spellData.addCast();
                SpellCategory category = template.getCategory();
                if (category != null) {
                    category.addCast();
                }
            }

            // Reward SP
            Wand wand = context.getWand();
            Wand activeWand = mage.getActiveWand();
            if (activeWand != null && wand != null && activeWand.getItem() != null && wand.getItem() != null && wand.getItem() != activeWand.getItem() && activeWand.getItem().equals(wand.getItem())) {
                wand = activeWand;
            }
            Wand offhandWand = mage.getOffhandWand();
            if (offhandWand != null && wand != null && offhandWand.getItem() != null && wand.getItem() != null && wand.getItem() != offhandWand.getItem() && offhandWand.getItem().equals(wand.getItem())) {
                wand = offhandWand;
            }
            WandUpgradePath path = wand == null ? null : wand.getPath();
            if (earns > 0 && wand != null && path != null && path.earnsSP() && controller.isSPEnabled() && controller.isSPEarnEnabled() && !mage.isAtMaxSkillPoints()) {
                long now = System.currentTimeMillis();
                int scaledEarn = earns;
                if (spellData.getLastEarn() > 0 && earnCooldown > 0 && now < spellData.getLastEarn() + earnCooldown) {
                    scaledEarn = (int)Math.floor((double)earns * (now - spellData.getLastEarn()) / earnCooldown);
                    if (scaledEarn > 0) {
                        context.playEffects("earn_scaled_sp");
                    }
                } else {
                    context.playEffects("earn_sp");
                }
                if (scaledEarn > 0) {
                    mage.addSkillPoints((int)Math.floor(mage.getSPMultiplier() * scaledEarn));
                    spellData.setLastEarn(now);
                }
            }

            // Check for level up
            // This currently only works on wands.
            if (wand != null && wand.upgradesAllowed() && wand.getSpellLevel(spellKey.getBaseKey()) == spellKey.getLevel())
            {
                if (controller.isSpellUpgradingEnabled()) {
                    SpellTemplate upgrade = getUpgrade();
                    long requiredCasts = getRequiredUpgradeCasts();
                    String upgradePath = getRequiredUpgradePath();
                    WandUpgradePath currentPath = wand.getPath();
                    Set<String> upgradeTags = getRequiredUpgradeTags();
                    if ((upgrade != null && requiredCasts > 0 && getCastCount() >= requiredCasts)
                            && (upgradePath == null || upgradePath.isEmpty() || (currentPath != null && currentPath.hasPath(upgradePath)))
                            && (upgradeTags == null || upgradeTags.isEmpty() || (currentPath != null && currentPath.hasAllTags(upgradeTags))))
                    {
                        if (PrerequisiteSpell.hasPrerequisites(wand, upgrade)) {
                            Spell newSpell = mage.getSpell(upgrade.getKey());
                            if (isActive()) {
                                deactivate(true, true);
                                if (newSpell != null && newSpell instanceof MageSpell) {
                                    ((MageSpell)newSpell).activate();
                                }
                            }
                            wand.forceAddSpell(upgrade.getKey());
                            playEffects("upgrade");

                            if (controller.isPathUpgradingEnabled()) {
                                wand.checkAndUpgrade(true);
                            }
                            return; // return so progress upgrade doesn't also happen
                        }
                    }
                }
                if (maxLevels > 0 && controller.isSpellProgressionEnabled()) {
                    long previousLevel = getPreviousCastProgressLevel();
                    long currentLevel = getProgressLevel();

                    if (currentLevel != previousLevel) {
                        wand.addSpell(getKey());
                        if (currentLevel > previousLevel) {
                            Messages messages = controller.getMessages();
                            String progressDescription = getProgressDescription();
                            playEffects("progress");
                            if (progressDescription != null && !progressDescription.isEmpty()) {
                                mage.sendMessage(messages.get("wand.spell_progression")
                                        .replace("$name", getName())
                                        .replace("$wand", getName())
                                        .replace("$level", Long.toString(getProgressLevel()))
                                        .replace("$max_level", Long.toString(maxLevels)));
                            }
                        }
                        if (controller.isPathUpgradingEnabled()) {
                            wand.checkAndUpgrade(true);
                        }
                    }
                }
            }
        }
    }

    public boolean getTargetsCaster() {
        return targetSelf;
    }

    public void setTargetsCaster(boolean target) {
        targetSelf = target;
    }

    @Override
    public double cancelOnDamage() {
        return cancelOnDamage;
    }

    @Override
    public boolean cancelOnCastOther() {
        return cancelOnCastOther;
    }

    @Override
    public boolean cancelOnNoPermission() {
        return cancelOnNoPermission;
    }

    @Override
    public ConfigurationSection getHandlerParameters(String handlerKey)
    {
        return null;
    }

    @Override
    public boolean hasHandlerParameters(String handlerKey)
    {
        return false;
    }

    /**
     * @return Whether or not this spell can bypass region permissions such as custom world-guard flags.
     */
    public boolean isBypassRegionPermission() {
        return bypassRegionPermission;
    }

    public void setBypassRegionPermission(boolean bypass) {
        bypassRegionPermission = bypass;
    }

    @Override
    public ConfigurationSection getSpellParameters() {
        return parameters;
    }

    @Override
    public String getRequiredSkillapiClass() {
        return requiredSkillapiClass;
    }

    @Override
    public String getRequiredSkillapiSkill() {
        return requiredSkillapiSkill;
    }

}
