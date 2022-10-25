package server;
import java.net.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Scanner;
import java.util.regex.*;

import javax.imageio.ImageIO;


public class Server {
    private static ServerSocket listener;
    public static boolean isValidPort(int port) {
    	if (port >= 5000 &  port <= 5500) {
           		 return true;
        }
    	return false;
    }
    
    public static int validPort(Scanner reader) {
        boolean serverPortOk = false;
        System.out.print("Server: enter a valid port(between 5000 and 5500): ");
        int serverPort = reader.nextInt();
        do{

            if(isValidPort(serverPort)){
         	   serverPortOk = true;
                
            } else{
                System.out.print("Server: an error has occurred, enter a valid port(between 5000 and 5500): ");
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
        System.out.print("Server: enter a valid ip address: ");
        String serverAddress = reader.nextLine();
        do{

            if(isValidIPAddress(serverAddress)){
                serverAddressOk = true;
                
            } else{
                System.out.print("Server: an error has occurred, enter a valid ip address: ");
                 serverAddress = reader.nextLine();
        		 serverAddressOk = false;
            }
            
        }while(serverAddressOk == false);
        System.out.print("Server: Ip address is: " + serverAddress + "\n");
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
        System.out.format("The server is running: /n", IpAddress, serverPort, "\n");
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
	
	public ClientHandler(Socket socket, int clientNumber){
		this.socket = socket;
		this.clientNumber = clientNumber;
		System.out.print("Server: new connection with client#" + clientNumber + "at"+ socket);
		
	}
	
	public void run() {
		try {
			// creer un channel pour envoyer un message que la connexion au serveur OK
			DataOutputStream helloMessage = new DataOutputStream(socket.getOutputStream());
			// creer un channel pour recevoir les informations du client
			DataInputStream connexionInformation = new DataInputStream(socket.getInputStream());

			helloMessage.writeUTF("Hello from server - you are client #"+ clientNumber + "\n");
			String clientUsername = connexionInformation.readUTF();
			//System.out.println(clientUsername);
			String clientPassword = connexionInformation.readUTF();
			//System.out.println(clientPassword);
			
			String adresse =(((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress()).toString().replace("/","");
			int port = socket.getLocalPort();
			//System.out.println( adresse + port);
			String nameFile = port + "-" + adresse + ".txt"; 
			createFile(nameFile);
			boolean userfind = findUser(nameFile, clientUsername, clientPassword);
			boolean passwordfind = findPassword(nameFile, clientUsername, clientPassword);
			// cree un channel pour prevenir le client si la connexion a été un succès ou pas
			DataOutputStream connexionMessage = new DataOutputStream(socket.getOutputStream());
			if(userfind && passwordfind) {
				connexionMessage.writeBoolean(true);
				receiveImage();
			}
			else if(!userfind) {
				connexionMessage.writeBoolean(true);
				addUser(nameFile, clientUsername, clientPassword);
				receiveImage();
			}
			
			else if (userfind && !passwordfind) {
				connexionMessage.writeBoolean(false);
				socket.close();
				return;
			}

			socket.close();
				
		} 
		catch (IOException e) {
			System.out.print("Server: Error handling client#" + clientNumber + ":" + e);
		}
		finally {
			try {
				socket.close();
				}
			catch (IOException e) {
			System.out.print("Server: Could not close a socket");
		}
		System.out.print("Server: Connection with client#" + clientNumber + "closed");
		}
	}
	
	public static void createFile(String fileName){
		try {
		File file = new File(fileName);
		if (file.createNewFile()) {
	        System.out.println("Server: File created: " + file.getName());
	      } else {
	        //System.out.println("File: '"+ file.getName()+"' already exists.");
	      }} catch (IOException e) {
	          System.out.println("Server: An error occurred.");
	          e.printStackTrace();
	        }
	}
	
	public static boolean findUser(String fileName, String user, String password) throws IOException {
		boolean userFind = false;
		File file = new File(fileName);
		
	    if (file.exists()) {
	    	Scanner fileScan = new Scanner(file);
	    	//System.out.println("test 0");
	    	// on lit le fichier
	    	while(fileScan.hasNextLine()) {
	    		//System.out.println("test 1");
	    		String currentLine = fileScan.nextLine();
	    		//System.out.println("test 2");
	    		// verfier si c'est le bon utilisateur
	    		if( currentLine.split(",")[0].equals(user)){
	    			//System.out.println("test 3");
	    			//System.out.println("user trouvé: "+ currentLine.split(",")[0]);
	    			//System.out.println("test 4");
	    			userFind = true;
	    			
	    			
	    		}
	    		
	    	}
	    	fileScan.close();
    		
	    }
	    else {
	    	createFile(fileName);
	    	//System.out.println("test 7");
	    	addUser(fileName, user, password);
	    	//System.out.println("test 8");
	    	
	    }
	    
		return userFind;
	}
	
	public static boolean findPassword(String fileName, String user, String password) throws IOException {
		boolean passwordFind = false;
		if(findUser(fileName, user, password)) {
			File file = new File(fileName);
			
		    if (file.exists()) {
		    	Scanner fileScan = new Scanner(file);
		    	// on lit le fichier
		    	while(fileScan.hasNextLine()) {
		    		String currentLine = fileScan.nextLine();
		    		if((currentLine.split(",")[1].equals(password))) {
	    				//System.out.println("mdp trouvé: "+ currentLine.split(",")[1]);
	    				passwordFind = true;
	    			} else {
	    				//System.out.println("mot de passe erroné");
	    				passwordFind = false;
	    			}
		    	}
		    	fileScan.close();
		    }
			
		}
		else {
			return false;
		}
		return passwordFind;
	}
	
	public static void addUser(String fileName, String user, String password) throws IOException {
		//System.out.println("test 9");
		boolean userFind = findUser(fileName, user, password);
		//System.out.println("test 10");
		if (!userFind) {
		String string = user + "," + password + "\n";
		FileWriter myWriter = new FileWriter(fileName, true);
		myWriter.write(string);
		System.out.println("Server: new user added in the file: " +user);
		myWriter.close();
		}
	}

	private void receiveImage() throws IOException {
		// on cree un channel pour recevoir l'image et ses informations
		DataInputStream imageReception = new DataInputStream(socket.getInputStream());
		String imageName = imageReception.readUTF();
		//System.out.println("image name: "+ imageName);
		File image = new File("lastImageFiltred.jpeg");		
		FileOutputStream fileOutputStream = new FileOutputStream(image);
		int imageBytesLength = imageReception.readInt();
		//System.out.println("nmbre int: " +imageBytesLength);
		byte[] imageBytes = new byte[imageBytesLength];
		//System.out.println("test 2: " + imageBytes);
		imageReception.readFully(imageBytes);
		//System.out.println("test 3");
		fileOutputStream.write(imageBytes, 0, imageBytes.length);
		//System.out.println("test 4");
		fileOutputStream.close();
		System.out.println("Server: '"+ imageName+ "' received");
		
		File filteredImage = new File("lastImageFiltred.jpeg");
		BufferedImage buffy = ImageIO.read(image);
		buffy = Sobel.process(buffy);
		sendFilteredImage(filteredImage, buffy );
}
	private void sendFilteredImage(File filteredImage, BufferedImage buffy) throws IOException {
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		ImageIO.write(buffy, "jpg", filteredImage);
		//System.out.println("test 1");
		byte[] imageBytes = new byte[(int)filteredImage.length()];
		//System.out.println("test 2");
		FileInputStream fileInputStream = new FileInputStream(filteredImage);

		//System.out.println("test 3");
		fileInputStream.read(imageBytes);
		//System.out.println("test 4");
		fileInputStream.close();
		//System.out.println("test 5");
		output.writeInt((int)filteredImage.length());
		//System.out.println("test 6");
		output.flush();
		//System.out.println("test 7");
		output.write(imageBytes, 0, imageBytes.length);
		//System.out.println("test 8");
		output.flush();
		//System.out.println("test 9");
		System.out.println("Server: image sended");
	}

}