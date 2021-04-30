package com.elmakers.mine.bukkit.utility;

import javax.annotation.Nullable;

public interface Replacer {
    @Nullable
    String getReplacement(String line, boolean integerValues);
}
