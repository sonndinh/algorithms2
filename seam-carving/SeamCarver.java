import java.util.Arrays;

import edu.princeton.cs.algs4.Picture;

public class SeamCarver {

	private static final double BORDER_ENERGY = 1000;
	private final Picture mypic;
	private int[][] colors; // Store current picture
	private int curWidth; // Current width of the picture
	private int curHeight; // Current height of the picture
	private double[][] energy;
	private double[][] dp;
	private double[][] dpTrans;

	public SeamCarver(Picture picture) {
		// create a seam carver object based on the given picture
		if (picture == null) {
			throw new IllegalArgumentException("Invalid input picture");
		}

		mypic = new Picture(picture);
		int width = mypic.width();
		int height = mypic.height();

		colors = new int[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				colors[i][j] = mypic.getRGB(j, i);
			}
		}
		curWidth = width;
		curHeight = height;

		// Pre-compute energies for all pixels of the original picture
		energy = new double[curHeight][curWidth];
		calEnergies();

		dp = new double[curHeight][curWidth];
		dpTrans = new double[curWidth][curHeight];
	}

	// Calculate energy for pixel at column x, row y
	private double calEnergy(int x, int y) {
		if (x == 0 || x == curWidth - 1 || y == 0 || y == curHeight - 1) {
			return BORDER_ENERGY;
		}

		// Must use pixels from the current picture
		int xNext = colors[y][x + 1];
		int xPrev = colors[y][x - 1];
		int yNext = colors[y + 1][x];
		int yPrev = colors[y - 1][x];

		int xNextRed = (xNext >> 16) & 0xFF;
		int xNextGreen = (xNext >> 8) & 0xFF;
		int xNextBlue = (xNext >> 0) & 0xFF;
		int xPrevRed = (xPrev >> 16) & 0xFF;
		int xPrevGreen = (xPrev >> 8) & 0xFF;
		int xPrevBlue = (xPrev >> 0) & 0xFF;
		int yNextRed = (yNext >> 16) & 0xFF;
		int yNextGreen = (yNext >> 8) & 0xFF;
		int yNextBlue = (yNext >> 0) & 0xFF;
		int yPrevRed = (yPrev >> 16) & 0xFF;
		int yPrevGreen = (yPrev >> 8) & 0xFF;
		int yPrevBlue = (yPrev >> 0) & 0xFF;

		double deltaX = Math.pow(xNextRed - xPrevRed, 2) + Math.pow(xNextGreen - xPrevGreen, 2)
				+ Math.pow(xNextBlue - xPrevBlue, 2);
		double deltaY = Math.pow(yNextRed - yPrevRed, 2) + Math.pow(yNextGreen - yPrevGreen, 2)
				+ Math.pow(yNextBlue - yPrevBlue, 2);

		return Math.sqrt(deltaX + deltaY);
	}

	// Calculate energy for all pixels
	private void calEnergies() {
		for (int x = 0; x < curWidth; x++) {
			for (int y = 0; y < curHeight; y++) {
				energy[y][x] = calEnergy(x, y);
			}
		}
	}

	// Calculate DP table for a matrix with specified number of rows and columns
	private void calDp(double[][] matrix, int row, int col) {
		for (int i = 1; i < row; i++) {
			if (col == 1) {
				// Corner case
				matrix[i][0] = matrix[i][0] + matrix[i - 1][0];
				continue;
			}

			for (int j = 0; j < col; j++) {
				if (j == 0) {
					matrix[i][j] = matrix[i][j] + Math.min(matrix[i - 1][j], matrix[i - 1][j + 1]);
				} else if (j == col - 1) {
					matrix[i][j] = matrix[i][j] + Math.min(matrix[i - 1][j], matrix[i - 1][j - 1]);
				} else {
					matrix[i][j] = matrix[i][j]
							+ Math.min(matrix[i - 1][j], Math.min(matrix[i - 1][j - 1], matrix[i - 1][j + 1]));
				}
			}
		}
	}

	public Picture picture() {
		// current picture
		Picture pic = new Picture(curWidth, curHeight);
		for (int i = 0; i < curWidth; i++) {
			for (int j = 0; j < curHeight; j++) {
				pic.setRGB(i, j, colors[j][i]);
			}
		}
		return pic;
	}

	public int width() {
		// width of current picture
		return curWidth;
	}

	public int height() {
		// height of current picture
		return curHeight;
	}

	public double energy(int x, int y) {
		// energy of pixel at column x and row y
		if (x < 0 || x >= mypic.width() || y < 0 || y >= mypic.height()) {
			throw new IllegalArgumentException("Invalid pixel coordinate");
		}

		return energy[y][x];
	}

	public int[] findHorizontalSeam() {
		// Create a transpose matrix
		for (int i = 0; i < curHeight; i++) {
			for (int j = 0; j < curWidth; j++) {
				dpTrans[j][i] = energy[i][j];
			}
		}
		
		// Only calculate the dp table when requested
		calDp(dpTrans, curWidth, curHeight);
		return findSeam(dpTrans, curWidth, curHeight);
	}

	public int[] findVerticalSeam() {
		// Only calculate the dp table when requested
		for (int i = 0; i < curHeight; i++) {
			dp[i] = Arrays.copyOf(energy[i], curWidth);
		}

		calDp(dp, curHeight, curWidth);
		return findSeam(dp, curHeight, curWidth);
	}

	// A general method to find a top-to-bottom seam in a given matrix.
	// This backtracks the seam from the input, already-computed matrix. 
	private int[] findSeam(double[][] matrix, int row, int col) {
		int[] seam = new int[row];
		if (col == 1) {
			// Corner case
			Arrays.fill(seam, 0);
			return seam;
		}

		int minIdx = 0;
		for (int i = 1; i < col; i++) {
			if (matrix[row - 1][i] < matrix[row - 1][minIdx]) {
				minIdx = i;
			}
		}
		seam[row - 1] = minIdx;

		for (int i = row - 2; i >= 0; i--) {
			int idx = minIdx;
			if (idx == 0) {
				if (matrix[i][idx + 1] < matrix[i][idx]) {
					minIdx = idx + 1;
				}
			} else if (idx == col - 1) {
				if (matrix[i][idx - 1] < matrix[i][idx]) {
					minIdx = idx - 1;
				}
			} else {
				if (matrix[i][idx - 1] < matrix[i][minIdx]) {
					minIdx = idx - 1;
				}
				if (matrix[i][idx + 1] < matrix[i][minIdx]) {
					minIdx = idx + 1;
				}
			}
			seam[i] = minIdx;
		}

		return seam;
	}

	public void removeHorizontalSeam(int[] seam) {
		// remove horizontal seam from current picture
		if (seam == null || seam.length != curWidth || curHeight <= 1) {
			throw new IllegalArgumentException("Invalid horizontal seam");
		}

		for (int i = 0; i < seam.length; i++) {
			if (seam[i] < 0 || seam[i] >= curHeight) {
				throw new IllegalArgumentException("Invalid horizontal seam");
			}
			if (i < seam.length - 1) {
				if (Math.abs(seam[i] - seam[i + 1]) > 1) {
					throw new IllegalArgumentException("Invalid horizontal seam");
				}
			}
		}

		// Update the current picture and its energy matrix
		curHeight -= 1;
		for (int i = 0; i < curWidth; i++) {
			int delAt = seam[i];
			for (int j = delAt; j < curHeight; j++) {
				colors[j][i] = colors[j + 1][i];
				energy[j][i] = energy[j + 1][i];
			}
		}

		// Recalculate the energies of pixels along the seam
		// (Pixels on the actual seam and the ones immediately above them)
		for (int i = 0; i < curWidth; i++) {
			int idx = seam[i];
			if (idx < curHeight) {
				energy[idx][i] = calEnergy(i, idx);
			}

			if (idx > 0) {
				energy[idx - 1][i] = calEnergy(i, idx - 1);
			}
		}
	}

	public void removeVerticalSeam(int[] seam) {
		// remove vertical seam from current picture
		if (seam == null || seam.length != curHeight || curWidth <= 1) {
			throw new IllegalArgumentException("Invalid vertical seam");
		}

		for (int i = 0; i < seam.length; i++) {
			if (seam[i] < 0 || seam[i] >= curWidth) {
				throw new IllegalArgumentException("Invalid vertical seam");
			}
			if (i < seam.length - 1) {
				if (Math.abs(seam[i] - seam[i + 1]) > 1) {
					throw new IllegalArgumentException("Invalid vertical seam");
				}
			}
		}

		// Update the current picture and its energy
		curWidth -= 1;
		for (int i = 0; i < curHeight; i++) {
			int delAt = seam[i];
			System.arraycopy(colors[i], delAt + 1, colors[i], delAt, curWidth - delAt);
			System.arraycopy(energy[i], delAt + 1, energy[i], delAt, curWidth - delAt);
		}

		// Recalculate energies of the pixels along the seam
		// (pixels along the actual seam and the ones immediately next to them on the
		// left-hand side)
		for (int i = 0; i < curHeight; i++) {
			int idx = seam[i];
			if (idx < curWidth) {
				energy[i][idx] = calEnergy(idx, i);
			}

			if (idx > 0) {
				energy[i][idx - 1] = calEnergy(idx - 1, i);
			}
		}
	}

}
