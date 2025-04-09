package common;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Remote interface for Key-Value Store operations
 * All methods must declare RemoteException as they are called remotely
 */
public interface KVStoreRMI extends Remote{
  /**
   * Store a key-value pair
   * @param key The key to store
   * @param value The value to associate with the key
   * @return Response string indicating success or failure
   * @throws RemoteException if a remote error occurs
   */
  String put(String key, String value) throws RemoteException;

  /**
   * Retrieve a value by its key
   * @param key The key to look up
   * @return Response string with the value or error message
   * @throws RemoteException if a remote error occurs
   */
  String get(String key) throws RemoteException;

  /**
   * Delete a key-value pair
   * @param key The key to delete
   * @return Response string indicating success or failure
   * @throws RemoteException if a remote error occurs
   */
  String delete(String key) throws RemoteException;


}
