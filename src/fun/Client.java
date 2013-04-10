package fun;
import java.io.*;
import java.net.*;

// Shamelessly ripped off
// http://zerioh.tripod.com/ressources/sockets.html
public class Client implements Runnable {
   Socket requestSocket;
   BufferedReader in;
   String message;
   
   int portNum = 55555;
   
   public Client(){}
   
   public void run() {
      try {
         //1. creating a socket to connect to the server
         requestSocket = new Socket("localhost", portNum);
         System.out.println("Connected to localhost in port" +  portNum);
         
         
         //2. get Input and Output streams
         in = new BufferedReader(new InputStreamReader(requestSocket.getInputStream()));
         
         //3: Communicating with the server
         String line = "";
         while (requestSocket.isConnected()) {
            line = in.readLine();
            if (line.equals("Q")) break;
            
            String[] parts = line.split("\\|");
            System.out.println(parts[0] + ": (" + parts[1] + "," + parts[2] + ")");
            
         }
         
         System.out.println("Quitting...");
      } catch( Exception e) {
         e.printStackTrace();
      }
      finally {
         //4: Closing connection
         try {
            in.close();
            requestSocket.close();
         } catch(IOException ioException) {
            ioException.printStackTrace();
         }
      }
   }
   
   
   public static void main(String args[]) {
      Client client = new Client();
      client.run();
   }
}