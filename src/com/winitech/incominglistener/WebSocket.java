package com.winitech.incominglistener;

import com.wowza.util.BufferUtils;
import com.wowza.wms.http.*;
import com.wowza.wms.logging.*;
import com.wowza.wms.util.WebSocketUtils;
import com.wowza.wms.vhost.*;
import com.wowza.wms.websocket.model.IWebSocketSession;
import com.wowza.wms.websocket.model.WebSocketEventNotifyBase;
import com.wowza.wms.websocket.model.WebSocketMessage;

public class WebSocket extends HTTProvider2Base {

	private static final Class<WebSocket> CLASS = WebSocket.class;
	private static final String CLASSNAME = "WebSocket";
	
	// 웹소켓 리스너
	class MyWebSocketListener extends WebSocketEventNotifyBase
	{
		@Override
		public void onCreate(IWebSocketSession webSocketSession)
		{
			WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+"#MyWebSocketListener.onCreate["+webSocketSession.getSessionId()+"]");
		}

		@Override
		public void onDestroy(IWebSocketSession webSocketSession)
		{
			WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+"#MyWebSocketListener.onDestroy["+webSocketSession.getSessionId()+"]");
		}

		// 클라이언트에 메시지가 도착했을 때
		@Override
		public void onMessage(IWebSocketSession webSocketSession, WebSocketMessage message)
		{
			if (message.isText())
			{
				WebSocketMessage messageText = WebSocketMessage.createMessageText(webSocketSession.isMaskOutgoingMessages(), message.getValueString());
				webSocketSession.sendMessage(messageText);
				
				WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+"#MyWebSocketListener.onMessage["+webSocketSession.getSessionId()+"][text]: "+message.getValueString());
			}
			else if (message.isBinary())
			{
				WebSocketMessage messageBinary = WebSocketMessage.createMessageBinary(webSocketSession.isMaskOutgoingMessages(), message.getBuffer(), message.getOffset(), message.getLen());
				webSocketSession.sendMessage(messageBinary);
				
				WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+"#MyWebSocketListener.onMessage["+webSocketSession.getSessionId()+"][binary]: #"+BufferUtils.encodeHexString(message.getBuffer(), message.getOffset(), message.getLen()));
			}
		}
	}

	// 처음 Webscoket 요청이 왔을 때
	public void onHTTPRequest(IVHost vhost, IHTTPRequest req, IHTTPResponse resp)
	{
		StackTraceElement[] ste = new Throwable().getStackTrace();
		
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME + " constructor");
		for(StackTraceElement element : ste) {
			WMSLoggerFactory.getLogger(CLASS).info(element.getClassName() + "["+ element.getMethodName() +"]" + "("+ element.getLineNumber()+ ")");
		}
		
		if (!doHTTPAuthentication(vhost, req, resp))
			return;
		
		// HTTP 요청이 UpgradeRequest일 때(즉 Websocket 요청일 때)
		if (req.isUpgradeRequest())
		{
			String upgradeType = req.getHeader("upgrade");
			if (upgradeType != null && upgradeType.equalsIgnoreCase(IWebSocketSession.HTTPHEADER_NAME))
			{
				// 응답 또한 Upgrade로 헤더를 지정해서 Websocket 응답임을 알림
				resp.setHeader("Upgrade", IWebSocketSession.HTTPHEADER_NAME);
				resp.setHeader("Connection", "Upgrade");
				
				String webSocketKey = req.getHeader(IWebSocketSession.HTTPHEADER_SECKEY);
				if (webSocketKey != null)
				{
					String digestStr = WebSocketUtils.createSecResponse(webSocketKey);
					if (digestStr != null)
						resp.setHeader(IWebSocketSession.HTTPHEADER_SECACCEPT, digestStr);
				}
				
				// websocket으로 전환됨을 클라이언트에 알림
				resp.setResponseCode(101);
				
				// 해당 Websocket에 대한 리스터를 등록
				resp.setUpgradeRequestProtocol(IHTTPResponse.UPGRADE_PROTOCOL_WEBSOCKETS, new MyWebSocketListener());
			}
			else
				resp.setResponseCode(404); // Websocket 요청이 아닐시에
		}
		else
			resp.setResponseCode(404); // Websocket 요청이 아닐시에
	}

}
