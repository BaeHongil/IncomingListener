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
	
	// ������ ������
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

		// Ŭ���̾�Ʈ�� �޽����� �������� ��
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

	// ó�� Webscoket ��û�� ���� ��
	public void onHTTPRequest(IVHost vhost, IHTTPRequest req, IHTTPResponse resp)
	{
		StackTraceElement[] ste = new Throwable().getStackTrace();
		
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME + " constructor");
		for(StackTraceElement element : ste) {
			WMSLoggerFactory.getLogger(CLASS).info(element.getClassName() + "["+ element.getMethodName() +"]" + "("+ element.getLineNumber()+ ")");
		}
		
		if (!doHTTPAuthentication(vhost, req, resp))
			return;
		
		// HTTP ��û�� UpgradeRequest�� ��(�� Websocket ��û�� ��)
		if (req.isUpgradeRequest())
		{
			String upgradeType = req.getHeader("upgrade");
			if (upgradeType != null && upgradeType.equalsIgnoreCase(IWebSocketSession.HTTPHEADER_NAME))
			{
				// ���� ���� Upgrade�� ����� �����ؼ� Websocket �������� �˸�
				resp.setHeader("Upgrade", IWebSocketSession.HTTPHEADER_NAME);
				resp.setHeader("Connection", "Upgrade");
				
				String webSocketKey = req.getHeader(IWebSocketSession.HTTPHEADER_SECKEY);
				if (webSocketKey != null)
				{
					String digestStr = WebSocketUtils.createSecResponse(webSocketKey);
					if (digestStr != null)
						resp.setHeader(IWebSocketSession.HTTPHEADER_SECACCEPT, digestStr);
				}
				
				// websocket���� ��ȯ���� Ŭ���̾�Ʈ�� �˸�
				resp.setResponseCode(101);
				
				// �ش� Websocket�� ���� �����͸� ���
				resp.setUpgradeRequestProtocol(IHTTPResponse.UPGRADE_PROTOCOL_WEBSOCKETS, new MyWebSocketListener());
			}
			else
				resp.setResponseCode(404); // Websocket ��û�� �ƴҽÿ�
		}
		else
			resp.setResponseCode(404); // Websocket ��û�� �ƴҽÿ�
	}

}
