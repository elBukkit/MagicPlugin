/* Copyright (C) Multiverse Team 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.mvplugin.testing;

import com.mvplugin.testing.answers.NoArgVoidAnswer;
import com.mvplugin.testing.answers.SetterAnswer;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import pluginbase.config.SerializableConfig;
import pluginbase.config.annotation.SerializableAs;
import pluginbase.config.annotation.SerializeWith;
import pluginbase.config.datasource.DataSource;
import pluginbase.config.datasource.hocon.HoconDataSource;
import pluginbase.config.serializers.Serializer;
import pluginbase.config.serializers.SerializerSet;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

public final class WorldFactory {

    @SerializableAs("World")
    private static class WorldData {

        static {
            SerializableConfig.registerSerializableAsClass(WorldData.class);
        }

        String name;
        @SerializeWith(UUIDSerializer.class)
        UUID uuid;
        WorldType type = WorldType.NORMAL;
        Environment environment;
        long seed = 0L;
        transient long time = 0L;
        boolean pvp = true;
        boolean keepSpawnInMemory = true;
        Difficulty difficulty = Difficulty.EASY;
        @SerializeWith(LocationSerializer.class)
        Location spawn = new Location(null, 0, 0, 0);

        transient File folder;
        transient File datFile;

        private WorldData() { }

        private WorldData(WorldCreator creator, World mockedWorld) {
            this.name = creator.name();
            this.environment = creator.environment();
            this.type = creator.type();
            this.seed = creator.seed();
            this.uuid = UUID.nameUUIDFromBytes(name.getBytes());
            folder = new File(FileLocations.WORLDS_DIRECTORY, name);
            datFile = new File(folder, "level.dat");
            if (!datFile.exists()) {
                save();
            } else {
                try {
                    DataSource dataSource = HoconDataSource.builder().setFile(datFile).build();
                    WorldData world = (WorldData) dataSource.load();
                    if (world != null) {
                        this.type = world.type;
                        this.seed = world.seed;
                        this.uuid = world.uuid;
                        this.environment = world.environment;
                        this.spawn = new Location(mockedWorld, world.spawn.getX(), world.spawn.getY(),
                                world.spawn.getZ(), world.spawn.getYaw(), world.spawn.getPitch());
                        this.difficulty = world.difficulty;
                        this.pvp = world.pvp;
                        this.keepSpawnInMemory = world.keepSpawnInMemory;
                    } else {
                        save();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void save() {
            try {
                if (!datFile.exists()) {
                    folder.mkdirs();
                    datFile.createNewFile();
                }
                DataSource dataSource = HoconDataSource.builder().setFile(datFile).build();
                dataSource.save(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static class UUIDSerializer implements Serializer<UUID> {
            @Nullable
            @Override
            public Object serialize(@Nullable UUID uuid, @NotNull SerializerSet serializerSet) {
                return uuid != null ? uuid.toString() : null;
            }

            @Nullable
            @Override
            public UUID deserialize(@Nullable Object o, @NotNull Class uuidClass, @NotNull SerializerSet serializerSet) throws IllegalArgumentException {
                return o != null ? UUID.fromString(o.toString()) : null;
            }
        }

        private static class LocationSerializer implements Serializer<Location> {
            @Nullable
            @Override
            public Object serialize(@Nullable Location l, @NotNull SerializerSet serializerSet) {
                if (l == null) {
                    return null;
                }
                Map<String, Object> map = new HashMap<String, Object>(3);
                map.put("x", l.getX());
                map.put("y", l.getY());
                map.put("z", l.getZ());
                return map;
            }

            @Nullable
            @Override
            public Location deserialize(@Nullable Object serialized, @NotNull Class wantedType, @NotNull SerializerSet serializerSet) throws IllegalArgumentException {
                if (serialized == null || !(serialized instanceof Map)) {
                    return null;
                }
                Map map = (Map) serialized;
                double x = 0, y = 0, z = 0;
                try {
                    x = Double.valueOf(map.get("x").toString());
                    y = Double.valueOf(map.get("y").toString());
                    z = Double.valueOf(map.get("z").toString());
                } catch (Exception ignore) { }
                return new Location(null, x, y, z);
            }
        }

    }

    public static World createWorld(WorldCreator creator) {
        final World world = mock(World.class);
        final WorldData data = new WorldData(creator, world);

        doAnswer(new NoArgVoidAnswer() {
            @Override
            public void call() {
                data.save();
            }
        }).when(world).save();
        when(world.getName()).thenReturn(data.name);
        when(world.getUID()).thenReturn(data.uuid);
        when(world.getSpawnLocation()).thenAnswer(new Answer<Location>() {
            @Override
            public Location answer(InvocationOnMock invocationOnMock) throws Throwable {
                return data.spawn;
            }
        });
        when(world.setSpawnLocation(anyInt(), anyInt(), anyInt())).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                data.spawn = new Location(world, (Integer) invocationOnMock.getArguments()[0],
                        (Integer) invocationOnMock.getArguments()[1],
                        (Integer) invocationOnMock.getArguments()[2]);
                return true;
            }
        });
        when(world.getTime()).thenAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocationOnMock) throws Throwable {
                return data.time;
            }
        });
        doAnswer(new SetterAnswer<Long>() {
            @Override
            public void set(Long value) {
                data.time = value;
            }
        }).when(world).setTime(anyLong());
        when(world.getFullTime()).thenAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocationOnMock) throws Throwable {
                return data.time;
            }
        });
        doAnswer(new SetterAnswer<Long>() {
            @Override
             public void set(Long value) {
                data.time = value;
            }
        }).when(world).setFullTime(anyLong());
        when(world.getEnvironment()).thenReturn(data.environment);
        when(world.getSeed()).thenReturn(data.seed);
        when(world.getPVP()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                return data.pvp;
            }
        });
        doAnswer(new SetterAnswer<Boolean>() {
            @Override
            protected void set(Boolean value) {
                data.pvp = value;
            }
        }).when(world).setPVP(anyBoolean());
        when(world.getKeepSpawnInMemory()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                return data.keepSpawnInMemory;
            }
        });
        doAnswer(new SetterAnswer<Boolean>() {
            @Override
            protected void set(Boolean value) {
                data.keepSpawnInMemory = value;
            }
        }).when(world).setKeepSpawnInMemory(anyBoolean());
        when(world.getDifficulty()).thenAnswer(new Answer<Difficulty>() {
            @Override
            public Difficulty answer(InvocationOnMock invocationOnMock) throws Throwable {
                return data.difficulty;
            }
        });
        doAnswer(new SetterAnswer<Difficulty>() {
            @Override
            protected void set(Difficulty value) {
                data.difficulty = value;
            }
        }).when(world).setDifficulty(any(Difficulty.class));
        when(world.getWorldFolder()).thenReturn(data.folder);
        when(world.getWorldType()).thenReturn(data.type);

        return world;
    }

    private WorldFactory() { }
}
