package com.company;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.*;

public class ServerThread extends Thread{
    private Socket clientSocket;
    DataInputStream inStream;

    public ServerThread(Socket inSocket) throws IOException{
        this.clientSocket = inSocket;
        inStream = new DataInputStream(clientSocket.getInputStream());
    }

    private void receiveFile(String fileName, long fileSize) {
        try {
            byte[] buffer = new byte[64 * 1024];
            Path filePath = Paths.get("uploads\\" + fileName);
            StringBuilder realFileName = new StringBuilder(fileName);
            if (!Files.isDirectory(filePath)) {
                while (Files.exists(filePath)) {
                    realFileName.insert(0, "1");
                    filePath = Paths.get("uploads\\" + realFileName.toString());
                }
            } else {
                throw new FileNotFoundException("It's not a file");
            }
            OutputStream outputFile = Files.newOutputStream(filePath, CREATE, WRITE);


            int count, total = 0, countBytesForSpeed = 0;
            long timeCur,timeLast,timeEnd,timeStart = System.currentTimeMillis();
            timeLast = timeStart;
            while ((count = inStream.read(buffer)) != -1) {
                total += count;
                countBytesForSpeed += count;
                timeCur = System.currentTimeMillis();
                if(timeCur - timeLast > 3000){
                    System.out.println("Downloading speed: " + Math.round(countBytesForSpeed/(((timeCur-timeLast)*1.0)/1000)/1024) + "KB/s" + "| FileName: " + fileName + "| from Client: #" + this.getId());
                    timeLast = timeCur;
                    countBytesForSpeed = 0;
                }
                outputFile.write(buffer, 0, count);
            }
            timeEnd = System.currentTimeMillis();
            System.out.println("Average speed " + Math.round((total/(((timeEnd - timeStart)*1.0)/1000))/1024) + "KB/s" + " FileName: " + fileName + "| from Client: #" + this.getId());


            outputFile.flush();
            if(fileSize != Files.size(filePath)) {
                System.out.println("File integrity error: " + fileName + " from # " + this.getId());
                Files.delete(filePath);
            } else {
                System.out.println("File " + fileName + " received " + " from # " + this.getId());
            }
            outputFile.close();
        } catch (IOException ignore) {}
    }


    @Override
    public void run(){
        try {
            System.out.println("Connected with host: " + clientSocket.getInetAddress() + "\nPort: " + clientSocket.getPort() + "\nNumber: " + this.getId() + " :D");
            long fileSize = inStream.readLong();
            String fileName = inStream.readUTF();
            System.out.println("Starting receiving: " + fileName + " from " + this.getId());
            receiveFile(fileName, fileSize);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }finally {
            try {
                clientSocket.close();
                inStream.close();
            }catch (IOException e){
            }
            System.out.println("---> Client # "+this.getId()+" disconnected :<<");
        }

    }

}

