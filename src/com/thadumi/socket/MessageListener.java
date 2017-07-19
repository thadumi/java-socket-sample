
package com.thadumi.socket;

/**
 * Gestore dei messaggi
 * @author Dumitrescu Theodor A.
 */
@Deprecated
public interface MessageListener {
    
    /**
     * Metodo chiamato per elaborare un messaggio
     * @param ID ID del socket che chiama il messaggio
     * @param msg messaggio del client collegato al socket 
     */
    public void handle(int ID, Message msg);
    
    public void handle(Message msg);
}
