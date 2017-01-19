package com.elmakers.mine.bukkit;

import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.utility.NMSUtilsMocker;
import com.mvplugin.testing.FileLocations;
import com.mvplugin.testing.ServerFactory;
import de.slikey.effectlib.util.ParticleEffect;
import org.bukkit.craftbukkit.v1_9_R1.TestingServer;
import de.slikey.effectlib.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;

import static org.junit.Assert.assertSame;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NMSUtils.class, ReflectionUtils.PackageType.class, ReflectionUtils.class, Bukkit.class,
        ParticleEffect.class, ParticleEffect.ParticlePacket.class})
@SuppressStaticInitializationFor({"com.elmakers.mine.bukkit.utility.NMSUtils",
        "de.slikey.effectlib.util.ReflectionUtils", "de.slikey.effectlib.util.ReflectionUtils.PackageType",
        "de.slikey.effectlib.util.ParticleEffect", "de.slikey.effectlib.util.ParticleEffect.ParticlePacket"})
public class MagicTest {

    private TestingServer server = null;
    protected MagicPlugin plugin;

    @Before
    public void initialSetup() throws Exception {
        FileLocations.setupDirectories();
        recreateServer();

        PowerMockito.mockStatic(NMSUtils.class);
        NMSUtilsMocker.initializeNMSUtilsMocking();
        PowerMockito.mockStatic(ReflectionUtils.class);
        PowerMockito.mockStatic(ReflectionUtils.PackageType.class); // TODO this is statically initializing the class
        PowerMockito.mockStatic(ParticleEffect.class);
        PowerMockito.mockStatic(ParticleEffect.ParticlePacket.class);
        // before the following line has a change to work or something...
        when(ReflectionUtils.PackageType.getServerVersion()).thenReturn("v1_10_R1");

        loadPlugins(server, new PluginDescriptionFile("Magic", "Test", MagicPlugin.class.getName()));
        plugin = (MagicPlugin) server.getPluginManager().getPlugin("Magic");
        extraSetup();
    }

    /**
     * Implement to perform extra @Before test steps after test directories are setup and testing server created.
     */
    protected void extraSetup() throws Exception { }

    @After
    public void cleanupDirectories() throws Exception {
        extraCleanup();
        //FileLocations.cleanupDirectories();
    }

    /**
     * Implement to perform extra @After test steps before test directories are cleaned up.
     */
    protected void extraCleanup() throws Exception { }



    public void recreateServer() throws Exception {
        if (server != null) {
            server.shutdown();
        }
        server = prepareBukkit();
    }

    private static TestingServer prepareBukkit() throws Exception {
        final TestingServer testingServer = ServerFactory.createTestingServer();
        System.out.println("Testing server created");

        Field field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, testingServer);
        assertSame(testingServer, Bukkit.getServer());


        return testingServer;
    }

    private static void loadPlugins(TestingServer server, PluginDescriptionFile... plugins) {
        for (PluginDescriptionFile pluginInfo : plugins) {
            server.getPluginManager().loadPlugin(pluginInfo);
        }
        server.loadDefaultWorlds();
        for (Plugin plugin : server.getPluginManager().getPlugins()) {
            server.getPluginManager().enablePlugin(plugin);
        }

    }
}
