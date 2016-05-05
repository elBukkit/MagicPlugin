/* Copyright (C) Multiverse Team 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.mvplugin.testing;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class FileLocations {

    public static final File SERVER_DIRECTORY = new File("bin/server");
    public static final File UPDATES_DIRECTORY = new File(SERVER_DIRECTORY, "updates");
    public static final File WORLDS_DIRECTORY = new File(SERVER_DIRECTORY, "worlds");
    public static final File PLUGIN_DIRECTORY = new File(SERVER_DIRECTORY, "plugins");
    public static final File MAGIC_DIRECTORY = new File(PLUGIN_DIRECTORY, "Multiverse-Core");

    public static void setupDirectories() {
        MAGIC_DIRECTORY.mkdirs();
        UPDATES_DIRECTORY.mkdirs();
        WORLDS_DIRECTORY.mkdirs();
    }

    public static void cleanupDirectories() throws IOException {
        FileUtils.deleteDirectory(SERVER_DIRECTORY);
    }
}
