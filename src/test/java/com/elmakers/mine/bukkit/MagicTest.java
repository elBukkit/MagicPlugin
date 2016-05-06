package com.elmakers.mine.bukkit;

import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.utility.NMSUtilsMocker;
import com.mvplugin.testing.FileLocations;
import com.mvplugin.testing.ServerFactory;
import com.mvplugin.testing.TestingServer;
import de.slikey.effectlib.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Server;
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
@PrepareForTest({NMSUtils.class, ReflectionUtils.PackageType.class, ReflectionUtils.class})
@SuppressStaticInitializationFor({"com.elmakers.mine.bukkit.utility.NMSUtils",
        "de.slikey.effectlib.util.ReflectionUtils", "de.slikey.effectlib.util.ReflectionUtils.PackageType"})
public class MagicTest {

    private Server server = null;
    protected MagicPlugin plugin;

    @Before
    public void initialSetup() throws Exception {
        FileLocations.setupDirectories();
        PowerMockito.mockStatic(NMSUtils.class);
        NMSUtilsMocker.initializeNMSUtilsMocking();
        PowerMockito.mockStatic(ReflectionUtils.class);
        PowerMockito.mockStatic(ReflectionUtils.PackageType.class); // TODO this is statically initializing the class
        // before the following line has a change to work or something...
        when(ReflectionUtils.PackageType.getServerVersion()).thenReturn("1_9_R1");

        reloadServer();
        extraSetup();
    }

    /**
     * Implement to perform extra @Before test steps after test directories are setup and testing server created.
     */
    protected void extraSetup() throws Exception { }

    @After
    public void cleanupDirectories() throws Exception {
        extraCleanup();
        FileLocations.cleanupDirectories();
    }

    /**
     * Implement to perform extra @After test steps before test directories are cleaned up.
     */
    protected void extraCleanup() throws Exception { }



    public void reloadServer() throws Exception {
        if (server != null) {
            server.shutdown();
        }
        server = prepareBukkit(new PluginDescriptionFile("Magic", "Test", MagicPlugin.class.getName()));
        plugin = (MagicPlugin) server.getPluginManager().getPlugin("Magic");
    }

    private static Server prepareBukkit(PluginDescriptionFile... plugins) throws Exception {
        final TestingServer testingServer = ServerFactory.createTestingServer();
        System.out.println("Testing server created");

        Field field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, testingServer);
        assertSame(testingServer, Bukkit.getServer());

        for (PluginDescriptionFile pluginInfo : plugins) {
            testingServer.getPluginManager().loadPlugin(pluginInfo);
        }
        testingServer.loadDefaultWorlds();
        for (Plugin plugin : testingServer.getPluginManager().getPlugins()) {
            testingServer.getPluginManager().enablePlugin(plugin);
        }

        return testingServer;
    }
}
