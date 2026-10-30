package com.elmakers.mine.bukkit.integration;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.skriptlang.skript.lang.converter.Converter;

import com.elmakers.mine.bukkit.api.event.CastEvent;
import com.elmakers.mine.bukkit.api.event.EarnEvent;
import com.elmakers.mine.bukkit.api.event.PreCastEvent;
import com.elmakers.mine.bukkit.api.event.StartCastEvent;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.integration.skript.CondHasItem;
import com.elmakers.mine.bukkit.integration.skript.CondHasPath;
import com.elmakers.mine.bukkit.integration.skript.CondHasSpell;
import com.elmakers.mine.bukkit.integration.skript.CondIsClass;
import com.elmakers.mine.bukkit.integration.skript.EffCast;
import com.elmakers.mine.bukkit.integration.skript.EffSpawn;
import com.elmakers.mine.bukkit.integration.skript.EvtCast;
import com.elmakers.mine.bukkit.integration.skript.EvtEarn;
import com.elmakers.mine.bukkit.integration.skript.EvtPreCast;
import com.elmakers.mine.bukkit.integration.skript.EvtStartCast;
import com.elmakers.mine.bukkit.integration.skript.ExprActiveSpell;
import com.elmakers.mine.bukkit.integration.skript.ExprCaster;
import com.elmakers.mine.bukkit.integration.skript.ExprTargets;

import ch.njol.skript.registrations.EventValues;

public class SkriptManager {
    public static MageController controller;

    public SkriptManager(MageController controller) {
        Plugin plugin = controller.getPlugin();
        SkriptManager.controller = controller;

        EvtCast.register();
        EvtPreCast.register();
        EvtStartCast.register();
        EvtEarn.register();
        EffCast.register();
        EffSpawn.register();
        CondIsClass.register();
        CondHasPath.register();
        CondHasSpell.register();
        CondHasItem.register();
        ExprCaster.register();
        ExprTargets.register();
        ExprActiveSpell.register();

        EventValues.registerEventValue(CastEvent.class, Player.class, (Converter<CastEvent, Player>) e -> e.getMage().getPlayer(), 0);

        EventValues.registerEventValue(CastEvent.class, Entity.class, (Converter<CastEvent, Entity>) e -> e.getMage().getEntity(), 0);

        EventValues.registerEventValue(PreCastEvent.class, Player.class, (Converter<PreCastEvent, Player>) e -> e.getMage().getPlayer(), 0);

        EventValues.registerEventValue(PreCastEvent.class, Entity.class, (Converter<PreCastEvent, Entity>) e -> e.getMage().getEntity(), 0);

        EventValues.registerEventValue(StartCastEvent.class, Entity.class, (Converter<StartCastEvent, Entity>) e -> e.getMage().getEntity(), 0);

        EventValues.registerEventValue(EarnEvent.class, Player.class, (Converter<EarnEvent, Player>) e -> e.getMage().getPlayer(), 0);

        EventValues.registerEventValue(EarnEvent.class, Entity.class, (Converter<EarnEvent, Entity>) e -> e.getMage().getEntity(), 0);

        plugin.getLogger().info("Skript found:");
        plugin.getLogger().info("  Added events: cast, casting, casted, earn");
        plugin.getLogger().info("  Added expressions: caster, targets, active spell");
        plugin.getLogger().info("  Added conditionals: has wand, is class");
        plugin.getLogger().info("  Added effect: cast");
    }
}
