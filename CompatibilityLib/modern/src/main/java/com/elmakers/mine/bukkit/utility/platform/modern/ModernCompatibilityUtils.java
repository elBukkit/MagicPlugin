package com.elmakers.mine.bukkit.utility.platform.modern;

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
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Art;
import org.bukkit.Bukkit;
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
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Snow;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fox;
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
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;

import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.ChatUtils;
import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.DoorActionType;
import com.elmakers.mine.bukkit.utility.EnteredStateTracker;
import com.elmakers.mine.bukkit.utility.ReflectionUtils;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.PlatformInterpreter;
import com.elmakers.mine.bukkit.utility.platform.SpigotUtils;
import com.elmakers.mine.bukkit.utility.platform.modern.populator.OutOfBoundsEntityCleanup;
import com.google.common.collect.Multimap;

public abstract class ModernCompatibilityUtils extends com.elmakers.mine.bukkit.utility.platform.base.CompatibilityUtilsBase {

    public ModernCompatibilityUtils(Platform platform) {
        super(platform);
    }

    protected abstract void setBossBarTitleComponents(BossBar bossBar, String serialized, String fallback);

    @Override
    public Collection<BoundingBox> getBoundingBoxes(Block block) {
        VoxelShape voxelShape = block.getCollisionShape();
        Collection<org.bukkit.util.BoundingBox> boxes = voxelShape.getBoundingBoxes();
        if (boxes.isEmpty()) {
            return super.getBoundingBoxes(block);
        }
        List<BoundingBox> converted = new ArrayList<>(boxes.size());
        Vector center = block.getLocation().toVector();

        for (org.bukkit.util.BoundingBox bukkitBB : boxes) {
            converted.add(new BoundingBox(center,
                    bukkitBB.getMinX(), bukkitBB.getMaxX(),
                    bukkitBB.getMinY(), bukkitBB.getMaxY(),
                    bukkitBB.getMinZ(), bukkitBB.getMaxZ())
            );
        }
        return converted;
    }

    @Override
    public Runnable getTaskRunnable(BukkitTask task) {
        return null;
    }

    @Override
    public boolean tame(Entity entity, Player tamer) {
        if (entity instanceof Fox) {
            if (tamer == null) return false;
            Fox fox = (Fox)entity;
            AnimalTamer current = fox.getFirstTrustedPlayer();
            if (current != null && current.getUniqueId().equals(tamer.getUniqueId())) {
                return false;
            }
            fox.setFirstTrustedPlayer(tamer);
        }
        return super.tame(entity, tamer);
    }

    @Override
    public Recipe createSmithingRecipe(String key, ItemStack item, ItemStack source, ItemStack addition) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = new RecipeChoice.ExactChoice(source);
            RecipeChoice additionChoice = new RecipeChoice.ExactChoice(addition);
            return new SmithingRecipe(namespacedKey, item, choice, additionChoice);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating smithing recipe", ex);
        }
        return null;
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }

    @Override
    public boolean isArrow(Entity projectile) {
        return projectile instanceof AbstractArrow;
    }

    @SuppressWarnings("deprecation")
    protected RecipeChoice getChoice(ItemStack item, boolean ignoreDamage) {
        RecipeChoice.ExactChoice exactChoice;
        short maxDurability = item.getType().getMaxDurability();
        if (ignoreDamage && maxDurability > 0) {
            List<ItemStack> damaged = new ArrayList<>();
            for (short damage = 0; damage < maxDurability; damage++) {
                item = item.clone();
                ItemMeta meta = item.getItemMeta();
                if (meta == null || !(meta instanceof org.bukkit.inventory.meta.Damageable))  break;
                org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable)meta;
                damageable.setDamage(damage);
                item.setItemMeta(meta);
                damaged.add(item);
            }
            // Not really deprecated, just a draft API at this point but it works.
            exactChoice = new RecipeChoice.ExactChoice(damaged);
        } else {
            exactChoice = new RecipeChoice.ExactChoice(item);
        }
        return exactChoice;
    }

    @Override
    public FurnaceRecipe createFurnaceRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new FurnaceRecipe(namespacedKey, item, choice, experience, cookingTime);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating furnace recipe", ex);
        }
        return null;
    }

    @Override
    public Recipe createBlastingRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new BlastingRecipe(namespacedKey, item, choice, experience, cookingTime);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating blasting recipe", ex);
        }
        return null;
    }

    @Override
    public Recipe createCampfireRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new CampfireRecipe(namespacedKey, item, choice, experience, cookingTime);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating campfire recipe", ex);
        }
        return null;
    }

    @Override
    public Recipe createSmokingRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage, float experience, int cookingTime) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new SmokingRecipe(namespacedKey, item, choice, experience, cookingTime);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating smoking recipe", ex);
        }
        return null;
    }

    @Override
    public Recipe createStonecuttingRecipe(String key, ItemStack item, ItemStack source, boolean ignoreDamage) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null || source == null) {
            return null;
        }
        try {
            RecipeChoice choice = getChoice(source, ignoreDamage);
            return new StonecuttingRecipe(namespacedKey, item, choice);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating stonecutting recipe", ex);
        }
        return null;
    }

    @Override
    public ShapelessRecipe createShapelessRecipe(String key, ItemStack item, Collection<ItemStack> ingredients, boolean ignoreDamage) {
        NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), key);
        if (item == null) {
            return null;
        }
        ShapelessRecipe recipe;
        try {
            recipe = new ShapelessRecipe(namespacedKey, item);
        } catch (Throwable ex) {
            platform.getLogger().log(Level.SEVERE, "Error creating shapeless recipe", ex);
            return null;
        }
        for (ItemStack ingredient : ingredients) {
            recipe.addIngredient(getChoice(ingredient, ignoreDamage));
        }
        return recipe;
    }

    @Override
    public boolean setRecipeGroup(Recipe recipe, String group) {
        if (recipe instanceof ShapedRecipe) {
            ((ShapedRecipe)recipe).setGroup(group);
            return true;
        }
        if (recipe instanceof CookingRecipe) {
            ((CookingRecipe<?>)recipe).setGroup(group);
            return true;
        }
        if (recipe instanceof StonecuttingRecipe) {
            ((StonecuttingRecipe)recipe).setGroup(group);
            return true;
        }
        return false;
    }

    @Override
    public boolean isTopBlock(Block block) {
        // Yes this is an ugly way to do it.
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Slab) {
            Slab slab = (Slab)blockData;
            Slab.Type slabType = slab.getType();
            return slabType != Slab.Type.BOTTOM;
        }
        return false;
    }

    @Override
    public boolean performDoorAction(Block[] doorBlocks, DoorActionType actionType) {
        BlockData blockData = doorBlocks[0].getBlockData();
        if (!(blockData instanceof Door)) {
            return false;
        }
        Door doorData = (Door)blockData;
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
                break;
            default:
                return false;
        }
        // Going to assume we only need to update one of them?
        doorBlocks[0].setBlockData(doorData);
        return true;
    }

    @Override
    public boolean checkDoorAction(Block[] doorBlocks, DoorActionType actionType) {
        BlockData blockData = doorBlocks[0].getBlockData();
        if (!(blockData instanceof Door)) {
            return false;
        }
        Door doorData = (Door)blockData;
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
        BlockData blockData = targetBlock.getBlockData();
        if (!(blockData instanceof Door)) {
            return null;
        }
        Door doorData = (Door)blockData;
        Block[] doorBlocks = new Block[2];
        if (doorData.getHalf() == Bisected.Half.TOP) {
            doorBlocks[1] = targetBlock;
            doorBlocks[0] = targetBlock.getRelative(BlockFace.DOWN);
        } else {
            doorBlocks[1] = targetBlock.getRelative(BlockFace.UP);
            doorBlocks[0] = targetBlock;
        }
        return doorBlocks;
    }

    @Override
    public boolean setTorchFacingDirection(Block block, BlockFace facing) {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Directional)) {
            return false;
        }
        Directional directional = (Directional)blockData;
        directional.setFacing(facing);
        block.setBlockData(directional);
        return true;
    }

    @Override
    public boolean canToggleBlockPower(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData == null) {
            return false;
        }
        if (blockData instanceof Powerable) {
            return true;
        }
        if (blockData instanceof Lightable) {
            return true;
        }
        if (blockData instanceof AnaloguePowerable) {
            return true;
        }
        return false;
    }

    @Override
    public boolean extendPiston(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData == null) {
            return false;
        }
        if (blockData instanceof Piston) {
            Piston piston = (Piston)blockData;
            piston.setExtended(true);
            block.setBlockData(piston, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean toggleBlockPower(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData == null) {
            return false;
        }
        if (blockData instanceof Powerable) {
            Powerable powerable = (Powerable)blockData;
            powerable.setPowered(!powerable.isPowered());
            block.setBlockData(powerable, true);
            return true;
        }
        if (blockData instanceof Lightable) {
            Lightable lightable = (Lightable)blockData;
            lightable.setLit(!lightable.isLit());
            block.setBlockData(lightable, true);
            return true;
        }
        if (blockData instanceof AnaloguePowerable) {
            AnaloguePowerable powerable = (AnaloguePowerable)blockData;
            powerable.setPower(powerable.getMaximumPower() - powerable.getPower());
            block.setBlockData(powerable, true);
            return true;
        }
        if (blockData instanceof Dispenser) {
            Dispenser dispenser = (Dispenser)blockData;
            dispenser.setTriggered(!dispenser.isTriggered());
        }
        return false;
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
    public boolean isFilledMap(Material material) {
        return material == Material.FILLED_MAP;
    }


    @Override
    public void setMaterialCooldown(Player player, Material material, int duration) {
        player.setCooldown(material, duration);
    }

    @Override
    public void sendBlockChange(Player player, Location location, Material material, String blockData) {
        if (blockData != null) {
            player.sendBlockChange(location, platform.getPlugin().getServer().createBlockData(blockData));
        } else {
            super.sendBlockChange(player, location, material, blockData);
        }
    }

    @Override
    @Nonnull
    public FallingBlock spawnFallingBlock(Location location, Material material, String blockDataString) {
        if (blockDataString != null && !blockDataString.isEmpty()) {
            BlockData blockData = PlatformInterpreter.getPlatform().getPlugin().getServer().createBlockData(blockDataString);
            return location.getWorld().spawnFallingBlock(location, blockData);
        }

        return super.spawnFallingBlock(location, material, blockDataString);
    }

    @Override
    public void setSnowLevel(Block block, int level) {
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Snow) {
            Snow snow = (Snow)blockData;
            snow.setLayers(level);
            block.setBlockData(blockData);
        }
    }

    @Override
    public int getSnowLevel(Block block) {
        int level = 0;
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Snow) {
            Snow snow = (Snow)blockData;
            level = snow.getLayers();
        }
        return level;
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, int size, final String name) {
        size = (int) (Math.ceil((double) size / 9) * 9);
        size = Math.min(size, 54);
        String translatedName = translateColors(name);
        return Bukkit.createInventory(holder, size, translatedName);
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
        World world = location.getWorld();
        Entity bukkitEntity = null;
        try {
            bukkitEntity = world.createEntity(location, entityType.getEntityClass());
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
        x = Math.min(x, MAX_ENTITY_RANGE);
        z = Math.min(z, MAX_ENTITY_RANGE);
        // Note this no longer special-cases ComplexParts
        return location.getWorld().getNearbyEntities(location, x, y, z);
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

                try (EnteredStateTracker.Touchable damaging = isDamaging.enter()) {
                    damaging.touch();
                    NMSUtils.class_EntityLiving_damageEntityMethod.invoke(
                            targetHandle,
                            damageSource,
                            (float) amount);
                }
            } else {
                try (EnteredStateTracker.Touchable damaging = isDamaging.enter()) {
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

        try (EnteredStateTracker.Touchable damaging = isDamaging.enter()) {
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
        return true;
    }

    @Override
    public boolean createExplosion(Entity entity, World world, double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
        return world.createExplosion(x, y, z, power, setFire, breakBlocks, entity);
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
            // Capture current tile entity snapshot
            Object entityData = NMSUtils.class_NBTTagCompound_constructor.newInstance();
            NMSUtils.class_TileEntity_saveMethod.invoke(tileEntity, entityData);

            // Remove items
            NMSUtils.class_NBTTagCompound_removeMethod.invoke(entityData, "Item");
            NMSUtils.class_NBTTagCompound_removeMethod.invoke(entityData, "Items");

            // Reload tile entity
            if (NMSUtils.class_IBlockData != null) {
                Object worldHandle = NMSUtils.getHandle(location.getWorld());
                Object blockLocation = NMSUtils.class_BlockPosition_Constructor.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                Object blockType = NMSUtils.class_World_getTypeMethod.invoke(worldHandle, blockLocation);
                NMSUtils.class_TileEntity_loadMethod.invoke(tileEntity, blockType, entityData);
            } else {
                NMSUtils.class_TileEntity_loadMethod.invoke(tileEntity, entityData);
            }
            NMSUtils.class_TileEntity_updateMethod.invoke(tileEntity);

            // Clear records from jukebox. Equivalent to
            // if (tileEntity instanceof TileEntityRecordPlayer)
            // >> tileEntity.record = null
            if (NMSUtils.class_TileEntityRecordPlayer_record != null
                    && NMSUtils.class_TileEntityRecordPlayer.isInstance(tileEntity)) {
                // TODO: Move into ItemUtils
                Object emptyStack = NMSUtils.class_CraftItemStack_copyMethod.invoke(
                        null,
                        new Object[] { null });
                NMSUtils.class_TileEntityRecordPlayer_record.set(
                        tileEntity,
                        emptyStack);
            }

            // Clear loot table
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
    @SuppressWarnings("deprecation")
    public MapView getMapById(int id) {
        return Bukkit.getMap(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getTypedMap(ConfigurationSection section) {
        if (section == null) return null;
        if (section instanceof MemorySection) {
            return (Map<String, T>) ReflectionUtils.getPrivate(platform.getLogger(), section, MemorySection.class, "map");
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
    public void setTNTSource(TNTPrimed tnt, LivingEntity source) {
        tnt.setSource(source);
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
        armorStand.setInvisible(invisible);
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
        // I think the NMS method may be slightly different, so if things go wrong we'll have to dig deeper
        armorStand.setGravity(gravity);
    }

    @Override
    public void setGravity(Entity entity, boolean gravity) {
        entity.setGravity(gravity);
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
            if (handle != null) {
                NMSUtils.class_Entity_setYawPitchMethod.invoke(handle, yaw, pitch);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
        try {
            Object handle = NMSUtils.getHandle(entity);
            if (handle != null) {
                NMSUtils.class_Entity_setLocationMethod.invoke(handle, x, y, z, yaw, pitch);
            }
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
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            Object tag = platform.getItemUtils().getOrCreateTag(handle);
            if (tag == null) return false;

            Object attributesNode = platform.getNBTUtils().getTag(tag, "AttributeModifiers");
            Object attributeNode = null;

            String attributeName = toMinecraftAttribute(attribute);
            if (attributesNode == null) {
                attributesNode = NMSUtils.class_NBTTagList_constructor.newInstance();
                NMSUtils.class_NBTTagCompound_setMethod.invoke(tag, "AttributeModifiers", attributesNode);
            } else {
                int size = (Integer) NMSUtils.class_NBTTagList_sizeMethod.invoke(attributesNode);
                for (int i = 0; i < size; i++) {
                    Object candidate = NMSUtils.class_NBTTagList_getMethod.invoke(attributesNode, i);
                    String key = platform.getNBTUtils().getString(candidate, "AttributeName");
                    if (key.equals(attributeName)) {
                        attributeNode = candidate;
                        break;
                    }
                }
            }
            if (attributeNode == null) {
                attributeNode = NMSUtils.class_NBTTagCompound_constructor.newInstance();
                platform.getNBTUtils().setString(attributeNode, "AttributeName", attributeName);
                platform.getNBTUtils().setString(attributeNode, "Name", "Equipment Modifier");
                platform.getNBTUtils().setInt(attributeNode, "Operation", attributeOperation);
                platform.getNBTUtils().setLong(attributeNode, "UUIDMost", attributeUUID.getMostSignificantBits());
                platform.getNBTUtils().setLong(attributeNode, "UUIDLeast", attributeUUID.getLeastSignificantBits());
                if (slot != null) {
                    platform.getNBTUtils().setString(attributeNode, "Slot", slot);
                }

                platform.getNBTUtils().addToList(attributesNode, attributeNode);
            }
            platform.getNBTUtils().setDouble(attributeNode, "Amount", value);
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

    @Override
    protected boolean sendActionBarPackets(Player player, String message) {
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
        return material.getBlastResistance();
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
        // Bailed on this in 1.20
        chunk.getBlock(x, y, z).setType(material);
        return true;
    }

    @Override
    public boolean setPickupStatus(Projectile projectile, String pickupStatus) {
        if (!(projectile instanceof AbstractArrow)) return false;
        AbstractArrow.PickupStatus status;
        try {
            status = AbstractArrow.PickupStatus.valueOf(pickupStatus.toUpperCase());
        } catch (Throwable ex) {
            platform.getLogger().warning("Invalid pickup status: " + pickupStatus);
            return false;
        }
        ((AbstractArrow)projectile).setPickupStatus(status);
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

    @SuppressWarnings("deprecation")
    protected Attribute getMaxHealthAttribute() {
        // Paper and Spigot can no longer agree on what this should be called????
        // Also stop deprecating crap that has no alternative omg
        Attribute attribute = null;
        try {
            Attribute.valueOf("MAX_HEALTH");
        } catch (Exception ignore) {
            // Current spigot API claims this should be called MAX_HEALTH
            // But running in Paper throws an error for that.
        }
        if (attribute == null) {
            attribute = Attribute.valueOf("GENERIC_MAX_HEALTH");
        }
        return attribute;
    }

    @Override
    public double getMaxHealth(Damageable li) {
        if (li instanceof LivingEntity) {
            return ((LivingEntity)li).getAttribute(getMaxHealthAttribute()).getValue();
        }
        return 0;
    }

    @Override
    public void setMaxHealth(Damageable li, double maxHealth) {
        if (li instanceof LivingEntity) {
            ((LivingEntity)li).getAttribute(getMaxHealthAttribute()).setBaseValue(maxHealth);
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
        return meta.getColor();
    }

    @Override
    public boolean setColor(PotionMeta meta, Color color) {
        meta.setColor(color);
        return true;
    }

    @Override
    public boolean hasBlockDataSupport() {
        return true;
    }

    @Override
    public byte getLegacyBlockData(FallingBlock falling) {
        return 0;
    }

    @Override
    public Material getMaterial(FallingBlock falling) {
        return falling.getBlockData().getMaterial();
    }

    @Override
    public String getBlockData(FallingBlock fallingBlock) {
        BlockData blockData = fallingBlock.getBlockData();
        return blockData.getAsString();
    }

    @Override
    public String getBlockData(Material material, byte data) {
        @SuppressWarnings("deprecation")
        BlockData blockData = platform.getDeprecatedUtils().getUnsafe().fromLegacy(material, data);
        return blockData == null ? null : blockData.getAsString();
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
                for (short damage = 0; damage < maxDurability; damage++) {
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
            entity.setSilent(true);
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
        return Bukkit.isPrimaryThread();
    }

    @Override
    public boolean isAdult(Zombie zombie) {
        return zombie.isAdult();
    }

    @Override
    public void setBaby(Zombie zombie) {
        zombie.setBaby();
    }

    @Override
    public void setAdult(Zombie zombie) {
        zombie.setAdult();
    }

    @Override
    public int getMinHeight(World world) {
        return world.getMinHeight();
    }

    @Override
    public BlockFace getSignFacing(Block signBlock) {
        BlockData blockData = signBlock.getBlockData();
        if (!(blockData instanceof WallSign)) {
            return null;
        }
        WallSign sign = (WallSign)blockData;
        return sign.getFacing();
    }

    @Override
    public void openSign(Player player, Location signBlock) {
        try {
            Object tileEntity = platform.getCompatibilityUtils().getTileEntity(signBlock);
            Object playerHandle = NMSUtils.getHandle(player);
            if (tileEntity != null && playerHandle != null) {
                NMSUtils.class_EntityPlayer_openSignMethod.invoke(playerHandle, tileEntity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<String> getRawLore(ItemStack itemStack) {
        List<String> lore = new ArrayList<>();
        Object displayNode = platform.getNBTUtils().getTag(itemStack, "display");
        if (displayNode == null) {
            return lore;
        }
        return platform.getItemUtils().getStringList(displayNode, "Lore");
    }

    @Override
    public boolean setRawLore(ItemStack itemStack, List<String> lore) {
        Object displayNode = platform.getNBTUtils().createTag(itemStack, "display");
        platform.getItemUtils().setStringList(displayNode, "Lore", lore);
        return true;
    }

    @Override
    public boolean setLore(ItemStack itemStack, List<String> lore) {
        SpigotUtils spigot = platform.getSpigotUtils();
        if (spigot == null) {
            return super.setLore(itemStack, lore);
        }
        List<String> serializedLore = spigot.serializeLore(lore);
        return setRawLore(itemStack, serializedLore);
    }

    @Nullable
    public BlockPopulator createOutOfBoundsPopulator(Logger logger) {
        return new OutOfBoundsEntityCleanup(logger);
    }

    @Override
    public String getEnchantmentKey(Enchantment enchantment) {
        // We don't use toString here since we'll be parsing this ourselves
        return enchantment.getKey().getNamespace() + ":" + enchantment.getKey().getKey();
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
    public boolean setCompassTarget(ItemMeta meta, Location targetLocation, boolean trackLocation) {
        if (meta == null || !(meta instanceof CompassMeta)) {
            return false;
        }
        CompassMeta compassMeta = (CompassMeta)meta;
        compassMeta.setLodestoneTracked(trackLocation);
        compassMeta.setLodestone(targetLocation);
        return true;
    }

    @Override
    public boolean isAware(Entity entity) {
        if (!(entity instanceof org.bukkit.entity.Mob)) {
            return true;
        }
        return ((org.bukkit.entity.Mob)entity).isAware();
    }

    @Override
    public void setAware(Entity entity, boolean aware) {
        if (!(entity instanceof org.bukkit.entity.Mob)) {
            return;
        }
        ((org.bukkit.entity.Mob)entity).setAware(aware);
    }

    @Override
    public boolean setBossBarTitle(BossBar bossBar, String title, String font) {
        if (ChatUtils.isDefaultFont(font)) {
            setBossBarTitle(bossBar, title);
            return true;
        }
        SpigotUtils spigot = platform.getSpigotUtils();
        if (spigot == null) {
            // Can't do fonts without chat components
            return false;
        }
        setBossBarTitleComponents(bossBar, spigot.serializeBossBar(title, font), title);
        return true;
    }

    @Override
    public void setBossBarTitle(BossBar bossBar, String title) {
        if (ChatUtils.hasJSON(title)) {
            SpigotUtils spigot = platform.getSpigotUtils();
            if (spigot != null) {
                setBossBarTitleComponents(bossBar, spigot.serializeBossBar(title), title);
            } else {
                bossBar.setTitle(ChatUtils.getSimpleMessage(title));
            }
        } else {
            bossBar.setTitle(title);
        }
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
}
