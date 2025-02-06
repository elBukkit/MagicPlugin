package com.elmakers.mine.bukkit.utility.platform.modern2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lockable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Zombie;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.ChatUtils;
import com.elmakers.mine.bukkit.utility.ReflectionUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.SpigotUtils;
import com.elmakers.mine.bukkit.utility.platform.modern.ModernCompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.modern2.populator.OutOfBoundsEntityCleanup;
import com.google.common.collect.Multimap;

public abstract class Modern2CompatibilityUtils extends ModernCompatibilityUtils {

    public Modern2CompatibilityUtils(Platform platform) {
        super(platform);
    }

    protected abstract void setBossBarTitleComponents(BossBar bossBar, String serialized, String fallback);

    @Nullable
    public BlockPopulator createOutOfBoundsPopulator(Logger logger) {
        return new OutOfBoundsEntityCleanup(logger);
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
    public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        if (location == null) return null;
        x = Math.min(x, MAX_ENTITY_RANGE);
        z = Math.min(z, MAX_ENTITY_RANGE);
        // Note this no longer special-cases ComplexParts
        return location.getWorld().getNearbyEntities(location, x, y, z);
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
    public void setTNTSource(TNTPrimed tnt, LivingEntity source) {
        tnt.setSource(source);
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
    public void setGravity(ArmorStand armorStand, boolean gravity) {
        // I think the NMS method may be slightly different, so if things go wrong we'll have to dig deeper
        armorStand.setGravity(gravity);
    }

    @Override
    public void setGravity(Entity entity, boolean gravity) {
        entity.setGravity(gravity);
    }

    @Override
    public void setInvisible(ArmorStand armorStand, boolean invisible) {
        armorStand.setInvisible(invisible);
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

            NamespacedKey namespacedKey = new NamespacedKey(platform.getPlugin(), "modifier");
            EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.ANY;
            if (slot != null && !slot.isEmpty()) {
                try {
                    if (slot.equalsIgnoreCase("mainhand")) {
                        equipmentSlotGroup = EquipmentSlotGroup.MAINHAND;
                    } else if (slot.equalsIgnoreCase("offhand")) {
                        equipmentSlotGroup = EquipmentSlotGroup.OFFHAND;
                    } else {
                        equipmentSlotGroup = EquipmentSlotGroup.getByName(slot.toUpperCase());
                    }
                } catch (Throwable ex) {
                    platform.getLogger().warning("[Magic] invalid attribute slot: " + slot);
                    return false;
                }
            }
            modifier = new AttributeModifier(namespacedKey, value, operation, equipmentSlotGroup);
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
    public boolean isJumping(LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            return player.getCurrentInput().isJump();
        }
        return false;
    }

    @Override
    public float getForwardMovement(LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getCurrentInput().isForward()) {
                // Forward + backwards cancels out
                if (player.getCurrentInput().isBackward()) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (player.getCurrentInput().isBackward()) {
                return -1;
            }
        }
        return 0.0f;
    }

    @Override
    public float getStrafeMovement(LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getCurrentInput().isRight()) {
                // Left + right cancels out
                if (player.getCurrentInput().isLeft()) {
                    return 0;
                } else {
                    return -1;
                }
            } else if (player.getCurrentInput().isLeft()) {
                return 1;
            }
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
    public boolean isPrimaryThread() {
        return Bukkit.isPrimaryThread();
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
    public boolean setLore(ItemStack itemStack, List<String> lore) {
        SpigotUtils spigot = platform.getSpigotUtils();
        if (spigot == null) {
            return super.setLore(itemStack, lore);
        }
        List<String> serializedLore = spigot.serializeLore(lore);
        return setRawLore(itemStack, serializedLore);
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
