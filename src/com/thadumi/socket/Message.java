
package com.thadumi.socket;

import java.io.Serializable;

/**
 *
 * @author nefirus
 */
public class Message implements Serializable{
    private static final long serialVersionUID = 1L;
    
    public static final String MESSAGE_TYPE        = "DEFAULT_MESSAGGE_TYPE";
    public static final String OPERATION_TYPE      = "DEFAULT_OPERATION_TYPE";
    public static final String CONNECTION_TYPE     = "DEFAULT_OPERATION_TYPE";
    public static final String NEED_TO_WAIT_TYPE   = "DEFAULT_NEED_TO_WAIT_TYPE";
    public static final String SERVER_CLOSING_TYPE = "DEFAULT_SERVER_CLOSING_TYPE";
    
    public static final String BROADCAST_RECIVER   = "ALL";
    
    /*public static enum TYPES{
        MESSAGE_TYPE, //per trasmissione dati dell'utente
        OPERATION_TYPE //per trasmissione dati del sistema
    };*/
    
    private final String idSender;
    private final String idRecipient;
    //private final Message.TYPES type;
    private final String type;
    private final String content;

    public Message(String _type, String _idSender, String _idRecipeint, String _content) {
        this.idSender = _idSender;
        this.idRecipient = _idRecipeint;
        this.type = _type;
        this.content = _content;
    }
    
    public Message(String _idSender, String _idRecipeint, String _content) {
        this(Message.MESSAGE_TYPE, _idSender, _idRecipeint, _content);
    }
    
    public String getIdSender() { return idSender; }
    public String getIdRecipient() { return idRecipient; }
    public String getMsgType() { return type; }
    public String getContent() { return content; }
    
    @Override
    public String toString() {
        return "msg[type: " + type + ", "
                + "idSender: " + idSender + ", "
                + "idRecipient: " + idRecipient + ", "
                + "content: " + content + "]"; 
    }
    
    
}
