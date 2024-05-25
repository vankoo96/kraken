package kraken.model;

public enum TradingPair {

	BITCOIN_TO_USD("BTC/USD"), ETHERIUM_TO_USD("ETH/USD");

	private final String value;

	private TradingPair(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
