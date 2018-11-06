/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import java.util.Scanner;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author sonndinh
 */
public class WordNet {

    private final Digraph wordnet;
    private final HashMap<Integer, ArrayList<String>> synsets_map; // map from synset id to synonym set
    private final HashSet<String> nouns_set; // set of all nouns
    private final SAP sap; // An SAP object for this wordnet graph
    private final HashMap<String, HashSet<Integer>> nouns_map; // a cache that map from a noun to set of synsets contain it

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        synsets_map = new HashMap<>();
        nouns_set = new HashSet<>();
        nouns_map = new HashMap<>();

        int vertices_num = read_synsets(synsets);

        // Initialize a digraph with a specified number of vertices
        wordnet = new Digraph(vertices_num);

        // This will add edges to the wordnet graph
        read_hypernyms(hypernyms);

        // Check if the wordnet is a rooted dag
        if (has_cycle()) {
            throw new IllegalArgumentException("Input wordnet is not a DAG");
        }

        if (!is_rooted()) {
            throw new IllegalArgumentException("Input wordnet is not a rooted DAG");
        }

        // Gather the set of nouns
        for (int i = 0; i < synsets_map.size(); i++) {
            for (String noun : synsets_map.get(i)) {
                nouns_set.add(noun);
            }
        }

        sap = new SAP(wordnet);
    }

    private int read_synsets(String synsets) {
        In syn_in = new In(synsets);
        int vertices_num = 0;

        while (syn_in.hasNextLine()) {
            vertices_num++;
            String line = syn_in.readLine();
            String[] items = line.split(",");
            int id = Integer.parseInt(items[0]);

            ArrayList<String> nouns = new ArrayList<>();
            String[] synonyms = items[1].split(" ");
            for (String syn : synonyms) {
                nouns.add(syn);
            }
            synsets_map.put(id, nouns);
        }

        return vertices_num;
    }

    private void read_hypernyms(String hypernyms) {
        In hyper_in = new In(hypernyms);

        while (hyper_in.hasNextLine()) {
            String line = hyper_in.readLine();
            String[] items = line.split(",");
            int id = Integer.parseInt(items[0]);

            for (int i = 1; i < items.length; i++) {
                int target = Integer.parseInt(items[i]);
                wordnet.addEdge(id, target);
            }
        }
    }

    private int read_synsets_slow(String synsets) {
        In syn_in = new In(synsets);
        // To scan each line of the input files
        In scan;
        int vertices_num = 0;

        // Read the synset file line by line
        while (syn_in.hasNextLine()) {
            vertices_num++;
            String line = syn_in.readLine();
            scan = new In(new Scanner(line));

            // Read synset id from the line
            String id_str = "";
            while (scan.hasNextChar()) {
                String c = String.valueOf(scan.readChar());
                if (c.equals(",") || c.equals(" ")) {
                    break;
                } else {
                    id_str = id_str.concat(c);
                }
            }
            int id = Integer.parseInt(id_str);

            // Read synonym set from the line
            ArrayList<String> nouns = new ArrayList<>();
            String syn = "";
            while (scan.hasNextChar()) {
                String c = String.valueOf(scan.readChar());
                if (c.equals(",")) {
                    nouns.add(syn);
                    break;
                } else if (c.equals(" ")) {
                    nouns.add(syn);
                    syn = "";
                } else {
                    syn = syn.concat(c);
                }
            }
            synsets_map.put(id, nouns);
            scan.close();
        }
        return vertices_num;
    }

    private void read_hypernyms_slow(String hypernyms) {
        In hyper_in = new In(hypernyms);
        // To scan each line of the input files
        In scan;

        // Read the hypernyms file line by line
        while (hyper_in.hasNextLine()) {
            String line = hyper_in.readLine();
            scan = new In(new Scanner(line));

            // Read synset id from the line
            String id_str = "";
            while (scan.hasNextChar()) {
                String c = String.valueOf(scan.readChar());
                if (c.equals(",") || c.equals(" ")) {
                    break;
                } else {
                    id_str = id_str.concat(c);
                }
            }
            int id = Integer.parseInt(id_str);

            // Read hypernym set from the same line
            String hypernym_id_str = "";
            while (scan.hasNextChar()) {
                String c = String.valueOf(scan.readChar());
                if (c.equals(",") || c.equals(" ") || c.equals("\n")) {
                    if (!hypernym_id_str.isEmpty()) {
                        int hypernym_id = Integer.parseInt(hypernym_id_str);
                        wordnet.addEdge(id, hypernym_id);
                        hypernym_id_str = "";
                    }
                } else {
                    hypernym_id_str = hypernym_id_str.concat(c);
                }
            }
            if (!hypernym_id_str.isEmpty()) {
                int hypernym_id = Integer.parseInt(hypernym_id_str);
                wordnet.addEdge(id, hypernym_id);
            }
            scan.close();
        }
    }

    private boolean has_cycle() {
        DirectedCycle cycle_check = new DirectedCycle(wordnet);
        return cycle_check.hasCycle();
    }

    private boolean is_rooted() {
        // There must be exactly 1 vertex with out-degree of 0
        int root_count = 0;
        for (int i = 0; i < wordnet.V(); i++) {
            if (wordnet.outdegree(i) == 0) {
                root_count++;
            }
        }

        return root_count == 1;
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return nouns_set;
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        return nouns_set.contains(word);
    }
    
    // A class for encapsulating sets of synsets containing nounA and nounB
    private class EncapData {
        public final HashSet<Integer> a_set, b_set;
        
        public EncapData(HashSet<Integer> first, HashSet<Integer> second) {
            a_set = first;
            b_set = second;
        }
    }

    private EncapData cache_helper(String nounA, String nounB) {
        // Set of synset ids that contain nounA (nounB)
        // Read from the cache if possible
        HashSet<Integer> a_set, b_set;
        boolean compute_a = false;
        boolean compute_b = false;
        if (nouns_map.containsKey(nounA)) {
            a_set = nouns_map.get(nounA);
        } else {
            a_set = new HashSet<>();
            compute_a = true;
        }
        
        if (nouns_map.containsKey(nounB)) {
            b_set = nouns_map.get(nounB);
        } else {
            b_set = new HashSet<>();
            compute_b = true;
        }
        
        //HashSet<Integer> a_set = new HashSet<>();
        //HashSet<Integer> b_set = new HashSet<>();
        for (Integer id : synsets_map.keySet()) {
            if (compute_a == true && synsets_map.get(id).contains(nounA)) {
                a_set.add(id);
            }
            if (compute_b == true && synsets_map.get(id).contains(nounB)) {
                b_set.add(id);
            }
        }
        
        // Cache the new computed results
        if (compute_a == true) {
            nouns_map.put(nounA, a_set);
        }
        if (compute_b == true) {
            nouns_map.put(nounB, b_set);
        }
        
        return new EncapData(a_set, b_set);
    }
    
    
    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (nounA == null || nounB == null || !nouns_set.contains(nounA) || !nouns_set.contains(nounB)) {
            throw new IllegalArgumentException("Invalid input nouns");
        }
        
        if (nounA.equals(nounB)) {
            return 0;
        }

        EncapData data = cache_helper(nounA, nounB);

        return sap.length(data.a_set, data.b_set);
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (nounA == null || nounB == null || !nouns_set.contains(nounA) || !nouns_set.contains(nounB)) {
            throw new IllegalArgumentException("Invalid inut nouns");
        }

        /*
        // Set of synset ids that contain nounA (nounB)
        HashSet<Integer> a_set = new HashSet<>();
        HashSet<Integer> b_set = new HashSet<>();
        for (Integer id : synsets_map.keySet()) {
            if (synsets_map.get(id).contains(nounA)) {
                a_set.add(id);
            }
            if (synsets_map.get(id).contains(nounB)) {
                b_set.add(id);
            }
        }
        */
        
        EncapData data = cache_helper(nounA, nounB);
        
        int ancestor = sap.ancestor(data.a_set, data.b_set);
        if (ancestor == -1) {
            return null;
        }

        StringBuilder tmp = new StringBuilder();
        ArrayList<String> synset = synsets_map.get(ancestor);
        for (String syn : synset) {
            tmp.append(syn);
            tmp.append(" ");
        }
        return tmp.toString();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            //WordNet wn = new WordNet("wordnet/synsets500-subgraph.txt", "wordnet/hypernyms500-subgraph.txt");
            //int length = wn.distance("turning", "bagasse");
            WordNet wn = new WordNet("synsets.txt", "hypernyms.txt");
            int length = wn.distance("arrivederci", "order_Piciformes");
            StdOut.println("Distance: " + length);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

}
