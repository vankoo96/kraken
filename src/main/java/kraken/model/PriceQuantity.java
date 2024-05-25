package kraken.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceQuantity {
	private BigDecimal price;
	private BigDecimal qty;

	@Override
	public String toString() {
		return "[ " + price + ", " + qty + " ]";
	}
}
