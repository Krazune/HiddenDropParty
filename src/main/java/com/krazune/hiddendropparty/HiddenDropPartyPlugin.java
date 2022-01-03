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
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Hidden Drop Party",
	description = "Hides drops. Useful for streamers that want to make drop parties on stream but don't want to show where the good drops were placed.",
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

	@Inject
	private EventBus eventBus;

	private KObjectLocationRegistry registry;
	private Map<WorldPoint, Instant> fakeDropLocationSpawnInstants;

	private List<Integer> tileModelIds;
	private List<Integer> chestModelIds;

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		removeOldFakeDrops();

		Random rand = new Random();

		if (rand.nextInt(100) < config.getFakeDropPercentage())
		{
			createFakeDrop(client.getLocalPlayer().getWorldLocation());
		}
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
		switch (gameStateChanged.getGameState())
		{
			case LOGIN_SCREEN:
			case HOPPING:
				resetRegistry();
				fakeDropLocationSpawnInstants.clear();
				break;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		String keyName = configChanged.getKey();

		if (keyName.equals("tileModelIdsGroup"))
		{
			loadTilesModelIdsConfig();
		}
		else if (keyName.equals("chestModelIdsGroup"))
		{
			loadChestModelIdsConfig();
		}
		else if (keyName.equals("customTileModelIds") && config.getTileModelIdsGroup() == TileModelIdsGroup.CUSTOM)
		{
			tileModelIds = parseIds(config.getCustomTileModelIds(), DEFAULT_TILE_MODEL_ID);
		}
		else if (keyName.equals("customChestModelIds") && config.getChestModelIdsGroup() == ChestModelIdsGroup.CUSTOM)
		{
			chestModelIds = parseIds(config.getCustomChestModelIds(), DEFAULT_CHEST_MODEL_ID);
		}
		else
		{
			return;
		}

		updateModelIds();
	}

	@Override
	protected void startUp()
	{
		loadModelIdsConfig();

		registry = new KObjectLocationRegistry(client, clientThread, eventBus, tileModelIds, chestModelIds);
		fakeDropLocationSpawnInstants = new HashMap<>();
	}

	@Override
	protected void shutDown()
	{
		deleteRegistry();

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
		loadTilesModelIdsConfig();
		loadChestModelIdsConfig();
	}

	private void loadTilesModelIdsConfig()
	{
		if (config.getTileModelIdsGroup() == TileModelIdsGroup.CUSTOM)
		{
			tileModelIds = parseIds(config.getCustomTileModelIds(), DEFAULT_TILE_MODEL_ID);
		}
		else
		{
			tileModelIds = parseIds(config.getTileModelIdsGroup().getValue(), DEFAULT_TILE_MODEL_ID);
		}
	}

	private void loadChestModelIdsConfig()
	{
		if (config.getChestModelIdsGroup() == ChestModelIdsGroup.CUSTOM.CUSTOM)
		{
			chestModelIds = parseIds(config.getCustomChestModelIds(), DEFAULT_CHEST_MODEL_ID);
		}
		else
		{
			chestModelIds = parseIds(config.getChestModelIdsGroup().getValue(), DEFAULT_CHEST_MODEL_ID);
		}
	}

	private void updateModelIds()
	{
		registry.setModelIds(tileModelIds, chestModelIds);
	}

	private void createFakeDrop(WorldPoint location)
	{
		if (fakeDropLocationSpawnInstants.putIfAbsent(location, Instant.now()) != null)
		{
			return;
		}

		registry.add(location);
	}

	private List<Integer> parseIds(String modelIdsString, int defaultModelId)
	{
		List<Integer> modelIds = new ArrayList<>();
		String[] stringSplit = modelIdsString.split(",");

		for (int i = 0; i < stringSplit.length; ++i)
		{
			int  modelId;

			try
			{
				modelId = Integer.parseInt(stringSplit[i]);
			}
			catch (NumberFormatException e)
			{
				continue;
			}

			if (modelId < 0)
			{
				continue;
			}

			modelIds.add(modelId);
		}

		if (modelIds.isEmpty())
		{
			modelIds.add(defaultModelId);
		}

		return modelIds;
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
		registry.deactivateAll();
		registry = new KObjectLocationRegistry(client, clientThread, eventBus, tileModelIds, chestModelIds);
	}

	private void deleteRegistry()
	{
		registry.deactivateAll();
		registry = null;
	}
}
