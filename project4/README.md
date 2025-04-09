# CS 6650 Project 4: Fault-Tolerant Key-Value Store Using Paxos

## Overview

This project implements a fault-tolerant distributed key-value store using the Paxos consensus algorithm as described in Leslie Lamport's paper "Paxos Made Simple." The implementation extends Project 3 (which used Two-Phase Commit) by adding fault tolerance capabilities, allowing the system to continue functioning even when some nodes fail.

## Features

- Distributed key-value store replicated across 5 distinct servers
- Implementation of the Paxos consensus algorithm for fault tolerance
- Support for basic operations: PUT, GET, DELETE
- Random acceptor failures with automatic recovery
- Client capable of connecting to any available server
- Stress testing capability to verify system stability under load

## Architecture

The system consists of the following components:

### Paxos Roles
- **Proposer**: Initiates proposals for operations to be performed
- **Acceptor**: Receives and accepts/rejects proposals
- **Learner**: Learns about accepted proposals and applies them to the state machine

### Key Components
- `PaxosKVStore.java`: Core implementation that combines the KV store with Paxos roles
- `PaxosServer.java`: Server startup class that initializes and exposes services
- `PaxosRMI.java`: Remote interface for Paxos operations
- `PaxosResponse.java`: Data transfer object for Paxos responses
- `ReplicatedRMIClient.java`: Client that can connect to any available server
- `PaxosFaultToleranceTest.java`: Test class that verifies fault tolerance

## Fault Tolerance Mechanism

The system implements Paxos to achieve consensus even in the presence of failures:
1. Each server can act as a proposer, acceptor, and learner
2. Acceptors randomly fail and restart to simulate real-world conditions
3. As long as a majority of servers (at least 3 out of 5) are operational, the system can continue to function
4. The system maintains consistency through the Paxos protocol's prepare-accept-learn phases

## How to Run

### Prerequisites
- Java Development Kit (JDK) 11 or higher
- Bash shell (for running scripts)

### Running the System
Use the provided script to compile, start servers, and run tests:
```bash
chmod +x run_paxos_test.sh
./run_paxos_test.sh
```

If you encounter "Address already in use" warnings, you can modify the BASE_PORT in PaxosServer.java to use a different port range:
```java
private static final int BASE_PORT = 8090;  // Instead of 1099
```

### Test Verification
The test performs the following:
1. **Test 1**: Basic operations with all servers running
2. **Wait Period**: Allow random server failures to occur
3. **Test 2**: Operations during partial server failures
4. **Wait Period**: Allow servers to recover
5. **Test 3**: Operations after server recovery
6. **Test 4**: Stress test with rapid operations during failures

## Implementation Notes

- **Consensus**: The implementation requires a majority (3 out of 5) of acceptors to agree
- **Failure Simulation**: Acceptors fail at random intervals and restart after a delay
- **Proposal IDs**: Each server generates unique proposal IDs to avoid conflicts
- **Fault Tolerance**: The system continues to function as long as a majority of servers remain operational
- **Client Connection**: Clients can connect to any available server and operations will be propagated to all servers

## Project Structure

```
project4/
├── src/
│   ├── common/
│   │   ├── KVStoreRMI.java
│   │   ├── Logger.java
│   │   ├── PaxosRMI.java
│   │   ├── PaxosResponse.java
│   │   └── Protocol.java
│   ├── kvstore/
│   │   └── KVStore.java
│   ├── client/
│   │   ├── RMIClient.java
│   │   └── ReplicatedRMIClient.java
│   ├── server/
│   │   ├── PaxosKVStore.java
│   │   ├── PaxosServer.java
│   │   ├── ReplicaInfo.java
│   │   ├── ReplicatedKVStore.java
│   │   ├── ReplicatedRMIServer.java
│   │   └── RMIServer.java
│   └── test/
│       ├── ConcurrentTest.java
│       └── PaxosFaultToleranceTest.java
├── classes/
├── out/
├── run_paxos_test.sh
└── README.md
```

## Troubleshooting

- If you encounter port conflicts, try stopping existing Java processes:
  ```bash
  pkill -f "java.*PaxosServer"
  ```

- If ports remain in use, modify the BASE_PORT constant in both PaxosServer.java and ReplicatedRMIClient.java to use a different port range.

## References

- Lamport, L. (2001). "Paxos Made Simple." ACM SIGACT News, 32(4), 51-58.
- CS 6650 Scalable Distributed Systems course materials