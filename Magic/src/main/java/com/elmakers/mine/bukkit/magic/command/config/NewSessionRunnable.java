package com.elmakers.mine.bukkit.magic.command.config;

import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.google.gson.Gson;

public class NewSessionRunnable extends HttpRequest {
    private final NewSessionCallback callback;

    public NewSessionRunnable(MagicController controller, Gson gson, CommandSender sender, NewSessionRequest request, NewSessionCallback callback) {
        super(controller, gson, sender, gson.toJson(request),controller.getEditorURL() + "/session/new");
        this.callback = callback;
    }

    @Override
    public void processResponse(String response) {
        NewSessionResponse newSessionResponse = gson.fromJson(response, NewSessionResponse.class);
        if (!newSessionResponse.isSuccess()) {
            fail(controller.getMessages().get("commands.mconfig.editor.server_error")
                    .replace("$message", newSessionResponse.getMessage()));
        } else {
            String message = controller.getMessages().get("commands.mconfig.editor.new_session");
            String session = newSessionResponse.getSession();
            message = message.replace("$url", controller.getEditorURL() + "/" + session);
            success(message);

            if (callback != null) {
                callback.success(session);
            }
        }
    }
}
