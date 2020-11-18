package com.elmakers.mine.bukkit.resourcepack;

import java.util.List;

public interface ResourcePackResponse {
    void finished(boolean success, List<String> responses, ResourcePack pack);
}
