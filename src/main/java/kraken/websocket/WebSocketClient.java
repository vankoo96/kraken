package kraken.websocket;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;

public class WebSocketClient {
	public WebSocketClient() {
		String webSocketURL = "wss://ws.kraken.com/v2";
		String subscriptionMsg = "{\"method\": \"subscribe\",\"params\": {\"channel\": \"book\",\"symbol\": [\"ETH/USD\", \"BTC/USD\"]}}";
		try {
			WebSocket ws = HttpClient.newHttpClient().newWebSocketBuilder()
					.buildAsync(new URI(webSocketURL), new KrakenListener()).join();
			ws.sendText(subscriptionMsg, true);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
