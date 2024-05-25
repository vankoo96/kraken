package kraken.model;

import java.util.LinkedList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Book {

	private String symbol;
	private LinkedList<PriceQuantity> asks;
	private LinkedList<PriceQuantity> bids;
}
