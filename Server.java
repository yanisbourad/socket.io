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
        System.out.format("The server is running: /n", IpAddress, serverPort);
        try {
        	while(true) {
        		//----------------- verifier si utilisateur existe------------        		
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
	private static DataOutputStream out;
	private static DataInputStream in;
	
	public ClientHandler(Socket socket, int clientNumber){
		this.socket = socket;
		this.clientNumber = clientNumber;
		System.out.print("new connection with client#" + clientNumber + "at"+ socket);
		
	}
	
	public void run() {
		try {
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
			//boolean deconnexion = false;
			//do {
			//	boolean validInfo= false;
			//	while(validInfo == false) {
			//	}
			//}while (deconnexion == false);
			out.writeUTF("Hello from server - you are client #"+ clientNumber);
			String userTest = in.readUTF();
			System.out.println(userTest);
			String passwordTest = in.readUTF();
			System.out.println(passwordTest);
			createFile();
			/*String user = "213";
			String password = "667";
			String user1 = "pessi";
			String password1 = "91";*/
			//findUser("accounts.txt", userTest, passwordTest);
			addUser("accounts.txt", userTest, passwordTest);
			
			
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
	
	public static void createFile(){
		try {
		File file = new File("accounts.txt");
		if (file.createNewFile()) {
	        System.out.println("File created: " + file.getName());
	      } else {
	        System.out.println("File: '"+ file.getName()+"' already exists.");
	      }} catch (IOException e) {
	          System.out.println("An error occurred.");
	          e.printStackTrace();
	        }
	}
	
	public static boolean findUser(String fileName, String user, String password) throws IOException {
		boolean userFind = false;
		File file = new File(fileName);
	    if (file.exists()) {
	    	Scanner fileScan = new Scanner(file);
	    	// on lit le fichier
	    	while(fileScan.hasNextLine()) {
	    		String currentLine = fileScan.nextLine();
	    		// verfier si c'est le bon utilisateur
	    		if( currentLine.split(",")[0].equals(user)){
	    			System.out.println("user trouvé: "+ currentLine.split(",")[0]);
	    			// verifier si c'est le bon mot de passe
	    			if((currentLine.split(",")[1].equals(password))) {
	    				System.out.println("mdp trouvé: "+ currentLine.split(",")[1]);
	    				userFind = true;
	    			} else {
	    				System.out.println("mot de passe erroné");
	    			}
	    			
	    		}
	    		
	    	}
	    	fileScan.close();
	    	if (!userFind) {
	    		 //on ajoute le user
	    		addUser(fileName,user, password);
	    	}
	    		
	    }
	    else {
	    	createFile();
	    	
	    }
	    
		
		return userFind;
	}
	
	public static void addUser(String fileName, String user, String password) throws IOException {
		String string = user + "," + password + "\n";
		FileWriter myWriter = new FileWriter(fileName, true);
		myWriter.write(string);
		System.out.println("un utilisateur a été ajouté: " +user);
		myWriter.close();
	}
}


