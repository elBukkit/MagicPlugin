package com.elmakers.mine.bukkit.integration;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import com.elmakers.mine.bukkit.api.event.CastEvent;
import com.elmakers.mine.bukkit.api.event.PreCastEvent;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.integration.skript.CondHasWand;
import com.elmakers.mine.bukkit.integration.skript.CondIsClass;
import com.elmakers.mine.bukkit.integration.skript.ExprCaster;
import com.elmakers.mine.bukkit.integration.skript.ExprTargets;
import com.elmakers.mine.bukkit.integration.skript.SkriptCastEffect;
import com.elmakers.mine.bukkit.integration.skript.SkriptCastEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SkriptManager {

    public SkriptManager(MageController controller) {
        Plugin plugin = controller.getPlugin();

        Skript.registerEvent("Cast Spell", SkriptCastEvent.class, CastEvent.class, "cast [[of] [spell] %-string%]")
            .description("Called when a player or magic mob casts a spell")
            .examples("on cast", "on cast of missile", "on cast of spell blink");

        Skript.registerEvent("Cast Spell", SkriptCastEvent.class, PreCastEvent.class, "casting [[of] [spell] %-string%]")
            .description("Called when a player or magic mob is about to cast a spell")
            .examples("on cast", "on casting of missile", "on casting of spell blink");

        Skript.registerEffect(SkriptCastEffect.class,
				"cast [the] [spell] %string% [by %-commandsenders%] [with %-string%]",
				"(let|make) %commandsenders% cast [[the] spell] %string% [with %-string%]");

		Skript.registerCondition(CondIsClass.class, "%entities% (is|are) class %strings%", "%entities% (isn't|is not|aren't|are not) class %strings%");
		Skript.registerCondition(CondHasWand.class,
	"[%entities%] ha(s|ve) wand [%-strings%] in [main] hand",
			"[%entities%] (is|are) holding wand [%-strings%] [in main hand]",
			"[%entities%] ha(s|ve) wand [%-strings%] in off[(-| )]hand",
			"[%entities%] (is|are) holding wand [%-strings%] in off[(-| )]hand",
			"[%entities%] (ha(s|ve) not|do[es]n't have) wand [%-strings%] in [main] hand",
			"[%entities%] (is not|isn't) holding wand [%-strings%] [in main hand]",
			"[%entities%] (ha(s|ve) not|do[es]n't have) wand [%-strings%] in off[(-| )]hand",
			"[%entities%] (is not|isn't) holding wand [%-strings%] in off[(-| )]hand"
		);

		EventValues.registerEventValue(CastEvent.class, Player.class, new Getter<Player, CastEvent>() {
			@Override
			public Player get(final CastEvent e) {
				return e.getMage().getPlayer();
			}
		}, 0);

		EventValues.registerEventValue(CastEvent.class, Entity.class, new Getter<Entity, CastEvent>() {
			@Override
			public Entity get(final CastEvent e) {
				return e.getMage().getEntity();
			}
		}, 0);

		Skript.registerExpression(ExprCaster.class, Entity.class, ExpressionType.SIMPLE, "[the] (caster)");
		Skript.registerExpression(ExprTargets.class, Entity.class, ExpressionType.SIMPLE, "[the] (targets)");

        plugin.getLogger().info("Skript found:");
        plugin.getLogger().info("  Added events: cast, casting");
        plugin.getLogger().info("  Added expressions: caster, targets");
        plugin.getLogger().info("  Added conditionals: has wand, is class");
        plugin.getLogger().info("  Added effect: cast");
    }
}
