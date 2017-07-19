
package com.thadumi.socket.client;

import com.thadumi.socket.Message;
import com.thadumi.socket.MessageListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Dumitrescu Theodor A.
 */
public class SocketClient implements Runnable{
    /*
     * Porta del server attraverso la quale il client si deve connettere
     */
    private final int port;
    
    /*
     * Inidrizzo del server al quale il client si deve connettere
     */
    private final String serverAddr;
    
    /*
     * Socket del clinet 
     */
    private final Socket socket;
    
    /*
     * Flusso di out attraverso il quale il client riceve i messaggi dal sever
     */
    private final ObjectInputStream inputStream;
    
    /*
     * Flusso di input attraverso il quale il client manda i messaggi al server
     */
    private final ObjectOutputStream outputStream;
    
    /*
     * Thread del client che si occupa dell'ascolto dei messagi in arrivo 
     */
    private Thread clientThread;
    
    /*
     * boolean che finchè è vero mantiene in vita il thread 
     */
    private boolean keepRunning;
    /*
     * Lista dei listener in ascolto dei messaggi ricevuti dal server 
     */
    private final List<ClientMessageListener> messageListeners;
    
    /**
     * Costruttore del SocketClient
     * @param serverAddr indirizzo IP del server a cui bisogna connettersi
     * @param port porta alla quale il server è in ascolto
     * @throws IOException ritorna un errore se c'è un problema durante la creazione della connessione
     */
    public SocketClient(String serverAddr, int port) throws IOException{
        this.serverAddr = serverAddr;
        this.port = port;
        socket = new Socket(InetAddress.getByName(serverAddr), port);
            
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(socket.getInputStream());
        
        messageListeners = new ArrayList<> ();
        
        clientThread = new Thread(this);
        clientThread.start();
    }

    @Override
    public void run() {
        if (keepRunning)
            throw new IllegalStateException("The clinet has already started listening once");
       
        System.out.println("Init attesa");
        keepRunning = true;
        while(keepRunning){
            try {
                System.out.println("in attesa di un messaggio");
                Message msg = (Message) inputStream.readObject();
                System.out.println("       Incoming : " + msg.toString());
                System.out.println("message get");
            }
            catch(IOException | ClassNotFoundException ex) {
                keepRunning = false;
                
                //clientThread.stop();
                
                System.err.println("Exception SocketClient run()");
            }
        }
    }
    
    /**
     * 
     * @param listener listener da aggiungere al socket
     * @return true -> listener aggiunto
     *         false -> listener non aggiunto
     */
    public boolean addMessageListener(ClientMessageListener listener) {
        return messageListeners.add(listener);
    }
    
    /**
     * 
     * @param listenerToRemove
     * @return 
     */
    public boolean removeMessageListener(MessageListener listenerToRemove) {
        return messageListeners.remove(listenerToRemove);
    }
    
    protected void fireMessage(Message msg) {
        messageListeners.stream().forEach(listener -> 
                listener.handle(msg));
    }
    
    public void send(Message msg){
        try {
            outputStream.writeObject(msg);
            outputStream.flush();
            System.out.println("Outgoing : " + msg.toString());
        } 
        catch (IOException ex) {
            System.err.println("Exception SocketClient send()");
        }
    }
    
    public void close(){
        if(clientThread != null) {
            this.clientThread = null;
            keepRunning = false;
        }
        
        try {
            if(inputStream != null) inputStream.close();
        } catch (IOException ex) {
            //Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(outputStream != null) outputStream.close();
            } catch (IOException ex) {
                //Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if(socket != null) socket.close();
                } catch (IOException iox) {
                    
                }
            }
        }
    }
}
