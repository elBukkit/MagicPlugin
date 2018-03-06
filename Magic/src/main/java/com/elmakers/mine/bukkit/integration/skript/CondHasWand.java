package com.elmakers.mine.bukkit.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Has Wand")
@Description("Checks whether a player has a specific wand.")
@Examples({"player has wand", "player has wand \"lightsaber\" in offhand", "player does not have wand \"battle\""})
public class CondHasWand extends Condition {
    private Expression<Entity> entities;
    private Expression<String> wands;
    private boolean offhand;

    @SuppressWarnings({"unchecked"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		entities = (Expression<Entity>) vars[0];
		wands = (Expression<String>) vars[1];
		offhand = (matchedPattern == 2 || matchedPattern == 3 || matchedPattern == 6 || matchedPattern == 7);
		setNegated(matchedPattern >= 4);
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
                final Wand wand = offhand ? mage.getOffhandWand() : mage.getActiveWand();
			    if (wands == null) {
			    	return (wand != null) != isNegated();
				}
				return wands.check(e, new Checker<String>() {
					@Override
					public boolean check(final String wandKey) {
					    return wand.getTemplateKey().equalsIgnoreCase(wandKey);
					}
				}, isNegated());
			}
		});
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
        String wandTypes = wands == null ? "" : wands.toString();
		return entities.toString(e, debug) + (entities.isSingle() ? " has" : " have") + (isNegated() ? " not " : "") + " wand " + wandTypes;
	}
}
