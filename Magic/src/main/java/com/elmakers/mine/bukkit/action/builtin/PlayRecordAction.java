package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class PlayRecordAction extends BaseSpellAction
{
    private String recordList = "";

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        recordList = parameters.getString("records", "records");
    }
	
	@SuppressWarnings("deprecation")
	@Override
    public SpellResult perform(CastContext context) {
		Location location = context.getTargetLocation();
        Set<Material> recordSet = context.getMaterialSet(recordList);
        if (recordSet == null || recordSet.size() == 0) {
            return SpellResult.FAIL;
        }
        List<Material> records = new ArrayList<>(recordSet);
        Random random = context.getRandom();
        Material record = records.get(random.nextInt(records.size()));
		location.getWorld().playEffect(location, Effect.RECORD_PLAY, record.getId());

		return SpellResult.CAST;
	}

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
