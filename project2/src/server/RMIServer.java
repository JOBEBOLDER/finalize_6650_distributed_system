package server;

import common.KVStoreRMI;
import common.Logger;
import kvstore.KVStore;

import java.util.concurrent.ExecutorService;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Multi-threaded RMI Server Implementation
 * This server uses Java RMI for remote method invocation and a thread pool
 * to handle concurrent client requests
 */
public class RMIServer implements KVStoreRMI  {
  private final Logger logger;
  private final ExecutorService threadPool;
  private final int numThreads;

  /**
   * Constructor creates a thread pool with the specified number of threads
   * @param numThreads number of threads in the poolnumThreads 指定线程池大小，即服务器可以同时处理多少个客户端请求。
   */
  public RMIServer(int numThreads) {
    this.logger = new Logger(RMIServer.class);
    this.numThreads = numThreads;
    this.threadPool = Executors.newFixedThreadPool(numThreads);
    logger.log("Server initialized with " + numThreads + " threads");
  }

  /**
   * Put operation - executes in a worker thread from the thread pool
   */

  public String put(String key, String value) throws RemoteException {
    try {
      Future<String> future = threadPool.submit(() -> {
        logger.log("PUT operation: key=" + key + ", value=" + value);
        return KVStore.put(key, value);
      });
      return future.get(); // Wait for the task to complete
    } catch (Exception e) {
      logger.log("Error in PUT operation: " + e.getMessage());
      throw new RemoteException("Error in PUT operation", e);
    }
  }

  /**
   * Get operation - executes in a worker thread from the thread pool
   */
  public String get(String key) throws RemoteException {
    try {
      Future<String> future = threadPool.submit(() -> {
        logger.log("Get operation: key=" + key);
        return KVStore.get(key);
      });
      return future.get();

    } catch (Exception e) {
      Logger.log("Error in GET operation: " + e.getMessage());
      throw new RemoteException("Error in GET operation", e);
    }
  }

  /**
   * Delete operation - executes in a worker thread from the thread pool
   */
  public String delete(String key) throws RemoteException {
    try {
      Future<String> future = threadPool.submit(() -> {
        logger.log("DELETE operation : key=" + key);
        return KVStore.delete(key);
      });
      return future.get(); // Wait for the task to complete

    } catch (Exception e) {
      Logger.log("Error in DELETE operation: " + e.getMessage());
      throw new RemoteException("Error in DELETE operation", e);
    }
  }

  public  void start(int port) {
    try {
      //build a remote object
      KVStoreRMI stub = (KVStoreRMI) UnicastRemoteObject.exportObject(this, 0);
      // Create or get the registry at the specified port
      Registry registry = LocateRegistry.createRegistry(port);

      // Bind the remote object's stub in the registry
      registry.rebind("KVStoreService", stub);

      logger.log("RMI Server started on port " + port + " with " + numThreads + " threads");
      logger.log("Service bound to registry as 'KVStoreService'");

    } catch (Exception e) {
      logger.log("RMI Server error:" + e.getMessage());
      e.printStackTrace();
    }

  }

    /**
     * Shuts down the thread pool
     */
    public void shutdown() {
      threadPool.shutdown();
      logger.log("Server shutting down");
    }

    public static void main(String[] args) {
      if (args.length < 1 || args.length > 2) {
        System.out.println("Usage: java RMIServer <port>[numThreads]");
        return;
      }
      int port =Integer.parseInt(args[0]);

      //default is 10 RMI threadpool
      int numThreads = (args.length == 2) ? Integer.parseInt(args[1]) : 10;

      RMIServer server = new RMIServer(numThreads);
      server.start(port);

      // 添加一个关闭钩子，确保服务器正确关闭
      Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }







}
