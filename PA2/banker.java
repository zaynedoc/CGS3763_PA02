import java.io.*;
import java.util.*;

public class banker {
	static final int NUMBER_OF_CUSTOMERS = 5;
	static final int NUMBER_OF_RESOURCES = 4;

	static int[] available = new int[NUMBER_OF_RESOURCES];
	static int[][] maximum = new int[NUMBER_OF_CUSTOMERS][NUMBER_OF_RESOURCES];
	static int[][] allocation = new int[NUMBER_OF_CUSTOMERS][NUMBER_OF_RESOURCES];
	static int[][] need = new int[NUMBER_OF_CUSTOMERS][NUMBER_OF_RESOURCES];

	public static void main(String[] args) {
		// Initialize available from command line
		if (args.length != NUMBER_OF_RESOURCES) {
			System.err.println("Usage: java banker <R0> <R1> <R2> <R3>");
			return;
		}
		try {
			for (int i = 0; i < NUMBER_OF_RESOURCES; i++) {
				available[i] = Integer.parseInt(args[i]);
			}
		} catch (NumberFormatException e) {
			System.err.println("Invalid resource counts on command line.");
			return;
		}

		// Read maximum matrix from file
		String maxPath = "/workspaces/CGS3763_PA02/PA2/max.txt";
		if (!readMaximumFromFile(maxPath)) {
			System.err.println("Failed to read maximum file: " + maxPath);
			return;
		}

		// initialize allocation[i][j] to 0
            // calculate "need = maximum - allocation"
		for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
			for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
				allocation[i][j] = 0;
				need[i][j] = maximum[i][j] - allocation[i][j];
			}
		}

		// User-interactive loop
		Scanner sc = new Scanner(System.in);
		while (true) {
			System.out.print("Enter Command: ");
			if (!sc.hasNextLine()) break;

			String line = sc.nextLine().trim();
			if (line.isEmpty()) continue;

			String[] parts = line.split("\\s+");
			String cmd = parts[0].toUpperCase(Locale.ROOT);

			if (cmd.equals("EXIT")) {
				System.out.println("Exiting.");
				break;
			} else if (cmd.equals("STATE")) {
				printState();
			} else if (cmd.equals("RQ")) {
				if (parts.length != 1 + 1 + NUMBER_OF_RESOURCES) {
					System.out.println("Usage: RQ <customer> <r0> <r1> <r2> <r3>");
					continue;
				} try {
					int customer = Integer.parseInt(parts[1]);
					int[] req = new int[NUMBER_OF_RESOURCES];       // request vector, copy of size as number of resources

					for (int i = 0; i < NUMBER_OF_RESOURCES; i++) {
                        req[i] = Integer.parseInt(parts[2 + i]);    // parse each resource request from command line
                    }

					int res = request_resources(customer, req);
					System.out.println(res == 0 ? "Request granted." : "Request denied.");
				} catch (Exception e) {
					System.out.println("Invalid RQ command.");
				}
			} else if (cmd.equals("RL")) {
				if (parts.length != 1 + 1 + NUMBER_OF_RESOURCES) {
					System.out.println("Usage: RL <customer> <r0> <r1> <r2> <r3>");
					continue;
				} try {
					int customer = Integer.parseInt(parts[1]);
					int[] rel = new int[NUMBER_OF_RESOURCES];

					for (int i = 0; i < NUMBER_OF_RESOURCES; i++) {
                        rel[i] = Integer.parseInt(parts[2 + i]);
                    }

					release_resources(customer, rel);
					System.out.println("Resources released.");
				} catch (Exception e) {
					System.out.println("Invalid RL command.");
				}
			} else if (cmd.equals("INFO")) {
                System.out.println("Commands\n" +
                                    "  RQ <customer> <r0> <r1> <r2> <r3>  - Request resources\n" +
                                    "  RL <customer> <r0> <r1> <r2> <r3>  - Release resources\n" +
                                    "  STATE                              - Show current state\n" +
                                    "  EXIT                               - Exit the program");
            } else {
				System.out.println("Unknown command. Use RQ, RL, STATE, EXIT, INFO");
			}
		}
		sc.close();
	}

	static boolean readMaximumFromFile(String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			int row = 0;

			while ((line = br.readLine()) != null && row < NUMBER_OF_CUSTOMERS) {
				line = line.trim();
				if (line.isEmpty()) continue;
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

	static int request_resources(int customer_num, int request[]) {
		// validate customer index
		if (customer_num < 0 || customer_num >= NUMBER_OF_CUSTOMERS) return -1;

		// check Request <= Need
		for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
			if (request[j] > need[customer_num][j]) return -1; // exceeds declared maximum
		}
		// check Request <= Available
		for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
			if (request[j] > available[j]) return -1; // not enough available
		}

		// allocate
		for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
			available[j] -= request[j];
			allocation[customer_num][j] += request[j];
			need[customer_num][j] -= request[j];
		}

		// check safety
		if (isSafe()) {
			return 0; // granted requrest/"safe"
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

	static void release_resources(int customer_num, int release[]) {
		// validate customer index
		if (customer_num < 0 || customer_num >= NUMBER_OF_CUSTOMERS) return;

		// ensure not releasing more than allocated
		for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
			if (release[j] > allocation[customer_num][j]) {
				release[j] = allocation[customer_num][j];
			}
		}

		for (int j = 0; j < NUMBER_OF_RESOURCES; j++) {
			allocation[customer_num][j] -= release[j];
			available[j] += release[j];
			need[customer_num][j] += release[j];
		}
	}

	static boolean isSafe() {
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
						for (int j = 0; j < NUMBER_OF_RESOURCES; j++) work[j] += allocation[i][j];
						finish[i] = true;
						progress = true;
					}
				}
			}
		} while (progress);

		for (boolean f : finish) if (!f) return false;
		return true;
	}

	static void printState() {
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