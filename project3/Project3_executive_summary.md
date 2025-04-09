# Replicated Key-Value Store with Two-Phase Commit

## Executive Summary

### Assignment Overview
This project extends our previous single-server key-value store to a distributed system with replication across five distinct servers. The primary goals were to increase system bandwidth, ensure high availability, and maintain data consistency across all replicas. To accomplish this, I implemented a two-phase commit (2PC) protocol that coordinates PUT and DELETE operations across all server instances. The system allows clients to contact any of the five replicas for operations while ensuring that all replicas maintain a consistent view of the data. The implementation includes proper transaction management with prepare, commit, and abort phases, along with timeout mechanisms to handle potential communication failures.

### Technical Impression
Implementing the replicated key-value store with two-phase commit protocol presented several interesting challenges. The first major hurdle was designing the communication framework between replica servers. I initially struggled with null pointer exceptions in the DELETE operations because of how I was marking deleted entries in the tempStore map. This led to inconsistent behavior where some servers couldn't properly process delete requests during the prepare phase. After debugging, I found that using null values in ConcurrentHashMap was causing issues, so I switched to using a DELETE_MARKER string instead, which resolved the problem.

The complexity of the two-phase commit protocol also became apparent when testing edge cases. Even though the assignment assumed no server failures would occur, implementing proper timeout mechanisms was crucial to prevent the protocol from stalling. Getting the right balance between waiting for responses and moving forward with transaction decisions required careful tuning. Another challenge was ensuring that transactions remained isolated - ensuring that operations on the same key didn't interfere with each other when executed concurrently. Overall, this project provided valuable insights into distributed consensus algorithms and highlighted the tradeoffs between consistency, availability, and partition tolerance described by the CAP theorem.

## System Architecture

### Components

1. **ReplicatedKVStore**: Core implementation of the replicated key-value store with two-phase commit protocol.
2. **ReplicatedRMIServer**: Server application that hosts a replica of the key-value store.
3. **ReplicatedRMIClient**: Client application that can connect to any of the five replicas.
4. **KVStoreRMI**: Remote interface defining the operations supported by the key-value store.
5. **ReplicaInfo**: Helper class to store information about other replicas in the system.

### Communication Protocol

The system implements a two-phase commit protocol for write operations (PUT and DELETE):

1. **Phase 1 (Prepare)**:
    - Coordinator sends PREPARE messages to all replicas
    - Each replica checks if it can perform the operation
    - Replicas respond with PREPARED or ABORT

2. **Phase 2 (Commit/Abort)**:
    - If all replicas responded with PREPARED, coordinator sends COMMIT
    - If any replica responded with ABORT, coordinator sends ABORT
    - Replicas execute or roll back the operation accordingly

## Implementation Details

### Key Classes

- **ReplicatedKVStore**: Implements the distributed key-value store with 2PC
- **ReplicatedRMIServer**: Main server class that initializes and hosts a replica
- **ReplicatedRMIClient**: Client class that can connect to any replica
- **KVStoreRMI**: RMI interface defining operations and 2PC protocol methods

### Error Handling

- Timeouts for communication between replicas
- Transaction IDs to uniquely identify and track operations
- Error messages returned to the client when operations fail
- Proper cleanup of temporary storage during aborts

## System Performance

The system successfully demonstrates:
- Data consistency across all replicas
- High availability through replication
- Atomicity of operations through the two-phase commit protocol
- Client transparency (clients can connect to any replica)

## Conclusion

This implementation showcases the core concepts of distributed systems, including data replication, consensus algorithms, and fault tolerance. While the two-phase commit protocol ensures strong consistency, it has limitations in terms of availability during network partitions. Future improvements could include implementing a three-phase commit protocol or exploring eventual consistency models.