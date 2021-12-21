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

		int tileModelId = getRandomTileModelId(location);
		int chestModelId = getRandomChestModelId(location);

		KObject newObject = new KObject(client, location, tileModelId, chestModelId);

		registry.put(location, new KObjectCounter(newObject, 1));
		newObject.spawn();
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

	public boolean isEmpty()
	{
		return registry.isEmpty();
	}

	public int size()
	{
		return registry.size();
	}
}
