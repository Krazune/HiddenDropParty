package com.krazune.hiddendropparty;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
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
	private final Duration FAKE_DROP_DURATION = Duration.ofMinutes(1);

	@Inject
	private Client client;

	@Inject
	private HiddenDropPartyPluginConfig config;

	@Inject
	private ClientThread clientThread;

	private KObjectLocationRegistry registry;
	private Map<WorldPoint, Instant> fakeDropLocationSpawnInstants;

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		Random rand = new Random();

		if (rand.nextInt(100) < config.fakeDropPercentage())
		{
			createFakeDrop(client.getLocalPlayer().getWorldLocation());
		}

		removeOldFakeDrops();
	}

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
			fakeDropLocationSpawnInstants.clear();
		}
		else if (newState == GameState.LOADING)
		{
			registry.reset();
		}
		else if (newState == GameState.LOGGED_IN)
		{
			recreateFakeDrops();
		}
	}

	@Override
	protected void startUp()
	{
		registry = new KObjectLocationRegistry(client);
		fakeDropLocationSpawnInstants = new HashMap<>();
	}

	@Override
	protected void shutDown()
	{
		clientThread.invokeLater(this::deleteRegistry);
		fakeDropLocationSpawnInstants = null;
	}

	@Provides
	HiddenDropPartyPluginConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HiddenDropPartyPluginConfig.class);
	}

	private void createFakeDrop(WorldPoint location)
	{
		if (fakeDropLocationSpawnInstants.putIfAbsent(location, Instant.now()) != null)
		{
			return;
		}

		registry.add(location);
	}

	private void removeOldFakeDrops()
	{
		Iterator<Map.Entry<WorldPoint, Instant>> i = fakeDropLocationSpawnInstants.entrySet().iterator();

		while (i.hasNext())
		{
			Map.Entry<WorldPoint, Instant> entry = i.next();

			if (Duration.between(entry.getValue(), Instant.now()).compareTo(FAKE_DROP_DURATION) < 0)
			{
				continue;
			}

			registry.remove(entry.getKey());
			i.remove();
		}
	}

	private void resetRegistry()
	{
		registry.despawnAll();
		registry = new KObjectLocationRegistry(client);
	}

	private void recreateFakeDrops()
	{
		for (WorldPoint location : fakeDropLocationSpawnInstants.keySet())
		{
			registry.add(location);
		}
	}

	private void deleteRegistry()
	{
		registry.despawnAll();
		registry = null;
	}
}
