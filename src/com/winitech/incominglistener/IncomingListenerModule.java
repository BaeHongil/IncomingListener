package com.winitech.incominglistener;

import com.wowza.wms.module.*;
import com.wowza.wms.stream.*;

public class IncomingListenerModule extends ModuleBase {
	private IMediaStreamActionNotify3 mIncomingListener = new IncomingListener();

	
	/**
	 * Application에서 stream(incoming, outgoing 모두) 생성시 발생
	 * @param stream 생성된 stream
	 */
	public void onStreamCreate(IMediaStream stream) {
		stream.addClientListener( mIncomingListener ); // incoming stream 리스너 등록
		getLogger().info("onStreamCreate: " + stream.getSrc());
	}

	/**
	 * Application에서 stream(incoming, outgoing 모두) 소멸시 발생
	 * @param stream 소멸된 stream
	 */
	public void onStreamDestroy(IMediaStream stream) {
		stream.removeClientListener( mIncomingListener ); // incoming stream 리스너 등록해제
		getLogger().info("onStreamDestroy: " + stream.getSrc());
	}

}
