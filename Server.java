package server;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.regex.*;

public class Server {
    private static ServerSocket listener;
    public static boolean isValidPort(int port) {
    	if (port >= 5000 &  port <= 5500) {
           		 return true;
        }
    	return false;
    }
    
    public static int validPort(Scanner reader) {
        Scanner serverPortScan = new Scanner(System.in);
        boolean serverPortOk = false;
        System.out.print("Serveur: entrez une port valide(entre 5000 et 5500): ");
        int serverPort = reader.nextInt();
        do{

            if(isValidPort(serverPort)){
         	   serverPortOk = true;
                
            } else{
                System.out.print("Serveur: Une erreur est survenu, entrez un port valide(entre 5000 et 5500): ");
                serverPort = reader.nextInt();
                serverPortOk = false;
            }
            
        }while(serverPortOk == false);
        System.out.print("le port est: " + serverPort + "\n");
        return serverPort;
    	}
    
    
    public static boolean isValidIPAddress(String ip)
   {

       String zeroTo255
           = "(\\d{1,2}|(0|1)\\"
             + "d{2}|2[0-4]\\d|25[0-5])";

       String regex
           = zeroTo255 + "\\."
             + zeroTo255 + "\\."
             + zeroTo255 + "\\."
             + zeroTo255;

       Pattern p = Pattern.compile(regex);

       if (ip == null) {
           return false;
       }
       Matcher m = p.matcher(ip);
       return m.matches();
   }
    public static String validIpAddress(Scanner reader){
        
        boolean serverAddressOk = false;
        System.out.print("Serveur: entrez une adresse ip valide: ");
        String serverAddress = reader.nextLine();
        do{

            if(isValidIPAddress(serverAddress)){
                serverAddressOk = true;
                
            } else{
                System.out.print("Serveur: Une erreur est survenu, entrez une adresse ip valide: ");
                 serverAddress = reader.nextLine();
        		 serverAddressOk = false;
            }
            
        }while(serverAddressOk == false);
        System.out.print("Serveur: L'adresse ip est: " + serverAddress + "\n");
        return serverAddress;
    }
    //--------------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        int clientNumber = 0;
        Scanner serverPortScan = new Scanner(System.in);
        Scanner serverAddressScan = new Scanner(System.in);
        int serverPort = validPort(serverPortScan);
        String IpAddress = validIpAddress(serverAddressScan);


//----------------------------------------------------------------------------------------------------------------------------
        listener = new ServerSocket();
        listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(IpAddress);
        
        listener.bind(new InetSocketAddress(serverIP, serverPort));
        System.out.format("The server is running", IpAddress, serverPort);
        try {
        	while(true) {
        		new ClientHandler(listener.accept(),clientNumber++).start();
        	}
        } finally {
        	listener.close();
        }
    }
}

class ClientHandler extends Thread{
	private Socket socket;
	private int clientNumber;
	
	public ClientHandler(Socket socket, int clientNumber){
		this.socket = socket;
		this.clientNumber = clientNumber;
		System.out.print("new connection with client#" + clientNumber + "at"+ socket);
		
	}
	
	public void run() {
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("Hello from server - you are client #"+ clientNumber);
		} 
		catch (IOException e) 
		{
			System.out.print("Error handling client#" + clientNumber + ":" + e);
		}
		finally 
		{
			try {
				socket.close();
				}
			catch (IOException e) {
			System.out.print("Could not close a socket");
		}
		System.out.print("Connection with client#" + clientNumber + "closed");
		}
	}
}


