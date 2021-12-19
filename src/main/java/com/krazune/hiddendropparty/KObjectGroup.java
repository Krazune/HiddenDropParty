package com.krazune.hiddendropparty;

import java.util.ArrayList;
import java.util.List;

public class KObjectGroup
{
	private final List<KObject> objects;

	public KObjectGroup(KObject... objects)
	{
		this.objects = new ArrayList<>();

		for (int i = 0; i < objects.length; ++i)
		{
			this.objects.add(objects[i]);
		}
	}

	public KObjectGroup(List<KObject> objects)
	{
		this.objects = objects;
	}

	public void spawn()
	{
		for (int i = 0; i < objects.size(); ++i)
		{
			objects.get(i).spawn();
		}
	}

	public void despawn()
	{
		for (int i = 0; i < objects.size(); ++i)
		{
			objects.get(i).despawn();
		}
	}
}
