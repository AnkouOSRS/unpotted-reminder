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

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("unpottedreminder")
public interface UnpottedReminderConfig extends Config
{
    // Sections
    @ConfigSection(
        name = "Melee",
        description = "Melee Reminder Settings",
        position = 0,
        closedByDefault = true
    )
    String MELEE_SECTION = "meleeSection";

    @ConfigSection(
        name = "Ranged",
        description = "Ranged Reminder Settings",
        position = 1,
        closedByDefault = true
    )
    String RANGED_SECTION = "rangedSection";

    @ConfigSection(
        name = "Magic",
        description = "Magic Reminder Settings",
        position = 2,
        closedByDefault = true
    )
    String MAGIC_SECTION = "magicSection";


    // Melee configuration items
    @ConfigItem(
        keyName = "enableMelee",
        name = "Enable reminders for Melee",
        description = "Whether or not a warning should display when attacking with melee",
        position = 0,
        section = MELEE_SECTION
    )
    default boolean enableMelee()
    {
        return true;
    }

    @ConfigItem(
        keyName = "meleeAlertStyle",
        name = "Melee Alert Style",
        description = "Which attack style boost will alert you when using melee",
        position = 1,
        section = MELEE_SECTION
    )
    default MeleeAlertStyle meleeAlertStyle()
    {
        return MeleeAlertStyle.ATTACK_AND_STRENGTH;
    }

    @ConfigItem(
        keyName = "meleeBoostThreshold",
        name = "Melee Boost Threshold",
        description = "Don't alert when melee stats are boosted above this amount",
        position = 2,
        section = MELEE_SECTION
    )
    default int meleeBoostThreshold()
    {
        return 0;
    }

    @ConfigItem(
        keyName = "meleeTimeout",
        name = "Timeout",
        description = "Stop showing warning after this long in seconds (unless triggered again)",
        position = 3,
        section = MELEE_SECTION
    )
    @Units(Units.SECONDS)
    default int meleeTimeout()
    {
        return 10;
    }

    @ConfigItem(
        keyName = "meleeExperienceThreshold",
        name = "Xp Threshold",
        description = "Don't alert when xp drop is more than this amount (0 to disable)",
        position = 4,
        section = MELEE_SECTION
    )
    default int meleeExperienceThreshold()
    {
        return 500;
    }

    @ConfigItem(
        keyName = "meleeShowOverlay",
        name = "Show overlay",
        description = "Whether or not to show the overlay when warning you to pot",
        position = 5,
        section = MELEE_SECTION
    )
    default boolean meleeShowOverlay()
    {
        return true;
    }

    @ConfigItem(
        keyName = "meleeReminderStyle",
        name = "Reminder style",
        description = "Changes the style of the reminder box",
        position = 6,
        section = MELEE_SECTION
    )
    default UnpottedReminderStyle meleeReminderStyle()
    {
        return UnpottedReminderStyle.LONG_TEXT;
    }

    @ConfigItem(
        keyName = "meleeCustomText",
        name = "Custom Text",
        description = "Changes the text in the reminder box if the style is set to custom text",
        position = 7,
        section = MELEE_SECTION
    )
    default String meleeCustomText()
    {
        return "You need to to drink your melee potion!";
    }

    @ConfigItem(
        keyName = "meleeShouldFlash",
        name = "Flash the Reminder Box",
        description = "Makes the reminder box flash between the defined colors.",
        position = 8,
        section = MELEE_SECTION
    )
    default boolean meleeShouldFlash()
    {
        return false;
    }

    @Alpha
    @ConfigItem(
        keyName = "meleeColor",
        name = "Color",
        description = "The primary color of the reminder.",
        position = 9,
        section = MELEE_SECTION
    )
    default Color meleeColor()
    {
        return new Color(70, 3, 0, 150);
    }

    @Alpha
    @ConfigItem(
        keyName = "meleeFlashColor",
        name = "Flash Color",
        description = "The secondary color to flash to.",
        position = 10,
        section = MELEE_SECTION
    )
    default Color meleeFlashColor()
    {
        return new Color(70,  70, 70, 150);
    }

    @ConfigItem(
        keyName = "meleeShouldNotify",
        name = "Notify",
        description = "Whether or not to notify you when warning you to pot",
        position = 11,
        section = MELEE_SECTION
    )
    default Notification meleeShouldNotify()
    {
        return Notification.ON;
    }

    @Units(Units.SECONDS)
    @ConfigItem(
        keyName = "meleeNotifyCooldown",
        name = "Notify Cooldown",
        description = "Seconds until notifier can be triggered again (0 to disable)",
        position = 12,
        section = MELEE_SECTION
    )
    default int meleeNotifyCooldown()
    {
        return 30;
    }

    // Ranged Section

    @ConfigItem(
        keyName = "enableRanged",
        name = "Alert for Ranged",
        description = "Whether or not the warning should display when attacking with ranged",
        position = 0,
        section = RANGED_SECTION
    )
    default boolean enableRanged()
    {
        return true;
    }

    @ConfigItem(
        keyName = "rangedBoostThreshold",
        name = "Ranged Boost Threshold",
        description = "Don't alert when the Ranged stat is boosted above this amount",
        position = 1,
        section = RANGED_SECTION
    )
    default int rangedBoostThreshold()
    {
        return 0;
    }

    @ConfigItem(
        keyName = "rangedTimeout",
        name = "Timeout",
        description = "Stop showing warning after this long in seconds (unless triggered again)",
        position = 2,
        section = RANGED_SECTION
    )
    @Units(Units.SECONDS)
    default int rangedTimeout()
    {
        return 10;
    }

    @ConfigItem(
        keyName = "rangedExperienceThreshold",
        name = "Xp Threshold",
        description = "Don't alert when xp drop is more than this amount (0 to disable)",
        position = 3,
        section = RANGED_SECTION
    )
    default int rangedExperienceThreshold()
    {
        return 500;
    }

    @ConfigItem(
        keyName = "rangedShowOverlay",
        name = "Show overlay",
        description = "Whether or not to show the overlay when warning you to pot",
        position = 4,
        section = RANGED_SECTION
    )
    default boolean rangedShowOverlay()
    {
        return true;
    }

    @ConfigItem(
        keyName = "rangedReminderStyle",
        name = "Reminder style",
        description = "Changes the style of the reminder box",
        position = 5,
        section = RANGED_SECTION
    )
    default UnpottedReminderStyle rangedReminderStyle()
    {
        return UnpottedReminderStyle.LONG_TEXT;
    }

    @ConfigItem(
        keyName = "rangedCustomText",
        name = "Custom Text",
        description = "Changes the text in the reminder box if the style is set to custom text",
        position = 6,
        section = RANGED_SECTION
    )
    default String rangedCustomText()
    {
        return "You need to to drink your ranging potion!";
    }

    @ConfigItem(
      keyName = "rangedShouldFlash",
      name = "Flash the Reminder Box",
      description = "Makes the reminder box flash between the defined colors.",
      position = 7,
      section = RANGED_SECTION
    )
    default boolean rangedShouldFlash()
    {
        return false;
    }

    @Alpha
    @ConfigItem(
        keyName = "rangedColor",
        name = "Color",
        description = "The primary color of the reminder.",
        position = 8,
        section = RANGED_SECTION
    )
    default Color rangedColor()
    {
        return new Color(107, 219, 0, 150);
    }

    @Alpha
    @ConfigItem(
        keyName = "rangedFlashColor",
        name = "Flash Color",
        description = "The secondary color to flash to.",
        position = 9,
        section = RANGED_SECTION
    )
    default Color rangedFlashColor()
    {
        return new Color(70,  70, 70, 150);
    }

    @ConfigItem(
        keyName = "rangedShouldNotify",
        name = "Notify",
        description = "Whether or not to notify you when warning you to pot",
        position = 10,
        section = RANGED_SECTION
    )
    default Notification rangedShouldNotify()
    {
        return Notification.ON;
    }

    @Units(Units.SECONDS)
    @ConfigItem(
        keyName = "rangedNotifyCooldown",
        name = "Notify Cooldown",
        description = "Seconds until notifier can be triggered again (0 to disable)",
        position = 11,
        section = RANGED_SECTION
    )
    default int rangedNotifyCooldown()
    {
        return 30;
    }

    // Magic Section

    @ConfigItem(
        keyName = "enableMagic",
        name = "Alert for Magic",
        description = "Whether or not the warning should display when attacking with magic",
        position = 0,
        section = MAGIC_SECTION
    )
    default boolean enableMagic()
    {
        return false;
    }

    @ConfigItem(
        keyName = "magicBoostThreshold",
        name = "Magic Boost Threshold",
        description = "Don't alert when the Magic stat is boosted above this amount",
        position = 1,
        section = MAGIC_SECTION
    )
    default int magicBoostThreshold()
    {
        return 0;
    }

    @ConfigItem(
        keyName = "magicTimeout",
        name = "Timeout",
        description = "Stop showing warning after this long in seconds (unless triggered again)",
        position = 2,
        section = MAGIC_SECTION
    )
    @Units(Units.SECONDS)
    default int magicTimeout()
    {
        return 10;
    }

    @ConfigItem(
        keyName = "magicExperienceThreshold",
        name = "Xp Threshold",
        description = "Don't alert when xp drop is more than this amount (0 to disable)",
        position = 3,
        section = MAGIC_SECTION
    )
    default int magicExperienceThreshold()
    {
        return 500;
    }

    @ConfigItem(
        keyName = "magicShowOverlay",
        name = "Show overlay",
        description = "Whether or not to show the overlay when warning you to pot",
        position = 4,
        section = MAGIC_SECTION
    )
    default boolean magicShowOverlay()
    {
        return true;
    }

    @ConfigItem(
        keyName = "magicReminderStyle",
        name = "Reminder style",
        description = "Changes the style of the reminder box",
        position = 5,
        section = MAGIC_SECTION
    )
    default UnpottedReminderStyle magicReminderStyle()
    {
        return UnpottedReminderStyle.LONG_TEXT;
    }

    @ConfigItem(
        keyName = "magicCustomText",
        name = "Custom Text",
        description = "Changes the text in the reminder box if the style is set to custom text",
        position = 6,
        section = MAGIC_SECTION
    )
    default String magicCustomText()
    {
        return "You need to to drink your magic potion!";
    }

    @ConfigItem(
      keyName = "magicShouldFlash",
      name = "Flash the Reminder Box",
      description = "Makes the reminder box flash between the defined colors.",
      position = 7,
      section = MAGIC_SECTION
    )
    default boolean magicShouldFlash()
    {
        return false;
    }

    @Alpha
    @ConfigItem(
        keyName = "magicColor",
        name = "Color",
        description = "The primary color of the reminder.",
        position = 8,
        section = MAGIC_SECTION
    )
    default Color magicColor()
    {
        return new Color(27, 3, 163, 150);
    }

    @Alpha
    @ConfigItem(
        keyName = "magicFlashColor",
        name = "Flash Color",
        description = "The secondary color to flash to.",
        position = 9,
        section = MAGIC_SECTION
    )
    default Color magicFlashColor()
    {
        return new Color(70,  70, 70, 150);
    }

    @ConfigItem(
        keyName = "magicShouldNotify",
        name = "Notify",
        description = "Whether or not to notify you when warning you to pot",
        position = 10,
        section = MAGIC_SECTION
    )
    default Notification magicShouldNotify()
    {
        return Notification.ON;
    }

    @Units(Units.SECONDS)
    @ConfigItem(
        keyName = "magicNotifyCooldown",
        name = "Notify Cooldown",
        description = "Seconds until notifier can be triggered again (0 to disable)",
        position = 11,
        section = MAGIC_SECTION
    )
    default int magicNotifyCooldown()
    {
        return 30;
    }

    // Global values

    @ConfigItem(
            keyName = "useWhitelist",
            name = "Enable NPC whitelist",
            description = "Whether or not to only alert when attacking NPCs in the list below (comma-separated)",
            position = 3
    )
    default boolean useWhitelist()
    {
        return false;
    }

    @ConfigItem(
            keyName = "whitelist",
            name = "NPC Whitelist",
            description = "Only alert when attacking NPCs in this comma-separated list when toggled above (supports wildcards)",
            position = 4
    )
    default String whitelist()
    {
        return "";
    }

    @ConfigItem(
            keyName = "useBlacklist",
            name = "Enable NPC blacklist",
            description = "Whether or not to alert when attacking NPCs in the list below (comma-separated)",
            position = 5
    )
    default boolean useBlacklist()
    {
        return false;
    }

    @ConfigItem(
            keyName = "blacklist",
            name = "NPC Blacklist",
            description = "Don't alert when attacking NPCs in this comma-separated list when toggled above (supports wildcards)",
            position = 6
    )
    default String blacklist()
    {
        return "";
    }

    @ConfigItem(
            keyName = "alertWhenNotInteracting",
            name = "Alert when not targeting any NPC",
            description = "Whether or not to alert when you are not interacting with an NPC",
            position = 7
    )
    default boolean alertWhenNotInteracting()
    {
        return false;
    }

    @ConfigItem(
            keyName = "onlyInInstances",
            name = "Only alert in instances",
            description = "Whether or not to only alert when you are in an instanced area in-game",
            position = 8
    )
    default boolean onlyInInstances()
    {
        return false;
    }
}
