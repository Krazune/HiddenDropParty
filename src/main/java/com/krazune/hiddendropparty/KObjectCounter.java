package com.krazune.hiddendropparty;

public class KObjectCounter
{
	private KObject kObject;
	private int count;

	public KObjectCounter(KObject kObject)
	{
		this(kObject, 0);
	}

	public KObjectCounter(KObject kObject, int count)
	{
		this.kObject = kObject;
		this.count = count;
	}

	public KObject getKObject()
	{
		return kObject;
	}

	public int getCount()
	{
		return count;
	}

	public void setkObject(KObject kObject)
	{
		this.kObject = kObject;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public void increment()
	{
		++count;
	}

	public void decrement()
	{
		--count;
	}
}
