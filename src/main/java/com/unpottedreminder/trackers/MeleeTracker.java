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
import com.unpottedreminder.overlays.MeleeOverlay;

import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.client.config.Notification;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class MeleeTracker extends StyleTracker
{
    // TODO: swap away from deprecated ItemID type
    private static final Set<Integer> STRENGTH_POTIONS = new HashSet<>(Arrays.asList(
        ItemID.COMBAT_POTION4, ItemID.COMBAT_POTION3,ItemID.COMBAT_POTION2,ItemID.COMBAT_POTION1,
        ItemID.SUPER_COMBAT_POTION4, ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION2, ItemID.SUPER_COMBAT_POTION1,
        ItemID.DIVINE_SUPER_COMBAT_POTION4, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION1,
        ItemID.STRENGTH_POTION4, ItemID.STRENGTH_POTION3,ItemID.STRENGTH_POTION2,ItemID.STRENGTH_POTION1,
        ItemID.SUPER_STRENGTH4, ItemID.SUPER_STRENGTH3, ItemID.SUPER_STRENGTH2, ItemID.SUPER_STRENGTH1,
        ItemID.DIVINE_SUPER_STRENGTH_POTION4, ItemID.DIVINE_SUPER_STRENGTH_POTION3, ItemID.DIVINE_SUPER_STRENGTH_POTION2, ItemID.DIVINE_SUPER_STRENGTH_POTION1,
        ItemID.SMELLING_SALTS_2, ItemID.SMELLING_SALTS_1,
        ItemID.OVERLOAD_4, ItemID.OVERLOAD_3, ItemID.OVERLOAD_2, ItemID.OVERLOAD_1,
        ItemID.OVERLOAD_4_20996, ItemID.OVERLOAD_3_20995, ItemID.OVERLOAD_2_20994, ItemID.OVERLOAD_1_20993));

    private static final Set<Integer> ATTACK_POTIONS = new HashSet<>(Arrays.asList(
        ItemID.COMBAT_POTION4, ItemID.COMBAT_POTION3,ItemID.COMBAT_POTION2,ItemID.COMBAT_POTION1,
        ItemID.SUPER_COMBAT_POTION4, ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION2, ItemID.SUPER_COMBAT_POTION1,
        ItemID.DIVINE_SUPER_COMBAT_POTION4, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION1,
        ItemID.ATTACK_POTION4, ItemID.ATTACK_POTION3,ItemID.ATTACK_POTION2,ItemID.ATTACK_POTION1,
        ItemID.SUPER_ATTACK4, ItemID.SUPER_ATTACK3, ItemID.SUPER_ATTACK2, ItemID.SUPER_ATTACK1,
        ItemID.DIVINE_SUPER_ATTACK_POTION4, ItemID.DIVINE_SUPER_ATTACK_POTION3, ItemID.DIVINE_SUPER_ATTACK_POTION2, ItemID.DIVINE_SUPER_ATTACK_POTION1,
        ItemID.SMELLING_SALTS_2, ItemID.SMELLING_SALTS_1,
        ItemID.OVERLOAD_4, ItemID.OVERLOAD_3, ItemID.OVERLOAD_2, ItemID.OVERLOAD_1,
        ItemID.OVERLOAD_4_20996, ItemID.OVERLOAD_3_20995, ItemID.OVERLOAD_2_20994, ItemID.OVERLOAD_1_20993));

    private static final Set<Integer> MELEE_POTIONS = new HashSet<>(); // Build in the constructor so it can stay final

    @Inject
    protected MeleeOverlay overlay;

    @Inject
    public MeleeTracker()
    {
        super();
        MELEE_POTIONS.addAll(STRENGTH_POTIONS);
        MELEE_POTIONS.addAll(ATTACK_POTIONS);
    }

    @Override
    protected boolean isStyleTracked()
    {
        return config.enableMelee();
    }

    @Override
    protected boolean isSkill(Skill skill)
    {
        if (Skill.STRENGTH == skill)
        {
            return true;
        }
        return Skill.ATTACK == skill && (config.meleeAlertStyle() == MeleeAlertStyle.ATTACK_AND_STRENGTH);
    }

    @Override
    protected boolean getShowOverlay()
    {
        return config.meleeShowOverlay();
    }

    @Override
    protected Notification getNotification()
    {
        return config.meleeShouldNotify();
    }

    @Override
    protected int getNotificationCooldown()
    {
        return config.meleeNotifyCooldown();
    }

    @Override
    protected String getCustomMessage()
    {
        return config.meleeCustomText();
    }

    @Override
    protected UnpottedReminderOverlay getOverlay()
    {
        return overlay;
    }

    @Override
    protected UnpottedReminderStyle getReminderStyle()
    {
        return config.meleeReminderStyle();
    }

    @Override
    protected int getExperienceThreshold()
    {
        return config.meleeExperienceThreshold();
    }

    @Override
    protected int getBoostThreshold()
    {
        return config.meleeBoostThreshold();
    }

    @Override
    protected void alertIfShould(Item[] playerItems)
    {
        // TODO compare per specific style
        if (isStyleTracked() && Arrays.stream(playerItems).anyMatch(item -> MELEE_POTIONS.contains(item.getId())))
        {
            stop(); // flags the tracker as expired, triggering an overlay
        }
    }

    @Override
    public String getReminderStyleConfigKey()
    {
        return "meleeReminderStyle";
    }
}