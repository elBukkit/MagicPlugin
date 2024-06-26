package com.elmakers.mine.bukkit.utility.platform.modern;

import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.utility.platform.base.MobUtilsBase;

import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;

public class MobUtils extends MobUtilsBase {
    private final Platform platform;

    public MobUtils(Platform platform) {
        this.platform = platform;
    }

    private net.minecraft.world.entity.Entity getNMS(Entity entity) {
        if (entity == null) return null;
        CraftEntity craft = (CraftEntity) entity;
        return craft.getHandle();
    }

    private Mob getMob(Entity entity) {
        net.minecraft.world.entity.Entity nms = getNMS(entity);
        if (!(nms instanceof Mob)) {
            return null;
        }
        return (Mob)nms;
    }

    private Class<? extends LivingEntity> getMobClass(String classType) {
        switch (classType) {
            case "ambient_creature":
            case "ambientcreature":

            // Ambient
            case "ambient":
                return AmbientCreature.class;
            case "bat":
                return Bat.class;

            // Axolotl
            case "axolotl":
                return Axolotl.class;

            // Goat
            case "goat":
                return Goat.class;

            // Horse
            case "abstractchestedhorse":
            case "abstract_chested_horse":
            case "chestedhorse":
            case "chested_horse":
                return AbstractChestedHorse.class;
            case "abstract_horse":
            case "abstracthorse":
            case "any_horse":
                return AbstractHorse.class;
            case "donkey":
                return Donkey.class;
            case "horse":
                return Horse.class;
            case "llama":
                return Llama.class;
            case "mule":
                return Mule.class;
            case "skeleton_horse":
            case "skeletonhorse":
                return Skeleton.class;
            case "trader_llama":
            case "traderllama":
                return TraderLlama.class;
            case "zombiehorse":
            case "zombie_horse":
                return ZombieHorse.class;

            // Animal
            case "abstractfish":
            case "abstract_fish":
            case "any_fish":
            case "fish":
                return AbstractFish.class;
            case "abstractgolem":
            case "abstract_golem":
            case "golem":
                return AbstractGolem.class;
            case "abstractschoolingfish":
            case "abstract_schooling_fish":
            case "schooling_fish":
                return AbstractSchoolingFish.class;
            case "animal":
                return Animal.class;
            case "bee":
                return Bee.class;
            case "cat":
                return Cat.class;
            case "chicken":
                return Chicken.class;
            case "cod":
                return Cod.class;
            case "cow":
                return Cow.class;
            case "dolphin":
                return Dolphin.class;
            case "fox":
                return Fox.class;
            case "iron_golem":
            case "irongolem":
                return IronGolem.class;
            case "mushroomcow":
            case "mushroom_cow":
            case "mooshroom":
                return MushroomCow.class;
            case "ocelot":
                return Ocelot.class;
            case "panda":
                return Panda.class;
            case "parrot":
                return Parrot.class;
            case "pig":
                return Pig.class;
            case "polarbear":
            case "polar_bear":
                return PolarBear.class;
            case "pufferfish":
                return Pufferfish.class;
            case "rabbit":
                return Rabbit.class;
            case "salmon":
                return Salmon.class;
            case "sheep":
                return Sheep.class;
            case "snowgolem":
            case "snow_golem":
            case "snowman":
                return SnowGolem.class;
            case "squid":
                return Squid.class;
            case "tropicalfish":
            case "tropical_fish":
                return TropicalFish.class;
            case "turle":
            case "sea_turtle":
                return Turtle.class;
            case "wolf":
                return Wolf.class;

            // Boss
            case "dragon":
            case "enderdragon":
            case "ender_dragon":
                return EnderDragon.class;
            case "wither":
            case "wither_boss":
            case "witherboss":
                return WitherBoss.class;

            // Decoration
            case "armorstand":
            case "armor_stand":
                return ArmorStand.class;

            // Hoglin
            case "hoglin":
                return Hoglin.class;

            // Piglin
            case "piglin":
                return Piglin.class;
            case "piglinbrute":
            case "piglin_brute":
                return PiglinBrute.class;

            // Monster
            case "abstractillager":
            case "abstract_illager":
            case "illager":
                return AbstractIllager.class;
            case "abstractskeleton":
            case "abstract_skeleton":
            case "any_skeleton":
                return AbstractSkeleton.class;
            case "blaze":
                return Blaze.class;
            case "cavespider":
            case "cave_spider":
                return CaveSpider.class;
            case "creeper":
                return Creeper.class;
            case "drowned":
                return Drowned.class;
            case "elderguardian":
            case "elder_guardian":
                return ElderGuardian.class;
            case "enderman":
                return EnderMan.class;
            case "endermite":
                return Endermite.class;
            case "evoker":
                return Evoker.class;
            case "ghast":
                return Ghast.class;
            case "giant":
                return Giant.class;
            case "guardian":
                return Guardian.class;
            case "husk":
                return Husk.class;
            case "illusioner":
                return Illusioner.class;
            case "magmacube":
            case "magma_cube":
                return MagmaCube.class;
            case "monster":
                return Monster.class;
            case "phantom":
                return Phantom.class;
            case "pillager":
                return Pillager.class;
            case "ravager":
                return Ravager.class;
            case "shulker":
                return Shulker.class;
            case "silverfish":
                return Silverfish.class;
            case "skeleton":
                return Skeleton.class;
            case "slime":
                return Slime.class;
            case "spellcasterillager":
            case "spellcaster_illager":
            case "spellcaster":
                return SpellcasterIllager.class;
            case "spider":
                return Spider.class;
            case "stray":
                return Stray.class;
            case "strider":
                return Strider.class;
            case "vex":
                return Vex.class;
            case "vindicator":
                return Vindicator.class;
            case "witch":
                return Witch.class;
            case "witherskeleton":
            case "wither_skeleton":
                return WitherSkeleton.class;
            case "zoglin":
                return Zoglin.class;
            case "zombie":
                return Zombie.class;
            case "zombievillager":
            case "zombie_villager":
                return ZombieVillager.class;
            case "zombifiedpiglin":
            case "zombified_piglin":
                return ZombifiedPiglin.class;

            // NPC
            case "villager":
                return Villager.class;
            case "wanderingtrader":
            case "wandering_trader":
                return WanderingTrader.class;

            // Player
            case "player":
                return Player.class;

            // Base
            // Why are you here?
            case "glowsquid":
                return GlowSquid.class;
            case "mob":
                return Mob.class;
            case "livingentity":
            case "living_entity":
                return LivingEntity.class;

            default:
                platform.getLogger().warning("Invalid entity_class in goal config: " + classType);
                return LivingEntity.class;
        }
    }

    @Override
    public boolean setPathfinderTarget(Entity entity, Entity target, double speed) {
        if (entity == null || target == null) return false;
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity)entity).getHandle();
        net.minecraft.world.entity.Entity nmstarget = ((CraftEntity)target).getHandle();
        if (!(nmsEntity instanceof Mob)) {
            return false;
        }

        Mob mob = (Mob)nmsEntity;
        mob.getNavigation().moveTo(nmstarget, speed);
        return true;
    }
}
