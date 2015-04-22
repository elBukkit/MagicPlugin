package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TileEntitiesAction extends CompoundAction
{
    private boolean targetAllWorlds;

    private List<BlockState> tiles = new ArrayList<BlockState>();
    private int currentTile = 0;

	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        targetAllWorlds = parameters.getBoolean("target_all_worlds", false);
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        createActionContext(context);
        currentTile = 0;
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Location sourceLocation = context.getLocation();
        if (sourceLocation == null && !targetAllWorlds)
        {
            return SpellResult.LOCATION_REQUIRED;
        }

        if (currentTile == 0)
        {
            tiles.clear();
            addTiles(context, tiles);
        }

        SpellResult result = SpellResult.NO_TARGET;
        while (currentTile < tiles.size())
        {
            BlockState tile = tiles.get(currentTile);
            actionContext.setTargetLocation(tile.getLocation());
            SpellResult entityResult = super.perform(actionContext);
            result = result.min(entityResult);
            if (entityResult == SpellResult.PENDING) {
                break;
            }
            currentTile++;
            if (currentTile < tiles.size())
            {
                super.reset(context);
            }
        }

        return result;
    }

    public void addTiles(CastContext context, List<BlockState> tiles)
    {
		Location sourceLocation = context.getLocation();

		if (sourceLocation == null && !targetAllWorlds)
		{
			return;
		}

        List<World> worlds;
        if (targetAllWorlds) {
            worlds = Bukkit.getWorlds();
        } else {
            worlds = new ArrayList<World>();
            worlds.add(sourceLocation.getWorld());
        }
        for (World world : worlds)
        {
            Chunk[] chunks = world.getLoadedChunks();
            for (Chunk chunk : chunks) {
                tiles.addAll(Arrays.asList(chunk.getTileEntities()));
            }
		}
	}

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("target_all_worlds");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("target_all_worlds")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
