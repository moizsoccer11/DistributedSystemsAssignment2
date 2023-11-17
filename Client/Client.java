import java.io.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        //Variables
        Socket server;
        PrintWriter serverOut;
        BufferedReader serverIn;
        BufferedReader stdIn;
        String userInput;
        boolean loggedIn =false;
        String loginID;
        String userName="";
        String[] loginDetails={};
        //Connect to socket and initalize reader and writer
        try {
            //Create Socket and Input/Output Streams
            server = new Socket("localhost", 3500);
            serverOut= new PrintWriter(server.getOutputStream(), true);
            serverIn= new BufferedReader(new InputStreamReader(server.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            //Get Java RMI details
            Registry registry = LocateRegistry.getRegistry();
            RemoteInterface r = (RemoteInterface) registry.lookup("service");
            //
            while(true){
                //Login Sequence
                System.out.println("Welcome to Threaded Rooms, If you have a Login ID please Enter, If you don't type '1'");
                userInput= stdIn.readLine();
                if(userInput.equals("1")){
                    System.out.println("Please enter a Login ID:");
                    userInput = stdIn.readLine();
                    loginID=userInput;
                    System.out.println("Please enter your user name:");
                    userInput = stdIn.readLine();
                    userName=userInput;
                    r.CreateLoginID(loginID,userName);
                    loggedIn=true;
                }
                else{
                    loginDetails = r.GetLoginDetails(userInput);
                    if(loginDetails.length > 1){
                        loggedIn=true;
                        userName=loginDetails[1];
                    }
                }
                while(!loggedIn){
                    System.out.println("Incorrect Login ID, If you don't have one type '1'");
                    userInput= stdIn.readLine();
                    if(userInput.equals("1")){
                        System.out.println("Please enter a Login ID:");
                        userInput = stdIn.readLine();
                        loginID=userInput;
                        System.out.println("Please enter your user name:");
                        userInput = stdIn.readLine();
                        userName=userInput;
                        r.CreateLoginID(loginID,userName);
                        loggedIn=true;
                    }
                    else{
                        loginDetails = r.GetLoginDetails(userInput);
                        if(loginDetails.length > 1){
                            loggedIn=true;
                            userName=loginDetails[1];
                        }
                    }
                }
                //Since user logged In, Send Username to Server;
                serverOut.println(userName);
                final String username= userName;
                //Interact with server:::
                // Create a separate thread for reading messages from the server
                Thread readThread = new Thread(() -> {
                    String message;
                    try {
                        String serverResponse;
                        while ((serverResponse = serverIn.readLine()) != null) {
                            if(serverResponse.equals("finished") || serverResponse.equals("Roomed")){
                                //dont print out to user
                            }
                            else{
                                System.out.println(serverResponse);
                            }
                             if(serverResponse.equals("finished") ){
                                message= stdIn.readLine();
                                if(message.equals("c")){
                                    System.out.println("Enter the Name of the Room:");
                                    message=stdIn.readLine();
                                    r.createChatRoom(username,message);
                                    serverOut.println("Roomed");
                                    Thread inChatRoom = new Thread(()->{
                                        String msg;
                                        try {
                                            while (true) {
                                                msg = stdIn.readLine();
                                                if (msg.equalsIgnoreCase("exit")) {
                                                    r.leaveChatRoom(username);
                                                    break;
                                                }
                                                else if(msg.contains("/")){
                                                    String[] arr = new String[2];
                                                    arr= msg.split("/");
                                                    r.sendPrivateMessage(username,arr[0], arr[1]);
                                                }
                                                else{
                                                    r.sendMessageToEveryone(username,msg);
                                                }
                                            }
                                        } catch (Exception e) {
                                        e.printStackTrace();
                                        }
                                    });
                                    inChatRoom.start();
                                }
                                else{
                                    r.joinChatRoom(username,message);
                                    serverOut.println("Roomed");
                                    Thread inChatRoom = new Thread(()->{
                                        String msg;
                                        try {
                                            while (true) {
                                                msg = stdIn.readLine();
                                                if (msg.equalsIgnoreCase("exit")) {
                                                    r.leaveChatRoom(username);
                                                    break;
                                                }
                                                else if(msg.contains("/")){
                                                    String[] arr = new String[2];
                                                    arr= msg.split("/");
                                                    r.sendPrivateMessage(username,arr[0], arr[1]);
                                                }
                                                else{
                                                    r.sendMessageToEveryone(username,msg);
                                                }
                                            }
                                        } catch (Exception e) {
                                        e.printStackTrace();
                                        }
                                    });
                                    inChatRoom.start();
                                }
                            }
                            if(serverResponse.equals("Roomed")){
                                serverOut.println("Roomed");
                            }
                            if(serverResponse.equals("exit")){
                                serverOut.println("exit");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                readThread.start();
                // Main thread for sending user messages
                while (true) {
                    //stall
                }
   
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
}
