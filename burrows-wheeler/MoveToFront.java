import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;

public class MoveToFront {
	private static final int R = 256;

	// Apply move-to-front encoding, reading from standard input and writing to
	// standard output.
	// Maintain an ordered sequence of 256 extended ASCII characters.
	public static void encode() {
		char[] ascii = new char[R];
		for (char i = 0; i < R; i++) {
			ascii[i] = i;
		}

		while (!BinaryStdIn.isEmpty()) {
			char c = BinaryStdIn.readChar();
			for (char i = 0; i < R; i++) {
				if (c == ascii[i]) {
					// Write the position
					BinaryStdOut.write(i);

					// Move to front and shift the preceding characters by 1
					for (char j = i; j >= 1; j--) {
						ascii[j] = ascii[j - 1];
					}
					ascii[0] = c;
					break;
				}
			}
		}

		BinaryStdOut.flush();
	}

	// apply move-to-front decoding, reading from standard input and writing to
	// standard output
	public static void decode() {
		char[] ascii = new char[R];
		for (char i = 0; i < R; i++) {
			ascii[i] = i;
		}

		while (!BinaryStdIn.isEmpty()) {
			char pos = BinaryStdIn.readChar();
			// Write the character
			char c = ascii[pos];
			BinaryStdOut.write(c);

			// Move it to the front
			for (char i = pos; i >= 1; i--) {
				ascii[i] = ascii[i - 1];
			}
			ascii[0] = c;
		}

		BinaryStdOut.flush();
	}

	// if args[0] is '-', apply move-to-front encoding
	// if args[0] is '+', apply move-to-front decoding
	public static void main(String[] args) {
		if (args.length != 1) {
			BinaryStdOut.write("Wrong argument! Must indicate encode (-) or decode (+)!");
			return;
		}
		
		if (args[0].equals("-")) {
			encode();
		} else if (args[0].equals("+")) {
			decode();
		}
	}
}
