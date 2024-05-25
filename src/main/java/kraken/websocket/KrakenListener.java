package kraken.websocket;

import java.math.BigDecimal;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;

import kraken.model.Book;
import kraken.model.BookData;
import kraken.model.OrderbookData;
import kraken.model.PriceQuantity;
import kraken.model.TradingPair;

public class KrakenListener implements Listener {

	private final ObjectMapper mapper;
	private final Integer MAX_PRICE_LEVELS = 10;
	private final Book btcUsd = new Book(TradingPair.BITCOIN_TO_USD.getValue(), new LinkedList<>(), new LinkedList<>());
	private final Book ethUsd = new Book(TradingPair.ETHERIUM_TO_USD.getValue(), new LinkedList<>(),
			new LinkedList<>());

	public KrakenListener() {
		mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		try {
			OrderbookData orderbookData = mapper.readValue(data.toString(), OrderbookData.class);
			System.out.println("Recived message");
			System.out.println(data);
			if ("book".equals(orderbookData.getChannel())) {
				Book currentBook = orderbookData.getData()[0].getSymbol().equals(TradingPair.BITCOIN_TO_USD.getValue())
						? btcUsd
						: ethUsd;
				switch (orderbookData.getType()) {
				case "snapshot":
					initBook(orderbookData);
					break;
				case "update":

					System.out.println("Before update");
					print(ethUsd, orderbookData.getData()[0].getTimestamp());
					updateTopTen(orderbookData.getData()[0], currentBook);
//					print(btcUsd, orderbookData.getData()[0].getTimestamp());
					System.out.println("After update");
					print(ethUsd, orderbookData.getData()[0].getTimestamp());
					break;
				default:
					System.out.println("Not supported type -> " + orderbookData.getType());
					break;
				}

				if (!validateChecksum(currentBook, orderbookData.getData()[0].getChecksum())) {
					System.err.println("WRONG CHECKSUM");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return WebSocket.Listener.super.onText(webSocket, data, false);
	}

	private void initBook(OrderbookData orderbookData) {
		if (orderbookData.getData()[0].getSymbol().equals(TradingPair.BITCOIN_TO_USD.getValue())) {
			btcUsd.setAsks(new LinkedList<PriceQuantity>(List.of(orderbookData.getData()[0].getAsks()).reversed()));
			btcUsd.setBids(new LinkedList<PriceQuantity>(List.of(orderbookData.getData()[0].getBids())));
		} else if (orderbookData.getData()[0].getSymbol().equals(TradingPair.ETHERIUM_TO_USD.getValue())) {
			ethUsd.setAsks(new LinkedList<PriceQuantity>(List.of(orderbookData.getData()[0].getAsks()).reversed()));
			ethUsd.setBids(new LinkedList<PriceQuantity>(List.of(orderbookData.getData()[0].getBids())));
		}
	}

	private void updateTopTen(BookData bookData, Book book) {
		PriceQuantity[] newAsks = bookData.getAsks();
		PriceQuantity[] newBids = bookData.getBids();

		updateList(newAsks, book.getAsks(), true);
		updateList(newBids, book.getBids(), false);
	}

	private void updateList(PriceQuantity[] newData, List<PriceQuantity> list, boolean isAsk) {
		for (int i = 0; i < newData.length; i++) {
			if (newData[i].getQty().equals(new BigDecimal("0E-8"))) {
				removeFromList(newData[i], list);
			} else {
				int j = list.size();
				boolean addNumber = false;
				while (j > 0) {
					int compare = newData[i].getPrice().compareTo(list.get(j - 1).getPrice());

					if (compare == 0) {
						list.get(j - 1).setQty(newData[i].getQty());
						addNumber = false;
						break;
					} else if (compare > 0) {
						addNumber = true;
						j--;
					} else {
						if (list.size() < MAX_PRICE_LEVELS) {
							addNumber = true;
						}
						break;
					}
				}
				if (addNumber) {
					System.out.println("adding " + addNumber + " " + list.size());
					list.add(j, newData[i]);
					if (list.size() > MAX_PRICE_LEVELS) {
						System.out.println("Removing last");
						if (isAsk) {
							list.removeFirst();
						} else {
							list.removeLast();
						}
					}
				}
			}
		}
	}

	private void removeFromList(PriceQuantity toBeRemoved, List<PriceQuantity> list) {
		Iterator<PriceQuantity> iterator = list.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getPrice().compareTo(toBeRemoved.getPrice()) == 0) {
				iterator.remove();
				break;
			}
		}
	}

	private boolean validateChecksum(Book book, Long checksum) {
		Long calculatedChecksum = ChecksumCalculator.calculate(book);
		System.out.println(calculatedChecksum + " comparing to " + checksum);
		return calculatedChecksum.equals(checksum);
	}

	private void print(Book book, ZonedDateTime timestamp) {
		System.out.println("<------------------------------------>");
		System.out.println("asks:");
		book.getAsks().forEach(ask -> System.out.println(ask.toString()));
		System.out.println("best bid: " + book.getBids().get(0));
		System.out.println("best ask: " + book.getAsks().get(0));
		System.out.println("bids:");
		book.getBids().forEach(bid -> System.out.println(bid.toString()));
		System.out.println(timestamp);
		System.out.println(book.getSymbol());
		System.out.println(">-------------------------------------<");
	}

}
