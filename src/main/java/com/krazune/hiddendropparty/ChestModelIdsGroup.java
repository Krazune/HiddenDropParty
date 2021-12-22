package com.krazune.hiddendropparty;

public enum ChestModelIdsGroup
{
	DEFAULT("11123,15567,15885"),
	CUSTOM("");

	private final String idsString;

	ChestModelIdsGroup(String idsString)
	{
		this.idsString = idsString;
	}

	public String getValue()
	{
		return idsString;
	}
}
