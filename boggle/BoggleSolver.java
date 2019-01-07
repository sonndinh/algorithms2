import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.TST;

public class BoggleSolver {
	private enum Color {
		WHITE, GRAY
	}

	private final TST<Integer> dict;
	private Color[][] colors;
	private List<List<List<Dice>>> nbors;

	// Initializes the data structure using the given array of strings as the
	// dictionary.
	// (You can assume each word in the dictionary contains only the uppercase
	// letters A through Z.)
	public BoggleSolver(String[] dictionary) {
		dict = new TST<Integer>();

		for (String word : dictionary) {
			dict.put(word, 1);
		}
	}

	private class Dice {
		private final int row;
		private final int col;

		public Dice(int i, int j) {
			row = i;
			col = j;
		}
	}

	// Return neighbors of the dice at row ith, column jth.
	// rows and cols are the numbers of rows and columns of the board.
	private Iterable<Dice> neighbors(int i, int j, int rows, int cols) {
		List<Dice> list = new ArrayList<Dice>();
		if (rows == 1 && cols > 1) {
			if (j == 0) {
				list.add(new Dice(i, j + 1));
			} else if (j == cols - 1) {
				list.add(new Dice(i, j - 1));
			} else {
				list.add(new Dice(i, j - 1));
				list.add(new Dice(i, j + 1));
			}
		} else if (cols == 1 && rows > 1) {
			if (i == 0) {
				list.add(new Dice(i + 1, j));
			} else if (i == rows - 1) {
				list.add(new Dice(i - 1, j));
			} else {
				list.add(new Dice(i - 1, j));
				list.add(new Dice(i + 1, j));
			}
		} else if (rows > 1 && cols > 1) {
			if (i == 0 && j == 0) {
				list.add(new Dice(i, j + 1));
				list.add(new Dice(i + 1, j));
				list.add(new Dice(i + 1, j + 1));
			} else if (i == 0 && j == cols - 1) {
				list.add(new Dice(i, j - 1));
				list.add(new Dice(i + 1, j));
				list.add(new Dice(i + 1, j - 1));
			} else if (i == rows - 1 && j == 0) {
				list.add(new Dice(i, j + 1));
				list.add(new Dice(i - 1, j));
				list.add(new Dice(i - 1, j + 1));
			} else if (i == rows - 1 && j == cols - 1) {
				list.add(new Dice(i, j - 1));
				list.add(new Dice(i - 1, j));
				list.add(new Dice(i - 1, j - 1));
			} else if (i == 0 && j > 0 && j < cols - 1) {
				list.add(new Dice(i, j - 1));
				list.add(new Dice(i + 1, j - 1));
				list.add(new Dice(i + 1, j));
				list.add(new Dice(i + 1, j + 1));
				list.add(new Dice(i, j + 1));
			} else if (i == rows - 1 && j > 0 && j < cols - 1) {
				list.add(new Dice(i, j - 1));
				list.add(new Dice(i - 1, j - 1));
				list.add(new Dice(i - 1, j));
				list.add(new Dice(i - 1, j + 1));
				list.add(new Dice(i, j + 1));
			} else if (j == 0 && i > 0 && i < rows - 1) {
				list.add(new Dice(i - 1, j));
				list.add(new Dice(i - 1, j + 1));
				list.add(new Dice(i, j + 1));
				list.add(new Dice(i + 1, j + 1));
				list.add(new Dice(i + 1, j));
			} else if (j == cols - 1 && i > 0 && i < rows - 1) {
				list.add(new Dice(i - 1, j));
				list.add(new Dice(i - 1, j - 1));
				list.add(new Dice(i, j - 1));
				list.add(new Dice(i + 1, j - 1));
				list.add(new Dice(i + 1, j));
			} else {
				list.add(new Dice(i - 1, j - 1));
				list.add(new Dice(i - 1, j));
				list.add(new Dice(i - 1, j + 1));
				list.add(new Dice(i, j + 1));
				list.add(new Dice(i + 1, j + 1));
				list.add(new Dice(i + 1, j));
				list.add(new Dice(i + 1, j - 1));
				list.add(new Dice(i, j - 1));
			}
		}

		return list;
	}

	private int checkSize(Iterable<String> container) {
		int size = 0;
		for (String item : container) {
			size++;
		}
		return size;
	}

	private void dfsVisit(BoggleBoard board, int i, int j, int rows, int cols, StringBuilder str,
			HashSet<String> words) {
		List<Dice> adj = nbors.get(i).get(j);
		for (Dice d : adj) {
			if (colors[d.row][d.col] == Color.WHITE) {
				if (board.getLetter(d.row, d.col) == 'Q') {
					str.append("QU");
				} else {
					str.append(board.getLetter(d.row, d.col));
				}

				// Backtrack from branch with an unmatched prefix
				if (checkSize(dict.keysWithPrefix(str.toString())) == 0) {
					if (board.getLetter(d.row, d.col) == 'Q') {
						str.delete(str.length() - 2, str.length());
					} else {
						str.deleteCharAt(str.length() - 1);
					}
					continue;
				}

				if (str.length() >= 3 && dict.contains(str.toString())) {
					words.add(str.toString());
				}
				colors[d.row][d.col] = Color.GRAY;
				dfsVisit(board, d.row, d.col, rows, cols, str, words);
				if (board.getLetter(d.row, d.col) == 'Q') {
					str.delete(str.length() - 2, str.length());
				} else {
					str.deleteCharAt(str.length() - 1);
				}
				colors[d.row][d.col] = Color.WHITE;
			}
		}
	}

	// Returns the set of all valid words in the given Boggle board, as an Iterable.
	public Iterable<String> getAllValidWords(BoggleBoard board) {
		int rows = board.rows();
		int cols = board.cols();
		colors = new Color[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				colors[i][j] = Color.WHITE;
			}
		}

		// Pre-compute neighbors list for each position in the board
		nbors = new ArrayList<List<List<Dice>>>();
		for (int i = 0; i < rows; i++) {
			nbors.add(new ArrayList<List<Dice>>());
			for (int j = 0; j < cols; j++) {
				nbors.get(i).add(new ArrayList<Dice>());
				for (Dice item : neighbors(i, j, rows, cols)) {
					nbors.get(i).get(j).add(item);
				}
			}
		}

		HashSet<String> words = new HashSet<String>();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				StringBuilder str = new StringBuilder();
				if (colors[i][j] == Color.WHITE) {
					if (board.getLetter(i, j) == 'Q') {
						str.append("QU");
					} else {
						str.append(board.getLetter(i, j));
					}
					colors[i][j] = Color.GRAY;
					dfsVisit(board, i, j, rows, cols, str, words);
					if (board.getLetter(i, j) == 'Q') {
						str.delete(0, 2);
					} else {
						str.deleteCharAt(0);
					}
					colors[i][j] = Color.WHITE;
				}
			}
		}

		return words;
	}

	// Returns the score of the given word if it is in the dictionary, zero
	// otherwise.
	// (You can assume the word contains only the uppercase letters A through Z.)
	public int scoreOf(String word) {
		if (!dict.contains(word)) {
			return 0;
		}
		
		int length = word.length();

		if (length <= 2) {
			return 0;
		} else if (length == 3 || length == 4) {
			return 1;
		} else if (length == 5) {
			return 2;
		} else if (length == 6) {
			return 3;
		} else if (length == 7) {
			return 5;
		} else {
			return 11;
		}
	}

	public static void main(String[] args) {
		In in = new In(args[0]);
		String[] dictionary = in.readAllStrings();
		BoggleSolver solver = new BoggleSolver(dictionary);
		BoggleBoard board = new BoggleBoard(args[1]);
		int score = 0;
		for (String word : solver.getAllValidWords(board)) {
			StdOut.println(word);
			score += solver.scoreOf(word);
		}
		StdOut.println("Score = " + score);
	}

}
