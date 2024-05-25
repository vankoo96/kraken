package kraken.websocket;

import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;

import kraken.model.OrderbookData;
import kraken.orderbook.OrderbookManager;

public class KrakenListener implements Listener {

	private final ObjectMapper mapper;
	private final OrderbookManager orderbookManager;

	public KrakenListener() {
		mapper = new ObjectMapper();
		mapper.findAndRegisterModules();

		orderbookManager = new OrderbookManager();
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		try {
			OrderbookData orderbookData = mapper.readValue(data.toString(), OrderbookData.class);
			orderbookManager.handleOrderbookData(orderbookData);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("dsfsdfdsfdsfds");
		}

		return WebSocket.Listener.super.onText(webSocket, data, false);
	}

}
