package com.company;

import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private ServerSocket serverSocket;
    private int listenPort;
    static ExecutorService service = Executors.newCachedThreadPool();

    public Server(int listenPort){
        this.listenPort = listenPort;
    }

    public void work(){
        System.out.println("#Server starting...");

        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> {
                    try {
//                        closeClients();
                        if (!serverSocket.isClosed()) {
                            serverSocket.close();
                        }
                        System.out.println("Server is down.");
                    }
                    catch (IOException exc) {
                        System.err.println(exc.getMessage());
                    }
                }
        ));

        try{
            InetAddress serverAddress = InetAddress.getLocalHost();
            serverSocket = new ServerSocket(listenPort);
            System.out.println("Server IP: " + serverAddress);

            Path uploads = Paths.get("uploads");
            if (!Files.exists(uploads)) {
                Files.createDirectory(uploads);
            }

            while(!serverSocket.isClosed()){
                System.out.println("#Server waiting for connections...");
                Socket client = serverSocket.accept();
                service.execute(new ServerThread(client));
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        finally {
            System.out.println("Server finished");
        }
    }

    public static void main(String args[]){
        try {
            if (args.length < 1)
                throw new Exception("Need more arguments");
            Server server = new Server(Integer.parseInt(args[0]));
            server.work();
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
