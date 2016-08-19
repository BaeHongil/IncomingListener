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
	 * websocket으로 스트링을 브로트캐스트
	 * @param vhost IVHost 객체
	 * @param mWebSocket 브로드캐스트 보낼 websocket 객체
	 * @param messageStr 브로드캐스트할 스트링
	 */
	private void broadcastWebSocketStr(IVHost vhost, WebSocket mWebSocket, String messageStr) {
		boolean isMaskOutgoingMessages = vhost.getWebSocketContext().isMaskOutgoingMessages();
		WebSocketMessage messageText = WebSocketMessage.createMessageText(isMaskOutgoingMessages, messageStr); // Websocket으로 전송할 메시지 생성
		mWebSocket.broadcastWebSocketMessage(messageText);
	}
	
	/**
	 * vhost에 포함된 HTTP Provider 중에서 WebSocket 객체 획득
	 * @param vhost 찾을 IVHost 객체
	 * @return WebSocket 객체
	 */
	private WebSocket getWebSocket(IVHost vhost) {
		HostPortList portList = vhost.getHostPortsList();
		for(int i = 0; i < portList.size(); i++) {
			HostPort hostPort = portList.get(i);
			int port = hostPort.getPort();
			
			if( hostPort.getTypeStr().equals("Admin") ) { // HTTP Provider를 가지고 있는 Admin HostPort 찾기
				for(Iterator<IHTTPProvider> iter = hostPort.getHttpProviders().iterator(); iter.hasNext(); ) {
					IHTTPProvider mIhttpProvider = iter.next();
					if( mIhttpProvider instanceof WebSocket ) // WebSocket 객체를 찾기
						return (WebSocket) mIhttpProvider;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * IMediaStream에서 얻은 데이터를 Websocket을 통해 브로드캐스트
	 * @param isPublish Publish이면 true, 아니면 false
	 * @param stream publish되거나 unpublish된 IMediaStream객체
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