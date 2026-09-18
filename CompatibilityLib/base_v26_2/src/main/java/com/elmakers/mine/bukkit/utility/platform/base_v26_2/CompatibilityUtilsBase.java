package com.elmakers.mine.bukkit.utility.platform.base_v26_2;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;

import com.elmakers.mine.bukkit.utility.EnteredStateTracker;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public class CompatibilityUtilsBase extends com.elmakers.mine.bukkit.utility.platform.base_v26_1.CompatibilityUtilsBase {

    protected CompatibilityUtilsBase(final Platform platform) {
        super(platform);
    }

    @Override
    public void damage(Damageable target, double amount, Entity source) {
        if (target == null || target.isDead() || amount <= 0) return;
        while (target instanceof ComplexEntityPart) {
            target = ((ComplexEntityPart) target).getParent();
        }
        if (USE_MAGIC_DAMAGE && target.getType() == EntityType.ENDER_DRAGON) {
            magicDamage(target, amount, source);
            return;
        }

        try (EnteredStateTracker.Touchable damaging = isDamaging.enter()) {
            damaging.touch();
            if (target instanceof ArmorStand) {
                double newHealth = Math.max(0, target.getHealth() - amount);
                if (newHealth <= 0) {
                    DamageSource damageSource = source == null ? DamageSource.builder(DamageType.GENERIC).build() : DamageSource.builder(DamageType.MAGIC).withCausingEntity(source).build();
                    EntityDeathEvent deathEvent = new EntityDeathEvent((ArmorStand) target, damageSource, new ArrayList<>());
                    Bukkit.getPluginManager().callEvent(deathEvent);
                    target.remove();
                } else {
                    target.setHealth(newHealth);
                }
            } else {
                target.damage(amount, source);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void populateProjectileClasses() {
        // Can't use reflection, so gonna do this the hard (coded) way.
        addProjectileClass("arrow", net.minecraft.world.entity.projectile.arrow.Arrow.class, net.minecraft.world.entity.EntityTypes.ARROW);
        addProjectileClass("tippedarrow", net.minecraft.world.entity.projectile.arrow.Arrow.class, net.minecraft.world.entity.EntityTypes.ARROW);
        addProjectileClass("tipped_arrow", net.minecraft.world.entity.projectile.arrow.Arrow.class, net.minecraft.world.entity.EntityTypes.ARROW);
        addProjectileClass("dragonfireball", net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball.class, net.minecraft.world.entity.EntityTypes.DRAGON_FIREBALL);
        addProjectileClass("dragon_fireball", net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball.class, net.minecraft.world.entity.EntityTypes.DRAGON_FIREBALL);
        addProjectileClass("fireball", net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball.class, net.minecraft.world.entity.EntityTypes.FIREBALL);
        addProjectileClass("largefireball", net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball.class, net.minecraft.world.entity.EntityTypes.FIREBALL);
        addProjectileClass("large_fireball", net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball.class, net.minecraft.world.entity.EntityTypes.FIREBALL);
        addProjectileClass("smallfireball", net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball.class, net.minecraft.world.entity.EntityTypes.SMALL_FIREBALL);
        addProjectileClass("small_fireball", net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball.class, net.minecraft.world.entity.EntityTypes.SMALL_FIREBALL);
        addProjectileClass("fireworks", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityTypes.FIREWORK_ROCKET);
        addProjectileClass("firework", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityTypes.FIREWORK_ROCKET);
        addProjectileClass("fireworkrocket", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityTypes.FIREWORK_ROCKET);
        addProjectileClass("firework_rocket", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityTypes.FIREWORK_ROCKET);
        addProjectileClass("fireworkrocketentity", net.minecraft.world.entity.projectile.FireworkRocketEntity.class, net.minecraft.world.entity.EntityTypes.FIREWORK_ROCKET);
        addProjectileClass("fishinghook", net.minecraft.world.entity.projectile.FishingHook.class, net.minecraft.world.entity.EntityTypes.FISHING_BOBBER);
        addProjectileClass("fishing_hook", net.minecraft.world.entity.projectile.FishingHook.class, net.minecraft.world.entity.EntityTypes.FISHING_BOBBER);
        addProjectileClass("fishing_bobber", net.minecraft.world.entity.projectile.FishingHook.class, net.minecraft.world.entity.EntityTypes.FISHING_BOBBER);
        addProjectileClass("llamaspit", net.minecraft.world.entity.projectile.LlamaSpit.class, net.minecraft.world.entity.EntityTypes.LLAMA_SPIT);
        addProjectileClass("llama_spit", net.minecraft.world.entity.projectile.LlamaSpit.class, net.minecraft.world.entity.EntityTypes.LLAMA_SPIT);
        addProjectileClass("shulkerbullet", net.minecraft.world.entity.projectile.ShulkerBullet.class, net.minecraft.world.entity.EntityTypes.SHULKER_BULLET);
        addProjectileClass("shulker_bullet", net.minecraft.world.entity.projectile.ShulkerBullet.class, net.minecraft.world.entity.EntityTypes.SHULKER_BULLET);
        addProjectileClass("snowball", net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball.class, net.minecraft.world.entity.EntityTypes.SNOWBALL);
        addProjectileClass("spectralarrow", net.minecraft.world.entity.projectile.arrow.SpectralArrow.class, net.minecraft.world.entity.EntityTypes.SPECTRAL_ARROW);
        addProjectileClass("spectral_arrow", net.minecraft.world.entity.projectile.arrow.SpectralArrow.class, net.minecraft.world.entity.EntityTypes.SPECTRAL_ARROW);
        addProjectileClass("egg", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg.class, net.minecraft.world.entity.EntityTypes.EGG);
        addProjectileClass("thrownegg", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg.class, net.minecraft.world.entity.EntityTypes.EGG);
        addProjectileClass("enderpearl", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl.class, net.minecraft.world.entity.EntityTypes.ENDER_PEARL);
        addProjectileClass("ender_pearl", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl.class, net.minecraft.world.entity.EntityTypes.ENDER_PEARL);
        addProjectileClass("thrownenderpearl", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl.class, net.minecraft.world.entity.EntityTypes.ENDER_PEARL);
        addProjectileClass("thrownexperiencebottle", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle.class, net.minecraft.world.entity.EntityTypes.EXPERIENCE_BOTTLE);
        addProjectileClass("experiencebottle", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle.class, net.minecraft.world.entity.EntityTypes.EXPERIENCE_BOTTLE);
        addProjectileClass("thrownpotion", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion.class, net.minecraft.world.entity.EntityTypes.SPLASH_POTION);
        addProjectileClass("potion", net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion.class, net.minecraft.world.entity.EntityTypes.SPLASH_POTION);
        addProjectileClass("throwntrident", net.minecraft.world.entity.projectile.arrow.ThrownTrident.class, net.minecraft.world.entity.EntityTypes.TRIDENT);
        addProjectileClass("trident", net.minecraft.world.entity.projectile.arrow.ThrownTrident.class, net.minecraft.world.entity.EntityTypes.TRIDENT);
        addProjectileClass("witherskull", net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull.class, net.minecraft.world.entity.EntityTypes.WITHER_SKULL);
        addProjectileClass("wither_skull", net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull.class, net.minecraft.world.entity.EntityTypes.WITHER_SKULL);
    }
}
