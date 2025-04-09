#!/bin/bash

# Paxos Fault Tolerance Test Script for CS 6650 Project 4

echo "===== Starting Paxos KV Storage Servers ====="

# Create output directory if it doesn't exist
mkdir -p out

# Compile all Java files
echo "Compiling Java files..."
find src -name "*.java" > sources.txt
javac -d out @sources.txt

# Check if compilation was successful
if [ $? -ne 0 ]; then
    echo "Compilation failed. Please fix the errors and try again."
    exit 1
fi

# Start 5 server processes
echo "Starting 5 server processes..."
for i in {0..4}
do
  echo "Starting server $i..."
  java -cp out server.PaxosServer $i &
  sleep 1
done

# Wait for all servers to start
echo "Waiting for all servers to start..."
sleep 5

# Run the fault tolerance test
echo "===== Running Paxos Fault Tolerance Test ====="
java -cp out test.PaxosFaultToleranceTest

# Wait for user input before terminating server processes
echo -e "\nTest complete. Press any key to terminate all server processes..."
read -n 1

# Terminate all server processes
echo "Terminating server processes..."
pkill -f "java -cp out server.PaxosServer"

echo "Test environment cleanup complete."