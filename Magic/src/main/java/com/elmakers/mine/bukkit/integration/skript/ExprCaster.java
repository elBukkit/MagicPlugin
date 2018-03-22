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
import com.elmakers.mine.bukkit.api.event.PreCastEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

@Name("Caster")
@Description({"The caster of a spell event.",
		"Please note that the attacker can also be a command block or the console, but this expression will not be set in these cases."})
@Examples({"on cast:",
		"	caster is a player",
		"	health of caster is less than or equal to 2",
		"	damage targets by 1 heart"})
public class ExprCaster extends SimpleExpression<Entity> {
    public static void register() {
		Skript.registerExpression(ExprCaster.class, Entity.class, ExpressionType.SIMPLE, "[the] (caster)");
    }

	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parser) {
		if (!ScriptLoader.isCurrentEvent(CastEvent.class) && !ScriptLoader.isCurrentEvent(PreCastEvent.class)) {
			Skript.error("Cannot use 'caster' outside of a cast event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	protected Entity[] get(final Event e) {
		return new Entity[] {getCaster(e)};
	}

	@Nullable private static Entity getCaster(final Event e) {
		if (e != null && e instanceof CastEvent) {
		    return ((CastEvent)e).getMage().getEntity();
		}
		return null;
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public String toString(@Nullable Event e, final boolean debug) {
		if (e == null)
			return "the caster";
		return Classes.getDebugMessage(getSingle(e));
	}

	@Override
	public boolean isSingle() {
		return true;
	}
}
