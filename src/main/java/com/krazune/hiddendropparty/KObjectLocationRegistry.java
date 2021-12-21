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
