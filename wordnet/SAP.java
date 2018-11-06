
import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author sonndinh
 */
public class SAP {

    private final Digraph graph;
    //private final HashMap<Integer, BreadthFirstDirectedPaths> bfs_map;

    // constructor takes a digraph (not necessarily a DAG)    
    public SAP(Digraph G) {
        if (G == null) {
            throw new IllegalArgumentException("Input graph cannot be null");
        }
        graph = new Digraph(G);
        //bfs_map = new HashMap<>();
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        if (v < 0 || v >= graph.V() || w < 0 || w >= graph.V()) {
            throw new IllegalArgumentException("Invalid input vertices");
        }

        if (v == w) {
            // Distance to itself is 0
            return 0;
        }

        BreadthFirstDirectedPaths bfs_v = new BreadthFirstDirectedPaths(graph, v);
        BreadthFirstDirectedPaths bfs_w = new BreadthFirstDirectedPaths(graph, w);
        /*
        if (bfs_map.containsKey(v)) {
            bfs_v = bfs_map.get(v);
        } else {
            bfs_v = new BreadthFirstDirectedPaths(graph, v);
            bfs_map.put(v, bfs_v);
        }

        if (bfs_map.containsKey(w)) {
            bfs_w = bfs_map.get(w);
        } else {
            bfs_w = new BreadthFirstDirectedPaths(graph, w);
            bfs_map.put(w, bfs_w);
        }
        */
        
        int length = Integer.MAX_VALUE;
        for (int i = 0; i < graph.V(); i++) {
            if (bfs_v.hasPathTo(i) && bfs_w.hasPathTo(i)) {
                int tmp = bfs_v.distTo(i) + bfs_w.distTo(i);

                if (tmp < length) {
                    length = tmp;
                }
            }

        }

        if (length == Integer.MAX_VALUE) {
            return -1;
        } else {
            return length;
        }
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        if (v < 0 || v >= graph.V() || w < 0 || w >= graph.V()) {
            throw new IllegalArgumentException("Invalid input vertices");
        }

        if (v == w) {
            // Return itself as its ancestor
            return v;
        }

        BreadthFirstDirectedPaths bfs_v = new BreadthFirstDirectedPaths(graph, v);
        BreadthFirstDirectedPaths bfs_w = new BreadthFirstDirectedPaths(graph, w);
        /*
        if (bfs_map.containsKey(v)) {
            bfs_v = bfs_map.get(v);
        } else {
            bfs_v = new BreadthFirstDirectedPaths(graph, v);
            bfs_map.put(v, bfs_v);
        }

        if (bfs_map.containsKey(w)) {
            bfs_w = bfs_map.get(w);
        } else {
            bfs_w = new BreadthFirstDirectedPaths(graph, w);
            bfs_map.put(w, bfs_w);
        }
        */

        int length = Integer.MAX_VALUE;
        int ancest = -1;
        for (int i = 0; i < graph.V(); i++) {
            if (bfs_v.hasPathTo(i) && bfs_w.hasPathTo(i)) {
                int tmp = bfs_v.distTo(i) + bfs_w.distTo(i);

                if (tmp < length) {
                    length = tmp;
                    ancest = i;
                }
            }

        }

        return ancest;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        for (Integer a : v) {
            if (a == null) {
                throw new IllegalArgumentException("Input contains null item");
            }
        }

        for (Integer a : w) {
            if (a == null) {
                throw new IllegalArgumentException("Input contains null item");
            }
        }

        BreadthFirstDirectedPaths bfs_v = new BreadthFirstDirectedPaths(graph, v);
        BreadthFirstDirectedPaths bfs_w = new BreadthFirstDirectedPaths(graph, w);

        int length = Integer.MAX_VALUE;
        for (int i = 0; i < graph.V(); i++) {
            if (bfs_v.hasPathTo(i) && bfs_w.hasPathTo(i)) {
                int tmp = bfs_v.distTo(i) + bfs_w.distTo(i);

                if (tmp < length) {
                    length = tmp;
                }
            }
        }

        if (length == Integer.MAX_VALUE) {
            return -1;
        } else {
            return length;
        }
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        for (Integer a : v) {
            if (a == null) {
                throw new IllegalArgumentException("Input contains null item");
            }
        }

        for (Integer a : w) {
            if (a == null) {
                throw new IllegalArgumentException("Input contains null item");
            }
        }

        BreadthFirstDirectedPaths bfs_v = new BreadthFirstDirectedPaths(graph, v);
        BreadthFirstDirectedPaths bfs_w = new BreadthFirstDirectedPaths(graph, w);

        int length = Integer.MAX_VALUE;
        int ancest = -1;
        for (int i = 0; i < graph.V(); i++) {
            if (bfs_v.hasPathTo(i) && bfs_w.hasPathTo(i)) {
                int tmp = bfs_v.distTo(i) + bfs_w.distTo(i);

                if (tmp < length) {
                    length = tmp;
                    ancest = i;
                }
            }
        }
        
        return ancest;
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            StdOut.printf("Source node: %d, Sink node: %d\n", v, w);
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}
