/*
 * Copyright (c) 2022, Adam <Adam@sigterm.info>
 * Copyright (c) 2022, Ankou <https://github.com/AnkouOSRS>
 * Copyright (c) 2025, perezect <https://github.com/perezect>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.unpottedreminder.overlays;

import com.unpottedreminder.UnpottedReminderConfig;
import com.unpottedreminder.UnpottedReminderOverlay;
import com.unpottedreminder.UnpottedReminderStyle;
import com.unpottedreminder.trackers.MagicTracker;
import net.runelite.api.Client;

import javax.inject.Inject;
import java.awt.*;

public class MagicOverlay extends UnpottedReminderOverlay {
    @Inject
    public MagicOverlay(Client client, UnpottedReminderConfig config, MagicTracker tracker) {
        super(client, config, tracker);
    }

    @Override
    protected String getLongText() {
        return "You need to to drink your magic potion!";
    }

    @Override
    protected String getShortText() {
        return "Magic";
    }

    @Override
    protected String getCustomText() {
        return config.magicCustomText();
    }

    @Override
    protected UnpottedReminderStyle getReminderStyle() {
        return config.magicReminderStyle();
    }

    @Override
    protected boolean shouldFlash() {
        return config.magicShouldFlash();
    }

    @Override
    protected Color getColor() {
        return config.magicColor();
    }

    @Override
    protected Color getFlashColor() {
        return config.magicFlashColor();
    }

    @Override
    protected int getTimeoutSeconds() {
        return config.magicTimeout();
    }
}