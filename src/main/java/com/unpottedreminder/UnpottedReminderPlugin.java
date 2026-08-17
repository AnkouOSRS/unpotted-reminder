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
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@PluginDescriptor(
	name = "Unpotted Reminder",
	description = "Reminds you you're unpotted in combat when you have one in your inventory",
	tags = {"combat", "potion", "reminder", "overlay", "infobox", "pvm", "alert"}
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

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	private UnpottedReminderInfoBox infoBox;

	private Item[] playerItems;

	private List<String> blacklisted = new ArrayList<>();
	private List<String> whitelisted = new ArrayList<>();

	private Instant alertStart;
	private Instant lastNotify;
	private int potionLastDrankGameCycle;
	
	static final String DEFAULT_ALERT_MESSAGE = "Drink a boost potion!";

	private static final int IMBUED_HEART_GRAPHIC = 1316;
	private static final int SATURATED_HEART_GRAPHIC = 2287;

	private static final List<Integer> DEFENSIVE_CASTING_WEAPONTYPES = List.of(18, 21);
	private static final List<Integer> RANGED_WEAPONTYPES = List.of(3, 5, 6, 7, 19);
	private static final List<Integer> POWERED_STAFF_WEAPONTYPES = List.of(23, 24);
	private static final Integer ATTACK_STYLE_DEFENSIVE = 3;

	private static final List<Skill> MELEE_SKILLS = List.of(Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE);
	private final List<Skill> trackedSkills = List.of(Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.RANGED, Skill.MAGIC);

	public static final List<Integer> MELEE_POTIONS = List.of(
			ItemID._4DOSECOMBAT, ItemID._3DOSECOMBAT, ItemID._2DOSECOMBAT, ItemID._1DOSECOMBAT,
			ItemID._4DOSE2COMBAT, ItemID._3DOSE2COMBAT, ItemID._2DOSE2COMBAT, ItemID._1DOSE2COMBAT,
			ItemID._4DOSEDIVINECOMBAT, ItemID._3DOSEDIVINECOMBAT, ItemID._2DOSEDIVINECOMBAT, ItemID._1DOSEDIVINECOMBAT,
			ItemID._4DOSE1ATTACK, ItemID._3DOSE1ATTACK, ItemID._2DOSE1ATTACK, ItemID._1DOSE1ATTACK,
			ItemID._4DOSE2ATTACK, ItemID._3DOSE2ATTACK, ItemID._2DOSE2ATTACK, ItemID._1DOSE2ATTACK,
			ItemID._4DOSEDIVINEATTACK, ItemID._3DOSEDIVINEATTACK, ItemID._2DOSEDIVINEATTACK, ItemID._1DOSEDIVINEATTACK,
			ItemID.STRENGTH4, ItemID._3DOSE1STRENGTH, ItemID._2DOSE1STRENGTH, ItemID._1DOSE1STRENGTH,
			ItemID._4DOSE2STRENGTH, ItemID._3DOSE2STRENGTH, ItemID._2DOSE2STRENGTH, ItemID._1DOSE2STRENGTH,
			ItemID._4DOSEDIVINESTRENGTH, ItemID._3DOSEDIVINESTRENGTH, ItemID._2DOSEDIVINESTRENGTH, ItemID._1DOSEDIVINESTRENGTH,
			ItemID._4DOSEMOONLIGHTPOTION, ItemID._3DOSEMOONLIGHTPOTION, ItemID._2DOSEMOONLIGHTPOTION, ItemID._1DOSEMOONLIGHTPOTION);

	public static final List<Integer> ATTACK_POTIONS = List.of(
			ItemID._4DOSECOMBAT, ItemID._3DOSECOMBAT, ItemID._2DOSECOMBAT, ItemID._1DOSECOMBAT,
			ItemID._4DOSE2COMBAT, ItemID._3DOSE2COMBAT, ItemID._2DOSE2COMBAT, ItemID._1DOSE2COMBAT,
			ItemID._4DOSEDIVINECOMBAT, ItemID._3DOSEDIVINECOMBAT, ItemID._2DOSEDIVINECOMBAT, ItemID._1DOSEDIVINECOMBAT,
			ItemID._4DOSE1ATTACK, ItemID._3DOSE1ATTACK, ItemID._2DOSE1ATTACK, ItemID._1DOSE1ATTACK,
			ItemID._4DOSE2ATTACK, ItemID._3DOSE2ATTACK, ItemID._2DOSE2ATTACK, ItemID._1DOSE2ATTACK,
			ItemID._4DOSEDIVINEATTACK, ItemID._3DOSEDIVINEATTACK, ItemID._2DOSEDIVINEATTACK, ItemID._1DOSEDIVINEATTACK,
			ItemID._4DOSEMOONLIGHTPOTION, ItemID._3DOSEMOONLIGHTPOTION, ItemID._2DOSEMOONLIGHTPOTION, ItemID._1DOSEMOONLIGHTPOTION);

	public static final List<Integer> STRENGTH_POTIONS = List.of(
			ItemID._4DOSECOMBAT, ItemID._3DOSECOMBAT, ItemID._2DOSECOMBAT, ItemID._1DOSECOMBAT,
			ItemID._4DOSE2COMBAT, ItemID._3DOSE2COMBAT, ItemID._2DOSE2COMBAT, ItemID._1DOSE2COMBAT,
			ItemID._4DOSEDIVINECOMBAT, ItemID._3DOSEDIVINECOMBAT, ItemID._2DOSEDIVINECOMBAT, ItemID._1DOSEDIVINECOMBAT,
			ItemID.STRENGTH4, ItemID._3DOSE1STRENGTH, ItemID._2DOSE1STRENGTH, ItemID._1DOSE1STRENGTH,
			ItemID._4DOSE2STRENGTH, ItemID._3DOSE2STRENGTH, ItemID._2DOSE2STRENGTH, ItemID._1DOSE2STRENGTH,
			ItemID._4DOSEDIVINESTRENGTH, ItemID._3DOSEDIVINESTRENGTH, ItemID._2DOSEDIVINESTRENGTH, ItemID._1DOSEDIVINESTRENGTH,
			ItemID._4DOSEMOONLIGHTPOTION, ItemID._3DOSEMOONLIGHTPOTION, ItemID._2DOSEMOONLIGHTPOTION, ItemID._1DOSEMOONLIGHTPOTION);

	public static final List<Integer> RANGED_POTIONS = List.of(
			ItemID._4DOSERANGERSPOTION, ItemID._3DOSERANGERSPOTION, ItemID._2DOSERANGERSPOTION, ItemID._1DOSERANGERSPOTION,
			ItemID._4DOSEDIVINERANGE, ItemID._3DOSEDIVINERANGE, ItemID._2DOSEDIVINERANGE, ItemID._1DOSEDIVINERANGE,
			ItemID._4DOSEBASTION, ItemID._3DOSEBASTION, ItemID._2DOSEBASTION, ItemID._1DOSEBASTION,
			ItemID._4DOSEDIVINEBASTION, ItemID._3DOSEDIVINEBASTION, ItemID._2DOSEDIVINEBASTION, ItemID._1DOSEDIVINEBASTION,
			ItemID._4DOSEARMADYLBREW, ItemID._3DOSEARMADYLBREW, ItemID._2DOSEARMADYLBREW, ItemID._1DOSEARMADYLBREW);

	public static final List<Integer> MAGIC_POTIONS = List.of(
			ItemID._4DOSE1MAGIC, ItemID._3DOSE1MAGIC, ItemID._2DOSE1MAGIC, ItemID._1DOSE1MAGIC,
			ItemID._4DOSEBATTLEMAGE, ItemID._3DOSEBATTLEMAGE, ItemID._2DOSEBATTLEMAGE, ItemID._1DOSEBATTLEMAGE,
			ItemID._4DOSEDIVINEMAGIC, ItemID._3DOSEDIVINEMAGIC, ItemID._2DOSEDIVINEMAGIC, ItemID._1DOSEDIVINEMAGIC,
			ItemID._4DOSEDIVINEBATTLEMAGE, ItemID._3DOSEDIVINEBATTLEMAGE, ItemID._2DOSEDIVINEBATTLEMAGE, ItemID._1DOSEDIVINEBATTLEMAGE,
			ItemID._4DOSEANCIENTBREW, ItemID._3DOSEANCIENTBREW, ItemID._2DOSEANCIENTBREW, ItemID._1DOSEANCIENTBREW,
			ItemID._4DOSEFORGOTTENBREW, ItemID._3DOSEFORGOTTENBREW, ItemID._2DOSEFORGOTTENBREW, ItemID._1DOSEFORGOTTENBREW);

	public static final List<Integer> OVERLOADS = List.of(
			ItemID.TOA_SUPPLY_STATS_2, ItemID.TOA_SUPPLY_STATS_1,
			ItemID.NZONE4DOSEOVERLOADPOTION, ItemID.NZONE3DOSEOVERLOADPOTION, ItemID.NZONE2DOSEOVERLOADPOTION, ItemID.NZONE1DOSEOVERLOADPOTION,
			ItemID.RAIDS_VIAL_OVERLOAD_STRONG_4, ItemID.RAIDS_VIAL_OVERLOAD_STRONG_3, ItemID.RAIDS_VIAL_OVERLOAD_STRONG_2, ItemID.RAIDS_VIAL_OVERLOAD_STRONG_1);

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

		infoBox = new UnpottedReminderInfoBox(itemManager.getImage(ItemID.VIAL_EMPTY), this, config);

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
		infoBoxManager.removeInfoBox(infoBox);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("unpottedreminder"))
		{
			blacklisted = splitList(config.blacklist());
			whitelisted = splitList(config.whitelist());

			if (!config.showOverlay())
			{
				overlayManager.remove(overlay);
				infoBoxManager.removeInfoBox(infoBox);
			}
			else if (config.alertDisplayMode() == AlertDisplayMode.INFOBOX)
			{
				overlayManager.remove(overlay);
			}
			else
			{
				infoBoxManager.removeInfoBox(infoBox);
			}
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

		if (client.getGameCycle() == potionLastDrankGameCycle)
			return;

		if (shouldAlert(skill))
		{
			alert();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (null != alertStart && Instant.now().minusSeconds(config.timeout()).isAfter(alertStart))
		{
			clearAlert();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		String msg = Text.removeTags(event.getMessage());

		if (msg.contains("You drink some of your") || msg.contains("You crush the salts"))
		{
			potionLastDrankGameCycle = client.getGameCycle();
			clearAlert();
		}
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event)
	{
		if ((event.getActor().hasSpotAnim(IMBUED_HEART_GRAPHIC) ||  event.getActor().hasSpotAnim(SATURATED_HEART_GRAPHIC))
				&& Objects.equals(event.getActor().getName(), client.getLocalPlayer().getName()))
		{
			clearAlert();
		}
	}

	private void alert()
	{
		boolean shouldNotify = (config.shouldNotify()
				&& (null == lastNotify || Instant.now().minusSeconds(config.notifyCooldown()).isAfter(lastNotify)));

		alertStart = Instant.now();

		if (config.showOverlay())
		{
			if (config.alertDisplayMode() == AlertDisplayMode.INFOBOX)
			{
				if (!infoBoxManager.getInfoBoxes().contains(infoBox))
				{
					infoBoxManager.addInfoBox(infoBox);
				}
			}
			else
			{
				overlayManager.add(overlay);
			}
		}

		if (shouldNotify)
		{
			notifier.notify(resolveAlertMessage(config));
			lastNotify = Instant.now();
		}
	}

	static String resolveAlertMessage(UnpottedReminderConfig config)
	{
		String message = config.alertMessage();
		return message == null || message.trim().isEmpty() ? DEFAULT_ALERT_MESSAGE : message;
	}

	private void clearAlert()
	{
		overlayManager.remove(overlay);
		infoBoxManager.removeInfoBox(infoBox);
		alertStart = null;
	}

	private boolean shouldAlert(Skill skill)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
			return false;

		if (Skill.DEFENCE.equals(skill))
			skill = getPrimarySkillForDefensive();

		if (isSkillDisabled(skill))
			return false;

		if (!client.isInInstancedRegion() && config.onlyInInstances())
			return false;

		if (!interactingShouldAlert())
			return false;

		if (!hasBoostPotionInInventory(skill))
			return false;

		return isBoostBelowThreshold(skill);
	}

	private Skill getPrimarySkillForDefensive()
	{
		if (usingDefensiveMagic())
			return Skill.MAGIC;
		if (usingDefensiveRanged())
			return Skill.RANGED;
		return Skill.STRENGTH;
	}

	private boolean isSkillDisabled(Skill skill)
	{
		switch (skill)
		{
			case MAGIC:
				return !config.enableMagic();
			case RANGED:
				return !config.enableRanged();
			case ATTACK:
			case STRENGTH:
				return !config.enableMelee();
			default:
				return true;
		}
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

	private boolean hasBoostPotionInInventory(Skill skill)
	{
		if (MELEE_SKILLS.contains(skill) && config.enableMelee() && hasMeleePotion(skill))
			return true;

		if (Skill.RANGED == skill && config.enableRanged()
				&& Arrays.stream(playerItems).anyMatch(item -> RANGED_POTIONS.contains(item.getId())))
			return true;

		if (Skill.MAGIC == skill && config.enableMagic() && Arrays.stream(playerItems).anyMatch(item ->
				(((item.getId() == ItemID.IMBUED_HEART || item.getId() == ItemID.SATURATED_HEART) && isHeartAvailable())
						|| MAGIC_POTIONS.contains(item.getId()))))
			return true;

		return (config.enableMelee() || config.enableRanged() || config.enableMagic())
				&& Arrays.stream(playerItems).anyMatch(item -> OVERLOADS.contains(item.getId()));
	}

	private boolean isHeartAvailable()
	{
		return client.getVarbitValue(Varbits.IMBUED_HEART_COOLDOWN) == 0;
	}

	private boolean hasMeleePotion(Skill skill)
	{
		if (skill == Skill.ATTACK)
			return Arrays.stream(playerItems).anyMatch(item -> ATTACK_POTIONS.contains(item.getId()));

		if (skill == Skill.STRENGTH)
			return Arrays.stream(playerItems).anyMatch(item -> STRENGTH_POTIONS.contains(item.getId()));

		return false;
	}

	private boolean isBoostBelowThreshold(Skill skill)
	{
		if (MELEE_SKILLS.contains(skill) && config.enableMelee() && isMeleeBoostBelowThreshold(skill))
			return true;

		if (Skill.RANGED == skill && config.enableRanged()
				&& playerBoosts.getOrDefault(Skill.RANGED, -1) <= config.rangedBoostThreshold())
			return true;

		return (Skill.MAGIC == skill && config.enableMagic()
				&& playerBoosts.getOrDefault(Skill.MAGIC, -1) <= config.magicBoostThreshold());
	}

	private boolean isMeleeBoostBelowThreshold(Skill skill)
	{
		if (Skill.STRENGTH == skill)
		{
			return playerBoosts.getOrDefault(Skill.STRENGTH, -1) <= config.meleeBoostThreshold();
		}

		if (Skill.ATTACK == skill && (config.meleeAlertStyle() == MeleeAlertStyle.ATTACK_AND_STRENGTH))
		{
			return playerBoosts.getOrDefault(Skill.ATTACK, -1) <= config.meleeBoostThreshold();
		}

		return false;
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
