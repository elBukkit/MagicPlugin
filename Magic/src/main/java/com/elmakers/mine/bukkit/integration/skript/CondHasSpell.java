package com.elmakers.mine.bukkit.integration.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

@Name("Knows Spell")
@Description("Checks whether a player knows a specific spell, or has it on their wand.")
@Examples({"player knows \"missile\"", "player has spell \"blink\"", "player has \"meteor\""})
public class CondHasSpell extends Condition {
    private Expression<Entity> entities;
    private Expression<String> spells;

    public static void register() {
    	Skript.registerCondition(CondHasSpell.class,
				"%entities% (has|have|know|knows) [spell[s]] %strings%",
				"%entities% (ha(s|ve) not|do[es]n't have|do[es]n't know|do[es] not know) [spell[s]] %strings%");
    }

    @SuppressWarnings({"unchecked"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		entities = (Expression<Entity>) vars[0];
		spells = (Expression<String>) vars[1];
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
				return spells.check(e, new Checker<String>() {
					@Override
					public boolean check(final String spellKey) {
					    Wand wand = mage.getActiveWand();
					    if (wand != null && wand.hasSpell(spellKey)) {
					        return true;
                        }
                        MageClass activeClass = mage.getActiveClass();
					    if (activeClass != null && activeClass.hasSpell(spellKey)) {
					        return true;
                        }
                        return false;
					}
				}, isNegated());
			}
		});
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return entities.toString(e, debug) + (entities.isSingle() ? " knows" : " know") + (isNegated() ? " not" : "") + " spell " + spells;
	}
}
