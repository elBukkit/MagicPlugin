package com.elmakers.mine.bukkit.integration.skript;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import com.elmakers.mine.bukkit.api.event.CastEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

@Name("Targets")
@Description({"The targets of a spell event."})
@Examples({"on cast:", "  damage targets by 1 heart"})
public class ExprTargets extends SimpleExpression<Entity> {
	private static final Entity[] templateArray = new Entity[] {};

    public static void register() {
		Skript.registerExpression(ExprTargets.class, Entity.class, ExpressionType.SIMPLE, "[the] (targets)");
    }

	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parser) {
		if (!ScriptLoader.isCurrentEvent(CastEvent.class)) {
			Skript.error("Cannot use 'targets' outside of a cast event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	protected Entity[] get(final Event e) {
		return getTargets(e);
	}

	private static Entity[] getTargets(final Event e) {
		if (e != null && e instanceof CastEvent) {
		    return ((CastEvent)e).getSpell().getCurrentCast().getTargetedEntities().toArray(templateArray);
		}
		return null;
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public String toString(final Event e, final boolean debug) {
		if (e == null)
			return "the targets";
		return Classes.getDebugMessage(e);
	}

	@Override
	public boolean isSingle() {
		return false;
	}
}
