#!/bin/bash

# 编译Java文件
javac -d classes src/common/*.java src/kvstore/*.java src/server/*.java src/client/*.java

# 启动5个服务器
for i in {0..4}
do
    echo "Starting server $i..."
    java -cp classes server.ReplicatedRMIServer $i &
    # 给每个服务器一点时间启动
    sleep 1
done

echo "All servers started!"