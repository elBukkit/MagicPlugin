package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class UndoSpell extends Spell
{
    @Override
    public String getDescription()
    {
        return "Undoes your last action";
    }

    @Override
    public String getName()
    {
        return "rewind";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        if (parameters.hasParameter("player"))
        {
            String undoPlayer = parameters.getString("player", "");
            boolean undone = magic.undo(undoPlayer);
            if (undone)
            {
                castMessage(player, "You revert " + undoPlayer + "'s construction");
            }
            else
            {
                castMessage(player, "There is nothing to undo for " + undoPlayer);
            }
            return undone;
        }

        /*
         * Use target if targeting
         */
        targeting.targetThrough(Material.GLASS);
        Block target = targeting.getTargetBlock();
        if (target != null)
        {
            boolean undone = magic.undo(player.getName(), target);
            if (undone)
            {
                castMessage(player, "You revert your construction");
                return true;
            }
        }

        /*
         * No target, or target isn't yours- just undo last
         */
        boolean undone = magic.undo(player.getName());
        if (undone)
        {
            castMessage(player, "You revert your construction");
        }
        else
        {
            castMessage(player, "Nothing to undo");
        }
        return undone;
    }

}
