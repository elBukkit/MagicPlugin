package com.elmakers.mine.bukkit.magic.command.config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.google.gson.Gson;

public abstract class HttpPost extends AsyncProcessor implements Runnable {
    private final String requestBody;
    private final String url;
    protected final MagicController controller;
    protected final CommandSender sender;
    protected final Gson gson;

    public HttpPost(MagicController controller, Gson gson, CommandSender sender, String requestBody, String url) {
        this.controller = controller;
        this.gson = gson;
        this.sender = sender;
        this.requestBody = requestBody;
        this.url = url;
    }

    @Override
    public void run() {
        HttpsURLConnection connection;
        try {
            URL url = new URL(this.url);
            connection = (HttpsURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.getBytes("UTF-8"));
            outputStream.close();

            BufferedReader responseInput = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            String response = "";
            while ((line = responseInput.readLine()) != null) {
                response += line;
            }
            processResponse(response);
        } catch (Exception ex) {
            fail(controller.getMessages().get("commands.mconfig.editor.error"), "Error processing HTTP request to " + this.url, ex);
            return;
        }
    }

    protected abstract void processResponse(String response);

    protected void success(String message) {
        success(controller, sender, message);
    }

    protected void fail(String message) {
        fail(controller, sender, message);
    }

    protected void fail(String message, String errorMessage, Exception ex) {
        fail(controller, sender, message, errorMessage, ex);
    }
}
