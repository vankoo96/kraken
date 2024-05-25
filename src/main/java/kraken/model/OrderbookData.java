package kraken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderbookData {
	private BookData[] data;
	private String channel;
	private String type;
}
