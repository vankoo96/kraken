package kraken.orderbook;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import kraken.model.Book;
import kraken.model.BookData;
import kraken.model.OrderbookData;
import kraken.model.PriceQuantity;
import kraken.model.TradingPair;

public class OrderbookManager {

	private final Integer MAX_PRICE_LEVELS = 10;
	private final BigDecimal ZERO = new BigDecimal("0E-8");
	private final Book btcUsd = new Book(TradingPair.BITCOIN_TO_USD.getValue(), new LinkedList<>(), new LinkedList<>());
	private final Book ethUsd = new Book(TradingPair.ETHERIUM_TO_USD.getValue(), new LinkedList<>(),
			new LinkedList<>());

	public void handleOrderbookData(OrderbookData orderbookData) {
		if ("book".equals(orderbookData.getChannel())) {
			Book currentBook = orderbookData.getData()[0].getSymbol().equals(TradingPair.BITCOIN_TO_USD.getValue())
					? btcUsd
					: ethUsd;
			switch (orderbookData.getType()) {
			case "snapshot":
				initBook(orderbookData);
				break;
			case "update":
				updateTopTen(orderbookData.getData()[0], currentBook);
				print(ethUsd, orderbookData.getData()[0].getTimestamp());
				print(btcUsd, orderbookData.getData()[0].getTimestamp());
				break;
			default:
				System.out.println("Not supported type -> " + orderbookData.getType());
				break;
			}

			if (!validateChecksum(currentBook, orderbookData.getData()[0].getChecksum())) {
				System.err.println("WRONG CHECKSUM");
			}
		}
	}

	/**
	 * Initialize the book with the data from the snapshot.
	 * 
	 * @param orderbookData {@link OrderbookData}
	 */
	private void initBook(OrderbookData orderbookData) {
		Book bookToInit = orderbookData.getData()[0].getSymbol().equals(TradingPair.BITCOIN_TO_USD.getValue()) ? btcUsd
				: ethUsd;
		bookToInit.setAsks(new LinkedList<PriceQuantity>(List.of(orderbookData.getData()[0].getAsks())));
		bookToInit.setBids(new LinkedList<PriceQuantity>(List.of(orderbookData.getData()[0].getBids())));
	}

	private void updateTopTen(BookData bookData, Book book) {
		updateAsks(bookData.getAsks(), book.getAsks());
		updateBids(bookData.getBids(), book.getBids());
	}

	private void updateAsks(PriceQuantity[] newData, List<PriceQuantity> asks) {
		for (int i = 0; i < newData.length; i++) {
			if (ZERO.equals(newData[i].getQty())) {
				removeFromList(newData[i], asks);
			} else {
				int j = 0;
				while (j < asks.size()) {
					int compare = newData[i].getPrice().compareTo(asks.get(j).getPrice());
					if (compare == 0) {
						asks.get(j).setQty(newData[i].getQty());
						break;
					} else if (compare > 0) {
						j++;
					} else {
						asks.add(j, newData[i]);
						break;
					}
				}

				if (asks.size() < MAX_PRICE_LEVELS) {
					asks.add(newData[i]);
				} else {
					while (asks.size() > MAX_PRICE_LEVELS) {
						asks.removeLast();
					}
				}
			}
		}
	}

	private void updateBids(PriceQuantity[] newData, List<PriceQuantity> list) {
		for (int i = 0; i < newData.length; i++) {
			if (ZERO.equals(newData[i].getQty())) {
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
					list.add(j, newData[i]);
					if (list.size() > MAX_PRICE_LEVELS) {
						list.removeLast();
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
		return ChecksumCalculator.calculate(book).equals(checksum);
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
