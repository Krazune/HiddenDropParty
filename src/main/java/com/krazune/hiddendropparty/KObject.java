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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;

// I swear I'm not narcissistic, I just didn't know what to name this class. Might change in the future.
public class KObject
{
	private final Duration MODEL_LOAD_TIMEOUT_DURATION = Duration.ofSeconds(1);

	private final Client client;
	private final ClientThread clientThread;

	private final WorldPoint location;
	private final List<Integer> modelIds;
	private List<RuneLiteObject> objects;

	public KObject(Client client, ClientThread clientThread, WorldPoint location, int... modelIds)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.location = location;
		this.modelIds = new ArrayList<>();

		for (int i = 0; i < modelIds.length; ++i)
		{
			this.modelIds.add(modelIds[i]);
		}

		objects = new ArrayList<>();
	}

	public KObject(Client client, ClientThread clientThread, WorldPoint location, List<Integer> modelIds)
	{
		this.client = client;
		this.clientThread = clientThread;
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

			RuneLiteObject newObject = client.createRuneLiteObject();
			int modelId = modelIds.get(i);
			Model newModel = client.loadModel(modelId);

			if (newModel == null)
			{
				repeatingModelLoading(newObject, modelId);
			}
			else
			{
				newObject.setModel(newModel);
			}

			newObject.setLocation(localLocation, location.getPlane());
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

	private void repeatingModelLoading(RuneLiteObject object, int modelId)
	{
		final Instant loadTimeoutInstant = Instant.now().plus(MODEL_LOAD_TIMEOUT_DURATION);

		clientThread.invokeLater(() ->
		{
			if (Instant.now().isAfter(loadTimeoutInstant))
			{
				return true;
			}

			Model newModel = client.loadModel(modelId);

			if (newModel == null)
			{
				return false;
			}

			object.setModel(newModel);

			return true;
		});
	}
}
