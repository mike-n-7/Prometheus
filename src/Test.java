
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Vector;
import java.util.Random;

/* Here we implement a basic ACO algorithm where the ant will try to navigate towards the food source until it converges upon a solution. */
public class Test {
	public enum EXPLORE_MODE { CONVERGE, ITERATIONS };
	
	public static void main(String args[]) throws InterruptedException, FileNotFoundException, UnsupportedEncodingException {
		boolean verboseMode = false; // If verbose mode is on, then we print out the map after every move.
		int memDepth = 1; // Keep track of how many positions the ant should remember.
		EXPLORE_MODE expMode = EXPLORE_MODE.CONVERGE;
		int convergenceLimit = 5;
		int numIterations = 50;
		String map = "map1.txt";
		
		String input = "";
		Scanner scanner = new Scanner(System.in);
		while (!input.equals("9")) {
			System.out.println();
			System.out.println("--MENU--");
			System.out.println();
			System.out.println("1. Change memory depth. (Current: " + memDepth + ")");
			if (verboseMode == true) System.out.println("2. Turn verbose mode off (Only show final path/pheromone map).");
			else System.out.println("2. Turn verbose mode on (Show pheromone map/path after each iteration).");
			System.out.println("3. Change convergence limit. (Current: " + convergenceLimit + ")");
			System.out.println("4. Run convergence experiment (single run).");
			System.out.println("5. Change number of iterations (ant sent out). (Current: " + numIterations + ")");
			System.out.println("6. Run iterations experiment (single run).");
			System.out.println("7. Average convergence experiment.");
			System.out.println("8. Change map. (Current: " + map + ")");
			System.out.println("9. Quit.");
			
			System.out.print("-> ");
			input = scanner.nextLine();
			
			System.out.println();
			
			if (input.equals("1")) {
				System.out.print("Enter the new memory depth: ");
				memDepth = scanner.nextInt();
			} else if (input.equals("2")) {
				if (verboseMode == false) verboseMode = true;
				else verboseMode = false;
			} else if (input.equals("3")) {
				System.out.print("Enter a new convergence limit: ");
				convergenceLimit = scanner.nextInt();
			} else if (input.equals("4")) {
				expMode = EXPLORE_MODE.CONVERGE;
				System.out.println("Number ants for convergence: " + findPath(memDepth, expMode, convergenceLimit, numIterations, verboseMode));
			} else if (input.equals("5")) {
				System.out.print("Enter number of iterations: ");
				numIterations = scanner.nextInt();
			} else if (input.equals("6")) {
				expMode = EXPLORE_MODE.ITERATIONS;
				System.out.println("Number ants for convergence: " + findPath(memDepth, expMode, convergenceLimit, numIterations, verboseMode));
			} else if (input.equals("7")) {
				expMode = EXPLORE_MODE.CONVERGE;
				int total = 0;
				for (int i = 0; i < 1000; i++) {
					int current = findPath(memDepth, expMode, convergenceLimit, numIterations, verboseMode);
					total += current;
				}
				double avg = (double)total/1000.0;
				System.out.println("Number ants for convergence: " + avg);

			} else if (input.equals("8")) {
				System.out.print("Enter number name of the map file: ");
				map = scanner.nextLine();
			}
		}
		
	}
	
	public static int findPath(int memDepth, EXPLORE_MODE mode, int convergeLimit, int numIterationsLimit,boolean verbose) throws InterruptedException {
		Map map = new Map("map5.txt");
		// Count the number of time the ant explores the map successfully vs. unsuccessfully.
		int numSuccess = 0;
		int numFailure = 0;
		
		Ant.PROCESS_RESULT success = Ant.PROCESS_RESULT.UNKNOWN;
		// We will count how many iterations it takes for the ants to converge to a single solution. The following variable is how many consecutive solutions we need to say it has converged.
		int converge = 0;
		int numIterations = 0;
		Vector<Point2D> prevPath = new Vector<Point2D>();
		while (true) {
			numIterations++;
			if (mode == EXPLORE_MODE.CONVERGE && converge > convergeLimit) break;
			if (mode == EXPLORE_MODE.ITERATIONS && numIterations > numIterationsLimit) break;
			success = Ant.PROCESS_RESULT.UNKNOWN;
			
			// Send out a new ant and record its path.
			Vector<Point2D> path = new Vector<Point2D>();
			Ant ant = new Ant((int)map.getStart().getX(), (int)map.getStart().getY(), Ant.DIRECTION.EAST, map, memDepth);
			
			// Wait until the ant either hits a dead end or find the food.
			while (success == Ant.PROCESS_RESULT.UNKNOWN) { 
				if (verbose) {
					map.printMap(ant);
					Thread.sleep(100);
				}
				success = ant.process(); 
				Point2D newPosition = new Point();
				newPosition.setLocation(ant.getPosition());
				path.add(newPosition);
			}

			// If the ant has failed to reach the goal, record that and update the pheromones accordingly.
			if (success == Ant.PROCESS_RESULT.FAILURE) { 
				numFailure++; 
				map.updatePheromone(path, false); 
				converge = 0; 
			} else { 
				numSuccess++; 
				map.updatePheromone(path, true); 
				
				// Check if same as previous path.
				for (Point2D p : path) {
					if (prevPath.indexOf(p) != path.indexOf(p)) {
						converge = 0;
						break;
					}
					converge++;
				}
				// Record the current path as previous path.
				prevPath.clear();
				for (Point2D p : path) {
					Point2D newPoint = new Point();
					newPoint.setLocation(p);
					prevPath.add(newPoint);
				}
			}
			ant = null;
		}
		// Print the "stable" pheromone map and resulting path.
		map.printPath(prevPath);
		map.printPheromoneMap();
		
		return prevPath.size();
		//return numFailure + numSuccess;
	}
}

class Ant {
	public Point2D position = new Point();
	private DIRECTION direction;
	private Map map;
	
	// Complete will be set to true when either the ant has found food, or can no longer move. 
	private boolean complete = false; 
	
	public enum DIRECTION { NORTH, EAST, SOUTH,  WEST };
	public enum TURN_DIR { LEFT, RIGHT };
	public enum PROCESS_RESULT { SUCCESS, FAILURE, UNKNOWN };
	
	Random rand = new Random();
	
	Memory memory;
	
	// An ant is created by passing 
	public Ant(int x, int y, DIRECTION dir, Map env, int memDepth) {
		this.map = env;
		this.direction = dir;
		this.position.setLocation(x, y);
		this.memory =  new Memory("aco_knowledge_nodes.txt", memDepth);
	}
	
	// Preprocesses the environment to see which directions it can move and gets pheromone prob. levels for each direction.
	// Loads previous positions from memory to see if it can travel to the previous position. 
	// Based on pheromone levels and valid movements, apply rule.
	public PROCESS_RESULT process() {
		// Check which directions we can move.
		Vector env = getValidDirs();
		LinkedList<String> subs = getSubstitutions();
		double[] pheromones = getPheromones();
		
		this.memory.Process(env, subs, pheromones);
		
		if (env.contains("(#TURN LD)")) this.turn(TURN_DIR.LEFT);
		if (env.contains("(#TURN RD)")) this.turn(TURN_DIR.RIGHT);
		if (env.contains("(#WALK)")) this.walk();

		if (this.map.isFood((int)this.position.getX(), (int)this.position.getY())) return PROCESS_RESULT.SUCCESS;
		if (!env.contains("(#WALK)")) return PROCESS_RESULT.FAILURE;
		
		else return PROCESS_RESULT.UNKNOWN;
	}
	
	private double[] getPheromones() {
		double[] p = new double[3];
		switch (this.direction) {
		case NORTH: 
			// Forward.
			p[0] = map.getPheromone((int)this.position.getX(), (int)this.position.getY() - 1);
			// Left.
			p[1] = map.getPheromone((int)this.position.getX() - 1, (int)this.position.getY());
			// Right.
			p[2] = map.getPheromone((int)this.position.getX() + 1, (int)this.position.getY());
			break;
		case SOUTH:
			// Forward.
			p[0] = map.getPheromone((int)this.position.getX(), (int)this.position.getY() + 1);
			// Left.
			p[1] = map.getPheromone((int)this.position.getX() + 1, (int)this.position.getY());
			// Right.
			p[2] = map.getPheromone((int)this.position.getX() - 1, (int)this.position.getY());
			break;
		case EAST:
			//Forward.
			p[0] = map.getPheromone((int)this.position.getX() + 1, (int)this.position.getY());
			// Left.
			p[1] = map.getPheromone((int)this.position.getX(), (int)this.position.getY() - 1 );
			// Right.
			p[2] = map.getPheromone((int)this.position.getX(), (int)this.position.getY() + 1);
			break;
		case WEST:
			// Forward.
			p[0] = map.getPheromone((int)this.position.getX() - 1, (int)this.position.getY());
			// Left.
			p[1] = map.getPheromone((int)this.position.getX(), (int)this.position.getY() + 1);
			// Right.
			p[2] = map.getPheromone((int)this.position.getX(), (int)this.position.getY() -1);
			break;
		}
		return p;
	}
	
	public Vector<Point2D> getMemoryPositions() {		
		return this.memory.getPositions();
	}
	
	private Vector getValidDirs() {
		Vector env = new Vector();
		switch (this.direction) {
		case NORTH: 
			// Forward.
			if (map.walkable((int)this.position.getX(), (int)this.position.getY() - 1)) {
				env.add("(CAN WALK F)");
			}
			// Left.
			if (map.walkable((int)this.position.getX() - 1, (int)this.position.getY())) {
				env.add("(CAN WALK LD)");
			}
			// Right.
			if (map.walkable((int)this.position.getX() + 1, (int)this.position.getY())) {
				env.add("(CAN WALK RD)");
			}
			break;
		case SOUTH:
			// Forward.
			if (map.walkable((int)this.position.getX(), (int)this.position.getY() + 1)) {
				env.add("(CAN WALK F)");
			} // Left.
			if (map.walkable((int)this.position.getX() + 1, (int)this.position.getY())) {
				env.add("(CAN WALK LD)");
			}
			// Right.
			if (map.walkable((int)this.position.getX() - 1, (int)this.position.getY())) {
				env.add("(CAN WALK RD)");
			}
			break;
		case EAST:
			//Forward.
			if (map.walkable((int)this.position.getX() + 1, (int)this.position.getY())) {
				env.add("(CAN WALK F)");
			}
			// Left.
			if (map.walkable((int)this.position.getX(), (int)this.position.getY() - 1 )) {
				env.add("(CAN WALK LD)");
			}
			// Right.
			if (map.walkable((int)this.position.getX(), (int)this.position.getY() + 1)) {
				env.add("(CAN WALK RD)");
			}
			break;
		case WEST:
			// Forward.
			if (map.walkable((int)this.position.getX() - 1, (int)this.position.getY())) {
				env.add("(CAN WALK F)");
			}
			// Left.
			if (map.walkable((int)this.position.getX(), (int)this.position.getY() + 1)) {
				env.add("(CAN WALK LD)");			
			}
			// Right.
			if (map.walkable((int)this.position.getX(), (int)this.position.getY() -1)) {
				env.add("(CAN WALK RD)");
			}
			break;
		}
		return env;
	}
	
	private LinkedList<String> getSubstitutions() {
		LinkedList<String> subs = new LinkedList<String>();
		switch (this.direction) {
		case NORTH: 
			// Forward.
			if (map.walkable((int)this.position.getX(), (int)this.position.getY() - 1)) {
				subs.add("FX " + (int)this.position.getX());
				subs.add("FY " + (int)(this.position.getY() - 1));
			}
			// Left.
			if (map.walkable((int)this.position.getX() - 1, (int)this.position.getY())) {
				subs.add("LDX " + (int)(this.position.getX() - 1));
				subs.add("LDY " + (int)(this.position.getY()));
			}
			// Right.
			if (map.walkable((int)this.position.getX() + 1, (int)this.position.getY())) {
				subs.add("RDX " + (int)(this.position.getX() + 1));
				subs.add("RDY " + (int)(this.position.getY()));
			}
			break;
		case SOUTH:
			// Forward.
			if (map.walkable((int)this.position.getX(), (int)this.position.getY() + 1)) {
				subs.add("FX " + (int)this.position.getX());
				subs.add("FY " + (int)(this.position.getY() + 1));
			} // Left.
			if (map.walkable((int)this.position.getX() + 1, (int)this.position.getY())) {
				subs.add("LDX " + (int)(this.position.getX() + 1));
				subs.add("LDY " + (int)(this.position.getY()));
			}
			// Right.
			if (map.walkable((int)this.position.getX() - 1, (int)this.position.getY())) {
				subs.add("RDX " + (int)(this.position.getX() - 1));
				subs.add("RDY " + (int)(this.position.getY()));
			}
			break;
		case EAST:
			//Forward.
			if (map.walkable((int)this.position.getX() + 1, (int)this.position.getY())) {
				subs.add("FX " + (int)(this.position.getX() + 1));
				subs.add("FY " + (int)(this.position.getY()));
			}
			// Left.
			if (map.walkable((int)this.position.getX(), (int)this.position.getY() -1 )) {
				subs.add("LDX " + (int)(this.position.getX()));
				subs.add("LDY " + (int)(this.position.getY() - 1));
			}
			// Right.
			if (map.walkable((int)this.position.getX(), (int)this.position.getY() + 1)) {
				subs.add("RDX " + (int)(this.position.getX()));
				subs.add("RDY " + (int)(this.position.getY() + 1));
			}
			break;
		case WEST:
			// Forward.
			if (map.walkable((int)this.position.getX() - 1, (int)this.position.getY())) {
				subs.add("FX " + (int)(this.position.getX() - 1));
				subs.add("FY " + (int)(this.position.getY()));
			}
			// Left.
			if (map.walkable((int)this.position.getX(), (int)this.position.getY() + 1)) {
				subs.add("LDX " + (int)(this.position.getX()));
				subs.add("LDY " + (int)(this.position.getY() + 1));			
			}
			// Right.
			if (map.walkable((int)this.position.getX(), (int)this.position.getY() - 1)) {
				subs.add("RDX " + (int)(this.position.getX()));
				subs.add("RDY " + (int)(this.position.getY() - 1));
			}
			break;
		}
		return subs;
	}
	
	// Make the ant turn based on it's current direction.
	private void turn(TURN_DIR dir) {
		if (dir == TURN_DIR.LEFT) {
			switch (this.direction) {
			case NORTH: this.direction = DIRECTION.WEST; break;
			case WEST: this.direction = DIRECTION.SOUTH; break;
			case SOUTH: this.direction = DIRECTION.EAST; break;
			case EAST: this.direction = DIRECTION.NORTH; break;
			}
		} else {
			switch (this.direction) {
			case NORTH: this.direction = DIRECTION.EAST; break;
			case WEST: this.direction = DIRECTION.NORTH; break;
			case SOUTH: this.direction = DIRECTION.WEST; break;
			case EAST: this.direction = DIRECTION.SOUTH; break;
			}
		}
	}
	
	public Point2D getPosition() {
		return this.position;
	}
	// Move forward one cell. Return true if we are allowed to move forward, false otherwise.
	private boolean walk() {
		switch (this.direction) {
		case NORTH: 
			if (map.walkable((int)this.position.getX(), (int)this.position.getY() - 1)) {
				this.position.setLocation(this.position.getX(), this.position.getY() - 1);
				return true;
			}
			break;
		case SOUTH:
			if (map.walkable((int)this.position.getX(), (int)this.position.getY() + 1)) {
				this.position.setLocation(this.position.getX(), this.position.getY() + 1);
				return true;
			}
			break;
		case EAST:
			if (map.walkable((int)this.position.getX() + 1, (int)this.position.getY())) {
				this.position.setLocation(this.position.getX() + 1, this.position.getY());
				return true;
			}
			break;
		case WEST:
			if (map.walkable((int)this.position.getX() - 1, (int)this.position.getY())) {
				this.position.setLocation(this.position.getX() - 1, this.position.getY());
				return true;
			}
			break;
		}
		return false;
	}
}

class Map {
	private double[][] pheromoneMap; //TODO: Implement the pheromone map.
	private char[][] map;
	private Point2D start = new Point();
	private Point2D finish = new Point();
	private Point2D size = new Point();
	
	// Parameters that effect how we update the pheromone values.
	private double initialPharomoneValue = 1.0;
	private double cDecay = 0.1;
	private double cSuccess = 10.0;
	private double cFailure = 0.5;
	
	public Map(String f) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			// The first line of the map file contains the size of the map in format "sizeX sizeY".
			String line = reader.readLine();
			double sizeX = Double.parseDouble(line.split(" ")[0]);
			double sizeY = Double.parseDouble(line.split(" ")[1]);
			this.size.setLocation(sizeX, sizeY);
			// The rest of the map is the grid.
			map = new char[(int)size.getY()][(int)size.getX()];
			initPheromoneLevels();
			for (int i = 0; i < size.getY(); i++) {
				line = reader.readLine();
				if (line == null) break;
				
				for (int j = 0; j < size.getX(); j++) {
					if (line.charAt(j) == 'h') {	// Set the starting position for the ants to spawn.
						start.setLocation(j, i);
					} else if (line.charAt(j) == 'f') {	 // Set the goal position the will mark a successful navigation of the map.
						finish.setLocation(j, i);
					} 
					map[i][j] = line.charAt(j);
				}
			}
		} catch (IOException e) {
			System.out.println("IOException!");
		} 
	}
	
	public boolean isFood(int x, int y) {
		if (map[y][x] == 'f') return true;
		else return false;
	}
	// We want at the pheromone paths to have a default value > 0 so that initial movements will not have probability 0.
	private void initPheromoneLevels() {
		pheromoneMap = new double[(int)size.getY()][(int)size.getX()];
		for (int i = 0; i < size.getY(); i++) {
			for (int j = 0; j < size.getX(); j++) {
				pheromoneMap[i][j] = initialPharomoneValue;
			}
		}
	}
	
	// Update every pheromone value based on whether the path travelled was a success of a failure.
	// Every cell decayed: t_ij = t_ij (1-p) where p is a constant parameter.
	// If path is a success, for each cell on the path, t_ij = t_ij + Q/l where Q is a constant parameter and l is the length of the path.
	// If the path is a failure, reduce the pheromone level of cells in second half of path by a constant parameter.
	public void updatePheromone(Vector<Point2D> path, boolean success) {
		// First decay every cell.
		for (int i = 0; i < size.getY(); i++) {
			for (int j = 0; j < size.getX(); j++) {
				pheromoneMap[i][j] = pheromoneMap[i][j] * (1.0 - cDecay);
			}
		}
		// Next update the pheromones on the path.
		for (int i = 0; i < path.size(); i++) {
			Point2D p = path.elementAt(i);
			if (success == true) {
				pheromoneMap[(int)p.getY()][(int)p.getX()] = pheromoneMap[(int)p.getY()][(int)p.getX()] + cSuccess/(double) path.size();
			} else {
				if (i > path.size()/2) pheromoneMap[(int)p.getY()][(int)p.getX()] = pheromoneMap[(int)p.getY()][(int)p.getX()] * cFailure;
			}
		}
	}
	
	// Return the start position as a point.
	public Point2D getStart() {
		return start;
	}
	
	public void printPath(Vector<Point2D> path) {
		System.out.println();
		for (int i = 0; i < size.getY(); i++) {
			for (int j = 0; j < size.getX(); j++) {
				// Print the ant as an X.
				boolean onPath = false;
				for (Point2D p : path) {
					if (p.getX() == j && p.getY() == i) {
						onPath = true;
						System.out.print("X");
						break;
					}
				}
				if (!onPath) {
					if (map[i][j] != 'g') System.out.print(map[i][j]);
					else System.out.print(" ");
				}
			}
			System.out.println();
		}
	}
	// Prints the map showing given the current position of the ant.
	public void printMap(Ant ant) {
		int x = (int)ant.getPosition().getX();
		int y = (int)ant.getPosition().getY();
		
		Vector<Point2D> memory = ant.getMemoryPositions();
		
		System.out.println();
		for (int i = 0; i < size.getY(); i++) {
			for (int j = 0; j < size.getX(); j++) {
				// Print the ant as an X.
				if (y == i && x == j) System.out.print("X");
				else {
					boolean inMem = false;
					for (Point2D p : memory) {
						if ((int)p.getX() == j && (int) p.getY() == i){
							System.out.print('O');
							inMem = true;
							break; 
						}
					}
					
					if (!inMem && map[i][j] != 'g') System.out.print(map[i][j]);
					else if (!inMem) System.out.print(" ");
				}
			}
			System.out.println();
		}
		System.out.println();
	}
	public void printPheromoneMap() {
		System.out.println();
		for (int i = 0; i < size.getY(); i++) {
			for (int j = 0; j < size.getX(); j++) {
				DecimalFormat df = new DecimalFormat("#.###");
				System.out.print(df.format(pheromoneMap[i][j]) + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public double getPheromone(int x, int y) {
		if (x < 0 || y < 0 || x >= size.getX() || y >= size.getY()) return 0.0;
		else return pheromoneMap[y][x];
	}
	/// Given an (x, y) location, check if it's valid for the ant to move to this cell.
	public boolean walkable(int x, int y) {
		// Check array bounds.
		if (x < 0 || y < 0 || x >= size.getX() || y >= size.getY()) return false;
		// Check obstacles.
		else if (map[y][x] == 'w') return false;
		else return true;
	}
}