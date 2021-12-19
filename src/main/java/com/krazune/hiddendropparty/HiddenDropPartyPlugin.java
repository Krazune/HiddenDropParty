package com.krazune.hiddendropparty;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Hidden Drop Party",
	description = "Hides the drop party drops. Useful for streamers.",
	tags = {
		"drop",
		"party",
		"stream",
		"hide",
		"hidden",
		"gifts",
		"presents"
	}
)
public class HiddenDropPartyPlugin extends Plugin
{
	@Inject
	private Client client;

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		KObject base = new KObject(client, itemSpawned.getTile().getWorldLocation(), 40205);
		KObject chest = new KObject(client, itemSpawned.getTile().getWorldLocation(), 11123);

		new KObjectGroup(base, chest).spawn();
	}
}
