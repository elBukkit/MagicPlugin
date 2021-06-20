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

import org.bukkit.Art;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
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
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

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

    @SuppressWarnings("unchecked")
    List<Entity> selectEntities(CommandSender sender, String selector);

    int getFacing(BlockFace direction);

    MapView getMapById(int id);

    Map<String, Object> getMap(ConfigurationSection section);

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("deprecation")
    void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut);

    boolean sendActionBar(Player player, String message);

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    boolean setPickupStatus(Arrow arrow, String pickupStatus);

    Block getHitBlock(ProjectileHitEvent event);

    @SuppressWarnings("unchecked")
    Entity getEntity(World world, UUID uuid);

    Entity getEntity(UUID uuid);

    boolean canRemoveRecipes();

    boolean removeRecipe(Recipe recipe);

    boolean removeRecipe(String key);

    ShapedRecipe createShapedRecipe(String key, ItemStack item);

    boolean discoverRecipe(HumanEntity entity, String key);

    boolean undiscoverRecipe(HumanEntity entity, String key);

    double getMaxHealth(Damageable li);

    void setMaxHealth(Damageable li, double maxHealth);

    @SuppressWarnings("deprecation")
    Material fromLegacy(org.bukkit.material.MaterialData materialData);

    @SuppressWarnings("deprecation")
    Material getMaterial(int id, byte data);

    @SuppressWarnings("deprecation")
    Material getMaterial(int id);

    Material getMaterial(String blockData);

    Material getMaterial(FallingBlock falling);

    boolean hasLegacyMaterials();

    boolean isLegacy(Material material);

    Material getLegacyMaterial(String materialName);

    @SuppressWarnings("deprecation")
    Material migrateMaterial(Material material, byte data);

    @SuppressWarnings("deprecation")
    String migrateMaterial(String materialKey);

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

    boolean hasBlockDataSupport();

    boolean isTopBlock(Block block);

    boolean applyPhysics(Block block);

    ItemStack getKnowledgeBook();

    boolean addRecipeToBook(ItemStack book, Plugin plugin, String recipeKey);

    boolean isPowerable(Block block);

    boolean isPowered(Block block);

    boolean setPowered(Block block, boolean powered);

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

    boolean setRecipeGroup(ShapedRecipe recipe, String group);

    boolean isSameKey(Plugin plugin, String key, Object keyed);

    boolean isLegacyRecipes();

    boolean setRecipeIngredient(ShapedRecipe recipe, char key, ItemStack ingredient, boolean ignoreDamage);

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
    List<Entity> getPassengers(Entity entity);

    void teleportVehicle(Entity vehicle, Location location);

    void teleportWithVehicle(Entity entity, Location location);

    boolean isTeleporting();

    boolean openBook(Player player, ItemStack itemStack);

    boolean isHandRaised(Player player);

    void playRecord(Location location, Material record);

    Class<?> getProjectileClass(String projectileTypeName);

    Entity spawnFireworkEffect(Material fireworkMaterial, Server server, Location location, FireworkEffect effect, int power, Vector direction, Integer expectedLifespan, Integer ticksFlown, boolean silent);

    boolean loadAllTagsFromNBT(ConfigurationSection tags, Object tag);

    BoundingBox getHitbox(Entity entity);

    int getMaxEntityRange();

    void load(ConfigurationSection configuration);

    void cancelDismount(EntityDismountEvent event);

    boolean isPrimaryThread();

    String translateColors(String message);

    String getEnchantmentKey(Enchantment enchantment);

    Enchantment getEnchantmentByKey(String key);

    boolean checkDoorAction(Block[] doorBlocks, DoorActionType actionType);

    boolean performDoorAction(Block[] doorBlocks, DoorActionType actionType);

    Block[] getDoorBlocks(Block targetBlock);

    boolean setTorchFacingDirection(Block block, BlockFace facing);
}
