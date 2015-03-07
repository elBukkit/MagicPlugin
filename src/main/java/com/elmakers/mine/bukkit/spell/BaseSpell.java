package com.elmakers.mine.bukkit.spell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import com.elmakers.mine.bukkit.action.CastContext;
import com.elmakers.mine.bukkit.api.event.CastEvent;
import com.elmakers.mine.bukkit.api.event.PreCastEvent;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellCategory;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.effect.EffectPlayer;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public abstract class BaseSpell implements MageSpell, Cloneable {
    protected static final double VIEW_HEIGHT = 1.65;
    protected static final double LOOK_THRESHOLD_RADIANS = 0.8;

    // TODO: Config-drive
    protected static final int MIN_Y = 1;
    protected static final int MAX_Y = 255;
    protected static final int MAX_NETHER_Y = 120;

    // TODO: Configurable default? this does look cool, though.
    protected final static Material DEFAULT_EFFECT_MATERIAL = Material.STATIONARY_WATER;

    public final static String[] EXAMPLE_VECTOR_COMPONENTS = {"-1", "-0.5", "0", "0.5", "1", "~-1", "~-0.5", "~0", "~0.5", "*1", "*-1", "*-0.5", "*0.5", "*1"};
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
        "px", "py", "pz", "pdx", "pdy", "pdz", "tx", "ty", "tz", "otx", "oty", "otz", "t2x", "t2y", "t2z",
        "otdx", "otdy", "otdz"
    };

    protected final static Set<String> vectorParameterMap = new HashSet<String>(Arrays.asList(VECTOR_PARAMETERS));

    public final static String[] BOOLEAN_PARAMETERS = {
        "allow_max_range", "prevent_passthrough", "reverse_targeting", "passthrough", "bypass_build", "bypass_pvp", "target_npc", "ignore_blocks"
    };

    protected final static Set<String> booleanParameterMap = new HashSet<String>(Arrays.asList(BOOLEAN_PARAMETERS));

    public final static String[] PERCENTAGE_PARAMETERS = {
        "fizzle_chance", "backfire_chance", "cooldown_reduction"
    };

    protected final static Set<String> percentageParameterMap = new HashSet<String>(Arrays.asList(PERCENTAGE_PARAMETERS));

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
    protected MageController				controller;
    protected Mage 							mage;
    protected Location    					location;
    protected CastContext currentCast;

    /*
     * Variant properties
     */
    private SpellKey spellKey;
    private String name;
    private String alias;
    private String description;
    private String extendedDescription;
    private String levelDescription;
    private String upgradeDescription;
    private String usage;
    private long worth;
    private Color color;
    private String particle;
    private SpellCategory category;
    private BaseSpell template;
    private MageSpell upgrade;
    private long requiredUpgradeCasts;
    private String requiredUpgradePath;
    private MaterialAndData icon = new MaterialAndData(Material.AIR);
    private String iconURL = null;
    private List<CastingCost> costs = null;
    private List<CastingCost> activeCosts = null;

    protected boolean pvpRestricted           	= false;
    protected boolean bypassPvpRestriction    	= false;
    protected boolean bypassConfusion             = false;
    protected boolean bypassPermissions           = false;
    protected boolean castOnNoTarget              = false;
    protected boolean bypassDeactivate            = false;
    protected boolean quiet                       = false;
    protected boolean loud                        = false;

    private boolean backfired                   = false;
    private boolean hidden                      = false;

    protected ConfigurationSection parameters = null;
    protected ConfigurationSection configuration = null;

    protected static Random random            = new Random();

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

    private Map<String, Collection<EffectPlayer>>     effects				= new HashMap<String, Collection<EffectPlayer>>();

    private float								fizzleChance			= 0.0f;
    private float								backfireChance			= 0.0f;

    private long 								lastMessageSent 			= 0;
    private Set<Material>						preventPassThroughMaterials = null;
    private Set<Material>                       passthroughMaterials = null;
    private Collection<EffectPlayer>            currentEffects              = null;

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

    /*
     * Ground / location search and test functions
     */
    public boolean isOkToStandIn(Material mat)
    {
        return passthroughMaterials.contains(mat);
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

        if (block.getY() > MAX_Y) {
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
        return tryFindPlaceToStand(targetLoc, MAX_Y, MAX_Y);
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
        int maxY = targetLoc.getWorld().getEnvironment() == Environment.NETHER ? MAX_NETHER_Y : MAX_Y;

        int targetY = targetLoc.getBlockY();
        if (targetY >= minY && targetY <= maxY && isSafeLocation(targetLoc)) return targetLoc;

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
            // First look down just a little bit
            int testMinY = Math.min(maxDownDelta, 4);
            location = findPlaceToStand(targetLoc, false, testMinY);

            // Then look up
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
        return findPlaceToStand(target, goUp, MAX_Y);
    }

    public Location findPlaceToStand(Location target, boolean goUp, int maxDelta)
    {
        int direction = goUp ? 1 : -1;

        // search for a spot to stand
        Location targetLocation = target.clone();
        int yDelta = 0;
        int minY = MIN_Y;
        int maxY = targetLocation.getWorld().getEnvironment() == Environment.NETHER ? MAX_NETHER_Y : MAX_Y;

        while (minY <= targetLocation.getY() && targetLocation.getY() <= maxY && yDelta < maxDelta)
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

            yDelta++;
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
    public void castMessage(String message)
    {
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
    public void sendMessage(String message)
    {
        if (!quiet && message != null && message.length() > 0)
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

    @Override
    public Location getEyeLocation()
    {
        if (this.location != null)
        {
            Location location = this.location.clone();
            location.setY(location.getY() + 1.5);
            return location;
        }
        return mage.getEyeLocation();
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
            if (random.nextDouble() > 0.5) {
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

    public static Collection<PotionEffect> getPotionEffects(ConfigurationSection parameters)
    {
        return getPotionEffects(parameters, null);
    }

    public static Collection<PotionEffect> getPotionEffects(ConfigurationSection parameters, Integer duration)
    {
        List<PotionEffect> effects = new ArrayList<PotionEffect>();
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
                        String[] pieces = value.split(",");
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

    public FireworkEffect getFireworkEffect() {
        return getFireworkEffect(null, null, null, null, null);
    }

    public FireworkEffect getFireworkEffect(Color color1, Color color2, org.bukkit.FireworkEffect.Type fireworkType) {
            return getFireworkEffect(color1, color2, fireworkType, null, null);
    }

    public FireworkEffect getFireworkEffect(Color color1, Color color2, org.bukkit.FireworkEffect.Type fireworkType, Boolean flicker, Boolean trail) {
        Color wandColor = mage == null ? null : mage.getEffectColor();
        if (wandColor != null) {
            color1 = wandColor;
            color2 = wandColor.mixColors(color1, Color.WHITE);
        } else {
            if (color1 == null) {
                color1 = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
            }
            if (color2 == null) {
                color2 = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
            }
        }
        if (fireworkType == null) {
            fireworkType = org.bukkit.FireworkEffect.Type.values()[random.nextInt(org.bukkit.FireworkEffect.Type.values().length)];
        }
        if (flicker == null) {
            flicker = random.nextBoolean();
        }
        if (trail == null) {
            trail = random.nextBoolean();
        }

        return FireworkEffect.builder().flicker(flicker).withColor(color1).withFade(color2).with(fireworkType).trail(trail).build();
    }

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
    public boolean isUndoable()
    {
        return false;
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

    protected List<CastingCost> parseCosts(ConfigurationSection node) {
        if (node == null) {
            return null;
        }
        List<CastingCost> castingCosts = new ArrayList<CastingCost>();
        Set<String> costKeys = node.getKeys(false);
        for (String key : costKeys)
        {
            castingCosts.add(new CastingCost(key, node.getInt(key, 1)));
        }

        return castingCosts;
    }

    @SuppressWarnings("unchecked")
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
        requiredUpgradeCasts = node.getLong("upgrade_required_casts");

        // Can be overridden by the base spell, or the variant spell
        levelDescription = controller.getMessages().get("spells." + baseKey + ".level_description", levelDescription);
        upgradeDescription = controller.getMessages().get("spells." + baseKey + ".upgrade_description", upgradeDescription);

        // Spell level variants can override
        if (spellKey.isVariant()) {
            String variantKey = spellKey.getKey();
            name = controller.getMessages().get("spells." + variantKey + ".name", name);
            description = controller.getMessages().get("spells." + variantKey + ".description", description);
            extendedDescription = controller.getMessages().get("spells." + variantKey + ".extended_description", extendedDescription);
            usage = controller.getMessages().get("spells." + variantKey + ".usage", usage);

            // Level description defaults to pre-formatted text
            levelDescription = controller.getMessages().get("spell.level_description", levelDescription);

            // Any spell may have a level description, including base spells if chosen.
            // Base spells must specify their own level in each spell config though,
            // they don't get an auto-generated one.
            levelDescription = controller.getMessages().get("spells." + variantKey + ".level_description", levelDescription);
            upgradeDescription = controller.getMessages().get("spells." + variantKey + ".upgrade_description", upgradeDescription);
        }

        // Individual spell configuration overrides all
        name = node.getString("name", name);
        alias = node.getString("alias", "");
        extendedDescription = node.getString("extended_description", extendedDescription);
        description = node.getString("description", description);
        levelDescription = node.getString("level_description", levelDescription);

        // Parameterize level description
        if (levelDescription != null && !levelDescription.isEmpty()) {
            levelDescription = levelDescription.replace("$level", Integer.toString(spellKey.getLevel()));
        }

        // Load basic properties
        icon = ConfigurationUtils.getMaterialAndData(node, "icon", icon);
        iconURL = node.getString("icon_url");
        color = ConfigurationUtils.getColor(node, "color", null);
        worth = node.getLong("worth", worth);
        category = controller.getCategory(node.getString("category"));
        parameters = node.getConfigurationSection("parameters");
        costs = parseCosts(node.getConfigurationSection("costs"));
        activeCosts = parseCosts(node.getConfigurationSection("active_costs"));
        pvpRestricted = node.getBoolean("pvp_restricted", pvpRestricted);
        castOnNoTarget = node.getBoolean("cast_on_no_target", castOnNoTarget);
        hidden = node.getBoolean("hidden", false);

        // Preload some parameters
        ConfigurationSection parameters = node.getConfigurationSection("parameters");
        if (parameters != null) {
            cooldown = parameters.getInt("cooldown", cooldown);
            cooldown = parameters.getInt("cool", cooldown);
            bypassPvpRestriction = parameters.getBoolean("bypass_pvp", bypassPvpRestriction);
            bypassPvpRestriction = parameters.getBoolean("bp", bypassPvpRestriction);
            bypassPermissions = parameters.getBoolean("bypass_permissions", bypassPermissions);
            duration = parameters.getInt("duration", duration);
        }

        effects.clear();
        if (node.contains("effects")) {
            ConfigurationSection effectsNode = node.getConfigurationSection("effects");
            Collection<String> effectKeys = effectsNode.getKeys(false);
            for (String effectKey : effectKeys) {
                if (effectsNode.isString(effectKey)) {
                    String referenceKey = effectsNode.getString(effectKey);
                    if (effects.containsKey(referenceKey)) {
                        effects.put(effectKey, new ArrayList(effects.get(referenceKey)));
                    }
                }
                else
                {
                    effects.put(effectKey, EffectPlayer.loadEffects(controller.getPlugin(), effectsNode, effectKey));
                }
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

        backfired = false;
        cancelEffects();
    }

    public boolean cast(String[] extraParameters, Location defaultLocation)
    {
        this.reset();

        // Allow other plugins to cancel this cast
        PreCastEvent preCast = new PreCastEvent(mage, this);
        Bukkit.getPluginManager().callEvent(preCast);

        if (preCast.isCancelled()) {
            processResult(SpellResult.CANCELLED);
            return false;
        }

        this.currentCast = new CastContext(this);

        if (this.parameters == null) {
            this.parameters = new MemoryConfiguration();
        }

        this.location = defaultLocation;

        final ConfigurationSection parameters = new MemoryConfiguration();
        ConfigurationUtils.addConfigurations(parameters, this.parameters);
        ConfigurationUtils.addParameters(extraParameters, parameters);
        processParameters(parameters);

        // Don't allow casting if the player is confused
        bypassConfusion = parameters.getBoolean("bypass_confusion", bypassConfusion);
        LivingEntity livingEntity = mage.getLivingEntity();
        if (livingEntity != null && !bypassConfusion && !mage.isSuperPowered() && livingEntity.hasPotionEffect(PotionEffectType.CONFUSION)) {
            processResult(SpellResult.CURSED);
            return false;
        }

        // Don't perform permission check until after processing parameters, in case of overrides
        if (!canCast(getLocation())) {
            processResult(SpellResult.INSUFFICIENT_PERMISSION);
            return false;
        }

        this.preCast();

        // PVP override settings
        bypassPvpRestriction = parameters.getBoolean("bypass_pvp", false);
        bypassPvpRestriction = parameters.getBoolean("bp", bypassPvpRestriction);
        bypassPermissions = parameters.getBoolean("bypass_permissions", bypassPermissions);

        // Check cooldowns
        cooldown = parameters.getInt("cooldown", cooldown);
        cooldown = parameters.getInt("cool", cooldown);

        // Color override
        color = ConfigurationUtils.getColor(parameters, "color", color);
        particle = parameters.getString("particle", null);

        long cooldownRemaining = getRemainingCooldown() / 1000;
        String timeDescription = "";
        if (cooldownRemaining > 0) {
            if (cooldownRemaining > 60 * 60 ) {
                long hours = cooldownRemaining / (60 * 60);
                if (hours == 1) {
                    timeDescription = controller.getMessages().get("cooldown.wait_hour");
                } else {
                    timeDescription = controller.getMessages().get("cooldown.wait_hours").replace("$hours", ((Long) hours).toString());
                }
            } else if (cooldownRemaining > 60) {
                long minutes = cooldownRemaining / 60;
                if (minutes == 1) {
                    timeDescription = controller.getMessages().get("cooldown.wait_minute");
                } else {
                    timeDescription = controller.getMessages().get("cooldown.wait_minutes").replace("$minutes", ((Long) minutes).toString());
                }
            } else if (cooldownRemaining > 1) {
                timeDescription = controller.getMessages().get("cooldown.wait_seconds").replace("$seconds", ((Long)cooldownRemaining).toString());
            } else {
                timeDescription = controller.getMessages().get("cooldown.wait_moment");
            }
            sendMessage(getMessage("cooldown").replace("$time", timeDescription));
            processResult(SpellResult.COOLDOWN);
            return false;
        }

        com.elmakers.mine.bukkit.api.spell.CastingCost required = getRequiredCost();
        if (required != null) {
            String baseMessage = getMessage("insufficient_resources");
            String costDescription = required.getDescription(controller.getMessages(), mage);
            sendMessage(baseMessage.replace("$cost", costDescription));
            processResult(SpellResult.INSUFFICIENT_RESOURCES);
            return false;
        }

        return finalizeCast(parameters);
    }

    public boolean canCast(Location location) {
        if (!hasCastPermission(mage.getCommandSender())) return false;
        Boolean regionPermission = controller.getRegionCastPermission(mage.getPlayer(), this, location);
        if (regionPermission != null && regionPermission == true) return true;
        Boolean personalPermission = controller.getPersonalCastPermission(mage.getPlayer(), this, location);
        if (personalPermission != null && personalPermission == true) return true;
        if (regionPermission != null && regionPermission == false) return false;
        if (requiresBuildPermission() && !hasBuildPermission(location.getBlock())) return false;
        return !pvpRestricted || bypassPvpRestriction || mage.isPVPAllowed(location);
    }

    @Override
    public boolean isPvpRestricted() {
        return pvpRestricted && !bypassPvpRestriction;
    }

    @Override
    public boolean requiresBuildPermission() {
        return false;
    }

    public boolean hasBuildPermission(Block block) {

        // Cast permissions bypass
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
        processResult(result);

        boolean success = result.isSuccess();
        boolean requiresCost = success || (castOnNoTarget && result == SpellResult.NO_TARGET);
        boolean free = !requiresCost && result.isFree();
        if (!free) {
            lastCast = System.currentTimeMillis();
            if (costs != null && !mage.isCostFree()) {
                for (CastingCost cost : costs)
                {
                    cost.use(this);
                }
            }
        }
        if (success) {
            castCount++;
            if (template != null) {
                template.castCount++;
            }
        }

        return success;
    }

    public String getMessage(String messageKey) {
        return getMessage(messageKey, "");
    }

    public String getMessage(String messageKey, String def) {
        String message = controller.getMessages().get("spells.default." + messageKey, def);
        message = controller.getMessages().get("spells." + spellKey.getBaseKey() + "." + messageKey, message);
        if (spellKey.isVariant()) {
            message = controller.getMessages().get("spells." + spellKey.getKey() + "." + messageKey, message);
        }
        if (message == null) message = "";

        // Escape some common parameters
        String playerName = mage.getName();
        message = message.replace("$player", playerName);

        String materialName = getDisplayMaterialName();

        // TODO: Localize "None", provide static getter
        materialName = materialName == null ? "None" : materialName;
        message = message.replace("$material", materialName);

        return message;
    }

    protected String getDisplayMaterialName()
    {
        return "None";
    }

    protected void processResult(SpellResult result) {
        // Notify other plugins of this spell cast
        CastEvent castEvent = new CastEvent(mage, this, result);
        Bukkit.getPluginManager().callEvent(castEvent);

        if (mage != null) {
            mage.onCast(this, result);
        }

        // Show messaging
        String resultName = result.name().toLowerCase();
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
            } else if (targetEntity instanceof Entity) {
                message = getMessage("cast_entity", message);
            }
            if (loud) {
                sendMessage(message);
            } else {
                castMessage(message);
            }
            messageTargets("cast_player_message");
        } else
        // Special cases where messaging is handled elsewhere
        if (result != SpellResult.INSUFFICIENT_RESOURCES && result != SpellResult.COOLDOWN)
        {
            String message = null;
            if (result.isFailure() && result != SpellResult.FAIL) {
                message = getMessage("fail");
            }

            sendMessage(getMessage(resultName, message));
        }

        // Play effects
        playEffects(resultName);
    }

    public void messageTargets(String messageKey)
    {
        if (currentCast != null)
        {
            currentCast.messageTargets(messageKey);
        }
    }

    public void playEffects(String effectName, float scale) {
        playEffects(effectName, getCurrentCast(), scale);
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

    @Override
    public void playEffects(String effectName, com.elmakers.mine.bukkit.api.action.CastContext context, float scale) {
        Location source = context.getEyeLocation();
        if (effects.containsKey(effectName) && source != null)
        {
            cancelEffects();
            currentEffects = effects.get(effectName);
            Collection<Entity> targeted = context.getTargetEntities();
            Entity sourceEntity = context.getEntity();
            Entity targetEntity = context.getTargetEntity();
            Location targetLocation = context.getTargetLocation();
            for (EffectPlayer player : currentEffects)
            {
                // Set scale
                player.setScale(scale);

                // Set material and color
                player.setMaterial(getEffectMaterial());
                player.setColor(mage.getEffectColor());
                String overrideParticle = particle != null ? particle : mage.getEffectParticleName();
                player.setParticleOverride(overrideParticle);

                if (player.shouldPlayAtAllTargets())
                {
                    player.start(source, sourceEntity, targeted);
                }
                else
                {
                    player.start(source, sourceEntity, targetLocation, targetEntity);
                }
            }
        }
    }

    @Override
    public void playEffects(String effectName, float scale, Location source, Entity sourceEntity, Location targetLocation, Entity targetEntity)
    {
        CastContext context = new CastContext(this, sourceEntity, source);
        context.setTargetLocation(targetLocation);
        context.setTargetEntity(targetEntity);
        playEffects(effectName, context, scale);
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
        return true;
    }

    @Override
    public Entity getTargetEntity() {
        return null;
    }

    public com.elmakers.mine.bukkit.api.block.MaterialAndData getEffectMaterial()
    {
        return new MaterialAndData(DEFAULT_EFFECT_MATERIAL);
    }

    protected void processParameters(ConfigurationSection parameters) {
        fizzleChance = (float)parameters.getDouble("fizzle_chance", fizzleChance);
        backfireChance = (float)parameters.getDouble("backfire_chance", backfireChance);

        Location defaultLocation = location == null ? mage.getLocation() : location;
        Location locationOverride = ConfigurationUtils.overrideLocation(parameters, "p", defaultLocation, controller.canCreateWorlds());
        if (locationOverride != null) {
            location = locationOverride;
        }
        costReduction = (float)parameters.getDouble("cost_reduction", 0);
        cooldownReduction = (float)parameters.getDouble("cooldown_reduction", 0);

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

        bypassDeactivate = parameters.getBoolean("bypass_deactivate", false);
        quiet = parameters.getBoolean("quiet", false);
        loud = parameters.getBoolean("loud", false);
    }


    public String getPermissionNode()
    {
        return "Magic.cast." + spellKey.getBaseKey();
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
    public void initialize(MageController instance)
    {
        this.controller = instance;
    }

    @Override
    public long getCastCount()
    {
        return castCount;
    }

    @Override
    public void setCastCount(long count) {
        castCount = count;
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
    public float getCostReduction()
    {
        return costReduction + mage.getCostReduction();
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
            spell.template = this;
        } catch (Throwable ex) {
            controller.getLogger().log(Level.WARNING, "Error creating spell " + spellKey.getKey(), ex);
        }
        return spell;
    }

    @Override
    public boolean cast()
    {
        return cast(null, null);
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
    public final com.elmakers.mine.bukkit.api.block.MaterialAndData getIcon()
    {
        return icon;
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
    public final long getWorth()
    {
        return worth;
    }

    @Override
    public final SpellCategory getCategory()
    {
        return category;
    }

    @Override
    public Collection<com.elmakers.mine.bukkit.api.effect.EffectPlayer> getEffects(SpellResult result) {
        return getEffects(result.name().toLowerCase());
    }
    @Override
    public Collection<com.elmakers.mine.bukkit.api.effect.EffectPlayer> getEffects(String key) {
        Collection<EffectPlayer> effectList = effects.get(key);
        if (effectList == null) {
            return new ArrayList<com.elmakers.mine.bukkit.api.effect.EffectPlayer>();
        }
        return new ArrayList<com.elmakers.mine.bukkit.api.effect.EffectPlayer>(effectList);
    }

    @Override
    public Collection<com.elmakers.mine.bukkit.api.spell.CastingCost> getCosts() {
        if (costs == null) return null;
        List<com.elmakers.mine.bukkit.api.spell.CastingCost> copy = new ArrayList<com.elmakers.mine.bukkit.api.spell.CastingCost>();
        copy.addAll(costs);
        return copy;
    }

    @Override
    public Collection<com.elmakers.mine.bukkit.api.spell.CastingCost> getActiveCosts() {
        if (activeCosts == null) return null;
        List<com.elmakers.mine.bukkit.api.spell.CastingCost> copy = new ArrayList<com.elmakers.mine.bukkit.api.spell.CastingCost>();
        copy.addAll(activeCosts);
        return copy;
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
    public String getCooldownDescription() {
        if (cooldown > 0) {
            int cooldownInSeconds = cooldown / 1000;
            if (cooldownInSeconds > 60 * 60 ) {
                int hours = cooldownInSeconds / (60 * 60);
                if (hours == 1) {
                    return controller.getMessages().get("cooldown.description_hour");
                }
                return controller.getMessages().get("cooldown.description_hours").replace("$hours", ((Integer)hours).toString());
            } else if (cooldownInSeconds > 60) {
                int minutes = cooldownInSeconds / 60;
                if (minutes == 1) {
                    return controller.getMessages().get("cooldown.description_minute");
                }
                return controller.getMessages().get("cooldown.description_minutes").replace("$minutes", ((Integer)minutes).toString());
            } else if (cooldownInSeconds > 1) {
                return controller.getMessages().get("cooldown.description_seconds").replace("$seconds", ((Integer)cooldownInSeconds).toString());
            } else {
                return controller.getMessages().get("cooldown.description_moment");
            }
        }
        return null;
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
            if (costs != null && !isActive)
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
    public long getRemainingCooldown() {
        long currentTime = System.currentTimeMillis();
        if (!mage.isCooldownFree()) {
            double cooldownReduction = mage.getCooldownReduction() + this.cooldownReduction;
            if (cooldownReduction < 1 && !isActive && cooldown > 0) {
                int reducedCooldown = (int)Math.ceil((1.0f - cooldownReduction) * cooldown);
                if (lastCast != 0 && lastCast > currentTime - reducedCooldown)
                {
                    return lastCast - (currentTime - reducedCooldown);
                }
            }
        }

        return 0;
    }

    @Override
    public long getDuration()
    {
        return duration;
    }

    @Override
    public void setMage(Mage mage)
    {
        this.mage = mage;
    }

    @Override
    public boolean cancel()
    {
        boolean cancelled = onCancel();
        if (cancelled) {
            sendMessage(getMessage("cancel"));
        }
        return cancelled;
    }

    @Override
    public void reactivate() {
        isActive = true;
        onActivate();
    }
    @Override
    public void activate() {
        if (!isActive) {
            mage.activateSpell(this);
        }
    }

    @Override
    public boolean deactivate() {
        return deactivate(false, false);
    }

    @Override
    public boolean deactivate(boolean force, boolean quiet) {
        if (!force && bypassDeactivate) {
            return false;
        }
        if (isActive) {
            isActive = false;
            onDeactivate();

            mage.deactivateSpell(this);
            if (!quiet) {
                sendMessage(getMessage("deactivate"));
            }
            cancelEffects();
        }

        return true;
    }

    public void cancelEffects() {
        if (currentEffects != null) {
            for (EffectPlayer player : currentEffects) {
                player.cancel();
            }
            currentEffects = null;
        }
    }

    @Override
    public Mage getMage() {
        return mage;
    }

    @Override
    public void load(ConfigurationSection node) {
        try {
            castCount = node.getLong("cast_count", 0);
            lastCast = node.getLong("last_cast", 0);
            if (category != null && template == null) {
                category.addCasts(castCount, lastCast);
            }
            isActive = node.getBoolean("active", false);
            onLoad(node);
        } catch (Exception ex) {
            controller.getPlugin().getLogger().warning("Failed to load data for spell " + name + ": " + ex.getMessage());
        }
    }

    @Override
    public void save(ConfigurationSection node) {
        try {
            node.set("cast_count", castCount);
            node.set("last_cast", lastCast);
            node.set("active", isActive);
            onSave(node);
        } catch (Exception ex) {
            controller.getPlugin().getLogger().warning("Failed to save data for spell " + name);
            ex.printStackTrace();
        }
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
         return isActive;
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
    public String getRequiredUpgradePath() {
        return requiredUpgradePath;
    }

    @Override
    public long getRequiredUpgradeCasts() {
        return requiredUpgradeCasts;
    }

    @Override
    public String getUpgradeDescription() {
        return upgradeDescription == null ? "" : upgradeDescription;
    }

    @Override
    public MageSpell getUpgrade() {
        return upgrade;
    }

    @Override
    public void setUpgrade(MageSpell upgrade) {
        this.upgrade = upgrade;
    }

    @Override
    public ConfigurationSection getConfiguration() {
        return this.configuration;
    }

    @Override
    public com.elmakers.mine.bukkit.api.action.CastContext getCurrentCast() {
        if (currentCast == null) {
            currentCast = new CastContext(this);
        }
        return currentCast;
    }
}
