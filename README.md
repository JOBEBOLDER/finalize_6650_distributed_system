# CS 6650: Building Scalable Distributed Systems

#### Northeastern 
#### professor: Prasad Saripalli

This repository contains the implementation of four progressive projects for CS 
6650 Scalable Distributed Systems course. Each project builds upon the previous one, 
exploring various concepts of distributed systems including socket programming, RPC, 
replication, and fault tolerance.

## Repository Structure

```
6650_building_scalable_distributedSystem/
├── project1/  - Socket Programming and Key-Value Store
├── project2/  - Multi-threaded Key-Value Store with RMI
├── project3/  - Replicated Key-Value Store with Two-Phase Commit
└── project4/  - Fault-Tolerant Key-Value Store using Paxos
```

## Project Descriptions

### Project 1: Socket Programming and Key-Value Store

A basic key-value store implementation with both TCP and UDP clients and servers.

**Key Features:**
- Thread-safe in-memory key-value store
- TCP client/server implementation
- UDP client/server implementation
- Support for PUT, GET, DELETE operations
- Custom protocol for client-server communication

### Project 2: Multi-threaded Key-Value Store with RMI

Extended the key-value store to use Java RMI (Remote Method Invocation) for client-server communication.

**Key Features:**
- Java RMI implementation
- Thread pooling for concurrent client requests
- Enhanced logging and error handling
- Performance testing with concurrent clients

### Project 3: Replicated Key-Value Store with Two-Phase Commit

Added replication across multiple servers with consistency guaranteed by Two-Phase Commit protocol.

**Key Features:**
- Replication across 5 distinct server nodes
- Two-Phase Commit (2PC) protocol implementation
- Client able to connect to any server node
- Consistent operations across all replicas

### Project 4: Fault-Tolerant Key-Value Store using Paxos

The final project implements a fault-tolerant distributed key-value store using the Paxos consensus algorithm.

**Key Features:**
- Paxos consensus algorithm implementation
- Fault tolerance (system works even when servers fail)
- Implementation of Proposer, Acceptor, and Learner roles
- Random acceptor failures with automatic recovery
- Support for basic PUT, GET, DELETE operations
- Stress testing under various failure scenarios

## Technologies

- **Languages**: Java
- **Communication Protocols**: Socket Programming, RPC, Java RMI
- **Consistency Protocols**: Two-Phase Commit, Paxos
- **Tools**: Multi-threading, Logging, Exception Handling

## Running the Projects

Each project contains its own README file with specific instructions for building and running. In general:

### Project 1
```bash
# Compile
javac -d out project1/src/*/*.java

# Run TCP Server
java -cp out server.TCPServer

# Run TCP Client
java -cp out client.TCPClient
```

### Project 2
```bash
# Compile
javac -d out project2/src/*/*.java

# Start RMI Registry
rmiregistry &

# Run Server
java -cp out server.RMIServer 1099 10

# Run Client
java -cp out client.RMIClient localhost 1099
```

### Project 3
```bash
# Compile
javac -d out project3/src/*/*.java

# Start Server Instances
./project3/start-servers.sh

# Run Client
./project3/run-client.sh
```

### Project 4
```bash
# Use the provided script to compile, start servers and run tests
chmod +x project4/run_paxos_test.sh
./project4/run_paxos_test.sh
```

## Learning Outcomes

Through these projects, the following distributed systems concepts were explored:

1. **Network Communication**: Using sockets and RPC mechanisms
2. **Concurrency**: Multi-threaded server design patterns
3. **Distributed Data Storage**: Key-value store implementation
4. **Consistency Models**: Strong consistency with 2PC
5. **Fault Tolerance**: Using Paxos consensus algorithm
6. **System Scalability**: Replication across multiple servers

## References

- Lamport, L. (2001). "Paxos Made Simple." ACM SIGACT News, 32(4), 51-58.
- Tanenbaum, A. S., & Van Steen, M. (2007). Distributed systems: principles and paradigms.
- Coulouris, G., Dollimore, J., Kindberg, T., & Blair, G. (2011). Distributed Systems: Concepts and Design (5th Edition).

## License

This project is created for educational purposes as part of CS 6650 Scalable Distributed Systems course.