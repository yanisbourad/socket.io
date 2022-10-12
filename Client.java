package server;
import java.util.Scanner;
import java.util.regex.*;
import java.net.*;
import java.io.*;


public class Client {
	public static Socket socket; 
    public static boolean isValidPort(int port) {
    	if (port >= 5000 &  port <= 5500) {
           		 return true;
        }
    	return false;
    }
    
    public static int validPort(Scanner reader) {

        boolean ClientPortOk = false;
        System.out.print("Client: entrez une port valide(entre 5000 et 5500): ");
        int ClientPort = reader.nextInt();
        do{

            if(isValidPort(ClientPort)){
         	   ClientPortOk = true;
                
            } else{
                System.out.print("Une erreur est survenu, entrez un port valide(entre 5000 et 5500: ");
                ClientPort = reader.nextInt();
                ClientPortOk = false;
            }
            
        }while(ClientPortOk == false);
        System.out.print("le port est: " + ClientPort + "\n");
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
        System.out.print("Client: entrez une adresse ip valide: ");
        String ClientAddress = reader.nextLine();
        do{

            if(isValidIPAddress(ClientAddress)){
                ClientAddressOk = true;
                
            } else{
                System.out.print("Client: Une erreur est survenu, entrez une adresse ip valide: ");
                 ClientAddress = reader.nextLine();
        		 ClientAddressOk = false;
            }
            
        }while(ClientAddressOk == false);
        System.out.print("Client: L'adresse ip est: " + ClientAddress + "\n");
        return ClientAddress;
    }
    
    public static String getIdUser(Scanner reader) {
        System.out.print("Client: entrez une nom d'utilisateur: ");
        String idUser = reader.nextLine();
        return idUser;
    }
    public static String getPasswordUser(Scanner reader) {
        System.out.print("Client: entrez un mot de passe: ");
        String idUser = reader.nextLine();
        return idUser;
    }
    private static String getImageName (Scanner reader) {
    	System.out.print("Client: entrez le nom de l'image ");
        String imageName = reader.nextLine();
        return imageName;
    }
    // à checker pour peut-être changer
    private static Boolean checkImage(String nameImage) throws IOException {
    	DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		File image = new File(nameImage);
		if (!image.exists()) {
			System.out.println("L'image recherchée n'a pas été trouvée.\n");
			//out.writeUTF("imageNotFound");
			return false;
		} else {
			System.out.println("L'image recherchée a été trouvée. La transmission au serveur commence.\n");
			//out.writeUTF("imageFound");
			return true;
		}
    }
	private static void sendImage(File img, String nameImg) throws IOException {
		byte[] imgByte = new byte[(int)img.length()];
		FileInputStream inputStream = new FileInputStream(img);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		inputStream.read(imgByte);
		out.writeUTF(nameImg);
		out.writeInt((int)img.length());
		out.flush();
		out.write(imgByte, 0, imgByte.length);
		out.flush();
		System.out.print("image envoyée");
		inputStream.close();
	}
	
	private static boolean receiveFilteredImage(String filteredImageName) throws IOException{
		DataInputStream input = new DataInputStream(socket.getInputStream());
		File newImage = new File(filteredImageName);		
		FileOutputStream fileOutputStream = new FileOutputStream(newImage);

		int imageBytesLength = input.readInt();
		byte[] imageBytes = new byte[imageBytesLength];

		input.readFully(imageBytes);
		fileOutputStream.write(imageBytes, 0, imageBytes.length);
		fileOutputStream.close();
		
		System.out.println("Succes, nouvelle image recue du server a l'adresse: " + newImage.getAbsolutePath());
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		output.writeUTF("imageReceivedSToC");
		return true;
	}
    
    //--------------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        int clientNumber = 0;
        Scanner ClientPortScan = new Scanner(System.in);
        Scanner ClientAddressScan = new Scanner(System.in);
        Scanner idUserScan = new Scanner(System.in);
        Scanner passwordUserScan = new Scanner(System.in);
        int ClientPort = validPort(ClientPortScan);
        String IpAddress = validIpAddress(ClientAddressScan);
        String idUser = getIdUser(idUserScan);
        String passwordUser = getPasswordUser(passwordUserScan);
        
        //-----------------------------------------------------------------------------------------------------------------
        socket = new Socket(IpAddress, ClientPort);
        System.out.format("The server is running", IpAddress, ClientPort);
        
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        
        String helloMessageFromServer = in.readUTF();
        System.out.print(helloMessageFromServer);
        
        out.writeUTF(idUser);
        out.writeUTF(passwordUser);
        
        boolean imageNameChecked = false;
        String imageName = "";
        Scanner imageNameScan = new Scanner(System.in);
        String reponse = "";
        boolean goodFile = false;
        do{
        	do {
        	imageName = getImageName(imageNameScan);
        	imageNameChecked = checkImage(imageName);
        }while(imageNameChecked == false); 
        	
        System.out.println("Nom fichier: " + imageName + " a été trouvé, voulez-vous l'envoyer? (Y/N)");
        Scanner verifScan = new Scanner(System.in);
        reponse = verifScan.nextLine();
        if (reponse.equals("Y") || reponse.equals("y")) {
        	goodFile = true;
        }
        } while(goodFile == false);
        
        File imageToSend = new File(imageName);
        sendImage(imageToSend,imageName);
        System.out.println("kkk sa marche");
    
        
        String[] fileNameSplit = imageName.split("\\.");
        String newName = fileNameSplit[0] + "-traité.jpeg";
        boolean test = receiveFilteredImage(newName);
        System.out.println(test);
        socket.close();
    }

}