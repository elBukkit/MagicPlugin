package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.google.common.collect.Iterables;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.Random;

public class PlayRecordAction extends BaseSpellAction
{
    private String recordList = "";

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        recordList = parameters.getString("records", "records");
    }
	
	@Override
    public SpellResult perform(CastContext context) {
        Collection<Material> records = context.getController()
                .getMaterialSetManager()
                .fromConfigEmpty(recordList)
                .getMaterials();
        if (records.isEmpty()) {
            return SpellResult.FAIL;
        }


        Random random = context.getRandom();
        Material record = Iterables.get(records, random.nextInt(records.size()));

        Location location = context.getTargetLocation();
        location.getWorld().playEffect(location, Effect.RECORD_PLAY,
                DeprecatedUtils.getId(record));

        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
