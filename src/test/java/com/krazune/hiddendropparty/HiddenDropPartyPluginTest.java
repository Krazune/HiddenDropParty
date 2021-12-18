package com.krazune.hiddendropparty;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HiddenDropPartyPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(HiddenDropPartyPlugin.class);
		RuneLite.main(args);
	}
}
