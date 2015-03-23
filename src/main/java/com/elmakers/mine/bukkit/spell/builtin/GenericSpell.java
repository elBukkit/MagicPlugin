package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

@Deprecated
public class GenericSpell extends UndoableSpell
{
    private boolean undoDamage = false;
    private boolean undoVelocity = false;
    private boolean undoMovement = false;

    @Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
        if (!target.hasTarget()) {
            return SpellResult.NO_TARGET;
        }
        if (target.hasEntity()) {
            Entity entity = target.getEntity();
            undoDamage = parameters.getBoolean("undo_damage", false);
            undoVelocity = parameters.getBoolean("undo_velocity", false);
            undoMovement = parameters.getBoolean("undo_movement", false);
            if (undoDamage || undoVelocity || undoMovement) {
                if (undoDamage) {
                    registerModified(entity);
                }
                if (undoVelocity) {
                    registerVelocity(entity);
                }
                if (undoMovement) {
                    registerMoved(entity);
                }

                registerForUndo();
            }
        }

        return SpellResult.CAST;
	}

    @Override
    protected void loadTemplate(ConfigurationSection node)
    {
        super.loadTemplate(node);

        // Also load this here so it is available from templates, prior to casting
        ConfigurationSection parameters = node.getConfigurationSection("parameters");
        undoMovement = parameters.getBoolean("undo_movement", false);
        undoVelocity = parameters.getBoolean("undo_velocity", false);
        undoDamage = parameters.getBoolean("undo_damage", false);
    }

    public boolean isUndoable()
    {
        return super.isUndoable() && (undoMovement || undoVelocity || undoDamage);
    }
}
