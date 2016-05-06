/* Copyright (C) Multiverse Team 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.mvplugin.testing;

import com.mvplugin.testing.answers.NoArgVoidAnswer;
import com.mvplugin.testing.answers.SetterAnswer;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpMap;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

public final class ServerFactory {

    private static class ServerData {
        private Logger logger = Logger.getLogger("MockBukkit");
        private List<World> worlds = new ArrayList<World>();
        private List<Player> onlinePlayers = new ArrayList<Player>();
        private boolean allowEnd = true;
        private boolean allowNether = true;

        private GameMode defaultGameMode = GameMode.SURVIVAL;

        private int monsterSpawn = 70;
        private int animalSpawn = 15;
        private int waterAnimalSpawn = 5;
        private int ambientSpawn = 15;
        private int ticksPerMonsterSpawn = 1;
        private int ticksPerAnimalSpawn = 400;

        private TestingPluginManager pluginManager;
        private BukkitScheduler bukkitScheduler = PowerMockito.mock(BukkitScheduler.class);
        private Messenger messenger = PowerMockito.mock(Messenger.class);
        private ServicesManager servicesManager = new SimpleServicesManager();
        private HelpMap helpMap = mock(HelpMap.class);

        ServerData(Server server) {
            pluginManager = new TestingPluginManager(server);
        }
    }

    public static TestingServer createTestingServer() {

        final TestingServer server = mock(TestingServer.class);
        final ServerData data = new ServerData(server);

        when(server.getName()).thenReturn("MockBukkit");
        when(server.getVersion()).thenReturn("0.0.1");
        when(server.getBukkitVersion()).thenReturn("1.9.1-R0.1-SNAPSHOT");
        when(server.getLogger()).thenReturn(data.logger);
        when(server.getOnlinePlayers()).thenAnswer(new Answer<Collection<Player>>() {
            @Override
            public Collection<Player> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return data.onlinePlayers;
            }
        });
        when(server.getMaxPlayers()).thenReturn(Integer.MAX_VALUE);
        when(server.getPort()).thenReturn(25565);
        when(server.getViewDistance()).thenReturn(15);
        when(server.getIp()).thenReturn("127.0.0.1");
        when(server.getServerName()).thenReturn("MockBukkit");
        when(server.getServerId()).thenReturn("1");
        when(server.getWorldType()).thenReturn("DEFAULT");
        when(server.getGenerateStructures()).thenReturn(true);
        doAnswer(new SetterAnswer<Boolean>() {
            @Override
            public void set(Boolean value) {
                data.allowEnd = value;
            }
        }).when(server).setAllowEnd(anyBoolean());
        when(server.getAllowEnd()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                return data.allowEnd;
            }
        });
        doAnswer(new SetterAnswer<Boolean>() {
            @Override
            protected void set(Boolean value) {
                data.allowNether = value;
            }
        }).when(server).setAllowNether(anyBoolean());
        when(server.getAllowNether()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                return data.allowNether;
            }
        });
        when(server.broadcastMessage(anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
                String message = (String) invocationOnMock.getArguments()[0];
                for (Player player : data.onlinePlayers) {
                    player.sendMessage(message);
                }
                return data.onlinePlayers.size();
            }
        });
        when(server.getUpdateFolder()).thenReturn(FileLocations.UPDATES_DIRECTORY.toString());
        when(server.getUpdateFolderFile()).thenReturn(FileLocations.UPDATES_DIRECTORY);
        doAnswer(new SetterAnswer<Integer>() {
            @Override
            protected void set(Integer value) {
                data.ticksPerAnimalSpawn = value;
            }
        }).when(server).setTicksPerAnimalSpawn(anyInt());
        doAnswer(new SetterAnswer<Integer>() {
            @Override
            protected void set(Integer value) {
                data.ticksPerMonsterSpawn = value;
            }
        }).when(server).setTicksPerMonsterSpawn(anyInt());
        when(server.getTicksPerAnimalSpawns()).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
                return data.ticksPerAnimalSpawn;
            }
        });
        when(server.getTicksPerMonsterSpawns()).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
                return data.ticksPerMonsterSpawn;
            }
        });
        when(server.getPlayer(anyString())).thenAnswer(new Answer<Player>() {
            @Override
            public Player answer(InvocationOnMock invocationOnMock) throws Throwable {
                String s = (String) invocationOnMock.getArguments()[0];
                s = s.toLowerCase();
                for (Player player : data.onlinePlayers) {
                    if (player.getName().toLowerCase().startsWith(s)) {
                        return player;
                    }
                }
                return null;
            }
        });
        when(server.getPlayerExact(anyString())).thenAnswer(new Answer<Player>() {
            @Override
            public Player answer(InvocationOnMock invocationOnMock) throws Throwable {
                String s = (String) invocationOnMock.getArguments()[0];
                for (Player player : data.onlinePlayers) {
                    if (player.getName().equals(s)) {
                        return player;
                    }
                }
                return null;
            }
        });
        when(server.getPlayer(any(UUID.class))).thenAnswer(new Answer<Player>() {
            @Override
            public Player answer(InvocationOnMock invocationOnMock) throws Throwable {
                UUID uuid = (UUID) invocationOnMock.getArguments()[0];
                for (Player player : data.onlinePlayers) {
                    if (player.getUniqueId().equals(uuid)) {
                        return player;
                    }
                }
                return null;
            }
        });
        when(server.getPluginManager()).thenReturn(data.pluginManager);
        when(server.getScheduler()).thenReturn(data.bukkitScheduler);
        when(server.getServicesManager()).thenReturn(data.servicesManager);
        when(server.getMessenger()).thenReturn(data.messenger);
        when(server.getWorlds()).thenReturn(data.worlds);
        when(server.createWorld(any(WorldCreator.class))).thenAnswer(new Answer<World>() {
            @Override
            public World answer(InvocationOnMock invocationOnMock) throws Throwable {
                World world = WorldFactory.createWorld((WorldCreator) invocationOnMock.getArguments()[0]);
                data.worlds.add(world);
                return world;
            }
        });
        when(server.unloadWorld(anyString(), anyBoolean())).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                World world = server.getWorld((String) invocationOnMock.getArguments()[0]);
                return server.unloadWorld(world, (Boolean) invocationOnMock.getArguments()[1]);
            }
        });
        when(server.unloadWorld(any(World.class), anyBoolean())).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                World world = (World) invocationOnMock.getArguments()[0];
                if (world != null) {
                    return data.worlds.remove(world);
                }
                return false;
            }
        });
        when(server.getWorld(anyString())).thenAnswer(new Answer<World>() {
            @Override
            public World answer(InvocationOnMock invocationOnMock) throws Throwable {
                String s = (String) invocationOnMock.getArguments()[0];
                for (World w : data.worlds) {
                    if (w.getName().equals(s)) {
                        return w;
                    }
                }
                return null;
            }
        });
        when(server.getWorld(any(UUID.class))).thenAnswer(new Answer<World>() {
            @Override
            public World answer(InvocationOnMock invocationOnMock) throws Throwable {
                UUID uuid = (UUID) invocationOnMock.getArguments()[0];
                for (World w : data.worlds) {
                    if (w.getUID().equals(uuid)) {
                        return w;
                    }
                }
                return null;
            }
        });
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                server.createWorld(new WorldCreator("world").environment(Environment.NORMAL));
                server.createWorld(new WorldCreator("world_nether").environment(Environment.NETHER));
                server.createWorld(new WorldCreator("world_the_end").environment(Environment.THE_END));
                return null;
            }
        }).when(server).loadDefaultWorlds();
        doAnswer(new NoArgVoidAnswer() {
            @Override
            protected void call() {
                // gonna need this apparently...
            }
        }).when(server).reload();
        // TODO when(server.getLogger()).thenReturn(CoreLogger.getLogger());
        when(server.getOnlineMode()).thenReturn(true);
        when(server.getAllowFlight()).thenReturn(true);
        when(server.isHardcore()).thenReturn(false);
        when(server.useExactLoginLocation()).thenReturn(true);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                System.out.println("Saving worlds...");
                for (World world : data.worlds) {
                    world.save();
                }
                return null;
            }
        }).when(server).shutdown();
        when(server.getDefaultGameMode()).thenAnswer(new Answer<GameMode>() {
            @Override
            public GameMode answer(InvocationOnMock invocationOnMock) throws Throwable {
                return data.defaultGameMode;
            }
        });
        doAnswer(new SetterAnswer<GameMode>() {
            @Override
            protected void set(GameMode value) {
                data.defaultGameMode = value;
            }
        }).when(server).setDefaultGameMode(any(GameMode.class));
        when(server.getWorldContainer()).thenReturn(FileLocations.WORLDS_DIRECTORY);
        when(server.getOfflinePlayers()).thenReturn(new OfflinePlayer[0]);
        when(server.getHelpMap()).thenReturn(data.helpMap);
        when(server.getMonsterSpawnLimit()).thenReturn(data.monsterSpawn);
        when(server.getAnimalSpawnLimit()).thenReturn(data.animalSpawn);
        when(server.getWaterAnimalSpawnLimit()).thenReturn(data.waterAnimalSpawn);
        when(server.getAmbientSpawnLimit()).thenReturn(data.ambientSpawn);
        when(server.isPrimaryThread()).thenReturn(true);
        when(server.getMotd()).thenReturn("Hello, world");
        when(server.getShutdownMessage()).thenReturn("Server shutting down...");
        when(server._INVALID_getOnlinePlayers()).thenReturn(data.onlinePlayers.toArray(new Player[data.onlinePlayers.size()]));

        return server;
    }

    private ServerFactory() { }
}
