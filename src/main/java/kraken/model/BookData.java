package kraken.model;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookData {
	private String symbol;
	private Long checksum;
	private PriceQuantity[] bids;
	private PriceQuantity[] asks;
	private ZonedDateTime timestamp;
}
