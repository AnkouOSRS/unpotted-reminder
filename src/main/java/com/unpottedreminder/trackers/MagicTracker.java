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
package com.unpottedreminder.trackers;

import com.google.common.eventbus.Subscribe;
import com.unpottedreminder.*;
import com.unpottedreminder.overlays.MagicOverlay;

import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GraphicChanged;
import net.runelite.client.config.Notification;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

public class MagicTracker extends StyleTracker
{
    @Inject
    protected MagicOverlay overlay;

    @Inject
    public MagicTracker()
    {
        super();
    }

    // TODO: swap away from deprecated ItemID type
    private static final Set<Integer> MAGIC_POTIONS = new HashSet<>(Arrays.asList(
        ItemID.MAGIC_POTION4, ItemID.MAGIC_POTION3, ItemID.MAGIC_POTION4,
        ItemID.BATTLEMAGE_POTION4, ItemID.BATTLEMAGE_POTION3, ItemID.BATTLEMAGE_POTION2, ItemID.BATTLEMAGE_POTION1,
        ItemID.DIVINE_MAGIC_POTION4, ItemID.DIVINE_MAGIC_POTION3, ItemID.DIVINE_MAGIC_POTION2, ItemID.DIVINE_MAGIC_POTION1,
        ItemID.SMELLING_SALTS_2, ItemID.SMELLING_SALTS_1,
        ItemID.OVERLOAD_4, ItemID.OVERLOAD_3, ItemID.OVERLOAD_2, ItemID.OVERLOAD_1,
        ItemID.OVERLOAD_4_20996, ItemID.OVERLOAD_3_20995, ItemID.OVERLOAD_2_20994, ItemID.OVERLOAD_1_20993));
    private static final int IMBUED_HEART_GRAPHIC = 1316;
    private static final int SATURATED_HEART_GRAPHIC = 2287;

    private boolean isHeartAvailable()
    {
        return client.getVarbitValue(Varbits.IMBUED_HEART_COOLDOWN) == 0;
    }

    protected void onGraphicChanged(GraphicChanged event)
    {
        if ((event.getActor().hasSpotAnim(IMBUED_HEART_GRAPHIC) ||  event.getActor().hasSpotAnim(SATURATED_HEART_GRAPHIC))
                && Objects.equals(event.getActor().getName(), client.getLocalPlayer().getName()))
        {
            start();
        }
    }

    @Override
    protected void onChatMessage(ChatMessage event)
    {

    }

    @Override
    protected boolean isStyleTracked()
    {
        return config.enableMagic();
    }

    @Override
    protected boolean isSkill(Skill skill)
    {
        return Skill.MAGIC == skill;
    }

    @Override
    protected boolean getShowOverlay()
    {
        return config.magicShowOverlay();
    }

    @Override
    protected Notification getNotification()
    {
        return config.magicShouldNotify();
    }

    @Override
    protected int getNotificationCooldown()
    {
        return config.magicNotifyCooldown();
    }

    @Override
    protected String getCustomMessage()
    {
        return config.magicCustomText();
    }

    @Override
    protected UnpottedReminderOverlay getOverlay()
    {
        return overlay;
    }

    @Override
    protected UnpottedReminderStyle getReminderStyle()
    {
        return config.magicReminderStyle();
    }

    @Override
    protected int getExperienceThreshold()
    {
        return config.magicExperienceThreshold();
    }

    @Override
    protected int getBoostThreshold()
    {
        return config.magicBoostThreshold();
    }

    @Override
    protected void alertIfShould(Item[] playerItems)
    {
        if (isStyleTracked() && Arrays.stream(playerItems).anyMatch(item -> 
            (((item.getId() == ItemID.IMBUED_HEART || item.getId() == ItemID.SATURATED_HEART) && isHeartAvailable())
                || MAGIC_POTIONS.contains(item.getId()))))
        {
            stop(); // flags the tracker as expired, triggering an overlay
        }
    }

    @Override
    public String getReminderStyleConfigKey() {
        return "magicReminderStyle";
    }
}