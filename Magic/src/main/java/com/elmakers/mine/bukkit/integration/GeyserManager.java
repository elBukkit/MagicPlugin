package com.elmakers.mine.bukkit.integration;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.geysermc.api.Geyser;
import org.geysermc.cumulus.ModalForm;
import org.geysermc.cumulus.response.ModalFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class GeyserManager {

    public GeyserManager(MageController controller) {
    }

    public boolean isBedrock(UUID uuid) {
        return Geyser.api().connectionByUuid(uuid) != null;
    }

    public void showModalForm(Player player, Mage source, String title, String content, String[] buttonLabels, String[] buttonTriggers) {
        if (player == null) return;

        ModalForm.Builder builder = ModalForm.builder().title(title).content(content);
        if (buttonLabels.length > 0) {
            builder.button1(buttonLabels[0]);
        }
        if (buttonLabels.length > 1) {
            builder.button2(buttonLabels[1]);
        }
        builder.responseHandler((form, responseData) -> {
            ModalFormResponse response = form.parseResponse(responseData);
            if (!response.isCorrect()) {
                // player closed the form or returned invalid info (see FormResponse)
                return;
            }

            // short version of getClickedButtonId == 0
            if (response.getClickedButtonId() == 0) {
                if (buttonTriggers.length > 0 && source != null) {
                    source.trigger(buttonTriggers[0]);
                }
                return;
            }
            if (response.getClickedButtonId() == 1) {
                if (buttonTriggers.length > 1 && source != null) {
                    source.trigger(buttonTriggers[1]);
                }
                return;
            }
        });
        ModalForm form = builder.build();
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }
}
