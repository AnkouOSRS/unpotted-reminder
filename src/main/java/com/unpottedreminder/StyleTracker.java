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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.Notification;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

import java.time.Instant;

import javax.inject.Inject;

@Slf4j
@Getter
public abstract class StyleTracker
{
    protected boolean active;
    protected boolean expired;
    protected int startTick;
    protected int finalTick;
    protected Instant lastNotify;

    @Inject
    protected Client client;

    @Inject
    protected UnpottedReminderPlugin plugin;

    @Inject
    protected UnpottedReminderConfig config;

    public StyleTracker()
    {
        reset();
    }

    // Tracker state getters and setters
    public void start()
    {
        active = true;
        expired = false;
        startTick = client.getTickCount();
        finalTick = Integer.MAX_VALUE;
    }

    public void start(int maxDuration)
    {
        active = true;
        expired = false;
        startTick = client.getTickCount();
        finalTick = startTick + maxDuration;
    }

    public void stop()
    {
        active = false;
        expired = true;
    }

    protected void reset()
    {
        active = false;
        expired = false;
        startTick = -1;
        finalTick = Integer.MAX_VALUE;
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
    }

    @Subscribe
    protected void onActorDeath(ActorDeath event)
    {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }

        if (event.getActor() instanceof Player) {
            Player actor = (Player) event.getActor();
            if (!actor.equals(player)) {
                return;
            }

            if (active) {
                stop();
            }
        }
    }

    @Subscribe
    protected void onVarbitChanged(VarbitChanged event) {
    }

    @Subscribe
    protected void onChatMessage(ChatMessage event) {
    }

    // Run this after the plugin's main game tick to reset a tracker
    @Subscribe(priority = -1)
    protected void onGameTick(GameTick ignored) {
        if (active && client.getTickCount() == finalTick) {
            stop();
        } else if (isExpired() || client.getTickCount() > finalTick) {
            reset();
        }
    }

    // Abstract methods - TODO decouple XP logic from trackers to support antifires/venoms
    /**
     * Checks if this attack style should be tracked based on config settings
     *
     * @return true if the attack style should be tracked, false otherwise
     */
    protected abstract boolean isStyleTracked();

    /**
     * Checks if a skill is associated with this tracker, if applicable
     *
     * @return the enum associated with the tracker
     */
    protected abstract boolean isSkill(Skill skill);

    /**
     * Checks if this attack style should show an overlay based on config settings
     * 
     * @return true if the attack style should have an overlay, false otherwise
     * 
     */
    protected abstract boolean getShowOverlay();

    /**
     * Checks if this attack style should show notifications based on config settings
     *
     * @return The custom notification for this attack style
     */
    protected abstract Notification getNotification();

    /**
     * Checks if this attack style should show notifications based on config settings
     *
     * @return The custom notification for this attack style
     */
    protected abstract int getNotificationCooldown();

    /**
     * Gets the custom message to display when the style boost expires
     *
     * @return The custom message for this attack style
     */
    protected abstract String getCustomMessage();

    /**
     * Gets the overlay to display when the style boost expires
     *
     * @return The custom overlay for this attack style
     */
    protected abstract UnpottedReminderOverlay getOverlay();

    /**
     * Gets the type of reminder style for a tracker
     *
     * @return The style to remind the user with
     */
    protected abstract UnpottedReminderStyle getReminderStyle();

    /**
     * Gets the XP threshold used for alerting on
     * 
     * @return the XP threshold, represented by an int
     */
    protected abstract int getExperienceThreshold();

    /**
     * Gets the boosted level threshold used for alerting on
     * 
     * @return the LVL threshold, represented by an int
     */
    protected abstract int getBoostThreshold();

    /**
     * Checks if the conditions for alerting are met for the tracker
     */

    protected abstract void alertIfShould(Item[] playerItems);

    /**
     * Returns the config key for the reminder style for this tracker.
     */
    public abstract String getReminderStyleConfigKey();
}