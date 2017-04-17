package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.effect.SoundEffect;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;

public class MountArmorStandAction extends BaseSpellAction
{
    private boolean armorStandInvisible;
    private boolean armorStandSmall;
    private boolean armorStandMarker;
    private boolean armorStandGravity;
    private boolean mountWand;
    private double armorStandPitch = 0;
    private double armorStandYaw = 0;
    private double moveDistance = 0;
    private double startSpeed = 0;
    private double minSpeed = 0;
    private double maxSpeed = 0;
    private double maxAcceleration = 0;
    private double maxDeceleration = 0;
    private double liftoffThrust = 0;
    private double crashDistance = 0;
    private int duration = 0;
    private int durationWarning = 0;
    private int liftoffDuration = 0;
    private int maxHeightAboveGround;
    private int maxHeight;
    private double pitchOffset = 0;
    private Double yDirection = null;
    private CreatureSpawnEvent.SpawnReason armorStandSpawnReason = CreatureSpawnEvent.SpawnReason.CUSTOM;
    private Collection<PotionEffect> crashEffects;
    private Collection<PotionEffect> warningEffects;

    private SoundEffect sound = null;
    private int soundInterval = 1000;
    private float soundMaxVolume = 1;
    private float soundMinVolume = 1;
    private float soundMaxPitch = 1;
    private float soundMinPitch = 1;

    private ItemStack item;
    private int slotNumber;
    private long liftoffTime;
    private ArmorStand armorStand;
    private Vector direction;
    private double speed;
    private boolean mounted;
    private boolean warningEffectsApplied;
    private long nextSoundPlay;
    private boolean mountTarget = false;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);
        
        crashEffects = ConfigurationUtils.getPotionEffects(parameters.getConfigurationSection("crash_effects"));
        durationWarning = parameters.getInt("duration_warning", 0);
        warningEffects = ConfigurationUtils.getPotionEffects(parameters.getConfigurationSection("warning_effects"), durationWarning);
    }

    @Override
    public void reset(CastContext context)
    {
        super.reset(context);
        mounted = false;
        item = null;
        if (armorStand != null) {
            armorStand.remove();
        }
        armorStand = null;
        warningEffectsApplied = false;
        nextSoundPlay = 0;
    }
    
    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        mountTarget = parameters.getBoolean("mount_target", false);
        armorStandInvisible = parameters.getBoolean("armor_stand_invisible", true);
        armorStandSmall = parameters.getBoolean("armor_stand_small", false);
        armorStandMarker = parameters.getBoolean("armor_stand_marker", true);
        armorStandGravity = parameters.getBoolean("armor_stand_gravity", true);
        armorStandPitch = parameters.getDouble("armor_stand_pitch", 0.0);
        armorStandYaw = ConfigurationUtils.getDouble(parameters, "armor_stand_yaw", 0.0);
        moveDistance = parameters.getDouble("steer_speed", 0);
        startSpeed = parameters.getDouble("start_speed", 0);
        minSpeed = parameters.getDouble("min_speed", 0);
        maxSpeed = parameters.getDouble("max_speed", 0);
        maxAcceleration = parameters.getDouble("max_acceleration", 0);
        maxDeceleration = parameters.getDouble("max_deceleration", 0);
        liftoffThrust = parameters.getDouble("liftoff_thrust", 0);
        liftoffDuration = parameters.getInt("liftoff_duration", 0);
        crashDistance = parameters.getDouble("crash_distance", 0);
        mountWand = parameters.getBoolean("mount_wand", false);
        maxHeight = parameters.getInt("max_height", 0);
        maxHeightAboveGround = parameters.getInt("max_height_above_ground", -1);
        duration = parameters.getInt("duration", 0);
        durationWarning = parameters.getInt("duration_warning", 0);
        pitchOffset = parameters.getDouble("pitch_offset", 0);
        if (parameters.contains("direction_y")) {
            yDirection = parameters.getDouble("direction_y");
        } else {
            yDirection = null;
        }

        sound = null;
        String soundKey = parameters.getString("sound");
        if (soundKey != null && !soundKey.isEmpty()) {
            sound = new SoundEffect(soundKey);
        }
        soundInterval =  parameters.getInt("sound_interval", 1000);
        soundMaxVolume = (float)parameters.getDouble("sound_volume", 1);
        soundMaxPitch = (float)parameters.getDouble("sound_pitch", 1);
        soundMinVolume = (float)parameters.getDouble("sound_volume", 1);
        soundMinPitch = (float)parameters.getDouble("sound_pitch", 1);
        soundMaxVolume = (float)parameters.getDouble("sound_max_volume", soundMaxVolume);
        soundMaxPitch = (float)parameters.getDouble("sound_max_pitch", soundMaxPitch);
        soundMinVolume = (float)parameters.getDouble("sound_min_volume", soundMinVolume);
        soundMinPitch = (float)parameters.getDouble("sound_min_pitch", soundMinPitch);

        if (parameters.contains("armor_stand_reason")) {
            String reasonText = parameters.getString("armor_stand_reason").toUpperCase();
            try {
                armorStandSpawnReason = CreatureSpawnEvent.SpawnReason.valueOf(reasonText);
            } catch (Exception ex) {
                context.getMage().sendMessage("Unknown spawn reason: " + reasonText);
            }
        }
    }

    protected Entity getMountingEntity(CastContext context) {
        return mountTarget ? context.getEntity() : context.getTargetEntity();
    }

	@Override
	public SpellResult perform(CastContext context) {
        if (!mounted) {
            return mount(context);
        }
        Entity mounted = getMountingEntity(context);
        if (mounted == null)
        {
            return SpellResult.CAST;
        }
        Entity mount = mounted.getVehicle();
        if (mount == null || mount != armorStand)
        {
            return SpellResult.CAST;
        }
        if (!armorStand.isValid())
        {
            if (mountTarget) {
                return SpellResult.FAIL;
            }

            // This seems to happen occasionally... guess we'll work around it for now.
            armorStand.remove();
            if (!mountNewArmorStand(context)) {
                return SpellResult.FAIL;
            }
        }
        
        // Play sound effects
        if (sound != null && nextSoundPlay < System.currentTimeMillis()) {
            nextSoundPlay = System.currentTimeMillis() + soundInterval;

            double speedRatio = minSpeed >= maxSpeed ? 1 : (speed - minSpeed) / (maxSpeed - minSpeed);
            sound.setPitch((float)((soundMaxPitch - soundMinPitch) * speedRatio));
            sound.setVolume((float)((soundMaxVolume - soundMinVolume) * speedRatio));
            sound.play(context.getPlugin(), mounted);
        }

        // Check for crashing
        if (crashDistance > 0)
        {
            Vector threshold = direction.clone().multiply(crashDistance);
            if (checkForCrash(context, mounted.getLocation(), threshold)) {
                crash(context);
                return SpellResult.CAST;
            }
        }
        if (!context.isPassthrough(mounted.getLocation().getBlock().getType())) {
            crash(context);
            return SpellResult.CAST;
        }
        
        adjustHeading(context);
        if (System.currentTimeMillis() > liftoffTime + liftoffDuration) {
            applyThrust(context);
        }
        
        return SpellResult.PENDING;
    }
    
    protected void adjustHeading(CastContext context) {
        Location targetLocation = getMountingEntity(context).getLocation();
        Vector targetDirection = targetLocation.getDirection();
        if (moveDistance == 0) {
            direction = targetDirection;
        } else {
            double moveDistanceSquared = moveDistance * moveDistance;
            double distanceSquared = direction.distanceSquared(targetDirection);
            if (distanceSquared <= moveDistanceSquared) {
                direction = targetDirection;
            } else {
                targetDirection = targetDirection.subtract(direction).normalize().multiply(moveDistance);
                direction.add(targetDirection).normalize();
            }
        }
        
        float targetPitch = targetLocation.getPitch();
        targetLocation.setDirection(direction);
        CompatibilityUtils.setYawPitch(armorStand, targetLocation.getYaw() + (float)armorStandYaw, targetLocation.getPitch());
        if (armorStandPitch != 0) {
            armorStand.setHeadPose(new EulerAngle(armorStandPitch * targetPitch / 180 * Math.PI, 0, 0));
        }
    }
    
    protected void applyThrust(CastContext context) {
        if (duration > 0) {
            long flightTime = System.currentTimeMillis() - liftoffTime;
            Entity mountedEntity = getMountingEntity(context);
            if (!warningEffectsApplied && warningEffects != null && mountedEntity instanceof LivingEntity && durationWarning > 0 && flightTime > duration - durationWarning) {
                CompatibilityUtils.applyPotionEffects((LivingEntity)mountedEntity, warningEffects);
                warningEffectsApplied = true;
            }

            if (flightTime > duration) {
                return;
            }
        }
        
        // Adjust speed
        if (direction.getY() < 0 && maxAcceleration > 0) {
            speed = speed - direction.getY() * maxAcceleration;
            if (maxSpeed > 0 && speed > maxSpeed) {
                speed = maxSpeed;
            }
        } else if (direction.getY() > 0 && maxDeceleration > 0) {
            speed = speed - direction.getY() * maxDeceleration;
            speed = Math.max(minSpeed, speed);
        }

        // Apply pitch offset
        if (yDirection != null) {
            direction.setY(yDirection).normalize();
        }
        if (pitchOffset != 0) {
            direction.setY(direction.getY() + pitchOffset).normalize();
        }
        
        // Check for max height
        double blocksAbove = 0;
        Location currentLocation = getMountingEntity(context).getLocation();
        if (maxHeight > 0 && currentLocation.getY() >= maxHeight) {
            blocksAbove = currentLocation.getY() - maxHeight + 1;
        } else if (maxHeightAboveGround >= 0) {
            Block block = currentLocation.getBlock();
            int height = 0;
            while (height < maxHeightAboveGround && context.isPassthrough(block.getType()))
            {
                block = block.getRelative(BlockFace.DOWN);
                height++;
            }
            if (context.isPassthrough(block.getType())) {
                blocksAbove = height + 1;
            }
        }
        if (blocksAbove > 0 && direction.getY() > 0) {
            direction.setY(-blocksAbove / 5).normalize();
        }
        
        // Apply thrust
        if (speed > 0) {
            armorStand.setVelocity(direction.multiply(speed));
        }
    }
    
    protected SpellResult mount(CastContext context) {
        Mage mage = context.getMage();
        Player player = mage.getPlayer();
        if (player == null && mountWand)
        {
            return SpellResult.PLAYER_REQUIRED;
        }
        
        Entity entity = getMountingEntity(context);
        if (entity == null)
        {
            return SpellResult.NO_TARGET;
        }

        item = null;
        if (mountWand) {
            Wand wand = context.getWand();

            if (wand == null) {
                return SpellResult.NO_TARGET;
            }
            wand.deactivate();

            item = wand.getItem();
            if (item == null || item.getType() == Material.AIR)
            {
                return SpellResult.FAIL;
            }
            slotNumber = wand.getHeldSlot();
        }

        direction = entity.getLocation().getDirection();
        if (mountTarget) {
            Entity targetEntity = context.getTargetEntity();
            if (targetEntity == null || !(targetEntity instanceof ArmorStand)) {
                return SpellResult.NO_TARGET;
            }
            armorStand = ((ArmorStand)targetEntity);
            mount(entity, context);
        } else if (!mountNewArmorStand(context)) {
            return SpellResult.FAIL;
        }
        if (mountWand) {
            player.getInventory().setItem(slotNumber, new ItemStack(Material.AIR));
        }
        
        liftoffTime = System.currentTimeMillis();
        mounted = true;
        speed = startSpeed;
        if (liftoffThrust > 0) {
            armorStand.setVelocity(new Vector(0, liftoffThrust, 0));
        }
        if (sound != null) {
            nextSoundPlay = System.currentTimeMillis();
        }
        
        return SpellResult.PENDING;
	}
	
	protected boolean mountNewArmorStand(CastContext context) {
        Mage mage = context.getMage();
        Entity entity = getMountingEntity(context);
        armorStand = CompatibilityUtils.spawnArmorStand(mage.getLocation());

        if (armorStandInvisible) {
            CompatibilityUtils.setInvisible(armorStand, true);
        }
        if (armorStandMarker) {
            armorStand.setMarker(true);
        }
        if (!armorStandGravity) {
            armorStand.setGravity(false);
        }
        CompatibilityUtils.setDisabledSlots(armorStand, 2039552);
        if (armorStandSmall) {
            armorStand.setSmall(true);
        }

        MageController controller = context.getController();
        controller.setForceSpawn(true);
        try {
            CompatibilityUtils.addToWorld(entity.getWorld(), armorStand, armorStandSpawnReason);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        controller.setForceSpawn(false);

        if (mountWand) {
            armorStand.setHelmet(item);
        }
        mount(entity, context);

        return true;
    }

    private void mount(Entity entity, CastContext context) {
        entity.eject();
        armorStand.setMetadata("notarget", new FixedMetadataValue(context.getController().getPlugin(), true));
        armorStand.setPassenger(entity);
        adjustHeading(context);
    }
	
	@Override
    public void finish(CastContext context) {
        if (armorStand != null) {
            armorStand.removeMetadata("notarget", context.getPlugin());
            if (!mountTarget) {
                armorStand.remove();
            }
            armorStand = null;
        }
        Entity mountedEntity = getMountingEntity(context);
        if (warningEffectsApplied && warningEffects != null && mountedEntity != null && mountedEntity instanceof LivingEntity) {
            for (PotionEffect effect : warningEffects) {
                ((LivingEntity)mountedEntity).removePotionEffect(effect.getType());
            }
        }
        Mage mage = context.getMage();
        Player player = mage.getPlayer();
        if (player == null || item == null) return;

        ItemStack currentItem = player.getInventory().getItem(slotNumber);
        if (currentItem != null) {
            context.getMage().giveItem(item);
        } else {
            player.getInventory().setItem(slotNumber, item);
        }
        mage.checkWand();
        
        item = null;
    }

    protected void crash(CastContext context)
    {
        context.sendMessageKey("crash");
        context.playEffects("crash");
        Entity mountedEntity = getMountingEntity(context);
        if (crashEffects != null && mountedEntity != null && crashEffects.size() > 0 && mountedEntity instanceof LivingEntity) {
            CompatibilityUtils.applyPotionEffects((LivingEntity)mountedEntity, crashEffects);
        }
        warningEffectsApplied = false;
    }

    protected boolean checkForCrash(CastContext context, Location source, Vector threshold)
    {
        Block facingBlock = source.getBlock();
        Block targetBlock = source.add(threshold).getBlock();

        if (!targetBlock.equals(facingBlock) && !context.isPassthrough(targetBlock.getType())) {
            return true;
        }

        return false;
    }

	@Override
	public boolean isUndoable()
	{
		return false;
	}
    
    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("armor_stand_invisible");
        parameters.add("armor_stand_small");
        parameters.add("armor_stand_marker");
        parameters.add("armor_stand_gravity");
        parameters.add("armor_stand_reason");
        parameters.add("armor_stand_pitch");
        parameters.add("armor_stand_yaw");
        parameters.add("steer_speed");
        parameters.add("liftoff_duration");
        parameters.add("liftoff_thrust");
        parameters.add("crash_distance");
        parameters.add("mount_wand");
        parameters.add("max_height");
        parameters.add("max_height_above_ground");
        parameters.add("duration");
        parameters.add("duration_warning");
        parameters.add("start_speed");
        parameters.add("min_speed");
        parameters.add("max_speed");
        parameters.add("max_acceleration");
        parameters.add("max_deceleration");
        parameters.add("pitch_offset");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("crash_distance")
                || parameterKey.equals("max_height")
                || parameterKey.equals("max_height_above_ground")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else if (parameterKey.equals("armor_stand_invisible") 
                || parameterKey.equals("armor_stand_marker") 
                || parameterKey.equals("armor_stand_small")
                || parameterKey.equals("armor_stand_gravity")
                || parameterKey.equals("mount_wand")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else if (parameterKey.equals("steer_speed")
                || parameterKey.equals("start_speed")
                || parameterKey.equals("armor_stand_pitch")
                || parameterKey.equals("armor_stand_yaw")
                || parameterKey.equals("min_speed")
                || parameterKey.equals("max_speed")
                || parameterKey.equals("max_acceleration")
                || parameterKey.equals("max_deceleration")
                || parameterKey.equals("pitch_offset")
                || parameterKey.equals("liftoff_thrust")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_VECTOR_COMPONENTS));
        } else if (parameterKey.equals("liftoff_duration")
                || parameterKey.equals("duration")
                || parameterKey.equals("duration_warning")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_DURATIONS));
        } else if (parameterKey.equals("armor_stand_reason")) {
            for (CreatureSpawnEvent.SpawnReason reason : CreatureSpawnEvent.SpawnReason.values()) {
                examples.add(reason.name().toLowerCase());
            }
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
