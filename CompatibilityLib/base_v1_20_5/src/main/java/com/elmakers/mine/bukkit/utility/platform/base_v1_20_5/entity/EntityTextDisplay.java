package com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityTextDisplay extends EntityDisplay {
    protected String text;

    public EntityTextDisplay(ConfigurationSection configuration, MageController controller) {
        super(configuration, controller);

        text = configuration.getString("text");
        if (text != null && !text.isEmpty()) {
            text = controller.getMessages().get(text, text);
        }
    }

    public EntityTextDisplay(Entity entity, MageController controller) {
        super(entity, controller);
        if (entity instanceof TextDisplay) {
            TextDisplay display = (TextDisplay) entity;
            this.text = display.getText();
        }
    }

    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof TextDisplay) {
            TextDisplay display = (TextDisplay) entity;
            if (text != null) {
                display.setText(text);
            }
        }
    }
}
