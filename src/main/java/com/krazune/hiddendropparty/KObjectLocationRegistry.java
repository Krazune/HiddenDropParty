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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

public class KObjectLocationRegistry
{
	private final int RANDOM_COMMON_SEED = new Random().nextInt();

	private Client client;

	private Map<WorldPoint, KObjectCounter> registry;

	private List<Integer> tileModelIds;
	private List<Integer> chestModelIds;

	public KObjectLocationRegistry(Client client, List<Integer> tileModelIds, List<Integer> chestModelIds)
	{
		this.client = client;
		registry = new HashMap<>();
		this.tileModelIds = tileModelIds;
		this.chestModelIds = chestModelIds;
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
		newObject.spawn();
	}

	public KObject createRandomKObject(WorldPoint location)
	{
		int tileModelId = getRandomTileModelId(location);
		int chestModelId = getRandomChestModelId(location);

		return new KObject(client, location, tileModelId, chestModelId);
	}

	private int getRandomTileModelId(WorldPoint location)
	{
		Random random = new Random();

		random.setSeed(generateSeed(location));

		return tileModelIds.get(random.nextInt(tileModelIds.size()));
	}

	private int getRandomChestModelId(WorldPoint location)
	{
		Random random = new Random();

		random.setSeed(generateSeed(location));

		return chestModelIds.get(random.nextInt(chestModelIds.size()));
	}

	public void setTileModelIds(List<Integer> tileModelIds)
	{
		this.tileModelIds = tileModelIds;

		recreateAll();
	}

	public void setChestModelIds(List<Integer> chestModelIds)
	{
		this.chestModelIds = chestModelIds;

		recreateAll();
	}

	public void setModelIds(List<Integer> tileModelIds, List<Integer> chestModelIds)
	{
		this.tileModelIds = tileModelIds;
		this.chestModelIds = chestModelIds;

		recreateAll();
	}

	private int generateSeed(WorldPoint location)
	{
		String stringSeed = "x" + location.getX() + "y" + location.getY() + "p" + location.getPlane();

		return RANDOM_COMMON_SEED ^ stringSeed.hashCode();
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

		kObjectCounter.getKObject().despawn();
		registry.remove(location);
	}

	public void reset()
	{
		despawnAll();

		registry = new HashMap<>();
	}

	public void spawnAll()
	{
		for (KObjectCounter kObjectCounter : registry.values())
		{
			kObjectCounter.getKObject().spawn();
		}
	}

	public void despawnAll()
	{
		for (KObjectCounter kObjectCounter : registry.values())
		{
			kObjectCounter.getKObject().despawn();
		}
	}

	public void recreateAll()
	{
		for (KObjectCounter kObjectCounter : registry.values())
		{
			WorldPoint location = kObjectCounter.getKObject().getLocation();

			kObjectCounter.getKObject().despawn();
			kObjectCounter.setkObject(createRandomKObject(location));
			kObjectCounter.getKObject().spawn();
		}
	}

	public boolean isEmpty()
	{
		return registry.isEmpty();
	}

	public int size()
	{
		return registry.size();
	}
}
