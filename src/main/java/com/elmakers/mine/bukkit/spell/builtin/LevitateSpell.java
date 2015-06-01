package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.effect.builtin.EffectRing;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import de.slikey.effectlib.util.ParticleEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.spell.SpellEventType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

public class LevitateSpell extends TargetingSpell implements Listener
{
    private static final BlockFace[] CHECK_FACES = {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};

	private static final float defaultFlySpeed = 0.1f;

	private final int safetyLength = 10000;
    
    private float flySpeed = 0;
    private int flyDelay = 2;
    private int startDelay = 0;

    private int autoDeactivateHeight = 0;
    private int boostTicksRemaining = 0;

    private double castBoost = 0;
    private int boostTicks = 0;
    private double yBoost = 2;
    private float thrustSpeed = 0;
    private int thrustFrequency = 1;
    private ThrustAction thrust;
    private double crashDistance = 0;
    private double slowMultiplier = 1;

    private boolean mountSilent = true;
    private boolean mountBaby = false;
    private boolean smallArmorStand = false;
    private boolean useArmorStand = false;
    private boolean armorStandMarker = false;
    private boolean useHelmet = false;
    private Material mountItem = null;
    private Vector armorStandArm = null;
    private Vector armorStandHead = null;
    private double pitchAmount = 0;
    private ArmorStand armorStand = null;
    private EntityType mountType = null;
    private Entity mountEntity = null;
    private Horse.Variant mountHorseVariant = null;
    private Horse.Color mountHorseColor = null;
    private Horse.Style mountHorseStyle = null;
    private double maxMountBoost = 1;
    private double mountBoostPerJump = 0.5;
    private double mountHealth = 8;
    private int slowReduceBoostTicks = 4;
    private int mountBoostTicks = 80;
    private boolean mountInvisible = true;
    private int forceSneak = 0;
    private double moveDistance = 0;
    private double sneakMoveDistance = -1;
    private CreatureSpawnEvent.SpawnReason mountSpawnReason = CreatureSpawnEvent.SpawnReason.CUSTOM;

    private Vector direction = null;

    private boolean stashItem = false;
    private ItemStack heldItem = null;
    private int heldItemSlot = 0;

    private int mountBoostTicksRemaining = 0;

    private static LevitateListener listener = null;

    private Collection<PotionEffect> crashEffects;

    private Sound effectSound = null;
    private int effectSoundInterval = 20;
    private int effectSoundCounter = 0;
    private float effectSoundVolume = 1;
    private float effectSoundPitch = 1;

    private ParticleEffect effectParticle = null;
    private float effectParticleData = 0;
    private int effectParticleCount = 0;
    private int effectParticleInterval = 20;
    private int effectParticleCounter = 0;

    private EffectRing effectPlayer = null;

    private class ThrustAction implements Runnable
    {
        private final LevitateSpell spell;
        private final int taskId;

        public ThrustAction(LevitateSpell spell, int delay, int interval)
        {
            Plugin plugin = spell.getMage().getController().getPlugin();
            this.spell = spell;
            BukkitScheduler scheduler = Bukkit.getScheduler();
            taskId = scheduler.scheduleSyncRepeatingTask(plugin, this, delay, interval);
        }

        public void stop()
        {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        public void run()
        {
            if (!spell.canCast(spell.getLocation()))
            {
                spell.land();
                return;
            }
            if (!spell.checkActive())
            {
                return;
            }
            Entity entity = spell.getMage().getEntity();
            if (entity == null || entity.isDead())
            {
                spell.land();
                return;
            }
            if (entity instanceof Player && !((Player)entity).isOnline()) {
                spell.land();
                return;
            }

            spell.thrust();
        }
    }

    private class LevitateListener implements Listener
    {
        private final MageController controller;

        public LevitateListener(MageController controller)
        {
            this.controller = controller;
        }

        @EventHandler
        public void onHorseJump(HorseJumpEvent event)
        {
            Entity horse = event.getEntity();
            if (horse.hasMetadata("broom"))
            {
                Entity passenger = horse.getPassenger();
                Mage mage = controller.getMage(passenger);
                Set<Spell> active = mage.getActiveSpells();
                for (Spell spell : active) {
                    if (spell instanceof LevitateSpell) {
                        LevitateSpell levitate = (LevitateSpell)spell;
                        levitate.boost(event.getPower());
                    }
                }
            }
        }

        @EventHandler
        public void onInventoryOpen(InventoryOpenEvent event)
        {
            HumanEntity player = event.getPlayer();
            Entity mount = player.getVehicle();
            if (mount != null && mount.hasMetadata("broom")) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onVehicleExit(VehicleExitEvent event)
        {
            Entity vehicle = event.getVehicle();
            if (vehicle.hasMetadata("broom"))
            {
                event.setCancelled(true);
                Entity passenger = vehicle.getPassenger();
                Mage mage = controller.getMage(passenger);
                Set<Spell> active = mage.getActiveSpells();
                for (Spell spell : active) {
                    if (spell instanceof LevitateSpell) {
                        LevitateSpell levitate = (LevitateSpell)spell;
                        levitate.forceSneak(10);
                    }
                }
            }
        }
    }

    protected void thrust()
    {
        if (thrustSpeed == 0) return;
        Player player = mage.getPlayer();
        if (player == null) return;

        if (mountEntity != null) {
            Entity currentMount = player.getVehicle();
            if (currentMount != mountEntity) {
                crash();
                return;
            }
            if (armorStand != null) {
                Entity currentPassenger = armorStand.getPassenger();
                if (currentPassenger != mountEntity) {
                    crash();
                    return;
                }
            }
        }

        boolean checkHeight = autoDeactivateHeight > 0;
        if (checkHeight && mage.isPlayer()) {
            checkHeight = mage.getPlayer().isSneaking();
        }
        if (checkHeight) {
            int height = 0;
            Block block = player.getLocation().getBlock();
            while (height < autoDeactivateHeight && block.getType() == Material.AIR)
            {
                block = block.getRelative(BlockFace.DOWN);
                height++;
            }

            if (height < autoDeactivateHeight)
            {
                land();
                return;
            }
        }
        Location location = player.getLocation();
        Vector mageDirection = location.getDirection();
        boolean sneaking = player.isSneaking() || forceSneak > 0;
        double move = sneakMoveDistance >= 0 && sneaking ? sneakMoveDistance : moveDistance;
        if (direction == null || move <= 0) {
            direction = mageDirection;
        } else {
            double moveDistanceSquared = move * move;
            double distanceSquared = direction.distanceSquared(mageDirection);
            if (distanceSquared <= moveDistanceSquared) {
                direction = mageDirection;
            } else {
                Vector targetDirection = mageDirection.subtract(direction).normalize().multiply(move);
                direction.add(targetDirection);
            }
        }
        direction.normalize();

        if (crashDistance > 0)
        {
            Vector threshold = direction.clone().multiply(crashDistance);
            if (checkForCrash(mage.getEyeLocation(), threshold)) {
                crash();
                return;
            }
            if (checkForCrash(mage.getLocation(), threshold)) {
                crash();
                return;
            }
        }

        double boost = thrustSpeed;

        if (mage.getPlayer().isSneaking() || forceSneak > 0) {
            forceSneak--;
            if (slowReduceBoostTicks > 0) {
                mountBoostTicksRemaining = Math.max(0, mountBoostTicksRemaining - slowReduceBoostTicks);
                updateMountHealth();
            }
            if (mountBoostTicksRemaining == 0) {
                boost *= slowMultiplier;
            }
        }
        if (mountBoostTicksRemaining > 0 && mountBoostTicks > 0) {
            boost += (maxMountBoost * ((double)mountBoostTicksRemaining / mountBoostTicks));
            --mountBoostTicksRemaining;
            updateMountHealth();
        }
        else if (boostTicksRemaining > 0) {
            boost += castBoost;
            --boostTicksRemaining;
        }
        direction.multiply(boost);
        if (mountEntity != null) {
            if (armorStand != null) {
                armorStand.setVelocity(direction);
                CompatibilityUtils.setYawPitch(armorStand, location.getYaw(), location.getPitch());
                if (pitchAmount != 0) {
                    if (armorStandHead == null && useHelmet) {
                        armorStand.setHeadPose(new EulerAngle(pitchAmount * location.getPitch() / 180 * Math.PI, 0, 0));
                    } else if (!useHelmet) {
                        EulerAngle armPose = armorStand.getRightArmPose();
                        armPose.setY(pitchAmount * location.getPitch() / 180 * Math.PI);
                        armorStand.setRightArmPose(armPose);
                    }
                }
            } else {
                mountEntity.setVelocity(direction);
                if (mountEntity instanceof ArmorStand) {
                    CompatibilityUtils.setYawPitch(mountEntity, location.getYaw(), location.getPitch());
                }
            }
        } else {
            player.setVelocity(direction);
        }

        if (effectParticle != null) {
            if ((effectParticleCounter++ % effectParticleInterval) == 0) {
                if (effectPlayer == null) {
                    effectPlayer = new EffectRing(controller.getPlugin());
                    effectPlayer.setParticleCount(2);
                    effectPlayer.setIterations(2);
                    effectPlayer.setRadius(2);
                    effectPlayer.setSize(5);
                    effectPlayer.setMaterial(location.getBlock().getRelative(BlockFace.DOWN));
                }
                effectPlayer.setParticleType(effectParticle);
                effectPlayer.setParticleData(effectParticleData);
                effectPlayer.setParticleCount(effectParticleCount);
                effectPlayer.start(player.getEyeLocation(), null);
            }
        }

        if (effectSound != null && controller.soundsEnabled()) {
            if ((effectSoundCounter++ % effectSoundInterval) == 0) {
                mage.getLocation().getWorld().playSound(location, effectSound, effectSoundVolume, effectSoundPitch);
            }
        }
    }

    protected void crash()
    {
        deactivate(true, false);
        sendMessage(getMessage("crash"));
        mage.deactivateAllSpells();
        playEffects("crash");
        LivingEntity livingEntity = mage.getLivingEntity();
        if (crashEffects != null && livingEntity != null && crashEffects.size() > 0) {
            CompatibilityUtils.applyPotionEffects(livingEntity, crashEffects);
        }
    }

    protected boolean checkForCrash(Location source, Vector threshold)
    {
        Block facingBlock = source.getBlock();
        Block targetBlock = source.add(threshold).getBlock();

        if (!targetBlock.equals(facingBlock) && !isPassthrough(targetBlock.getType())) {
            return true;
        }

        return false;
    }

    protected boolean checkActive()
    {
        if (!isActive()) return false;

        Entity entity = mage.getEntity();
        if (entity == null || entity.isDead()) return false;
        if (entity instanceof Player && !((Player)entity).isOnline()) return false;

        return true;
    }

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }

        int checkHeight = parameters.getInt("check_height", 4);
        startDelay = parameters.getInt("start_delay", 0);
        flyDelay = parameters.getInt("fly_delay", 2);
        slowMultiplier = parameters.getDouble("slow", 1);
        castBoost = parameters.getDouble("boost", 0);
        yBoost = parameters.getDouble("y_boost", 2);
		flySpeed = (float)parameters.getDouble("speed", 0);
        thrustSpeed = (float)parameters.getDouble("thrust", 0);
        thrustFrequency = parameters.getInt("thrust_interval", thrustFrequency);
        autoDeactivateHeight = parameters.getInt("auto_deactivate", 0);
        boostTicks = parameters.getInt("boost_ticks", 1);
        crashDistance = parameters.getDouble("crash_distance", 0);
        slowReduceBoostTicks = parameters.getInt("slow_ticks", 4);
        moveDistance = parameters.getDouble("steer_speed", 0);
        sneakMoveDistance = parameters.getDouble("slow_steer_speed", -1);
        if (parameters.contains("mount_item")) {
            mountItem = ConfigurationUtils.getMaterial(parameters, "mount_item");
        } else {
            mountItem = null;
        }
        mountBaby = parameters.getBoolean("mount_baby", false);
        useHelmet = parameters.getBoolean("armor_stand_helmet", false);
        useArmorStand = parameters.getBoolean("armor_stand", false);
        armorStandMarker = parameters.getBoolean("armor_stand_marker", false);
        smallArmorStand = parameters.getBoolean("armor_stand_small", false);
        armorStandArm = ConfigurationUtils.getVector(parameters, "armor_stand_arm");
        armorStandHead = ConfigurationUtils.getVector(parameters, "armor_stand_head");
        pitchAmount = ConfigurationUtils.getDouble(parameters, "armor_stand_pitch", 0.0);

        maxMountBoost = parameters.getDouble("mount_boost", 1);
        mountBoostPerJump = parameters.getDouble("mount_boost_per_jump", 0.5);
        mountBoostTicks = parameters.getInt("mount_boost_ticks", 40);
        mountHealth = parameters.getDouble("mount_health", 2);
        mountInvisible = parameters.getBoolean("mount_invisible", true);
        mountSilent = parameters.getBoolean("mount_silent", true);
        stashItem = parameters.getBoolean("stash_item", false);

        mountBoostTicks += mage.getPower() * parameters.getInt("power_mount_boost_ticks", 0);
        mountHealth += mage.getPower() * parameters.getDouble("power_mount_health", 0);
        if (moveDistance != 0) {
            moveDistance += mage.getPower() * parameters.getDouble("power_steer_speed", 0);
        }
        mountBoostPerJump += mage.getPower() * parameters.getDouble("power_mount_boost_per_jump", 0);
        slowReduceBoostTicks +=  mage.getPower() * parameters.getDouble("power_slow_ticks", 0);

        if (moveDistance < 0) {
            moveDistance = 0;
        }

        // FX
        if (parameters.contains("effect_particle")) {
            parseParticleEffect(parameters.getString("effect_particle"));
            effectParticleData = 0;
        } else {
            effectParticle = null;
        }
        if (parameters.contains("effect_sound")) {
            parseSoundEffect(parameters.getString("effect_sound"));
        } else {
            effectSound = null;
        }
        effectParticleData = (float)parameters.getDouble("effect_particle_data", effectParticleData);
        effectParticleCount = parameters.getInt("effect_particle_count", effectParticleCount);
        effectParticleInterval = parameters.getInt("effect_particle_interval", effectParticleInterval);
        effectSoundInterval =  parameters.getInt("effect_sound_interval", effectSoundInterval);
        effectSoundVolume = (float)parameters.getDouble("effect_sound_volume", effectSoundVolume);
        effectSoundPitch = (float)parameters.getDouble("effect_sound_pitch", effectSoundPitch);
        effectSoundCounter = 0;
        effectParticleCounter = 0;

        if (parameters.contains("mount_reason")) {
            String reasonText = parameters.getString("mount_reason").toUpperCase();
            try {
                mountSpawnReason = CreatureSpawnEvent.SpawnReason.valueOf(reasonText);
            } catch (Exception ex) {
                sendMessage("Unknown spawn reason: " + reasonText);
                return SpellResult.FAIL;
            }
        }

        if (parameters.contains("mount_type")) {
            try {
                String entityType = parameters.getString("mount_type");
                mountType = EntityType.valueOf(entityType.toUpperCase());
            } catch (Exception ex) {
                ex.printStackTrace();
                return SpellResult.FAIL;
            }
        } else {
            mountType = null;
        }

        if (parameters.contains("mount_variant")) {
            try {
                String variantString = parameters.getString("mount_variant");
                mountHorseVariant = Horse.Variant.valueOf(variantString.toUpperCase());
            } catch (Exception ex) {
                ex.printStackTrace();
                return SpellResult.FAIL;
            }
        } else {
            mountHorseVariant = Horse.Variant.HORSE;
        }

        if (parameters.contains("mount_color")) {
            try {
                String colorString = parameters.getString("mount_color");
                mountHorseColor = Horse.Color.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
                ex.printStackTrace();
                return SpellResult.FAIL;
            }
        } else {
            mountHorseColor = Horse.Color.WHITE;
        }

        if (parameters.contains("mount_style")) {
            try {
                String styleString = parameters.getString("mount_style");
                mountHorseStyle = Horse.Style.valueOf(styleString.toUpperCase());
            } catch (Exception ex) {
                ex.printStackTrace();
                return SpellResult.FAIL;
            }
        } else {
            mountHorseStyle = Horse.Style.NONE;
        }

        crashEffects = getPotionEffects(parameters);

        double speedBonus = parameters.getDouble("power_speed", 0);
        double boostBonus = parameters.getDouble("power_boost", 0);
        double maxSpeedBonus = parameters.getDouble("power_mount_boost", 0);
        thrustSpeed += mage.getPower() * speedBonus;
        castBoost += mage.getPower() * boostBonus;
        maxMountBoost += mage.getPower() * maxSpeedBonus;

		if (isActive()) {
            if (castBoost != 0) {
                boostTicksRemaining += boostTicks;
                return SpellResult.ALTERNATE;
            } else if (mountEntity != null && thrust != null) {
                return SpellResult.NO_ACTION;
            }
            land();
			return SpellResult.DEACTIVATE;
		}

        if (mountType != null) {
            Location testLocation = getEyeLocation();
            for (BlockFace facing : CHECK_FACES) {
                Block block = testLocation.getBlock().getRelative(facing);
                if (!isOkToStandIn(block.getType())) {
                    return SpellResult.FAIL;
                }
            }
            Block block = testLocation.getBlock();
            for (int i = 0; i < checkHeight; i++) {
                if (!isOkToStandIn(block.getType())) {
                    return SpellResult.FAIL;
                }
                block = block.getRelative(BlockFace.UP);
            }
        }

        if (stashItem) {
            PlayerInventory inventory = player.getInventory();
            heldItemSlot = inventory.getHeldItemSlot();
        }

		activate();

		return SpellResult.CAST;
	}

    protected void parseSoundEffect(String effectSoundName) {
        if (effectSoundName.length() > 0) {
            String soundName = effectSoundName.toUpperCase();
            try {
                effectSound = Sound.valueOf(soundName);
            } catch (Exception ex) {
                effectSound = null;
            }
        } else {
            effectSound = null;
        }
    }

    protected void parseParticleEffect(String effectParticleName) {
        if (effectParticleName.length() > 0) {
            String particleName = effectParticleName.toUpperCase();
            try {
                effectParticle = ParticleEffect.valueOf(particleName);
            } catch (Exception ex) {
                effectParticle = null;
            }
        } else {
            effectParticle = null;
        }
    }

    public void boost(double amount)
    {
        if (maxMountBoost > 0 && mountBoostTicks > 0) {
            if (mountBoostTicksRemaining == 0) {
                playEffects("boost");
            }
            mountBoostTicksRemaining = (int)Math.min((double)mountBoostTicksRemaining + mountBoostPerJump * mountBoostTicks * amount, mountBoostTicks);
            updateMountHealth();
        }
    }

    protected void updateMountHealth() {
        if (mountEntity != null && mountBoostTicks > 0 && mountEntity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)mountEntity;
            double maxHealth = living.getMaxHealth();
            double health = Math.min(0.5 + maxHealth * mountBoostTicksRemaining / mountBoostTicks, maxHealth);
            living.setHealth(health);
        }
    }

    public void land() {
        deactivate(true, false);
    }

	@Override
	public void onDeactivate() {
        if (safetyLength > 0) {
            mage.enableFallProtection(safetyLength, this);
        }
        if (thrust != null) {
            thrust.stop();
            thrust = null;
        }
        Entity mageEntity = mage.getEntity();
        if (mountEntity != null) {
            if (mageEntity != null) {
                mageEntity.eject();
            }
            if (armorStand != null) {
                armorStand.remove();
            }
            if (mountEntity instanceof Horse) {
                Horse horse = (Horse)mountEntity;
                horse.getInventory().clear();
            }
            if (mountEntity instanceof Pig) {
                Pig pig = (Pig)mountEntity;
                pig.setSaddle(false);
            }
            mountEntity.eject();
            mountEntity.setPassenger(null);
            Plugin plugin = controller.getPlugin();
            mountEntity.removeMetadata("notarget", plugin);
            mountEntity.removeMetadata("broom", plugin);
            CompatibilityUtils.setInvulnerable(mountEntity, false);
            if (mountEntity instanceof LivingEntity) {
                ((LivingEntity)mountEntity).setHealth(0);
            }
            mountEntity.remove();
            mountEntity = null;
        }
        final Player player = mage.getPlayer();
        if (player == null) return;
		
		if (flySpeed > 0) {
			player.setFlySpeed(defaultFlySpeed);
		}

        if (heldItem != null) {
            Inventory inventory = mage.getInventory();
            ItemStack current = inventory.getItem(heldItemSlot);
            inventory.setItem(heldItemSlot, heldItem);
            if (player.getInventory().getHeldItemSlot() == heldItemSlot && Wand.isWand(heldItem)) {
                Wand wand = new Wand((MagicController)controller, heldItem);
                wand.activate(mage);
            }
            if (current != null && current.getType() != Material.AIR) {
                controller.giveItemToPlayer(player, current);
            }
            heldItem = null;
        }
		
		player.setFlying(false);
		player.setAllowFlight(false);
	}
	
	@Override
	public void onActivate() {
		final Player player = mage.getPlayer();
		if (player == null) return;

        // Prevent the player from death by fall
        direction = null;
        mountBoostTicksRemaining = 0;
        boostTicksRemaining = 0;

        if (stashItem) {
            final PlayerInventory inventory = player.getInventory();
            heldItem = inventory.getItem(heldItemSlot);
            Plugin plugin = controller.getPlugin();
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    com.elmakers.mine.bukkit.api.wand.Wand wand = mage.getActiveWand();
                    if (wand != null) {
                        wand.deactivate();
                    }
                    heldItem = inventory.getItem(heldItemSlot);
                    inventory.setItem(heldItemSlot, null);
                }
            }, 1);
        } else {
            heldItem = null;
        }

		if (flySpeed > 0) {
			player.setFlySpeed(flySpeed * defaultFlySpeed);
		}

        if (thrustSpeed > 0) {
            if (thrust != null) {
                thrust.stop();
            }
            thrust = new ThrustAction(this, thrustFrequency + flyDelay + startDelay, thrustFrequency);
        }

        if (mountType != null) {
            Location location = mage.getLocation();
            World world = location.getWorld();
            Entity entity = null;
            try {
                String mountName = mountType.getName();
                if (mountName.indexOf("Entity") != 0) {
                    mountName = "Entity" + mountName;
                }
                Class<?> mountClass = NMSUtils.getBukkitClass("net.minecraft.server." + mountName);
                if (mountClass != null) {
                    final Class<?> worldClass = NMSUtils.getBukkitClass("net.minecraft.server.World");
                    final Class<?> entityClass = NMSUtils.getBukkitClass("net.minecraft.server.Entity");
                    Constructor<? extends Object> constructor = mountClass.getConstructor(worldClass);
                    Object nmsWorld = NMSUtils.getHandle(world);
                    Object nmsEntity = constructor.newInstance(nmsWorld);
                    entity = NMSUtils.getBukkitEntity(nmsEntity);
                    if (entity != null) {
                        if (entity instanceof LivingEntity && mountInvisible) {
                            ((LivingEntity)entity).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 2 << 24, 0));
                        }
                        if (mountSilent) {
                            CompatibilityUtils.setSilent(entity, true);
                        }
                        Method setLocationMethod = mountClass.getMethod("setLocation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
                        setLocationMethod.invoke(nmsEntity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
                        Method addEntityMethod = worldClass.getMethod("addEntity", entityClass, CreatureSpawnEvent.SpawnReason.class);
                        addEntityMethod.invoke(nmsWorld, nmsEntity, mountSpawnReason);
                    } else {
                        mage.sendMessage("Failed to spawn entity of type: " + mountType + " (" + mountType.getName() + ")");
                        return;
                    }
                } else {
                    mage.sendMessage("Invalid entity type: " + mountType + " (" + mountType.getName() + ")");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (entity != null) {
                mountEntity = entity;
                CompatibilityUtils.setInvulnerable(mountEntity);

                if (entity instanceof Horse) {
                    Horse horse = (Horse) mountEntity;
                    horse.setTamed(true);
                    horse.setOwner(player);
                    horse.setAdult();
                    horse.setStyle(mountHorseStyle);
                    horse.setVariant(mountHorseVariant);
                    horse.setColor(mountHorseColor);
                    horse.getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));
                    if (mountItem != null) {
                        horse.getInventory().setArmor(new ItemStack(mountItem, 1));
                    }
                    if (mountBaby) {
                        horse.setBaby();
                    }
                }
                if (entity instanceof Pig) {
                    Pig pig = (Pig) entity;
                    pig.setSaddle(true);
                    if (mountBaby) {
                       pig.setBaby();
                    }
                }
                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity)mountEntity;
                    living.setHealth(0.5);
                    living.setMaxHealth(mountHealth);
                }
                if (entity instanceof ArmorStand) {
                    ArmorStand armorStand = (ArmorStand)entity;
                    configureArmorStand(armorStand);
                }
                else if (useArmorStand) {
                    armorStand = (ArmorStand) mage.getLocation().getWorld().spawnEntity(mage.getLocation(), EntityType.ARMOR_STAND);
                    configureArmorStand(armorStand);
                    armorStand.setPassenger(mountEntity);
                } else {
                    armorStand = null;
                }

                mountEntity.setPassenger(mage.getEntity());
                mountEntity.setMetadata("notarget", new FixedMetadataValue(controller.getPlugin(), true));
                mountEntity.setMetadata("broom", new FixedMetadataValue(controller.getPlugin(), true));

                if (listener == null) {
                    listener = new LevitateListener(controller);
                    Plugin plugin = controller.getPlugin();
                    plugin.getServer().getPluginManager().registerEvents(listener, plugin);
                }
            }
        }
		
		Vector velocity = player.getVelocity();
		velocity.setY(velocity.getY() + yBoost);
        if (mountEntity != null) {
            if (armorStand != null) {
                armorStand.setVelocity(velocity);
            } else {
                mountEntity.setVelocity(velocity);
            }
        } else {
            player.setVelocity(velocity);
        }
		Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new Runnable() {
			public void run() {
				player.setAllowFlight(true);
				player.setFlying(true);
			}
		}, flyDelay);
	}

    protected void configureArmorStand(ArmorStand armorStand) {
        if (useHelmet) {
            armorStand.setHelmet(heldItem);
        } else {
            armorStand.setItemInHand(heldItem);
        }
        armorStand.setMetadata("notarget", new FixedMetadataValue(controller.getPlugin(), true));
        armorStand.setMetadata("broom", new FixedMetadataValue(controller.getPlugin(), true));
        if (mountInvisible) {
            CompatibilityUtils.setInvisible(armorStand, true);
        }
        if (armorStandMarker) {
            CompatibilityUtils.setMarker(armorStand, true);
        }
        CompatibilityUtils.setGravity(armorStand, false);
        CompatibilityUtils.setDisabledSlots(armorStand, 2039552);
        if (armorStandArm != null) {
            armorStand.setRightArmPose(new EulerAngle(armorStandArm.getX(), armorStandArm.getY(), armorStandArm.getZ()));
        }
        if (armorStandHead != null) {
            armorStand.setHeadPose(new EulerAngle(armorStandHead.getX(), armorStandHead.getY(), armorStandHead.getZ()));
        }
        if (smallArmorStand) {
            CompatibilityUtils.setSmall(armorStand, true);
        }
    }

    public void forceSneak(int ticks) {
        forceSneak = ticks;
    }

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getEffectMaterial()
    {
        Block block = mage.getEntity().getLocation().getBlock();
        block = block.getRelative(BlockFace.DOWN);
        return new MaterialAndData(block);
    }
}
