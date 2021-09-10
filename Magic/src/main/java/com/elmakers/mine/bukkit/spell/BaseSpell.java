package com.elmakers.mine.bukkit.spell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BossBar;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
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

import com.elmakers.mine.bukkit.action.CastContext;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.batch.SpellBatch;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.data.SpellData;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.event.CastEvent;
import com.elmakers.mine.bukkit.api.event.EarnEvent;
import com.elmakers.mine.bukkit.api.event.PreCastEvent;
import com.elmakers.mine.bukkit.api.event.StartCastEvent;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.MaterialSetManager;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.magic.Trigger;
import com.elmakers.mine.bukkit.api.magic.VariableScope;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.spell.CastParameter;
import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.CooldownReducer;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.PrerequisiteSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellCategory;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.boss.BossBarConfiguration;
import com.elmakers.mine.bukkit.configuration.SpellParameters;
import com.elmakers.mine.bukkit.item.Cost;
import com.elmakers.mine.bukkit.item.Icon;
import com.elmakers.mine.bukkit.magic.MageClass;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

import de.slikey.effectlib.math.EquationStore;
import de.slikey.effectlib.math.EquationTransform;

public class BaseSpell implements MageSpell, Cloneable {
    public static String DEFAULT_DISABLED_ICON_URL = "";
    public static Material DEFAULT_SPELL_ICON = Material.STICK;

    public enum ToggleType { NONE, CANCEL, UNDO, UNDO_IF_ACTIVE }

    protected static final double LOOK_THRESHOLD = 0.98;

    // TODO: Configurable default? this does look cool, though.
    protected static final Material DEFAULT_EFFECT_MATERIAL = Material.WATER;

    public static final String[] EXAMPLE_VECTOR_COMPONENTS = {"-1", "-0.5", "0", "0.5", "1", "~-1", "~-0.5", "~0", "~0.5", "*1", "*-1", "*-0.5", "*0.5", "*1"};
    public static final String[] EXAMPLE_SIZES = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "12", "16", "32", "64"};
    public static final String[] EXAMPLE_INTEGERS = {"-10", "-5", "-1", "1", "5", "10"};
    public static final String[] EXAMPLE_BOOLEANS = {"true", "false"};
    public static final String[] EXAMPLE_DURATIONS = {"500", "1000", "2000", "5000", "10000", "60000", "120000"};
    public static final String[] EXAMPLE_PERCENTAGES = {"0", "0.1", "0.25", "0.5", "0.75", "1"};

    public static final String[] OTHER_PARAMETERS = {
        "transparent", "target", "target_type", "range", "duration", "player", "cooldown", "charges", "charge_regeneration"
    };

    public static final String[] WORLD_PARAMETERS = {
        "pworld", "tworld", "otworld", "t2world"
    };

    protected static final Set<String> worldParameterMap = new HashSet<>(Arrays.asList(WORLD_PARAMETERS));

    public static final String[] VECTOR_PARAMETERS = {
        "px", "py", "pz", "pdx", "pdy", "pdz", "tx", "ty", "tz", "otx", "oty", "otz", "t2x", "t2y", "t2z",
        "otdx", "otdy", "otdz"
    };

    protected static final Set<String> vectorParameterMap = new HashSet<>(Arrays.asList(VECTOR_PARAMETERS));

    public static final String[] BOOLEAN_PARAMETERS = {
        "allow_max_range", "prevent_passthrough", "reverse_targeting", "passthrough", "bypass_protection",
        "bypass", "bypass_build", "bypass_break", "bypass_pvp", "target_npc", "ignore_blocks", "target_self",
        "disable_mana_regeneration", "deny_build", "deny_break", "bypass_friendly_fire"
    };

    protected static final Set<String> booleanParameterMap = new HashSet<>(Arrays.asList(BOOLEAN_PARAMETERS));

    public static final String[] PERCENTAGE_PARAMETERS = {
        "fizzle_chance", "backfire_chance", "cooldown_reduction"
    };

    protected static final Set<String> percentageParameterMap = new HashSet<>(Arrays.asList(PERCENTAGE_PARAMETERS));

    public static final String[] COMMON_PARAMETERS = (String[])
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
    protected MageController                controller;
    protected Mage                             mage;
    protected MageClass                     mageClass;
    protected Location                        location;
    protected CastContext                   currentCast;
    protected UndoList                      toggleUndo;

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
    private String creatorId;
    private String creatorName;
    private Cost cost;
    private Cost earns;
    private Color color;
    private String particle;
    private SpellCategory category;
    private Set<String> tags;
    private BaseSpell template;
    private long requiredUpgradeCasts;
    private String requiredUpgradePath;
    private Set<String> requiredUpgradeTags;
    private Collection<PrerequisiteSpell> requiredSpells;
    private List<SpellKey> removesSpells;
    private MaterialAndData iconMaterial =  null;
    private MaterialAndData disabledIconMaterial = null;
    private com.elmakers.mine.bukkit.api.item.Icon icon;
    protected Set<EntityType> friendlyEntityTypes = null;
    private double requiredHealth;
    private List<CastingCost> costs = null;
    private List<CastingCost> activeCosts = null;
    private List<Trigger> triggers = null;
    private ConfigurationSection messages = null;

    // Variable default definitions
    // For convenience, these can be provide as a list of configurations, or as a map to objects
    private Collection<ConfigurationSection> variablesList;
    private ConfigurationSection variablesSection;
    private final Map<String, VariableScope> variableScopes = new HashMap<>();

    protected boolean cancelOnDeactivate        = true;
    protected double cancelOnDamage             = 0;
    protected boolean cancelOnDeath            = false;
    protected boolean cancelOnCastOther         = false;
    protected boolean cancelOnWorldChange         = false;
    protected boolean cancelOnNoPermission      = false;
    protected boolean cancelOnNoWand            = false;
    protected boolean creativeRestricted               = false;
    protected boolean pvpRestricted               = false;
    protected boolean disguiseRestricted        = false;
    protected boolean worldBorderRestricted     = true;
    protected boolean glideRestricted           = false;
    protected boolean glideExclusive            = false;
    protected boolean usesBrushSelection        = false;
    protected boolean bypassFriendlyFire        = false;
    protected boolean onlyFriendlyFire            = false;
    protected boolean bypassPvpRestriction        = false;
    protected boolean bypassBuildRestriction    = false;
    protected boolean bypassBreakRestriction    = false;
    protected boolean selfTargetingRequiresPvP  = false;
    protected boolean denyBreakPermission       = false;
    protected boolean denyBuildPermission       = false;
    protected boolean bypassProtection          = false;
    protected boolean bypassConfusion           = true;
    protected boolean bypassWeakness            = true;
    protected boolean bypassPermissions         = false;
    protected boolean bypassRegionPermission    = false;
    protected boolean ignoreRegionOverrides     = false;
    protected boolean castOnNoTarget            = true;
    protected boolean refundOnNoTarget          = false;
    protected boolean bypassDeactivate          = false;
    protected boolean bypassAll                 = false;
    protected boolean quiet                     = false;
    protected boolean loud                      = false;
    protected ToggleType toggle                 = ToggleType.NONE;
    protected boolean disableManaRegeneration   = false;
    protected boolean messageTargets            = true;
    protected boolean targetSelf                = false;
    protected boolean showUndoable              = true;
    protected boolean cancellable               = true;
    protected boolean quickCast                 = false;
    protected boolean cancelEffects             = false;
    protected boolean deactivateEffects         = true;
    protected boolean commandBlockAllowed       = true;
    protected int verticalSearchDistance        = 8;
    protected Set<String> hideMessages          = null;
    protected Set<String> activeSpellsExclusive = null;
    protected Set<String> activeSpellsRestricted = null;

    private boolean backfired                   = false;
    private boolean hidden                      = false;
    private boolean passive                     = false;
    private boolean aura                        = false;
    private boolean toggleable                  = true;
    private boolean reactivate                  = false;
    private boolean isActive                    = false;
    private boolean cancelled                   = false;

    protected ConfigurationSection progressLevels = null;
    protected ConfigurationSection progressLevelParameters = null;
    protected SpellParameters parameters;
    protected ConfigurationSection workingParameters = null;
    protected ConfigurationSection configuration = null;
    protected Collection<Requirement> requirements = null;

    // Boss bar
    protected BossBar bossBar;
    protected BossBarConfiguration bossBarConfiguration;
    protected double bossBarMana;

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
    private boolean                             bypassCooldown          = false;
    private int                                 mageCooldown            = 0;
    private int                                 cooldown                = 0;
    private int maxCharges = 0;
    private double                              rechargeRate            = 1;
    private int                                 displayCooldown         = -1;
    private int                                 warmup                  = 0;
    private int                                 earnCooldown            = 0;
    private int                                 duration                = 0;
    private int                                 totalDuration           = -1;
    private long                                lastActiveCost          = 0;
    private float                               activeCostScale         = 1;

    private Map<String, Collection<EffectPlayer>>     effects                = new HashMap<>();

    private float                                fizzleChance            = 0.0f;
    private float                                backfireChance            = 0.0f;

    private long                                 lastMessageSent             = 0;
    private MaterialSet                         preventPassThroughMaterials = null;
    private MaterialSet                         passthroughMaterials = null;
    private MaterialSet                         unsafeMaterials = null;

    // Kind of hacky, used in lore generation.
    private Mage                                reducerMage = null;
    private Wand                                reducerWand = null;

    @Deprecated // Material
    public boolean allowPassThrough(Material mat)
    {
        if (mage != null && mage.isSuperPowered()) {
            return true;
        }
        if (passthroughMaterials != null && passthroughMaterials.testMaterial(mat)) {
            return true;
        }
        return preventPassThroughMaterials == null || !preventPassThroughMaterials.testMaterial(mat);
    }

    public boolean allowPassThrough(Block block) {
        if (!CompatibilityLib.getCompatibilityUtils().isChunkLoaded(block)) return false;
        if (mage != null && mage.isSuperPowered()) {
            return true;
        }
        if (passthroughMaterials != null
                && passthroughMaterials.testBlock(block)) {
            return true;
        }
        return preventPassThroughMaterials == null
                || !preventPassThroughMaterials.testBlock(block);
    }

    @Deprecated // Material
    public boolean isPassthrough(Material mat) {
        return passthroughMaterials != null
                && passthroughMaterials.testMaterial(mat);
    }

    public boolean isPassthrough(Block block) {
        if (!CompatibilityLib.getCompatibilityUtils().isChunkLoaded(block)) return false;
        return passthroughMaterials != null
                && passthroughMaterials.testBlock(block);
    }

    /*
     * Ground / location search and test functions
     */
    @Deprecated // Material
    public boolean isOkToStandIn(Material mat)
    {
        if (isHalfBlock(mat)) {
            return false;
        }
        return passthroughMaterials.testMaterial(mat) && !unsafeMaterials.testMaterial(mat);
    }

    public boolean isOkToStandIn(Block block) {
        if (!CompatibilityLib.getCompatibilityUtils().isChunkLoaded(block)) return false;
        if (isHalfBlock(block.getType())) {
            return false;
        }
        return passthroughMaterials.testBlock(block)
                && !unsafeMaterials.testBlock(block);
    }

    public boolean isWater(Material mat)
    {
        return DefaultMaterials.isWater(mat);
    }

    public boolean isOkToStandOn(Block block)
    {
        if (!CompatibilityLib.getCompatibilityUtils().isChunkLoaded(block)) return false;
        return isOkToStandOn(block.getType());
    }

    @Deprecated // Material
    public boolean isOkToStandOn(Material mat)
    {
        if (isHalfBlock(mat)) {
            return true;
        }
        return (mat != Material.AIR && !unsafeMaterials.testMaterial(mat) && !passthroughMaterials.testMaterial(mat));
    }

    protected boolean isHalfBlock(Material mat) {
        return DefaultMaterials.isHalfBlock(mat);
    }

    public boolean isSafeLocation(Block block)
    {
        if (!CompatibilityLib.getCompatibilityUtils().isChunkLoaded(block)) {
            return false;
        }

        if (block.getY() > controller.getMaxHeight(block.getWorld())) {
            return false;
        }

        Block blockOneUp = block.getRelative(BlockFace.UP);
        Block blockOneDown = block.getRelative(BlockFace.DOWN);

        // Ascend to top of water
        if (isUnderwater() && isWater(blockOneDown.getType())
            && blockOneUp.getType() == Material.AIR && block.getType() == Material.AIR) {
            return true;
        }

        Player player = mage.getPlayer();
        return (
                (isOkToStandOn(blockOneDown) || (player != null && player.isFlying()))
                &&    isOkToStandIn(blockOneUp)
                &&     isOkToStandIn(block)
        );
    }

    public boolean isSafeLocation(Location loc)
    {
        return isSafeLocation(loc.getBlock());
    }

    @Nullable
    public Location tryFindPlaceToStand(Location targetLoc) {
        int maxHeight = controller.getMaxHeight(targetLoc.getWorld());
        return tryFindPlaceToStand(targetLoc, maxHeight, maxHeight);
    }

    @Nullable
    public Location tryFindPlaceToStand(Location targetLoc, int maxDownDelta, int maxUpDelta) {
        Location location = findPlaceToStand(targetLoc, maxDownDelta, maxUpDelta);
        return location == null ? targetLoc : location;
    }

    @Nullable
    public Location findPlaceToStand(Location targetLoc) {
        return findPlaceToStand(targetLoc, verticalSearchDistance, verticalSearchDistance);
    }

    @Nullable
    public Location findPlaceToStand(Location targetLoc, int maxDownDelta, int maxUpDelta) {
        if (!CompatibilityLib.getCompatibilityUtils().isChunkLoaded(targetLoc)) return null;
        int minY = controller.getMinHeight(targetLoc.getWorld());
        int maxY = controller.getMaxHeight(targetLoc.getWorld());

        // Teleport above half blocks, we will get bumped down by checkForHalfBlock
        // if the location is safe
        if (isHalfBlock(targetLoc.getBlock().getType())) {
            targetLoc.setY(Math.floor(targetLoc.getY() + 1));
        }
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

    @Nullable
    public Location findPlaceToStand(Location target, boolean goUp) {
        return findPlaceToStand(target, goUp, verticalSearchDistance);
    }

    @Nullable
    public Location findPlaceToStand(Location target, boolean goUp, int maxDelta) {
        int direction = goUp ? 1 : -1;

        // search for a spot to stand
        Location targetLocation = target.clone();
        int yDelta = 0;
        int minY = controller.getMinHeight(targetLocation.getWorld());
        int maxY = controller.getMaxHeight(targetLocation.getWorld());

        while (minY <= targetLocation.getY() && targetLocation.getY() <= maxY && yDelta < maxDelta)
        {
            Block block = targetLocation.getBlock();
            if (!allowPassThrough(block)) {
                return null;
            }
            if (isSafeLocation(block))
            {
                // spot found - return location
                return checkForHalfBlock(targetLocation);
            }

            yDelta++;
            targetLocation.setY(targetLocation.getY() + direction);
        }

        // no spot found
        return null;
    }

    protected Location checkForHalfBlock(Location location) {
        // If the block below us is a half block, move the location down
        // to be on top of the half-block.
        Block downBlock = location.getBlock().getRelative(BlockFace.DOWN);
        Material material = downBlock.getType();
        if (isHalfBlock(material)) {
            if (!CompatibilityLib.getCompatibilityUtils().isTopBlock(downBlock)) {
                // Drop down to half-steps
                location.setY(location.getY() - 0.5);
            }
        }

        return location;
    }

    /**
     * Get the block the player is standing on.
     *
     * @return The Block the player is standing on
     */
    @Nullable
    public Block getPlayerBlock() {
        Location location = getLocation();
        if (location == null) return null;
        if (!CompatibilityLib.getCompatibilityUtils().isChunkLoaded(location)) return null;
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
        if (location == null) {
            return BlockFace.SELF;
        }
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
            if (currentCast != null) {
                message = currentCast.parameterize(message);
            }
            mage.castMessage(message);
            lastMessageSent = System.currentTimeMillis();
        }
    }

    /**
     * Send a message to a player.
     *
     * <p>Use this to send messages to the player that are important.
     *
     * @param message The message to send
     */
    @Override
    public void sendMessage(String message) {
        sendMessage(mage, message);
    }

    public void sendMessage(Mage mage, String message) {
        Wand activeWand = mage.getActiveWand();

        // First check wand
        if (!loud && activeWand != null && !activeWand.showMessages()) return;

        if (!quiet && message != null && message.length() > 0)
        {
            if (currentCast != null) {
                message = currentCast.parameterize(message);
            }
            mage.sendMessage(message);
            lastMessageSent = System.currentTimeMillis();
        }
    }

    public void sendMessageKey(String key) {
        sendMessageKey(key, null);
    }

    @Override
    public void sendMessageKey(String key, String message) {
        sendMessageKey(mage, key, message);
    }

    public void sendMessageKey(Mage mage, String key, String message) {
        if (hideMessages != null && hideMessages.contains(key.toLowerCase())) return;
        if (message == null) message = getMessage(key);
        sendMessage(mage, message);
    }

    public void castMessageKey(String key) {
        castMessageKey(key, null);
    }

    @Override
    public void castMessageKey(String key, String message) {
        if (hideMessages != null && hideMessages.contains(key.toLowerCase())) return;
        if (message == null) message = getMessage(key);
        castMessage(message);
    }

    @Nullable
    @Override
    public Location getLocation() {
        if (location != null) return location.clone();
        if (mage != null) {
            return mage.getLocation();
        }
        return null;
    }

    @Nullable
    public Location getWandLocation() {
        if (this.location != null)
        {
            return location.clone();
        }
        return mage.getWandLocation();
    }

    @Nullable
    public Location getCastLocation() {
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
        return direction.getY() > LOOK_THRESHOLD;
    }

    public boolean isLookingDown()
    {
        Vector direction = getDirection();
        if (direction == null) return false;
        return direction.getY() < -LOOK_THRESHOLD;
    }

    @Nullable
    public World getWorld() {
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
        Block eyeBlock = playerBlock.getRelative(BlockFace.UP);
        return isWater(playerBlock.getType()) && isWater(eyeBlock.getType());
    }

    @Nullable
    protected String getBlockSkin(Material blockType) {
        return controller.getBlockSkin(blockType);
    }

    @Nullable
    protected String getMobSkin(EntityType mobType) {
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
                PotionEffect effect = new PotionEffect(effectType, ticks, power, ambient, particles);
                effects.add(effect);
            }
        }
        return effects;
    }

    public boolean isInCircle(int x, int z, int r) {
        return ((x * x) +  (z * z) - (r * r)) <= 0;
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
    public boolean showBrush() {
        return usesBrushSelection();
    }

    @Override
    public boolean isUndoable()
    {
        return false;
    }

    @Override
    public boolean isScheduledUndo() {
        return false;
    }

    public void checkActiveCosts() {
        if (activeCosts == null) return;

        long now = System.currentTimeMillis();
        activeCostScale = (float)((double)(now - lastActiveCost) / 1000);
        lastActiveCost = now;

        CasterProperties caster = null;
        if (currentCast != null) {
            caster = currentCast.getWand();
            if (caster == null) {
                caster = currentCast.getMageClass();
            }
        }
        for (CastingCost cost : activeCosts)
        {
            if (!cost.has(mage, caster, this))
            {
                deactivate();
                break;
            }

            cost.deduct(mage, caster, this);
        }

        activeCostScale = 1;
    }

    public void checkActiveDuration() {
        if (duration > 0 && spellData.getLastCast() < System.currentTimeMillis() - duration) {
            deactivate();
        }
    }

    @Override
    public long getLastCast() {
        return spellData.getLastCast();
    }

    @Nullable
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

    @Override
    public void loadTemplate(String key, ConfigurationSection node) {
        spellKey = new SpellKey(key);
        // Create a temporary cast and mage variable holders so they can be resolved if needed for template processing
        ConfigurationSection castVariables = ConfigurationUtils.newConfigurationSection();
        ConfigurationSection mageVariables = ConfigurationUtils.newConfigurationSection();
        this.parameters = new SpellParameters(this, mageVariables, castVariables);
        this.configuration = node;
        this.loadTemplate(node, parameters);
    }

    protected void loadTemplate(ConfigurationSection node, SpellParameters parameters) {
        // Get variable definitions
        variablesList = ConfigurationUtils.getNodeList(node, "variables");
        variablesSection = node.getConfigurationSection("variables");

        initializeVariables(parameters);
        loadTemplate(node);
    }

    protected void loadTemplate(ConfigurationSection node) {
        // Get localizations
        String baseKey = spellKey.getBaseKey();

        // Message defaults come from the messages.yml file
        name = controller.getMessages().get("spells." + baseKey + ".name", baseKey);
        description = controller.getMessages().get("spells." + baseKey + ".description", "");
        extendedDescription = controller.getMessages().get("spells." + baseKey + ".extended_description", "");
        usage = controller.getMessages().get("spells." + baseKey + ".usage", "");

        // Owner information for editor use and fill_wand_creator
        creatorName = node.getString("creator");
        creatorId = node.getString("creator_id");

        // Embedded messages, will override anything in the messages configs
        messages = node.getConfigurationSection("messages");

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

        List<String> hideMessageKeys = ConfigurationUtils.getStringList(node, "hide_messages");
        if (hideMessageKeys == null || hideMessageKeys.isEmpty()) {
            hideMessages = null;
        } else {
            hideMessages = new HashSet<>(hideMessageKeys);
        }

        List<String> activeSpellsExclusiveKeys = ConfigurationUtils.getStringList(node, "active_spells_exclusive");
        if (activeSpellsExclusiveKeys == null || activeSpellsExclusiveKeys.isEmpty()) {
            activeSpellsExclusive = null;
        } else {
            activeSpellsExclusive = new HashSet<>(activeSpellsExclusiveKeys);
        }

        List<String> activeSpellsRestrictedKeys = ConfigurationUtils.getStringList(node, "active_spells_restricted");
        if (activeSpellsRestrictedKeys == null || activeSpellsRestrictedKeys.isEmpty()) {
            activeSpellsRestricted = null;
        } else {
            activeSpellsRestricted = new HashSet<>(activeSpellsRestrictedKeys);
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
        String iconReference = node.getString("icon");
        com.elmakers.mine.bukkit.api.item.Icon defaultIcon = controller.getDefaultIcon();
        icon = controller.getIcon(iconReference);
        if (icon == null) {
            // We will assume the "icon" requested is an item, so use it as the default
            icon = new Icon(defaultIcon, node, iconReference);
        } else {
            // Merge in defaults
            icon = new Icon(defaultIcon, icon);
            // Allow overriding via this config
            // The default item can still be overridden by "icon_item"
            icon = new Icon(icon, node, null);
        }
        boolean legacyIcons = controller.isLegacyIconsEnabled();
        iconMaterial = icon.getItemMaterial(legacyIcons);
        disabledIconMaterial = icon.getItemDisabledMaterial(legacyIcons);

        color = ConfigurationUtils.getColor(node, "color", null);
        if (node.contains("worth_sp")) {
            double worth = node.getDouble("worth_sp", 0) * controller.getWorthSkillPoints();
            cost = new Cost(controller, "sp", worth);
        } else {
            String costType = node.getString("worth_type", "sp");
            cost = Cost.parseCost(controller, node.getString("worth"), costType);
        }
        int spEarns = node.getInt("earns_sp", 0);
        if (spEarns > 0) {
            earns = new Cost(controller, "sp", spEarns);
        } else {
            String earnsType = node.getString("earns_type", "sp");
            earns = Cost.parseCost(controller, node.getString("earns"), earnsType);
        }
        earnCooldown = node.getInt("earns_cooldown", 0);
        double earnCooldownScale = node.getDouble("earns_cooldown_scale", 1);
        earnCooldown = (int)Math.ceil(earnCooldownScale * earnCooldown);
        category = controller.getCategory(node.getString("category"));
        Collection<String> tagList = ConfigurationUtils.getStringList(node, "tags");
        if (tagList != null) {
            tags = new HashSet<>(tagList);
        } else {
            tags = null;
        }

        requiredHealth = node.getDouble("require_health_percentage", 0);
        costs = parseCosts(node.getConfigurationSection("costs"));
        activeCosts = parseCosts(node.getConfigurationSection("active_costs"));
        pvpRestricted = node.getBoolean("pvp_restricted", false);
        quickCast = node.getBoolean("quick_cast", false);
        passive = node.getBoolean("passive", false);
        reactivate = node.getBoolean("reactivate", false);
        toggleable = node.getBoolean("toggleable", true);
        disguiseRestricted = node.getBoolean("disguise_restricted", false);
        creativeRestricted = node.getBoolean("creative_restricted", false);
        glideRestricted = node.getBoolean("glide_restricted", false);
        glideExclusive = node.getBoolean("glide_exclusive", false);
        worldBorderRestricted = node.getBoolean("world_border_restricted", false);
        usesBrushSelection = node.getBoolean("brush_selection", false);
        castOnNoTarget = node.getBoolean("cast_on_no_target", true);
        refundOnNoTarget = node.getBoolean("refund_on_no_target", false);
        hidden = node.getBoolean("hidden", false);
        showUndoable = node.getBoolean("show_undoable", true);
        cancellable = node.getBoolean("cancellable", true);
        cancelEffects = node.getBoolean("cancel_effects", false);
        deactivateEffects = node.getBoolean("deactivate_effects", true);
        disableManaRegeneration = node.getBoolean("disable_mana_regeneration", false);

        String toggleString = node.getString("toggle", "NONE");
        try {
            toggle = ToggleType.valueOf(toggleString.toUpperCase());
        } catch (Exception ex) {
            controller.getLogger().warning("Invalid toggle type: " + toggleString);
        }

        if (node.isList("triggers")) {
            List<?> list = node.getList("triggers");
            if (!list.isEmpty()) {
                Object item = list.get(0);
                if (item instanceof String) {
                    List<String> triggerList = node.getStringList("triggers");
                    triggers = new ArrayList<>();
                    for (String triggerKey : triggerList) {
                        triggers.add(new Trigger(controller, triggerKey));
                    }
                } else {
                    Collection<ConfigurationSection> triggersConfiguration = ConfigurationUtils.getNodeList(node, "triggers");
                    if (triggersConfiguration != null && !triggersConfiguration.isEmpty()) {
                        triggers = new ArrayList<>();
                        for (ConfigurationSection triggerConfiguration : triggersConfiguration) {
                            triggers.add(new Trigger(controller, triggerConfiguration));
                        }
                    }
                }
            }
        } else if (node.isString("triggers")) {
            String[] triggerList = StringUtils.split(node.getString("triggers"), ',');
            triggers = new ArrayList<>();
            for (String triggerKey : triggerList) {
                triggers.add(new Trigger(controller, triggerKey));
            }
        }

        requirements = ConfigurationUtils.getRequirements(node);
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
                        progressLevelEquations.put(key, EquationStore.getInstance().getTransform(value));
                    }
                }
            }
        }

        // Preload some parameters
        parameters.wrap(node.getConfigurationSection("parameters"));
        updateTemplateParameters();

        effects.clear();
        ConfigurationSection effectsNode = node.getConfigurationSection("effects");
        if (effectsNode != null) {
            Collection<String> effectKeys = effectsNode.getKeys(false);
            for (String effectKey : effectKeys) {
                if (effectsNode.isString(effectKey)) {
                    String referenceKey = effectsNode.getString(effectKey);
                    if (effects.containsKey(referenceKey)) {
                        effects.put(effectKey, new ArrayList<>(effects.get(referenceKey)));
                    } else {
                        effects.put(effectKey, controller.getEffects(referenceKey));
                    }
                }
                else
                {
                    effects.put(effectKey, controller.loadEffects(effectsNode, effectKey, "spell " + getKey(), parameters));
                }
            }
        } else if (node.contains("effects")) {
            controller.getLogger().warning("Invalid effects section in spell " + getKey() + ", did you forget to add cast: ?");
        }

        // Boss bar, can be a simple boolean or a config
        bossBarConfiguration = BossBarConfiguration.parse(controller, node, "$spell");
    }

    @Nullable
    private MaterialAndData loadIcon(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        ItemData itemData = controller.getOrCreateItem(key);
        return itemData == null ? null : itemData.getMaterialAndData();
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

        if (!isActive)
        {
            this.currentCast = null;
        }
    }

    @Nullable
    protected Boolean prepareCast() {
        return prepareCast(null, null, null);
    }

    @Nullable
    protected Boolean prepareCast(@Nullable Wand wand, @Nullable ConfigurationSection extraParameters, @Nullable Location defaultLocation)
    {
        if (mage.isPlayer() && mage.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            if (mage.getDebugLevel() > 0 && extraParameters != null) {
                mage.sendDebugMessage("Cannot cast in spectator mode.");
            }
            return false;
        }

        if (toggle != ToggleType.NONE && isActive()) {
            mage.sendDebugMessage(ChatColor.DARK_BLUE + "Deactivating " + ChatColor.GOLD + getName());
            deactivate(true, true);
            processResult(SpellResult.DEACTIVATE, parameters);
            return true;
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
                    extraParameters = ConfigurationUtils.newConfigurationSection();
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

        if (this.currentCast == null) {
            getCurrentCast(wand);
        }

        this.location = defaultLocation;

        workingParameters = new SpellParameters(this, currentCast);
        ConfigurationUtils.addConfigurations(workingParameters, this.parameters);
        ConfigurationUtils.addConfigurations(workingParameters, extraParameters);
        List<CastParameter> overrides = mage.getOverrides(getSpellKey().getBaseKey());
        if (overrides != null) {
            for (CastParameter parameter : overrides) {
                workingParameters.set(parameter.getParameter(), parameter.getConvertedValue());
            }
        }
        currentCast.setWorkingParameters(workingParameters);
        initializeVariables((SpellParameters)workingParameters);
        processParameters(workingParameters);

        // Check to see if this is allowed to be cast by a command block
        if (!commandBlockAllowed) {
            CommandSender sender = mage.getCommandSender();
            if (sender != null && sender instanceof BlockCommandSender) {
                Block block = mage.getLocation().getBlock();
                if (DefaultMaterials.isCommand(block.getType())) {
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
            sendCastDebugMessage(SpellResult.CANCELLED, " (no cast)");
            return false;
        }

        // Don't allow casting if the player is confused or weakened
        bypassConfusion = workingParameters.getBoolean("bypass_confusion", bypassConfusion);
        bypassWeakness = workingParameters.getBoolean("bypass_weakness", bypassWeakness);
        LivingEntity livingEntity = mage.getLivingEntity();
        if (livingEntity != null && !mage.isSuperPowered()) {
            if (!bypassConfusion && livingEntity.hasPotionEffect(PotionEffectType.CONFUSION)) {
                processResult(SpellResult.CURSED, workingParameters);
                sendCastDebugMessage(SpellResult.CURSED, " (no cast)");
                return false;
            }

            // Don't allow casting if the player is weakened
            if (!bypassWeakness && livingEntity.hasPotionEffect(PotionEffectType.WEAKNESS)) {
                processResult(SpellResult.CURSED, workingParameters);
                sendCastDebugMessage(SpellResult.CURSED, " (no cast)");
                return false;
            }
        }

        // Don't perform permission check until after processing parameters, in case of overrides
        if (!canCast(getLocation())) {
            processResult(SpellResult.INSUFFICIENT_PERMISSION, workingParameters);

            if (creativeRestricted && mage.isPlayer() && mage.getPlayer().getGameMode() == GameMode.CREATIVE) {
                String creativeMessage = getMessage("creative_fail");
                mage.sendMessage(creativeMessage);
            } else {
                sendCastDebugMessage(SpellResult.INSUFFICIENT_PERMISSION, " (no cast)");
            }

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
        return null;
    }

    @Override
    public boolean cast(@Nullable String[] extraParameters, @Nullable Location defaultLocation) {
        ConfigurationSection parameters = null;
        if (extraParameters != null && extraParameters.length > 0) {
            parameters = ConfigurationUtils.newConfigurationSection();
            ConfigurationUtils.addParameters(extraParameters, parameters);
        }
        return cast(parameters, defaultLocation);
    }

    @Override
    public boolean cast(@Nullable ConfigurationSection parameters)
    {
        return cast(parameters, null);
    }

    @Override
    public boolean cast(@Nullable Wand wand, @Nullable ConfigurationSection parameters) {
        return cast(wand, parameters, null);
    }

    @Override
    public boolean cast(@Nullable ConfigurationSection extraParameters, @Nullable Location defaultLocation) {
        return cast(null, extraParameters, defaultLocation);
    }

    public boolean cast(@Nullable Wand wand, @Nullable ConfigurationSection extraParameters, @Nullable Location defaultLocation) {
        Boolean prepared = prepareCast(wand, extraParameters, defaultLocation);
        if (prepared != null) {
            return prepared;
        }

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
        maxCharges = workingParameters.getInt("charges", maxCharges);
        rechargeRate = parameters.getDouble("charge_regeneration", rechargeRate);

        // Color override
        color = ConfigurationUtils.getColor(workingParameters, "color", color);
        particle = workingParameters.getString("particle", null);

        long cooldownRemaining = getRemainingCooldown();
        String timeDescription = "";
        if (cooldownRemaining > 0) {
            // TODO: API?
            boolean handled = false;
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                com.elmakers.mine.bukkit.magic.Mage implMage = (com.elmakers.mine.bukkit.magic.Mage)mage;
                handled = implMage.handleCooldown(this);
            }
            if (!handled) {
                timeDescription = controller.getMessages().getTimeDescription(cooldownRemaining, "wait", "cooldown");
                sendMessageKey("cooldown", getMessage("cooldown").replace("$time", timeDescription));
            }
            processResult(SpellResult.COOLDOWN, workingParameters);
            sendCastDebugMessage(SpellResult.COOLDOWN, " (no cast)");
            return false;
        }

        CastingCost required = getRequiredCost();
        if (required != null) {
            // TODO: API?
            boolean handled = false;
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                com.elmakers.mine.bukkit.magic.Mage implMage = (com.elmakers.mine.bukkit.magic.Mage)mage;
                handled = implMage.handleInsufficientResources(this, required);
            }
            if (!handled) {
                String baseMessage = getMessage("insufficient_resources");
                String costDescription = required.getDescription(controller.getMessages(), mage);
                sendMessageKey("insufficient_resources", baseMessage.replace("$cost", costDescription));
            }
            processResult(SpellResult.INSUFFICIENT_RESOURCES, workingParameters);
            sendCastDebugMessage(SpellResult.INSUFFICIENT_RESOURCES, " (no cast)");
            return false;
        }

        if (!isCooldownFree() && !spellData.useCharge(rechargeRate, maxCharges)) {
            // TODO: API?
            boolean handled = false;
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                com.elmakers.mine.bukkit.magic.Mage implMage = (com.elmakers.mine.bukkit.magic.Mage)mage;
                handled = implMage.handleInsufficientCharges(this);
            }
            if (!handled) {
                long timeRemaining = spellData.getTimeToRecharge(rechargeRate, maxCharges);
                timeDescription = controller.getMessages().getTimeDescription(timeRemaining, "wait", "charge");
                sendMessageKey("charge", getMessage("charge").replace("$time", timeDescription));
            }
            processResult(SpellResult.INSUFFICIENT_CHARGES, workingParameters);
            sendCastDebugMessage(SpellResult.INSUFFICIENT_CHARGES, " (no cast)");
            return false;
        }

        if (requiredHealth > 0) {
            LivingEntity li = mage.getLivingEntity();
            if (li != null) {
                double healthPercentage = 100 * li.getHealth() / CompatibilityLib.getCompatibilityUtils().getMaxHealth(li);
                if (healthPercentage < requiredHealth) {
                    processResult(SpellResult.INSUFFICIENT_RESOURCES, workingParameters);
                    sendCastDebugMessage(SpellResult.INSUFFICIENT_RESOURCES, " (no cast)");
                    return false;
                }
            }
        }

        if (controller.isSpellProgressionEnabled()) {
            long progressLevel = getProgressLevel();
            for (Entry<String, EquationTransform> entry : progressLevelEquations.entrySet()) {
                workingParameters.set(entry.getKey(), entry.getValue().get(progressLevel));
            }
        }

        // Check for cancel-on-cast-other spells, after we have determined that this spell can really be cast.
        if (!aura && !passive) {
            for (Iterator<Batch> iterator = mage.getPendingBatches().iterator(); iterator.hasNext();) {
                Batch batch = iterator.next();
                if (!(batch instanceof SpellBatch)) continue;
                SpellBatch spellBatch = (SpellBatch)batch;
                Spell spell = spellBatch.getSpell();
                if (spell.cancelOnCastOther()) {
                    batch.cancel();
                    iterator.remove();
                }
            }

            for (Spell spell : mage.getActiveSpells()) {
                if (spell.cancelOnCastOther()) {
                    spell.cancel();
                }
            }
        }

        return finalizeCast(workingParameters);
    }

    @Override
    public boolean cast() {
        return cast((ConfigurationSection)null, null);
    }

    @Override
    public boolean cast(String[] extraParameters) {
        return cast(extraParameters, null);
    }

    public void initializeVariables(SpellParameters parameters) {
        if (variablesSection != null) {
            VariableScope scope = VariableScope.CAST;
            ConfigurationSection variables = parameters.getVariables(scope);
            if (variables != null) {
                Set<String> keys = variablesSection.getKeys(false);
                for (String variable : keys) {
                    variableScopes.put(variable, scope);
                    if (!variables.contains(variable)) {
                        variables.set(variable, variablesSection.get(variable));
                    }
                }
            }
        } else {
            if (variablesList != null && !variablesList.isEmpty()) {
                for (ConfigurationSection variableConfig : variablesList) {
                    VariableScope scope = ConfigurationUtils.parseScope(variableConfig.getString("scope"), VariableScope.CAST, controller.getLogger());
                    String variable = variableConfig.getString("variable");
                    variableScopes.put(variable, scope);
                    ConfigurationSection variables = parameters.getVariables(scope);
                    if (variables == null) continue;

                    if (!variables.contains(variable)) {
                        variables.set(variable, variableConfig.get("value", variableConfig.get("default")));
                    }
                }
            }
        }
    }

    @Override
    @Nullable
    public VariableScope getVariableScope(String variable) {
        return variableScopes.get(variable);
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
        if (!canContinue(location)) return false;
        if (activeSpellsExclusive != null || activeSpellsRestricted != null) {
            Collection<Batch> batches = mage.getPendingBatches();
            boolean hasExclusive = activeSpellsExclusive == null;
            for (Batch batch : batches) {
                if (batch instanceof SpellBatch) {
                    SpellBatch spellBatch = (SpellBatch)batch;
                    Spell spell = spellBatch.getSpell();
                    if (spell != null && activeSpellsRestricted != null && activeSpellsRestricted.contains(spell.getSpellKey().getBaseKey())) {
                        return false;
                    }
                    if (spell != null && activeSpellsExclusive != null && activeSpellsExclusive.contains(spell.getSpellKey().getBaseKey())) {
                        hasExclusive = true;
                        if (activeSpellsRestricted == null) {
                            break;
                        }
                    }
                }
            }
            if (!hasExclusive) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canContinue(Location location) {
        if (bypassAll) return true;
        if (!hasCastPermission(mage.getCommandSender())) return false;
        Entity entity = mage.getEntity();
        if (disguiseRestricted && entity != null && entity instanceof Player && controller.isDisguised(entity)) return false;
        if (glideRestricted && entity != null && entity instanceof LivingEntity && ((LivingEntity)entity).isGliding()) return false;
        if (glideExclusive && entity != null && entity instanceof LivingEntity && !((LivingEntity)entity).isGliding()) return false;
        if (creativeRestricted && entity != null && entity instanceof Player && ((Player)entity).getGameMode() == GameMode.CREATIVE) return false;

        if (location == null) return true;
        Boolean regionPermission = bypassRegionPermission ? null : controller.getRegionCastPermission(mage.getPlayer(), this, location);
        if (regionPermission != null && !ignoreRegionOverrides && regionPermission == true) return true;
        Boolean personalPermission = bypassRegionPermission ? null : controller.getPersonalCastPermission(mage.getPlayer(), this, location);
        if (personalPermission != null && !ignoreRegionOverrides && personalPermission == true) return true;
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
        if (denyBreakPermission) return false;
        // Cast permissions bypass
        if (bypassBuildRestriction || bypassRegionPermission || bypassAll) return true;
        if (!ignoreRegionOverrides) {
            Boolean castPermission = controller.getRegionCastPermission(mage.getPlayer(), this, block.getLocation());
            if (castPermission != null && castPermission == true) return true;
            if (castPermission != null && castPermission == false) return false;
        }
        return mage.hasBreakPermission(block);
    }

    public boolean hasBuildPermission(Location location) {
        if (location == null) return true;
        return hasBuildPermission(location.getBlock());
    }

    public boolean hasBuildPermission(Block block) {
        if (denyBuildPermission) return false;
        // Cast permissions bypass
        if (bypassBuildRestriction || bypassRegionPermission || bypassAll) return true;
        if (!ignoreRegionOverrides) {
            Boolean castPermission = controller.getRegionCastPermission(mage.getPlayer(), this, block.getLocation());
            if (castPermission != null && castPermission == true) return true;
            if (castPermission != null && castPermission == false) return false;
        }
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

        boolean success = result.isSuccess(castOnNoTarget);
        boolean free = result.isFree(castOnNoTarget);
        if (!free) {
            if (costs != null && !mage.isCostFree()) {
                for (CastingCost cost : costs) {
                    cost.use(this);
                }
            }
            if (toggle == ToggleType.NONE) {
                updateCooldown();
            }
        }

        // Record cast time, may be redudant if updateCooldown was called above but that's ok
        spellData.setLastCast(System.currentTimeMillis());

        // Need some way to clear selected target here
        if (success && toggle != ToggleType.NONE) {
            activate();
        }

        StartCastEvent castEvent = new StartCastEvent(mage, this, result, success);
        Bukkit.getPluginManager().callEvent(castEvent);

        sendCastDebugMessage(result, " (" + success + ")");
        onFinalizeCast(result);
        return success;
    }

    protected void onFinalizeCast(SpellResult result) {

    }

    protected void updateCooldown() {
        Wand wand = currentCast != null ? currentCast.getWand() : null;
        boolean isCooldownFree = wand != null ? wand.isCooldownFree() : mage.isCooldownFree();
        double cooldownReduction = wand != null ? wand.getCooldownReduction() : mage.getCooldownReduction();
        cooldownReduction += this.cooldownReduction;
        spellData.setLastCast(System.currentTimeMillis());
        if (!isCooldownFree && !bypassCooldown && cooldown > 0 && cooldownReduction < 1) {
            int reducedCooldown = (int)Math.ceil((1.0f - cooldownReduction) * cooldown);
            spellData.setCooldownExpiration(Math.max(spellData.getCooldownExpiration(), System.currentTimeMillis() + reducedCooldown));
        }
        if (!isCooldownFree && mageCooldown > 0 && cooldownReduction < 1 && !bypassMageCooldown) {
            int reducedCooldown = (int)Math.ceil((1.0f - cooldownReduction) * mageCooldown);
            mage.setRemainingCooldown(reducedCooldown);
        }
    }

    protected void sendCastDebugMessage(SpellResult result, String message)
    {
        Location source = getEyeLocation();
        if (mage == null || source == null) return;

        mage.sendDebugMessage(ChatColor.WHITE + "Cast " + ChatColor.GOLD + getName() + ChatColor.WHITE + " from "
                + ChatColor.GRAY + source.getBlockX()
                + ChatColor.DARK_GRAY + ","  + ChatColor.GRAY + source.getBlockY()
                + ChatColor.DARK_GRAY + "," + ChatColor.GRAY + source.getBlockZ()
                + ChatColor.WHITE  + ": " + ChatColor.AQUA + result.name().toLowerCase()
                + ChatColor.DARK_AQUA + message);
    }

    @Nonnull
    protected String getMessageRaw(String messageKey, String def) {
        String message = controller.getMessages().get(messageKey, def);
        message = controller.getMessages().get("spells.default." + messageKey, message);
        if (inheritKey != null && !inheritKey.isEmpty()) {
            message = controller.getMessages().get("spells." + inheritKey + "." + messageKey, message);
        }
        message = controller.getMessages().get("spells." + spellKey.getBaseKey() + "." + messageKey, message);
        if (spellKey.isVariant()) {
            message = controller.getMessages().get("spells." + spellKey.getKey() + "." + messageKey, message);
        }
        return message;
    }

    @Override
    public String getMessage(String messageKey) {
        return getMessage(messageKey, "");
    }

    public String getMessage(String messageKey, String def) {
        String embedded = messages == null ? null : messages.getString(messageKey);
        if (embedded != null) {
            return CompatibilityLib.getCompatibilityUtils().translateColors(embedded);
        }
        String message = getMessageRaw(messageKey, def);
        if (currentCast != null && currentCast.getAlternateResult().isAlternate()) {
            message = getMessageRaw(currentCast.getAlternateResult().toString().toLowerCase() + "_" + messageKey, message);
        }
        if (!message.isEmpty()) {
            // Escape some common parameters
            String playerName = mage.getName();
            message = message.replace("$player", playerName);
            message = message.replace("$caster", playerName);

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
                if (!currentCast.getTargetedEntities().isEmpty()) {
                    message = getMessage("cast_target", message);
                }
                LivingEntity sourceEntity = mage.getLivingEntity();
                Entity targetEntity = getTargetEntity();
                if (targetEntity == sourceEntity) {
                    message = getMessage("cast_self", message);
                } else if (targetEntity instanceof Player) {
                    message = getMessage("cast_player", message);
                } else if (targetEntity instanceof LivingEntity) {
                    message = getMessage("cast_livingentity", message);
                } else if (targetEntity != null) {
                    message = getMessage("cast_entity", message);
                }
                castMessageKey(resultName, message);
            } else
            // Special cases where messaging is handled elsewhere
            if (result != SpellResult.INSUFFICIENT_RESOURCES && result != SpellResult.COOLDOWN && result != SpellResult.INSUFFICIENT_CHARGES)
            {
                String message = null;
                if (result.isFailure() && result != SpellResult.FAIL) {
                    message = getMessage("fail");
                }

                if (result.isFailure()) {
                    sendMessageKey(resultName, getMessage(resultName, message));
                } else {
                    castMessageKey(resultName, getMessage(resultName, message));
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

    public void validateEffects() {
        for (Collection<EffectPlayer> players : effects.values()) {
            for (EffectPlayer player : players) {
                player.validate();
            }
        }
    }

    @Override
    public void target() {

    }

    @Nullable
    @Override
    public Location getTargetLocation() {
        return null;
    }

    @Override
    public boolean canTarget(Entity entity) {
        if (bypassAll) {
            return true;
        }
        if (!bypassPvpRestriction && entity instanceof Player)
        {
            Player magePlayer = mage.getPlayer();
            boolean checkPvP = selfTargetingRequiresPvP ? true : magePlayer != entity;
            if (magePlayer != null && !magePlayer.hasPermission("magic.bypass_pvp") && checkPvP)
            {
                // Check that the other player does not have PVP disabled for fairness
                if (!controller.isPVPAllowed((Player)entity, entity.getLocation())) {
                    mage.sendDebugMessage("PVP not allowed for target at target location", 30);
                    return false;
                }
                if (!controller.isPVPAllowed(magePlayer, entity.getLocation())) {
                    mage.sendDebugMessage("PVP not allowed for caster at target location", 30);
                    return false;
                }
                if (!controller.isPVPAllowed(magePlayer, mage.getLocation())) {
                    mage.sendDebugMessage("PVP not allowed for caster at caster location", 30);
                    return false;
                }
            }
        }
        if (onlyFriendlyFire && (friendlyEntityTypes == null || !friendlyEntityTypes.contains(entity.getType())))
        {
            boolean isFriendly = controller.isFriendly(mage.getEntity(), entity);
            if (!isFriendly) {
                mage.sendDebugMessage("Target is not friendly and spell is set to only friendly", 30);
            }
            return isFriendly;
        }
        if (!bypassProtection && !bypassFriendlyFire)
        {
            boolean canTarget = controller.canTarget(mage.getEntity(), entity);
            if (!canTarget) {
                mage.sendDebugMessage("Controller says can't target entity", 30);
            }
            return canTarget;
        }

        return true;
    }

    @Nullable
    @Override
    public Entity getTargetEntity() {
        return null;
    }

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getEffectMaterial()
    {
        return new com.elmakers.mine.bukkit.block.MaterialAndData(DEFAULT_EFFECT_MATERIAL);
    }

    @Override
    public void updateTemplateParameters() {
        processTemplateParameters(parameters);
    }

    public void processTemplateParameters(ConfigurationSection parameters) {
        bypassMageCooldown = parameters.getBoolean("bypass_mage_cooldown", false);
        bypassCooldown = parameters.getBoolean("bypass_cooldown", false);
        warmup = parameters.getInt("warmup", 0);
        cooldown = parameters.getInt("cooldown", 0);
        cooldown = parameters.getInt("cool", cooldown);
        mageCooldown = parameters.getInt("cooldown_mage", 0);
        maxCharges = parameters.getInt("charges", 0);
        rechargeRate = parameters.getDouble("charge_regeneration", 1);
        displayCooldown = parameters.getInt("display_cooldown", -1);
        bypassPvpRestriction = parameters.getBoolean("bypass_pvp", false);
        bypassPvpRestriction = parameters.getBoolean("bp", bypassPvpRestriction);
        bypassPermissions = parameters.getBoolean("bypass_permissions", false);
        bypassBuildRestriction = parameters.getBoolean("bypass_build", false);
        bypassBuildRestriction = parameters.getBoolean("bb", bypassBuildRestriction);
        ignoreRegionOverrides = parameters.getBoolean("ignore_region_overrides", false);
        bypassBreakRestriction = parameters.getBoolean("bypass_break", false);
        denyBuildPermission = parameters.getBoolean("deny_build", false);
        denyBreakPermission = parameters.getBoolean("deny_break", false);
        bypassProtection = parameters.getBoolean("bypass_protection", false);
        bypassProtection = parameters.getBoolean("bp", bypassProtection);
        bypassAll = parameters.getBoolean("bypass", false);
        duration = parameters.getInt("duration", 0);
        totalDuration = parameters.getInt("total_duration", -1);

        costReduction = (float)parameters.getDouble("cost_reduction", 0);
        consumeReduction = (float)parameters.getDouble("consume_reduction", 0);
        cooldownReduction = (float)parameters.getDouble("cooldown_reduction", 0);
        if (parameters.getBoolean("free", false)) {
            costReduction = 2;
            consumeReduction = 2;
        }
    }

    public void processParameters(ConfigurationSection parameters) {
        processTemplateParameters(parameters);
        fizzleChance = (float)parameters.getDouble("fizzle_chance", 0);
        backfireChance = (float)parameters.getDouble("backfire_chance", 0);

        Location defaultLocation = location == null ? mage.getLocation() : location;
        Location locationOverride = ConfigurationUtils.overrideLocation(controller, parameters, "p", defaultLocation, controller.canCreateWorlds());
        if (locationOverride != null) {
            location = locationOverride;
        }
        if (parameters.getBoolean("cancel_on_damage")) {
            cancelOnDamage = 0.00001;
        } else {
            cancelOnDamage = parameters.getDouble("cancel_on_damage", 0);
        }
        cancelOnWorldChange = parameters.getBoolean("cancel_on_world_change", false);
        cancelOnCastOther = parameters.getBoolean("cancel_on_cast_other", false);
        cancelOnDeath = parameters.getBoolean("cancel_on_death", false);
        cancelOnDeactivate = parameters.getBoolean("cancel_on_deactivate", true);
        cancelOnNoPermission = parameters.getBoolean("cancel_on_no_permission", false);
        cancelOnNoWand = parameters.getBoolean("cancel_on_no_wand", false);
        commandBlockAllowed = parameters.getBoolean("command_block_allowed", true);
        passive = parameters.getBoolean("passive", passive);
        aura = parameters.getBoolean("aura", aura);

        MaterialSetManager materials = controller.getMaterialSetManager();
        preventPassThroughMaterials = materials.getMaterialSetEmpty("indestructible");
        preventPassThroughMaterials = materials.fromConfig(
                parameters.getString("prevent_passthrough"),
                preventPassThroughMaterials);

        passthroughMaterials = materials.getMaterialSetEmpty("passthrough");
        passthroughMaterials = materials.fromConfig(
                parameters.getString("passthrough"),
                passthroughMaterials);

        unsafeMaterials = materials.getMaterialSetEmpty("unsafe");
        unsafeMaterials = materials.fromConfig(
                    parameters.getString("unsafe"),
                    unsafeMaterials);

        bypassDeactivate = parameters.getBoolean("bypass_deactivate", false);
        quiet = parameters.getBoolean("quiet", false);
        loud = parameters.getBoolean("loud", false);
        selfTargetingRequiresPvP = parameters.getBoolean("target_self_requires_pvp", false);
        targetSelf = parameters.getBoolean("target_self", false);
        messageTargets = parameters.getBoolean("message_targets", true);
        verticalSearchDistance = parameters.getInt("vertical_range", 8);
        String nameOverride = parameters.getString("name", "");
        if (!nameOverride.isEmpty()) {
            name = CompatibilityLib.getCompatibilityUtils().translateColors(nameOverride).replace('_', ' ');
        }

        friendlyEntityTypes = null;
        if (parameters.contains("friendly_types")) {
            friendlyEntityTypes = new HashSet<>();
            Collection<String> typeKeys = ConfigurationUtils.getStringList(parameters, "friendly_types");
            for (String typeKey : typeKeys) {
                try {
                    EntityType entityType = EntityType.valueOf(typeKey.toUpperCase());
                    friendlyEntityTypes.add(entityType);
                } catch (Throwable ex) {
                    controller.getLogger().warning("Unknown entity type in friendly_types of " + getKey() + ": " + typeKey);
                }
            }
        }
    }

    @Override
    public void reloadParameters(com.elmakers.mine.bukkit.api.action.CastContext context) {
        processParameters(context.getWorkingParameters());
    }

    @Override
    public boolean brushIsErase() {
        return false;
    }

    @Override
    public String getPermissionNode()
    {
        return "magic.cast." + spellKey.getBaseKey();
    }

    @Override
    @Nullable
    public String getCategoryPermissionNode() {
        if (category == null) {
            return null;
        }
        return "magic.allowed_spell_categories." + category.getKey();
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

    //
    // Cloneable implementation
    //

    @Nullable
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
        CostReducer reducer = mageClass != null ? mageClass : (currentCast != null ? currentCast.getWand() : mage);
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
        CostReducer reducer = reducerWand == null ? reducerMage : reducerWand;
        if (reducer == null) {
            reducer = mageClass != null ? mageClass : (currentCast != null ? currentCast.getWand() : mage);
        }
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

    @Nullable
    @Override
    @Deprecated
    public com.elmakers.mine.bukkit.api.spell.Spell createSpell()
    {
        return createMageSpell(null);
    }

    @Nullable
    @Override
    public com.elmakers.mine.bukkit.api.spell.MageSpell createMageSpell(Mage mage)
    {
        BaseSpell spell = null;
        try {
            spell = this.getClass().getDeclaredConstructor().newInstance();
            spell.setMage(mage);
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
    public final String getKey()
    {
        return spellKey.getKey();
    }

    @Override
    public final String getName()
    {
        return name == null ? getKey() : name;
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
        return iconMaterial;
    }

    @Override
    public final com.elmakers.mine.bukkit.api.block.MaterialAndData getDisabledIcon()
    {
        return disabledIconMaterial;
    }

    @Override
    public boolean hasIcon() {
        return iconMaterial != null && iconMaterial.getMaterial() != Material.AIR;
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
        return cost == null ? 0.0 : cost.getAmount();
    }

    @Override
    @Nullable
    public Cost getCost() {
        return cost == null ? null : new Cost(cost);
    }

    @Override
    public final double getEarns()
    {
        return earns == null ? 0 : earns.getAmount();
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

    public boolean hasEffects(String key) {
        Collection<EffectPlayer> effectList = effects.get(key);
        return effectList != null && effectList.size() > 0;
    }

    @Override
    public Collection<EffectPlayer> getEffects(SpellResult result) {
        return getEffects(result.name().toLowerCase());
    }

    @Override
    public Collection<EffectPlayer> getEffects(String key) {
        Collection<EffectPlayer> effectList = effects.get(key);
        if (effectList == null) {
            return controller.getEffects(key);
        }
        return new ArrayList<>(effectList);
    }

    @Nullable
    @Override
    public Collection<CastingCost> getCosts() {
        if (costs == null) return null;
        List<CastingCost> copy = new ArrayList<>();
        copy.addAll(costs);
        return copy;
    }

    @Nullable
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
            examples.addAll(controller.getMaterialSetManager().getMaterialSets());
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

    @Nullable
    @Override
    public String getMageCooldownDescription() {
        return getCooldownDescription(controller.getMessages(), mageCooldown, mage, null);
    }

    @Nullable
    public String getMageCooldownDescription(Mage mage, com.elmakers.mine.bukkit.api.wand.Wand wand) {
        return getCooldownDescription(controller.getMessages(), mageCooldown, mage, wand);
    }

    @Nullable
    @Override
    public String getCooldownDescription() {
        return getCooldownDescription(
                controller.getMessages(), getDisplayCooldown(),  mage, null);
    }

    @Nullable
    public String getCooldownDescription(Mage mage, com.elmakers.mine.bukkit.api.wand.Wand wand) {
        return getCooldownDescription(
                controller.getMessages(), getDisplayCooldown(), mage, wand);
    }

    @Nullable
    protected String getCooldownDescription(Messages messages, int cooldown, Mage mage, com.elmakers.mine.bukkit.api.wand.Wand wand) {
        CooldownReducer reducer = mageClass != null ? mageClass : (wand != null ? wand : mage);
        if (reducer != null) {
            if (reducer.isCooldownFree() || bypassCooldown) {
                cooldown = 0;
            }
            double cooldownReduction = reducer.getCooldownReduction();
            cooldownReduction += this.cooldownReduction;
            if (cooldown > 0 && cooldownReduction < 1) {
                cooldown = (int)Math.ceil((1.0f - cooldownReduction) * cooldown);
            }
        }

        return getTimeDescription(messages, cooldown);
    }

    @Nullable
    public String getWarmupDescription() {
        return getTimeDescription(controller.getMessages(), warmup);
    }

    @Override
    public int getMaxCharges() {
        return maxCharges;
    }

    @Override
    public double getChargeRegeneration() {
        return rechargeRate;
    }

    @Override
    public double getChargesRemaining() {
        return spellData.getCharges(rechargeRate, maxCharges);
    }

    /**
     * @return The cooldown to show in UI. Spells can manually set their
     *         "display_cooldown" if they apply cooldown via an action.
     */
    private int getDisplayCooldown() {
        return displayCooldown != -1 ? displayCooldown : cooldown;
    }

    @Nullable
    private String getTimeDescription(Messages messages, int time) {
        if (time > 0) {
            return controller.getMessages().getTimeDescription(time, "description", "cooldown");
        }
        return null;
    }

    @Override
    public long getCooldown()
    {
        return cooldown;
    }

    @Override
    public boolean isCostFree() {
        return (mage != null && mage.isCostFree()) || (mageClass != null && mageClass.isCostFree());
    }

    @Override
    public boolean isConsumeFree() {
        return (mage != null && mage.isConsumeFree()) || (mageClass != null && mageClass.isConsumeFree());
    }

    @Nullable
    @Override
    public CastingCost getRequiredCost() {
        if (!isCostFree())
        {
            CasterProperties caster = mageClass != null ? mageClass : getCurrentCast().getWand();
            if (costs != null && !isActive)
            {
                for (CastingCost cost : costs)
                {
                    if (!cost.has(mage, caster, this))
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
    public void reduceRemainingCooldown(long ms) {
        spellData.setCooldownExpiration(Math.max(0, spellData.getCooldownExpiration() - ms));
    }

    public boolean isCooldownFree() {
        if (mage.isCooldownFree()) return true;
        if (mageClass != null && mageClass.isCooldownFree()) return true;
        if (mageClass == null) {
            // TODO: Why don't we check the wand if there is a class?
            Wand wand = mage.getActiveWand();
            if (wand != null && wand.isCooldownFree()) return true;
        }
        return false;
    }

    @Override
    public long getRemainingCooldown() {
        long remaining = 0;
        if (isCooldownFree()) return 0;
        if (spellData.getCooldownExpiration() > 0 && !bypassCooldown)
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
            sendMessageKey("cancel_selection");
            cancel(true);
        }
        return cancelled;
    }

    @Override
    public boolean cancel() {
        return cancel(false);
    }

    private boolean cancel(boolean quiet)
    {
        if (isActive() && !cancelled) {
            // Avoid double-cancels because deactivate() will also chain to cancel()
            cancelled = true;
            if (!quiet) {
                sendMessageKey("cancel");
            }
            deactivate(false, false, true, true);
            // this is not a persistent flag, it's really just here to prevent double-cancels
            cancelled = false;
        }
        if (currentCast != null) {
            currentCast.cancelEffects();
        }
        return true;
    }

    @Override
    public boolean stop() {
        return cancel(true);
    }

    @Override
    public void setActive(boolean active) {
        if (active && !isActive) {
            onActivate();
        } else if (!active && isActive) {
            onDeactivate();
        }
        isActive = active;
        lastActiveCost = System.currentTimeMillis();
    }

    @Override
    public void activate() {
        if (!isActive) {
            mage.activateSpell(this);
        }
        if (currentCast != null) {
            toggleUndo = currentCast.getUndoList();
        }
    }

    @Override
    public boolean deactivate() {
        return deactivate(false, false);
    }

    @Override
    public boolean deactivate(boolean force, boolean quiet) {
        return deactivate(force, quiet, true);
    }

    public boolean deactivate(boolean force, boolean quiet, boolean effects) {
        return deactivate(force, quiet, effects, false);
    }

    public boolean deactivate(boolean force, boolean quiet, boolean effects, boolean cancelled) {
        if (!force && bypassDeactivate) {
            return false;
        }
        if (currentCast == null || !currentCast.getResult().isFree()) {
            updateCooldown();
        }
        if (isActive || cancelled) {
            isActive = false;
            onDeactivate();

            mage.deactivateSpell(this);
            if (!quiet) {
                sendMessageKey("deactivate");
            }
            if (currentCast != null) {
                // this is skipped here to avoid default deactivation messaging,
                // and in particular so it doesn't override any results published by Construct or Fill batches,
                // since those spells activate and deactivate for selection.
                if (!quiet) {
                    currentCast.addResult(SpellResult.DEACTIVATE);
                }
                if (deactivateEffects) {
                    currentCast.cancelEffects();
                }
                if ((toggle == ToggleType.UNDO || toggle == ToggleType.UNDO_IF_ACTIVE) && toggleUndo != null && !toggleUndo.isUndone() && isActive()) {
                    toggleUndo.undo();
                    removeBossBar();
                }
                toggleUndo = null;
            }
            if (toggle != ToggleType.NONE) {
                mage.cancelPending(getSpellKey().getBaseKey(), true);
            }
            if (effects) {
                playEffects("deactivate");
            }
        }

        return true;
    }

    @Override
    public Mage getMage() {
        return mage;
    }

    @Override
    public SpellData getSpellData() {
        return spellData;
    }

    @Override
    public void setSpellData(SpellData data) {
        this.spellData = data;
        if (this.parameters != null) {
            this.parameters.setSpellVariables(getVariables());
            if (mage != null) {
                this.parameters.setMageVariables(mage.getVariables());
            }
        }
    }

    @Override
    public void tick()
    {
        checkBossBar();
        checkActiveDuration();
        checkActiveCosts();
    }

    private void checkBossBar() {
        Player player = mage.getPlayer();
        if (player != null && bossBarConfiguration != null && currentCast != null) {
            if (bossBar == null) {
                bossBar = bossBarConfiguration.createBossBar(currentCast);
                bossBar.addPlayer(player);
                bossBarMana = mage.getMana();
            }
            double durationRemaining = 1;
            if (duration > 0) {
                durationRemaining = 1.0 - ((double)System.currentTimeMillis() - spellData.getLastCast()) / duration;
            }
            double costsRemaining = 1;
            if (activeCosts != null) {
                int manaRequired = 0;
                if (!mage.isCostFree()) {
                    for (CastingCost cost : activeCosts) {
                        manaRequired = cost.getMana(this);
                        if (manaRequired > 0) break;
                    }
                }

                if (manaRequired > 0) {
                    double manaMax =  mage.getEffectiveManaMax();
                    double manaRegeneration = 0;
                    if (!disableManaRegeneration) {
                        manaRegeneration = mage.getEffectiveManaRegeneration();
                    }
                    if (manaMax > manaRequired && manaRequired > manaRegeneration) {
                        double manaDrain = manaRequired - manaRegeneration;
                        double possibleDuration = bossBarMana / manaDrain;
                        if (possibleDuration > 0) {
                            double remainingMana = mage.getMana();
                            double remainingDuration = remainingMana / manaDrain;
                            costsRemaining = remainingDuration / possibleDuration;
                        }
                    }
                }
            }

            bossBar.setProgress(Math.min(1,Math.max(0,Math.min(durationRemaining, costsRemaining))));
        }
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

    @Nullable
    @Override
    public Color getColor() {
        if (color != null) return color;
        if (category != null) return category.getColor();
        return null;
    }

    @Override
    public boolean isHidden()
    {
        return hidden;
    }

    /**
     * Called when this spell is cast.
     *
     * <p>This is where you do your work!
     *
     * <p>If parameters were passed to this spell, either via a variant or the command line,
     * they will be passed in here.
     *
     * @param parameters Any parameters that were passed to this spell
     * @return The SpellResult of this cast.
     */
    public SpellResult onCast(ConfigurationSection parameters) {
        throw new UnsupportedOperationException("The onCast method has not been implemented");
    }

    @Override
    public MageController getController() {
        return controller;
    }

    @Override
    public String getGlyph() {
        return icon.getGlyph();
    }

    @Override
    public String getIconURL() {
        return icon.getUrl();
    }

    @Override
    public String getDisabledIconURL() {
        return icon.getUrlDisabled();
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

    @Nullable
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
        return getCurrentCast(null);
    }

    public CastContext getCurrentCast(@Nullable Wand wand) {
        if (currentCast == null) {
            if (wand != null) {
                currentCast = new CastContext(this, wand);
            } else {
                currentCast = new CastContext(this);
            }
            currentCast.initialize();
        }
        return currentCast;
    }

    @Override
    public Entity getEntity() {
        return mage.getEntity();
    }

    @Nullable
    @Override
    public String getEffectParticle() {
        if (particle == null) {
            return mage.getEffectParticleName();
        }
        return particle;
    }

    @Nullable
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
        if (levelDescription != null && levelDescription.length() > 0) {
            String descriptionTemplate = messages.get("spell.level_lore", "");
            if (!descriptionTemplate.isEmpty()) {
                CompatibilityLib.getInventoryUtils().wrapText(descriptionTemplate.replace("$level", levelDescription), lore);
            }
        }
        if (description != null && description.length() > 0) {
            String descriptionTemplate = messages.get("spell.description_lore", "");
            if (!descriptionTemplate.isEmpty()) {
                CompatibilityLib.getInventoryUtils().wrapText(descriptionTemplate.replace("$description", description), lore);
            }
        }
        if (creatorName != null && !creatorName.isEmpty()) {
            String creatorTemplate = messages.get("spell.creator", "");
            if (!creatorTemplate.isEmpty()) {
                CompatibilityLib.getInventoryUtils().wrapText(creatorTemplate.replace("$name", creatorName), lore);
            }
        }
        if (usage != null && usage.length() > 0) {
            CompatibilityLib.getInventoryUtils().wrapText(usage, lore);
        }
        if (category != null) {
            String categoryLore = messages.get("spell.category", "");
            String categoryName = category.getName();
            if (!categoryLore.isEmpty() && !categoryName.isEmpty()) {
                lore.add(categoryLore.replace("$category", categoryName));
            }
        }
        if (passive) {
            String passiveText = messages.get("spell.passive", "");
            if (!passiveText.isEmpty()) {
                lore.add(passiveText);
            }
        }
        if (!isEnabled()) {
            String disabledText = messages.get("spell.disabled", "");
            if (!disabledText.isEmpty()) {
                lore.add(disabledText);
            }
        }
        if (quickCast && wand != null && !wand.isQuickCastDisabled() && wand.hasInventory()) {
            String quickCastText = messages.get("spell.quick_cast", "");
            if (!quickCastText.isEmpty()) {
                lore.add(quickCastText);
            }
        }
        String warmupDescription = getWarmupDescription();
        String description = messages.get("warmup.description");
        if (warmupDescription != null && !warmupDescription.isEmpty() && !description.isEmpty()) {
            lore.add(description.replace("$time", warmupDescription));
        }
        if (maxCharges > 0) {
            double displayRegeneration = rechargeRate;
            if (rechargeRate == 0) {
                description = messages.get("charges.description");
            } else if (rechargeRate >= 1) {
                description = messages.get("charges.regeneration_description");
            } else {
                description = messages.get("charges.recharge_description");
                displayRegeneration = 1.0 / rechargeRate;
            }
            if (!description.isEmpty()) {
                lore.add(description
                        .replace("$count", Integer.toString(maxCharges))
                        .replace("$rate", Integer.toString((int) (Math.ceil(displayRegeneration))))
                );
            }
        }
        description = messages.get("cooldown.description");
        String cooldownDescription = getCooldownDescription(mage, wand);
        if (cooldownDescription != null && !cooldownDescription.isEmpty() && !description.isEmpty()) {
            lore.add(description.replace("$time", cooldownDescription));
        }
        description = messages.get("cooldown.mage_description");
        String mageCooldownDescription = getMageCooldownDescription(mage, wand);
        if (mageCooldownDescription != null && !mageCooldownDescription.isEmpty() && !description.isEmpty()) {
            lore.add(description.replace("$time", mageCooldownDescription));
        }

        double range = getRange();
        if (range > 0) {
            String message = messages.getRangeDescription(range, "wand.range_description");
            if (!message.isEmpty()) {
                lore.add(ChatColor.GRAY + message);
            }
        }

        String effectiveDuration = this.getDurationDescription(messages);
        if (effectiveDuration != null) {
            lore.add(ChatColor.GRAY + effectiveDuration);
        } else if (showUndoable()) {
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
        reducerMage = mage;
        reducerWand = wand;
        description = messages.get("wand.costs_description");
        if (costs != null && !description.isEmpty()) {
            for (CastingCost cost : costs) {
                if (!cost.isEmpty(this)) {
                    lore.add(ChatColor.YELLOW + description.replace("$description", cost.getFullDescription(messages, this)));
                }
            }
        }
        description = messages.get("wand.active_costs_description");
        if (activeCosts != null && !description.isEmpty()) {
            for (CastingCost cost : activeCosts) {
                if (!cost.isEmpty(this)) {
                    lore.add(ChatColor.YELLOW + description.replace("$description", cost.getFullDescription(messages, this)));
                }
            }
        }
        reducerMage = null;
        reducerWand =  null;
        if (toggle != ToggleType.NONE) {
            String toggleText = messages.get("spell.toggle", "");
            if (!toggleText.isEmpty()) {
                lore.add(toggleText);
            }
        }

        if (earns != null && controller.isSPEnabled() && controller.isSPEarnEnabled()) {
            int scaledEarn = earns.getRoundedAmount();
            if (mage != null) {
                scaledEarn = (int)Math.floor(mage.getEarnMultiplier("sp") * scaledEarn);
            }
            if (scaledEarn > 0) {
                Cost scaled = new Cost(earns);
                scaled.setAmount(scaledEarn);
                String earnsText = messages.get("spell.earns").replace("$earns", scaled.getFullDescription(controller.getMessages()));
                if (!earnsText.isEmpty()) {
                    lore.add(earnsText);
                }
            }
        }
        if (controller.isSpellProgressionEnabled() && progressDescription != null
                && progressDescription.length() > 0 && maxLevels > 0 && template != null) {
            CompatibilityLib.getInventoryUtils().wrapText(progressDescription
                    .replace("$level", Long.toString(Math.max(0, getProgressLevel())))
                    .replace("$max_level", Long.toString(maxLevels)),
                    lore);
        }
    }

    @Nullable
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

    private void removeBossBar() {
        if (bossBar != null) {
            bossBar.setVisible(false);
            bossBar.removeAll();
            bossBar = null;
        }
    }

    @Override
    public void finish(com.elmakers.mine.bukkit.api.action.CastContext context) {
        // Notify other plugins of this spell cast
        CastEvent castEvent = new CastEvent(context);
        Bukkit.getPluginManager().callEvent(castEvent);

        // Message targets
        SpellResult result = context.getResult();
        if (result.isSuccess() && (loud || (!mage.isQuiet() && !quiet))) {
            messageTargets("cast_player_message");
        }

        // Clear cooldown on miss
        if (result.shouldRefundCooldown(castOnNoTarget)) {
            clearCooldown();
        }

        // Refund costs if appropriate
        if (result.shouldRefundCosts(castOnNoTarget, refundOnNoTarget) && costs != null && !mage.isCostFree()) {
            for (CastingCost cost : costs)
            {
                cost.refund(this);
            }
        }

        removeBossBar();

        if (cancelEffects) {
            context.cancelEffects();
        }

        // Track cast counts
        if (result.isSuccess()) {
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
            CasterProperties activeProperties = mage.getActiveProperties();
            if (wand != null) {
                activeProperties = wand;
            }
            ProgressionPath path = activeProperties.getPath();
            if (earns != null && path != null && path.earnsSP() && controller.isSPEnabled() && controller.isSPEarnEnabled()) {
                long now = System.currentTimeMillis();
                int scaledEarn = earns.getRoundedAmount();
                boolean scaled = false;
                if (spellData.getLastEarn() > 0 && earnCooldown > 0 && now < spellData.getLastEarn() + earnCooldown) {
                    scaledEarn = (int)Math.floor((double)scaledEarn * (now - spellData.getLastEarn()) / earnCooldown);
                    scaled = true;
                }
                if (scaledEarn > 0) {
                    Cost earnCost = new Cost(earns);
                    earnCost.setAmount(Math.floor(mage.getEarnMultiplier("sp") * scaledEarn));

                    EarnEvent event = new EarnEvent(mage, earns.getType(), earnCost.getAmount(), EarnEvent.EarnCause.SPELL_CAST);
                    Bukkit.getPluginManager().callEvent(event);

                    if (!event.isCancelled() && earnCost.give(mage, activeProperties)) {
                        if (scaled) {
                            context.playEffects("earn_scaled_sp");
                        } else {
                            context.playEffects("earn_sp");
                        }
                        spellData.setLastEarn(now);
                    }
                }
            }

            // Check for level up
            if (activeProperties.upgradesAllowed() && activeProperties.getSpellLevel(spellKey.getBaseKey()) == spellKey.getLevel())
            {
                if (controller.isSpellUpgradingEnabled()) {
                    SpellTemplate upgrade = getUpgrade();
                    long requiredCasts = getRequiredUpgradeCasts();
                    String upgradePath = getRequiredUpgradePath();
                    ProgressionPath currentPath = activeProperties.getPath();
                    Set<String> upgradeTags = getRequiredUpgradeTags();
                    if ((upgrade != null && requiredCasts > 0 && getCastCount() >= requiredCasts)
                            && (upgradePath == null || upgradePath.isEmpty() || (currentPath != null && currentPath.hasPath(upgradePath)))
                            && (upgradeTags == null || upgradeTags.isEmpty() || (currentPath != null && currentPath.hasAllTags(upgradeTags))))
                    {
                        if (PrerequisiteSpell.hasPrerequisites(activeProperties, upgrade)) {
                            MageSpell newSpell = mage.getSpell(upgrade.getKey());
                            if (isActive()) {
                                deactivate(true, true);
                                if (newSpell != null) {
                                    newSpell.activate();
                                }
                            }
                            activeProperties.forceAddSpell(upgrade.getKey());
                            playEffects("upgrade");

                            if (controller.isPathUpgradingEnabled()) {
                                activeProperties.checkAndUpgrade(true);
                            }
                            mage.updatePassiveEffects();
                            return; // return so progress upgrade doesn't also happen
                        }
                    }
                }
                if (maxLevels > 0 && controller.isSpellProgressionEnabled()) {
                    long previousLevel = getPreviousCastProgressLevel();
                    long currentLevel = getProgressLevel();

                    if (currentLevel != previousLevel) {
                        activeProperties.addSpell(getKey());
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
                            activeProperties.checkAndUpgrade(true);
                        }
                        mage.updatePassiveEffects();
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
    public boolean cancelOnDeath() {
        return cancelOnDeath;
    }

    @Override
    public boolean cancelOnDeactivate() {
        return cancelOnDeactivate;
    }

    @Override
    public boolean cancelOnCastOther() {
        return cancelOnCastOther;
    }

    @Override
    public boolean cancelOnWorldChange() {
        return cancelOnWorldChange;
    }

    @Override
    public boolean cancelOnNoPermission() {
        return cancelOnNoPermission;
    }

    @Override
    public boolean cancelOnNoWand() {
        return cancelOnNoWand;
    }

    @Nullable
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
    public Collection<Requirement> getRequirements() {
        return requirements;
    }

    @Nullable
    @Override
    public String getDurationDescription(Messages messages) {
        String description = null;
        long effectiveDuration = this.getDuration();
        if (effectiveDuration > 0) {
            long seconds = effectiveDuration / 1000;
            if (seconds > 60 * 60) {
                long hours = seconds / (60 * 60);
                description = messages.get("duration.lasts_hours").replace("$hours", ((Long)hours).toString());
            } else if (seconds == 60 * 60) {
                description = messages.get("duration.lasts_hour").replace("$hours", "1");
            } else if (seconds > 60) {
                long minutes = seconds / 60;
                description = messages.get("duration.lasts_minutes").replace("$minutes", ((Long)minutes).toString());
            } else if (seconds == 60) {
                description = messages.get("duration.lasts_minute").replace("$minutes", "1");
            } else if (seconds == 1) {
                description = messages.get("duration.lasts_second").replace("$seconds", ((Long)seconds).toString());
            } else {
                description = messages.get("duration.lasts_seconds").replace("$seconds", ((Long)seconds).toString());
            }
        }

        return description;
    }

    @Nullable
    @Override
    public Double getAttribute(String attributeKey) {
        return mage == null ? null : mage.getAttribute(attributeKey);
    }

    public void setMageClass(MageClass mageClass) {
        this.mageClass = mageClass;
    }

    @Override
    public boolean disableManaRegenerationWhenActive() {
        return disableManaRegeneration;
    }

    public boolean isIndestructible(Block block) {
        return false;
    }

    public boolean isDestructible(Block block) {
        return false;
    }

    @Override
    @Nullable
    public String getCreator() {
        return creatorName;
    }

    @Override
    @Nullable
    public String getCreatorId() {
        return creatorId;
    }

    @Override
    @Nonnull
    public ConfigurationSection getVariables() {
        if (spellData == null) {
            spellData = new SpellData(getKey());
        }
        return spellData.getVariables();
    }

    @Override
    public boolean isPassive() {
        return passive;
    }

    @Override
    public boolean isEnabled() {
        return spellData.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        spellData.setIsEnabled(enabled);
    }

    @Override
    public boolean isToggleable() {
        return toggleable;
    }

    @Override
    @Nullable
    public Collection<Trigger> getTriggers() {
        return triggers;
    }

    public void flagForReactivation() {
        if (isActive() && reactivate) {
            spellData.setIsActive(true);
        } else {
            spellData.setIsActive(false);
        }
    }

    @Override
    public boolean reactivate() {
        if (spellData.isActive() && reactivate) {
            if (onReactivate()) {
                mage.activateSpell(this);
                sendMessage(getMessage("reactivate").replace("$name", getName()));
                spellData.setIsActive(false);
                return true;
            }
        }
        spellData.setIsActive(false);
        return false;
    }

    protected boolean onReactivate() {
        return false;
    }

    @Override
    public long getMaxTimeToCast() {
        long maxTimeToCast = cooldown;
        if (costs != null && mage != null) {
            for (CastingCost cost : costs) {
                int mana = cost.getMana();
                if (mana > 0) {
                    long targetManaTime = (long)(1000.0 * mana / mage.getEffectiveManaRegeneration());
                    maxTimeToCast = Math.max(maxTimeToCast, targetManaTime);
                }
            }
        }
        if (maxCharges > 0 && rechargeRate > 0) {
            maxTimeToCast = Math.max(maxTimeToCast, (int)Math.ceil(1000.0 / rechargeRate));
        }
        return maxTimeToCast;
    }

    @Override
    public Long getTimeToCast() {
        if (mage == null) {
            return null;
        }
        Location location = mage.getLocation();
        if (!canCast(location)) {
            return null;
        }
        long remainingCooldown = getRemainingCooldown();
        CastingCost requiredCost = getRequiredCost();
        long timeToRecharge = spellData.getTimeToRecharge(rechargeRate, maxCharges);

        long timeToCast;
        if (remainingCooldown == 0 && requiredCost == null && timeToRecharge == 0) {
            timeToCast = 0L;
        } else {
            // TODO: These Wand flags are really ugly, can we move these to spell?
            timeToCast = com.elmakers.mine.bukkit.wand.Wand.LiveHotbarCooldown ? remainingCooldown : 0;
            if (com.elmakers.mine.bukkit.wand.Wand.LiveHotbarCharges && timeToRecharge > 0) {
                timeToCast = Math.max(timeToCast, timeToRecharge);
            }
            if (com.elmakers.mine.bukkit.wand.Wand.LiveHotbarMana && requiredCost != null) {
                int mana = requiredCost.getMana();
                if (mana > 0) {
                    if (mana > mage.getEffectiveManaMax()) {
                        return null;
                    }
                    if (mage.getEffectiveManaRegeneration() > 0) {
                        float remainingMana = mana - mage.getMana();
                        long targetManaTime = (long)(1000.0 * remainingMana / mage.getEffectiveManaRegeneration());
                        timeToCast = Math.max(targetManaTime, timeToCast);
                    }
                }
            }
        }
        return timeToCast;
    }

    @Override
    public boolean isQuiet() {
        return quiet;
    }

    public boolean useUrlIcon(Mage mage) {
        String iconURL = getIconURL();
        if (iconURL == null || iconURL.isEmpty()) {
            return false;
        }
        com.elmakers.mine.bukkit.api.block.MaterialAndData icon = getIcon();
        boolean urlIcons = mage == null ? controller.isUrlIconsEnabled() : mage.isUrlIconsEnabled();
        return this.icon.forceUrl() || urlIcons || icon == null || !icon.isValid() || icon.getMaterial() == Material.AIR;
    }

    @Nullable
    public ItemStack createItem(com.elmakers.mine.bukkit.api.magic.Mage mage) {
        String iconURL = getIconURL();
        ItemStack itemStack = null;
        com.elmakers.mine.bukkit.api.block.MaterialAndData icon = getIcon();
        if (useUrlIcon(mage)) {
            itemStack = controller.getURLSkull(iconURL);
        }

        if (itemStack == null) {
            ItemStack originalItemStack = null;
            if (icon == null) {
                controller.getLogger().warning("Unable to create spell icon for " + getKey() + ", missing material");
                return null;
            }
            try {
                originalItemStack = icon.getItemStack(1);
                itemStack = CompatibilityLib.getItemUtils().makeReal(originalItemStack);
            } catch (Exception ex) {
                itemStack = null;
            }

            if (itemStack == null) {
                if (icon.getMaterial() != Material.AIR) {
                    String iconName = icon.getName();
                    controller.getLogger().warning("Unable to create spell icon for " + getKey() + " with material " + iconName);
                }
                return originalItemStack;
            }
        }

        return itemStack;
    }

    // Returns non-null if the item needs an update... not sure this is needed, looks like it's just for skulls though
    public ItemStack updateItem(ItemStack spellItem, boolean canCast) {
        ItemStack needsUpdate = null;
        MaterialAndData disabledIcon = getDisabledIcon();
        MaterialAndData spellIcon = getIcon();
        String urlIcon = getIconURL();
        String disabledUrlIcon = getDisabledIconURL();
        boolean usingURLIcon = useUrlIcon(mage);
        if (disabledIcon != null && spellIcon != null && !usingURLIcon) {
            if (!canCast || !isEnabled()) {
                if (disabledIcon.isValid() && disabledIcon.isDifferent(spellItem)) {
                    disabledIcon.applyToItem(spellItem);
                }
            } else {
                if (spellIcon.isValid() && spellIcon.isDifferent(spellItem)) {
                    spellIcon.applyToItem(spellItem);
                }
            }
        } else if (usingURLIcon && disabledUrlIcon != null && !disabledUrlIcon.isEmpty() && DefaultMaterials.isSkull(spellItem.getType())) {
            String currentURL = CompatibilityLib.getInventoryUtils().getSkullURL(spellItem);
            if (!canCast) {
                if (!disabledUrlIcon.equals(currentURL)) {
                    spellItem = CompatibilityLib.getInventoryUtils().setSkullURL(spellItem, disabledUrlIcon);
                    needsUpdate = spellItem;
                }
            } else {
                if (!urlIcon.equals(currentURL)) {
                    spellItem = CompatibilityLib.getInventoryUtils().setSkullURL(spellItem, urlIcon);
                    needsUpdate = spellItem;
                }
            }
        }
        return needsUpdate;
    }
}
