package com.elmakers.mine.bukkit.api.magic;

import java.util.List;

public interface Messages {
    public String get(String key);
    public String getRandomized(String key);
    public String get(String key, String defaultValue);
    public List<String> getAll(String path);
    public String getParameterized(String key, String paramName, String paramValue);
    public String getParameterized(String key, String paramName1, String paramValue1, String paramName2, String paramValue2);
    public String escape(String source);
}
