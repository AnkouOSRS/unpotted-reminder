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

import com.google.inject.Provides;
import com.unpottedreminder.trackers.MagicTracker;
import com.unpottedreminder.trackers.MeleeTracker;
import com.unpottedreminder.trackers.RangedTracker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@PluginDescriptor(
    name = "Unpotted Reminder",
    description = "Reminds you you're unpotted in combat when you have a potion in your inventory",
    tags = {"combat", "potion", "reminder", "overlay", "pvm", "alert"}
)

@Slf4j
public class UnpottedReminderPlugin extends Plugin
{
    private final List<StyleTracker> styleTrackers = new ArrayList<>();

    @Inject
    protected MagicTracker magicTracker;

    @Inject
    protected MeleeTracker meleeTracker;

    @Inject
    protected RangedTracker rangedTracker;

    @Inject
    protected Client client;

    @Inject
    protected EventBus eventBus;

    @Inject
    protected UnpottedReminderConfig config;

    @Inject
    protected UnpottedReminderOverlayFactory overlayFactory;

    @Inject
    protected Notifier notifier;

    @Inject
    protected ClientThread clientThread;

    protected Item[] playerItems;

    private List<String> blacklisted = new ArrayList<>();
    private List<String> whitelisted = new ArrayList<>();
    
    private int potionLastDrankGameCycle;
    
    private static final int IMBUED_HEART_GRAPHIC = 1316;
    private static final int SATURATED_HEART_GRAPHIC = 2287;

    private static final List<Integer> DEFENSIVE_CASTING_WEAPONTYPES = List.of(18, 21);
    private static final List<Integer> RANGED_WEAPONTYPES = List.of(3, 5, 6, 7, 19);
    private static final List<Integer> POWERED_STAFF_WEAPONTYPES = List.of(23, 24);
    private static final Integer ATTACK_STYLE_DEFENSIVE = 3;

    private final List<Skill> trackedSkills = List.of(Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.RANGED, Skill.MAGIC);

    private final EnumMap<Skill, Integer> playerExperience = new EnumMap<>(Skill.class);
    private final EnumMap<Skill, Integer> playerBoosts = new EnumMap<>(Skill.class);

    @Provides
    UnpottedReminderConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(UnpottedReminderConfig.class);
    }

    @Override
    protected void startUp()
    {
        // TODO change the startup + refresh logic to only load enabled trackers
        styleTrackers.add(magicTracker);
        styleTrackers.add(meleeTracker);
        styleTrackers.add(rangedTracker);

        for (StyleTracker tracker : styleTrackers)
        {
            eventBus.register(tracker);
        }

        blacklisted = splitList(config.blacklist());
        whitelisted = splitList(config.whitelist());

        clientThread.invoke(() ->
        {
            if (client.getGameState() == GameState.LOGGED_IN)
            {
                for (Skill skill : trackedSkills)
                {
                    playerExperience.put(skill, client.getSkillExperience(skill));
                }

                ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
                if (inventory != null)
                {
                    playerItems = inventory.getItems();
                }
            }
        });
    }

    @Override
    protected void shutDown()
    {
        for (StyleTracker tracker : styleTrackers)
        {
            eventBus.unregister(tracker);
        }
        styleTrackers.clear();

        playerItems = null;
        playerExperience.clear();
        playerBoosts.clear();
        overlayFactory.removeAllOverlays();
    }

    @Subscribe
    protected void onConfigChanged(ConfigChanged event)
    {
        if (!event.getGroup().equals("unpottedreminder")) {
            return;
        }

        blacklisted = splitList(config.blacklist());
        whitelisted = splitList(config.whitelist());

        for (StyleTracker tracker : styleTrackers)
        {
            if (!event.getKey().equals(tracker.getReminderStyleConfigKey()))
            {
                continue;
            }

            boolean overlayWasActive = overlayFactory.isOverlayActive(tracker);

            overlayFactory.removeOverlay(tracker);

            // Don't recreate an overlay if it wasn't active
            if (!overlayWasActive) {
                continue;
            }
            overlayFactory.createOverlay(tracker);
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (event.getItemContainer() == client.getItemContainer(InventoryID.INVENTORY))
        {
            playerItems = event.getItemContainer().getItems();
        }
    }

    // TODO: pull logic into styletracker class and just share the inventory object
    // TODO: filter out stat regeneration; right now it alerts if a lowered stat restores below the threshold
    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        Skill skill = event.getSkill();

        if (Skill.DEFENCE.equals(skill))
            skill = getPrimarySkillForDefensive();

        for (StyleTracker tracker : styleTrackers)
        {
            if(!tracker.isStyleTracked())
            {
                continue;
            }
            if (tracker.isSkill(skill))
            {
                int xpDiff = event.getXp() - playerExperience.getOrDefault(skill, -1);
                int boost = event.getBoostedLevel() - event.getLevel();

                playerBoosts.put(skill, boost);
                playerExperience.put(skill, event.getXp());

                if (tracker.getExperienceThreshold() > 0 && xpDiff > tracker.getExperienceThreshold())
                {
                    continue;
                }
                if (playerBoosts.getOrDefault(skill, -1) > tracker.getBoostThreshold())
                {
                    continue;
                }
                // Placeholder fix to address salts triggering alarms
                if (client.getGameCycle() == potionLastDrankGameCycle)
                {
                    continue;
                }

                // Maintain interactions + inventory globally to reduce footprint
                if (checkGlobalAlertRestrictions())
                {
                    tracker.alertIfShould(playerItems);
                }
            }
        }
    }

    /*
     * Track tracker expirations
     * 
     * Mostly for implementing antifires/antipoisons/overload/salts
     * 
     * 
     * Overlays manage their own removal timeouts
     */
    @Subscribe
    public void onGameTick(GameTick event)
    {
        for (StyleTracker tracker : styleTrackers)
        {
            if (!tracker.isStyleTracked())
            {
                continue;
            }

            // Should alert when a tracker expires
            if (tracker.isExpired())
            {
                if (tracker.getShowOverlay())
                {
                    overlayFactory.createOverlay(tracker);
                }
                if (tracker.getNotification().isEnabled() && (null == tracker.lastNotify || Instant.now().minusSeconds(tracker.getNotificationCooldown()).isAfter(tracker.lastNotify)))
                {
                    notifier.notify(tracker.getNotification(), "Potion reminder:" + tracker.getCustomMessage());
                    tracker.lastNotify = Instant.now();
                }
            }

            // Remove overlays if their trackers restarted
            if (tracker.isActive())
            {
                overlayFactory.removeOverlay(tracker);
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        String msg = Text.removeTags(event.getMessage());
        // Clear overlays when taking a potion
        // Naive way to clear overlays TODO: use varbits to monitor this within trackers
        if (msg.contains("You drink some of your") || msg.contains("You crush the salts"))
        {
            potionLastDrankGameCycle = client.getGameCycle();
            for (StyleTracker tracker : styleTrackers)
            {
                if (tracker.isStyleTracked())
                {
                    overlayFactory.removeOverlay(tracker);
                    tracker.reset();
                }
            }
        }
    }

    private boolean checkGlobalAlertRestrictions()
    {
        if (client.getGameState() != GameState.LOGGED_IN)
            return false;

        if (!client.isInInstancedRegion() && config.onlyInInstances())
            return false;

        if (!interactingShouldAlert())
            return false;

        return true;
    }

    private Skill getPrimarySkillForDefensive()
    {
        if (usingDefensiveMagic())
            return Skill.MAGIC;
        if (usingDefensiveRanged())
            return Skill.RANGED;
        return Skill.STRENGTH;
    }

    private boolean interactingShouldAlert()
    {
        String interactingName = client.getLocalPlayer().getInteracting() != null
                ? client.getLocalPlayer().getInteracting().getName() : null;

        if (null == interactingName)
        {
            return config.alertWhenNotInteracting();
        }
        else
        {
            boolean isBlackListed = config.useBlacklist() && blacklisted.stream().anyMatch(npcName -> WildcardMatcher.matches(npcName, interactingName));
            boolean isWhitelisted = !config.useWhitelist() || whitelisted.stream().anyMatch(npcName -> WildcardMatcher.matches(npcName, interactingName));

            return isWhitelisted && !isBlackListed;
        }
    }

    private boolean usingDefensiveMagic()
    {
        int defensiveCasting = client.getVarbitValue(Varbits.DEFENSIVE_CASTING_MODE);
        int currentAttackStyleVarbit = client.getVarpValue(VarPlayer.ATTACK_STYLE);
        int equippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);

        if (POWERED_STAFF_WEAPONTYPES.contains(equippedWeaponTypeVarbit) && ATTACK_STYLE_DEFENSIVE == currentAttackStyleVarbit)
        {
            return true;
        }

        return (DEFENSIVE_CASTING_WEAPONTYPES.contains(equippedWeaponTypeVarbit) && defensiveCasting == 1);
    }

    private boolean usingDefensiveRanged()
    {
        int currentAttackStyleVarbit = client.getVarpValue(VarPlayer.ATTACK_STYLE);
        int equippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);

        return (RANGED_WEAPONTYPES.contains(equippedWeaponTypeVarbit) && ATTACK_STYLE_DEFENSIVE == currentAttackStyleVarbit);
    }

    private List<String> splitList(String list)
    {
        return Arrays.stream(list.split(",")).map(String::trim).collect(Collectors.toList());
    }
}
