# CGS3763_PA02
Banker's Algorithm -- Assignment 2

## Overview
This project implements the Banker's Algorithm to manage resource allocation for multiple customers (processes). The program tracks:
- Available resources vector
- Maximum demand matrix (per-customer)
- Current allocation matrix
- Need matrix (maximum - allocation)

When a request is made, the program tentatively allocates resources and runs the safety algorithm to ensure the system remains in a safe state; if safe, the request is granted, otherwise it is deemed unsafe and denied.

## Disclaimers
Generative-AI, including ChatGPT models, were utilized in the making of contents in this repository:
- This README.md was written with assistance from ChatGPT, including concepts regarding Banker's Algorithm.
- All code within this repository was written by a human, with minor bug fixes assisted by generative-AI.

## Files
- `PA2/banker.java` -- main program (reads `max.txt`, interactive console).
- `PA2/max.txt` -- maximum demand matrix.
  Example:
  ```
  6,4,7,3
  4,2,3,2
  2,5,3,3
  6,3,3,2
  5,6,7,5
  ```

## Max File Format and Locations
- The program uses `max.txt` to read the maximum demand matrix.
- You can either place `max.txt` in a common relative location or pass its path to the program.
- The program will look for max.txt in the following locations (in order) when no path is provided:
  1. ./max.txt
  2. max.txt
  3. ./PA2/max.txt
  4. PA2/max.txt
- Or provide an explicit path as a 5th command-line argument.

## Compile & Run
Open a terminal in the repository (or the PA2 directory) and compile:
```
cd PA2
javac Banker.java Main.java
```

Run the program with the initial available resources (four integers).
You may optionally provide the path to max.txt as a 5th argument.

Usage:
```
java Main <R0> <R1> <R2> <R3> [<path/to/max.txt>]
```

Examples:
- Rely on default search (run from repository root or appropriate folder):
```
java Main 3 3 2 2
```
- Provide explicit path to max.txt:
```
java Main 3 3 2 2 /home/alice/projects/CGS3763_PA02/PA2/max.txt
```

## Interactive Commands
After starting, enter commands at the `Enter Command:` prompt.

- `STATE`
  - Print current state (Available, Maximum, Allocation, Need).

- `RQ <customer #> <r0> <r1> <r2> <r3>`
  - Request resources for a customer.
  - `<customer #>` is an integer 0..4 (index into rows of `max.txt`).
  - Returns either `Request granted.` or `Request denied.` depending on safety check.
  - Example:
    ```
    RQ 1 0 2 1 0
    ```

- `RL <customer #> <r0> <r1> <r2> <r3>`
  - Release resources from a customer.
  - Releases up to the allocated amount (the program clamps excessive releases).
  - Example:
    ```
    RL 1 0 1 0 0
    ```
  - Program prints `Resources released.`

- `EXIT`
  - Quit the program.
  - Program prints `Exiting.`

Notes:
- The program validates customer index and request/release sizes relative to need/allocation.
- Requests are first tested against Need and Available, then a tentative allocation is checked with the safety algorithm; if unsafe, the allocation is rolled back.

## Example Terminal
Assume `max.txt` as above and initial available `3 3 2 2`.

1) Start:
```
$ java banker 3 3 2 2
Enter Command:
```

2) Print initial state:
```
STATE
Available: 3 3 2 2
Maximum:
C0: 6 4 7 3
C1: 4 2 3 2
C2: 2 5 3 3
C3: 6 3 3 2
C4: 5 6 7 5
Allocation:
C0: 0 0 0 0
C1: 0 0 0 0
C2: 0 0 0 0
C3: 0 0 0 0
C4: 0 0 0 0
Need:
C0: 6 4 7 3
C1: 4 2 3 2
C2: 2 5 3 3
C3: 6 3 3 2
C4: 5 6 7 5
```

3) Make a request:
```
RQ 2 1 0 2 0
Request granted.
```

4) Release resources:
```
RL 2 1 0 1 0
Resources released.
```

5) Exit:
```
EXIT
Exiting.
```

## Implementation notes
- Number of customers and resources are constants in `banker.java`:
  - `NUMBER_OF_CUSTOMERS = 5`
  - `NUMBER_OF_RESOURCES = 4`
- The safety check uses standard Banker's Algorithm (work/finish simulation).
- Adjust `max.txt` and initial available vector as needed.