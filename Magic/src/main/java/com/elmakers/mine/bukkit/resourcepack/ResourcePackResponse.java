package com.elmakers.mine.bukkit.resourcepack;

import java.util.List;

public interface ResourcePackResponse {
    void finished(boolean success, boolean hasModifiedTime, List<String> responses, ResourcePack pack);
}
