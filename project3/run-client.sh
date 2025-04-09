#!/bin/bash

# 编译Java文件（如果尚未编译）
javac -d classes src/common/*.java src/kvstore/*.java src/server/*.java src/client/*.java

# 运行客户端
java -cp classes client.ReplicatedRMIClient
