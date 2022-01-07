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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;

public class KObjectLocationRegistry
{
	private final int RANDOM_COMMON_SEED = new Random().nextInt();

	private final Client client;
	private final ClientThread clientThread;
	private final EventBus eventBus;

	private Map<WorldPoint, KObjectCounter> registry;

	private List<List<Integer>> tileModelIdGroups;
	private List<List<Integer>> chestModelIdGroups;

	public KObjectLocationRegistry(Client client, ClientThread clientThread, EventBus eventBus, List<List<Integer>> tileModelIdGroups, List<List<Integer>> chestModelIdGroups)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.eventBus = eventBus;

		registry = new HashMap<>();

		this.tileModelIdGroups = tileModelIdGroups;
		this.chestModelIdGroups = chestModelIdGroups;
	}

	public void add(WorldPoint location)
	{
		KObjectCounter kObjectCounter = registry.get(location);

		if (kObjectCounter != null)
		{
			kObjectCounter.increment();

			return;
		}

		KObject newObject = createRandomKObject(location);

		registry.put(location, new KObjectCounter(newObject, 1));
		newObject.setActive(true);
	}

	public void remove(WorldPoint location)
	{
		KObjectCounter kObjectCounter = registry.get(location);

		if (kObjectCounter == null)
		{
			return;
		}

		if (kObjectCounter.getCount() > 1)
		{
			kObjectCounter.decrement();

			return;
		}

		kObjectCounter.getKObject().setActive(false);
		registry.remove(location);
	}

	public boolean isEmpty()
	{
		return registry.isEmpty();
	}

	public int size()
	{
		return registry.size();
	}

	public void reset()
	{
		deactivateAll();

		registry = new HashMap<>();
	}

	public KObject createRandomKObject(WorldPoint location)
	{
		List<Integer> models = new ArrayList<>(getRandomTileModelIdGroup(location));

		models.addAll(getRandomChestModelIdGroup(location));

		return new KObject(client, clientThread, eventBus, location, models);
	}

	public void setTileModelIdGroups(List<List<Integer>> tileModelIdGroups)
	{
		this.tileModelIdGroups = tileModelIdGroups;

		recreateAll();
	}

	public void setChestModelIdGroups(List<List<Integer>> chestModelIdGroups)
	{
		this.chestModelIdGroups = chestModelIdGroups;

		recreateAll();
	}

	public void setModelIds(List<List<Integer>> tileModelIdGroups, List<List<Integer>> chestModelIdGroups)
	{
		this.tileModelIdGroups = tileModelIdGroups;
		this.chestModelIdGroups = chestModelIdGroups;

		recreateAll();
	}

	public void activateAll()
	{
		for (KObjectCounter kObjectCounter : registry.values())
		{
			kObjectCounter.getKObject().setActive(true);
		}
	}

	public void deactivateAll()
	{
		for (KObjectCounter kObjectCounter : registry.values())
		{
			kObjectCounter.getKObject().setActive(false);
		}
	}

	public void recreateAll()
	{
		for (KObjectCounter kObjectCounter : registry.values())
		{
			WorldPoint location = kObjectCounter.getKObject().getLocation();

			kObjectCounter.getKObject().setActive(false);
			kObjectCounter.setkObject(createRandomKObject(location));
			kObjectCounter.getKObject().setActive(true);
		}
	}

	private List<Integer> getRandomTileModelIdGroup(WorldPoint location)
	{
		Random random = new Random();

		random.setSeed(generateSeed(location));

		return tileModelIdGroups.get(random.nextInt(tileModelIdGroups.size()));
	}

	private List<Integer> getRandomChestModelIdGroup(WorldPoint location)
	{
		Random random = new Random();

		random.setSeed(generateSeed(location));

		return chestModelIdGroups.get(random.nextInt(chestModelIdGroups.size()));
	}

	private int generateSeed(WorldPoint location)
	{
		String stringSeed = "x" + location.getX() + "y" + location.getY() + "p" + location.getPlane();

		return RANDOM_COMMON_SEED ^ stringSeed.hashCode();
	}
}
