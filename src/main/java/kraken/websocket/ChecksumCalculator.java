package kraken.websocket;

import java.util.zip.CRC32;

import kraken.model.Book;

public class ChecksumCalculator {

	/**
	 * Returns the checksum for the given book.
	 * 
	 * @param book
	 * @return
	 */
	public static Long calculate(Book book) {
		StringBuilder builder = new StringBuilder();
		book.getAsks().reversed().forEach(a -> {
			builder.append(removeLeadingZerosAndPoints(a.getPrice().toString())
					+ removeLeadingZerosAndPoints(a.getQty().toString()));
		});

		book.getBids().forEach(b -> {
			builder.append(removeLeadingZerosAndPoints(b.getPrice().toString())
					+ removeLeadingZerosAndPoints(b.getQty().toString()));
		});

		CRC32 crc32 = new CRC32();
		crc32.update(builder.toString().getBytes());
		return crc32.getValue();
	}

	private static String removeLeadingZerosAndPoints(String input) {
		StringBuilder builder = new StringBuilder();
		boolean startAppending = false;

		for (Character ch : input.toCharArray()) {
			if (ch != '0' && ch != '.') {
				startAppending = true;
			}
			if (startAppending && ch != '.') {
				builder.append(ch);
			}
		}

		return builder.toString();
	}

}
