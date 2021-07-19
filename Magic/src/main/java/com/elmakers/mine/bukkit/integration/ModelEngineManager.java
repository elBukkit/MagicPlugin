package com.elmakers.mine.bukkit.integration;

import java.util.Iterator;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.integration.ModelEngine;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;

public class ModelEngineManager implements ModelEngine {
    private final ModelEngineAPI api;
    private final Plugin owningPlugin;

    public ModelEngineManager(Plugin owningPlugin, Plugin modelEnginePlugin) {
        this.owningPlugin = owningPlugin;
        api = modelEnginePlugin instanceof ModelEngineAPI ? (ModelEngineAPI)modelEnginePlugin : null;
    }

    public boolean isValid() {
        return api != null;
    }

    @Override
    public boolean applyModel(Entity entity, ConfigurationSection config) {
        if (config == null || entity == null) return false;
        String modelName = config.getString("id");
        ActiveModel model = api.getModelManager().createActiveModel(modelName);
        if (model == null) {
            return false;
        }

        ModeledEntity modeledEntity = api.getModelManager().createModeledEntity(entity);
        if (modeledEntity == null) {
            return false;
        }

        modeledEntity.addActiveModel(model);
        modeledEntity.detectPlayers();
        modeledEntity.setInvisible(config.getBoolean("invisible", true));
        return true;
    }

    @Override
    public boolean removeModelState(Entity entity, @Nullable String modelId, String state, boolean ignoreLerp) {
        if (entity == null || state == null) return false;
        ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().getModeledEntity(entity.getUniqueId());
        if (modeledEntity == null) {
            return false;
        }
        if (modelId == null) {
            Iterator<ActiveModel> it = modeledEntity.getAllActiveModel().values().iterator();
            while (it.hasNext()) {
                ActiveModel activeModel = it.next();
                activeModel.removeState(state, ignoreLerp);
            }
        } else {
            ActiveModel activeModel = modeledEntity.getActiveModel(modelId);
            if (activeModel == null) {
                return false;
            }
            activeModel.removeState(state, ignoreLerp);
        }
        return true;
    }

    @Override
    public boolean addModelState(Entity entity, @Nullable String modelId, String state, int lerpIn, int lerpOut, double speed) {
        if (entity == null || state == null) return false;
        ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().getModeledEntity(entity.getUniqueId());
        if (modeledEntity == null) {
            return false;
        }
        if (modelId == null) {
            Iterator<ActiveModel> it = modeledEntity.getAllActiveModel().values().iterator();
            while (it.hasNext()) {
                ActiveModel activeModel = it.next();
                activeModel.addState(state, lerpIn, lerpOut, speed);
            }
        } else {
            ActiveModel activeModel = modeledEntity.getActiveModel(modelId);
            if (activeModel == null) {
                return false;
            }
            activeModel.addState(state, lerpIn, lerpOut, speed);
        }
        return true;
    }
}
