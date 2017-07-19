
package com.thadumi.socket.server;

import com.thadumi.socket.ExceptionListener;
import com.thadumi.socket.ExceptionListener;
import com.thadumi.socket.Message;
import com.thadumi.socket.Message;
import com.thadumi.socket.MessageListener;
import com.thadumi.socket.MessageListener;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Dumitrescu Theodor A.
 */
public final class SocketServer implements Runnable, Closeable /*, AutoCloseable*/ {
    
    /*
     * Pool dei thread utilizzati dal server. La grandezza della pool viene specificata
     * nel costruttore e non può essere cambiata a runtime
     */
    private final ServerThread clients[];
    
    /*
     * Questa pool contiene tutti i clinet che si vogliono connette al server
     * ma questo ha già raggiunto il numero massimo di clinet da gestire
     */
    private final List<Socket> clientsOverPoll;
    
    private final List<ServerMessageListener> messageListeners;
    
    private final List<ExceptionListener> exceptionListeners;
    
    /*
     * Socket che utlizza il server per rimanere in attesa della connessione dei clinet
     */
    private ServerSocket server = null;
    
    /*
     * Thread che esegue le operazioni del server, ovvero rimanere in attesa di nuovi clinete
     */
    private Thread thread = null;
    
    /*
     * boolean che mantiene in vita il thread finchè è vero
     */
    private boolean keepRunning;
    
    /*
     * clientCount rappresata il numero di clinet attualmente connessi.
     * Non può superare  il numero massimo di thread che può contenere la pool.
     * port è la porta su cui di default il server è in ascolto
     */
    private int clientCount , port;
    
    /**
     * Costruttore del server. 
     * Si occupa della creazione del ServerSocket e di iniializzare la pool
     * @param _port porta su cui il server rimane in attesa di connessioni
     * @param maxThreadPool grandezza della pool di thread
     */
    public SocketServer(int _port, int maxThreadPool) {

        clients = new ServerThread[maxThreadPool];
        
        clientsOverPoll    = new ArrayList<> ();
        messageListeners   = new ArrayList<> ();
        exceptionListeners = new ArrayList<> ();
        /*
         * contatore a 0 -> non ci sono thread in esecuzione
         */
        clientCount = 0;
        port = _port;
        
        /*
         * l'esecuzione del thread deve ancora iniziare 
         */
        keepRunning = false;
        
        try {
            server = new ServerSocket(port);
            port = server.getLocalPort();
            System.out.println("Server startet. IP : " + InetAddress.getLocalHost() + ", Port : " + server.getLocalPort());
            start();
        } catch (IOException ioe) {
            System.err.println("\nCan not bind to port " + port + ": " + ioe.getMessage());
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { SocketServer.this.stop(); }));
    }
    
    /**
     * @see SocketServerv2(int, int)
     * La grandezza della pool sarà 50. Ciò significa che ci saranno 50 clinet
     * al massimo che possono comunicare contemporaneamente
     * @param port porta su cui il server rimane in attesa di connessioni
     */
    public SocketServer(int port) {
        this(port, 50);
    }
    
    public SocketServer() {
        this(13000, 50);
    }

    @Override
    public void run() {
        while (thread != null && keepRunning) {
            try {
                System.out.println("\nWaiting for a client ...");
                /*
                 * il server rimana in attesa di un clinet e appena uno si connette
                 * crea il thread che lo rappresenta
                 */
                addThread(server.accept());
            } catch (Exception ioe) {
                System.err.println("\nServer accept error: \n");
                fireException(ioe);
            }
        }
    }

    public void start() {
        if (thread == null && !keepRunning) {
            keepRunning = true;
            thread = new Thread(this);
            thread.start();
        }
    }

    //@SuppressWarnings("deprecation")
    public void stop() {
        if (thread != null && keepRunning) {
            broadcast(new Message(Message.SERVER_CLOSING_TYPE, "SERVER", "ALL", "the server is closing"));
            keepRunning = false;
            //thread.stop();
            thread = null;
            
            try {
                server.close();
            } catch (IOException iox) {
                System.err.println("Error closing the server " + iox);
            }
        }
    }
    
        
    @Override
    public void close() {
        stop();
    }
    
    /**
     * Cerca il thread che ha ID all'ID passato
     * @param ID ID del thread da cercare
     * @return  indice del thread nella pool 
     *          -1 se il thread con quell'ID non esiste
     */
    private int findClient(int ID) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getID() == ID) {
                return i;
            }
        }
        return -1;
    }
    
    public ServerThread findUserThread(int ID) {
        return clients[findClient(ID)];
    }
    
    public boolean addExceptionListener(ExceptionListener listener) {
        return exceptionListeners.add(listener);
    }
    
    public boolean removeExceptionListener(ExceptionListener listnerToRemove) {
        return exceptionListeners.remove(listnerToRemove);
    }
    
    protected void fireException(Exception ex) {
        exceptionListeners.stream().forEach((ExceptionListener listener) ->
                listener.handle(ex));
    }    
    
    public boolean addMessageListener(ServerMessageListener listener) {
        return messageListeners.add(listener);
    }
    
    public boolean removeMessaListener(ServerMessageListener listnerToRemove) {
        return messageListeners.remove(listnerToRemove);
    }
    
    /**
    protected void fireMessage(Message msg) {
        messageListeners.stream().forEach((MessageListener listener) ->
                listener.handle(msg));
    }
    */
    
    protected void fireMessage(int ID, Message msg) {
        messageListeners.stream().forEach(listener ->
                listener.handle(ID, msg));
    }
    
    /**
     * Metodo chiamato dal ServerThread quando ricevono un messaggio.
     * Il messaggio viene inoltre inviato ai message listener del server
     * @param ID ID del thread a cui mandare il messaggio
     * @param msg Messsaggio ricevuto
     */
    protected synchronized void handle(int ID, Message msg) {
        System.out.println(".....messaggio da mandare" + msg);
        clients[findClient(ID)].send(msg);
        System.out.println(".....pos " + findClient(ID));
        fireMessage(ID, msg);
    }
    
    /**
     * Invia un messaggio a tutti i thread della pool
     * @param type tipo di messaggio
     * @param sender ID di chi invia il messaggio
     * @param content contenuto del messaggio
     */
    public void broadcast(String type, String sender, String content) {
        Message msg = new Message(type, sender, "All", content);
        broadcast(msg);
        /*for (int i = 0; i < clientCount; i++) {
            clients[i].send(msg);
        }*/
    }
    
    /**
     * Invia un messaggio a tutti i thread della pool
     * @param msg messaggio da inviare
     */
    public void broadcast(Message msg) {
        for (int i = 0; i < clientCount; i++) {
            clients[i].send(msg);
        }
    }
    
    /**
     * Invia la lista delgi clinet connessi al server. La lista è inviato tramite
     * una sequenza i messaggi ed ogni messaggio contiene l'idSocket di ogni thread
     * @param toWhom idSocket del clinet a cui inviare la lista
     */
    public void sendUserList(String toWhom) {
        for (int i = 0; i < clientCount; i++) {
            findUserThread(toWhom).send(new Message("newuser", "SERVER", toWhom, clients[i].idSocket));
        }
    }
    
    /**
     * Ritorna il ServerThread, presente nella pool, avente idSocket uguale a whatSocket
     * @param whatThread
     * @return ServerThread se vine trovate
     *         null se non viene trovato
     */
    public ServerThread findUserThread(String whatThread) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].idSocket.equals(whatThread)) {
                return clients[i];
            }
        }
        return null;
    }
    
    /**
     * Rimuove il thread avente ID pari al ID passato.
     * Viene inviato un messaggio in automati al client per avvertirlo che è stato
     * disconnesso
     * @param ID ID univoco del thread da rimuovere dalla pool
     */
    //@SuppressWarnings("deprecation")
    public synchronized void remove(int ID) {
        int pos = findClient(ID);
        if (pos >= 0) {
            ServerThread toTerminate = clients[pos];
            System.out.println("\nRemoving client thread " + ID + " at " + pos);
            if (pos < clientCount - 1) {
                for (int i = pos + 1; i < clientCount; i++) {
                    clients[i - 1] = clients[i];
                }
            }
            clientCount--;
            
            try {
                toTerminate.send(new Message(Message.SERVER_CLOSING_TYPE, "SERVER", toTerminate.getIdSocket(), "this port is closing"));
                toTerminate.close();
            } catch (IOException ioe) {
                System.out.println("\nError closing thread: " + ioe);
            }
            //toTerminate.stop();
            
            /*
             * rimuovo il primo clinet dalla lista di attesa e
             * lo aggiungo alla lista dei thread in esecuzione
             */
            if(clientsOverPoll.size() > 0) {
                /*clients[clientCount] = clientsOverPoll.remove(0);
                try {
                    clients[clientCount].open();
                    clients[clientCount].start();
                    clientCount++;
                } catch (IOException ioe) {
                    System.out.println("\nError opening thread: " + ioe);
                }*/
                
                addThread(clientsOverPoll.remove(0));
            }
        }
    }
    
    /**
     * Cerca di aggiungere un thread alla pool avente il socket passato,
     * @param socket Socket da aggiungere alla pool 
     * @return  1 -> il thread è stato creato e aggiunto alla pool
     *          0 -> se è stato aggiunto alla pool dei thread in attesa
     *         -1 -> thread non è stato aggiunto
     */
    private int addThread(Socket socket) {
        if (clientCount < clients.length) {
            System.out.println("\nClient accepted: " + socket);
            clients[clientCount] = new ServerThread(this, socket);
            try {
                clients[clientCount].open();
                clients[clientCount].start();
                clientCount++;
            
                return 1;
            } catch (IOException ioe) {
                System.err.println("\nError opening thread: " + ioe);
                return -1;
            }
        } else {
            System.out.println("\nClient refused: maximum " + clients.length + " reached.");
            return clientsOverPoll.add(socket) ? 0 : -1;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    }
    
    
    
    /**
     * Definisce i thread che utilizza il server per gestire i socket dei
     * client collegati
     */
    public static class ServerThread extends Thread {
        /*
         * server a cui appartiene il thread
         */

        private final SocketServer server;

        /**
         * socket che utlizza il thread per comunicare col client che è in
         * ascolto su quella specifica porta
         */
        private final Socket socket;

        /**
         * Siccome ogni thread è collegato al socket di un client e ad ogni
         * socket viene assegnata una porta che appartiene solo ad esso
         * (ovviamente finchè è in esecuzione il rispettivo socket); viene
         * utilizzato il numero della porta come identificativo per trovare il
         * thread nella pool
         */
        private int ID = -1;

        /*
         * L'idSocket ha il stesso socopo dell'ID ma con la differenz che questo
         * identificativo potrebbe non essere univoco, per questo bisognerebbe evitare
         * di richiedere un thread tramite questo id poichè se ce ne sono più di uno
         * col stesso identificativo verrà restituito il primo trovato.
         * Può essere utile però per avere un altro identificativo del thread da utilzzare
         * quando di gestiscono, ad esempio, chat perchè si potrebbe assegnare a questo id
         * l'username del rispettivo client per evitare di creare una propria tabella in cui
         * memorizzare l'associazione tra un client e la porta a cui è collegato
         */
        private String idSocket = "";

        /*
         * Stream utilizzato dal socket per inviare i messagi al clinet 
         * il stream va utlizzato tramite la funzione send(Message) 
         * per più dettagli @see SocketServer.SocketThread::send(Message)
         */
        private ObjectInputStream streamIn = null;

        /*
         * Stream utilizzato dal socket per ricevere i messaggi dal clinet 
         * il stream va utlizzato tramite i MessageListener
         * che devono essere aggiunti al server tramite
         * @see SocketServer::addMessageListener(MessageListener)
         */
        private ObjectOutputStream streamOut = null;
        
        /*
         * boolean che mantiene in vita il thread
         */
        private boolean keepRunning;
        
        /**
         * @param _server server che a creato il thread
         * @param _socket socket sui cui opera il thread
         */
        public ServerThread(SocketServer _server, Socket _socket) {
            super();
            server = _server;
            socket = _socket;
            ID = socket.getPort();
        }
        
        /**
         * Invia un messaggio al clinet a cui è collegato il suo socket
         * @param msg messaggio da inviare al clinet
         */
        public void send(Message msg) {
            try {
                streamOut.writeObject(msg);
                streamOut.flush();
            } catch (IOException ex) {
                System.err.println("Exception [SocketClient : send(...)]");
            }
        }
        
        /**
         * L'idSocket è un indentificatore del socket.
         * L'identificativo potrebbe non essere univoco, per questo bisognerebbe evitare
         * di richiedere un thread tramite questo id poichè se ce ne sono più di uno
         * col stesso identificativo verrà restituito il primo trovato.
         * Può essere utile però per avere un altro identificativo del thread da utilzzare
         * quando di gestiscono, ad esempio, chat perchè si potrebbe assegnare a questo id
         * l'username del rispettivo client per evitare di creare una propria tabella in cui
         * memorizzare l'associazione tra un client e la porta a cui è collegato
         *
         * @param newIdSocket ID che si vuole assegnare la socket
         * @return ID assegnato al socket
         */
        public String setIdSocket(String newIdSocket) {
            try {
                setName(idSocket);
            } catch (SecurityException ex) {}
            
            return idSocket = newIdSocket;
        }

        /**
         * Ritorna l'ID univoco del thread. Questo identificativo è 
         * univoco perchè rappresenta anche la porta a cui è collegato il 
         * suo socket. 
         * Si cosiglia di utilizzare questo id per ottenere un thread dalla pool
         * @return ID univoco del socket
         */    
        public int getID() {
            return ID;
        }
        
        /**
         * @return restituisce l'ID, potenzialemente non univocolo del thread,
         *         questo identificativo deve essere assegnato dall'utente tramite
         *         see  SocketServerv2.SocketThreadv2::setIdSocket(String)
         */
        public String getIdSocket() {
            return idSocket;
        }

        /**
         * Codice eseguito dal thread durante la sua esistenza. Questo metodo non
         * deve mai essere chiamato direttamente (bisogna chiamare start()).
         */
        @Override
        public void run() {
            keepRunning = true;
            
            /*
             * cliclo che continua finchè keepRunning non divnta false che mantine il socket in attesa di un input dal stream
             * finchè non si verifica un errore, o finchè non viene chiamato il metodo
             * close(). Se si riceve un messaggio questo viene passato al server insieme
             * al ID del thread
             */
            System.out.println("\nServer Thread " + ID + " running.");
            while (keepRunning) {
                try {
                    System.out.println("In attesa di un messagio");
                    Message msg = (Message) streamIn.readObject();
                    System.out.println("Messaggio ricevuto");
                    server.handle(ID, msg);
                } catch (IOException | ClassNotFoundException ioe) {
                    //System.err.println(ID + " ERROR reading: " + ioe.getMessage());
                    server.remove(ID);
                    break;
                }
            }
        }
        
        /**
         * Apre i flussi di I/O del socket
         * @throws java.io.IOException
         */
        public void open() throws IOException {
            streamOut = new ObjectOutputStream(socket.getOutputStream());
            streamOut.flush();
            streamIn = new ObjectInputStream(socket.getInputStream());
        }
        
        /**
         * Chiude l'esecuzione di flussi e termina l'esecuzione del thread
         * 
         * @throws IOException Ritorna un IOException se si verificano errori durante
         *         la chiusura dei stream
         */
        public void close() throws IOException {
            if (socket != null) {
                send(new Message(Message.SERVER_CLOSING_TYPE, "SERVER", "ALL", "this port is closing"));
                socket.close();
            }
            if (streamIn != null) {
                streamIn.close();
            }
            if (streamOut != null) {
                streamOut.close();
            }
            if(keepRunning) keepRunning = false;
        }
        
        @Override
        protected void finalize() throws Throwable {
            close();
            super.finalize();
        }
    }

}
