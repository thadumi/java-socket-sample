
package com.thadumi.example;

import com.thadumi.socket.Message;
import com.thadumi.socket.server.SocketServer;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 *
 * @author Dumitrescu Theodor A.
 */
public class Server {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Inserisci la porta da usare per l'ascolto: ");
        final int port = scanner.nextInt();

        final SocketServer server = new SocketServer(port);

        server.addMessageListener((int ID, Message msg) -> {
            int whatToDo = Integer.parseInt(msg.getMsgType());
            
            switch (whatToDo) {
                case 1:
                    server.findUserThread(ID).send(
                            new Message("1", "SERVER", msg.getIdSender(),
                                    msg.getContent().toUpperCase()));
                    break;
                    
                case 2:
                    server.findUserThread(ID).send(
                            new Message("2", "SERVER", msg.getIdSender(),
                                    String.valueOf(Math.pow(Integer.parseInt(msg.getContent()), 2))));
                    break;
                    
                case 3:
                    //non ho capito se si deve chiudere anche il server o no
                    //se si deve chiudere basta togliere il commento al codice
                    //server.close();
                    break;
                    
                case 4:
                    server.findUserThread(ID).send(
                            new Message("2", "SERVER", msg.getIdSender(),
                                    "Ciao " + msg.getContent()));
                    break;
                    
                case 5:
                    String domanda = msg.getContent();
                    switch (domanda) {
                        case "Chi sei?":
                            server.findUserThread(ID).send(
                                    new Message("5", "SERVER", msg.getIdSender(),
                                            "Sono il server"));
                            break;
                            
                        case "Come stai?":
                            server.findUserThread(ID).send(
                                    new Message("5", "SERVER", msg.getIdSender(),
                                            "Sto bene grazie :D"));
                            break;
                            
                        case "Dove aibiti?":
                            server.findUserThread(ID).send(
                                    new Message("5", "SERVER", msg.getIdSender(),
                                            "Abbito in un thread"));
                            break;
                            
                    }
                    
                case 6:
                    StringTokenizer parser = new StringTokenizer(msg.getContent(), "_");
                    String operatore = parser.nextToken();
                    int num1 = Integer.parseInt(parser.nextToken());
                    int num2 = Integer.parseInt(parser.nextToken());
                    
                    int res;
                    switch (operatore.charAt(0)) {
                        case '+':
                            res = num1 + num2;
                            break;
                        case '-':
                            res = num1 - num2;
                            break;
                        case '*':
                            res = num1 * num2;
                            break;
                        case '/':
                            res = num1 / num2;
                            break;
                            
                        default:
                            res = Integer.MAX_VALUE;
                            
                    }
                    server.findUserThread(ID).send(
                            new Message("6", "SERVER", msg.getIdSender(),
                                    num1 + " " + operatore + " " + num2 + " = " + res));
                    break;
                    
                case 7:
                    StringTokenizer _parser = new StringTokenizer(msg.getContent(), "_");
                    int _num1 = Integer.parseInt(_parser.nextToken());
                    int _num2 = Integer.parseInt(_parser.nextToken());
                    String _operatore = _parser.nextToken();
                    
                    int _res;
                    switch (_operatore.charAt(0)) {
                        case '+':
                            _res = _num1 + _num2;
                            break;
                        case '-':
                            _res = _num1 - _num2;
                            break;
                        case '*':
                            _res = _num1 * _num2;
                            break;
                        case '/':
                            _res = _num1 / _num2;
                            break;
                            
                        default:
                            _res = Integer.MAX_VALUE;
                            
                    }
                    server.findUserThread(ID).send(
                            new Message("7", "SERVER", msg.getIdSender(),
                                    _num1 + " " + _operatore + " " + _num2 + " = " + _res));
                    break;
                    
                default:
                    server.findUserThread(ID).send(
                            new Message("def", "SERVER", msg.getIdSender(),
                                    "operazione non riconosciuta: " + msg.getContent()));
            }
        });
        
        
        server.start();
    }
}
