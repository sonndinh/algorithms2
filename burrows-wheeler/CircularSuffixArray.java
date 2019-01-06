import java.util.Arrays;

import edu.princeton.cs.algs4.StdOut;

public class CircularSuffixArray {
	private final String s;
	private final CircularSuffix[] suffixes;

	public CircularSuffixArray(String s) {
		// circular suffix array of s
		if (s == null) {
			throw new IllegalArgumentException("Null argument");
		}
		
		this.s = s;
		suffixes = new CircularSuffix[s.length()];
		for (int i = 0; i < s.length(); i++) {
			suffixes[i] = new CircularSuffix(s, i);
		}
		Arrays.sort(suffixes);
	}
	
	// Based on the implementation of SuffixArray in the course's book site.
	private static class CircularSuffix implements Comparable<CircularSuffix> {
		private final String text;
		private final int index;
		private final int length;
		
		CircularSuffix(String text, int index) {
			this.text = text;
			this.index = index;
			this.length = text.length();
		}
		
		// This makes the difference between this and SuffixArray.
		public char charAt(int i) {
			return text.charAt((index + i) % length);
		}
		
		public int compareTo(CircularSuffix that) {
			if (this == that) return 0;
			for (int i = 0; i < length; i++) {
				if (this.charAt(i) < that.charAt(i)) return -1;
				if (this.charAt(i) > that.charAt(i)) return +1;
			}
			return 0;
		}
	}

	public int length() {
		// length of s
		return s.length();
	}

	public int index(int i) {
		// returns index of ith sorted suffix
		if (i < 0 || i > s.length()-1) {
			throw new IllegalArgumentException("Index out-of-bound");
		}
		
		return suffixes[i].index;
	}
	
	public static void main(String[] args) {
		// unit testing (required)
		String s = "ABRACADABRA!";
		CircularSuffixArray array = new CircularSuffixArray(s);
		for (int i = 0; i < s.length(); i++) {
			StdOut.println("Index[" + i + "]: " + array.index(i));
		}
	}
}
