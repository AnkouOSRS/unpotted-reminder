/*
 * Copyright (c) 2022, Adam <Adam@sigterm.info>
 * Copyright (c) 2022, Ankou <https://github.com/AnkouOSRS>
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
package com.unpottedreminder;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.annotation.Nullable;
import javax.inject.Inject;

import lombok.NonNull;
import lombok.Setter;

import java.awt.*;


public abstract class UnpottedReminderOverlay extends OverlayPanel
{
    protected final Client client;
    protected final UnpottedReminderConfig config;
    protected final StyleTracker tracker;
    @Setter
    private Long startTime;

    @Inject
    protected UnpottedReminderOverlay(Client client, UnpottedReminderConfig config, StyleTracker tracker)
    {
        this.client = client;
        this.config = config;
        this.tracker = tracker;
        this.startTime = null;
        setPosition(OverlayPosition.BOTTOM_RIGHT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (isExpired())
        {
            return null;
        }

        final int padding = getTextPadding();
        final String displayText = getDisplayText();
        if (displayText == null)
        {
            return null;
        }

        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(LineComponent.builder()
            .left(displayText)
            .build());

        panelComponent.setPreferredSize(getTextWidth(graphics, displayText, padding));
        if (shouldFlash() && client.getGameCycle() % 40 >= 20)
        {
            panelComponent.setBackgroundColor(getFlashColor());
        }
        else
        {
            panelComponent.setBackgroundColor(getColor());
        }

        if (getReminderStyle() == UnpottedReminderStyle.CUSTOM_TEXT) {
          return super.render(graphics);
        } else {
          return panelComponent.render(graphics);
        }
    }

    @Nullable
    private String getDisplayText()
    {
        switch (getReminderStyle())
        {
            case LONG_TEXT:
                return getLongText();
            case SHORT_TEXT:
                return getShortText();
            case CUSTOM_TEXT:
                return getCustomText();
            default:
                return null;
        }
    }

    private int getTextPadding()
    {
        switch (getReminderStyle())
        {
            case LONG_TEXT:
            case CUSTOM_TEXT:
                return -20;
            case SHORT_TEXT:
                return 10;
            default:
                return 0;
        }
    }

    @NonNull
    private Dimension getTextWidth(Graphics2D graphics, String string, int offset)
    {
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int stringWidth = fontMetrics.stringWidth(string);
        return new Dimension(stringWidth + offset, 0);
    }

    protected boolean isExpired() {
        if (startTime == null) {
            return true;
        }

        final long timeoutMillis = getTimeoutSeconds() * 1000L;
        final long elapsedMillis = System.currentTimeMillis() - startTime;

        return elapsedMillis > timeoutMillis;
    }

    protected abstract String getLongText();

    protected abstract String getShortText();

    protected abstract String getCustomText();

    protected abstract UnpottedReminderStyle getReminderStyle();

    protected abstract boolean shouldFlash();

    protected abstract Color getColor();

    protected abstract Color getFlashColor();

    protected abstract int getTimeoutSeconds();
}
