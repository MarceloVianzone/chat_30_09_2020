package com.company;

import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Scanner;
import javax.swing.text.*;
import java.util.concurrent.*;
import java.nio.charset.StandardCharsets;

import static com.company.InvioPacchetti.*;
import static com.company.InvioPacchetti.login;

public class Grafica extends JFrame implements Runnable{
    //----------| VARIABILI GLOBALI |----------
    //dati per la grafica
    protected static final int larghezza = 800, altezza=500;
    protected JPanel chat, console;
    protected JButton invio;
    protected JTextField inserimento;
    protected JTextPane messaggi;
    protected String messaggio;

    //dati generali
    protected static final int porta=2000;
    protected byte[] tunnel;
    protected boolean errore;
    protected DatagramSocket socket=null;
    protected DatagramPacket paccInv,paccRic;
    protected InetAddress ip;
    protected String nickname;
    //protected static final String ipNome ="172.16.3.50";
    //protected static final String ipNome ="172.16.20.70";
    protected static final String ipNome = "172.16.20.90";
    //protected static final String ipNome = "3.130.135.149";

    //Unico COSTRUTTORE
    public Grafica(){
        this.logIn();
        errore = false;

        //vera parte grafica
        setSize(larghezza,altezza);
        setTitle("Marcelo's Chat xD");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        try{
            ip = InetAddress.getByName(ipNome);
            //ip = InetAddress.getByName("192.168.137.165");
            socket.setSoTimeout(1000);
        }catch (Exception exception){
            System.out.println(exception);
        }
        tunnel = new byte[512];

        chatPubblica();
        add(chat);
        setVisible(true);
        setLocation(400,200);
        //Separo il thread principale dal figlio
        Thread thread = Thread.currentThread();
        thread.setName("Principale");
        thread.setPriority(10);
        //-------------------------
        Thread f = new Thread(this, "Figlio1");
        f.start();

        input();
    }

    //sottoprogramma per la creazione della grafica
    private void chatPubblica(){
        //Creo la chat e gli do un layout a lati
        chat = new JPanel();
        chat.setLayout(new BorderLayout());

        //creo il JPanel che conterrà la console e il tasto invio
        console = new JPanel();
        console.setLayout(new BorderLayout());

        //creo e aggiungo la parte grafica che mi renderà possibile vedere i messaggi ricevuti
        //JEditorPane messaggi = new JEditorPane();
        messaggi = new JTextPane();
        chat.add(messaggi);


        //creo il campo per inserire i dati e il tasto invio, poi li aggiungo al pannello console
        inserimento = new JTextField("");
        inserimento.addActionListener(this::gestore);
        invio = new JButton("<--|");
        invio.addActionListener(this::gestore);
        console.add(invio, BorderLayout.EAST);
        console.add(inserimento);

        //aggiungo il pannello console al pannello principale chat
        chat.add(console, BorderLayout.SOUTH);

        messaggio="";


    }

    //sottoprogramma per tentare il login
    private void logIn(){
        int contatore = 0;
        try {
            ip = InetAddress.getByName(ipNome);
            //ip = InetAddress.getByName("192.168.137.39");
            socket = new DatagramSocket();
            socket.setSoTimeout(1000);

        }catch (Exception e){
            System.out.println(e);
        }
        Scanner t = new Scanner(System.in);


        //Tentiamo il LOGIN
        String nickname="";

        //finchè il server non dice ok
        try{
            byte rBuf[] = new byte[512];
            rBuf[0]=1;
            while(rBuf[0]!=0){
                //Finchè il nome non è accettabile
                while(!nome(nickname)){
                    System.out.print("Come vuoi chiamarti? ");
                    nickname=t.nextLine();
                }

                byte br[] = login(nickname);
                paccInv = new DatagramPacket(br, br.length,ip,porta);
                socket.send(paccInv);

                try{
                    rBuf = null;
                    rBuf = new byte[512];
                    paccRic = new DatagramPacket(rBuf,rBuf.length);
                    socket.receive(paccRic);
                }catch (java.net.SocketTimeoutException socketTimeoutException){
                    rBuf[0]=2;
                }
                if (rBuf[0]==1){
                    System.out.println("ERRORE! Il nome non va bene! Cambialo!");
                    nickname = "";
                }
                else if(rBuf[0]==2) {
                    System.out.println("ERRORE! Il server non risponde!\tRiprovo. " + (9 - contatore) + " tentativi rimasti...");
                    contatore++;
                }
                //Se il programma prova a contattare il servere e quest'ultimo non risponde per 10 volte, il programma termina
                if (contatore>9){
                    System.out.println("Troppi tentativi di connessione vani.\nChiusura programma in corso...");
                    System.exit(0);
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }



        //LOGIN EFFETTUATO
        String comando = "";
        System.out.println("CIAO! "+nickname+" Benvenuto in questa chat\n");
        this.nickname=nickname;
    }

    //thread principale che ascolta la "Console" della chat
    private void input(){
        while(true){

            try {TimeUnit.MILLISECONDS.sleep(1);}
            catch (InterruptedException e) {System.out.println(e);}

            //Messaggio pubblico
            if (!this.messaggio.equals("") && this.messaggio.charAt(0)!='/' ){
                //creo e spedisco il messaggio
                sendMex(PublicMessage(this.messaggio));
                this.messaggio="";
            }
            //Messaggio per mandare un messaggio privato
            else if (this.messaggio.length()>=4 && (this.messaggio.substring(0,4).equals("/msg"))){
                String destinatario="", messaggio;

                //Estrazione del nome
                int i;
                for (i = 5; this.messaggio.charAt(i)!=' ' ; i++)
                    destinatario+=this.messaggio.charAt(i);
                //Nome ricavato
                //Estrazione del messaggio
                i++;
                messaggio = this.messaggio.substring(i);
                //messaggio ricavato
                //Invio del messaggio privato
                sendMex(PrivateMessage(destinatario,messaggio));
                //Attendo la risposta del server per 20 millisecondi
                try {Thread.sleep(20);}catch (InterruptedException e){System.out.println(e);}
                //Se il server da errore, allora non esiste nessuno con quel nome
                if (errore) {
                    JOptionPane.showMessageDialog(null,"Errore! Non esiste nessuno chiamato: "+destinatario,"Destinatario inesistente", JOptionPane.ERROR_MESSAGE);
                    errore = false;
                }else{
                    appendToPane(messaggi, nickname+" --> "+destinatario+": "+messaggio+"\n", Color.black);
                }
                this.messaggio="";
            }
            //Messaggio della lista
            else if (this.messaggio.equals("/lista")){
                byte lista[] = lista();
                paccInv = new DatagramPacket(lista, lista.length,ip,porta);
                try {
                    socket.send(paccInv);
                } catch (IOException e) {
                    System.out.println(e);
                }
                this.messaggio="";
            }
            //Messaggio di logout
            else if(this.messaggio.equals("/logout") || this.messaggio.equals("/exit")){
                int risposta = JOptionPane.showConfirmDialog(null,
                        "Vuoi davvero uscire?","EXIT",JOptionPane.YES_NO_OPTION);
                if(risposta == JOptionPane.YES_OPTION) {
                    byte logout[] = logout();
                    paccInv = new DatagramPacket(logout, logout.length,ip,porta);
                    try {
                        socket.send(paccInv);
                    } catch (IOException e) {
                        System.out.println(e);
                    }
                    System.exit(0);
                }else{
                    this.messaggio="";
                }
            }
        }
    }

    //sottoprogramma che manda i pacchetti
    public void sendMex(byte mex[]){
        try{
            paccInv = new DatagramPacket(mex, mex.length,ip,porta);
            socket.send(paccInv);
        }catch (Exception e){
            System.out.println(e);
        }
    }

    @Override   //Thread che ascolta e gestisce i pacchetti in arrivo
    public void run() {
        while(true){
            byte paccRic[] = new byte[512];
            this.paccRic = new DatagramPacket(paccRic,paccRic.length);
            try {
                socket.setSoTimeout(0);
                socket.receive(this.paccRic);

            } catch (Exception e){System.out.println(e);}
            //Ritorno della LISTA UTENTI


            if (paccRic[0]==43){
                JFrame lista = new JFrame("Lista utenti");
                lista.setSize(200,400);
                lista.setResizable(false);
                JTextArea utenti = new JTextArea();

                for (int i = 3; i < paccRic.length; i++) {
                    if (paccRic[i] != 0)
                        utenti.append((char) paccRic[i] + "");
                    else if (paccRic[i - 1] != 0)
                        utenti.append("\n");
                    if (paccRic[i]==0 && paccRic[i-1]==0 && paccRic[i-2]==0)
                        break;
                }

                lista.add(utenti);
                lista.setVisible(true);
            }
            else if(paccRic[0]==21)
                appendToPane(messaggi,getMex(paccRic)+"\n",Color.black);
            else if(paccRic[0]==23)
                appendToPane(messaggi, getMex(paccRic)+"\n",Color.cyan);
            else if (paccRic[0]==1)
                errore=true;

        }
    }

    private String getMex(byte pacchetto[]){
        int contatoreNome=0, contatoreMex=0;

        //decriptazione nome
        for (int i = 3; i < pacchetto.length; i++) {
            if (pacchetto[i]==0)
                break;
            contatoreNome++;
        }

        for (int i = 4+contatoreNome; i <pacchetto.length ; i++) {
            if (pacchetto[i]==0)
                break;
            contatoreMex++;
        }
        byte[] nome=new byte[contatoreNome],mex= new byte[contatoreMex];
        for (int i = 0; i < contatoreNome ; i++)
            nome[i] = pacchetto[i+3];
        for (int i = 0; i < contatoreMex; i++)
            mex[i] = pacchetto[4+contatoreNome+i];

        return (new String(nome, StandardCharsets.UTF_8) + ": "+ new String(mex, StandardCharsets.UTF_8));
    }

    private void appendToPane(JTextPane tp, String msg, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }

    //gestore degli eventi (JButton)
    public void gestore(ActionEvent e){
        if (e.getSource()==invio){
            messaggio = inserimento.getText();
            inserimento.setText("");
        }
        
    }
}
