/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2021, Miguel Sousa
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.krazune.hiddendropparty;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

import static net.runelite.client.config.Units.PERCENT;

@ConfigGroup("hiddendropparty")
public interface HiddenDropPartyPluginConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "tileModelIdsGroup",
		name = "Tile models group",
		description = "Group of model IDs for the obstructing tiles."
	)
	default TileModelIdsGroup getTileModelIdsGroup()
	{
		return TileModelIdsGroup.DEFAULT;
	}

	@ConfigItem(
		position = 1,
		keyName = "customTileModelIds",
		name = "Custom tile model IDs",
		description = "List of model IDs for the obstructing tiles, separated by commas, and multiple ids grouped by plus sign (the GitHub page has some useful IDs)."
	)
	default String getCustomTileModelIds()
	{
		return "";
	}

	@ConfigItem(
		position = 2,
		keyName = "chestModelIdsGroup",
		name = "Chest models group",
		description = "Group of model IDs for the main object."
	)
	default ChestModelIdsGroup getChestModelIdsGroup()
	{
		return ChestModelIdsGroup.DEFAULT;
	}

	@ConfigItem(
		position = 3,
		keyName = "customChestModelIds",
		name = "Custom chest model IDs",
		description = "List of model IDs for the main objects, separated by commas, and multiple ids grouped by plus sign (the GitHub page has some useful IDs)."
	)
	default String getCustomChestModelIds()
	{
		return "";
	}

	@ConfigItem(
		position = 4,
		keyName = "fakeDropPercentage",
		name = "Fake drop percentage",
		description = "The chance of spawning a fake drop per tick."
	)
	@Range(
		min = 0,
		max = 100
	)
	@Units(PERCENT)
	default int getFakeDropPercentage()
	{
		return 25;
	}
}
