package com.thadumi.example;

import com.thadumi.socket.client.ClientMessageListener;
import com.thadumi.socket.Message;
import com.thadumi.socket.client.SocketClient;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dumitrescu Theodor A.
 */
public class Client {

    static void printMenu() {
        System.out.println("1. Invio di una stringa al server");
        System.out.println("2. Invio di un numero al server");
        System.out.println("3. Chiudi connessione");
        System.out.println("4. Saltua server (nome e cognome)");
        System.out.println("5. Fai domande al server");
        System.out.println("6. Calcolatrice RPN");
        System.out.println("7. Calcolatrice");
    }

    public static void main(String[] arg) {

        final int port;
        final String serverAddr;

        final Scanner scanner = new Scanner(System.in);

        System.out.print("Inserisci l'IP del server: ");
        serverAddr = scanner.nextLine();
        System.out.print("\nInserisci la porta di ascolto: ");
        port = scanner.nextInt();

        SocketClient client = null;

        try {
            client = new SocketClient(serverAddr, port);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Impossibile stabilie una connessione perchè:" + ex.getMessage());
        }

        if (client != null) {

            client.addMessageListener((Message msg) -> {
                System.out.println("   Risposta del server: " + msg.getContent());
            });

            short whatToDo = 0;

            while (whatToDo != 3) {
                Client.printMenu();
                System.out.print("Cosa fare: ");
                whatToDo = scanner.nextShort();

                System.out.println("\n");
                switch (whatToDo) {
                    case 1:
                        System.out.print("Inserisci stringa da inviare al server: ");
                        String tmp = scanner.nextLine();
                        client.send(new Message("1", "client", "SERVER", tmp));
                        break;

                    case 2:
                        int nmb;
                        System.out.print("Inserisci il numero da inivare al server");
                        nmb = scanner.nextInt();
                        client.send(new Message("2", "clinet", "SERVER", String.valueOf(nmb)));
                        break;

                    case 3:
                        System.out.print("Inserisci STOP per confermare la cahiusura");
                        String temp = scanner.nextLine();
                        client.send(new Message("3", "client", "SERVER", temp));
                        if (temp.equals("STOP")) {
                            client.close();
                        }
                        break;

                    case 4:
                        System.out.print("inserisci il nome: ");
                        String nome = scanner.nextLine();
                        String cognome = scanner.nextLine();
                        client.send(new Message("4", "client", "SERVER", nome + " " + cognome));
                        break;

                    case 5:
                        System.out.print("inserisci il nome: ");
                        String domanda = scanner.nextLine();
                        client.send(new Message("5", "client", "SERVER", domanda));
                        break;

                    case 6:
                        System.out.print("Insersci l'operando: ");
                        String operazione = scanner.nextLine();
                        if (operazione.length() > 1) {
                            System.err.println("Insierisci il simbolo dell'operazione es + - *");
                            break;
                        }

                        System.out.println("Inserisci il primo numero: ");
                        int primoNr = scanner.nextInt();
                        System.out.println("Inserisci il secondo numero: ");
                        int secondoNr = scanner.nextInt();

                        client.send(new Message("6", "client", "SERVER", operazione + "_" + primoNr + "_" + secondoNr));
                        break;

                    case 7:
                        System.out.print("Inserisci il primo numero: ");
                        int _primoNr = scanner.nextInt();
                        System.out.print("Inserisci il secondo numero: ");
                        int _secondoNr = scanner.nextInt();

                        System.out.print("Insersci l'operando: ");
                        String _operazione = scanner.nextLine();
                        if (_operazione.length() > 1) {
                            System.err.println("Insierisci il simbolo dell'operazione es + - *");
                            break;
                        }

                        client.send(new Message("7", "client", "SERVER", _primoNr + "_" + _secondoNr + "_" + _operazione));
                        break;

                    default:
                        System.err.println("\n\nValore non valido\n\n\n");
                }
            }
        }

        scanner.close();

    }
}
