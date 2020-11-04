package com.elmakers.mine.bukkit.magic.command.config;

public class GetSessionRequest {
    private final String session;

    public GetSessionRequest(String session) {
        this.session = session;
    }

    public String getSession() {
        return session;
    }
}
