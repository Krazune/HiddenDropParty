package com.krazune.hiddendropparty;

public enum ChestModelIdsGroup
{
	DEFAULT("11123,15567,15885"),
	WOODEN_CRATES("12152,15509,29973,33922,31450"),
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
