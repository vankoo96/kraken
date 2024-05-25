package kraken;

import kraken.websocket.WebSocketClient;

public class App {

	public static void main(String[] args) throws InterruptedException {
		new WebSocketClient();
		Thread.currentThread().join();
	}
}
