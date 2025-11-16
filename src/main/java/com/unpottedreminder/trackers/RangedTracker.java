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

import com.unpottedreminder.*;
import com.unpottedreminder.overlays.RangedOverlay;

import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.client.config.Notification;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class RangedTracker extends StyleTracker
{
    // TODO: swap away from deprecated ItemID type
    private static final Set<Integer> RANGED_POTIONS = new HashSet<>(Arrays.asList(
        ItemID.RANGING_POTION4, ItemID.RANGING_POTION3, ItemID.RANGING_POTION2, ItemID.RANGING_POTION1,
        ItemID.DIVINE_RANGING_POTION4, ItemID.DIVINE_RANGING_POTION3, ItemID.DIVINE_RANGING_POTION2, ItemID.DIVINE_RANGING_POTION1,
        ItemID.BASTION_POTION4, ItemID.BASTION_POTION3, ItemID.BASTION_POTION2, ItemID.BASTION_POTION1,
        ItemID.DIVINE_BASTION_POTION4, ItemID.DIVINE_BASTION_POTION3, ItemID.DIVINE_BASTION_POTION2, ItemID.DIVINE_BASTION_POTION1,
        ItemID.ARMADYL_BREW4, ItemID.ARMADYL_BREW3, ItemID.ARMADYL_BREW2, ItemID.ARMADYL_BREW1,
        ItemID.SMELLING_SALTS_2, ItemID.SMELLING_SALTS_1,
        ItemID.OVERLOAD_4, ItemID.OVERLOAD_3, ItemID.OVERLOAD_2, ItemID.OVERLOAD_1,
        ItemID.OVERLOAD_4_20996, ItemID.OVERLOAD_3_20995, ItemID.OVERLOAD_2_20994, ItemID.OVERLOAD_1_20993));


    @Inject
    protected RangedOverlay overlay;

    @Inject
    public RangedTracker()
    {
        super();
    }

    @Override
    protected boolean isStyleTracked()
    {
        return config.enableRanged();
    }

    @Override
    protected boolean isSkill(Skill skill)
    {
        return Skill.RANGED == skill;
    }

    @Override
    protected boolean getShowOverlay()
    {
        return config.rangedShowOverlay();
    }

    @Override
    protected Notification getNotification()
    {
        return config.rangedShouldNotify();
    }

    @Override
    protected int getNotificationCooldown()
    {
        return config.rangedNotifyCooldown();
    }

    @Override
    protected String getCustomMessage()
    {
        return config.rangedCustomText();
    }

    @Override
    protected UnpottedReminderOverlay getOverlay()
    {
        return overlay;
    }

    @Override
    protected UnpottedReminderStyle getReminderStyle()
    {
        return config.rangedReminderStyle();
    }

    @Override
    protected int getExperienceThreshold()
    {
        return config.rangedExperienceThreshold();
    }

    @Override
    protected int getBoostThreshold()
    {
        return config.rangedBoostThreshold();
    }

    @Override
    protected void alertIfShould(Item[] playerItems)
    {
        if (isStyleTracked() && Arrays.stream(playerItems).anyMatch(item -> RANGED_POTIONS.contains(item.getId())))
        {
            stop(); // flags the tracker as expired, triggering an overlay
        }
    }

    @Override
    public String getReminderStyleConfigKey()
    {
        return "rangedReminderStyle";
    }
}