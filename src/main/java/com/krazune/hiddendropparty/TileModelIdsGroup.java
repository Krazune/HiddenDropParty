package com.krazune.hiddendropparty;

public enum TileModelIdsGroup
{
	DEFAULT("21367,21369,21370"),
	CUSTOM("");

	private final String idsString;

	TileModelIdsGroup(String idsString)
	{
		this.idsString = idsString;
	}

	public String getValue()
	{
		return idsString;
	}
}
