
import java.util.Scanner;
import java.util.regex.*;
import java.net.*;
import java.io.*;


public class Client {
	public static Socket socket; 

    public static void main(String[] args) throws Exception {
        Scanner ClientPortScan = new Scanner(System.in);
        Scanner ClientAddressScan = new Scanner(System.in);
        Scanner idUserScan = new Scanner(System.in);
        Scanner passwordUserScan = new Scanner(System.in);
        int ClientPort = validPort(ClientPortScan);
        String IpAddress = validIpAddress(ClientAddressScan);
        String idUser = getIdUser(idUserScan);
        String passwordUser = getPasswordUser(passwordUserScan);
        
        socket = new Socket(IpAddress, ClientPort);
        // creer un channel pour recevoir le message de bienvenue serveur
        DataInputStream messageServer = new DataInputStream(socket.getInputStream());
        // creer un channel pour envoyer information de connexion au serveur
        DataOutputStream connexionInformation = new DataOutputStream(socket.getOutputStream());
        
        String helloMessageFromServer = messageServer.readUTF();
        System.out.print(helloMessageFromServer);
        
        connexionInformation.writeUTF(idUser);
        connexionInformation.writeUTF(passwordUser);
        
        DataInputStream connexionMessage = new DataInputStream(socket.getInputStream());
        boolean isConnexionSucceed = connexionMessage.readBoolean();
        if(!isConnexionSucceed) {
        	System.out.println("Client: username and/or password incorrect.");
        	socket.close();
        	return;
        }
        
        boolean imageNameChecked = false;
        String imageName = "";
        String reponse = "";
        boolean goodFile = false;

        do{
        	do {
        	Scanner imageNameScan = new Scanner(System.in);
        	imageName = getImageName(imageNameScan);
        	imageNameChecked = checkImage(imageName);
        }while(imageNameChecked == false); 
        	
        System.out.println("Client: File name: " + imageName + " found, do you want to send it? (Y/N)");
		@SuppressWarnings("resource")
		Scanner verifScan = new Scanner(System.in);
			reponse = verifScan.nextLine();
		
        if (reponse.equals("Y") || reponse.equals("y")) {
        	goodFile = true;
        }else {
        	goodFile = false;
        }
        } while(goodFile == false);
        File imageToSend = new File(imageName);
        sendImage(imageToSend,imageName);
    
        
        String[] fileNameSplit = imageName.split("\\.");
        String newName = fileNameSplit[0] + "-traité.jpeg";
        receiveFilteredImage(newName);

        System.out.println("Client: I disconnect...");
        socket.close();
    }
    
    
    public static boolean isValidPort(int port) {
    	if (port >= 5000 &  port <= 5500) {
           		 return true;
        }
    	return false;
    }
    
    public static int validPort(Scanner reader) {

        boolean ClientPortOk = false;
        System.out.print("Client: enter a valid port(between 5000 and 5500): ");
        int ClientPort = reader.nextInt();
        do{

            if(isValidPort(ClientPort)){
         	   ClientPortOk = true;
                
            } else{
                System.out.print("Client: an error has occurred, enter a valid port(between 5000 and 5500): ");
                ClientPort = reader.nextInt();
                ClientPortOk = false;
            }
            
        }while(ClientPortOk == false);
        //System.out.print(" : " + ClientPort + "\n");
        return ClientPort;
    	}
    
    
    public static boolean isValidIPAddress(String ip)
   {
    	// inspiré de: https://www.geeksforgeeks.org/how-to-validate-an-ip-address-using-regular-expressions-in-java/
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
        
        boolean ClientAddressOk = false;
        System.out.print("Client: enter a valid ip address: ");
        String ClientAddress = reader.nextLine();
        do{

            if(isValidIPAddress(ClientAddress)){
                ClientAddressOk = true;
                
            } else{
                System.out.print("Client: an error has occurred, enter a valid ip address: ");
                 ClientAddress = reader.nextLine();
        		 ClientAddressOk = false;
            }
            
        }while(ClientAddressOk == false);
        return ClientAddress;
    }
    
    public static String getIdUser(Scanner reader) {
        System.out.print("Client: enter an username: ");
        String idUser = reader.nextLine();
        return idUser;
    }
    public static String getPasswordUser(Scanner reader) {
        System.out.print("Client: enter a password: ");
        String idUser = reader.nextLine();
        return idUser;
    }
    private static String getImageName (Scanner reader) {
    	System.out.print("Client: enter the image name with the format of the image"
    			+ " (example: image.jpg):  ");
        String imageName = reader.nextLine();
        return imageName;
    }
    // à checker pour peut-être changer
    private static Boolean checkImage(String nameImage) throws IOException {
		File image = new File(nameImage);
		if (!image.exists()) {
			System.out.println("Client: image not found. Try again\n");
			//out.writeUTF("imageNotFound");
			return false;
		} else {
			System.out.println("Client: Image found. the image is sent to the server.\n");
			//out.writeUTF("imageFound");
			return true;
		}
    }
	private static void sendImage(File img, String nameImg) throws IOException {
		byte[] imgByte = new byte[(int)img.length()];
		//On crée un channel pour recevoir l'image que le client a dans ses fichiers
		FileInputStream inputFile = new FileInputStream(img);
		//On crée un channel pour envoyer l'image au serveur
		DataOutputStream outImage = new DataOutputStream(socket.getOutputStream());
		//inspiré de https://www.codegrepper.com/code-examples/java/java+send+an+image+over+a+socket
		inputFile.read(imgByte);
		outImage.writeUTF(nameImg);
		outImage.writeInt((int)img.length());
		outImage.flush();
		outImage.write(imgByte, 0, imgByte.length);
		outImage.flush();
		System.out.print("Client: image sent successfully");
		inputFile.close();
	}
	
	private static boolean receiveFilteredImage(String filteredImageName) throws IOException{
		//On crée un channel pour recevoir l'image filtré
		DataInputStream inputImage = new DataInputStream(socket.getInputStream());
		File newImage = new File(filteredImageName);		
		FileOutputStream fileOutputStream = new FileOutputStream(newImage);

		int imageBytesLength = inputImage.readInt();
		byte[] imageBytes = new byte[imageBytesLength];

		inputImage.readFully(imageBytes);
		fileOutputStream.write(imageBytes, 0, imageBytes.length);
		fileOutputStream.close();
		
		System.out.println("Client: a new image filtered is downloaded in this directory path: "
							+ newImage.getAbsolutePath());
		return true;
	}
}