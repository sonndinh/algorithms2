import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;

public class BurrowsWheeler {
	private static final int R = 256;

	// apply Burrows-Wheeler transform, reading from standard input and writing to
	// standard output
	public static void transform() {
		// Read from binary input
		String s = BinaryStdIn.readString();
		int length = s.length();
		CircularSuffixArray suffixArr = new CircularSuffixArray(s);

		// Find the index of the suffix that equals to the original message in
		// the list of sorted suffixes.
		int first = 0;
		for (int i = 0; i < length; i++) {
			if (suffixArr.index(i) == 0) {
				first = i;
				break;
			}
		}
		BinaryStdOut.write(first);

		// Write the list of characters at the end of each circular suffix
		for (int i = 0; i < length; i++) {
			// Write to binary output
			BinaryStdOut.write(s.charAt((suffixArr.index(i) - 1 + length) % length));
		}
		BinaryStdOut.flush();
	}

	// apply Burrows-Wheeler inverse transform, reading from standard input and
	// writing to standard output
	public static void inverseTransform() {
		// Read from binary input
		int first = BinaryStdIn.readInt();
		String t = BinaryStdIn.readString();
		int length = t.length();

		// The first column of characters
		char[] f = new char[length];

		// The array storing the next indices
		int[] next = new int[length];

		// Use key-indexed counting to sort the first column and
		// also construct the next array at the same time
		int[] count = new int[R + 1];
		for (int i = 0; i < length; i++) {
			count[t.charAt(i) + 1]++;
		}
		for (int r = 0; r < R; r++) {
			count[r + 1] += count[r];
		}
		for (int i = 0; i < length; i++) {
			char c = t.charAt(i);
			f[count[c]] = t.charAt(i);
			next[count[c]] = i;
			count[c]++;
		}

		// Reconstruct the original message
		char[] orig = new char[length];
		for (int i = 0; i < length; i++) {
			orig[i] = f[first];
			first = next[first];
		}

		// Write to binary output
		BinaryStdOut.write(new String(orig));
		BinaryStdOut.flush();
	}

	// if args[0] is '-', apply Burrows-Wheeler transform
	// if args[0] is '+', apply Burrows-Wheeler inverse transform
	public static void main(String[] args) {
		if (args.length != 1) {
			BinaryStdOut.write("Wrong argument! Must indicate encode (-) or decode (+)!");
			return;
		}

		if (args[0].equals("-")) {
			transform();
		} else if (args[0].equals("+")) {
			inverseTransform();
		}
	}
}
