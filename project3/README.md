# Project 3: Replicated Key-Value Store with Two-Phase Commit

This project implements a replicated key-value store across multiple servers with consistency guaranteed by the Two-Phase Commit (2PC) protocol.

## Project Structure

```
project3/
├── classes/             # Compiled Java classes
├── src/                 # Source code
│   ├── client/          # Client implementation
│   │   ├── RMIClient.java
│   │   └── ReplicatedRMIClient.java
│   ├── common/          # Shared classes
│   │   ├── KVStoreRMI.java
│   │   ├── Logger.java
│   │   └── Protocol.java
│   ├── kvstore/         # Key-Value store implementation
│   │   └── KVStore.java
│   ├── server/          # Server implementation
│   │   ├── RMIServer.java
│   │   ├── ReplicaInfo.java
│   │   ├── ReplicatedKVStore.java
│   │   └── ReplicatedRMIServer.java
│   └── test/            # Test classes
│       └── ConcurrentTest.java
├── project2.iml         # IntelliJ project file
├── Project3_executive_summary.md # Project summary
├── run-client.sh        # Script to run the client
└── start-servers.sh     # Script to start all server replicas
```

## Overview

This project builds upon Project 2 by adding replication capabilities to the key-value store. The system replicates data across 5 distinct server nodes and ensures consistency using the Two-Phase Commit protocol.

### Key Features

- **Replication**: Data is replicated across 5 distinct server nodes
- **Two-Phase Commit (2PC)**: Ensures consistency across all replicas for write operations
- **Distributed Coordination**: Coordinator manages the 2PC process
- **Fault Detection**: System detects and handles communication failures
- **RMI-based Communication**: Java RMI for remote method invocation
- **Thread Safety**: Concurrent operations are properly synchronized

## Running the Project

### Starting the Servers

Use the provided script to start all 5 server replicas:

```bash
chmod +x start-servers.sh
./start-servers.sh
```

This will start 5 server instances on local ports 1099-1103.

### Running the Client

Use the provided script to run the client:

```bash
chmod +x run-client.sh
./run-client.sh
```

The client will connect to one of the available servers and perform operations that will be replicated across all servers.

## Implementation Details

### Two-Phase Commit Protocol

The implementation follows the standard 2PC protocol:

1. **Prepare Phase**: The coordinator asks all participants if they can commit
2. **Commit Phase**: If all participants agree, the coordinator tells them to commit

### Operation Flow

1. Client sends a request to any server
2. The receiving server acts as the coordinator for the 2PC protocol
3. Coordinator sends prepare requests to all replicas
4. If all replicas vote to commit, the coordinator sends commit messages
5. If any replica votes to abort, the coordinator sends abort messages
6. The operation result is returned to the client

## Testing

The project includes a concurrent test class that verifies the system's behavior under simultaneous client operations. Run the test to see how the system handles concurrent requests and maintains consistency.

## Limitations

- No recovery mechanism for permanent server failures
- Blocking behavior during 2PC process
- No timeout mechanism for unresponsive participants

## References

- "Concurrency Control and Recovery in Database Systems" by Bernstein, Hadzilacos, and Goodman
- Java RMI Documentation: https://docs.oracle.com/javase/tutorial/rmi/