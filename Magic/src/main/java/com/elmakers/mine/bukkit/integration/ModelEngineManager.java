package com.elmakers.mine.bukkit.integration;

import java.util.Iterator;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.integration.ModelEngine;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.ModelBlueprint;
import com.ticxo.modelengine.api.generator.blueprint.Bone;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.PartEntity;

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

        ModeledEntity modeledEntity = api.getModelManager().getModeledEntity(entity.getUniqueId());
        if (modeledEntity == null) {
            modeledEntity = api.getModelManager().createModeledEntity(entity);
        }
        if (modeledEntity == null) {
            return false;
        }

        if (modeledEntity.getAllActiveModel().containsKey(modelName)) {
            return true;
        }

        ActiveModel model = api.getModelManager().createActiveModel(modelName);
        if (model == null) {
            return false;
        }

        modeledEntity.clearModels();
        try {
            modeledEntity.addActiveModel(model);
        } catch (Exception ex) {
            owningPlugin.getLogger().log(Level.WARNING, "Error applying ModelEngine model id '" + modelName + "'", ex);
            return false;
        }
        modeledEntity.detectPlayers();
        modeledEntity.setInvisible(config.getBoolean("invisible", true));
        return true;
    }

    @Override
    public boolean removeModel(Entity entity, String modelId) {
        if (entity == null || modelId == null) return false;
        ModeledEntity modeledEntity = api.getModelManager().createModeledEntity(entity);
        if (modeledEntity == null) {
            return false;
        }
        ActiveModel activeModel = modeledEntity.getActiveModel(modelId);
        if (activeModel != null) {
            activeModel.clearModel();
        }
        modeledEntity.removeModel(modelId);
        return true;
    }

    @Override
    public boolean removeAllModels(Entity entity) {
        if (entity == null) return false;
        ModeledEntity modeledEntity = api.getModelManager().createModeledEntity(entity);
        if (modeledEntity == null) {
            return false;
        }
        modeledEntity.clearModels();
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

    @Override
    public boolean removeSubModel(Entity entity, @Nullable String modelId, String subPartId, String customId) {
        if (entity == null || modelId == null) return false;
        ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().getModeledEntity(entity.getUniqueId());
        if (modeledEntity == null) {
            return false;
        }
        if (subPartId == null && customId == null) {
            return false;
        }
        ActiveModel currentModel = modeledEntity.getActiveModel(modelId);
        if (currentModel == null) {
            return false;
        }
        PartEntity partEntity = currentModel.getPartEntity(customId != null ? customId : subPartId);
        if (partEntity == null) {
            return false;
        }

        partEntity.clearModel();
        partEntity.getParent().removeChild(partEntity);
        return true;
    }

    @Override
    public boolean addSubModel(Entity entity, String modelId, String partId, String subModelId, String subPartId, String customId) {
        if (entity == null || modelId == null) return false;
        ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().getModeledEntity(entity.getUniqueId());
        if (modeledEntity == null) {
            return false;
        }
        ActiveModel currentModel = modeledEntity.getActiveModel(modelId);
        if (currentModel == null) {
            return false;
        }
        PartEntity partEntity = currentModel.getPartEntity(partId);
        if (partEntity == null) {
            return false;
        }

        ModelBlueprint blueprint = ModelEngineAPI.api.getModelBlueprint(subModelId);
        if (blueprint == null) {
            return false;
        }
        Bone bone = blueprint.getBone(subPartId);
        if (bone == null) {
            return false;
        }

        Vector pos = new Vector(bone.getLocalOffsetX(), bone.getLocalOffsetY(), bone.getLocalOffsetZ());
        EulerAngle rot = new EulerAngle(bone.getLocalRotationX(), bone.getLocalRotationY(), bone.getLocalRotationZ());
        int id = blueprint.getItemModelId(subPartId);
        PartEntity part = null;

        // Er.. whoops.. no API for this?
        /*
        if (id == 0) {
            if (bone.getOption("item_right")) {
                part = new Right(currentModel, blueprint, subPartId, pos, rot, partEntity);
            } else if (bone.getOption("item_left")) {
                part = new Left(currentModel, blueprint, subPartId, pos, rot, partEntity);
            } else {
                part = new MEBasePartEntity(currentModel, blueprint, subPartId, pos, rot, partEntity);
            }
        } else {
            part = new MEPartEntity(currentModel, blueprint, subPartId, id, pos, rot, partEntity);
        }
        */

        if (part == null) {
            return false;
        }

        part.setCustomId(customId);
        part.generatePartEntities(blueprint, currentModel, bone.getChildren());
        part.initialize();
        partEntity.addChild(part);
        Iterator<Player> it = currentModel.getModeledEntity().getPlayerInRange().iterator();

        while (it.hasNext()) {
            Player player = it.next();
            part.showModel(player);
        }

        currentModel.addToIndex(part);
        return true;
    }

    @Override
    public boolean tintModel(Entity entity, String modelId, String partId, Color color, boolean exactMatch) {
        if (entity == null) {
            return false;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().getModeledEntity(entity.getUniqueId());
        if (modeledEntity == null) {
            return false;
        }
        modeledEntity.setTint(color, modelId, partId, exactMatch);
        return true;
    }
}
