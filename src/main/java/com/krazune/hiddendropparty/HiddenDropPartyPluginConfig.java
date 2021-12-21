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
		keyName = "tileModelIds",
		name = "Tile model IDs",
		description = "List of model IDs for the obstructing tiles, separated by commas (the GitHub page has some useful IDs)."
	)
	default String getTileModelIds()
	{
		return "21367,21369,21370";
	}

	@ConfigItem(
		position = 1,
		keyName = "chestModelIds",
		name = "Chest model IDs",
		description = "List of model IDs for the main objects, separated by commas (the GitHub page has some useful IDs)."
	)
	default String getChestModelIds()
	{
		return "11123,12884,15567,15885";
	}

	@ConfigItem(
		position = 2,
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
