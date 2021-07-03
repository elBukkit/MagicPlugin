package com.elmakers.mine.bukkit.utility.platform.legacy;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
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
import java.util.logging.Level;

import org.bukkit.Art;
import org.bukkit.Bukkit;
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
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;
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
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.map.MapView;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.bukkit.material.RedstoneWire;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.DoorActionType;
import com.elmakers.mine.bukkit.utility.EnteredStateTracker.Touchable;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.CompatibilityUtilsBase;
import com.google.common.io.BaseEncoding;

/**
 * A generic place to put compatibility-based utilities.
 *
 * <p>These are generally here when there is a new method added
 * to the Bukkti API we'd like to use, but aren't quite
 * ready to give up backwards compatibility.
 *
 * <p>The easy solution to this problem is to shamelessly copy
 * Bukkit's code in here, mark it as deprecated and then
 * switch everything over once the new Bukkit method is in an
 * official release.
 */
@SuppressWarnings("deprecation")
public class CompatibilityUtils extends CompatibilityUtilsBase {
    public static int OFFHAND_BROADCAST_RANGE = 32;
    private final WeakReference<Thread> primaryThread;

    public CompatibilityUtils(Platform platform) {
        super(platform);
        primaryThread = new WeakReference<>(Thread.currentThread());
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

        // TODO: Is this even still necessary?
        if (NMSUtils.class_CraftInventoryCustom_constructor == null) {
            return Bukkit.createInventory(holder, size, shorterName);
        }
        Inventory inventory = null;
        try {
            inventory = (Inventory) NMSUtils.class_CraftInventoryCustom_constructor.newInstance(holder, size, shorterName);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return inventory;
    }

    @Override
    public boolean isInvulnerable(Entity entity) {
        if (NMSUtils.class_Entity_invulnerableField == null) return false;
        try {
            Object handle = NMSUtils.getHandle(entity);
            return (boolean) NMSUtils.class_Entity_invulnerableField.get(handle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void setInvulnerable(Entity entity, boolean flag) {
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_invulnerableField.set(handle, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isSilent(Entity entity) {
        if (NMSUtils.class_Entity_isSilentMethod == null) return false;
        try {
            Object handle = NMSUtils.getHandle(entity);
            return (boolean) NMSUtils.class_Entity_isSilentMethod.invoke(handle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void setSilent(Entity entity, boolean flag) {
        if (NMSUtils.class_Entity_setSilentMethod == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_setSilentMethod.invoke(handle, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setSilent(Object nmsEntity, boolean flag) {
        if (NMSUtils.class_Entity_setSilentMethod == null) return;
        try {
            NMSUtils.class_Entity_setSilentMethod.invoke(nmsEntity, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isPersist(Entity entity) {
        if (NMSUtils.class_Entity_persistField == null) return false;
        try {
            Object handle = NMSUtils.getHandle(entity);
            return (boolean) NMSUtils.class_Entity_persistField.get(handle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void setRemoveWhenFarAway(Entity entity, boolean flag) {
        if (NMSUtils.class_LivingEntity_setRemoveWhenFarAway == null || !(entity instanceof LivingEntity)) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_LivingEntity_setRemoveWhenFarAway.invoke(entity, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setPersist(Entity entity, boolean flag) {
        if (NMSUtils.class_Entity_persistField == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_persistField.set(handle, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isSitting(Entity entity) {
        if (NMSUtils.class_Sittable == null) return false;
        if (!NMSUtils.class_Sittable.isAssignableFrom(entity.getClass())) return false;
        try {
            return (boolean) NMSUtils.class_Sitting_isSittingMethod.invoke(entity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void setSitting(Entity entity, boolean flag) {
        if (NMSUtils.class_Sittable == null) return;
        if (!NMSUtils.class_Sittable.isAssignableFrom(entity.getClass())) return;
        try {
            NMSUtils.class_Sitting_setSittingMethod.invoke(entity, flag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Painting createPainting(Location location, BlockFace facing, Art art) {
        Painting newPainting = null;
        try {
            Object worldHandle = NMSUtils.getHandle(location.getWorld());
            Object newEntity = null;
            @SuppressWarnings("unchecked")
            Enum<?> directionEnum = Enum.valueOf(NMSUtils.class_EnumDirection, facing.name());
            Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
            newEntity = NMSUtils.class_EntityPaintingConstructor.newInstance(worldHandle, blockLocation, directionEnum);
            if (newEntity != null) {
                if (NMSUtils.class_EntityPainting_art != null) {
                    Object notchArt = NMSUtils.class_CraftArt_NotchToBukkitMethod.invoke(null, art);
                    NMSUtils.class_EntityPainting_art.set(newEntity, notchArt);
                }
                Entity bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof Painting)) return null;

                newPainting = (Painting) bukkitEntity;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            newPainting = null;
        }
        return newPainting;
    }

    @Override
    public ItemFrame createItemFrame(Location location, BlockFace facing, Rotation rotation, ItemStack item) {
        ItemFrame newItemFrame = null;
        try {
            Object worldHandle = NMSUtils.getHandle(location.getWorld());
            Object newEntity = null;
            @SuppressWarnings("unchecked")
            Enum<?> directionEnum = Enum.valueOf(NMSUtils.class_EnumDirection, facing.name());
            Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
            newEntity = NMSUtils.class_EntityItemFrameConstructor.newInstance(worldHandle, blockLocation, directionEnum);
            if (newEntity != null) {
                Entity bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !(bukkitEntity instanceof ItemFrame)) return null;

                newItemFrame = (ItemFrame) bukkitEntity;
                newItemFrame.setItem(platform.getItemUtils().getCopy(item));
                newItemFrame.setRotation(rotation);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return newItemFrame;
    }

    @Override
    public Entity createEntity(Location location, EntityType entityType) {
        Entity bukkitEntity = null;
        try {
            Class<? extends Entity> entityClass = entityType.getEntityClass();
            Object newEntity = NMSUtils.class_CraftWorld_createEntityMethod.invoke(location.getWorld(), location, entityClass);
            if (newEntity != null) {
                bukkitEntity = getBukkitEntity(newEntity);
                if (bukkitEntity == null || !entityClass.isAssignableFrom(bukkitEntity.getClass())) return null;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return bukkitEntity;
    }

    @Override
    public boolean addToWorld(World world, Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        try {
            Object worldHandle = NMSUtils.getHandle(world);
            Object entityHandle = NMSUtils.getHandle(entity);
            NMSUtils.class_World_addEntityMethod.invoke(worldHandle, entityHandle, reason);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        if (location == null) return null;
        Object worldHandle = NMSUtils.getHandle(location.getWorld());
        try {
            x = Math.min(x, MAX_ENTITY_RANGE);
            z = Math.min(z, MAX_ENTITY_RANGE);
            Object bb = NMSUtils.class_AxisAlignedBB_Constructor.newInstance(location.getX() - x, location.getY() - y, location.getZ() - z,
                    location.getX() + x, location.getY() + y, location.getZ() + z);

            // The input entity is only used for equivalency testing, so this "null" should be ok.
            @SuppressWarnings("unchecked")
            List<? extends Object> entityList = (List<? extends Object>) NMSUtils.class_World_getEntitiesMethod.invoke(worldHandle, null, bb);
            List<Entity> bukkitEntityList = new java.util.ArrayList<>(entityList.size());

            for (Object entity : entityList) {
                Entity bukkitEntity = (Entity) NMSUtils.class_Entity_getBukkitEntityMethod.invoke(entity);
                if (bukkitEntity instanceof ComplexLivingEntity) {
                    ComplexLivingEntity complex = (ComplexLivingEntity) bukkitEntity;
                    Set<ComplexEntityPart> parts = complex.getParts();
                    for (ComplexEntityPart part : parts) {
                        bukkitEntityList.add(part);
                    }
                } else {
                    bukkitEntityList.add(bukkitEntity);
                }
            }
            return bukkitEntityList;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Runnable getTaskRunnable(BukkitTask task) {
        Runnable runnable = null;
        try {
            Field taskField = NMSUtils.class_CraftTask.getDeclaredField("task");
            taskField.setAccessible(true);
            runnable = (Runnable) taskField.get(task);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return runnable;
    }

    @Override
    public void ageItem(Item item, int ticksToAge) {
        try {
            Class<?> itemClass = NMSUtils.fixBukkitClass("net.minecraft.server.EntityItem");
            Object handle = NMSUtils.getHandle(item);
            Field ageField = itemClass.getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.set(handle, ticksToAge);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void magicDamage(Damageable target, double amount, Entity source) {
        try {
            if (target == null || target.isDead()) return;

            if (NMSUtils.class_EntityLiving_damageEntityMethod == null || NMSUtils.object_magicSource == null || NMSUtils.class_DamageSource_getMagicSourceMethod == null) {
                damage(target, amount, source);
                return;
            }

            // Special-case for witches .. witches are immune to magic damage :\
            // And endermen are immune to indirect damage .. or something.
            // Also armor stands suck.
            // Might need to config-drive this, or just go back to defaulting to normal damage
            if (!USE_MAGIC_DAMAGE || target instanceof Witch || target instanceof Enderman || target instanceof ArmorStand || !(target instanceof LivingEntity)) {
                damage(target, amount, source);
                return;
            }

            Object targetHandle = NMSUtils.getHandle(target);
            if (targetHandle == null) return;

            Object sourceHandle = NMSUtils.getHandle(source);

            // Bukkit won't allow magic damage from anything but a potion..
            if (sourceHandle != null && source instanceof LivingEntity) {
                Location location = target.getLocation();

                ThrownPotion potion = getOrCreatePotionEntity(location);
                potion.setShooter((LivingEntity) source);

                Object potionHandle = NMSUtils.getHandle(potion);
                Object damageSource = NMSUtils.class_DamageSource_getMagicSourceMethod.invoke(null, potionHandle, sourceHandle);

                // This is a bit of hack that lets us damage the ender dragon, who is a weird and annoying collection
                // of various non-living entity pieces.
                if (NMSUtils.class_EntityDamageSource_setThornsMethod != null) {
                    NMSUtils.class_EntityDamageSource_setThornsMethod.invoke(damageSource);
                }

                try (Touchable damaging = isDamaging.enter()) {
                    damaging.touch();
                    NMSUtils.class_EntityLiving_damageEntityMethod.invoke(
                            targetHandle,
                            damageSource,
                            (float) amount);
                }
            } else {
                try (Touchable damaging = isDamaging.enter()) {
                    damaging.touch();
                    NMSUtils.class_EntityLiving_damageEntityMethod.invoke(
                            targetHandle,
                            NMSUtils.object_magicSource,
                            (float) amount);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
        Object damageSource = (NMSUtils.damageSources == null) ? null : NMSUtils.damageSources.get(damageType.toUpperCase());
        if (damageSource == null || NMSUtils.class_EntityLiving_damageEntityMethod == null) {
            magicDamage(target, amount, source);
            return;
        }

        try (Touchable damaging = isDamaging.enter()) {
            damaging.touch();
            Object targetHandle = NMSUtils.getHandle(target);
            if (targetHandle == null) return;

            NMSUtils.class_EntityLiving_damageEntityMethod.invoke(targetHandle, damageSource, (float) amount);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isReady(Chunk chunk) {
        if (NMSUtils.class_Chunk_isReadyMethod == null) return true;

        Object chunkHandle = NMSUtils.getHandle(chunk);
        boolean ready = true;
        try {
            ready = (Boolean) NMSUtils.class_Chunk_isReadyMethod.invoke(chunkHandle);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return ready;
    }

    @Override
    public boolean createExplosion(Entity entity, World world, double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
        boolean result = false;
        if (world == null) return false;
        if (NMSUtils.class_World_explodeMethod == null) {
            return world.createExplosion(x, y, z, power, setFire, breakBlocks);
        }
        try {
            Object worldHandle = NMSUtils.getHandle(world);
            if (worldHandle == null) return false;
            Object entityHandle = entity == null ? null : NMSUtils.getHandle(entity);

            Object explosion = NMSUtils.class_EnumExplosionEffect != null
                    ? NMSUtils.class_World_explodeMethod.invoke(worldHandle, entityHandle, x, y, z, power, setFire, breakBlocks ? NMSUtils.enum_ExplosionEffect_BREAK : NMSUtils.enum_ExplosionEffect_NONE)
                    : NMSUtils.class_World_explodeMethod.invoke(worldHandle, entityHandle, x, y, z, power, setFire, breakBlocks);
            Field cancelledField = explosion.getClass().getDeclaredField("wasCanceled");
            result = (Boolean)cancelledField.get(explosion);
        } catch (Throwable ex) {
            ex.printStackTrace();
            result = false;
        }
        return result;
    }

    @Override
    public Object getTileEntityData(Location location) {
       if (NMSUtils.class_TileEntity_saveMethod == null) return null;
        Object tileEntity = getTileEntity(location);
        if (tileEntity == null) return null;
        Object data = null;
        try {
            data = NMSUtils.class_NBTTagCompound_constructor.newInstance();
            NMSUtils.class_TileEntity_saveMethod.invoke(tileEntity, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }

    @Override
    public Object getTileEntity(Location location) {
        if (NMSUtils.class_World_getTileEntityMethod != null) {
            Object tileEntity = null;
            try {
                World world = location.getWorld();
                Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
                tileEntity = NMSUtils.class_World_getTileEntityMethod.invoke(NMSUtils.getHandle(world), blockLocation);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return tileEntity;
        }

        if (NMSUtils.class_CraftWorld_getTileEntityAtMethod == null) return null;
        Object tileEntity = null;
        try {
            World world = location.getWorld();
            tileEntity = NMSUtils.class_CraftWorld_getTileEntityAtMethod.invoke(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return tileEntity;
    }

    @Override
    public void clearItems(Location location) {
        if (NMSUtils.class_TileEntity_loadMethod == null || NMSUtils.class_TileEntity_updateMethod == null || NMSUtils.class_TileEntity_saveMethod == null) return;
        if (location == null) return;
        Object tileEntity = getTileEntity(location);
        if (tileEntity == null) return;
        try {
            Object entityData = NMSUtils.class_NBTTagCompound_constructor.newInstance();
            NMSUtils.class_TileEntity_saveMethod.invoke(tileEntity, entityData);
            Object itemList = NMSUtils.class_NBTTagCompound_getListMethod.invoke(entityData, "Items", CompatibilityConstants.NBT_TYPE_COMPOUND);
            if (itemList != null) {
                List<?> items = (List<?>) NMSUtils.class_NBTTagList_list.get(itemList);
                items.clear();
            }
            NMSUtils.class_NBTTagCompound_removeMethod.invoke(entityData,"Item");
            if (NMSUtils.class_IBlockData != null) {
                Object worldHandle = NMSUtils.getHandle(location.getWorld());
                Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                Object blockType = NMSUtils.class_World_getTypeMethod.invoke(worldHandle, blockLocation);
                NMSUtils.class_TileEntity_loadMethod.invoke(tileEntity, blockType, entityData);
            } else {
                NMSUtils.class_TileEntity_loadMethod.invoke(tileEntity, entityData);
            }
            NMSUtils.class_TileEntity_updateMethod.invoke(tileEntity);

            if (NMSUtils.class_Lootable_setLootTableMethod != null && NMSUtils.class_Lootable != null) {
                Block block = location.getBlock();
                BlockState blockState = block.getState();
                if (NMSUtils.class_Lootable.isAssignableFrom(blockState.getClass())) {
                    NMSUtils.class_Lootable_setLootTableMethod.invoke(blockState, new Object[]{ null });
                    blockState.update();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setTileEntityData(Location location, Object data) {
        if (NMSUtils.class_TileEntity_loadMethod == null || NMSUtils.class_TileEntity_updateMethod == null) return;
        if (location == null || data == null) return;
        Object tileEntity = getTileEntity(location);
        if (tileEntity == null) return;
        try {
            NMSUtils.class_NBTTagCompound_setIntMethod.invoke(data, "x", location.getBlockX());
            NMSUtils.class_NBTTagCompound_setIntMethod.invoke(data, "y", location.getBlockY());
            NMSUtils.class_NBTTagCompound_setIntMethod.invoke(data, "z", location.getBlockZ());

            if (NMSUtils.class_IBlockData != null) {
                Object worldHandle = NMSUtils.getHandle(location.getWorld());
                Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                Object blockType = NMSUtils.class_World_getTypeMethod.invoke(worldHandle, blockLocation);
                NMSUtils.class_TileEntity_loadMethod.invoke(tileEntity, blockType, data);
            } else {
                NMSUtils.class_TileEntity_loadMethod.invoke(tileEntity, data);
            }
            NMSUtils.class_TileEntity_updateMethod.invoke(tileEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setEnvironment(World world, World.Environment environment) {
        try {
            NMSUtils.class_CraftWorld_environmentField.set(world, environment);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void playCustomSound(Player player, Location location, String sound, float volume, float pitch)
    {
        if (NMSUtils.class_PacketPlayOutCustomSoundEffect_Constructor == null || sound == null) return;
        try {
            Object packet = null;
            if (NMSUtils.class_MinecraftKey_constructor != null) {
                Object key = NMSUtils.class_MinecraftKey_constructor.newInstance(sound);
                Object vec = NMSUtils.class_Vec3D_constructor.newInstance(location.getX(), location.getY(), location.getZ());
                packet = NMSUtils.class_PacketPlayOutCustomSoundEffect_Constructor.newInstance(key, NMSUtils.enum_SoundCategory_PLAYERS, vec, volume, pitch);
            } else {
                packet = NMSUtils.class_PacketPlayOutCustomSoundEffect_Constructor.newInstance(sound, NMSUtils.enum_SoundCategory_PLAYERS, location.getX(), location.getY(), location.getZ(), volume, pitch);
            }
            NMSUtils.sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Entity> selectEntities(CommandSender sender, String selector) {
        if (NMSUtils.class_Bukkit_selectEntitiesMethod == null) return null;
        if (!selector.startsWith("@")) return null;
        try {
            return (List<Entity>) NMSUtils.class_Bukkit_selectEntitiesMethod.invoke(null, sender, selector);
        } catch (Throwable ex) {
            platform.getLogger().warning("Invalid selector: " + ex.getMessage());
        }
        return null;
    }

    protected Entity getBukkitEntity(Object entity)
    {
        if (entity == null) return null;
        try {
            Method getMethod = entity.getClass().getMethod("getBukkitEntity");
            Object bukkitEntity = getMethod.invoke(entity);
            if (!(bukkitEntity instanceof Entity)) return null;
            return (Entity)bukkitEntity;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public MapView getMapById(int id) {
        if (NMSUtils.class_Bukkit_getMapMethod == null) return null;
        try {
            if (NMSUtils.legacyMaps) {
                return (MapView) NMSUtils.class_Bukkit_getMapMethod.invoke(null, (short)id);
            }
            return (MapView) NMSUtils.class_Bukkit_getMapMethod.invoke(null, (short)id);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getTypedMap(ConfigurationSection section)
    {
        if (section == null) return null;
        if (section instanceof MemorySection && NMSUtils.class_MemorySection_mapField != null)
        {
            try {
                Object mapObject = NMSUtils.class_MemorySection_mapField.get(section);
                if (mapObject instanceof Map) {
                    return (Map<String, T>)mapObject;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
    public boolean setMap(ConfigurationSection section, Map<String, Object> map)
    {
        if (section == null || NMSUtils.class_MemorySection_mapField == null) return false;
        if (section instanceof MemorySection)
        {
            try {
                NMSUtils.class_MemorySection_mapField.set(section, map);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    @Override
    public Vector getPosition(Object entityData, String tag) {
        if (NMSUtils.class_NBTTagList_getDoubleMethod == null) return null;
        try {
            Object posList = NMSUtils.class_NBTTagCompound_getListMethod.invoke(entityData, tag, CompatibilityConstants.NBT_TYPE_DOUBLE);
            Double x = (Double) NMSUtils.class_NBTTagList_getDoubleMethod.invoke(posList, 0);
            Double y = (Double) NMSUtils.class_NBTTagList_getDoubleMethod.invoke(posList, 1);
            Double z = (Double) NMSUtils.class_NBTTagList_getDoubleMethod.invoke(posList, 2);
            if (x != null && y != null && z != null) {
                return new Vector(x, y, z);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public BlockVector getBlockVector(Object entityData, String tag) {
        if (NMSUtils.class_NBTTagCompound_getIntArrayMethod == null) return null;
        try {
            int[] coords = (int[]) NMSUtils.class_NBTTagCompound_getIntArrayMethod.invoke(entityData, tag);
            if (coords == null || coords.length < 3) return null;
            return new BlockVector(coords[0], coords[1], coords[2]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void setTNTSource(TNTPrimed tnt, LivingEntity source)
    {
        try {
            Object tntHandle = NMSUtils.getHandle(tnt);
            Object sourceHandle = NMSUtils.getHandle(source);
            NMSUtils.class_EntityTNTPrimed_source.set(tntHandle, sourceHandle);
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Unable to set TNT source", ex);
        }
    }

    @Override
    public void setEntityMotion(Entity entity, Vector motion) {
        try {
            Object handle = NMSUtils.getHandle(entity);
            if (NMSUtils.class_Entity_motField != null) {
                Object vec = NMSUtils.class_Vec3D_constructor.newInstance(motion.getX(), motion.getY(), motion.getZ());
                NMSUtils.class_Entity_motField.set(handle, vec);
            } else if (NMSUtils.class_Entity_motXField != null) {
                NMSUtils.class_Entity_motXField.set(handle, motion.getX());
                NMSUtils.class_Entity_motYField.set(handle, motion.getY());
                NMSUtils.class_Entity_motZField.set(handle, motion.getZ());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean setLock(Block block, String lockName)
    {
        if (NMSUtils.class_ChestLock_Constructor == null) return false;
        if (NMSUtils.class_TileEntityContainer_setLock == null && NMSUtils.class_TileEntityContainer_lock == null) return false;
        Object tileEntity = getTileEntity(block.getLocation());
        if (tileEntity == null) return false;
        if (!NMSUtils.class_TileEntityContainer.isInstance(tileEntity)) return false;
        try {
            Object lock = NMSUtils.class_ChestLock_Constructor.newInstance(lockName);
            if (NMSUtils.class_TileEntityContainer_lock != null) {
                NMSUtils.class_TileEntityContainer_lock.set(tileEntity, lock);
            } else {
                NMSUtils.class_TileEntityContainer_setLock.invoke(tileEntity, lock);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean clearLock(Block block)
    {
        if (NMSUtils.class_TileEntityContainer_setLock == null && NMSUtils.class_TileEntityContainer_lock == null) return false;
        Object tileEntity = getTileEntity(block.getLocation());
        if (tileEntity == null) return false;
        if (!NMSUtils.class_TileEntityContainer.isInstance(tileEntity)) return false;
        try {
            if (NMSUtils.class_TileEntityContainer_lock != null) {
                if (NMSUtils.object_emptyChestLock == null) {
                    return false;
                }
                NMSUtils.class_TileEntityContainer_lock.set(tileEntity, NMSUtils.object_emptyChestLock);
            } else {
                NMSUtils.class_TileEntityContainer_setLock.invoke(tileEntity, new Object[] {null});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean isLocked(Block block)
    {
        if (NMSUtils.class_TileEntityContainer_getLock == null && NMSUtils.class_TileEntityContainer_lock == null) return false;
        Object tileEntity = getTileEntity(block.getLocation());
        if (tileEntity == null) return false;
        if (!NMSUtils.class_TileEntityContainer.isInstance(tileEntity)) return false;
        try {
            Object lock = NMSUtils.class_TileEntityContainer_lock != null ? NMSUtils.class_TileEntityContainer_lock.get(tileEntity) :
                NMSUtils.class_TileEntityContainer_getLock.invoke(tileEntity);
            if (lock == null) return false;
            String key = NMSUtils.class_ChestLock_key != null ? (String) NMSUtils.class_ChestLock_key.get(lock) :
                (String) NMSUtils.class_ChestLock_getString.invoke(lock);
            return key != null && !key.isEmpty();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public String getLock(Block block)
    {
        if (NMSUtils.class_ChestLock_getString == null && NMSUtils.class_ChestLock_key == null) return null;
        if (NMSUtils.class_TileEntityContainer_getLock == null && NMSUtils.class_TileEntityContainer_lock == null) return null;
        Object tileEntity = getTileEntity(block.getLocation());
        if (tileEntity == null) return null;
        if (!NMSUtils.class_TileEntityContainer.isInstance(tileEntity)) return null;
        try {
            Object lock = NMSUtils.class_TileEntityContainer_lock != null ? NMSUtils.class_TileEntityContainer_lock.get(tileEntity) :
                NMSUtils.class_TileEntityContainer_getLock.invoke(tileEntity);
            if (lock == null) return null;
            return NMSUtils.class_ChestLock_key != null ? (String) NMSUtils.class_ChestLock_key.get(lock) :
                (String) NMSUtils.class_ChestLock_getString.invoke(lock);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void setFallingBlockDamage(FallingBlock entity, float fallHurtAmount, int fallHurtMax)
    {
        Object entityHandle = NMSUtils.getHandle(entity);
        if (entityHandle == null) return;
        try {
            NMSUtils.class_EntityFallingBlock_hurtEntitiesField.set(entityHandle, true);
            NMSUtils.class_EntityFallingBlock_fallHurtAmountField.set(entityHandle, fallHurtAmount);
            NMSUtils.class_EntityFallingBlock_fallHurtMaxField.set(entityHandle, fallHurtMax);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setInvisible(Entity entity, boolean invisible) {
        if (NMSUtils.class_Entity_setInvisible == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_setInvisible.invoke(handle, invisible);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setInvisible(ArmorStand armorStand, boolean invisible) {
        try {
            Object handle = NMSUtils.getHandle(armorStand);
            NMSUtils.class_ArmorStand_setInvisible.invoke(handle, invisible);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Boolean isInvisible(Entity entity) {
        if (NMSUtils.class_Entity_isInvisible == null) return null;
        try {
            Object handle = NMSUtils.getHandle(entity);
            return (boolean) NMSUtils.class_Entity_isInvisible.invoke(handle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void setGravity(ArmorStand armorStand, boolean gravity) {
        if (NMSUtils.class_Entity_setNoGravity == null && NMSUtils.class_ArmorStand_setGravity == null) return;
        try {
            Object handle = NMSUtils.getHandle(armorStand);
            if (NMSUtils.class_Entity_setNoGravity != null) {
                NMSUtils.class_Entity_setNoGravity.invoke(handle, !gravity);
            } else {
                NMSUtils.class_ArmorStand_setGravity.invoke(handle, gravity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setGravity(Entity entity, boolean gravity) {
        if (NMSUtils.class_Entity_setNoGravity == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_setNoGravity.invoke(handle, !gravity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setDisabledSlots(ArmorStand armorStand, int disabledSlots) {
        if (NMSUtils.class_EntityArmorStand_disabledSlotsField == null) return;
        try {
            Object handle = NMSUtils.getHandle(armorStand);
            NMSUtils.class_EntityArmorStand_disabledSlotsField.set(handle, disabledSlots);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getDisabledSlots(ArmorStand armorStand) {
        if (NMSUtils.class_EntityArmorStand_disabledSlotsField == null) return 0;
        try {
            Object handle = NMSUtils.getHandle(armorStand);
            return (int) NMSUtils.class_EntityArmorStand_disabledSlotsField.get(handle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean isPersistentInvisible(Entity entity) {
        if (NMSUtils.class_Entity_persistentInvisibilityField == null) return false;
        try {
            Object handle = NMSUtils.getHandle(entity);
            return (boolean) NMSUtils.class_Entity_persistentInvisibilityField.get(handle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void setPersistentInvisible(Entity entity, boolean invisible) {
        if (NMSUtils.class_Entity_persistentInvisibilityField == null) return;
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_persistentInvisibilityField.set(handle, invisible);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setYawPitch(Entity entity, float yaw, float pitch) {
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_setYawPitchMethod.invoke(handle, yaw, pitch);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
        try {
            Object handle = NMSUtils.getHandle(entity);
            NMSUtils.class_Entity_setLocationMethod.invoke(handle, x, y, z, yaw, pitch);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void addFlightExemption(Player player, int ticks) {
        if (NMSUtils.class_PlayerConnection_floatCountField == null) return;
        try {
            Object handle = NMSUtils.getHandle(player);
            Object connection = NMSUtils.class_EntityPlayer_playerConnectionField.get(handle);
            NMSUtils.class_PlayerConnection_floatCountField.set(connection, -ticks);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isValidProjectileClass(Class<?> projectileType) {
        return projectileType != null
                && (NMSUtils.class_EntityArrow.isAssignableFrom(projectileType)
                || NMSUtils.class_EntityProjectile.isAssignableFrom(projectileType)
                || NMSUtils.class_EntityFireball.isAssignableFrom(projectileType)
                || (NMSUtils.class_IProjectile != null && NMSUtils.class_IProjectile.isAssignableFrom(projectileType))
        );
    }

    @Override
    public Projectile spawnProjectile(Class<?> projectileType, Location location, Vector direction, ProjectileSource source, float speed, float spread, float spreadLocations, Random random) {
        Constructor<? extends Object> constructor = null;
        Method shootMethod = null;
        Method setPositionRotationMethod = null;
        Field projectileSourceField = null;
        Field dirXField = null;
        Field dirYField = null;
        Field dirZField = null;
        Object nmsWorld = NMSUtils.getHandle(location.getWorld());
        Projectile projectile = null;
        try {
            Object entityType = null;
            if (NMSUtils.entityTypes != null) {
                constructor = projectileType.getConstructor(NMSUtils.class_entityTypes, NMSUtils.class_World);
                entityType = NMSUtils.entityTypes.get(projectileType.getSimpleName());
                if (entityType == null) {
                    throw new Exception("Failed to find entity type for projectile class " + projectileType.getName());
                }
            } else {
                constructor = projectileType.getConstructor(NMSUtils.class_World);
            }

            if (NMSUtils.class_EntityFireball.isAssignableFrom(projectileType)) {
                dirXField = projectileType.getField("dirX");
                dirYField = projectileType.getField("dirY");
                dirZField = projectileType.getField("dirZ");
            }

            if (NMSUtils.class_EntityProjectile.isAssignableFrom(projectileType) || NMSUtils.class_EntityArrow.isAssignableFrom(projectileType)) {
                shootMethod = projectileType.getMethod("shoot", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
            }

            setPositionRotationMethod = projectileType.getMethod("setPositionRotation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
            projectileSourceField = projectileType.getField("projectileSource");

            Object nmsProjectile = null;
            try {
                nmsProjectile = entityType == null ? constructor.newInstance(nmsWorld) : constructor.newInstance(entityType, nmsWorld);
            } catch (Exception ex) {
                nmsProjectile = null;
                platform.getLogger().log(Level.WARNING, "Error spawning projectile of class " + projectileType.getName(), ex);
            }

            if (nmsProjectile == null) {
                throw new Exception("Failed to spawn projectile of class " + projectileType.getName());
            }

            // Set position and rotation, and potentially velocity (direction)
            // Velocity must be set manually- EntityFireball.setDirection applies a crazy-wide gaussian distribution!
            if (dirXField != null && dirYField != null && dirZField != null) {
                // Taken from EntityArrow
                double spreadWeight = Math.min(0.4f,  spread * 0.007499999832361937D);

                double dx = speed * (direction.getX() + (random.nextGaussian() * spreadWeight));
                double dy = speed * (direction.getY() + (random.nextGaussian() * spreadWeight));
                double dz = speed * (direction.getZ() + (random.nextGaussian() * spreadWeight));

                dirXField.set(nmsProjectile, dx * 0.1D);
                dirYField.set(nmsProjectile, dy * 0.1D);
                dirZField.set(nmsProjectile, dz * 0.1D);
            }
            Vector modifiedLocation = location.toVector().clone();
            if (NMSUtils.class_EntityFireball.isAssignableFrom(projectileType) && spreadLocations > 0) {
                modifiedLocation.setX(modifiedLocation.getX() + direction.getX() + (random.nextGaussian() * spread / 5));
                modifiedLocation.setY(modifiedLocation.getY() + direction.getY() + (random.nextGaussian() * spread / 5));
                modifiedLocation.setZ(modifiedLocation.getZ() + direction.getZ() + (random.nextGaussian() * spread / 5));
            }
            setPositionRotationMethod.invoke(nmsProjectile, modifiedLocation.getX(), modifiedLocation.getY(), modifiedLocation.getZ(), location.getYaw(), location.getPitch());

            if (shootMethod != null) {
                shootMethod.invoke(nmsProjectile, direction.getX(), direction.getY(), direction.getZ(), speed, spread);
            }

            Entity entity = getBukkitEntity(nmsProjectile);
            if (entity == null || !(entity instanceof Projectile)) {
                throw new Exception("Got invalid bukkit entity from projectile of class " + projectileType.getName());
            }

            projectile = (Projectile)entity;
            if (source != null) {
                projectile.setShooter(source);
                projectileSourceField.set(nmsProjectile, source);
            }

            NMSUtils.class_World_addEntityMethod.invoke(nmsWorld, nmsProjectile, CreatureSpawnEvent.SpawnReason.DEFAULT);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }

        return projectile;
    }

    @Override
    public void setDamage(Projectile projectile, double damage) {
        if (NMSUtils.class_EntityArrow_damageField == null) return;
        try {
            Object handle = NMSUtils.getHandle(projectile);
            NMSUtils.class_EntityArrow_damageField.set(handle, damage);
        } catch (Exception ex) {
            ex.printStackTrace();;
        }
    }

    @Override
    public void decreaseLifespan(Projectile projectile, int ticks) {
        if (NMSUtils.class_EntityArrow_lifeField == null) return;
        try {
            Object handle = NMSUtils.getHandle(projectile);
            int currentLife = (Integer) NMSUtils.class_EntityArrow_lifeField.get(handle);
            if (currentLife < ticks) {
                NMSUtils.class_EntityArrow_lifeField.set(handle, ticks);
            }
        } catch (Exception ex) {
            ex.printStackTrace();;
        }
    }

    @Override
    public Entity spawnEntity(Location target, EntityType entityType, CreatureSpawnEvent.SpawnReason spawnReason)
    {
        if (NMSUtils.class_CraftWorld_spawnMethod == null) {
            return target.getWorld().spawnEntity(target, entityType);
        }
        Entity entity = null;
        try {
            World world = target.getWorld();
            try {
                if (!NMSUtils.class_CraftWorld_spawnMethod_isLegacy) {
                    entity = (Entity) NMSUtils.class_CraftWorld_spawnMethod.invoke(world, target, entityType.getEntityClass(), null, spawnReason);
                } else {
                    entity = (Entity) NMSUtils.class_CraftWorld_spawnMethod.invoke(world, target, entityType.getEntityClass(), spawnReason);
                }
            } catch (Exception ex) {
                entity = target.getWorld().spawnEntity(target, entityType);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entity;
    }

    @Override
    public String getResourcePack(Server server) {
        String rp = null;
        try {
            Object minecraftServer = NMSUtils.getHandle(server);
            if (minecraftServer != null) {
                rp = (String) NMSUtils.class_MinecraftServer_getResourcePackMethod.invoke(minecraftServer);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rp;
    }

    @Override
    public boolean setResourcePack(Player player, String rp, byte[] hash) {
        // TODO: Player.setResourcePack in 1.11+
        try {
            String hashString = BaseEncoding.base16().lowerCase().encode(hash);
            NMSUtils.class_EntityPlayer_setResourcePackMethod.invoke(NMSUtils.getHandle(player), rp, hashString);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean removeItemAttribute(ItemStack item, Attribute attribute) {
        try {
            Object handle = platform.getItemUtils().getHandle(item);
            if (handle == null) return false;
            Object tag = platform.getItemUtils().getTag(handle);
            if (tag == null) return false;

            String attributeName = toMinecraftAttribute(attribute);
            Object attributesNode = platform.getNBTUtils().getNode(tag, "AttributeModifiers");
            if (attributesNode == null) {
                return false;
            }
            int size = (Integer) NMSUtils.class_NBTTagList_sizeMethod.invoke(attributesNode);
            for (int i = 0; i < size; i++) {
                Object candidate = NMSUtils.class_NBTTagList_getMethod.invoke(attributesNode, i);
                String key = platform.getNBTUtils().getMetaString(candidate, "AttributeName");
                if (key.equals(attributeName)) {
                    if (size == 1) {
                        platform.getNBTUtils().removeMeta(tag, "AttributeModifiers");
                    } else {
                        NMSUtils.class_NBTTagList_removeMethod.invoke(attributesNode, i);
                    }
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean removeItemAttributes(ItemStack item) {
        try {
            Object handle = platform.getItemUtils().getHandle(item);
            if (handle == null) return false;
            Object tag = platform.getItemUtils().getTag(handle);
            if (tag == null) return false;

            Object attributesNode = platform.getNBTUtils().getNode(tag, "AttributeModifiers");
            if (attributesNode == null) {
                return false;
            }
            platform.getNBTUtils().removeMeta(tag, "AttributeModifiers");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean setItemAttribute(ItemStack item, Attribute attribute, double value, String slot, int attributeOperation, UUID attributeUUID) {
        if (NMSUtils.class_ItemMeta_addAttributeModifierMethod != null) {
            try {
                AttributeModifier.Operation operation;
                try {
                     operation = AttributeModifier.Operation.values()[attributeOperation];
                } catch (Throwable ex) {
                    platform.getLogger().warning("[Magic] invalid attribute operation ordinal: " + attributeOperation);
                    return false;
                }
                ItemMeta meta = item.getItemMeta();
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

                    modifier = (AttributeModifier) NMSUtils.class_AttributeModifier_constructor.newInstance(
                        attributeUUID, "Equipment Modifier", value, operation, equipmentSlot);
                } else {
                    modifier = new AttributeModifier(attributeUUID, "Equipment Modifier", value, operation);
                }
                NMSUtils.class_ItemMeta_addAttributeModifierMethod.invoke(meta, attribute, modifier);
                item.setItemMeta(meta);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        }
        try {
            Object handle = platform.getItemUtils().getHandle(item);
            if (handle == null) {
                return false;
            }
            Object tag = platform.getItemUtils().getTag(handle);
            if (tag == null) return false;

            Object attributesNode = platform.getNBTUtils().getNode(tag, "AttributeModifiers");
            Object attributeNode = null;

            String attributeName = toMinecraftAttribute(attribute);
            if (attributesNode == null) {
                attributesNode = NMSUtils.class_NBTTagList_constructor.newInstance();
                NMSUtils.class_NBTTagCompound_setMethod.invoke(tag, "AttributeModifiers", attributesNode);
            } else {
                int size = (Integer) NMSUtils.class_NBTTagList_sizeMethod.invoke(attributesNode);
                for (int i = 0; i < size; i++) {
                    Object candidate = NMSUtils.class_NBTTagList_getMethod.invoke(attributesNode, i);
                    String key = platform.getNBTUtils().getMetaString(candidate, "AttributeName");
                    if (key.equals(attributeName)) {
                        attributeNode = candidate;
                        break;
                    }
                }
            }
            if (attributeNode == null) {
                attributeNode = NMSUtils.class_NBTTagCompound_constructor.newInstance();
                platform.getNBTUtils().setMeta(attributeNode, "AttributeName", attributeName);
                platform.getNBTUtils().setMeta(attributeNode, "Name", "Equipment Modifier");
                platform.getNBTUtils().setMetaInt(attributeNode, "Operation", attributeOperation);
                platform.getNBTUtils().setMetaLong(attributeNode, "UUIDMost", attributeUUID.getMostSignificantBits());
                platform.getNBTUtils().setMetaLong(attributeNode, "UUIDLeast", attributeUUID.getLeastSignificantBits());
                if (slot != null) {
                    platform.getNBTUtils().setMeta(attributeNode, "Slot", slot);
                }

                platform.getNBTUtils().addToList(attributesNode, attributeNode);
            }
            platform.getNBTUtils().setMetaDouble(attributeNode, "Amount", value);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void sendExperienceUpdate(Player player, float experience, int level) {
        try {
            Object packet = NMSUtils.class_PacketPlayOutExperience_Constructor.newInstance(experience, player.getTotalExperience(), level);
            NMSUtils.sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Object getEntityData(Entity entity) {
        if (NMSUtils.class_Entity_saveMethod == null) return null;

        Object data = null;
        try {
            Object nmsEntity = NMSUtils.getHandle(entity);
            if (nmsEntity != null) {
                data = NMSUtils.class_NBTTagCompound_constructor.newInstance();
                NMSUtils.class_Entity_saveMethod.invoke(nmsEntity, data);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }

    @Override
    public String getEntityType(Entity entity) {
        if (NMSUtils.class_Entity_getTypeMethod == null) return null;
        String entityType = null;
        try {
            Object nmsEntity = NMSUtils.getHandle(entity);
            if (nmsEntity != null) {
                entityType = (String) NMSUtils.class_Entity_getTypeMethod.invoke(nmsEntity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entityType;
    }

    @Override
    public void swingOffhand(Entity entity) {
        int rangeSquared = OFFHAND_BROADCAST_RANGE * OFFHAND_BROADCAST_RANGE;
        String worldName = entity.getWorld().getName();
        Location center = entity.getLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equals(worldName) || player.getLocation().distanceSquared(center) > rangeSquared) {
                continue;
            }
            swingOffhand(player, entity);
        }
    }

    private void swingOffhand(Player sendToPlayer, Entity entity) {
        try {
            Object packet = NMSUtils.class_PacketPlayOutAnimation_Constructor.newInstance(NMSUtils.getHandle(entity), 3);
            NMSUtils.sendPacket(sendToPlayer, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        // TODO: New Player.sendTitle in 1.11
        player.sendTitle(title, subTitle);
    }

    @Override
    public void swingMainHand(Entity entity) {
        int rangeSquared = OFFHAND_BROADCAST_RANGE * OFFHAND_BROADCAST_RANGE;
        String worldName = entity.getWorld().getName();
        Location center = entity.getLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equals(worldName) || player.getLocation().distanceSquared(center) > rangeSquared) {
                continue;
            }
            swingMainHand(player, entity);
        }
    }

    private void swingMainHand(Player sendToPlayer, Entity entity) {
        try {
            Object packet = NMSUtils.class_PacketPlayOutAnimation_Constructor.newInstance(NMSUtils.getHandle(entity), 0);
            NMSUtils.sendPacket(sendToPlayer, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean sendActionBar(Player player, String message) {
        if (NMSUtils.class_PacketPlayOutChat == null) return false;
        try {
            Object chatComponent = NMSUtils.class_ChatComponentText_constructor.newInstance(message);
            Object packet;
            if (NMSUtils.enum_ChatMessageType_GAME_INFO == null) {
                packet = NMSUtils.class_PacketPlayOutChat_constructor.newInstance(chatComponent, (byte)2);
            } else if (NMSUtils.chatPacketHasUUID) {
                packet = NMSUtils.class_PacketPlayOutChat_constructor.newInstance(chatComponent, NMSUtils.enum_ChatMessageType_GAME_INFO, emptyUUID);
            } else {
                packet = NMSUtils.class_PacketPlayOutChat_constructor.newInstance(chatComponent, NMSUtils.enum_ChatMessageType_GAME_INFO);
            }
            NMSUtils.sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public float getDurability(Material material) {
        if (NMSUtils.class_Block_durabilityField == null || NMSUtils.class_CraftMagicNumbers_getBlockMethod == null) return 0.0f;
        try {
            Object block = NMSUtils.class_CraftMagicNumbers_getBlockMethod.invoke(null, material);
            if (block == null) {
                return 0.0f;
            }
            return (float) NMSUtils.class_Block_durabilityField.get(block);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0f;
    }

    @Override
    public void sendBreaking(Player player, long id, Location location, int breakAmount) {
        try {
            Object blockPosition = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
            Object packet = NMSUtils.class_PacketPlayOutBlockBreakAnimation_Constructor.newInstance((int)id, blockPosition, breakAmount);
            NMSUtils.sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Set<String> getTags(Entity entity) {
        // TODO: Use Entity.getScoreboardTags in a future version.
        return null;
    }

    @Override
    public boolean isJumping(LivingEntity entity) {
        if (NMSUtils.class_Entity_jumpingField == null) return false;
        try {
            return (boolean) NMSUtils.class_Entity_jumpingField.get(NMSUtils.getHandle(entity));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public float getForwardMovement(LivingEntity entity) {
        if (NMSUtils.class_Entity_moveForwardField == null) return 0.0f;
        try {
            return (float) NMSUtils.class_Entity_moveForwardField.get(NMSUtils.getHandle(entity));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0f;
    }

    @Override
    public float getStrafeMovement(LivingEntity entity) {
        if (NMSUtils.class_Entity_moveStrafingField == null) return 0.0f;
        try {
            return (float) NMSUtils.class_Entity_moveStrafingField.get(NMSUtils.getHandle(entity));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0f;
    }

    @Override
    public boolean setBlockFast(Chunk chunk, int x, int y, int z, Material material, int data) {
        if (NMSUtils.class_Block_fromLegacyData == null || NMSUtils.class_CraftMagicNumbers_getBlockMethod == null || NMSUtils.class_Chunk_setBlockMethod == null || NMSUtils.class_BlockPosition_Constructor == null) {
            platform.getDeprecatedUtils().setTypeAndData(chunk.getWorld().getBlockAt(x, y, z), material, (byte)data, false);
            return true;
        }
        try {
            Object chunkHandle = NMSUtils.getHandle(chunk);
            Object nmsBlock = NMSUtils.class_CraftMagicNumbers_getBlockMethod.invoke(null, material);
            nmsBlock = NMSUtils.class_Block_fromLegacyData.invoke(nmsBlock, data);
            Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(x, y, z);
            NMSUtils.class_Chunk_setBlockMethod.invoke(chunkHandle, blockLocation, nmsBlock);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean setPickupStatus(Arrow arrow, String pickupStatus) {
        if (arrow == null || pickupStatus == null || NMSUtils.class_Arrow_setPickupStatusMethod == null || NMSUtils.class_PickupStatus == null) return false;

        try {
            Enum enumValue = Enum.valueOf(NMSUtils.class_PickupStatus, pickupStatus.toUpperCase());
            NMSUtils.class_Arrow_setPickupStatusMethod.invoke(arrow, enumValue);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public Block getHitBlock(ProjectileHitEvent event) {
        if (NMSUtils.class_ProjectileHitEvent_getHitBlockMethod == null) return null;
        try {
            return (Block) NMSUtils.class_ProjectileHitEvent_getHitBlockMethod.invoke(event);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Entity getEntity(World world, UUID uuid) {
        try {
            Object worldHandle = NMSUtils.getHandle(world);
            final Map<UUID, Entity> entityMap = (Map<UUID, Entity>) NMSUtils.class_WorldServer_entitiesByUUIDField.get(worldHandle);
            if (entityMap != null) {
                Object nmsEntity = entityMap.get(uuid);
                if (nmsEntity != null) {
                    return getBukkitEntity(nmsEntity);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Entity getEntity(UUID uuid) {
        if (NMSUtils.class_Server_getEntityMethod != null) {
            try {
                return (Entity) NMSUtils.class_Server_getEntityMethod.invoke(Bukkit.getServer(), uuid);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        for (World world : Bukkit.getWorlds()) {
            Entity found = getEntity(world, uuid);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    @Override
    public boolean canRemoveRecipes() {
        return NMSUtils.class_Server_removeRecipeMethod != null;
    }

    @Override
    public boolean removeRecipe(Recipe recipe) {
        if (NMSUtils.class_Keyed == null || NMSUtils.class_Keyed_getKeyMethod == null || NMSUtils.class_Server_removeRecipeMethod == null) {
            return false;
        }
        if (!NMSUtils.class_Keyed.isAssignableFrom(recipe.getClass())) {
            return false;
        }
        try {
            Object namespacedKey = NMSUtils.class_Keyed_getKeyMethod.invoke(recipe);
            return (boolean) NMSUtils.class_Server_removeRecipeMethod.invoke(platform.getPlugin().getServer(), namespacedKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeRecipe(String key) {
        if (NMSUtils.class_NamespacedKey == null || NMSUtils.class_Server_removeRecipeMethod == null) {
            return false;
        }

        try {
            Object namespacedKey = NMSUtils.class_NamespacedKey_constructor.newInstance(platform.getPlugin(), key.toLowerCase());
            return (boolean) NMSUtils.class_Server_removeRecipeMethod.invoke(platform.getPlugin().getServer(), namespacedKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public ShapedRecipe createShapedRecipe(String key, ItemStack item) {
        if (NMSUtils.class_NamespacedKey == null) {
            return new ShapedRecipe(item);
        }

        try {
            Object namespacedKey = NMSUtils.class_NamespacedKey_constructor.newInstance(platform.getPlugin(), key.toLowerCase());
            return (ShapedRecipe) NMSUtils.class_ShapedRecipe_constructor.newInstance(namespacedKey, item);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ShapedRecipe(item);
        }
    }

    @Override
    public boolean discoverRecipe(HumanEntity entity, String key) {
        if (NMSUtils.class_NamespacedKey == null || NMSUtils.class_HumanEntity_discoverRecipeMethod == null) {
            return false;
        }

        try {
            Object namespacedKey = NMSUtils.class_NamespacedKey_constructor.newInstance(platform.getPlugin(), key.toLowerCase());
            return (boolean) NMSUtils.class_HumanEntity_discoverRecipeMethod.invoke(entity, namespacedKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean undiscoverRecipe(HumanEntity entity, String key) {
        if (NMSUtils.class_NamespacedKey == null || NMSUtils.class_HumanEntity_undiscoverRecipeMethod == null) {
            return false;
        }

        try {
            Object namespacedKey = NMSUtils.class_NamespacedKey_constructor.newInstance(platform.getPlugin(), key.toLowerCase());
            return (boolean) NMSUtils.class_HumanEntity_undiscoverRecipeMethod.invoke(entity, namespacedKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public double getMaxHealth(Damageable li) {
        // return li.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        return li.getMaxHealth();
    }

    @Override
    public void setMaxHealth(Damageable li, double maxHealth) {
        // li.getAttribute(Attribute.GENERIC_MAX_HEALTH).setValue(maxHealth);
        li.setMaxHealth(maxHealth);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Material fromLegacy(org.bukkit.material.MaterialData materialData) {
        if (NMSUtils.class_UnsafeValues_fromLegacyDataMethod != null) {
            try {
                Material converted = (Material) NMSUtils.class_UnsafeValues_fromLegacyDataMethod.invoke(platform.getDeprecatedUtils().getUnsafe(), materialData);
                if (converted == Material.AIR) {
                    materialData.setData((byte)0);
                    converted = (Material) NMSUtils.class_UnsafeValues_fromLegacyDataMethod.invoke(platform.getDeprecatedUtils().getUnsafe(), materialData);
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
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return materialData.getItemType();
    }

    @Override
    public boolean hasLegacyMaterials() {
        return NMSUtils.class_Material_isLegacyMethod != null;
    }

    @Override
    public boolean isLegacy(Material material) {
        if (NMSUtils.class_Material_isLegacyMethod == null) {
            return false;
        }
        try {
            return (boolean) NMSUtils.class_Material_isLegacyMethod.invoke(material);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public Material getLegacyMaterial(String materialName) {
        if (NMSUtils.class_Material_getLegacyMethod != null) {
            try {
                return (Material) NMSUtils.class_Material_getLegacyMethod.invoke(null, materialName, true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return Material.getMaterial(materialName);
    }

    @Override
    public boolean applyBonemeal(Location location) {
        if (NMSUtils.class_ItemDye_bonemealMethod == null) return false;

        if (dummyItem == null) {
             dummyItem = new ItemStack(Material.DIRT, 64);
             dummyItem = platform.getItemUtils().makeReal(dummyItem);
        }
        dummyItem.setAmount(64);

        try {
            Object world = NMSUtils.getHandle(location.getWorld());
            Object itemStack = platform.getItemUtils().getHandle(dummyItem);
            Object blockPosition = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getX(), location.getY(), location.getZ());
            Object result = NMSUtils.class_ItemDye_bonemealMethod.invoke(null, itemStack, world, blockPosition);
            return (Boolean)result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public Color getColor(PotionMeta meta) {
        Color color = Color.BLACK;
        if (NMSUtils.class_PotionMeta_getColorMethod != null) {
            try {
                color = (Color) NMSUtils.class_PotionMeta_getColorMethod.invoke(meta);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return color;
    }

    @Override
    public boolean setColor(PotionMeta meta, Color color) {
        if (NMSUtils.class_PotionMeta_setColorMethod != null) {
            try {
                NMSUtils.class_PotionMeta_setColorMethod.invoke(meta, color);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean hasBlockDataSupport() {
        return NMSUtils.class_Block_getBlockDataMethod != null;
    }

    @Override
    public byte getLegacyBlockData(FallingBlock falling) {
        // @deprecated Magic value
        byte data = 0;
        try {
            if (NMSUtils.class_FallingBlock_getBlockDataMethod != null) {
                data = (byte) NMSUtils.class_FallingBlock_getBlockDataMethod.invoke(falling);
            }
        } catch (Exception ignore) {

        }
        return data;
    }

    @Override
    public Material getMaterial(FallingBlock falling) {
        return falling.getMaterial();
    }

    @Override
    public String getBlockData(FallingBlock fallingBlock) {
        return null;
    }

    @Override
    public String getBlockData(Block block) {
        if (NMSUtils.class_Block_getBlockDataMethod == null) return null;
        try {
            Object blockData = NMSUtils.class_Block_getBlockDataMethod.invoke(block);
            if (blockData == null) {
                return null;
            }
            return (String) NMSUtils.class_BlockData_getAsStringMethod.invoke(blockData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String getBlockData(Material material, byte data) {
        if (NMSUtils.class_UnsafeValues_fromLegacyMethod == null) return null;
        try {
            Object blockData = NMSUtils.class_UnsafeValues_fromLegacyMethod.invoke(platform.getDeprecatedUtils().getUnsafe(), material, data);
            if (blockData != null) {
                return (String) NMSUtils.class_BlockData_getAsStringMethod.invoke(blockData);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean setBlockData(Block block, String data) {
        if (NMSUtils.class_Block_getBlockDataMethod == null) return false;
        try {
            Object blockData = NMSUtils.class_Server_createBlockDataMethod.invoke(platform.getPlugin().getServer(), data);
            NMSUtils.class_Block_setBlockDataMethod.invoke(block, blockData, false);
            return true;
        } catch (Exception ignore) {
            // Ignore issues setting invalid block data
        }
        return false;
    }

    @Override
    public boolean applyPhysics(Block block) {
        if (NMSUtils.class_World_setTypeAndDataMethod == null || NMSUtils.class_World_getTypeMethod == null || NMSUtils.class_BlockPosition_Constructor == null) return false;
        try {
            Object worldHandle = NMSUtils.getHandle(block.getWorld());
            Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(block.getX(), block.getY(), block.getZ());
            Object blockType = NMSUtils.class_World_getTypeMethod.invoke(worldHandle, blockLocation);
            clearItems(block.getLocation());
            platform.getDeprecatedUtils().setTypeAndData(block, Material.AIR, (byte)0, false);
            return (boolean) NMSUtils.class_World_setTypeAndDataMethod.invoke(worldHandle, blockLocation, blockType, 3);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean addRecipeToBook(ItemStack book, Plugin plugin, String recipeKey) {
        if (NMSUtils.class_NamespacedKey_constructor == null || NMSUtils.class_KnowledgeBookMeta_addRecipeMethod == null) return false;
        ItemMeta meta = book.getItemMeta();
        if (!NMSUtils.class_KnowledgeBookMeta.isAssignableFrom(meta.getClass())) return false;
        try {
            Object namespacedKey = NMSUtils.class_NamespacedKey_constructor.newInstance(plugin, recipeKey.toLowerCase());
            Object array = Array.newInstance(NMSUtils.class_NamespacedKey, 1);
            Array.set(array, 0, namespacedKey);
            NMSUtils.class_KnowledgeBookMeta_addRecipeMethod.invoke(meta, array);
            book.setItemMeta(meta);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean canToggleBlockPower(Block block) {
        if (isPowerable(block)) {
            return true;
        }

        Material material = block.getType();
        if (material == Material.REDSTONE_TORCH_OFF || material == Material.REDSTONE_TORCH_ON) {
            return true;
        }

        BlockState blockState = block.getState();
        MaterialData data = blockState.getData();
        if (data instanceof RedstoneWire) {
            return true;
        }
        return false;
    }

    @Override
    public boolean toggleBlockPower(Block block) {
        if (isPowerable(block)) {
            setPowered(block, !isPowered(block));
            return true;
        }

        Material material = block.getType();
        if (material == Material.REDSTONE_TORCH_OFF) {
            block.setType(Material.REDSTONE_TORCH_ON);
            return true;
        }
        if (material == Material.REDSTONE_TORCH_ON) {
            block.setType(Material.REDSTONE_TORCH_OFF);
            return true;
        }

        BlockState blockState = block.getState();
        MaterialData data = blockState.getData();
        if (data instanceof RedstoneWire) {
            RedstoneWire wireData = (RedstoneWire)data;
            wireData.setData((byte)(15 - wireData.getData()));
            blockState.update();
            return true;
        }
        return false;
    }

    @Override
    public boolean isPowerable(Block block) {
        if (NMSUtils.class_Powerable == null || NMSUtils.class_Powerable_setPoweredMethod == null || NMSUtils.class_Block_getBlockDataMethod == null) {
            return isPowerableLegacy(block);
        }
        try {
            Object blockData = NMSUtils.class_Block_getBlockDataMethod.invoke(block);
            return blockData != null && NMSUtils.class_Powerable.isAssignableFrom(blockData.getClass());
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected boolean isPowerableLegacy(Block block) {
        BlockState blockState = block.getState();
        org.bukkit.material.MaterialData data = blockState.getData();
        return data instanceof org.bukkit.material.Button
                || data instanceof org.bukkit.material.Lever
                || data instanceof org.bukkit.material.PistonBaseMaterial
                || data instanceof org.bukkit.material.PoweredRail;
    }

    @Override
    public boolean isPowered(Block block) {
        if (NMSUtils.class_Powerable == null || NMSUtils.class_Powerable_setPoweredMethod == null || NMSUtils.class_Block_getBlockDataMethod == null) {
            return isPoweredLegacy(block);
        }
        try {
            Object blockData = NMSUtils.class_Block_getBlockDataMethod.invoke(block);
            if (blockData == null) return false;
            if (!NMSUtils.class_Powerable.isAssignableFrom(blockData.getClass())) return false;
            return (boolean) NMSUtils.class_Powerable_isPoweredMethod.invoke(blockData);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected boolean isPoweredLegacy(Block block) {
        BlockState blockState = block.getState();
        org.bukkit.material.MaterialData data = blockState.getData();
        if (data instanceof org.bukkit.material.Button) {
            org.bukkit.material.Button powerData = (org.bukkit.material.Button)data;
            return powerData.isPowered();
        } else if (data instanceof org.bukkit.material.Lever) {
            org.bukkit.material.Lever powerData = (org.bukkit.material.Lever)data;
            return powerData.isPowered();
        } else if (data instanceof org.bukkit.material.PistonBaseMaterial) {
            org.bukkit.material.PistonBaseMaterial powerData = (org.bukkit.material.PistonBaseMaterial)data;
            return powerData.isPowered();
        } else if (data instanceof org.bukkit.material.PoweredRail) {
            org.bukkit.material.PoweredRail powerData = (org.bukkit.material.PoweredRail)data;
            return powerData.isPowered();
        }
        return false;
    }

    @Override
    public boolean setPowered(Block block, boolean powered) {
        if (NMSUtils.class_Powerable == null || NMSUtils.class_Powerable_setPoweredMethod == null
                || NMSUtils.class_Block_setBlockDataMethod == null || NMSUtils.class_Block_getBlockDataMethod == null) {
            return setPoweredLegacy(block, powered);
        }

        try {
            Object blockData = NMSUtils.class_Block_getBlockDataMethod.invoke(block);
            if (blockData == null) return false;
            if (!NMSUtils.class_Powerable.isAssignableFrom(blockData.getClass())) return false;
            NMSUtils.class_Powerable_setPoweredMethod.invoke(blockData, powered);
            NMSUtils.class_Block_setBlockDataMethod.invoke(block, blockData, true);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isWaterLoggable(Block block) {
        if (NMSUtils.class_Waterlogged == null || NMSUtils.class_Waterlogged_setWaterloggedMethod == null
            || NMSUtils.class_Block_setBlockDataMethod == null || NMSUtils.class_Block_getBlockDataMethod == null) {
            return false;
        }

        try {
            Object blockData = NMSUtils.class_Block_getBlockDataMethod.invoke(block);
            if (blockData == null) return false;
            return NMSUtils.class_Waterlogged.isAssignableFrom(blockData.getClass());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean setWaterlogged(Block block, boolean waterlogged) {
        if (NMSUtils.class_Waterlogged == null || NMSUtils.class_Waterlogged_setWaterloggedMethod == null
            || NMSUtils.class_Block_setBlockDataMethod == null || NMSUtils.class_Block_getBlockDataMethod == null) {
            return false;
        }

        try {
            Object blockData = NMSUtils.class_Block_getBlockDataMethod.invoke(block);
            if (blockData == null) return false;
            if (!NMSUtils.class_Waterlogged.isAssignableFrom(blockData.getClass())) return false;
            NMSUtils.class_Waterlogged_setWaterloggedMethod.invoke(blockData, waterlogged);
            NMSUtils.class_Block_setBlockDataMethod.invoke(block, blockData, true);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected boolean setPoweredLegacy(Block block, boolean powered) {
        BlockState blockState = block.getState();
        org.bukkit.material.MaterialData data = blockState.getData();
        boolean powerBlock = false;
        if (data instanceof org.bukkit.material.Button) {
            org.bukkit.material.Button powerData = (org.bukkit.material.Button)data;
            powerData.setPowered(powered);
            powerBlock = true;
        } else if (data instanceof org.bukkit.material.Lever) {
            org.bukkit.material.Lever powerData = (org.bukkit.material.Lever)data;
            powerData.setPowered(powered);
            powerBlock = true;
        } else if (data instanceof org.bukkit.material.PistonBaseMaterial) {
            org.bukkit.material.PistonBaseMaterial powerData = (org.bukkit.material.PistonBaseMaterial)data;
            powerData.setPowered(powered);
            powerBlock = true;
        } else if (data instanceof org.bukkit.material.PoweredRail) {
            org.bukkit.material.PoweredRail powerData = (org.bukkit.material.PoweredRail)data;
            powerData.setPowered(powered);
            powerBlock = true;
        }
        if (powerBlock) {
            blockState.update();
        }
        return powerBlock;
    }

    @Override
    public boolean setTopHalf(Block block) {
        if (NMSUtils.class_Bisected == null) {
            return setTopHalfLegacy(block);
        }
        try {
            Object blockData = NMSUtils.class_Block_getBlockDataMethod.invoke(block);
            if (blockData == null || !NMSUtils.class_Bisected.isAssignableFrom(blockData.getClass())) return false;
            NMSUtils.class_Bisected_setHalfMethod.invoke(blockData, NMSUtils.enum_BisectedHalf_TOP);
            NMSUtils.class_Block_setBlockDataMethod.invoke(block, blockData, false);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected boolean setTopHalfLegacy(Block block) {
        byte data = platform.getDeprecatedUtils().getData(block);
        platform.getDeprecatedUtils().setTypeAndData(block, block.getType(), (byte)(data | 8), false);
        return true;
    }

    @Override
    public boolean stopSound(Player player, Sound sound) {
        if (NMSUtils.class_Player_stopSoundMethod == null) return false;
        try {
            NMSUtils.class_Player_stopSoundMethod.invoke(player, sound);
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean stopSound(Player player, String sound) {
        if (NMSUtils.class_Player_stopSoundStringMethod == null) return false;
        try {
            NMSUtils.class_Player_stopSoundStringMethod.invoke(player, sound);
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean lockChunk(Chunk chunk) {
        if (!platform.getPlugin().isEnabled()) return false;
        if (!chunk.isLoaded()) {
            platform.getLogger().info("Locking unloaded chunk");
        }
        if (NMSUtils.class_Chunk_addPluginChunkTicketMethod == null) return false;
        try {
            NMSUtils.class_Chunk_addPluginChunkTicketMethod.invoke(chunk, platform.getPlugin());
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean unlockChunk(Chunk chunk) {
        if (!platform.getPlugin().isEnabled()) return false;
        if (NMSUtils.class_Chunk_removePluginChunkTicketMethod == null) return false;
        try {
            NMSUtils.class_Chunk_removePluginChunkTicketMethod.invoke(chunk, platform.getPlugin());
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public Location getHangingLocation(Entity entity) {
        Location location = entity.getLocation();
        if (NMSUtils.class_EntityHanging_blockPosition == null || !(entity instanceof Hanging)) {
            return location;
        }
        Object handle = NMSUtils.getHandle(entity);
        try {
            Object position = NMSUtils.class_EntityHanging_blockPosition.get(handle);
            location.setX((int) NMSUtils.class_BlockPosition_getXMethod.invoke(position));
            location.setY((int) NMSUtils.class_BlockPosition_getYMethod.invoke(position));
            location.setZ((int) NMSUtils.class_BlockPosition_getZMethod.invoke(position));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return location;
    }

    @Override
    public boolean setRecipeGroup(Recipe recipe, String group) {
        if (!(recipe instanceof ShapedRecipe)) return false;
        if (NMSUtils.class_Recipe_setGroupMethod == null) return false;
        try {
            NMSUtils.class_Recipe_setGroupMethod.invoke(recipe, group);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isSameKey(Plugin plugin, String key, Object keyed) {
        if (keyed == null || NMSUtils.class_Keyed == null || !NMSUtils.class_Keyed.isAssignableFrom(keyed.getClass())) {
            return false;
        }
        String namespace = plugin.getName().toLowerCase(Locale.ROOT);
        key = key.toLowerCase(Locale.ROOT);
        try {
            Object namespacedKey = NMSUtils.class_Keyed_getKeyMethod.invoke(keyed);
            Object keyNamespace = NMSUtils.class_NamespacedKey_getNamespaceMethod.invoke(namespacedKey);
            Object keyKey = NMSUtils.class_NamespacedKey_getKeyMethod.invoke(namespacedKey);
            return keyNamespace.equals(namespace) && keyKey.equals(key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isLegacyRecipes() {
        return NMSUtils.class_RecipeChoice_ExactChoice == null || NMSUtils.class_NamespacedKey == null;
    }

    @Override
    public boolean setRecipeIngredient(ShapedRecipe recipe, char key, ItemStack ingredient, boolean ignoreDamage) {
        if (ingredient == null) return false;
        if (NMSUtils.class_RecipeChoice_ExactChoice == null) {
            if (platform.isLegacy()) {
                @SuppressWarnings("deprecation")
                org.bukkit.material.MaterialData material = ingredient == null ? null : ingredient.getData();
                if (material == null) {
                    return false;
                }
                recipe.setIngredient(key, material);
            } else {
                recipe.setIngredient(key, ingredient.getType());
            }
            return true;
        }
        try {
            short maxDurability = ingredient.getType().getMaxDurability();
            if (ignoreDamage && maxDurability > 0) {
                List<ItemStack> damaged = new ArrayList<>();
                for (short damage = 0; damage < maxDurability; damage++) {
                    ingredient = ingredient.clone();
                    ingredient.setDurability(damage);
                    damaged.add(ingredient);
                }
                Object exactChoice = NMSUtils.class_RecipeChoice_ExactChoice_List_constructor.newInstance(damaged);
                NMSUtils.class_ShapedRecipe_setIngredientMethod.invoke(recipe, key, exactChoice);
                return true;
            }
            Object exactChoice = NMSUtils.class_RecipeChoice_ExactChoice_constructor.newInstance(ingredient);
            NMSUtils.class_ShapedRecipe_setIngredientMethod.invoke(recipe, key, exactChoice);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean setAutoBlockState(Block block, Location target, BlockFace facing, boolean physics, Player originator) {
        if (NMSUtils.class_CraftBlock == null || block == null || facing == null || target == null) return false;
        try {
            Object nmsBlock = NMSUtils.class_CraftBlock_getNMSBlockMethod.invoke(block);
            if (nmsBlock == null) return false;
            ItemStack blockItem = new ItemStack(block.getType());
            Object originatorHandle = NMSUtils.getHandle(originator);
            Object world = NMSUtils.getHandle(block.getWorld());
            Object item = platform.getItemUtils().getHandle(platform.getItemUtils().makeReal(blockItem));
            if (originatorHandle == null || world == null || item == null) {
                return false;
            }
            Object blockPosition = NMSUtils.class_BlockPosition_Constructor.newInstance(block.getX(), block.getY(), block.getZ());
            Object vec3D = NMSUtils.class_Vec3D_constructor.newInstance(target.getX(), target.getY(), target.getZ());
            Enum<?> directionEnum = Enum.valueOf(NMSUtils.class_EnumDirection, facing.name());
            Object movingObject = NMSUtils.class_MovingObjectPositionBlock_createMethod.invoke(null, vec3D, directionEnum, blockPosition);
            Object actionContext = NMSUtils.class_BlockActionContext_constructor.newInstance(world, originatorHandle, NMSUtils.enum_EnumHand_MAIN_HAND, item, movingObject);
            Object placedState = NMSUtils.class_Block_getPlacedStateMethod.invoke(nmsBlock, actionContext);
            if (placedState == null) return false;
            NMSUtils.class_CraftBlock_setTypeAndDataMethod.invoke(block, placedState, physics);
            // class_World_setTypeAndDataMethod.invoke(world, blockPosition, placedState, 11);
            return true;
         } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean forceUpdate(Block block, boolean physics) {
        if (NMSUtils.class_nms_Block_getBlockDataMethod == null) return false;
        try {
            Object nmsBlock = NMSUtils.class_CraftBlock_getNMSBlockMethod.invoke(block);
            Object blockData = NMSUtils.class_nms_Block_getBlockDataMethod.invoke(nmsBlock);
            Object world = NMSUtils.getHandle(block.getWorld());
            Object blockPosition = NMSUtils.class_BlockPosition_Constructor.newInstance(block.getX(), block.getY(), block.getZ());
            NMSUtils.class_World_setTypeAndDataMethod.invoke(world, blockPosition, blockData, 11);
            return true;
         } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getPhantomSize(Entity entity) {
        if (NMSUtils.class_Phantom == null || entity == null) return 0;
        try {
            if (!NMSUtils.class_Phantom.isAssignableFrom(entity.getClass())) return 0;
            return (int) NMSUtils.class_Phantom_getSizeMethod.invoke(entity);
         } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean setPhantomSize(Entity entity, int size) {
        if (NMSUtils.class_Phantom == null || entity == null) return false;
        try {
            if (!NMSUtils.class_Phantom.isAssignableFrom(entity.getClass())) return false;
            NMSUtils.class_Phantom_setSizeMethod.invoke(entity, size);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Location getBedSpawnLocation(Player player) {
        if (player == null) {
            return null;
        }
        if (NMSUtils.class_EntityHuman_getBedMethod != null && NMSUtils.class_EntityPlayer_getSpawnDimensionMethod != null) {
            try {
                Object playerHandle = NMSUtils.getHandle(player);
                Object bedLocation = NMSUtils.class_EntityHuman_getBedMethod.invoke(playerHandle);
                Object spawnDimension = NMSUtils.class_EntityPlayer_getSpawnDimensionMethod.invoke(playerHandle);
                if (spawnDimension != null && bedLocation != null) {
                    Object server = NMSUtils.class_EntityPlayer_serverField.get(playerHandle);
                    Object worldServer = server != null ? NMSUtils.class_MinecraftServer_getWorldServerMethod.invoke(server, spawnDimension) : null;
                    World world = worldServer != null ? (World) NMSUtils.class_WorldServer_worldMethod.invoke(worldServer) : null;
                    if (world != null) {
                        int x = (int) NMSUtils.class_BlockPosition_getXMethod.invoke(bedLocation);
                        int y = (int) NMSUtils.class_BlockPosition_getYMethod.invoke(bedLocation);
                        int z = (int) NMSUtils.class_BlockPosition_getZMethod.invoke(bedLocation);
                        return new Location(world, x, y, z);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (NMSUtils.class_EntityHuman_getBedMethod != null && NMSUtils.class_EntityHuman_spawnWorldField != null) {
            try {
                Object playerHandle = NMSUtils.getHandle(player);
                Object bedLocation = NMSUtils.class_EntityHuman_getBedMethod.invoke(playerHandle);
                String spawnWorld = (String) NMSUtils.class_EntityHuman_spawnWorldField.get(playerHandle);
                if (spawnWorld != null && bedLocation != null) {
                    World world = Bukkit.getWorld(spawnWorld);
                    if (world != null) {
                        int x = (int) NMSUtils.class_BlockPosition_getXMethod.invoke(bedLocation);
                        int y = (int) NMSUtils.class_BlockPosition_getYMethod.invoke(bedLocation);
                        int z = (int) NMSUtils.class_BlockPosition_getZMethod.invoke(bedLocation);
                        return new Location(world, x, y, z);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return player.getBedSpawnLocation();
    }

    @Override
    public void addPassenger(Entity vehicle, Entity passenger) {
        if (NMSUtils.class_Entity_addPassengerMethod != null) {
            try {
                NMSUtils.class_Entity_addPassengerMethod.invoke(vehicle, passenger);
                return;
            } catch (Exception ex) {
                platform.getLogger().log(Level.WARNING, "Error adding entity passenger", ex);
            }
        }
        platform.getDeprecatedUtils().setPassenger(vehicle, passenger);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Entity> getPassengers(Entity entity) {
        if (NMSUtils.class_Entity_getPassengersMethod != null) {
            try {
                return (List<Entity>) NMSUtils.class_Entity_getPassengersMethod.invoke(entity);
            } catch (Exception ex) {
                platform.getLogger().log(Level.WARNING, "Error getting entity passengers", ex);
            }
        }
        List<Entity> passengerList = new ArrayList<>();
        Entity passenger = platform.getDeprecatedUtils().getPassenger(entity);
        if (passenger != null) {
            passengerList.add(passenger);
        }
        return passengerList;
    }

    @Override
    public boolean openBook(Player player, ItemStack itemStack) {
        if (NMSUtils.class_Player_openBookMethod == null) {
            return false;
        }
        try {
            NMSUtils.class_Player_openBookMethod.invoke(player, itemStack);
            return true;
        } catch (Exception ex) {
            platform.getLogger().log(Level.SEVERE, "Unexpected error showing book", ex);
        }
        return false;
    }

    @Override
    public boolean isHandRaised(Player player) {
        if (NMSUtils.class_Player_isHandRaisedMethod == null) return false;
        try {
            return (boolean) NMSUtils.class_Player_isHandRaisedMethod.invoke(player);
        } catch (Exception ex) {
            platform.getLogger().log(Level.SEVERE, "Unexpected error checking block status", ex);
        }
        return false;
    }

    @Override
    public Class<?> getProjectileClass(String projectileTypeName) {
        Class<?> projectileType = NMSUtils.getBukkitClass("net.minecraft.server.Entity" + projectileTypeName);
        if (!isValidProjectileClass(projectileType)) {
            return null;
        }
        return projectileType;
    }

    @Override
    public Entity spawnFireworkEffect(Material fireworkMaterial, Server server, Location location, FireworkEffect effect, int power, Vector direction, Integer expectedLifespan, Integer ticksFlown, boolean silent) {
        Entity entity = null;
        try {
            if (fireworkMaterial == null) {
                return null;
            }
            Object world = NMSUtils.getHandle(location.getWorld());
            ItemStack itemStack = new ItemStack(fireworkMaterial);
            FireworkMeta meta = (FireworkMeta) itemStack.getItemMeta();
            meta.addEffect(effect);
            meta.setPower(power);
            itemStack.setItemMeta(meta);

            Object item = platform.getItemUtils().getHandle(platform.getItemUtils().makeReal(itemStack));
            final Object fireworkHandle = NMSUtils.class_EntityFireworkConstructor.newInstance(world, location.getX(), location.getY(), location.getZ(), item);
            setSilent(fireworkHandle, silent);

            if (direction != null) {
                if (NMSUtils.class_Entity_motField != null) {
                    Object vec = NMSUtils.class_Vec3D_constructor.newInstance(direction.getX(), direction.getY(), direction.getZ());
                    NMSUtils.class_Entity_motField.set(fireworkHandle, vec);
                } else if (NMSUtils.class_Entity_motXField != null) {
                    NMSUtils.class_Entity_motXField.set(fireworkHandle, direction.getX());
                    NMSUtils.class_Entity_motYField.set(fireworkHandle, direction.getY());
                    NMSUtils.class_Entity_motZField.set(fireworkHandle, direction.getZ());
                }
            }

            if (ticksFlown != null) {
                NMSUtils.class_Firework_ticksFlownField.set(fireworkHandle, ticksFlown);
            }
            if (expectedLifespan != null) {
                NMSUtils.class_Firework_expectedLifespanField.set(fireworkHandle, expectedLifespan);
            }

            if (direction == null)
            {
                Object fireworkPacket = NMSUtils.class_PacketSpawnEntityConstructor.newInstance(fireworkHandle, CompatibilityConstants.FIREWORK_TYPE);
                Object fireworkId = NMSUtils.class_Entity_getIdMethod.invoke(fireworkHandle);
                Object watcher = NMSUtils.class_Entity_getDataWatcherMethod.invoke(fireworkHandle);
                Object metadataPacket = NMSUtils.class_PacketPlayOutEntityMetadata_Constructor.newInstance(fireworkId, watcher, true);
                Object statusPacket = NMSUtils.class_PacketPlayOutEntityStatus_Constructor.newInstance(fireworkHandle, (byte)17);

                Constructor<?> packetDestroyEntityConstructor = NMSUtils.class_PacketPlayOutEntityDestroy.getConstructor(int[].class);
                Object destroyPacket = packetDestroyEntityConstructor.newInstance(new int[] {(Integer)fireworkId});

                Collection<? extends Player> players = server.getOnlinePlayers();
                NMSUtils.sendPacket(server, location, players, fireworkPacket);
                NMSUtils.sendPacket(server, location, players, metadataPacket);
                NMSUtils.sendPacket(server, location, players, statusPacket);
                NMSUtils.sendPacket(server, location, players, destroyPacket);
                return null;
            }

            NMSUtils.class_World_addEntityMethod.invoke(world, fireworkHandle, CreatureSpawnEvent.SpawnReason.CUSTOM);
            entity = (Entity) NMSUtils.class_Entity_getBukkitEntityMethod.invoke(fireworkHandle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return entity;
    }

    @Override
    public boolean loadAllTagsFromNBT(ConfigurationSection tags, Object tag)
    {
        try {
            Set<String> keys = platform.getInventoryUtils().getTagKeys(tag);
            if (keys == null) return false;

            for (String tagName : keys) {
                Object metaBase = NMSUtils.class_NBTTagCompound_getMethod.invoke(tag, tagName);
                if (metaBase != null) {
                    if (NMSUtils.class_NBTTagCompound.isAssignableFrom(metaBase.getClass())) {
                        ConfigurationSection newSection = tags.createSection(tagName);
                        loadAllTagsFromNBT(newSection, metaBase);
                    } else {
                        tags.set(tagName, platform.getInventoryUtils().getTagValue(metaBase));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public BoundingBox getHitbox(Entity entity) {
        if (NMSUtils.class_Entity_getBoundingBox != null) {
            try {
                Object entityHandle = NMSUtils.getHandle(entity);
                Object aabb = NMSUtils.class_Entity_getBoundingBox.invoke(entityHandle);
                if (aabb == null) {
                    return null;
                }
                return new BoundingBox(
                        NMSUtils.class_AxisAlignedBB_minXField.getDouble(aabb),
                        NMSUtils.class_AxisAlignedBB_maxXField.getDouble(aabb),
                        NMSUtils.class_AxisAlignedBB_minYField.getDouble(aabb),
                        NMSUtils.class_AxisAlignedBB_maxYField.getDouble(aabb),
                        NMSUtils.class_AxisAlignedBB_minZField.getDouble(aabb),
                        NMSUtils.class_AxisAlignedBB_maxZField.getDouble(aabb)
                );

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean isPrimaryThread() {
        return primaryThread.get() == Thread.currentThread();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean performDoorAction(Block[] doorBlocks, DoorActionType actionType) {
        BlockState blockState = doorBlocks[0].getState();
        MaterialData data = blockState.getData();
        if (!(data instanceof Door)) {
            return false;
        }
        Door doorData = (Door)data;

        switch (actionType) {
            case OPEN:
                if (doorData.isOpen()) {
                    return false;
                }
                doorData.setOpen(true);
                break;
            case CLOSE:
                if (!doorData.isOpen()) {
                    return false;
                }
                doorData.setOpen(false);
                break;
            case TOGGLE:
                doorData.setOpen(!doorData.isOpen());
            default:
                return false;
        }
        blockState.setData(doorData);
        blockState.update();
        return true;
    }

    @Override
    public boolean checkDoorAction(Block[] doorBlocks, DoorActionType actionType) {
        BlockState blockState = doorBlocks[0].getState();
        MaterialData data = blockState.getData();
        if (!(data instanceof Door)) {
            return false;
        }
        Door doorData = (Door)data;
        switch (actionType) {
            case OPEN:
                return !doorData.isOpen();
            case CLOSE:
                return doorData.isOpen();
            case TOGGLE:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Block[] getDoorBlocks(Block targetBlock) {
        BlockState blockState = targetBlock.getState();
        MaterialData data = blockState.getData();
        if (!(data instanceof Door)) {
            return null;
        }
        Block[] doorBlocks = new Block[2];
        Door doorData = (Door)data;
        if (doorData.isTopHalf()) {
            doorBlocks[1] = targetBlock;
            doorBlocks[0] = targetBlock.getRelative(BlockFace.DOWN);
        } else {
            doorBlocks[1] = targetBlock.getRelative(BlockFace.UP);
            doorBlocks[0] = targetBlock;
        }
        return doorBlocks;
    }

    @Override
    public boolean isAdult(Zombie zombie) {
        return !zombie.isBaby();
    }

    @Override
    public void setBaby(Zombie zombie) {
        zombie.setBaby(true);
    }

    @Override
    public void setAdult(Zombie zombie) {
        zombie.setBaby(false);
    }

    @Override
    public boolean isFilledMap(Material material) {
        return material == Material.MAP;
    }
}
