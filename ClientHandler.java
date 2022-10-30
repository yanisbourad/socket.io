import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class ClientHandler extends Thread{
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
				String clientPassword = connexionInformation.readUTF();
				
				String adresse =(((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress()).toString().replace("/","");
				int port = socket.getLocalPort();
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
		    	// on lit le fichier
		    	while(fileScan.hasNextLine()) {
		    		String currentLine = fileScan.nextLine();
		    		// verfier si c'est le bon utilisateur
		    		if( currentLine.split(",")[0].equals(user)){
		    			userFind = true;
		    		}
		    		
		    	}
		    	fileScan.close();
	    		
		    }
		    else {
		    	createFile(fileName);
		    	addUser(fileName, user, password);
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
		    				passwordFind = true;
		    			} else {
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
			boolean userFind = findUser(fileName, user, password);
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
			File image = new File("lastImageFiltred.jpeg");
			//Nous permet d'écrire des data dans le fichier
			FileOutputStream fileOutputStream = new FileOutputStream(image);
			int imageBytesLength = imageReception.readInt();
			byte[] imageBytes = new byte[imageBytesLength];
			imageReception.readFully(imageBytes);
			fileOutputStream.write(imageBytes, 0, imageBytes.length);
			fileOutputStream.close();
			System.out.println("Server: '"+ imageName+ "' received");
			
			File filteredImage = new File("lastImageFiltred.jpeg");
			BufferedImage buffy = ImageIO.read(image);
			buffy = Sobel.process(buffy);
			sendFilteredImage(filteredImage, buffy );
	}
		private void sendFilteredImage(File filteredImage, BufferedImage buffy) throws IOException {
			//Permet d'envoyer l'image filtré 
			DataOutputStream outputFiltered = new DataOutputStream(socket.getOutputStream());
			ImageIO.write(buffy, "jpg", filteredImage);
			byte[] imageBytes = new byte[(int)filteredImage.length()];
			FileInputStream fileInputStream = new FileInputStream(filteredImage);
			fileInputStream.read(imageBytes);
			fileInputStream.close();
			outputFiltered.writeInt((int)filteredImage.length());
			outputFiltered.flush();
			outputFiltered.write(imageBytes, 0, imageBytes.length);
			outputFiltered.flush();
			System.out.println("Server: image sended");
		}
	}

