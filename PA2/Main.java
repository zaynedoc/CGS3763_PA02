import java.io.*;
import java.util.*;

// Main class with user-interactive loop for Banker class
public class Main {
	public static void main(String[] args) {
		// accept either 4 args (resources) or 5 args (resources + maxFilePath)
		if (args.length != Banker.NUMBER_OF_RESOURCES && args.length != Banker.NUMBER_OF_RESOURCES + 1) {
			System.err.println("Usage: java Main <R0> <R1> <R2> <R3> [<path/to/max.txt>]");
			return;
		}

		// parse available resources from command line
		int[] available = new int[Banker.NUMBER_OF_RESOURCES];

		try {
			for (int i = 0; i < Banker.NUMBER_OF_RESOURCES; i++)
				available[i] = Integer.parseInt(args[i]);
		} catch (NumberFormatException e) {
			System.err.println("Invalid resource counts on command line.");
			return;
		}

		// determine max.txt path: optional 5th arg or try common relative locations
		String maxPath = null;
		if (args.length == Banker.NUMBER_OF_RESOURCES + 1) {
			maxPath = args[Banker.NUMBER_OF_RESOURCES];
			if (!(new File(maxPath)).exists()) {
				System.err.println("Provided max.txt path does not exist: " + maxPath);
				return;
			}
		} else {
			// try to locate max.txt in common relative locations
			maxPath = locateMaxFile();
			if (maxPath == null) {
				System.err.println("Could not find max.txt. Put max.txt in the current directory or PA2/ subdirectory,\n" +
								   "or provide its path as the 5th argument:\n" +
								   "  java Main <R0> <R1> <R2> <R3> <path/to/max.txt>"
				);
				return;
			}
		}

		Banker banker;
		try {
			banker = new Banker(available, maxPath);
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			return;
		}

		// user-interactive loop
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
				banker.printState();
			} else if (cmd.equals("RQ")) { // request command; parse and call request_resources
				if (parts.length != 1 + 1 + Banker.NUMBER_OF_RESOURCES) {
					System.out.println("Usage: RQ <customer #> <r0> <r1> <r2> <r3>");
					continue;
				}
				try {
					int customer = Integer.parseInt(parts[1]);
					int[] req = new int[Banker.NUMBER_OF_RESOURCES];
					for (int i = 0; i < Banker.NUMBER_OF_RESOURCES; i++) 
						req[i] = Integer.parseInt(parts[2 + i]);

					int res = banker.request_resources(customer, req);
					System.out.println(res == 0 ? "Safe; request granted." : "Unsafe; request denied.");
				} catch (Exception e) {
					System.out.println("Invalid RQ command.");
				}
			} else if (cmd.equals("RL")) { // release command; parse and call release_resources
				if (parts.length != 1 + 1 + Banker.NUMBER_OF_RESOURCES) {
					System.out.println("Usage: RL <customer #> <r0> <r1> <r2> <r3>");
					continue;
				}
				try {
					int customer = Integer.parseInt(parts[1]);
					int[] rel = new int[Banker.NUMBER_OF_RESOURCES];

					for (int i = 0; i < Banker.NUMBER_OF_RESOURCES; i++) 
						rel[i] = Integer.parseInt(parts[2 + i]);

					banker.release_resources(customer, rel);
					System.out.println("Resources released.");
				} catch (Exception e) {
					System.out.println("Invalid RL command.");
				}
			} else if (cmd.equals("INFO")) {
				System.out.println("Commands\n" +
								   "  RQ <customer #> <r0> <r1> <r2> <r3>  - Request resources\n" +
								   "  RL <customer #> <r0> <r1> <r2> <r3>  - Release resources\n" +
								   "  STATE                              - Show current state\n" +
								   "  EXIT                               - Exit the program");
			} else {
				System.out.println("Unknown command. Use RQ, RL, STATE, EXIT, INFO");
			}
		}
		sc.close();
	}

	// helper: try common relative locations for max.txt and return the first existing absolute path, or null
	private static String locateMaxFile() {
		List<String> candidates = Arrays.asList(
			"./max.txt",
			"max.txt",
			"./PA2/max.txt",
			"PA2/max.txt"
		);
		for (String c : candidates) {
			File f = new File(c);
			if (f.exists() && f.isFile()) {
				try {
					return f.getCanonicalPath();
				} catch (IOException e) {
					return f.getAbsolutePath();
				}
			}
		}
		return null;
	}
}