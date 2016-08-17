package com.winitech.incominglistener;

import com.wowza.wms.module.*;
import com.wowza.wms.stream.*;

public class IncomingListenerModule extends ModuleBase {
	private IMediaStreamActionNotify3 mIncomingListener = new IncomingListener();

	
	/**
	 * Application���� stream(incoming, outgoing ���) ������ �߻�
	 * @param stream ������ stream
	 */
	public void onStreamCreate(IMediaStream stream) {
		stream.addClientListener( mIncomingListener ); // incoming stream ������ ���
		getLogger().info("onStreamCreate: " + stream.getSrc());
	}

	/**
	 * Application���� stream(incoming, outgoing ���) �Ҹ�� �߻�
	 * @param stream �Ҹ�� stream
	 */
	public void onStreamDestroy(IMediaStream stream) {
		stream.removeClientListener( mIncomingListener ); // incoming stream ������ �������
		getLogger().info("onStreamDestroy: " + stream.getSrc());
	}

}
