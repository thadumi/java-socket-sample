package com.thadumi.socket.server;

import com.thadumi.socket.Message;

/**
 *
 * @author Dumitrescu Theodor A.
 */
@FunctionalInterface
public interface ServerMessageListener {

    /**
     * Metodo chiamato per elaborare un messaggio
     *
     * @param ID ID del socket che chiama il messaggio
     * @param msg messaggio del client collegato al socket
     */
    public void handle(int ID, Message msg);
}
