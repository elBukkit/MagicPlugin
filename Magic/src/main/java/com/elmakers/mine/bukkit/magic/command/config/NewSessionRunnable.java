package com.elmakers.mine.bukkit.magic.command.config;

import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.google.gson.Gson;

public class NewSessionRunnable extends HttpRequest {

    public NewSessionRunnable(MagicController controller, Gson gson, CommandSender sender, NewSessionRequest request) {
        super(controller, gson, sender, gson.toJson(request),controller.getEditorURL() + "/session/new");
    }

    @Override
    public void processResponse(String response) {
        NewSessionResponse newSessionResponse = gson.fromJson(response, NewSessionResponse.class);
        if (!newSessionResponse.isSuccess()) {
            fail(controller.getMessages().get("commands.mconfig.editor.server_error")
                    .replace("$message", newSessionResponse.getMessage()));
        } else {
            String message = controller.getMessages().get("commands.mconfig.editor.new_session");
            message = message.replace("$url", controller.getEditorURL() + "/" + newSessionResponse.getSession());
            success(message);
        }
    }
}
