package com.krazune.hiddendropparty;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("hiddendropparty")
public interface HiddenDropPartyPluginConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "fakeDropPercentage",
		name = "Fake drop percentage",
		description = "The chance of spawning a fake drop."
	)
	@Range(
		min = 0,
		max = 100
	)
	default int fakeDropPercentage()
	{
		return 25;
	}
}
