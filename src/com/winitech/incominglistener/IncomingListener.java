package com.winitech.incominglistener;

import com.wowza.wms.stream.IMediaStream;

import java.util.Iterator;

import com.google.gson.Gson;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.client.IClient;
import com.wowza.wms.http.IHTTPProvider;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.MediaStreamActionNotify3Base;
import com.wowza.wms.vhost.HostPort;
import com.wowza.wms.vhost.HostPortList;
import com.wowza.wms.vhost.IVHost;
import com.wowza.wms.websocket.model.WebSocketMessage;

public class IncomingListener extends MediaStreamActionNotify3Base
{	
	private Gson gson = new Gson();
	
	/**
	 * websocket���� ��Ʈ���� ���Ʈĳ��Ʈ
	 * @param vhost IVHost ��ü
	 * @param mWebSocket ��ε�ĳ��Ʈ ���� websocket ��ü
	 * @param messageStr ��ε�ĳ��Ʈ�� ��Ʈ��
	 */
	private void broadcastWebSocketStr(IVHost vhost, WebSocket mWebSocket, String messageStr) {
		boolean isMaskOutgoingMessages = vhost.getWebSocketContext().isMaskOutgoingMessages();
		WebSocketMessage messageText = WebSocketMessage.createMessageText(isMaskOutgoingMessages, messageStr); // Websocket���� ������ �޽��� ����
		mWebSocket.broadcastWebSocketMessage(messageText);
	}
	
	/**
	 * vhost�� ���Ե� HTTP Provider �߿��� WebSocket ��ü ȹ��
	 * @param vhost ã�� IVHost ��ü
	 * @return WebSocket ��ü
	 */
	private WebSocket getWebSocket(IVHost vhost) {
		HostPortList portList = vhost.getHostPortsList();
		for(int i = 0; i < portList.size(); i++) {
			HostPort hostPort = portList.get(i);
			int port = hostPort.getPort();
			
			if( hostPort.getTypeStr().equals("Admin") ) { // HTTP Provider�� ������ �ִ� Admin HostPort ã��
				for(Iterator<IHTTPProvider> iter = hostPort.getHttpProviders().iterator(); iter.hasNext(); ) {
					IHTTPProvider mIhttpProvider = iter.next();
					if( mIhttpProvider instanceof WebSocket ) // WebSocket ��ü�� ã��
						return (WebSocket) mIhttpProvider;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * IMediaStream���� ���� �����͸� Websocket�� ���� ��ε�ĳ��Ʈ
	 * @param isPublish Publish�̸� true, �ƴϸ� false
	 * @param stream publish�ǰų� unpublish�� IMediaStream��ü
	 */
	private void broadcastPublishStream(boolean isPublish, IMediaStream stream) {
		IApplicationInstance appInst = stream.getStreams().getAppInstance();
		IVHost vhost = appInst.getVHost();
		String appName = appInst.getApplication().getName();
		String appInstanceName = appInst.getName();
		
		WebSocket mWebSocketTest = getWebSocket( vhost );
		IncomingStreamAddr mIncomingStreamAddr = new IncomingStreamAddr(isPublish, vhost.getName(), appName, appInstanceName, stream.getName());
		
		broadcastWebSocketStr(vhost, mWebSocketTest, gson.toJson(mIncomingStreamAddr));
	}

	@Override
	public void onPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend)
	{
		/*
		IClient client = stream.getClient();
		IVHost vhost = client.getVHost();
		String appName = client.getApplication().getName();
		String appInstanceName = client.getAppInstance().getName();
		*/
		/*IApplicationInstance appInst = stream.getStreams().getAppInstance();
		IVHost vhost = appInst.getVHost();
		String appName = appInst.getApplication().getName();
		String appInstanceName = appInst.getName();
		
		
		WebSocket mWebSocketTest = getWebSocket( vhost );
		IncomingStreamAddr mIncomingStreamAddr = new IncomingStreamAddr(true, vhost.getName(), appName, appInstanceName, streamName);
		
		broadcastWebSocketStr(vhost, mWebSocketTest, gson.toJson(mIncomingStreamAddr));*/
		
		broadcastPublishStream(true, stream);
		WMSLoggerFactory.getLogger(null).info("onPublish: " + stream.getName());
	}

	@Override
	public void onUnPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend)
	{
		broadcastPublishStream(false, stream);
		WMSLoggerFactory.getLogger(null).info("onUnPublish: " + stream.getName());
	}
}