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
	@ConfigItem(
			keyName = "enableMelee",
			name = "Alert for Melee",
			description = "Whether or not the warning should display when attacking with melee",
			position = 1
	)
	default boolean enableMelee()
	{
		return true;
	}

	@ConfigItem(
			keyName = "enableRanged",
			name = "Alert for Ranged",
			description = "Whether or not the warning should display when attacking with ranged",
			position = 2
	)
	default boolean enableRanged()
	{
		return true;
	}

	@ConfigItem(
			keyName = "enableMagic",
			name = "Alert for Magic",
			description = "Whether or not the warning should display when attacking with magic",
			position = 3
	)
	default boolean enableMagic()
	{
		return false;
	}


	@ConfigItem(
			keyName = "meleeBoostThreshold",
			name = "Melee Boost Threshold",
			description = "Don't alert when melee stats are boosted above this amount",
			position = 4
	)
	default int meleeBoostThreshold()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "rangedBoostThreshold",
			name = "Ranged Boost Threshold",
			description = "Don't alert when ranged stats are boosted above this amount",
			position = 5
	)
	default int rangedBoostThreshold()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "magicBoostThreshold",
			name = "Magic Boost Threshold",
			description = "Don't alert when magic stats are boosted above this amount",
			position = 6
	)
	default int magicBoostThreshold()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "timeout",
			name = "Timeout",
			description = "Stop showing warning after this long in seconds (unless triggered again)",
			position = 7
	)
	@Units(Units.SECONDS)
	default int timeout()
	{
		return 10;
	}

	@ConfigItem(
			keyName = "experienceThreshold",
			name = "Xp Threshold",
			description = "Don't alert when xp drop is more than this amount (0 to disable)",
			position = 8
	)
	default int experienceThreshold()
	{
		return 500;
	}

	@ConfigItem(
			keyName = "shouldFlash",
			name = "Flash overlay",
			description = "Whether or not the overlay should flash colors",
			position = 9
	)
	default boolean shouldFlash()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
			keyName = "flashColor1",
			name = "Flash color 1",
			description = "First color to flash between if 'Flash overlay' is on",
			position = 10
	)
	default Color flashColor1()
	{
		return new Color(0, 128, 255, 150);
	}

	@Alpha
	@ConfigItem(
			keyName = "flashColor2",
			name = "Flash color 2",
			description = "Second color to flash between if 'Flash overlay' is on",
			position = 11
	)
	default Color flashColor2()
	{
		return new Color(50, 50, 50, 150);
	}

	@ConfigItem(
			keyName = "showOverlay",
			name = "Show overlay",
			description = "Whether or not to show the overlay when warning you to pot",
			position = 12
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
			keyName = "shouldNotify",
			name = "Notify",
			description = "Whether or not to notify you when warning you to pot",
			position = 13
	)
	default boolean shouldNotify()
	{
		return false;
	}

	@Units(Units.SECONDS)
	@ConfigItem(
			keyName = "notifyCooldown",
			name = "Notify Cooldown",
			description = "Seconds until notifier can be triggered again (0 to disable)",
			position = 14
	)
	default int notifyCooldown()
	{
		return 5;
	}

	@ConfigItem(
			keyName = "useWhitelist",
			name = "Enable NPC whitelist",
			description = "Whether or not to only alert when attacking NPCs in the list below (comma-separated)",
			position = 15
	)
	default boolean useWhitelist()
	{
		return false;
	}

	@ConfigItem(
			keyName = "whitelist",
			name = "NPC Whitelist",
			description = "Only alert when attacking NPCs in this comma-separated list when toggled above (supports wildcards)",
			position = 16
	)
	default String whitelist()
	{
		return "";
	}

	@ConfigItem(
			keyName = "useBlacklist",
			name = "Enable NPC blacklist",
			description = "Whether or not to alert when attacking NPCs in the list below (comma-separated)",
			position = 17
	)
	default boolean useBlacklist()
	{
		return false;
	}

	@ConfigItem(
			keyName = "blacklist",
			name = "NPC Blacklist",
			description = "Don't alert when attacking NPCs in this comma-separated list when toggled above (supports wildcards)",
			position = 18
	)
	default String blacklist()
	{
		return "";
	}

	@ConfigItem(
			keyName = "alertWhenNotInteracting",
			name = "Alert when not targeting any NPC",
			description = "Whether or not to alert when you are not interacting with an NPC",
			position = 19
	)
	default boolean alertWhenNotInteracting()
	{
		return false;
	}

	@ConfigItem(
			keyName = "onlyInInstances",
			name = "Only alert in instances",
			description = "Whether or not to only alert when you are in an instanced area in-game",
			position = 20
	)
	default boolean onlyInInstances()
	{
		return false;
	}
}
