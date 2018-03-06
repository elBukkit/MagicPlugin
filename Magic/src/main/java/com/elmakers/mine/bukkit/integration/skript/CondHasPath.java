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
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is On Path")
@Description("Checks whether a player is on a progression path.")
@Examples({"player is on path \"architect\"", "player does not have path \"master\"", "player has finished path \"beginner\""})
public class CondHasPath extends Condition {
    private Expression<Entity> entities;
    private Expression<String> paths;
    private boolean finished;
    private boolean exact;

    public static void register() {
    	Skript.registerCondition(CondHasPath.class,
			"%entities% (is|are) on path[s] %strings%",
			"%entities% has path[s] %strings%",
			"%entities% has finished path[s] %strings%",
			"%entities% (isn't|is not|aren't|are not) on path[s] %strings%",
			"%entities% (ha(s|ve) not|do[es]n't have) path[s] %strings%",
			"%entities% (ha(s|ve) not) finished path[s] %strings%");
    }

    @SuppressWarnings({"unchecked"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		entities = (Expression<Entity>) vars[0];
		paths = (Expression<String>) vars[1];
		setNegated(matchedPattern >= 3);
		exact = matchedPattern == 0 || matchedPattern == 3;
		finished = matchedPattern == 2 || matchedPattern == 5;
		return true;
	}

	private boolean checkPath(CasterProperties properties, String pathKey) {
        if (properties == null) return false;
        ProgressionPath path = properties.getPath();
		if (path == null) return false;

		        	org.bukkit.Bukkit.getLogger().info("Checking " + finished + "/" + exact + " " + pathKey + " " + path.hasPath(pathKey) + " " + path.getKey() + " " + path.canProgress(properties));


        if (finished) {
            if (path.hasPath(pathKey)) {
                if (path.getKey().equals(pathKey)) {
                    return !path.canProgress(properties);
                } else {
                    return true;
                }
            }
            return false;
        } else if (exact) {
            return path.getKey().equalsIgnoreCase(pathKey);
        }
        return path.hasPath(pathKey);
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
				return paths.check(e, new Checker<String>() {
					@Override
					public boolean check(final String pathKey) {
					    Wand wand = mage.getActiveWand();
					    if (checkPath(wand, pathKey)) {
					        return true;
                        }
                        MageClass activeClass = mage.getActiveClass();
					    if (checkPath(activeClass, pathKey)) {
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
    	if (exact) {
			return entities.toString(e, debug) + (entities.isSingle() ? " is" : " are") + (isNegated() ? " not" : "") + " on path " + paths;
		}
		return entities.toString(e, debug) + (entities.isSingle() ? " has" : " have") + (isNegated() ? " not" : "") + (finished ? " finished" : "") + " path " + paths;
	}
}
