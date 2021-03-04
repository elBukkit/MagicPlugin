package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.BaseProjectileAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.SourceLocation;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;

public class ProjectileAction  extends BaseProjectileAction
{
    private int defaultSize = 1;

    private int count;
    private int size;
    private double damage;
    private float speed;
    private float spread;
    private boolean useFire;
    private boolean breakBlocks;
    private int tickIncrease;
    private String projectileTypeName;
    private int startDistance;
    private SourceLocation sourceLocation;
    private String pickupStatus;
    private String color;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);

        breakBlocks = parameters.getBoolean("break_blocks", false);
        useFire = parameters.getBoolean("fire", false);
    }

    @Override
    public boolean requiresBuildPermission() {
        return useFire;
    }

    @Override
    public boolean requiresBreakPermission() {
        return breakBlocks;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        track = true;
        super.prepare(context, parameters);
        count = parameters.getInt("count", 1);
        size = parameters.getInt("size", defaultSize);
        damage = parameters.getDouble("damage", 0);
        damage = parameters.getDouble("arrow_damage", damage);
        speed = (float)parameters.getDouble("speed", 0.6f);
        spread = (float)parameters.getDouble("spread", 12);
        useFire = parameters.getBoolean("fire", false);
        tickIncrease = parameters.getInt("tick_increase", 1180);
        projectileTypeName = parameters.getString("projectile", "TippedArrow");
        breakBlocks = parameters.getBoolean("break_blocks", false);
        startDistance = parameters.getInt("start", 0);
        pickupStatus = parameters.getString("pickup", "disallowed");
        color = parameters.getString("color");
        sourceLocation = new SourceLocation(parameters);
    }

    @Override
    public SpellResult start(CastContext context)
    {
        MageController controller = context.getController();
        Mage mage = context.getMage();

        // Modify with wand power
        // Turned some of this off for now
        // int count = this.count * mage.getRadiusMultiplier();
        // int speed = this.speed * damageMultiplier;
        int size = (int)(mage.getRadiusMultiplier() * this.size);
        double damageMultiplier = mage.getDamageMultiplier("projectile");
        double damage = damageMultiplier * this.damage;
        float radiusMultiplier = mage.getRadiusMultiplier();
        float spread = this.spread;
        if (radiusMultiplier > 1) {
             spread = spread / radiusMultiplier;
        }
        Random random = context.getRandom();

        Class<?> projectileType = NMSUtils.getBukkitClass("net.minecraft.server.Entity" + projectileTypeName);
        if (!CompatibilityUtils.isValidProjectileClass(projectileType)) {
            controller.getLogger().warning("Bad projectile class: " + projectileTypeName);
            return SpellResult.FAIL;
        }

        // Prepare parameters
        Location location = sourceLocation.getLocation(context);
        if (location == null) {
            return SpellResult.LOCATION_REQUIRED;
        }
        Vector direction = location.getDirection();

        if (startDistance > 0) {
            location = location.clone().add(direction.clone().multiply(startDistance));
        }

        // Spawn projectiles
        LivingEntity shootingEntity = context.getLivingEntity();
        ProjectileSource source = null;
        if (shootingEntity != null)
        {
            source = shootingEntity;
        }
        mage.setLaunchingProjectile(true);
        for (int i = 0; i < count; i++) {
            try {
                // Spawn a new projectile
                Projectile projectile = CompatibilityUtils.spawnProjectile(projectileType, location, direction, source, speed, spread, i > 0 ? spread : 0, random);
                if (projectile == null) {
                    return SpellResult.FAIL;
                }
                if (shootingEntity != null) {
                    projectile.setShooter(shootingEntity);
                }

                if (projectile instanceof ThrownPotion && color != null && !color.isEmpty()) {
                    ThrownPotion potion = (ThrownPotion)projectile;
                    if (color.startsWith("#")) {
                        color = color.substring(1);
                    }
                    try {
                        Color potionColor = Color.fromRGB(Integer.parseInt(color, 16));
                        ItemStack itemStack = new ItemStack(Material.SPLASH_POTION);
                        ItemMeta meta = itemStack.getItemMeta();
                        if (meta != null && meta instanceof PotionMeta) {
                            CompatibilityUtils.setColor((PotionMeta)meta, potionColor);
                            itemStack.setItemMeta(meta);
                            potion.setItem(itemStack);
                        }
                    } catch (Exception ex) {
                        context.getLogger().warning("Invalid potion color in Projectile action: " + color);
                    }
                }
                if (projectile instanceof Fireball) {
                    Fireball fireball = (Fireball)projectile;
                    fireball.setIsIncendiary(useFire);
                    fireball.setYield(size);
                }
                if (projectile instanceof Arrow) {
                    Arrow arrow = (Arrow)projectile;
                    if (useFire) {
                        arrow.setFireTicks(300);
                    }

                    if (damage > 0) {
                        CompatibilityUtils.setDamage(projectile, damage);
                    }
                    if (tickIncrease > 0) {
                        CompatibilityUtils.decreaseLifespan(projectile, tickIncrease);
                    }
                    if (pickupStatus != null && !pickupStatus.isEmpty()) {
                        CompatibilityUtils.setPickupStatus(arrow, pickupStatus);
                    }
                }
                if (!breakBlocks) {
                    EntityMetadataUtils.instance().setBoolean(projectile, "cancel_explosion_blocks", true);
                }
                track(context, projectile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        mage.setLaunchingProjectile(false);

        return checkTracking(context);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("count");
        parameters.add("size");
        parameters.add("damage");
        parameters.add("speed");
        parameters.add("spread");
        parameters.add("start");
        parameters.add("projectile");
        parameters.add("fire");
        parameters.add("tick_increase");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("undo_interval")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_DURATIONS));
        } else if (parameterKey.equals("count") || parameterKey.equals("size") || parameterKey.equals("speed")
                || parameterKey.equals("spread") || parameterKey.equals("tick_increase")
                || parameterKey.equals("damage") || parameterKey.equals("start")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else if (parameterKey.equals("fire")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else if (parameterKey.equals("projectile")) {
            examples.add("LargeFireball");
            examples.add("SmallFireball");
            examples.add("WitherSkull");
            examples.add("TippedArrow");
            examples.add("SpectralArrow");
            examples.add("Snowball");
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
