package server;
import java.util.Scanner;
import java.util.regex.*;
import java.net.*;
import java.io.*;
import java.util.HashMap;

public class Client {
	public static Socket socket; 
    public static boolean isValidPort(int port) {
    	if (port >= 5000 &  port <= 5500) {
           		 return true;
        }
    	return false;
    }
    
    public static int validPort(Scanner reader) {
        Scanner ClientPortScan = new Scanner(System.in);
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
        
        String helloMessageFromServer = in.readUTF();
        System.out.print(helloMessageFromServer);
        socket.close();
    }

}
