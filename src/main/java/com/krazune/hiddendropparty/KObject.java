package com.krazune.hiddendropparty;

import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

// I swear I'm not narcissistic, I just didn't know what to name this class. Might change in the future.
public class KObject
{
	private final WorldPoint location;
	private final int modelId;

	private final Client client;

	private RuneLiteObject object;

	public KObject(Client client, WorldPoint location, int modelId)
	{
		this.client = client;
		this.location = location;
		this.modelId = modelId;
	}

	public WorldPoint getLocation()
	{
		return location;
	}

	public int getModelId()
	{
		return modelId;
	}

	public void spawn()
	{
		if (object != null)
		{
			object.setActive(false);
		}

		LocalPoint localLocation = LocalPoint.fromWorld(client, location);

		if (localLocation == null)
		{
			return;
		}

		RuneLiteObject newObject = client.createRuneLiteObject();
		Model newModel = loadModel();

		if (newModel == null)
		{
			return;
		}

		newObject.setLocation(localLocation, location.getPlane());
		newObject.setModel(newModel);
		newObject.setActive(true);
	}

	public void despawn()
	{
		if (object == null)
		{
			return;
		}

		object.setActive(false);
		object = null;
	}

	private Model loadModel()
	{
		return client.loadModel(modelId);
	}
}
