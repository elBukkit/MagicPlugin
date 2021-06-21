package com.elmakers.mine.bukkit.utility.platform;

public abstract class PlatformInterpreter {
    private static Platform platform;

    public static Platform getPlatform() {
        if (platform == null) {
            throw new IllegalStateException("getPlatform called before initialization");
        }
        return platform;
    }

    protected static void setPlatform(Platform platform) {
        PlatformInterpreter.platform = platform;
    }
}
