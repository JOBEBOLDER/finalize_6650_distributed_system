# CS 6650 Scalable Distributed Systems - Project #2
# Multi-threaded Key-Value Store using RPC

## How to Build Server and Client Code

This project is implemented in Java using the built-in RMI framework for remote method invocation. The project doesn't depend on any external libraries and only requires JDK 8 or higher.

### Building from Command Line

1. **Create output directory for compiled classes**:
```bash
mkdir -p classes
```

2. **Compile all Java files**:
```bash
javac -d classes src/common/*.java src/kvstore/*.java src/server/*.java src/client/*.java src/test/*.java
```

### Building with an IDE (IntelliJ IDEA or Eclipse)

1. Import the project into your IDE
2. Use the IDE's build functionality to compile the project

## How to Run Server and Client Programs

### Running the Server

The server requires a port number and an optional thread pool size:

```bash
# Start server on port 1099 with default 10 threads
java -cp classes server.RMIServer 1099

# Start server on port 1099 with 20 threads
java -cp classes server.RMIServer 1099 20
```

### Running the Client

The client requires the server address and port number:

```bash
# Connect to local server on port 1099
java -cp classes client.RMIClient localhost 1099
```

### Running Concurrent Tests

The concurrent test program can test server performance under high concurrency:

```bash
# Use default configuration (10 clients, 20 operations per client)
java -cp classes test.ConcurrentTest localhost 1099

# Specify number of clients and operations per client
java -cp classes test.ConcurrentTest localhost 1099 50 10
```

## Project Structure

```
src/
├── common/            # Shared interfaces and utilities
│   ├── KVStoreRMI.java  # Remote interface definition
│   ├── Logger.java      # Logging utility
│   └── Protocol.java    # Communication protocol constants
├── kvstore/           # Key-value store implementation
│   └── KVStore.java     # Thread-safe key-value storage
├── server/            # Server implementation
│   └── RMIServer.java   # Multi-threaded RMI server
├── client/            # Client implementation
│   └── RMIClient.java   # RMI client
└── test/              # Test code
    └── ConcurrentTest.java  # Concurrent testing program
```

## Executive Summary

### Assignment Overview

This project extends a previous socket-based Key-Value store system through two key improvements: replacing socket communication with RPC and implementing a multi-threaded server architecture. By utilizing Java's RMI framework for RPC communication, the client can directly invoke methods on the server without dealing with the complexities of low-level network communication. Simultaneously, the multi-threaded server design enables the system to handle multiple concurrent client requests, significantly enhancing throughput and responsiveness. To ensure thread safety, the project employs ConcurrentHashMap for data storage, effectively avoiding data inconsistency issues during concurrent access. This project simulates common design patterns and challenges found in real-world distributed systems, providing valuable practical experience for building scalable distributed applications.

### Technical Impression

While implementing this project, I gained a deeper understanding of RPC mechanisms and multi-threaded programming. Java RMI provides a clean abstraction over network communication, allowing me to focus on business logic rather than networking details. Compared to the previous socket-based approach, the RPC model makes client-server communication more intuitive and maintainable, resulting in cleaner code structure.

For multi-threading, using the ExecutorService thread pool proved more efficient and flexible than directly creating and managing threads. The thread pool eliminates the overhead of frequent thread creation and destruction while providing control over the number of concurrent requests, preventing resource exhaustion. ConcurrentHashMap greatly simplified thread synchronization complexities with its internal fine-grained locking mechanism, offering better performance than traditional HashMap with explicit synchronization.

During testing, I observed that thread pool size significantly impacts system performance. When concurrent requests exceed the thread pool capacity, additional requests are queued, potentially increasing response latency. Conversely, an oversized thread pool leads to excessive context switching overhead, reducing overall performance. Finding the optimal thread pool size for specific workloads requires a balanced approach determined through performance testing.

Overall, this project provided valuable insights into key considerations for distributed system design: network communication efficiency, concurrent processing capacity, and thread safety. These experiences will be invaluable for developing more complex distributed applications in the future.
