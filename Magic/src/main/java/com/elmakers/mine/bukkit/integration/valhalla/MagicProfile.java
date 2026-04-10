package com.elmakers.mine.bukkit.integration.valhalla;

import java.util.UUID;

import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.skills.skills.Skill;

public class MagicProfile extends Profile {
    private final String id;

    public MagicProfile(UUID owner, MagicProfile copyFrom) {
        super(owner);
        this.id = copyFrom.id;
    }

    public MagicProfile(String profileId) {
        super(null);
        this.id = profileId;
    }

    @Override
    public String getTableName() {
        return "profiles_" + id;
    }

    @Override
    public Class<? extends Skill> getSkillType() {
        return MagicSkill.class;
    }

    @Override
    public Profile getBlankProfile(UUID uuid) {
        return ProfileRegistry.copyDefaultStats(new MagicProfile(owner, this));
    }
}
