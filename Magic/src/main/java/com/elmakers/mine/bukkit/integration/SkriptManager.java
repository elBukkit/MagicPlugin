package com.elmakers.mine.bukkit.integration;

import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import com.elmakers.mine.bukkit.api.event.CastEvent;
import com.elmakers.mine.bukkit.api.event.PreCastEvent;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.integration.skript.CondHasItem;
import com.elmakers.mine.bukkit.integration.skript.CondHasPath;
import com.elmakers.mine.bukkit.integration.skript.CondHasSpell;
import com.elmakers.mine.bukkit.integration.skript.CondIsClass;
import com.elmakers.mine.bukkit.integration.skript.EffSpawn;
import com.elmakers.mine.bukkit.integration.skript.ExprActiveSpell;
import com.elmakers.mine.bukkit.integration.skript.ExprCaster;
import com.elmakers.mine.bukkit.integration.skript.ExprTargets;
import com.elmakers.mine.bukkit.integration.skript.EffCast;
import com.elmakers.mine.bukkit.integration.skript.EvtCast;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class SkriptManager {

    public SkriptManager(MageController controller) {
        Plugin plugin = controller.getPlugin();

        EvtCast.register();
        EffCast.register();
        EffSpawn.register();
        CondIsClass.register();
        CondHasPath.register();
        CondHasSpell.register();
        CondHasItem.register();
        ExprCaster.register();
        ExprTargets.register();
        ExprActiveSpell.register();

        EventValues.registerEventValue(CastEvent.class, Player.class, new Getter<Player, CastEvent>() {
            @Nullable
            @Override
            public Player get(final CastEvent e) {
                return e.getMage().getPlayer();
            }
        }, 0);
        
        EventValues.registerEventValue(CastEvent.class, Entity.class, new Getter<Entity, CastEvent>() {
            @Nullable
            @Override
            public Entity get(final CastEvent e) {
                return e.getMage().getEntity();
            }
        }, 0);

        EventValues.registerEventValue(PreCastEvent.class, Player.class, new Getter<Player, PreCastEvent>() {
                @Nullable
                @Override
                public Player get(final PreCastEvent e) {
                    return e.getMage().getPlayer();
                }
        }, 0);
    
        EventValues.registerEventValue(PreCastEvent.class, Entity.class, new Getter<Entity, PreCastEvent>() {
            @Nullable
            @Override
            public Entity get(final PreCastEvent e) {
                return e.getMage().getEntity();
            }
        }, 0);

        plugin.getLogger().info("Skript found:");
        plugin.getLogger().info("  Added events: cast, casting");
        plugin.getLogger().info("  Added expressions: caster, targets, active spell");
        plugin.getLogger().info("  Added conditionals: has wand, is class");
        plugin.getLogger().info("  Added effect: cast");
    }
}
