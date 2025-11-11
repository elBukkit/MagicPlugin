package com.elmakers.mine.bukkit.utility.platform.v1_21_0;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ExplosionResult;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Rotation;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Lectern;
import org.bukkit.block.Lockable;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.craftbukkit.v1_21_R1.CraftArt;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftHanging;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.scheduler.CraftTask;
import org.bukkit.craftbukkit.v1_21_R1.util.CraftChatMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.loot.Lootable;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.EnteredStateTracker;
import com.elmakers.mine.bukkit.utility.ReflectionUtils;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.CompatibilityUtilsBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CompatibilityUtils extends CompatibilityUtilsBase {
    private final Map<String, net.minecraft.world.entity.EntityType<?>> projectileEntityTypes = new HashMap<>();
    private final Map<String, Class<? extends net.minecraft.world.entity.projectile.Projectile>> projectileClasses = new HashMap<>();

    public CompatibilityUtils(Platform platform) {
        super(platform);
        populateProjectileClasses();
    }

    private void addProjectileClass(String key, Class<? extends net.minecraft.world.entity.projectile.Projectile> projectileClass, net.minecraft.world.entity.EntityType<?> entityType) {
        projectileClasses.put(key, projectileClass);
        projectileEntityTypes.put(projectileClass.getSimpleName(), entityType);
    }

    private void populateProjectileClasses() {
        // Can't use reflection, so gonna do this the hard (coded) way.
        addProjectileClass("arrow", net.minecraft.world.entity.projectile.Arrow.class, net.minecraft.world.entity.EntityType.ARROW);
        addProjectileClass("tippedarrow", net.minecraft.world.entity.projectile.Arrow.class, net.minecraft.world.entity.EntityType.ARROW);
        addProjectileClass("tipped_arrow", net.minecraft.world.entity.projectile.Arrow.class, net.minecraft.world.entity.EntityType.ARROW);
        addProjectileClass("dragonfireball", net.minecraft.world.entity.projectile.DragonFireball.class, net.minecraft.world.entity.EntityType.DRAGON_FIREBALL);
        addProjectileClass("dragon_fireball", net.minecraft.world.entity.projectile.DragonFireball.class, net.minecraft.world.entity.EntityType.DRAGON_FIREBALL);
        addProjectileClass("fireball", net.minecraft.world.entity.projectile.LargeFireball.class, net.minecraft.world.entity.EntityType.FIREBALL);
        addProjectileClass("largefireball", net.minecraft.world.entity.projectile.LargeFireball.class, net.minecraft.world.entity.EntityType.FIREBALL);
        addProjectileClass("large_fireball", net.minecraft.world.entity.projectile.LargeFireball.class, net.minecraft.world.entity.EntityType.FIREBALL);
        addProjectileClass("smallfireball", net.minecraft.world.entity.projectile.SmallFireball.class, net.minecraft.world.entity.EntityType.SMALL_FIREBALL);
        addProjectileClass("small_fireball", net.minecraft.world.entity.projectile.SmallFireball.class, net.minecraft.world.entity.EntityType.SMALL_FIREBALL);
        addProjectileClass("fireworks", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityType.FIREWORK_ROCKET);
        addProjectileClass("firework", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityType.FIREWORK_ROCKET);
        addProjectileClass("fireworkrocket", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityType.FIREWORK_ROCKET);
        addProjectileClass("firework_rocket", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityType.FIREWORK_ROCKET);
        addProjectileClass("fireworkrocketentity", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityType.FIREWORK_ROCKET);
        addProjectileClass("fishinghook", net.minecraft.world.entity.projectile.FishingHook.class, net.minecraft.world.entity.EntityType.FISHING_BOBBER);
        addProjectileClass("fishing_hook", net.minecraft.world.entity.projectile.FishingHook.class, net.minecraft.world.entity.EntityType.FISHING_BOBBER);
        addProjectileClass("fishing_bobber", net.minecraft.world.entity.projectile.FishingHook.class, net.minecraft.world.entity.EntityType.FISHING_BOBBER);
        addProjectileClass("llamaspit", net.minecraft.world.entity.projectile.LlamaSpit.class, net.minecraft.world.entity.EntityType.LLAMA_SPIT);
        addProjectileClass("llama_spit", net.minecraft.world.entity.projectile.LlamaSpit.class, net.minecraft.world.entity.EntityType.LLAMA_SPIT);
        addProjectileClass("shulkerbullet", net.minecraft.world.entity.projectile.ShulkerBullet.class, net.minecraft.world.entity.EntityType.SHULKER_BULLET);
        addProjectileClass("shulker_bullet", net.minecraft.world.entity.projectile.ShulkerBullet.class, net.minecraft.world.entity.EntityType.SHULKER_BULLET);
        addProjectileClass("snowball", net.minecraft.world.entity.projectile.Snowball.class, net.minecraft.world.entity.EntityType.SNOWBALL);
        addProjectileClass("spectralarrow", net.minecraft.world.entity.projectile.SpectralArrow.class, net.minecraft.world.entity.EntityType.SPECTRAL_ARROW);
        addProjectileClass("spectral_arrow", net.minecraft.world.entity.projectile.SpectralArrow.class, net.minecraft.world.entity.EntityType.SPECTRAL_ARROW);
        addProjectileClass("egg", net.minecraft.world.entity.projectile.ThrownEgg.class, net.minecraft.world.entity.EntityType.EGG);
        addProjectileClass("thrownegg", net.minecraft.world.entity.projectile.ThrownEgg.class, net.minecraft.world.entity.EntityType.EGG);
        addProjectileClass("enderpearl", net.minecraft.world.entity.projectile.ThrownEnderpearl.class, net.minecraft.world.entity.EntityType.ENDER_PEARL);
        addProjectileClass("ender_pearl", net.minecraft.world.entity.projectile.ThrownEnderpearl.class, net.minecraft.world.entity.EntityType.ENDER_PEARL);
        addProjectileClass("thrownenderpearl", net.minecraft.world.entity.projectile.ThrownEnderpearl.class, net.minecraft.world.entity.EntityType.ENDER_PEARL);
        addProjectileClass("thrownexperiencebottle", net.minecraft.world.entity.projectile.ThrownExperienceBottle.class, net.minecraft.world.entity.EntityType.EXPERIENCE_BOTTLE);
        addProjectileClass("experiencebottle", net.minecraft.world.entity.projectile.ThrownExperienceBottle.class, net.minecraft.world.entity.EntityType.EXPERIENCE_BOTTLE);
        addProjectileClass("thrownpotion", net.minecraft.world.entity.projectile.ThrownPotion.class, net.minecraft.world.entity.EntityType.POTION);
        addProjectileClass("potion", net.minecraft.world.entity.projectile.ThrownPotion.class, net.minecraft.world.entity.EntityType.POTION);
        addProjectileClass("throwntrident", net.minecraft.world.entity.projectile.ThrownTrident.class, net.minecraft.world.entity.EntityType.TRIDENT);
        addProjectileClass("trident", net.minecraft.world.entity.projectile.ThrownTrident.class, net.minecraft.world.entity.EntityType.TRIDENT);
        addProjectileClass("witherskull", net.minecraft.world.entity.projectile.WitherSkull.class, net.minecraft.world.entity.EntityType.WITHER_SKULL);
        addProjectileClass("wither_skull", net.minecraft.world.entity.projectile.WitherSkull.class, net.minecraft.world.entity.EntityType.WITHER_SKULL);
    }

    @Override
    public Painting createPainting(Location location, BlockFace facing, Art art) {
        Painting newPainting = null;
        ServerLevel level = ((CraftWorld)location.getWorld()).getHandle();
        Direction directionEnum = null;
        try {
            directionEnum = Direction.valueOf(facing.name());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        BlockPos blockLocation = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraft.world.entity.decoration.Painting newEntity = new net.minecraft.world.entity.decoration.Painting(level, blockLocation, directionEnum, Holder.direct(CraftArt.bukkitToMinecraft(art)));
        Entity bukkitEntity = newEntity.getBukkitEntity();
        if (bukkitEntity != null && bukkitEntity instanceof Painting) {
            newPainting = (Painting)bukkitEntity;
        }
        return newPainting;
    }

    @Override
    public ItemFrame createItemFrame(Location location, BlockFace facing, Rotation rotation, ItemStack item) {
        ItemFrame newItemFrame = null;
        ServerLevel level = ((CraftWorld)location.getWorld()).getHandle();
        Direction directionEnum = null;
        try {
            directionEnum = Direction.valueOf(facing.name());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        BlockPos blockLocation = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraft.world.entity.decoration.ItemFrame newEntity = new net.minecraft.world.entity.decoration.ItemFrame(level, blockLocation, directionEnum);
        Entity bukkitEntity = newEntity.getBukkitEntity();
        if (bukkitEntity != null && bukkitEntity instanceof ItemFrame) {
            newItemFrame = (ItemFrame)bukkitEntity;
            newItemFrame.setItem(platform.getItemUtils().getCopy(item));
            newItemFrame.setRotation(rotation);
        }
        return newItemFrame;
    }

    @Override
    public boolean addToWorld(World world, Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        ServerLevel level = ((CraftWorld)world).getHandle();
        net.minecraft.world.entity.Entity entityHandle = ((CraftEntity)entity).getHandle();
        level.addFreshEntity(entityHandle, reason);
        return true;
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        if (location == null) return null;
        x = Math.min(x, MAX_ENTITY_RANGE);
        z = Math.min(z, MAX_ENTITY_RANGE);
        // Note this no longer special-cases ComplexParts
        return location.getWorld().getNearbyEntities(location, x, y, z);
    }

    @Override
    public void ageItem(Item item, int ticksToAge) {
        ItemEntity nmsItem = (ItemEntity)((CraftItem)item).getHandle();
        nmsItem.age = ticksToAge;
    }

    @Override
    public void magicDamage(Damageable target, double amount, Entity source) {
        try {
            if (target == null || target.isDead()) return;

            // Special-case for witches .. witches are immune to magic damage :\
            // And endermen are immune to indirect damage .. or something.
            // Also armor stands suck.
            // Might need to config-drive this, or just go back to defaulting to normal damage
            if (!USE_MAGIC_DAMAGE || target instanceof Witch || target instanceof Enderman || target instanceof ArmorStand || !(target instanceof LivingEntity)) {
                damage(target, amount, source);
                return;
            }

            net.minecraft.world.entity.Entity targetHandle = ((CraftEntity)target).getHandle();
            if (targetHandle == null) return;

            net.minecraft.world.entity.Entity sourceHandle = source == null ? null : ((CraftEntity)source).getHandle();

            // Bukkit won't allow magic damage from anything but a potion..
            if (sourceHandle != null && source instanceof LivingEntity) {
                Location location = target.getLocation();

                ThrownPotion potion = getOrCreatePotionEntity(location);
                net.minecraft.world.entity.Entity potionHandle = ((CraftEntity)potion).getHandle();
                potion.setShooter((LivingEntity) source);

                DamageSource magicSource = sourceHandle.damageSources().indirectMagic(potionHandle, sourceHandle);

                /*
                // We can't  modify the damage source anymore, it is taken from a common registry, rather than
                // created on demand. We'll need to check and make sure that the ender dragon is damageable from spells.
                // This is a bit of hack that lets us damage the ender dragon, who is a weird and annoying collection
                // of various non-living entity pieces.
                ((EntityDamageSource)magicSource).setThorns();
                */

                try (EnteredStateTracker.Touchable damaging = isDamaging.enter()) {
                    damaging.touch();
                    targetHandle.hurt(magicSource, (float)amount);
                }
            } else {
                try (EnteredStateTracker.Touchable damaging = isDamaging.enter()) {
                    damaging.touch();
                    targetHandle.hurt(targetHandle.damageSources().magic(), (float)amount);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected DamageSource getDamageSource(String damageType, net.minecraft.world.entity.Entity source, DamageSources damageSources) {
        switch (damageType.toUpperCase()) {
            case "IN_FIRE" : return damageSources.inFire();
            case "LIGHTNING_BOLT" : return damageSources.lightningBolt();
            case "ON_FIRE" : return damageSources.onFire();
            case "LAVA" : return damageSources.lava();
            case "HOT_FLOOR" : return damageSources.hotFloor();
            case "IN_WALL" : return damageSources.inWall();
            case "CRAMMING" : return damageSources.cramming();
            case "DROWN" : return damageSources.drown();
            case "STARVE" : return damageSources.starve();
            case "CACTUS" : return damageSources.cactus();
            case "FALL" : return damageSources.fall();
            case "FLY_INTO_WALL" : return damageSources.flyIntoWall();
            case "OUT_OF_WORLD" : return damageSources.fellOutOfWorld();
            case "GENERIC" : return damageSources.generic();
            case "MAGIC" : return damageSources.magic();
            case "WITHER" : return damageSources.wither();
            case "ANVIL" : return damageSources.anvil(source);
            case "FALLING_BLOCK" : return damageSources.fallingBlock(source);
            case "DRAGON_BREATH" : return damageSources.dragonBreath();
            case "DRY_OUT" : return damageSources.dryOut();
            case "SWEET_BERRY_BUSH" : return damageSources.sweetBerryBush();
            case "FREEZE" : return damageSources.freeze();
            case "FALLING_STALACTITE" : return damageSources.fallingStalactite(source);
            case "STALAGMITE" : return damageSources.stalagmite();
            default: return null;
        }
    }

    @Override
    public void damage(Damageable target, double amount, Entity source, String damageType) {
        if (target == null || target.isDead()) return;
        if (damageType.equalsIgnoreCase("direct")) {
            double health = target.getHealth() - amount;
            target.setHealth(Math.max(health, 0));
            return;
        }
        if (damageType.equalsIgnoreCase("magic")) {
            magicDamage(target, amount, source);
            return;
        }
        net.minecraft.world.entity.Entity targetHandle = ((CraftEntity)target).getHandle();
        if (targetHandle == null) return;
        net.minecraft.world.entity.Entity sourceHandle = ((CraftEntity)source).getHandle();
        DamageSource damageSource = getDamageSource(damageType, sourceHandle, targetHandle.damageSources());
        if (damageSource == null) {
            magicDamage(target, amount, source);
            return;
        }

        try (EnteredStateTracker.Touchable damaging = isDamaging.enter()) {
            damaging.touch();
            targetHandle.hurt(damageSource, (float)amount);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isReady(Chunk chunk) {
        return true;
    }

    @Override
    public boolean createExplosion(Entity entity, World world, double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
        return world.createExplosion(x, y, z, power, setFire, breakBlocks, entity);
    }

    @Override
    public BlockEntity getTileEntity(Location location) {
        ServerLevel world = ((CraftWorld)location.getWorld()).getHandle();
        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return world.getBlockEntity(blockPos, false);
    }

    @Override
    public CompoundTag getTileEntityData(Location location) {
        if (location == null) return null;
        BlockEntity tileEntity = getTileEntity(location);
        if (tileEntity == null) return null;
        CompoundTag tag = tileEntity.saveWithFullMetadata(tileEntity.getLevel().registryAccess());
        return tag;
    }

    @Override
    public void setTileEntityData(Location location, Object data) {
        if (location == null || data == null || !(data instanceof CompoundTag)) return;
        BlockEntity tileEntity = getTileEntity(location);
        if (tileEntity == null) return;
        CompoundTag tag = (CompoundTag)data;
        // We need to copy the tag, otherwise this will modify the item being used
        tag = tag.copy();
        tag.putInt("x", location.getBlockX());
        tag.putInt("y", location.getBlockY());
        tag.putInt("z", location.getBlockZ());
        tileEntity.loadWithComponents(tag, tileEntity.getLevel().registryAccess());
        tileEntity.setChanged();
    }

    @Override
    public void clearItems(Location location) {
        if (location == null) return;

        // Block-specific behaviors
        Block block = location.getBlock();
        BlockState blockState = block.getState();
        if (blockState instanceof Lootable) {
            Lootable lootable = (Lootable)blockState;
            lootable.setLootTable(null);
            blockState.update();
        }
        if (blockState instanceof Lectern) {
            Lectern lectern = (Lectern)blockState;
            lectern.getInventory().setItem(0, new ItemStack(Material.AIR));
            blockState.update();
        }
        if (blockState instanceof Jukebox) {
            ((Jukebox) blockState).setRecord(null);
            blockState.update();
        }

        // TODO: Just clear inventory instead?
        BlockEntity tileEntity = getTileEntity(location);
        if (tileEntity == null) return;
        CompoundTag tag = tileEntity.saveWithFullMetadata(tileEntity.getLevel().registryAccess());
        // Is there really not an enum for these NBT types?
        ListTag itemList = tag.getList("Items", CompatibilityConstants.NBT_TYPE_COMPOUND);
        // Is it really necessary to clear the list before removing it?
        if (itemList != null) {
            itemList.clear();
            tag.remove("Items");
            tileEntity.loadWithComponents(tag, tileEntity.getLevel().registryAccess());
            tileEntity.setChanged();
        }
    }

    @Override
    public void setEnvironment(World world, World.Environment environment) {
        // Nah, broken and too ugly anyway
    }

    @Override
    public void playCustomSound(Player player, Location location, String sound, float volume, float pitch) {
        player.playSound(location, sound, volume, pitch);
    }

    @Override
    public List<Entity> selectEntities(CommandSender sender, String selector) {
        if (!selector.startsWith("@")) return null;
        try {
            return Bukkit.selectEntities(sender, selector);
        } catch (IllegalArgumentException ex) {
            platform.getLogger().warning("Invalid selector: " + ex.getMessage());
        }
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public MapView getMapById(int id) {
        return Bukkit.getMap(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getTypedMap(ConfigurationSection section) {
        if (section == null) return null;
        if (section instanceof MemorySection) {
            return (Map<String, T>)ReflectionUtils.getPrivate(platform.getLogger(), section, MemorySection.class, "map");
        }

        // Do it the slow way
        Map<String, T> map = new HashMap<>();
        Set<String> keys = section.getKeys(false);
        for (String key : keys) {
            map.put(key, (T)section.get(key));
        }

        return map;
    }

    @Override
    public boolean setMap(ConfigurationSection section, Map<String, Object> map) {
        if (section == null) return false;
        if (section instanceof MemorySection) {
            return ReflectionUtils.setPrivate(platform.getLogger(), section, MemorySection.class, "map", map);
        }

        return true;
    }

    @Override
    public Vector getPosition(Object entityData, String tag) {
        if (entityData == null || !(entityData instanceof CompoundTag)) return null;
        CompoundTag data = (CompoundTag)entityData;
        ListTag list = data.getList(tag, CompatibilityConstants.NBT_TYPE_DOUBLE);
        double x = list.getDouble(0);
        double y = list.getDouble(1);
        double z = list.getDouble(2);
        return new Vector(x, y, z);
    }

    @Override
    public BlockVector getBlockVector(Object entityData, String tag) {
        if (entityData == null || !(entityData instanceof CompoundTag)) return null;
        CompoundTag data = (CompoundTag)entityData;
        int[] coords = data.getIntArray(tag);
        if (coords == null || coords.length < 3) return null;
        return new BlockVector(coords[0], coords[1], coords[2]);
    }

    @Override
    public void setTNTSource(TNTPrimed tnt, LivingEntity source) {
        tnt.setSource(source);
    }

    @Override
    public void setEntityMotion(Entity entity, Vector motion) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        nms.setDeltaMovement(new Vec3(motion.getX(), motion.getY(), motion.getZ()));
    }

    @Override
    public boolean setLock(Block block, String lockName) {
        BlockState blockData = block.getState();
        if (!(blockData instanceof Lockable)) return false;
        Lockable lockable = (Lockable)blockData;
        lockable.setLock(lockName);
        blockData.update();
        return true;
    }

    @Override
    public boolean clearLock(Block block) {
        BlockState blockData = block.getState();
        if (!(blockData instanceof Lockable)) return false;
        Lockable lockable = (Lockable)blockData;
        lockable.setLock(null);
        blockData.update();
        return true;
    }

    @Override
    public boolean isLocked(Block block) {
        BlockState blockData = block.getState();
        if (!(blockData instanceof Lockable)) return false;
        Lockable lockable = (Lockable)blockData;
        return lockable.isLocked();
    }

    @Override
    public String getLock(Block block) {
        BlockState blockData = block.getState();
        if (!(blockData instanceof Lockable)) return null;
        Lockable lockable = (Lockable)blockData;
        return lockable.getLock();
    }

    @Override
    public void setFallingBlockDamage(FallingBlock entity, float fallHurtAmount, int fallHurtMax) {
        entity.setHurtEntities(true);
        FallingBlockEntity nms = (FallingBlockEntity)((CraftEntity)entity).getHandle();
        nms.fallDamageMax = fallHurtMax;
        nms.fallDamagePerDistance = fallHurtAmount;
    }

    @Override
    public void setGravity(ArmorStand armorStand, boolean gravity) {
        // I think the NMS method may be slightly different, so if things go wrong we'll have to dig deeper
        armorStand.setGravity(gravity);
    }

    @Override
    public void setGravity(Entity entity, boolean gravity) {
        entity.setGravity(gravity);
    }

    @Override
    public void setDisabledSlots(ArmorStand armorStand, int disabledSlots) {
        net.minecraft.world.entity.decoration.ArmorStand nms = ((CraftArmorStand)armorStand).getHandle();
        nms.disabledSlots = disabledSlots;
    }

    @Override
    public int getDisabledSlots(ArmorStand armorStand) {
        net.minecraft.world.entity.decoration.ArmorStand nms = ((CraftArmorStand)armorStand).getHandle();
        return nms.disabledSlots;
    }

    @Override
    public void setInvisible(ArmorStand armorStand, boolean invisible) {
        armorStand.setInvisible(invisible);
    }

    @Override
    public void setInvisible(Entity entity, boolean invisible) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        nms.setInvisible(invisible);
    }

    @Override
    public Boolean isInvisible(Entity entity) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        return nms.isInvisible();
    }

    @Override
    public boolean isPersistentInvisible(Entity entity) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        return nms.persistentInvisibility;
    }

    @Override
    public void setPersistentInvisible(Entity entity, boolean invisible) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        nms.persistentInvisibility = invisible;
    }

    @Override
    public void setYawPitch(Entity entity, float yaw, float pitch) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        // Entity setYawPitch is protected, but I think we can do without the extra
        // CraftBukkit checks ... er, hopefully.
        nms.setYRot(yaw % 360.0F);
        nms.setXRot(pitch % 360.0F);
    }

    @Override
    public void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        nms.absMoveTo(x, y, z, yaw, pitch);
    }

    @Override
    public void addFlightExemption(Player player, int ticks) {
        ServerPlayer nms = ((CraftPlayer)player).getHandle();
        // net.minecraft.server.network.PlayerConnection
        ReflectionUtils.setPrivateNeedsFixing(platform.getLogger(), nms.connection, ServerGamePacketListenerImpl.class, "aboveGroundTickCount", "J", -ticks);
    }

    @Override
    public boolean isValidProjectileClass(Class<?> projectileType) {
        return projectileType != null && net.minecraft.world.entity.projectile.Projectile.class.isAssignableFrom(projectileType);
    }

    @Override
    public Projectile spawnProjectile(Class<?> projectileType, Location location, Vector direction, ProjectileSource source, float speed, float spread, float spreadLocations, Random random) {
        Constructor<? extends Object> constructor = null;
        ServerLevel nmsWorld = ((CraftWorld)location.getWorld()).getHandle();
        Projectile projectile = null;
        try {
            Object entityType = null;
            constructor = projectileType.getConstructor(net.minecraft.world.entity.EntityType.class, net.minecraft.world.level.Level.class);
            entityType = projectileEntityTypes.get(projectileType.getSimpleName());
            if (entityType == null) {
                throw new Exception("Failed to find entity type for projectile class " + projectileType.getName());
            }

            Object nmsProjectile = null;
            try {
                nmsProjectile = constructor.newInstance(entityType, nmsWorld);
            } catch (Exception ex) {
                nmsProjectile = null;
                platform.getLogger().log(Level.WARNING, "Error spawning projectile of class " + projectileType.getName(), ex);
            }

            if (nmsProjectile == null || !(nmsProjectile instanceof net.minecraft.world.entity.Entity)) {
                throw new Exception("Failed to spawn projectile of class " + projectileType.getName());
            }

            // Set position and rotation, and potentially velocity (direction)
            // Velocity must be set manually- EntityFireball.setDirection applies a crazy-wide gaussian distribution!
            if (nmsProjectile instanceof AbstractHurtingProjectile) {
                AbstractHurtingProjectile fireballIsh = (AbstractHurtingProjectile)nmsProjectile;
                // Taken from EntityArrow
                double spreadWeight = Math.min(0.4f,  spread * 0.007499999832361937D);

                double dx = direction.getX() + (random.nextGaussian() * spreadWeight);
                double dy = direction.getY() + (random.nextGaussian() * spreadWeight);
                double dz = direction.getZ() + (random.nextGaussian() * spreadWeight);

                fireballIsh.assignDirectionalMovement(new Vec3(dx, dy, dz), speed * 0.1D);
            }

            net.minecraft.world.entity.Entity nmsEntity = ((net.minecraft.world.entity.Entity)nmsProjectile);
            Vector modifiedLocation = location.toVector().clone();
            if (Fireball.class.isAssignableFrom(projectileType) && spreadLocations > 0) {
                modifiedLocation.setX(modifiedLocation.getX() + direction.getX() + (random.nextGaussian() * spread / 5));
                modifiedLocation.setY(modifiedLocation.getY() + direction.getY() + (random.nextGaussian() * spread / 5));
                modifiedLocation.setZ(modifiedLocation.getZ() + direction.getZ() + (random.nextGaussian() * spread / 5));
            }
            nmsEntity.moveTo(modifiedLocation.getX(), modifiedLocation.getY(), modifiedLocation.getZ(), location.getYaw(), location.getPitch());

            if (nmsEntity instanceof net.minecraft.world.entity.projectile.Projectile) {
                net.minecraft.world.entity.projectile.Projectile nms = (net.minecraft.world.entity.projectile.Projectile)nmsEntity;
                nms.shoot(direction.getX(), direction.getY(), direction.getZ(), speed, spread);
            }

            Entity entity = nmsEntity.getBukkitEntity();
            if (entity == null || !(entity instanceof Projectile)) {
                throw new Exception("Got invalid bukkit entity from projectile of class " + projectileType.getName());
            }

            projectile = (Projectile)entity;
            if (source != null) {
                projectile.setShooter(source);
                nmsEntity.projectileSource = source;
            }

            nmsWorld.addFreshEntity(nmsEntity, CreatureSpawnEvent.SpawnReason.DEFAULT);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }

        return projectile;
    }

    @Override
    public void setDamage(Projectile projectile, double damage) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)projectile).getHandle();
        if (!(nms instanceof net.minecraft.world.entity.projectile.Arrow)) {
            return;
        }
        net.minecraft.world.entity.projectile.Arrow arrow = (net.minecraft.world.entity.projectile.Arrow)nms;
        arrow.setBaseDamage(damage);
    }

    @Override
    public void decreaseLifespan(Projectile projectile, int ticks) {
        net.minecraft.world.entity.Entity nms = ((CraftEntity)projectile).getHandle();
        if (!(nms instanceof net.minecraft.world.entity.projectile.Arrow)) {
            return;
        }
        net.minecraft.world.entity.projectile.Arrow arrow = (net.minecraft.world.entity.projectile.Arrow)nms;
        arrow.life = ticks;
    }

    @Override
    public Entity spawnEntity(Location target, EntityType entityType, CreatureSpawnEvent.SpawnReason spawnReason) {
        CraftWorld craftWorld = (CraftWorld)target.getWorld();
        return craftWorld.spawn(target, entityType.getEntityClass(), null, spawnReason);
    }

    @Override
    public CompoundTag getEntityData(Entity entity) {
        if (entity == null) return null;
        CompoundTag data = new CompoundTag();
        ((CraftEntity)entity).getHandle().save(data);
        return data;
    }

    @Override
    public String getEntityType(Entity entity) {
        if (entity == null) return null;
        return ((CraftEntity)entity).getHandle().getEncodeId();
    }

    @Override
    public void swingOffhand(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        ((LivingEntity)entity).swingOffHand();
    }

    @Override
    public void swingMainHand(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        ((LivingEntity)entity).swingMainHand();
    }

    protected void sendPacket(Server server, Location source, Collection<? extends Player> players, Packet<?> packet) throws Exception  {
        players = ((players != null && players.size() > 0) ? players : server.getOnlinePlayers());

        int viewDistance = Bukkit.getServer().getViewDistance() * 16;
        int viewDistanceSquared =  viewDistance * viewDistance;
        World sourceWorld = source.getWorld();
        for (Player player : players) {
            Location location = player.getLocation();
            if (!location.getWorld().equals(sourceWorld)) continue;
            if (location.distanceSquared(source) <= viewDistanceSquared) {
                sendPacket(player, packet);
            }
        }
    }

    protected void sendPacket(Player player, Packet<?> packet) throws Exception {
        ServerPlayer nmsPlayer = ((CraftPlayer)player).getHandle();
        nmsPlayer.connection.send(packet);
    }

    @Override
    public void sendBreaking(Player player, long id, Location location, int breakAmount) {
        try {
            BlockPos blockPosition = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            Packet<?> packet = new ClientboundBlockDestructionPacket((int)id, blockPosition, breakAmount);
            sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isJumping(LivingEntity entity) {
        net.minecraft.world.entity.LivingEntity living = ((CraftLivingEntity)entity).getHandle();
        // net.minecraft.world.entity.EntityLiving
        return (boolean)ReflectionUtils.getPrivateNeedsFixing(platform.getLogger(), living, net.minecraft.world.entity.LivingEntity.class, "jumping", "bn");
    }

    @Override
    public float getForwardMovement(LivingEntity entity) {
        net.minecraft.world.entity.LivingEntity living = ((CraftLivingEntity)entity).getHandle();
        // For real, I guess?
        return living.zza;
    }

    @Override
    public float getStrafeMovement(LivingEntity entity) {
        net.minecraft.world.entity.LivingEntity living = ((CraftLivingEntity)entity).getHandle();
        // For real, I guess?
        return living.xxa;
    }

    @Override
    public Entity getEntity(World world, UUID uuid) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftWorld)world).getHandle().getEntity(uuid);
        return nmsEntity == null ? null : nmsEntity.getBukkitEntity();
    }

    @Override
    public boolean applyBonemeal(Location location) {
        if (dummyItem == null) {
            dummyItem = new ItemStack(Material.DIRT, 64);
            dummyItem = platform.getItemUtils().makeReal(dummyItem);
        }
        dummyItem.setAmount(64);
        ServerLevel nmsWorld = ((CraftWorld)location.getWorld()).getHandle();
        net.minecraft.world.item.ItemStack itemStack = (net.minecraft.world.item.ItemStack)platform.getItemUtils().getHandle(dummyItem);
        BlockPos blockPosition = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return BoneMealItem.growCrop(itemStack, nmsWorld, blockPosition);
    }

    @Override
    public boolean applyPhysics(Block block) {
        ServerLevel nmsWorld = ((CraftWorld)block.getWorld()).getHandle();
        BlockPos blockLocation = new BlockPos(block.getX(), block.getY(), block.getZ());
        net.minecraft.world.level.block.state.BlockState blockState = nmsWorld.getBlockState(blockLocation);
        clearItems(block.getLocation());
        platform.getDeprecatedUtils().setTypeAndData(block, Material.AIR, (byte)0, false);
        return nmsWorld.setBlock(blockLocation, blockState, 3);
    }

    @Override
    public Location getHangingLocation(Entity entity) {
        Location location = entity.getLocation();
        if (!(entity instanceof Hanging)) return location;
        HangingEntity nms = ((CraftHanging)entity).getHandle();
        BlockPos position = nms.blockPosition();
        location.setX(position.getX());
        location.setY(position.getY());
        location.setZ(position.getZ());
        return location;
    }

    @Override
    public boolean setAutoBlockState(Block block, Location target, BlockFace facing, boolean physics, Player originator) {
        if (block == null || facing == null || target == null) return false;
        net.minecraft.world.level.block.state.BlockState blockState = ((CraftBlock)block).getNMS();
        if (blockState == null) return false;
        net.minecraft.world.level.block.Block nmsBlock = blockState.getBlock();
        ItemStack blockItem = new ItemStack(block.getType());
        ServerPlayer originatorHandle = ((CraftPlayer)originator).getHandle();
        ServerLevel world = ((CraftWorld)block.getWorld()).getHandle();
        Object item = platform.getItemUtils().getHandle(platform.getItemUtils().makeReal(blockItem));
        if (originatorHandle == null || world == null || item == null) {
            return false;
        }
        BlockPos blockPosition = new BlockPos(block.getX(), block.getY(), block.getZ());
        Vec3 vec3D = new Vec3(target.getX(), target.getY(), target.getZ());
        Direction direction;
        try {
            direction = Direction.valueOf(facing.name());
        } catch (Exception ex) {
            platform.getLogger().log(Level.SEVERE, "Could not translate to NMS direction: " + facing);
            return false;
        }
        BlockHitResult hitResult = new BlockHitResult(vec3D, direction, blockPosition, false);
        BlockPlaceContext actionContext = new BlockPlaceContext(originatorHandle, InteractionHand.MAIN_HAND, (net.minecraft.world.item.ItemStack)item, hitResult);
        net.minecraft.world.level.block.state.BlockState state = nmsBlock.getStateForPlacement(actionContext);
        if (state == null) return false;
        CraftBlock cBlock = (CraftBlock)block;
        CraftBlock.setTypeAndData(cBlock.getHandle(), cBlock.getPosition(), cBlock.getNMS(), state, physics);
        return true;
    }

    @Override
    public boolean forceUpdate(Block block, boolean physics) {
        if (block == null) return false;
        net.minecraft.world.level.block.state.BlockState blockState = ((CraftBlock)block).getNMS();
        if (blockState == null) return false;
        net.minecraft.world.level.block.Block nmsBlock = blockState.getBlock();
        net.minecraft.world.level.block.state.BlockState blockData = nmsBlock.defaultBlockState();
        ServerLevel world = ((CraftWorld)block.getWorld()).getHandle();
        BlockPos blockPosition = new BlockPos(block.getX(), block.getY(), block.getZ());
        world.setBlock(blockPosition, blockData, 11);
        return false;
    }

    @Override
    public Class<?> getProjectileClass(String projectileTypeName) {
        return projectileClasses.get(projectileTypeName.toLowerCase());
    }

    @Override
    public Entity spawnFireworkEffect(Material fireworkMaterial, Server server, Location location, FireworkEffect effect, int power, Vector direction, Integer expectedLifespan, Integer ticksFlown, boolean silent) {
        Entity entity = null;
        try {
            if (fireworkMaterial == null) {
                return null;
            }
            ServerLevel level = ((CraftWorld)location.getWorld()).getHandle();
            ItemStack itemStack = new ItemStack(fireworkMaterial);
            FireworkMeta meta = (FireworkMeta) itemStack.getItemMeta();
            meta.addEffect(effect);
            meta.setPower(power);
            itemStack.setItemMeta(meta);

            Object item = platform.getItemUtils().getHandle(platform.getItemUtils().makeReal(itemStack));
            final FireworkRocketEntity fireworkHandle = new FireworkRocketEntity(level, location.getX(), location.getY(), location.getZ(), (net.minecraft.world.item.ItemStack)item);
            fireworkHandle.setSilent(silent);

            if (direction != null) {
                fireworkHandle.setDeltaMovement(new Vec3(direction.getX(), direction.getY(), direction.getZ()));
            }

            if (ticksFlown != null) {
                fireworkHandle.life = ticksFlown;
            }
            if (expectedLifespan != null) {
                fireworkHandle.lifetime = expectedLifespan;
            }

            if (direction == null) {
                BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                ClientboundAddEntityPacket fireworkPacket = new ClientboundAddEntityPacket(fireworkHandle, CompatibilityConstants.FIREWORK_TYPE, blockPos);
                int fireworkId = fireworkHandle.getId();
                SynchedEntityData watcher = fireworkHandle.getEntityData();
                ClientboundSetEntityDataPacket metadataPacket = new ClientboundSetEntityDataPacket(fireworkId, watcher.packDirty());
                ClientboundEntityEventPacket statusPacket = new ClientboundEntityEventPacket(fireworkHandle, (byte)17);
                ClientboundRemoveEntitiesPacket destroyPacket = new ClientboundRemoveEntitiesPacket(fireworkId);

                Collection<? extends Player> players = server.getOnlinePlayers();
                sendPacket(server, location, players, fireworkPacket);
                sendPacket(server, location, players, metadataPacket);
                sendPacket(server, location, players, statusPacket);
                sendPacket(server, location, players, destroyPacket);
                return null;
            }

            level.addFreshEntity(fireworkHandle, CreatureSpawnEvent.SpawnReason.CUSTOM);
            entity = fireworkHandle.getBukkitEntity();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return entity;
    }

    @Override
    public boolean loadAllTagsFromNBT(ConfigurationSection tags, Object tag)
    {
        if (!(tag instanceof CompoundTag)) return false;
        CompoundTag compoundTag = (CompoundTag) tag;
        Set<String> keys = platform.getNBTUtils().getTagKeys(tag);
        if (keys == null) return false;
        for (String tagName : keys) {
            Tag metaBase = compoundTag.get(tagName);
            if (metaBase != null) {
                if (metaBase instanceof CompoundTag) {
                    ConfigurationSection newSection = tags.createSection(tagName);
                    loadAllTagsFromNBT(newSection, metaBase);
                } else {
                    try {
                        tags.set(tagName, platform.getNBTUtils().getTagValue(metaBase));
                    } catch (Exception ex) {
                        platform.getLogger().log(Level.SEVERE, "Failed to load NBT tags", ex);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Enchantment getEnchantmentByKey(String key) {
        // Really wish there was a fromString that took a string default namespace
        String namespace = NamespacedKey.MINECRAFT;
        Enchantment enchantment = null;
        if (key.contains(":")) {
            String[] pieces = StringUtils.split(key, ":", 2);
            namespace = pieces[0];
            key = pieces[1];
        } else {
            // Convert legacy enum names
            enchantment = Enchantment.getByName(key.toUpperCase());
            if (enchantment != null) {
                return enchantment;
            }
        }

        // API says plugins aren't supposed to use this, but i have no idea how to deal
        // with custom enchants otherwise
        try {
            NamespacedKey namespacedKey = new NamespacedKey(namespace, key.toLowerCase());
            enchantment = Enchantment.getByKey(namespacedKey);
            if (enchantment == null) {
                // Convert legacy enchantments
                enchantment = Enchantment.getByName(key.toUpperCase());
            }
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Unexpected error parsing enchantment key", ex);
        }
        return enchantment;
    }

    @Override
    public boolean setRawLore(ItemStack itemStack, List<String> lore) {
        ItemUtils itemUtils = platform.getItemUtils();
        net.minecraft.world.item.ItemStack mcItemStack = (net.minecraft.world.item.ItemStack)itemUtils.getHandle(itemStack);
        if (mcItemStack == null) {
            return false;
        }
        List<Component> components = new ArrayList<>();
        for (String line : lore) {
            components.add(toNMSComponent(line));
        }
        ItemLore loreComponent = new ItemLore(components);
        mcItemStack.set(DataComponents.LORE, loreComponent);
        return true;
    }

    @Override
    public List<String> getRawLore(ItemStack itemStack) {
        List<String> lines = new ArrayList<>();
        ItemUtils itemUtils = platform.getItemUtils();
        net.minecraft.world.item.ItemStack mcItemStack = (net.minecraft.world.item.ItemStack)itemUtils.getHandle(itemStack);
        if (mcItemStack == null) return lines;
        ItemLore itemLore = mcItemStack.get(DataComponents.LORE);
        for (Component component : itemLore.lines()) {
            lines.add(fromNMSComponent(component));
        }
        return lines;
    }

    @Override
    protected boolean sendActionBarPackets(Player player, String message) {
        Component component = Component.literal(message);
        ClientboundSystemChatPacket packet = new ClientboundSystemChatPacket(component, true);
        try {
            sendPacket(player, packet);
        } catch (Exception ex) {
            platform.getLogger().log(Level.SEVERE, "Error updating action bar", ex);
            return false;
        }
        return true;
    }

    @Override
    protected void setBossBarTitleComponents(BossBar bossBar, String serialized, String fallback) {
        Object handle = ReflectionUtils.getHandle(platform.getLogger(), bossBar);
        if (handle == null || !(handle instanceof ServerBossEvent)) {
            bossBar.setTitle(fallback);
            return;
        }
        ServerBossEvent bossEvent = (ServerBossEvent)handle;
        Component component = toNMSComponent(serialized);
        if (component == null) {
            bossBar.setTitle(fallback);
        } else {
            bossEvent.setName(component);
        }
    }

    private Component toNMSComponent(String serialized) {
        if (serialized == null || serialized.isEmpty()) {
            return null;
        }
        Component component;
        try {
            if (serialized.isEmpty()) {
                component = CommonComponents.EMPTY;
            } else if (serialized.startsWith("{")) {
                component = CraftChatMessage.fromJSON(serialized);
            } else {
                component = Component.literal(serialized);
            }
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Invalid JSON message: " + serialized, ex);
            component = CommonComponents.EMPTY;
        }
        return component;
    }

    private String fromNMSComponent(Component component) {
        return CraftChatMessage.toJSON(component);
    }

    @Override
    public Runnable getTaskRunnable(BukkitTask task) {
        return (Runnable)ReflectionUtils.getPrivate(platform.getLogger(), task, CraftTask.class, "rTask");
    }

    @Override
    public boolean isSwingingArm(Entity entity) {
        if (entity == null) return false;
        net.minecraft.world.entity.Entity nms = ((CraftEntity)entity).getHandle();
        if (nms == null || !(nms instanceof net.minecraft.world.entity.LivingEntity)) {
            return false;
        }
        return ((net.minecraft.world.entity.LivingEntity)nms).swinging;
    }

    @Override
    public boolean setLastDamaged(Entity damaged, Entity damager) {
        if (damager == null) {
            return false;
        }
        net.minecraft.world.entity.Entity nmsDamager = ((CraftEntity)damager).getHandle();
        if (nmsDamager == null || !(nmsDamager instanceof net.minecraft.world.entity.LivingEntity)) {
            return false;
        }
        net.minecraft.world.entity.LivingEntity livingDamager = (net.minecraft.world.entity.LivingEntity)nmsDamager;
        net.minecraft.world.entity.Entity nmsDamaged = damaged == null ? null : ((CraftEntity)damaged).getHandle();
        livingDamager.setLastHurtMob(nmsDamaged);
        return true;
    }

    @Override
    public boolean setLastDamagedBy(Entity damaged, Entity damager) {
        if (damager == null) {
            return false;
        }
        net.minecraft.world.entity.Entity nmsDamaged = ((CraftEntity)damager).getHandle();
        if (nmsDamaged == null || !(nmsDamaged instanceof net.minecraft.world.entity.LivingEntity)) {
            return false;
        }
        net.minecraft.world.entity.LivingEntity livingDamaged = (net.minecraft.world.entity.LivingEntity)nmsDamaged;
        net.minecraft.world.entity.Entity nmsDamager = ((CraftEntity)damager).getHandle();
        net.minecraft.world.entity.LivingEntity livingDamager = nmsDamager instanceof net.minecraft.world.entity.LivingEntity
                ? (net.minecraft.world.entity.LivingEntity)nmsDamaged : null;
        livingDamaged.setLastHurtByMob(livingDamager);
        return true;
    }

    @Override
    public Enchantment getInfinityEnchantment() {
        return Enchantment.INFINITY;
    }

    @Override
    public Enchantment getPowerEnchantment() {
        return Enchantment.POWER;
    }

    @Override
    public PotionEffectType getJumpPotionEffectType() {
        return PotionEffectType.JUMP_BOOST;
    }

    @Override
    public boolean isDestructive(EntityExplodeEvent explosion) {
        ExplosionResult result = explosion.getExplosionResult();
        return result == ExplosionResult.DESTROY || result == ExplosionResult.DESTROY_WITH_DECAY;
    }
}
