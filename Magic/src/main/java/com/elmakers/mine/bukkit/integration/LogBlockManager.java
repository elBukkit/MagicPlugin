package com.elmakers.mine.bukkit.integration;

import javax.annotation.Nonnull;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;

public class LogBlockManager {
    private final Consumer consumer;
    private final Plugin owner;

    public LogBlockManager(Plugin owningPlugin, Plugin logBlockPlugin) {
        this.owner = owningPlugin;
        consumer = ((LogBlock)logBlockPlugin).getConsumer();
    }

    public void logBlockChange(@Nonnull Entity entity, @Nonnull BlockState priorState, @Nonnull BlockState newState) {
        Actor actor = Actor.actorFromEntity(entity);
        consumer.queueBlockReplace(actor, priorState, newState);
    }
}
