package com.krazune.hiddendropparty;

import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

// I swear I'm not narcissistic, I just didn't know what to name this class. Might change in the future.
public class KObject
{
	private final Client client;

	private final WorldPoint location;
	private final List<Integer> modelIds;
	private List<RuneLiteObject> objects;

	public KObject(Client client, WorldPoint location, int... modelIds)
	{
		this.client = client;
		this.location = location;
		this.modelIds = new ArrayList<>();

		for (int i = 0; i < modelIds.length; ++i)
		{
			this.modelIds.add(modelIds[i]);
		}

		objects = new ArrayList<>();
	}

	public KObject(Client client, WorldPoint location, List<Integer> modelIds)
	{
		this.client = client;
		this.location = location;
		this.modelIds = modelIds;
		objects = new ArrayList<>();
	}

	public WorldPoint getLocation()
	{
		return location;
	}

	public List<Integer> getModelIds()
	{
		return modelIds;
	}

	public void spawn()
	{
		despawn();

		for (int i = 0; i < modelIds.size(); ++i)
		{
			LocalPoint localLocation = LocalPoint.fromWorld(client, location);

			if (localLocation == null)
			{
				continue;
			}

			Model newModel = client.loadModel(modelIds.get(i));

			if (newModel == null)
			{
				continue;
			}

			RuneLiteObject newObject = client.createRuneLiteObject();

			newObject.setLocation(localLocation, location.getPlane());
			newObject.setModel(newModel);
			newObject.setActive(true);

			objects.add(newObject);
		}
	}

	public void despawn()
	{
		for (int i = 0; i < objects.size(); ++i)
		{
			objects.get(i).setActive(false);
		}

		objects.clear();
	}
}
