package com.elmakers.mine.bukkit.integration.skript;

import javax.annotation.Nullable;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicPlugin;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

@Name("Is Class")
@Description("Checks whether a player is a magic class.")
@Examples({"player is class \"mage\"", "player is not class \"jedi\""})
public class CondIsClass extends Condition {
    private Expression<Entity> entities;
    private Expression<String> classes;

    public static void register() {
		Skript.registerCondition(CondIsClass.class,
        "%entities% (is|are) class[es] %strings%",
        "%entities% (isn't|is not|aren't|are not) class[es] %strings%");
    }

    @SuppressWarnings({"unchecked"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		entities = (Expression<Entity>) vars[0];
		classes = (Expression<String>) vars[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(final Event e) {
		return entities.check(e, new Checker<Entity>() {
			@Override
			public boolean check(final Entity entity) {
			    final Mage mage = MagicPlugin.getAPI().getController().getRegisteredMage(entity);
			    if (mage == null) {
			        return false;
                }
				return classes.check(e, new Checker<String>() {
					@Override
					public boolean check(final String className) {
					    return mage.hasClassUnlocked(className);
					}
				}, isNegated());
			}
		});
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return entities.toString(e, debug) + (entities.isSingle() ? " is" : " are") + (isNegated() ? " not" : "") + " class " + classes;
	}
}
