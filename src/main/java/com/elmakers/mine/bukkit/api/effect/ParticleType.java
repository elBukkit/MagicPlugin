package com.elmakers.mine.bukkit.api.effect;

public enum ParticleType {
    HUGE_EXPLOSION("hugeexplosion"),
    LARGE_EXPLOSION("largeexplode"),
    FIREWORKS_SPARK("fireworksSpark"),
    BUBBLE("bubble"),
    SUSPENDED("suspend"),
    DEPTH_SUSPENDED("depthSuspend"),
    TOWN_AURA("townaura"),
    CRITICAL("crit"),
    MAGIC_CRITICAL("magicCrit"),
    SMOKE("smoke"),
    MOB_SPELL("mobSpell"),
    MOB_SPELL_AMBIENT("mobSpellAmbient"),
    SPELL("spell"),
    INSTANT_SPELL("instantSpell"),
    WITCH_MAGIC("witchMagic"),
    NOTE("note"),
    PORTAL("portal"),
    MAGIC_RUNES("enchantmenttable"),
    EXPLOSION("explode"),
    FLAME("flame"),
    LAVA("lava"),
    FOOTSTEP("footstep"),
    SPLASH("splash"),
    WAKE("wake"),
    LARGE_SMOKE("largesmoke"),
    CLOUD("cloud"),
    RED_DUST("reddust"),
    SNOWBALL_POOF("snowballpoof"),
    WATER_DRIPPING("dripWater"),
    LAVA_DRIPPING("dripLava"),
    SLIME("slime"),
    HEART("heart"),
    ANGRY_VILLAGER("angryVillager"),
    HAPPY_VILLAGER("happyVillager"),
    SNOW_SHOVEL("snowshovel"),

    TOOL_BREAKING("iconcrack_{subtype}"),
    BLOCK_BREAKING("blockcrack_{subtype}"),
    TILE_BREAKING("tilecrack_{subtype}"),

    UNKNOWN("nil");

    private String particleName;

    private ParticleType(String particleName) {
        this.particleName = particleName;
    }

    public String getParticleName() {
        return particleName;
    }

    public String getParticleName(String subtype) {
        return particleName.replace("{subtype}", subtype);
    }

    public static ParticleType fromName(String name, ParticleType particle) {
        for (ParticleType t : ParticleType.values()) {
            if (t.getParticleName().replace("_", "").equalsIgnoreCase(name.replace("_", ""))) {
                particle = t;
                break;
            }
        }
        return particle;
    }
}
