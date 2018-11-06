import java.awt.Color;

import edu.princeton.cs.algs4.DijkstraAllPairsSP;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.Picture;

public class SeamCarver {

	private final double BORDER_ENERGY = 1000;
	private Picture mypic;
	private EdgeWeightedDigraph vgraph;
	private DijkstraAllPairsSP vapsp;
	private EdgeWeightedDigraph hgraph;
	private DijkstraAllPairsSP hapsp;

	public SeamCarver(Picture picture) {
		// create a seam carver object based on the given picture
		if (picture == null) {
			throw new IllegalArgumentException("Invalid input picture");
		}

		mypic = new Picture(picture);
		build_vgraph();
		vapsp = new DijkstraAllPairsSP(vgraph);
		build_hgraph();
		hapsp = new DijkstraAllPairsSP(hgraph);
	}

	private void build_vgraph() {
		int width = mypic.width();
		int height = mypic.height();
		int num_vertices = width * height;
		vgraph = new EdgeWeightedDigraph(num_vertices);

		for (int i = 0; i < height - 1; i++) {
			for (int j = 0; j < width; j++) {
				int source = i * width + j;
				int down = (i + 1) * width + j;
				double weight = energy(j, i);
				vgraph.addEdge(new DirectedEdge(source, down, weight));

				if (width > 1) {
					if (j == 0) {
						int right = (i + 1) * width + j + 1;
						vgraph.addEdge(new DirectedEdge(source, right, weight));
					} else if (j == width - 1) {
						int left = (i + 1) * width + j - 1;
						vgraph.addEdge(new DirectedEdge(source, left, weight));
					} else {
						int right = (i + 1) * width + j + 1;
						int left = (i + 1) * width + j - 1;
						vgraph.addEdge(new DirectedEdge(source, right, weight));
						vgraph.addEdge(new DirectedEdge(source, left, weight));
					}
				}
			}
		}
	}

	private void build_hgraph() {
		int width = mypic.width();
		int height = mypic.height();
		int num_vertices = width * height;
		hgraph = new EdgeWeightedDigraph(num_vertices);

		for (int i = 0; i < width - 1; i++) {
			for (int j = 0; j < height; j++) {
				int source = i * height + j;
				int right = (i + 1) * height + j;
				double weight = energy(i, j);
				hgraph.addEdge(new DirectedEdge(source, right, weight));

				if (height > 1) {
					if (j == 0) {
						int down = (i + 1) * height + j + 1;
						hgraph.addEdge(new DirectedEdge(source, down, weight));
					} else if (j == height - 1) {
						int up = (i + 1) * height + j - 1;
						hgraph.addEdge(new DirectedEdge(source, up, weight));
					} else {
						int down = (i + 1) * height + j + 1;
						int up = (i + 1) * height + j - 1;
						hgraph.addEdge(new DirectedEdge(source, down, weight));
						hgraph.addEdge(new DirectedEdge(source, up, weight));
					}
				}
			}
		}
	}

	public Picture picture() {
		// current picture
		return mypic;
	}

	public int width() {
		// width of current picture
		return mypic.width();
	}

	public int height() {
		// height of current picture
		return mypic.height();
	}

	public double energy(int x, int y) {
		// energy of pixel at column x and row y
		if (x < 0 || x >= mypic.width() || y < 0 || y >= mypic.height()) {
			throw new IllegalArgumentException("Invalid pixel coordinate");
		}

		if (x == 0 || x == mypic.width() - 1 || y == 0 || y == mypic.height() - 1) {
			return BORDER_ENERGY;
		}

		Color xNext = mypic.get(x + 1, y);
		Color xPrev = mypic.get(x - 1, y);
		Color yNext = mypic.get(x, y + 1);
		Color yPrev = mypic.get(x, y - 1);

		double deltaX = Math.pow(xNext.getRed() - xPrev.getRed(), 2)
				+ Math.pow(xNext.getGreen() - xPrev.getGreen(), 2) + Math.pow(xNext.getBlue() - xPrev.getBlue(), 2);
		double deltaY = Math.pow(yNext.getRed() - yPrev.getRed(), 2)
				+ Math.pow(yNext.getGreen() - yPrev.getGreen(), 2) + Math.pow(yNext.getBlue() - yPrev.getBlue(), 2);

		return Math.sqrt(deltaX + deltaY);
	}

	public int[] findHorizontalSeam() {
		// sequence of indices for horizontal seam
		int width = mypic.width();
		int height = mypic.height();
		double min_dist = Double.POSITIVE_INFINITY;
		Iterable<DirectedEdge> min_path = null;
		for (int i = 0; i < height; i++) {
			int source = i;
			for (int j = 0; j < height; j++) {
				int target = height * (width - 1) + j;
				double dist = hapsp.dist(source, target);
				if (dist < min_dist) {
					min_dist = dist;
					min_path = hapsp.path(source, target);
				}
			}
		}

		int[] path = new int[width];
		int idx = 0;
		for (DirectedEdge e : min_path) {
			int from = e.from();
			int to = e.to();
			if (idx == width - 2) {
				path[idx] = from % height;
				path[idx + 1] = to % height;
			} else {
				path[idx] = from % height;
			}
			idx++;
		}

		return path;
	}

	public int[] findVerticalSeam() {
		// sequence of indices for vertical seam
		int width = mypic.width();
		int height = mypic.height();
		double min_dist = Double.POSITIVE_INFINITY;
		Iterable<DirectedEdge> min_path = null;
		for (int i = 0; i < width; i++) {
			int source = i;
			for (int j = 0; j < width; j++) {
				int target = width * (height - 1) + j;
				double dist = vapsp.dist(source, target);
				if (dist < min_dist) {
					min_dist = dist;
					min_path = vapsp.path(source, target);
				}
			}
		}

		int[] path = new int[height];
		int idx = 0;
		for (DirectedEdge e : min_path) {
			int from = e.from();
			int to = e.to();
			if (idx == height - 2) {
				path[idx] = from % width;
				path[idx + 1] = to % width;
			} else {
				path[idx] = from % width;
			}
			idx++;
		}

		return path;
	}

	public void removeHorizontalSeam(int[] seam) {
		// remove horizontal seam from current picture
		if (seam == null || seam.length != mypic.width() || mypic.height() <= 1) {
			throw new IllegalArgumentException("Invalid horizontal seam");
		}

		for (int i = 0; i < seam.length; i++) {
			if (seam[i] < 0 || seam[i] >= mypic.height()) {
				throw new IllegalArgumentException("Invalid horizontal seam");
			}
			if (i < seam.length - 1) {
				if (Math.abs(seam[i] - seam[i + 1]) > 1) {
					throw new IllegalArgumentException("Invalid horizontal seam");
				}
			}
		}

		int width = mypic.width();
		int height = mypic.height() - 1;
		Picture pic = new Picture(width, height);
		for (int i = 0; i < width; i++) {
			int del_pixel = seam[i];
			for (int j = 0; j < height; j++) {
				if (j < del_pixel) {
					pic.setRGB(i, j, mypic.getRGB(i, j));
				} else {
					pic.setRGB(i, j, mypic.getRGB(i, j + 1));
				}
			}
		}
		mypic = pic;
		build_vgraph();
		vapsp = new DijkstraAllPairsSP(vgraph);
		build_hgraph();
		hapsp = new DijkstraAllPairsSP(hgraph);
	}

	public void removeVerticalSeam(int[] seam) {
		// remove vertical seam from current picture
		if (seam == null || seam.length != mypic.height() || mypic.width() <= 1) {
			throw new IllegalArgumentException("Invalid vertical seam");
		}

		for (int i = 0; i < seam.length; i++) {
			if (seam[i] < 0 || seam[i] >= mypic.width()) {
				throw new IllegalArgumentException("Invalid vertical seam");
			}
			if (i < seam.length - 1) {
				if (Math.abs(seam[i] - seam[i + 1]) > 1) {
					throw new IllegalArgumentException("Invalid vertical seam");
				}
			}
		}

		int width = mypic.width() - 1;
		int height = mypic.height();
		Picture pic = new Picture(width, height);
		for (int i = 0; i < height; i++) {
			int del_pixel = seam[i];
			for (int j = 0; j < width; j++) {
				if (j < del_pixel) {
					pic.setRGB(j, i, mypic.getRGB(j, i));
				} else {
					pic.setRGB(j, i, mypic.getRGB(j + 1, i));
				}
			}
		}
		mypic = pic;
		build_vgraph();
		vapsp = new DijkstraAllPairsSP(vgraph);
		build_hgraph();
		hapsp = new DijkstraAllPairsSP(hgraph);
	}

}
