import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


//The Server is responsible for Connecting Clients to the Rooms and Creating New ChatRooms
public class Server {
    //List  to keep track of Rooms
    static List<Room> chatRooms = new ArrayList<>();
    //List to keep track of clients
    static List<Client> clients = new ArrayList<>(); 
    public static void main(String[] args) {
        //Variables
        ServerSocket serverSoc;
        Socket clientSoc;
        try {
            //Create server socket
            serverSoc =  new ServerSocket(3500);
            //Create the Java RMI
            RemoteInterface r = new ServerImpl("service");
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(r,0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("service", stub);
            //Start Program
            System.out.println("Threaded Rooms Server Started...!");
            while (true) {
                clientSoc = serverSoc.accept();
                System.out.println("New client connected");
                ClientHandler client = new ClientHandler(clientSoc);
                client.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static class ClientHandler extends Thread{
        //Client Socket
        Socket client;
        //Client User Name
        String userName;
        //Input and Output from Client
        PrintWriter clientOut;
        BufferedReader clientIn;

        public ClientHandler(Socket clientSoc){
            client=clientSoc;
        }

        public void run(){
            try {
                //Track if user is in room or not
                boolean clientInRoom=false;
                //Client Input for Room Name or to create new Room
                String userInput="";
                //Create Input and Output Streams for the client
                clientOut = new PrintWriter(client.getOutputStream(), true);
                clientIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
                //Get User Name from Client
                userName=clientIn.readLine();
                //Create the Client Object
                Client client = new Client(userName,clientOut);
                clients.add(client);
                //Ask client to either create new room or join an existing room:
                while(true){
                    //Get All current room names
                    List<String> chatRoomNames = new ArrayList<>();
                    for(Room room: chatRooms){
                            chatRoomNames.add(room.getRoomName());
                    }
                    //Display all available rooms to Client and prompt them to join or create new room
                    clientOut.println("Welcome "+userName+" to Threaded Rooms!");
                    if(chatRoomNames.isEmpty()){
                        clientOut.println("No available rooms, you can create a room by typing 'c'.");
                        clientOut.println("finished");
                    }
                    else{
                        clientOut.println("To Join a Room, Type The Name of any of the available Rooms below, or press 'c' to create new room:");
                        for(String roomName: chatRoomNames){

                            clientOut.println("Available Room: "+roomName);
    
                        }
                        clientOut.println("finished");
                    }
                    //Get Input from Client
                    while(!clientInRoom){
                        userInput = clientIn.readLine();
                    
                        //Client is in a room
                        if(userInput.equals("Roomed")){
                            clientInRoom=true;
                        }
                    }
                    String message;
                    while ((message = clientIn.readLine()) != null) {
                        if (message.equals("exit")) {
                            break;
                        }
                    }
                    //Remove client from the room
                   // selectedRoom.removeClientFromRoom(clientOut);

                }
            } catch (IOException e) {
                
            }
        }
    }
    static class Room {
        private String chatRoomName="";
        private List<PrintWriter> clients = new ArrayList<>();

        public Room(String name){
            chatRoomName= name;
        }

        public String getRoomName(){
            return chatRoomName;
        }

        public List<PrintWriter> getClientsInRoom (){
            return clients;
        }

        public void addClientToRoom(PrintWriter client){
            clients.add(client);
        }
        public void removeClientFromRoom(PrintWriter client){
            clients.remove(client);
        }

    }
    static class ServerImpl implements RemoteInterface {

    public ServerImpl(String s) throws RemoteException{
        super();
     }

     // Join chat room method
    public void joinChatRoom(String userName, String roomName) throws RemoteException {
        //Check if room exists
        for(Client client : clients){
            PrintWriter clientOut;
            Room selectedRoom = null;
            if(client.clientName.equals(userName)){
                clientOut = client.client;
                for(Room room: chatRooms){
                    if(room.getRoomName().toLowerCase().equals(roomName.toLowerCase())){
                        selectedRoom = room;
                        //Add Client to the room
                        selectedRoom.addClientToRoom(clientOut);
                        //Add Room to the client object
                        client.inRoom=selectedRoom;
                        //Send message to client to notify them
                        clientOut.println("You are now in the chat room: " + selectedRoom.getRoomName());
                        clientOut.println("Roomed");//notify client side they are in room
                    }
                }
                if(selectedRoom == null){
                    clientOut.println("Room does not exist!, type 'c' to create a room");
                }
            }
        }
    }
    //Leave chat room method
    public void leaveChatRoom(String userName) throws RemoteException {
        PrintWriter clientOut;
        for(Client client : clients){
            if(client.clientName.equals(userName)){
                clientOut=client.client;
                Room room = client.inRoom;
                //Tell others in group that client left
                for (PrintWriter user : room.getClientsInRoom()) {
                    if(user.equals(clientOut)){
                        //Dont send message to yourself
                    }else{
                        user.println(userName+" has left the chat...");
                    }
                }
                client.inRoom.removeClientFromRoom(clientOut);
                //Send server message to make user exit
                clientOut.println("exit");
            }
        }
    }

    //Send broadcast message method
    public void sendMessageToEveryone(String userName,String message) throws RemoteException {
        for(Client client: clients){
            PrintWriter clientOut;
             //Check for user
            if(client.clientName.equals(userName)){
                Room room = client.inRoom;
                clientOut=client.client;
                for (PrintWriter user : room.getClientsInRoom()) {
                    if(user.equals(clientOut)){
                        //Dont send message to yourself
                    }else{
                        user.println(userName+": " + message);
                    }
                }
            }
        }
    }

    //Send Private Message method
    public void sendPrivateMessage(String userName,String clientName, String message) throws RemoteException {
       //find user
       for(Client client: clients){
            PrintWriter clientOut;
            if(client.clientName.equals(clientName)){
                clientOut=client.client;
                clientOut.println("|PRIVATE| "+userName+": "+message);
            }
       }
    }

    //Create Chat Room Method
    public void createChatRoom(String userName,String roomName) throws RemoteException {
         //find user printwriter object
        for( Client client : clients){
            PrintWriter clientOut;
             Room selectedRoom;
             //Check for user
                if(client.clientName.equals(userName)){
                    clientOut=client.client;  
                    //Create room
                    selectedRoom = new Room(roomName);
                    //add room
                    chatRooms.add(selectedRoom);
                    //Add Client to the room
                    selectedRoom.addClientToRoom(clientOut);  
                    //Add Room to the client object
                    client.inRoom=selectedRoom;
                    //Send message to client to notify them
                    clientOut.println("You are now in the chat room: " + selectedRoom.getRoomName());
                    
                }
        }
    }
    //Create Login Id method
    public void CreateLoginID(String userName, String loginID) throws RemoteException {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("login.txt",true));
                writer.write(loginID+"/"+userName);
                writer.newLine();
                writer.close();
            } catch (IOException e) {
        }
    }
    //Get Login Id details method
    public String[] GetLoginDetails(String loginID) throws RemoteException {
       String[] loginDetails = {};
        try {
        File myObj = new File("login.txt");
        Scanner myReader = new Scanner(myObj);
        while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if(data.contains(loginID)){
                  loginDetails= data.split("/");
                }
        }
        myReader.close();
        } catch (Exception e) {
           
        }
        return loginDetails;
    }
    
}
static class Client{
    private PrintWriter client;
    private String clientName;
    private Room inRoom;

    public Client(String clientName, PrintWriter clientOut){
        this.clientName= clientName;
        client=clientOut;
    }

    public void addToRoom(Room room){
        this.inRoom=room;
    }

}
    
}
