/* Copyright (C) Multiverse Team 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.mvplugin.testing.answers;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public abstract class SetterAnswer<ARG_TYPE> implements Answer<Void> {

    @Override
    public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
        set((ARG_TYPE) invocationOnMock.getArguments()[0]);
        return null;
    }

    protected abstract void set(ARG_TYPE value);
}
