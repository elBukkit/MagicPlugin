package com.elmakers.mine.bukkit.magic.command.config;

import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.google.gson.Gson;

public class GetSessionRunnable extends HttpPost {
    private final ApplySessionCallback successCallback;

    public GetSessionRunnable(MagicController controller, Gson gson, CommandSender sender, GetSessionRequest request, ApplySessionCallback successCallback) {
        super(controller, gson, sender, gson.toJson(request),controller.getEditorURL() + "/session/get");
        this.successCallback = successCallback;
    }

    @Override
    public void processResponse(String response) {
        GetSessionResponse getSessionResponse = gson.fromJson(response, GetSessionResponse.class);
        if (!getSessionResponse.isSuccess()) {
            fail(controller.getMessages().get("commands.mconfig.editor.server_error")
                .replace("$message", getSessionResponse.getMessage()));
        } else {
            Session session = getSessionResponse.getSession();
            if (session == null) {
                fail("Missing session in response");
                return;
            }

            if (successCallback != null) {
                successCallback.success(session);
            }
        }
    }
}
