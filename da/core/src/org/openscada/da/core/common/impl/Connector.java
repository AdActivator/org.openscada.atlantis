package org.openscada.da.core.common.impl;

import org.openscada.da.core.Session;
import org.openscada.da.core.common.DataItem;

public class Connector {
	private DataItemInfo _itemInfo;
	private Session _session;
	
	public Connector ( DataItemInfo itemInfo, Session session )
	{
		_itemInfo = itemInfo;
		_session = session;
	}
}
