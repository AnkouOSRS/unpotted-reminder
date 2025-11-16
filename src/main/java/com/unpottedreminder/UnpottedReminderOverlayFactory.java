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
package com.unpottedreminder;

import com.google.inject.Inject;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.HashMap;
import java.util.Map;

public class UnpottedReminderOverlayFactory
{
    private final Map<StyleTracker, UnpottedReminderOverlay> activeOverlays;

    @Inject
    private final OverlayManager overlayManager;

    @Inject
    public UnpottedReminderOverlayFactory(OverlayManager overlayManager)
    {
        this.overlayManager = overlayManager;
        this.activeOverlays = new HashMap<>();
    }

    public void createOverlay(StyleTracker tracker)
    {
        if (tracker == null || !tracker.isStyleTracked())
        {
            return;
        }

        UnpottedReminderOverlay overlay = tracker.getOverlay();
        if (overlay == null)
        {
            return;
        }

        overlay.setStartTime(System.currentTimeMillis());
        activeOverlays.put(tracker, overlay);
        overlayManager.add(overlay);
    }

    public void removeOverlay(StyleTracker tracker)
    {
        if (tracker == null)
        {
            return;
        }

        UnpottedReminderOverlay overlay = activeOverlays.remove(tracker);
        if (overlay != null)
        {
            overlayManager.remove(overlay);
        }
    }

    public void removeAllOverlays()
    {
        activeOverlays.values().forEach(overlayManager::remove);
        activeOverlays.clear();
    }

    public boolean isOverlayActive(StyleTracker tracker)
    {
        return activeOverlays.containsKey(tracker);
    }
}