package com.company;

import java.io.*;
import java.net.Socket;

public class Client {

    private Socket socket;

    public Client(String addr,int port)throws IOException {
        socket = new Socket(addr,port);
    }

    public void loadFile(String fname,DataOutputStream outStream)throws IOException{
        File file = new File(fname);
        if(file.length() != 0) {
            outStream.writeLong(file.length());
            outStream.writeUTF(file.getName());
            FileInputStream inputFile = new FileInputStream(file);
            byte[] buffer = new byte[64 * 1024];
            int count;
            while ((count = inputFile.read(buffer)) != -1) {
                outStream.write(buffer, 0, count);
            }
            outStream.flush();
            inputFile.close();
            outStream.close();
            System.out.println("File transferred " + fname);
        }else{
            System.out.println("Empty file");
        }
    }

    public void work(){
        try(
                DataOutputStream outStream=new DataOutputStream(socket.getOutputStream());
                BufferedReader br=new BufferedReader(new InputStreamReader(System.in))){
            String fileName="";
            System.out.println("Enter file: ");
            fileName = br.readLine();
            if(fileName.isEmpty() || fileName.getBytes().length > 4096) {
                System.out.println("Incorrect fileName try again: ");
            }
            loadFile(fileName,outStream);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length < 2)
                throw new Exception("Need more arguments");
            Client client = new Client(args[0],Integer.parseInt(args[1]));
            client.work();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
