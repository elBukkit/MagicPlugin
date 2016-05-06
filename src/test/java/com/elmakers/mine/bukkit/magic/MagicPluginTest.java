package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.MagicTest;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.junit.Assert.*;

@PrepareForTest({Bukkit.class, MagicPlugin.class, JavaPlugin.class})
public class MagicPluginTest extends MagicTest {

    @Test
    public void testPluginEnabled() throws Exception {
        assertTrue(plugin.isEnabled());
    }
}
