
import edu.princeton.cs.algs4.In;
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
public class Outcast {

    private final WordNet wnet;

    public Outcast(WordNet wordnet) {
        // constructor takes a WordNet object
        wnet = wordnet;
    }

    public String outcast(String[] nouns) {
        // given an array of WordNet nouns, return an outcast
        int size = nouns.length;
        //StdOut.println("Length of input array: " + size);
        int[][] distances = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                } else {
                    distances[i][j] = Integer.MAX_VALUE;
                }
            }
        }

        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                distances[i][j] = wnet.distance(nouns[i], nouns[j]);
                distances[j][i] = distances[i][j];
                //StdOut.println("Distance[" + i + "," + j + "] = " + distances[i][j]);
            }
        }
        
        int max = 0;
        int outcast = -1;
        for (int i = 0; i < size; i++) {
            int sum = 0;
            for (int j = 0; j < size; j++) {
                sum += distances[i][j];
            }

            //StdOut.println("Distance for i=" + i + " is " + sum);
            
            if (sum > max) {
                max = sum;
                outcast = i;
            }
        }

        if (outcast != -1) {
            return nouns[outcast];
        }
        return null;
    }

    public static void main(String[] args) {
        // see test client below
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }

}
