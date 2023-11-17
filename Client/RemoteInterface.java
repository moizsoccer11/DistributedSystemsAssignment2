
//Imports
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface RemoteInterface extends Remote {
    //Method to allow Client to join a chat room
    void joinChatRoom(String message , String roomName) throws RemoteException;
    //Method to allow Client to leave the chat room
    void leaveChatRoom(String userName) throws RemoteException;
    //Method to broadcast messgae to all clients in the chat room
    void sendMessageToEveryone(String userName,String message) throws RemoteException;
    //Method to send a private message to a specific client in the chat room
    void sendPrivateMessage(String userName,String clientName , String message) throws RemoteException;
    //Method to create a new chat room
    void createChatRoom(String userName,String roomName) throws RemoteException;
    //Method to create login ID
    void CreateLoginID(String userName, String loginID) throws RemoteException;
    //Method to Get Login ID Details
    String[] GetLoginDetails(String loginID) throws RemoteException;
}
