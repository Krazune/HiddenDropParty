package com.krazune.hiddendropparty;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
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

	@Inject
	private ClientThread clientThread;

	private KObjectLocationRegistry registry;

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		registry.add(itemSpawned.getTile().getWorldLocation());
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		registry.remove(itemDespawned.getTile().getWorldLocation());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState newState = gameStateChanged.getGameState();

		if (newState == GameState.LOGIN_SCREEN || newState == GameState.HOPPING)
		{
			clientThread.invokeLater(this::resetRegistry);
		}
		else if (newState == GameState.LOADING)
		{
			registry.reset();
		}
	}

	@Override
	protected void startUp()
	{
		registry = new KObjectLocationRegistry(client);
	}

	@Override
	protected void shutDown()
	{
		clientThread.invokeLater(this::deleteRegistry);
	}

	@Provides
	HiddenDropPartyPluginConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HiddenDropPartyPluginConfig.class);
	}

	private void resetRegistry()
	{
		registry.despawnAll();
		registry = new KObjectLocationRegistry(client);
	}

	private void deleteRegistry()
	{
		registry.despawnAll();
		registry = null;
	}
}
