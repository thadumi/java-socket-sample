
package com.thadumi.socket.client;

import com.thadumi.socket.Message;

/**
 *
 * @author Dumitrescu Theodor A.
 */
@FunctionalInterface
public interface ClientMessageListener {
    public void handle(Message msg);
}
