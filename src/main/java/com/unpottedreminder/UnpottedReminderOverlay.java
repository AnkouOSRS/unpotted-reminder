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

import net.runelite.api.Client;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.util.AsyncBufferedImage;

import javax.inject.Inject;
import java.awt.*;


class UnpottedReminderOverlay extends OverlayPanel
{
	private final Client client;
	private final UnpottedReminderConfig config;
	private final AsyncBufferedImage vialImage;

	@Inject
	private UnpottedReminderOverlay(Client client, UnpottedReminderConfig config, ItemManager itemManager)
	{
		this.client = client;
		this.config = config;
		this.vialImage = itemManager.getImage(ItemID.VIAL_EMPTY);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();

		int contentWidth;
		if (config.useVialIcon())
		{
			panelComponent.getChildren().add(new ImageComponent(vialImage));
			contentWidth = vialImage.getWidth();
		}
		else
		{
			String alertMessage = UnpottedReminderPlugin.resolveAlertMessage(config);
			panelComponent.getChildren().add((LineComponent.builder())
					.left(alertMessage)
					.build());
			contentWidth = graphics.getFontMetrics().stringWidth(alertMessage);
		}

		panelComponent.setPreferredSize(new Dimension(contentWidth + 2 * ComponentConstants.STANDARD_BORDER, 0));

		if (config.shouldFlash())
		{
			if (client.getGameCycle() % 40 >= 20)
			{
				panelComponent.setBackgroundColor(config.flashColor1());
			}
			else
			{
				panelComponent.setBackgroundColor(config.flashColor2());
			}
		}
		else
		{
			panelComponent.setBackgroundColor(config.flashColor1());
		}

		setPosition(OverlayPosition.BOTTOM_RIGHT);
		return panelComponent.render(graphics);
	}
}
