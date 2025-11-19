import java.io.*;
import java.util.*;

public class Banker {
	public static final int NUMBER_OF_CUSTOMERS = 5;
	public static final int NUMBER_OF_RESOURCES = 4;

	private int[] available = new int[NUMBER_OF_RESOURCES];
	private int[][] maximum = new int[NUMBER_OF_CUSTOMERS][NUMBER_OF_RESOURCES];
	private int[][] allocation = new int[NUMBER_OF_CUSTOMERS][NUMBER_OF_RESOURCES];
	private int[][] need = new int[NUMBER_OF_CUSTOMERS][NUMBER_OF_RESOURCES];

	// Constructor: copy available vector and read maximums from file; initialize
	// allocation & need
	public Banker(int[] availableInit, String maxFilePath) throws IllegalArgumentException {
		if (availableInit == null || availableInit.length != NUMBER_OF_RESOURCES) {
			throw new IllegalArgumentException("available vector must have length " + NUMBER_OF_RESOURCES);
		}
		System.arraycopy(availableInit, 0, this.available, 0, NUMBER_OF_RESOURCES);

		if (!readMaximumFromFile(maxFilePath)) {
			throw new IllegalArgumentException("Failed to read max file: " + maxFilePath);
		}

		// allocation defaults to 0; compute need = maximum - allocation
		for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
			for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
				allocation[i][j] = 0;
				need[i][j] = maximum[i][j] - allocation[i][j];
			}
		}
	}

	public boolean readMaximumFromFile(String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			int row = 0;
			while ((line = br.readLine()) != null && row < NUMBER_OF_CUSTOMERS) {
				line = line.trim();
				if (line.isEmpty())
					continue;
				String[] toks = line.split("\\s*,\\s*");
				if (toks.length != NUMBER_OF_RESOURCES) {
					System.err.println("Invalid line in max file at row " + row + ": " + line);
					return false;
				}
				for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
					maximum[row][j] = Integer.parseInt(toks[j]);
				}
				row++;
			}
			if (row != NUMBER_OF_CUSTOMERS) {
				System.err.println("max file must contain " + NUMBER_OF_CUSTOMERS + " rows.");
				return false;
			}
			return true;
		} catch (IOException | NumberFormatException e) {
			System.err.println("Error reading max file: " + e.getMessage());
			return false;
		}
	}

	public int request_resources(int customer_num, int request[]) {
		// validate customer index
		if (customer_num < 0 || customer_num >= NUMBER_OF_CUSTOMERS)
			return -1;

		// check Request <= Need
		for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
			if (request[j] > need[customer_num][j])
				return -1; // exceeds declared maximum
		}
		// check Request <= Available
		for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
			if (request[j] > available[j])
				return -1; // not enough available
		}

		// pretend to allocate
		for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
			available[j] -= request[j];
			allocation[customer_num][j] += request[j];
			need[customer_num][j] -= request[j];
		}

		// check safety
		if (isSafe()) {
			return 0; // granted request/"safe"
		} else {
			// rollback
			for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
				available[j] += request[j];
				allocation[customer_num][j] -= request[j];
				need[customer_num][j] += request[j];
			}
			return -1; // denied request/"unsafe"
		}
	}

	public void release_resources(int customer_num, int release[]) {
		// validate customer index
		if (customer_num < 0 || customer_num >= NUMBER_OF_CUSTOMERS)
			return;

		// copy release to avoid mutating caller's array
		int[] rel = new int[NUMBER_OF_RESOURCES];
		System.arraycopy(release, 0, rel, 0, Math.min(release.length, NUMBER_OF_RESOURCES));

		// ensure not releasing more than allocated
		for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
			if (rel[j] > allocation[customer_num][j]) {
				rel[j] = allocation[customer_num][j];
			}
		}

		for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
			allocation[customer_num][j] -= rel[j];
			available[j] += rel[j];
			need[customer_num][j] += rel[j];
		}
	}

	public boolean isSafe() {
		int[] work = new int[NUMBER_OF_RESOURCES];
		boolean[] finish = new boolean[NUMBER_OF_CUSTOMERS];
		System.arraycopy(available, 0, work, 0, NUMBER_OF_RESOURCES);
		Arrays.fill(finish, false);

		boolean progress;
		do {
			progress = false;
			for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
				if (!finish[i]) {
					boolean canFinish = true;
					for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
						if (need[i][j] > work[j]) {
							canFinish = false;
							break;
						}
					}
					if (canFinish) {
						for (int j = 0; j < NUMBER_OF_RESOURCES; j++)
							work[j] += allocation[i][j];
						finish[i] = true;
						progress = true;
					}
				}
			}
		} while (progress);

		for (boolean f : finish)
			if (!f)
				return false;
		return true;
	}

	public void printState() {
		System.out.print("Available: ");
		for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
			System.out.print(available[j] + (j == NUMBER_OF_RESOURCES - 1 ? "" : " "));
		}
		System.out.println();

		System.out.println("Maximum:");
		for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
			System.out.print("C" + i + ": ");
			for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
				System.out.print(maximum[i][j] + (j == NUMBER_OF_RESOURCES - 1 ? "" : " "));
			}
			System.out.println();
		}

		System.out.println("Allocation:");
		for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
			System.out.print("C" + i + ": ");
			for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
				System.out.print(allocation[i][j] + (j == NUMBER_OF_RESOURCES - 1 ? "" : " "));
			}
			System.out.println();
		}

		System.out.println("Need:");
		for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
			System.out.print("C" + i + ": ");
			for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
				System.out.print(need[i][j] + (j == NUMBER_OF_RESOURCES - 1 ? "" : " "));
			}
			System.out.println();
		}
	}
}