package com.krazune.hiddendropparty;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import net.runelite.client.events.ConfigChanged;
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

	private final int DEFAULT_TILE_MODEL_ID = 21367;
	private final int DEFAULT_CHEST_MODEL_ID = 11123;

	@Inject
	private Client client;

	@Inject
	private HiddenDropPartyPluginConfig config;

	@Inject
	private ClientThread clientThread;

	private KObjectLocationRegistry registry;
	private Map<WorldPoint, Instant> fakeDropLocationSpawnInstants;

	private List<Integer> tileModelIds;
	private List<Integer> chestModelIds;

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

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getKey().equals("tileModelIds"))
		{
			tileModelIds = getTileModelIdsListFromConfig();
		}
		else if (configChanged.getKey().equals("chestModelIds"))
		{
			chestModelIds = getChestModelIdsListFromConfig();
		}
		else
		{
			return;
		}

		clientThread.invokeLater(this::updateModelIds);
	}

	@Override
	protected void startUp()
	{
		loadModelIdsConfig();

		registry = new KObjectLocationRegistry(client, tileModelIds, chestModelIds);
		fakeDropLocationSpawnInstants = new HashMap<>();
	}

	@Override
	protected void shutDown()
	{
		clientThread.invokeLater(this::deleteRegistry);
		fakeDropLocationSpawnInstants = null;
		tileModelIds = null;
		chestModelIds = null;
	}

	@Provides
	HiddenDropPartyPluginConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HiddenDropPartyPluginConfig.class);
	}

	private void loadModelIdsConfig()
	{
		tileModelIds = getTileModelIdsListFromConfig();
		chestModelIds = getChestModelIdsListFromConfig();
	}

	private void updateModelIds()
	{
		registry.setModelIds(tileModelIds, chestModelIds);
	}

	private List<Integer> getTileModelIdsListFromConfig()
	{
		List<Integer> tiles = new ArrayList<>();
		String[] configSplit = config.tileModelIds().split(",");

		for (int i = 0; i < configSplit.length; ++i)
		{
			int  modelId;

			try
			{
				modelId = Integer.parseInt(configSplit[i]);
			}
			catch (NumberFormatException e)
			{
				continue;
			}

			if (modelId < 0)
			{
				continue;
			}

			tiles.add(modelId);
		}

		if (tiles.isEmpty())
		{
			tiles.add(DEFAULT_TILE_MODEL_ID);
		}

		return tiles;
	}

	private List<Integer> getChestModelIdsListFromConfig()
	{
		List<Integer> chest = new ArrayList<>();
		String[] configSplit = config.chestModelIds().split(",");

		for (int i = 0; i < configSplit.length; ++i)
		{
			int  modelId;

			try
			{
				modelId = Integer.parseInt(configSplit[i]);
			}
			catch (NumberFormatException e)
			{
				continue;
			}

			if (modelId < 0)
			{
				continue;
			}

			chest.add(modelId);
		}

		if (chest.isEmpty())
		{
			chest.add(DEFAULT_CHEST_MODEL_ID);
		}

		return chest;
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
		registry = new KObjectLocationRegistry(client, tileModelIds, chestModelIds);
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
