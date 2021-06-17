package com.elmakers.mine.bukkit.utility.platform.v1_17_0;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Rotation;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lockable;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.craftbukkit.v1_17_R1.CraftArt;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftHanging;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.loot.Lootable;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.EnteredStateTracker;
import com.elmakers.mine.bukkit.utility.LoadingChunk;
import com.elmakers.mine.bukkit.utility.ReflectionUtils;
import com.elmakers.mine.bukkit.utility.platform.PaperUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.CompatibilityUtilsBase;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Multimap;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CompatibilityUtils extends CompatibilityUtilsBase {
    private final Map<String, net.minecraft.world.entity.EntityType> projectileEntityTypes = new HashMap<>();
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
    public Inventory createInventory(InventoryHolder holder, int size, final String name) {
        size = (int) (Math.ceil((double) size / 9) * 9);
        size = Math.min(size, 54);

        String shorterName = name;
        if (shorterName.length() > 32) {
            shorterName = shorterName.substring(0, 31);
        }
        shorterName = translateColors(shorterName);
        return Bukkit.createInventory(holder, size, shorterName);
    }

    @Override
    public boolean isInvulnerable(Entity entity) {
        return entity.isInvulnerable();
    }

    @Override
    public void setInvulnerable(Entity entity, boolean flag) {
        entity.setInvulnerable(flag);
    }

    @Override
    public boolean isSilent(Entity entity) {
        return entity.isSilent();
    }

    @Override
    public void setSilent(Entity entity, boolean flag) {
        entity.setSilent(flag);
    }

    @Override
    public boolean isPersist(Entity entity) {
        return entity.isPersistent();
    }

    @Override
    public void setPersist(Entity entity, boolean flag) {
        entity.setPersistent(flag);
    }

    @Override
    public void setRemoveWhenFarAway(Entity entity, boolean flag) {
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity li = (LivingEntity)entity;
        li.setRemoveWhenFarAway(flag);
    }

    @Override
    public boolean isSitting(Entity entity) {
        if (!(entity instanceof Sittable)) return false;
        Sittable sittable = (Sittable)entity;
        return sittable.isSitting();
    }

    @Override
    public void setSitting(Entity entity, boolean flag) {
        if (!(entity instanceof Sittable)) return;
        Sittable sittable = (Sittable)entity;
        sittable.setSitting(flag);
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
        BlockPos blockLocation = new BlockPos(location.getX(), location.getY(), location.getZ());
        net.minecraft.world.entity.decoration.Painting newEntity = new net.minecraft.world.entity.decoration.Painting(level, blockLocation, directionEnum);
        if (newEntity != null) {
            Motive notchArt = CraftArt.BukkitToNotch(art);
            newEntity.motive = notchArt;
        }
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
        BlockPos blockLocation = new BlockPos(location.getX(), location.getY(), location.getZ());
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
    public Entity createEntity(Location location, EntityType entityType) {
        Entity bukkitEntity = null;
        try {
            Class<? extends Entity> entityClass = entityType.getEntityClass();
            CraftWorld craftWorld = (CraftWorld)location.getWorld();
            net.minecraft.world.entity.Entity newEntity = craftWorld.createEntity(location, entityClass);
            if (newEntity != null) {
                bukkitEntity = newEntity.getBukkitEntity();
                if (bukkitEntity == null || !entityClass.isAssignableFrom(bukkitEntity.getClass())) return null;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return bukkitEntity;
    }

    @Override
    public boolean addToWorld(World world, Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        ServerLevel level = ((CraftWorld)world).getHandle();
        net.minecraft.world.entity.Entity entityHandle = ((CraftEntity)entity).getHandle();
        level.addEntity(entityHandle, reason);
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

            net.minecraft.world.entity.Entity sourceHandle = ((CraftEntity)source).getHandle();

            // Bukkit won't allow magic damage from anything but a potion..
            if (sourceHandle != null && source instanceof LivingEntity) {
                Location location = target.getLocation();

                ThrownPotion potion = getOrCreatePotionEntity(location);
                net.minecraft.world.entity.Entity potionHandle = ((CraftEntity)potion).getHandle();
                potion.setShooter((LivingEntity) source);

                DamageSource magicSource = DamageSource.indirectMagic(potionHandle, sourceHandle);

                // This is a bit of hack that lets us damage the ender dragon, who is a weird and annoying collection
                // of various non-living entity pieces.
                ((EntityDamageSource)magicSource).setThorns();

                try (EnteredStateTracker.Touchable damaging = DAMAGING.enter()) {
                    damaging.touch();
                    targetHandle.hurt(magicSource, (float)amount);
                }
            } else {
                try (EnteredStateTracker.Touchable damaging = DAMAGING.enter()) {
                    damaging.touch();
                    targetHandle.hurt(DamageSource.MAGIC, (float)amount);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected DamageSource getDamageSource(String damageType) {
        switch (damageType.toUpperCase()) {
            case "IN_FIRE" : return DamageSource.IN_FIRE;
            case "LIGHTNING_BOLT" : return DamageSource.LIGHTNING_BOLT;
            case "ON_FIRE" : return DamageSource.ON_FIRE;
            case "LAVA" : return DamageSource.LAVA;
            case "HOT_FLOOR" : return DamageSource.HOT_FLOOR;
            case "IN_WALL" : return DamageSource.IN_WALL;
            case "CRAMMING" : return DamageSource.CRAMMING;
            case "DROWN" : return DamageSource.DROWN;
            case "STARVE" : return DamageSource.STARVE;
            case "CACTUS" : return DamageSource.CACTUS;
            case "FALL" : return DamageSource.FALL;
            case "FLY_INTO_WALL" : return DamageSource.FLY_INTO_WALL;
            case "OUT_OF_WORLD" : return DamageSource.OUT_OF_WORLD;
            case "GENERIC" : return DamageSource.GENERIC;
            case "MAGIC" : return DamageSource.MAGIC;
            case "WITHER" : return DamageSource.WITHER;
            case "ANVIL" : return DamageSource.ANVIL;
            case "FALLING_BLOCK" : return DamageSource.FALLING_BLOCK;
            case "DRAGON_BREATH" : return DamageSource.DRAGON_BREATH;
            case "DRY_OUT" : return DamageSource.DRY_OUT;
            case "SWEET_BERRY_BUSH" : return DamageSource.SWEET_BERRY_BUSH;
            case "FREEZE" : return DamageSource.FREEZE;
            case "FALLING_STALACTITE" : return DamageSource.FALLING_STALACTITE;
            case "STALAGMITE" : return DamageSource.STALAGMITE;
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
        DamageSource damageSource = getDamageSource(damageType);
        if (damageSource == null) {
            magicDamage(target, amount, source);
            return;
        }
        net.minecraft.world.entity.Entity targetHandle = ((CraftEntity)target).getHandle();
        if (targetHandle == null) return;

        try (EnteredStateTracker.Touchable damaging = DAMAGING.enter()) {
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
        return world.getTileEntity(new BlockPos(location.getX(), location.getY(), location.getZ()), false);
    }

    @Override
    public CompoundTag getTileEntityData(Location location) {
        if (location == null) return null;
        BlockEntity tileEntity = getTileEntity(location);
        if (tileEntity == null) return null;
        CompoundTag tag = new CompoundTag();
        tileEntity.save(tag);
        return tag;
    }

    @Override
    public void setTileEntityData(Location location, Object data) {
        if (location == null || data == null || !(data instanceof CompoundTag)) return;
        BlockEntity tileEntity = getTileEntity(location);
        if (tileEntity == null) return;
        CompoundTag tag = (CompoundTag)data;
        tag.putInt("x", location.getBlockX());
        tag.putInt("y", location.getBlockY());
        tag.putInt("z", location.getBlockZ());
        tileEntity.load(tag);
        tileEntity.setChanged();
    }

    @Override
    public void clearItems(Location location) {
        if (location == null) return;
        BlockEntity tileEntity = getTileEntity(location);
        if (tileEntity == null) return;
        CompoundTag tag = new CompoundTag();
        tileEntity.save(tag);
        // Is there really not an enum for these NBT types?
        ListTag itemList = tag.getList("Items", CompatibilityConstants.NBT_TYPE_COMPOUND);
        // Is it really necessary to clear the list before removing it?
        if (itemList != null) {
            itemList.clear();
        }
        tag.remove("Items");
        tileEntity.load(tag);
        tileEntity.setChanged();

        // Clear loot tables
        Block block = location.getBlock();
        BlockState blockState = block.getState();
        if (blockState instanceof Lootable) {
            Lootable lootable = (Lootable)blockState;
            lootable.setLootTable(null);
            blockState.update();
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
    public byte getBlockData(FallingBlock falling) {
        return 0;
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
        if (!(block instanceof Lockable)) return false;
        Lockable lockable = (Lockable)block;
        lockable.setLock(lockName);
        return true;
    }

    @Override
    public boolean clearLock(Block block) {
        if (!(block instanceof Lockable)) return false;
        Lockable lockable = (Lockable)block;
        lockable.setLock(null);
        return true;
    }

    @Override
    public boolean isLocked(Block block) {
        if (!(block instanceof Lockable)) return false;
        Lockable lockable = (Lockable)block;
        return lockable.isLocked();
    }

    @Override
    public String getLock(Block block) {
        if (!(block instanceof Lockable)) return null;
        Lockable lockable = (Lockable)block;
        return lockable.getLock();
    }

    @Override
    public void setFallingBlockDamage(FallingBlock entity, float fallHurtAmount, int fallHurtMax) {
        entity.setHurtEntities(true);
        FallingBlockEntity nms = (FallingBlockEntity)((CraftEntity)entity).getHandle();
        ReflectionUtils.setPrivateNeedsFixing(platform.getLogger(), nms, FallingBlockEntity.class, "fallDamagePerDistance", fallHurtAmount);
        ReflectionUtils.setPrivateNeedsFixing(platform.getLogger(), nms, FallingBlockEntity.class, "fallDamageMax", fallHurtMax);
    }

    @Override
    public void setInvisible(ArmorStand armorStand, boolean invisible) {
        armorStand.setInvisible(invisible);
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
        ReflectionUtils.setPrivateNeedsFixing(platform.getLogger(), nms.connection, ServerGamePacketListenerImpl.class, "aboveGroundTickCount", -ticks);
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
                nmsProjectile = entityType == null ? constructor.newInstance(nmsWorld) : constructor.newInstance(entityType, nmsWorld);
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

                double dx = speed * (direction.getX() + (random.nextGaussian() * spreadWeight));
                double dy = speed * (direction.getY() + (random.nextGaussian() * spreadWeight));
                double dz = speed * (direction.getZ() + (random.nextGaussian() * spreadWeight));

                fireballIsh.xPower = dx * 0.1D;
                fireballIsh.yPower = dy * 0.1D;
                fireballIsh.zPower = dz * 0.1D;
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

            nmsWorld.addEntity(nmsEntity, CreatureSpawnEvent.SpawnReason.DEFAULT);
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
    public String getResourcePack(Server server) {
        return ((CraftServer)server).getServer().getResourcePack();
    }

    @Override
    public boolean setResourcePack(Player player, String rp, byte[] hash) {
        player.setResourcePack(rp, hash);
        return true;
    }

    @Override
    public boolean removeItemAttribute(ItemStack item, Attribute attribute) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        if (!meta.removeAttributeModifier(attribute)) {
            return false;
        }
        item.setItemMeta(meta);
        return true;
    }

    @Override
    public boolean removeItemAttributes(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
        if (modifiers == null || modifiers.isEmpty()) {
            return false;
        }
        for (Attribute attribute : modifiers.keySet()) {
            meta.removeAttributeModifier(attribute);
        }
        item.setItemMeta(meta);
        return true;
    }

    @Override
    public boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, int attributeOperation, UUID attributeUUID) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        try {
            AttributeModifier.Operation operation;
            try {
                operation = AttributeModifier.Operation.values()[attributeOperation];
            } catch (Throwable ex) {
                platform.getLogger().warning("[Magic] invalid attribute operation ordinal: " + attributeOperation);
                return false;
            }
            AttributeModifier modifier;
            if (slot != null && !slot.isEmpty()) {
                EquipmentSlot equipmentSlot;
                try {
                    if (slot.equalsIgnoreCase("mainhand")) {
                        equipmentSlot = EquipmentSlot.HAND;
                    } else if (slot.equalsIgnoreCase("offhand")) {
                        equipmentSlot = EquipmentSlot.OFF_HAND;
                    } else {
                        equipmentSlot = EquipmentSlot.valueOf(slot.toUpperCase());
                    }
                } catch (Throwable ex) {
                    platform.getLogger().warning("[Magic] invalid attribute slot: " + slot);
                    return false;
                }

                modifier = new AttributeModifier(attributeUUID, "Equipment Modifier", value, operation, equipmentSlot);
            } else {
                modifier = new AttributeModifier(attributeUUID, "Equipment Modifier", value, operation);
            }
            meta.addAttributeModifier(attribute, modifier);
            item.setItemMeta(meta);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void sendExperienceUpdate(Player player, float experience, int level) {
        player.sendExperienceChange(experience, level);
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
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }

    @Override
    public boolean sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        return true;
    }

    @Override
    public float getDurability(Material material) {
        return CraftMagicNumbers.getBlock(material).getExplosionResistance();
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
            BlockPos blockPosition = new BlockPos(location.getX(), location.getY(), location.getZ());
            Packet<?> packet = new ClientboundBlockDestructionPacket((int)id, blockPosition, breakAmount);
            sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Set<String> getTags(Entity entity) {
        return entity.getScoreboardTags();
    }

    @Override
    public boolean isJumping(LivingEntity entity) {
        net.minecraft.world.entity.LivingEntity living = ((CraftLivingEntity)entity).getHandle();
        return (boolean)ReflectionUtils.getPrivateNeedsFixing(platform.getLogger(), living, net.minecraft.world.entity.LivingEntity.class, "jumping");
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
    public boolean setBlockFast(Chunk chunk, int x, int y, int z, Material material, int data) {
        LevelChunk nmsChunk = ((CraftChunk)chunk).getHandle();
        net.minecraft.world.level.block.Block block = CraftMagicNumbers.getBlock(material);
        BlockPos blockLocation = new BlockPos(x, y, z);
        nmsChunk.setBlockState(blockLocation, block.defaultBlockState(), false);
        return true;
    }

    @Override
    public boolean setPickupStatus(Arrow arrow, String pickupStatus) {
        AbstractArrow.PickupStatus status;
        try {
            status = AbstractArrow.PickupStatus.valueOf(pickupStatus.toUpperCase());
        } catch (Throwable ex) {
            platform.getLogger().warning("Invalid pickup status: " + pickupStatus);
            return false;
        }
        arrow.setPickupStatus(status);
        return true;
    }

    @Override
    public Block getHitBlock(ProjectileHitEvent event) {
        return event.getHitBlock();
    }

    @Override
    public Entity getEntity(World world, UUID uuid) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftWorld)world).getHandle().getEntity(uuid);
        return nmsEntity == null ? null : nmsEntity.getBukkitEntity();
    }

    @Override
    public Entity getEntity(UUID uuid) {
        return Bukkit.getEntity(uuid);
    }

    @Override
    public boolean canRemoveRecipes() {
        return true;
    }

    @Override
    public boolean removeRecipe(Recipe recipe) {
        if (!(recipe instanceof Keyed)) {
            return false;
        }
        Keyed keyed = (Keyed)recipe;
        return platform.getPlugin().getServer().removeRecipe(keyed.getKey());
    }

    @Override
    public boolean removeRecipe(String key) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key.toLowerCase());
        return platform.getPlugin().getServer().removeRecipe(namespacedKey);
    }

    @Override
    public ShapedRecipe createShapedRecipe(String key, ItemStack item) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key.toLowerCase());
        return new ShapedRecipe(namespacedKey, item);
    }

    @Override
    public boolean discoverRecipe(HumanEntity entity, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key.toLowerCase());
        return entity.discoverRecipe(namespacedKey);
    }

    @Override
    public boolean undiscoverRecipe(HumanEntity entity, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key.toLowerCase());
        return entity.undiscoverRecipe(namespacedKey);
    }

    @Override
    public double getMaxHealth(Damageable li) {
        if (li instanceof LivingEntity) {
            return ((LivingEntity)li).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        }
        return 0;
    }

    @Override
    public void setMaxHealth(Damageable li, double maxHealth) {
        if (li instanceof LivingEntity) {
            ((LivingEntity)li).getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public Material fromLegacy(org.bukkit.material.MaterialData materialData) {
        Material converted = Bukkit.getUnsafe().fromLegacy(materialData);
        if (converted == Material.AIR) {
            materialData.setData((byte)0);
            converted = Bukkit.getUnsafe().fromLegacy(materialData);
        }
        // Converting legacy signs doesn't seem to work
        // This fixes them, but the direction is wrong, and restoring text causes internal errors
        // So I guess it's best to just let signs be broken for now.
            /*
            if (converted == Material.AIR) {
                String typeKey = materialData.getItemType().name();
                if (typeKey.equals("LEGACY_WALL_SIGN")) return Material.WALL_SIGN;
                if (typeKey.equals("LEGACY_SIGN_POST")) return Material.SIGN_POST;
                if (typeKey.equals("LEGACY_SIGN")) return Material.SIGN;
            }
            */
        return converted;
    }

    @Override
    public boolean hasLegacyMaterials() {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isLegacy(Material material) {
        return material.isLegacy();
    }

    @Override
    public Material getLegacyMaterial(String materialName) {
        return Material.getMaterial(materialName, true);
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
        BlockPos blockPosition = new BlockPos(location.getX(), location.getY(), location.getZ());
        return BoneMealItem.growCrop(itemStack, nmsWorld, blockPosition);
    }

    @Override
    public Color getColor(PotionMeta meta) {
        return meta.getColor();
    }

    @Override
    public boolean setColor(PotionMeta meta, Color color) {
        meta.setColor(color);
        return true;
    }

    @Override
    public String getBlockData(Material material, byte data) {
        BlockData blockData = platform.getDeprecatedUtils().getUnsafe().fromLegacy(material, data);
        return blockData == null ? null : blockData.getAsString();
    }

    @Override
    public boolean hasBlockDataSupport() {
        return true;
    }

    @Override
    public String getBlockData(Block block) {
        BlockData blockData = block.getBlockData();
        return blockData == null ? null : blockData.getAsString();
    }

    @Override
    public boolean setBlockData(Block block, String data) {
        BlockData blockData = platform.getPlugin().getServer().createBlockData(data);
        block.setBlockData(blockData, false);
        return true;
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
    public boolean addRecipeToBook(ItemStack book, Plugin plugin, String recipeKey) {
        if (book == null) return false;
        ItemMeta meta = book.getItemMeta();
        if (!(meta instanceof KnowledgeBookMeta)) return false;
        KnowledgeBookMeta bookMeta = (KnowledgeBookMeta)meta;
        NamespacedKey key = new NamespacedKey(plugin, recipeKey.toLowerCase());
        bookMeta.addRecipe(key);
        book.setItemMeta(bookMeta);
        return true;
    }

    @Override
    public boolean isPowerable(Block block) {
        BlockData blockData = block.getBlockData();
        return blockData != null && blockData instanceof Powerable;
    }

    @Override
    public boolean isPowered(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData == null || !(blockData instanceof Powerable)) return false;
        Powerable powerable = (Powerable)blockData;
        return powerable.isPowered();
    }

    @Override
    public boolean setPowered(Block block, boolean powered) {
        BlockData blockData = block.getBlockData();
        if (blockData == null || !(blockData instanceof Powerable)) return false;
        Powerable powerable = (Powerable)blockData;
        powerable.setPowered(powered);
        block.setBlockData(powerable, true);
        return true;
    }

    @Override
    public boolean isWaterLoggable(Block block) {
        BlockData blockData = block.getBlockData();
        return blockData != null && blockData instanceof Waterlogged;
    }

    @Override
    public boolean setWaterlogged(Block block, boolean waterlogged) {
        BlockData blockData = block.getBlockData();
        if (blockData == null || !(blockData instanceof Waterlogged)) return false;
        Waterlogged waterlogger = (Waterlogged)blockData;
        waterlogger.setWaterlogged(waterlogged);
        block.setBlockData(waterlogger, true);
        return true;
    }

    @Override
    public boolean setTopHalf(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData == null || !(blockData instanceof Bisected)) return false;
        Bisected bisected = (Bisected)blockData;
        bisected.setHalf(Bisected.Half.TOP);
        block.setBlockData(bisected, false);
        return true;
    }

    @Override
    public boolean stopSound(Player player, Sound sound) {
        player.stopSound(sound);
        return true;
    }

    @Override
    public boolean stopSound(Player player, String sound) {
        player.stopSound(sound);
        return true;
    }

    @Override
    public boolean lockChunk(Chunk chunk) {
        if (!platform.getPlugin().isEnabled()) return false;
        if (!chunk.isLoaded()) {
            platform.getLogger().info("Locking unloaded chunk");
        }
        chunk.addPluginChunkTicket(platform.getPlugin());
        return true;
    }

    @Override
    public boolean unlockChunk(Chunk chunk) {
        if (!platform.getPlugin().isEnabled()) return false;
        chunk.removePluginChunkTicket(platform.getPlugin());
        return true;
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
    public boolean setRecipeGroup(ShapedRecipe recipe, String group) {
        recipe.setGroup(group);
        return true;
    }

    @Override
    public boolean isSameKey(Plugin plugin, String key, Object keyedObject) {
        if (!(keyedObject instanceof Keyed)) return false;
        String namespace = plugin.getName().toLowerCase(Locale.ROOT);
        key = key.toLowerCase(Locale.ROOT);
        Keyed keyed = (Keyed)keyedObject;
        NamespacedKey namespacedKey = keyed.getKey();
        String keyNamespace = namespacedKey.getNamespace();
        String keyKey = namespacedKey.getKey();
        return keyNamespace.equals(namespace) && keyKey.equals(key);
    }

    @Override
    public boolean isLegacyRecipes() {
        return false;
    }

    @Override
    public boolean setRecipeIngredient(ShapedRecipe recipe, char key, ItemStack ingredient, boolean ignoreDamage) {
        if (ingredient == null) return false;
        try {
            short maxDurability = ingredient.getType().getMaxDurability();
            if (ignoreDamage && maxDurability > 0) {
                List<ItemStack> damaged = new ArrayList<>();
                for (short damage = 0 ; damage < maxDurability; damage++) {
                    ingredient = ingredient.clone();
                    ItemMeta meta = ingredient.getItemMeta();
                    if (meta == null || !(meta instanceof org.bukkit.inventory.meta.Damageable))  break;
                    org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable)meta;
                    damageable.setDamage(damage);
                    ingredient.setItemMeta(meta);
                    damaged.add(ingredient);
                }
                RecipeChoice.ExactChoice exactChoice = new RecipeChoice.ExactChoice(damaged);
                recipe.setIngredient(key, exactChoice);
                return true;
            }

            RecipeChoice.ExactChoice exactChoice = new RecipeChoice.ExactChoice(ingredient);
            recipe.setIngredient(key, exactChoice);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
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
        ((CraftBlock)block).setTypeAndData(state, physics);
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
    public int getPhantomSize(Entity entity) {
        if (entity == null || !(entity instanceof Phantom)) return 0;
        return ((Phantom)entity).getSize();
    }

    @Override
    public boolean setPhantomSize(Entity entity, int size) {
        if (entity == null || !(entity instanceof Phantom)) return false;
        ((Phantom)entity).setSize(size);
        return true;
    }

    @Override
    public Location getBedSpawnLocation(Player player) {
        if (player == null) {
            return null;
        }
        ServerPlayer nmsPlayer = ((CraftPlayer)player).getHandle();
        BlockPos bedLocation = nmsPlayer.getRespawnPosition();
        ResourceKey<net.minecraft.world.level.Level> bedDimension = nmsPlayer.getRespawnDimension();
        if (bedLocation != null && bedDimension != null) {
            MinecraftServer server = nmsPlayer.getServer();
            ServerLevel worldServer = server != null ? server.getLevel(bedDimension) : null;
            World world = worldServer != null ? worldServer.getWorld() : null;
            if (world != null) {
                return new Location(world, bedLocation.getX(), bedLocation.getY(), bedLocation.getZ());
            }
        }
        return player.getBedSpawnLocation();
    }

    /**
     * This will load chunks asynchronously if possible.
     *
     * But note that it will never be truly asynchronous, it is important not to call this in a tight retry loop,
     * the main server thread needs to free up to actually process the async chunk loads.
     */
    @Override
    public void loadChunk(World world, int x, int z, boolean generate, Consumer<Chunk> consumer) {
        PaperUtils paperUtils = platform.getPaperUtils();
        if (paperUtils == null) {
            Chunk chunk = world.getChunkAt(x, z);
            chunk.load();
            if (consumer != null) {
                consumer.accept(chunk);
            }
            return;
        }

        final LoadingChunk loading = new LoadingChunk(world, x, z);
        Integer requestCount = loadingChunks.get(loading);
        if (requestCount != null) {
            requestCount++;
            if (requestCount > MAX_CHUNK_LOAD_TRY) {
                platform.getLogger().warning("Exceeded retry count for asynchronous chunk load, loading synchronously");
                if (!hasDumpedStack) {
                    hasDumpedStack = true;
                    Thread.dumpStack();
                }
                Chunk chunk = world.getChunkAt(x, z);
                chunk.load();
                if (consumer != null) {
                    consumer.accept(chunk);
                }
                loadingChunks.remove(loading);
                return;
            }
            loadingChunks.put(loading, requestCount);
            return;
        }

        loadingChunks.put(loading, 1);
        paperUtils.loadChunk(world, x, z, generate, chunk -> {
            loadingChunks.remove(loading);
            if (consumer != null) {
                consumer.accept(chunk);
            }
        });
    }

    @Override
    public void addPassenger(Entity vehicle, Entity passenger) {
        vehicle.addPassenger(passenger);
    }

    @Override
    public List<Entity> getPassengers(Entity entity) {
        return entity.getPassengers();
    }

    @Override
    public boolean openBook(Player player, ItemStack itemStack) {
        player.openBook(itemStack);
        return true;
    }

    @Override
    public boolean isHandRaised(Player player) {
        return player.isHandRaised();
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
                ReflectionUtils.setPrivateNeedsFixing(platform.getLogger(), fireworkHandle, FireworkRocketEntity.class, "life", ticksFlown);
            }
            if (expectedLifespan != null) {
                fireworkHandle.lifetime = expectedLifespan;
            }

            if (direction == null) {
                ClientboundAddEntityPacket fireworkPacket = new ClientboundAddEntityPacket(fireworkHandle, CompatibilityConstants.FIREWORK_TYPE);
                int fireworkId = fireworkHandle.getId();
                SynchedEntityData watcher = fireworkHandle.getEntityData();
                ClientboundSetEntityDataPacket metadataPacket = new ClientboundSetEntityDataPacket(fireworkId, watcher, true);
                ClientboundEntityEventPacket statusPacket = new ClientboundEntityEventPacket(fireworkHandle, (byte)17);
                ClientboundRemoveEntityPacket destroyPacket = new ClientboundRemoveEntityPacket(fireworkId);

                Collection<? extends Player> players = server.getOnlinePlayers();
                sendPacket(server, location, players, fireworkPacket);
                sendPacket(server, location, players, metadataPacket);
                sendPacket(server, location, players, statusPacket);
                sendPacket(server, location, players, destroyPacket);
                return null;
            }

            level.addEntity(fireworkHandle, CreatureSpawnEvent.SpawnReason.CUSTOM);
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
        Set<String> keys = platform.getInventoryUtils().getTagKeys(tag);
        if (keys == null) return false;
        for (String tagName : keys) {
            Tag metaBase = compoundTag.get(tagName);
            if (metaBase != null) {
                if (metaBase instanceof CompoundTag) {
                    ConfigurationSection newSection = tags.createSection(tagName);
                    loadAllTagsFromNBT(newSection, metaBase);
                } else {
                    try {
                        tags.set(tagName, platform.getInventoryUtils().getTagValue(metaBase));
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
    public BoundingBox getHitbox(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity)entity).getHandle();
        AABB aabb = nmsEntity.getBoundingBox();
        if (aabb == null) {
            return null;
        }
        return new BoundingBox(
                aabb.minX,
                aabb.maxX,
                aabb.minY,
                aabb.maxY,
                aabb.minZ,
                aabb.maxZ
        );
    }

    @Override
    public boolean isPrimaryThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public String getEnchantmentKey(Enchantment enchantment) {
        return enchantment.getKey().getKey();
    }
}
