/* Copyright (C) Multiverse Team 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.bukkit.craftbukkit.v1_9_R1;

import com.mvplugin.testing.TestingPluginManager;
import org.bukkit.Server;

public interface TestingServer extends Server {

    void loadDefaultWorlds();

    TestingPluginManager getPluginManager();

    void setAllowEnd(boolean allowEnd);

    void setAllowNether(boolean allowNether);

    void setTicksPerMonsterSpawn(int ticksPerMonsterSpawn);

    void setTicksPerAnimalSpawn(int ticksPerAnimalSpawn);
}
