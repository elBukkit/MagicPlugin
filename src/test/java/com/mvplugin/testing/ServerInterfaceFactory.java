/* Copyright (C) Multiverse Team 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.mvplugin.testing;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import pluginbase.minecraft.BasePlayer;
import pluginbase.minecraft.Entity;
import pluginbase.minecraft.location.EntityCoordinates;
import pluginbase.minecraft.location.Locations;
import pluginbase.minecraft.location.Vector;
import pluginbase.permission.Perm;
import pluginbase.plugin.ServerInterface;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class ServerInterfaceFactory {

    public static ServerInterface getMockedServerInterface() {
        ServerInterface serverInterface = PowerMockito.mock(ServerInterface.class);

        when(serverInterface.getServerFolder()).thenReturn(FileLocations.SERVER_DIRECTORY);
        when(serverInterface.getWorldContainer()).thenReturn(FileLocations.SERVER_DIRECTORY);

        when(serverInterface.getPlayer(anyString())).thenAnswer(new Answer<BasePlayer>() {
            @Override
            public BasePlayer answer(InvocationOnMock invocation) throws Throwable {
                String name = (String) invocation.getArguments()[0];
                if (name.equals("fakeplayer")) {
                    return null;
                }
                return getMockedBasePlayer(name);
            }
        });

        return serverInterface;
    }

    private static BasePlayer getMockedBasePlayer(String name) {
        EntityPlayer player = PowerMockito.mock(EntityPlayer.class);
        final EntityData data = new EntityData();

        when(player.getName()).thenReturn(name);

        when(player.getLocation()).thenAnswer(new Answer<EntityCoordinates>() {
            @Override
            public EntityCoordinates answer(InvocationOnMock invocation) throws Throwable {
                return data.coordinates;
            }
        });
        when(player.getVelocity()).thenAnswer(new Answer<Vector>() {
            @Override
            public Vector answer(InvocationOnMock invocation) throws Throwable {
                return data.velocity;
            }
        });
        when(player.teleport(any(EntityCoordinates.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                EntityCoordinates coords = (EntityCoordinates) invocation.getArguments()[0];
                data.coordinates = coords;
                data.velocity = coords.getDirection();
                return true;
            }
        });
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                data.velocity = (Vector) invocation.getArguments()[0];
                return null;
            }
        }).when(player).setVelocity(any(Vector.class));

        when(player.hasPerm(any(Perm.class))).thenReturn(true);
        when(player.hasPerm(any(Perm.class), anyString())).thenReturn(true);

        return player;
    }

    private abstract static class EntityPlayer extends BasePlayer implements Entity { }

    private static class EntityData {
        private EntityCoordinates coordinates = Locations.getEntityCoordinates("world", 0.5, 0, 0.5, 0, 0);
        private Vector velocity = coordinates.getDirection();
    }
}
