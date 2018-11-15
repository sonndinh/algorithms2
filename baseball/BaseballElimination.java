import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.In;

public class BaseballElimination {
	private FordFulkerson[] ffs;
	private final int numTeams;
	private final ArrayList<String> teamNames;
	private final HashMap<String, Integer> idMap;
	private final int[] wins;
	private final int[] losses;
	private final int[] remains;
	private final int[][] games;

	public BaseballElimination(String filename) {
		// create a baseball division from given filename in format specified below
		if (filename == null) {
			throw new IllegalArgumentException("Incorrect input filename");
		}

		In in = new In(filename);
		numTeams = Integer.parseInt(in.readLine().trim().split("\\s+")[0]);
		if (numTeams < 1) {
			throw new IllegalArgumentException("There must be at least 1 team");
		}

		teamNames = new ArrayList<>(numTeams);
		wins = new int[numTeams];
		losses = new int[numTeams];
		remains = new int[numTeams];
		games = new int[numTeams][numTeams];
		idMap = new HashMap<>();

		for (int i = 0; i < numTeams; i++) {
			String line = in.readLine();
			String[] items = line.trim().split("\\s+");

			teamNames.add(items[0]);
			idMap.put(items[0], i);			
			wins[i] = Integer.parseInt(items[1]);
			losses[i] = Integer.parseInt(items[2]);
			remains[i] = Integer.parseInt(items[3]);
			for (int j = 0; j < numTeams; j++) {
				games[i][j] = Integer.parseInt(items[j + 4]);
			}
		}

		ffs = new FordFulkerson[numTeams];
	}

	public int numberOfTeams() {
		// number of teams
		return numTeams;
	}

	public Iterable<String> teams() {
		// all teams
		return teamNames;
	}

	public int wins(String team) {
		// number of wins for given team
		if (team == null || !idMap.containsKey(team)) {
			throw new IllegalArgumentException("Invalid team name");
		}

		return wins[idMap.get(team)];
	}

	public int losses(String team) {
		// number of losses for given team
		if (team == null || !idMap.containsKey(team)) {
			throw new IllegalArgumentException("Invalid team name");
		}

		return losses[idMap.get(team)];
	}

	public int remaining(String team) {
		// number of remaining games for given team
		if (team == null || !idMap.containsKey(team)) {
			throw new IllegalArgumentException("Invalid team name");
		}

		return remains[idMap.get(team)];
	}

	public int against(String team1, String team2) {
		// number of remaining games between team1 and team2
		if (team1 == null || team2 == null || !idMap.containsKey(team1) || !idMap.containsKey(team2)) {
			throw new IllegalArgumentException("Invalid team name");
		}

		return games[idMap.get(team1)][idMap.get(team2)];
	}

	public boolean isEliminated(String team) {
		// is given team eliminated?
		if (team == null || !idMap.containsKey(team)) {
			throw new IllegalArgumentException("Invalid team name");
		}

		int id = idMap.get(team);

		// Trivial case
		for (int i = 0; i < numTeams; i++) {
			if (wins[id] + remains[id] < wins[i]) {
				return true;
			}
		}

		// Non-trivial case
		Iterable<String> certificate = certificateOfElimination(team);
		if (certificate == null) {
			return false;
		}
		
		ArrayList<Integer> certIds = new ArrayList<>();
		for (String t : certificate) {
			certIds.add(idMap.get(t));
		}

		// Total capacity of the edges from team in the certificate to sink
		double totalCap = 0;
		for (int i : certIds) {
			totalCap += wins[id] + remains[id] - wins[i];
		}

		// Total number of games between teams in the certificate
		double totalGames = 0;
		for (int i = 0; i < certIds.size() - 1; i++) {
			for (int j = i + 1; j < certIds.size(); j++) {
				totalGames += games[certIds.get(i)][certIds.get(j)];
			}
		}

		if (totalGames > totalCap) {
			return true;
		}

		return false;
	}

	// Convert a team id i to its vertex id in flow network
	private int teamToVertex(int id, int i) {
		int teamVertex = 1 + (numTeams - 1) * (numTeams - 2) / 2;
		if (i > id) {
			return (teamVertex + i - 1);
		} else {
			return (teamVertex + i);
		}
	}

	public Iterable<String> certificateOfElimination(String team) {
		// subset R of teams that eliminates given team; null if not eliminated
		if (team == null || !idMap.containsKey(team)) {
			throw new IllegalArgumentException("Invalid team name");
		}

		int id = idMap.get(team);

		// Trivial case
		HashSet<String> ret = new HashSet<String>();
		for (int i = 0; i < numTeams; i++) {
			if (wins[id] + remains[id] < wins[i]) {
				ret.add(teamNames.get(i));
				return ret;
			}
		}

		// Non-trivial case
		if (ffs[id] == null) {
			buildFF(id);
		}

		for (int i = 0; i < numTeams; i++) {
			if (i != id) {
				int vertexId = teamToVertex(id, i);
				if (ffs[id].inCut(vertexId)) {
					ret.add(teamNames.get(i));
				}
			}
		}

		if (ret.isEmpty()) {
			return null;
		}
		return ret;
	}

	// Build a flow network for a team with a given id
	private void buildFF(int id) {
		// the first and last vertices are source and sink, respectively
		int numGamesVer = (numTeams - 1) * (numTeams - 2) / 2;
		int numVer = 2 + numGamesVer + numTeams - 1;
		FlowNetwork fn = new FlowNetwork(numVer);

		// Starting id of the game vertices
		int gameVertex = 1;

		// Add edges from source to game vertices
		for (int i = 0; i < numTeams - 1; i++) {
			for (int j = i + 1; j < numTeams; j++) {
				if (i != id && j != id) {
					FlowEdge sToGame = new FlowEdge(0, gameVertex, games[i][j]);
					fn.addEdge(sToGame);

					int first = teamToVertex(id, i);
					int second = teamToVertex(id, j);

					// Add edges from each game vertex to the two corresponding teams
					FlowEdge toFirst = new FlowEdge(gameVertex, first, Double.POSITIVE_INFINITY);
					FlowEdge toSecond = new FlowEdge(gameVertex, second, Double.POSITIVE_INFINITY);
					fn.addEdge(toFirst);
					fn.addEdge(toSecond);

					gameVertex++;
				}
			}
		}

		// Add edges from team vertices to sink
		for (int i = 0; i < numTeams; i++) {
			if (i != id) {
				int team = teamToVertex(id, i);

				double capacity = wins[id] + remains[id] - wins[i];
				FlowEdge toSink = new FlowEdge(team, numVer - 1, capacity);
				fn.addEdge(toSink);
			}
		}

		ffs[id] = new FordFulkerson(fn, 0, numVer - 1);
	}

	public static void main(String[] args) {
		BaseballElimination division = new BaseballElimination(args[0]);
		for (String team : division.teams()) {
			if (division.isEliminated(team)) {
				StdOut.print(team + " is eliminated by the subset R = { ");
				for (String t : division.certificateOfElimination(team)) {
					StdOut.print(t + " ");
				}
				StdOut.println("}");
			} else {
				StdOut.println(team + " is not eliminated");
			}
		}
	}
}
