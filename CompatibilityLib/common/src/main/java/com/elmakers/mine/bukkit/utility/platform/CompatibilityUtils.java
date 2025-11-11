package com.elmakers.mine.bukkit.utility.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Art;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Rotation;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.DoorActionType;

public interface CompatibilityUtils {
    void applyPotionEffects(LivingEntity entity, Collection<PotionEffect> effects);

    boolean isDamaging();

    Inventory createInventory(InventoryHolder holder, int size, String name);

    boolean applyPotionEffect(LivingEntity entity, PotionEffect effect);

    boolean setDisplayNameRaw(ItemStack itemStack, String displayName);

    boolean setDisplayName(ItemStack itemStack, String displayName);

    boolean setLore(ItemStack itemStack, List<String> lore);

    boolean setRawLore(ItemStack itemStack, List<String> lore);

    List<String> getRawLore(ItemStack itemStack);

    boolean isInvulnerable(Entity entity);

    void setInvulnerable(Entity entity);

    void setInvulnerable(Entity entity, boolean flag);

    boolean isSilent(Entity entity);

    void setSilent(Entity entity, boolean flag);

    boolean isPersist(Entity entity);

    void setRemoveWhenFarAway(Entity entity, boolean flag);

    void setPersist(Entity entity, boolean flag);

    boolean isSitting(Entity entity);

    void setSitting(Entity entity, boolean flag);

    Painting createPainting(Location location, BlockFace facing, Art art);

    ItemFrame createItemFrame(Location location, BlockFace facing, Rotation rotation, ItemStack item);

    ArmorStand createArmorStand(Location location);

    Entity createEntity(Location location, EntityType entityType);

    boolean isFilledMap(Material material);

    boolean addToWorld(World world, Entity entity, CreatureSpawnEvent.SpawnReason reason);

    Collection<Entity> getNearbyEntities(Location location, double x, double y, double z);

    Runnable getTaskRunnable(BukkitTask task);

    void ageItem(Item item, int ticksToAge);

    void damage(Damageable target, double amount, Entity source);

    void damage(Damageable target, double amount, Entity source, String damageType);

    void magicDamage(Damageable target, double amount, Entity source);

    Location getEyeLocation(Entity entity);

    YamlConfiguration loadBuiltinConfiguration(String fileName) throws IOException, InvalidConfigurationException;

    ConfigurationSection loadConfiguration(File file) throws IOException, InvalidConfigurationException;

    ConfigurationSection loadConfiguration(String fileName) throws IOException, InvalidConfigurationException;

    YamlConfiguration loadConfiguration(InputStream stream, String fileName) throws IOException, InvalidConfigurationException;

    boolean isReady(Chunk chunk);

    boolean createExplosion(Entity entity, World world, double x, double y, double z, float power, boolean setFire, boolean breakBlocks);

    Object getTileEntityData(Location location);

    Object getTileEntity(Location location);

    void clearItems(Location location);

    void setTileEntityData(Location location, Object data);

    void setEnvironment(World world, World.Environment environment);

    void playCustomSound(Player player, Location location, String sound, float volume, float pitch);

    List<Entity> selectEntities(CommandSender sender, String selector);

    int getFacing(BlockFace direction);

    MapView getMapById(int id);

    Map<String, Object> getMap(ConfigurationSection section);

    <T> Map<String, T> getTypedMap(ConfigurationSection section);

    boolean setMap(ConfigurationSection section, Map<String, Object> map);

    Vector getPosition(Object entityData, String tag);

    BlockVector getBlockVector(Object entityData, String tag);

    void setTNTSource(TNTPrimed tnt, LivingEntity source);

    void setEntityMotion(Entity entity, Vector motion);

    Vector getNormal(Block block, Location intersection);

    boolean setLock(Block block, String lockName);

    boolean clearLock(Block block);

    boolean isLocked(Block block);

    String getLock(Block block);

    void setFallingBlockDamage(FallingBlock entity, float fallHurtAmount, int fallHurtMax);

    void configureMaxHeights(ConfigurationSection config);

    int getMinHeight(World world);

    int getMaxHeight(World world);

    void setGravity(ArmorStand armorStand, boolean gravity);

    void setGravity(Entity entity, boolean gravity);

    void setDisabledSlots(ArmorStand armorStand, int disabledSlots);

    int getDisabledSlots(ArmorStand armorStand);

    void setInvisible(ArmorStand armorStand, boolean invisible);

    void setInvisible(Entity entity, boolean invisible);

    Boolean isInvisible(Entity entity);

    boolean isPersistentInvisible(Entity entity);

    void setPersistentInvisible(Entity entity, boolean invisible);

    void setYawPitch(Entity entity, float yaw, float pitch);

    void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch);

    void addFlightExemption(Player player, int ticks);

    boolean isValidProjectileClass(Class<?> projectileType);

    Projectile spawnProjectile(Class<?> projectileType, Location location, Vector direction, ProjectileSource source, float speed, float spread, float spreadLocations, Random random);

    void setDamage(Projectile projectile, double damage);

    void decreaseLifespan(Projectile projectile, int ticks);

    Entity spawnEntity(Location target, EntityType entityType, CreatureSpawnEvent.SpawnReason spawnReason);

    String getResourcePack(Server server);

    boolean setResourcePack(Player player, String rp, byte[] hash);

    boolean removeItemAttribute(ItemStack item, Attribute attribute);

    boolean removeItemAttributes(ItemStack item);

    boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, int attributeOperation);

    boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, int attributeOperation, UUID attributeUUID);

    void sendExperienceUpdate(Player player, float experience, int level);

    Object getEntityData(Entity entity);

    String getEntityType(Entity entity);

    void applyItemData(ItemStack item, Block block);

    void swingOffhand(Entity entity);

    void swingMainHand(Entity entity);

    void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut);

    boolean sendActionBar(Player player, String message);

    boolean sendActionBar(Player player, String message, String font);

    void setBossBarTitle(BossBar bossBar, String title);

    boolean setBossBarTitle(BossBar bossBar, String title, String font);

    void sendMessage(CommandSender sender, String message);

    float getDurability(Material material);

    void sendBreaking(Player player, long id, Location location, int breakAmount);

    void clearBreaking(Block block);

    void setBreaking(Block block, double percentage);

    void setBreaking(Block block, int breakAmount);

    void setBreaking(Block block, int breakAmount, int range);

    Set<String> getTags(Entity entity);

    boolean isJumping(LivingEntity entity);

    float getForwardMovement(LivingEntity entity);

    float getStrafeMovement(LivingEntity entity);

    boolean setBlockFast(Block block, Material material, int data);

    boolean setBlockFast(Chunk chunk, int x, int y, int z, Material material, int data);

    boolean setPickupStatus(Projectile projectile, String pickupStatus);

    Block getHitBlock(ProjectileHitEvent event);

    Entity getEntity(World world, UUID uuid);

    Entity getEntity(UUID uuid);

    boolean canRemoveRecipes();

    boolean removeRecipe(Recipe recipe);

    boolean removeRecipe(String key);

    ShapelessRecipe createShapelessRecipe(String key, ItemStack item, Collection<ItemStack> ingredients, boolean ignoreDamage);

    ShapedRecipe createShapedRecipe(String key, ItemStack item);

    FurnaceRecipe createFurnaceRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime);

    Recipe createBlastingRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime);

    Recipe createCampfireRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime);

    Recipe createSmokingRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime);

    Recipe createStonecuttingRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage);

    Recipe createSmithingRecipe(String key, ItemStack item, ItemStack source, ItemStack addition);

    boolean discoverRecipe(HumanEntity entity, String key);

    boolean undiscoverRecipe(HumanEntity entity, String key);

    double getMaxHealth(Damageable li);

    void setMaxHealth(Damageable li, double maxHealth);

    Material getMaterial(String blockData);

    Material getMaterial(FallingBlock falling);

    boolean isChunkLoaded(Block block);

    boolean isChunkLoaded(Location location);

    boolean checkChunk(Location location);

    boolean checkChunk(Location location, boolean generate);

    boolean checkChunk(World world, int chunkX, int chunkZ);

    boolean checkChunk(World world, int chunkX, int chunkZ, boolean generate);

    boolean applyBonemeal(Location location);

    Color getColor(PotionMeta meta);

    boolean setColor(PotionMeta meta, Color color);

    byte getLegacyBlockData(FallingBlock falling);

    String getBlockData(FallingBlock fallingBlock);

    String getBlockData(Material material, byte data);

    String getBlockData(Block block);

    boolean setBlockData(Block block, String data);

    boolean isTopBlock(Block block);

    boolean applyPhysics(Block block);

    ItemStack getKnowledgeBook();

    boolean addRecipeToBook(ItemStack book, Plugin plugin, String recipeKey);

    boolean isPowerable(Block block);

    boolean isPowered(Block block);

    boolean setPowered(Block block, boolean powered);

    boolean extendPiston(Block block);

    boolean toggleBlockPower(Block block);

    boolean canToggleBlockPower(Block block);

    boolean isWaterLoggable(Block block);

    boolean setWaterlogged(Block block, boolean waterlogged);

    boolean setTopHalf(Block block);

    Entity getSource(Entity entity);

    boolean stopSound(Player player, Sound sound);

    boolean stopSound(Player player, String sound);

    boolean lockChunk(Chunk chunk);

    boolean unlockChunk(Chunk chunk);

    Location getHangingLocation(Entity entity);

    BlockFace getCCW(BlockFace face);

    boolean setRecipeGroup(Recipe recipe, String group);

    boolean isSameKey(Plugin plugin, String key, Object keyed);

    boolean setRecipeIngredient(ShapedRecipe recipe, char key, ItemStack ingredient, boolean ignoreDamage);

    boolean setAutoBlockState(Block block, Location target, BlockFace facing, boolean physics, Player originator);

    boolean forceUpdate(Block block, boolean physics);

    int getPhantomSize(Entity entity);

    boolean setPhantomSize(Entity entity, int size);

    Location getBedSpawnLocation(Player player);

    void loadChunk(Location location, boolean generate, Consumer<Chunk> consumer);

    void loadChunk(World world, int x, int z, boolean generate);

    void loadChunk(World world, int x, int z, boolean generate, Consumer<Chunk> consumer);

    Entity getRootVehicle(Entity entity);

    void addPassenger(Entity vehicle, Entity passenger);

    List<Entity> getPassengers(Entity entity);

    void teleportVehicle(Entity vehicle, Location location);

    void teleportWithVehicle(Entity entity, Location location);

    boolean isTeleporting();

    boolean openBook(Player player, ItemStack itemStack);

    boolean isHandRaised(Player player);

    Class<?> getProjectileClass(String projectileTypeName);

    Entity spawnFireworkEffect(Material fireworkMaterial, Server server, Location location, FireworkEffect effect, int power, Vector direction, Integer expectedLifespan, Integer ticksFlown, boolean silent);

    boolean loadAllTagsFromNBT(ConfigurationSection tags, Object tag);

    BoundingBox getHitbox(Entity entity);

    int getMaxEntityRange();

    void load(ConfigurationSection configuration);

    boolean isPrimaryThread();

    String translateColors(String message);

    @Nullable
    String getEnchantmentKey(Enchantment enchantment);

    @Nullable
    String getEnchantmentBaseKey(Enchantment enchantment);

    Enchantment getEnchantmentByKey(String key);

    Collection<String> getEnchantmentBaseKeys();

    boolean checkDoorAction(Block[] doorBlocks, DoorActionType actionType);

    boolean performDoorAction(Block[] doorBlocks, DoorActionType actionType);

    Block[] getDoorBlocks(Block targetBlock);

    boolean setTorchFacingDirection(Block block, BlockFace facing);

    boolean isAdult(Zombie zombie);

    void setBaby(Zombie zombie);

    void setAdult(Zombie zombie);

    boolean tame(Entity entity, Player tamer);

    boolean isArrow(Entity entity);

    void setMaterialCooldown(Player player, Material material, int duration);

    Particle getParticle(String particle);

    BlockFace getSignFacing(Block sign);

    void sendBlockChange(Player player, Block block);

    void sendBlockChange(Player player, Location location, Material material, String blockData);

    default boolean setCompassTarget(ItemMeta meta, Location targetLocation, boolean trackLocation) {
        return false;
    }

    default boolean isAware(Entity entity) {
        return true;
    }

    default void setAware(Entity entity, boolean aware) {}

    void openSign(Player player, Location signBlock);

    Collection<BoundingBox> getBoundingBoxes(Block block);

    @Nonnull
    FallingBlock spawnFallingBlock(Location location, Material material, String blockData);

    UUID getOwnerId(Entity entity);

    void setOwner(Entity entity, Entity owner);

    void setOwner(Entity entity, UUID ownerId);

    default boolean isSwingingArm(Entity entity) { return false; }

    default boolean setLastDamaged(Entity damaged, Entity damager) { return false; }

    default boolean setLastDamagedBy(Entity damaged, Entity damager) { return false; }

    void setSnowLevel(Block block, int level);

    int getSnowLevel(Block block);

    @Nullable
    BlockPopulator createOutOfBoundsPopulator(Logger logger);

    Enchantment getInfinityEnchantment();

    Enchantment getPowerEnchantment();

    PotionEffectType getJumpPotionEffectType();

    Set<PotionEffectType> getNegativeEffects();

    boolean isDestructive(EntityExplodeEvent explosion);
}
