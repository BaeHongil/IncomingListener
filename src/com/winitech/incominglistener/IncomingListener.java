package com.winitech.incominglistener;

import com.wowza.wms.stream.IMediaStream;

import java.util.Iterator;

import com.google.gson.Gson;
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
		WebSocketMessage messageText = WebSocketMessage.createMessageText(isMaskOutgoingMessages, messageStr);
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
			
			if( hostPort.getTypeStr().equals("Admin") ) {
				for(Iterator<IHTTPProvider> iter = hostPort.getHttpProviders().iterator(); iter.hasNext(); ) {
					IHTTPProvider mIhttpProvider = iter.next();
					if( mIhttpProvider instanceof WebSocket ) 
						return (WebSocket) mIhttpProvider;
				}
			}
		}
		
		return null;
	}

	@Override
	public void onPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend)
	{
		IClient client = stream.getClient();
		IVHost vhost = client.getVHost();
		String appName = client.getApplication().getName();
		String appInstanceName =client.getAppInstance().getName();
		
		WebSocket mWebSocketTest = getWebSocket( vhost );
		IncomingStreamAddr mIncomingStreamAddr = new IncomingStreamAddr(true, vhost.getName(), appName, appInstanceName, streamName);
		
		broadcastWebSocketStr(vhost, mWebSocketTest, gson.toJson(mIncomingStreamAddr));
	
		WMSLoggerFactory.getLogger(null).info("onPublish: " + stream.getContextStr());
	}

	@Override
	public void onUnPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend)
	{
		IClient client = stream.getClient();
		IVHost vhost = client.getVHost();
		String appName = client.getApplication().getName();
		String appInstanceName =client.getAppInstance().getName();
		
		WebSocket mWebSocketTest = getWebSocket( vhost );
		IncomingStreamAddr mIncomingStreamAddr = new IncomingStreamAddr(false, vhost.getName(), appName, appInstanceName, streamName);
		
		broadcastWebSocketStr(vhost, mWebSocketTest, gson.toJson(mIncomingStreamAddr));
		
		WMSLoggerFactory.getLogger(null).info("onUnPublish: " + stream.getName());
	}
}