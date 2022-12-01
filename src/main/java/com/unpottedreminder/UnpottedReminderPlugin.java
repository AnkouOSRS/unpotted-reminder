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

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
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
	description = "Reminds you you're unpotted in combat when you have one in your inventory",
	tags = {"combat", "potion", "reminder", "overlay", "pvm", "alert"}
)
@Slf4j
public class UnpottedReminderPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private UnpottedReminderConfig config;

	@Inject
	private UnpottedReminderOverlay overlay;

	@Inject
	private Notifier notifier;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	private Item[] playerItems;

	private List<String> blacklisted = new ArrayList<>();
	private List<String> whitelisted = new ArrayList<>();

	private Instant alertStart;

	private static final List<Integer> MELEE_POTIONS = ImmutableList.of(
			ItemID.COMBAT_POTION4, ItemID.COMBAT_POTION3,ItemID.COMBAT_POTION2,ItemID.COMBAT_POTION1,
			ItemID.SUPER_COMBAT_POTION4, ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION2, ItemID.SUPER_COMBAT_POTION1,
			ItemID.DIVINE_SUPER_COMBAT_POTION4, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION1,
			ItemID.ATTACK_POTION4, ItemID.ATTACK_POTION3,ItemID.ATTACK_POTION2,ItemID.ATTACK_POTION1,
			ItemID.SUPER_ATTACK4, ItemID.SUPER_ATTACK3, ItemID.SUPER_ATTACK2, ItemID.SUPER_ATTACK1,
			ItemID.DIVINE_SUPER_ATTACK_POTION4, ItemID.DIVINE_SUPER_ATTACK_POTION3, ItemID.DIVINE_SUPER_ATTACK_POTION2, ItemID.DIVINE_SUPER_ATTACK_POTION1,
			ItemID.STRENGTH_POTION4, ItemID.STRENGTH_POTION3,ItemID.STRENGTH_POTION2,ItemID.STRENGTH_POTION1,
			ItemID.SUPER_STRENGTH4, ItemID.SUPER_STRENGTH3, ItemID.SUPER_STRENGTH2, ItemID.SUPER_STRENGTH1,
			ItemID.DIVINE_SUPER_STRENGTH_POTION4, ItemID.DIVINE_SUPER_STRENGTH_POTION3, ItemID.DIVINE_SUPER_STRENGTH_POTION2, ItemID.DIVINE_SUPER_STRENGTH_POTION1);

	private static final List<Integer> RANGED_POTIONS = ImmutableList.of(
			ItemID.RANGING_POTION4, ItemID.RANGING_POTION3, ItemID.RANGING_POTION2, ItemID.RANGING_POTION1,
			ItemID.DIVINE_RANGING_POTION4, ItemID.DIVINE_RANGING_POTION3, ItemID.DIVINE_RANGING_POTION2, ItemID.DIVINE_RANGING_POTION1,
			ItemID.BASTION_POTION4, ItemID.BASTION_POTION3, ItemID.BASTION_POTION2, ItemID.BASTION_POTION1,
			ItemID.DIVINE_BASTION_POTION4, ItemID.DIVINE_BASTION_POTION3, ItemID.DIVINE_BASTION_POTION2, ItemID.DIVINE_BASTION_POTION1);

	private static final List<Integer> MAGIC_POTIONS = ImmutableList.of(
			ItemID.MAGIC_POTION4, ItemID.MAGIC_POTION3, ItemID.MAGIC_POTION4,
			ItemID.BATTLEMAGE_POTION4, ItemID.BATTLEMAGE_POTION3, ItemID.BATTLEMAGE_POTION2, ItemID.BATTLEMAGE_POTION1, ItemID.IMBUED_HEART);
	private static final List<Integer> OVERLOADS = ImmutableList.of(
			ItemID.SMELLING_SALTS_2, ItemID.SMELLING_SALTS_1,
			ItemID.OVERLOAD_4, ItemID.OVERLOAD_3, ItemID.OVERLOAD_2, ItemID.OVERLOAD_1,
			ItemID.OVERLOAD_4_20996, ItemID.OVERLOAD_3_20995, ItemID.OVERLOAD_2_20994, ItemID.OVERLOAD_1_20993);

	private static final List<Integer> DEFENSIVE_CASTING_WEAPONTYPES = ImmutableList.of(18, 21);
	private static final List<Integer> RANGED_WEAPONTYPES = ImmutableList.of(3, 5, 6, 7, 19);
	private static final Integer POWERED_STAFF_WEAPONTYPE = 23;
	private static final Integer ATTACK_STYLE_DEFENSIVE = 3;

	private static final List<Skill> MELEE_SKILLS = ImmutableList.of(Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE);
	private final List<Skill> trackedSkills = ImmutableList.of(Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.RANGED, Skill.MAGIC);

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
		playerItems = null;
		alertStart = null;
		playerExperience.clear();
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("unpottedreminder"))
		{
			blacklisted = splitList(config.blacklist());
			whitelisted = splitList(config.whitelist());

			if (!config.showOverlay())
				overlayManager.remove(overlay);
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

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		Skill skill = event.getSkill();

		if (!trackedSkills.contains(skill))
			return;

		int xpDiff = event.getXp() - playerExperience.getOrDefault(skill, -1);
		int boost = event.getBoostedLevel() - event.getLevel();

		playerBoosts.put(skill, boost);
		playerExperience.put(skill, event.getXp());

		if (config.experienceThreshold() > 0 && xpDiff > config.experienceThreshold())
			return;

		if (shouldAlert(skill))
		{
			alertStart = Instant.now();

			if (config.showOverlay())
				overlayManager.add(overlay);

			if (config.shouldNotify())
				notifier.notify("You need to drink your boost potion!");
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (null == alertStart)
			return;

		if (Instant.now().minusSeconds(config.timeout()).isAfter(alertStart))
		{
			overlayManager.remove(overlay);
			alertStart = null;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		String msg = Text.removeTags(event.getMessage());

		if (msg.contains("You drink some of your"))
		{
			overlayManager.remove(overlay);
			alertStart = null;
		}
	}

	private boolean shouldAlert(Skill skill)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
			return false;

		if (config.enableMagic() && usingDefensiveMagic())
			return false;

		if (config.enableRanged() && usingDefensiveRanged())
			return false;

		if (!client.isInInstancedRegion() && config.onlyInInstances())
			return false;

		if (!interactingShouldAlert())
			return false;

		if (!hasBoostPotionInInventory(skill))
			return false;

		return isBoostBelowThreshold(skill);
	}

	private boolean interactingShouldAlert() {
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

	private boolean hasBoostPotionInInventory(Skill skill)
	{
		if (MELEE_SKILLS.contains(skill) && config.enableMelee()
				&& Arrays.stream(playerItems).anyMatch(item -> MELEE_POTIONS.contains(item.getId())))
			return true;

		if (Skill.RANGED == skill && config.enableRanged()
				&& Arrays.stream(playerItems).anyMatch(item -> RANGED_POTIONS.contains(item.getId())))
			return true;

		if (Skill.MAGIC == skill && config.enableMagic()
				&& Arrays.stream(playerItems).anyMatch(item -> MAGIC_POTIONS.contains(item.getId())))
			return true;

		return Arrays.stream(playerItems).anyMatch(item -> OVERLOADS.contains(item.getId()));
	}

	private boolean isBoostBelowThreshold(Skill skill)
	{
		if (MELEE_SKILLS.contains(skill) && config.enableMelee()
				&& playerBoosts.getOrDefault(skill, -1) <= config.meleeBoostThreshold())
			return true;

		if (Skill.RANGED == skill && config.enableRanged()
				&& playerBoosts.getOrDefault(Skill.RANGED, -1) <= config.rangedBoostThreshold())
			return true;

		return (Skill.MAGIC == skill && config.enableMagic()
				&& playerBoosts.getOrDefault(Skill.MAGIC, -1) <= config.magicBoostThreshold());
	}

	private boolean usingDefensiveMagic()
	{
		int defensiveCasting = client.getVarbitValue(Varbits.DEFENSIVE_CASTING_MODE);
		int currentAttackStyleVarbit = client.getVarpValue(VarPlayer.ATTACK_STYLE);
		int equippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);

		if (POWERED_STAFF_WEAPONTYPE == equippedWeaponTypeVarbit && ATTACK_STYLE_DEFENSIVE == currentAttackStyleVarbit)
			return true;

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
