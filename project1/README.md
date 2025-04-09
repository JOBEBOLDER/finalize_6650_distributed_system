# Key-Value Store with TCP/UDP Support

A Java-based distributed key-value store implementation that supports both TCP and UDP protocols. This project demonstrates basic networking concepts, client-server architecture, and concurrent programming principles.

## Project Structure

```
src/
├── client/
│   ├── TCPClient.java
│   └── UDPClient.java
├── common/
│   ├── Logger.java
│   └── Protocol.java
├── kvstore/
│   └── KVStore.java
└── server/
    ├── TCPServer.java
    └── UDPServer.java
```

## Features

- Support for both TCP and UDP protocols
- Thread-safe key-value operations
- In-memory storage with HashMap implementation
- Basic CRUD operations (Create, Read, Delete)
- Logging system for debugging and monitoring
- Configurable network settings
- Client timeout handling
- Concurrent client request handling

## Components

### Servers
- **TCPServer**: Handles client connections using TCP protocol
- **UDPServer**: Handles client requests using UDP protocol

### Clients
- **TCPClient**: Implements TCP-based communication with server
- **UDPClient**: Implements UDP-based communication with server

### Common Components
- **KVStore**: Core key-value storage implementation
- **Protocol**: Defines communication protocol and message formatting
- **Logger**: Provides logging functionality

## Getting Started

### Prerequisites
- Java JDK 8 or higher
- JUnit (for running tests)

### Compilation
```bash
# Create bin directory
mkdir -p bin

# Compile all source files
javac -d bin src/**/*.java
```

### Running the Servers
```bash
# Start TCP Server (e.g., on port 8080)
java -cp bin server.TCPServer 8080

# Start UDP Server (e.g., on port 8081)
java -cp bin server.UDPServer 8081
```

### Running the Clients
```bash
# Run TCP Client
java -cp bin client.TCPClient localhost 8080

# Run UDP Client
java -cp bin client.UDPClient localhost 8081
```

## Usage Examples

### TCP Client
```java
TCPClient client = new TCPClient("localhost", 8080);

// Store a value
client.sendRequest(Protocol.PUT, "name", "John");

// Retrieve a value
client.sendRequest(Protocol.GET, "name", null);

// Delete a value
client.sendRequest(Protocol.DELETE, "name", null);
```

### UDP Client
```java
UDPClient client = new UDPClient("localhost", 8081);

// Store a value
client.sendRequest(Protocol.PUT, "age", "25");

// Retrieve a value
client.sendRequest(Protocol.GET, "age", null);

// Delete a value
client.sendRequest(Protocol.DELETE, "age", null);
```

## Protocol Specification

### Request Format
- PUT: `PUT key value`
- GET: `GET key`
- DELETE: `DELETE key`

### Response Format
- PUT: `PUT_OK` or `PUT_ERROR`
- GET: `GET_RESULT value` or `GET_ERROR`
- DELETE: `DELETE_OK` or `DELETE_ERROR`

## Testing

The project includes comprehensive test suites for all components:

```bash
# Compile tests
javac -cp .:junit-4.13.2.jar:hamcrest-core-1.3.jar -d bin test/**/*.java

# Run tests
java -cp .:junit-4.13.2.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore test.AllTests
```

## Design Considerations

1. **Thread Safety**: The KVStore uses HashMap's thread-safe operations for concurrent access
2. **Protocol Design**: Simple text-based protocol for easy debugging and testing
3. **Error Handling**: Comprehensive error handling and logging
4. **Timeout Handling**: UDP client implements timeout for unreliable connections
5. **Resource Management**: Proper cleanup of sockets and connections

## Implementation Notes

- TCP implementation uses persistent connections for reliability
- UDP implementation includes timeout handling for packet loss
- In-memory storage means data is not persistent across server restarts
- Logging system provides debugging information with timestamps

## Future Improvements

1. Add persistent storage support
2. Implement data replication
3. Add authentication and authorization
4. Support for complex data types
5. Add transaction support
6. Implement cache mechanism
7. Add monitoring and metrics
8. Support for data compression

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.