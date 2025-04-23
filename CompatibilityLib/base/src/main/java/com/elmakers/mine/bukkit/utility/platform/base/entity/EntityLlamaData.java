package com.elmakers.mine.bukkit.utility.platform.base.entity;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Llama;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.platform.PlatformInterpreter;

public class EntityLlamaData extends EntityChestedHorseData {
    private Llama.Color color;
    private Integer strength;
    protected ItemStack decor;

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
        strength = ConfigUtils.getOptionalInteger(parameters, "llama_strength");
        String itemKey = parameters.getString("decor_item");
        if (itemKey != null && !itemKey.isEmpty()) {
            ItemData itemData = controller.getOrCreateItem(itemKey);
            if (itemData == null) {
                log.warning("Invalid decor_item in llama config: " + itemKey);
            } else {
                decor = itemData.getItemStack();
            }
        }
    }

    public EntityLlamaData(Entity entity, MageController controller) {
        super(entity, controller);
        if (entity instanceof Llama) {
            Llama llama = (Llama)entity;
            color = llama.getColor();
            strength = llama.getStrength();
            decor = PlatformInterpreter.getPlatform().getItemUtils().getCopy(llama.getInventory().getDecor());
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
            if (strength != null && strength > 0 && strength < 6) {
                llama.setStrength(strength);
            }
            if (!PlatformInterpreter.getPlatform().getItemUtils().isEmpty(decor)) {
                llama.getInventory().setDecor(decor);
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
