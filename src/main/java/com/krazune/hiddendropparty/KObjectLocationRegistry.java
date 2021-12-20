package com.krazune.hiddendropparty;

import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

public class KObjectLocationRegistry
{
	private Client client;

	private Map<WorldPoint, KObjectCounter> registry;

	public KObjectLocationRegistry(Client client)
	{
		this.client = client;
		registry = new HashMap<>();
	}

	public void add(WorldPoint location)
	{
		KObjectCounter kObjectCounter = registry.get(location);

		if (kObjectCounter != null)
		{
			kObjectCounter.increment();

			return;
		}

		KObject newObject = new KObject(client, location, 40205, 11123);

		registry.put(location, new KObjectCounter(newObject, 1));
		newObject.spawn();
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
