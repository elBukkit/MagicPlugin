package com.elmakers.mine.bukkit.integration.skript;

import org.bukkit.Location;
import org.bukkit.event.Event;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.MagicPlugin;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;

@Name("Spawn Magic Mob")
@Description("Spawn a magic mob.")
@Examples({"spawn 3 \"warlock\" at the targeted block", "spawn a \"mutant\" 5 meters above the player"})
public class EffSpawn extends Effect {
    private Expression<Location> locations;
    private Expression<String> mobKeys;
    private Expression<Number> amount;

    public static void register() {
        Skript.registerEffect(EffSpawn.class,
            "spawn [a] %strings% [%directions% %locations%]",
            "spawn %number% of %strings% [%directions% %locations%]");
    }

    @Override
    protected void execute(Event event) {
        Number count = null;
        if (amount != null) {
            count = amount.getSingle(event);
        };
        if (count == null) {
            count = 1;
        }
        MageController controller = MagicPlugin.getAPI().getController();
        final String[] keys = mobKeys.getArray(event);
        for (final Location location : locations.getArray(event)) {
            for (final String mobKey : keys) {
                for (int i = 0; i < count.doubleValue(); i++) {
                    controller.spawnMob(mobKey, location);
                }
            }
        }
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "spawn " + (amount != null ? amount.toString(event, debug) + " " : "") + mobKeys.toString(event, debug) + " " + locations.toString(event, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
        amount = matchedPattern == 0 ? null : (Expression<Number>)exprs[0];
        mobKeys = (Expression<String>) exprs[matchedPattern];
        locations = Direction.combine((Expression<? extends Direction>) exprs[1 + matchedPattern], (Expression<? extends Location>) exprs[2 + matchedPattern]);
        return true;
    }
}
