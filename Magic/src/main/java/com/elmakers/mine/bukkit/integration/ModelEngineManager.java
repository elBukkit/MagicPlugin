package com.elmakers.mine.bukkit.integration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;

public class ModelEngineManager {
    private final ModelEngineAPI api;
    private final Plugin owningPlugin;

    public ModelEngineManager(Plugin owningPlugin, Plugin modelEnginePlugin) {
        this.owningPlugin = owningPlugin;
        api = modelEnginePlugin instanceof ModelEngineAPI ? (ModelEngineAPI)modelEnginePlugin : null;
    }

    public boolean isValid() {
        return api != null;
    }

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
}
