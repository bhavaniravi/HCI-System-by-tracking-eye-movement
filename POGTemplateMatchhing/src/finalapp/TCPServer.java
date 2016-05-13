package finalapp;



import java.io.*;

import java.net.ServerSocket;

import java.net.Socket;

/* The class extends the Thread class so we can receive and send messages at the same time*/

public class TCPServer extends Thread {


public static final int SERVERPORT = 4444;

private boolean running = false;

private PrintWriter mOut;

/* Method to send the messages from server to client @param message the message sent by the server*/

public void sendMessage(String message){

if (mOut != null && !mOut.checkError()) {

mOut.println(message);

mOut.flush();

}

}

@Override

public void run() {

super.run();

running = true;

try {

System.out.println("S: Connecting…");


//create a server socket. A server socket waits for requests to come in over the network.

ServerSocket serverSocket = new ServerSocket(SERVERPORT);

//create client socket… the method accept() listens for a connection to be made to this socket //and accepts it.

Socket client = serverSocket.accept();

System.out.println("S: Receiving…");


try {
mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
}

 catch (Exception e) {

System.out.println("S: Error");

e.printStackTrace();

} finally {

System.out.println("S: Done.");

}


} catch (Exception e) {

System.out.println("S: Error");

e.printStackTrace();

}

}

//Declare the interface. The method messageReceived(String message) will must be implemented in the ServerBoard

//class at on startServer button click



}

