package com.elmakers.mine.bukkit.utility.platform.v1_12.entity;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Llama;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityLlamaData extends EntityChestedHorseData {
    private Llama.Color color;
    private int strength;

    public EntityLlamaData() {

    }

    public EntityLlamaData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        Logger log = controller.getLogger();
        String colorString = parameters.getString("llama_color");
        if (colorString != null && !colorString.isEmpty()) {
            try {
                color = Llama.Color.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid llama_color: " + colorString, ex);
            }
        }
        strength = parameters.getInt("llama_strength", 1);
    }

    public EntityLlamaData(Entity entity, MageController controller) {
        super(entity, controller);
        if (entity instanceof Llama) {
            Llama llama = (Llama)entity;
            color = llama.getColor();
            strength = llama.getStrength();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Llama) {
            Llama llama = (Llama)entity;
            if (color != null) {
                llama.setColor(color);
            }
            if (strength > 0 && strength < 6) {
                llama.setStrength(strength);
            }
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }
        Llama llama = (Llama)entity;
        Llama.Color type = llama.getColor();
        Llama.Color[] typeValues = Llama.Color.values();
        int typeOrdinal = (type.ordinal() + 1) % typeValues.length;
        type = typeValues[typeOrdinal];
        llama.setColor(type);
        return true;
    }

    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Llama;
    }
}
