package com.winitech.incominglistener;

import com.wowza.wms.module.*;
import com.wowza.wms.stream.*;

public class IncomingListenerModule extends ModuleBase {
	private IMediaStreamActionNotify3 mIncomingListener = new IncomingListener();
	
	public void onStreamCreate(IMediaStream stream) {
		stream.addClientListener( mIncomingListener );
		getLogger().info("onStreamCreate: " + stream.getSrc());
	}

	public void onStreamDestroy(IMediaStream stream) {
		stream.removeClientListener( mIncomingListener );
		getLogger().info("onStreamDestroy: " + stream.getSrc());
	}

}
