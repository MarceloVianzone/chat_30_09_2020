package com.company;

import java.nio.charset.StandardCharsets;

public class InvioPacchetti {

    //controllo del nome
    public static boolean nome(String nome){
        if (nome.length() < 6 || nome.length() > 15)
            return false;
        for (int i = 0; i < nome.length(); i++){
            char carattere = nome.charAt(i);
            int numCar = (int) carattere;
            if (! ( (numCar >= 65 && numCar <= 90)  || /*se è una lettera maiuscola */
                    (numCar >= 97 && numCar <= 122) || /*se è una lettera minuscola */
                    (numCar >= 48 && numCar <= 57)  || /*se è un numero */
                    (numCar == 95  || numCar == 63 || numCar == 45 )) ) /*se è ? o _ o - */
                return false;
        }
        return true;
    }

    //intero in 2 byte
    public static byte[] bytes(int n){
        if(n<256){
            byte a[] = new byte[2];
            a[0]=0;
            a[1]=(byte)n;
            return a;
        }
        String binario = Integer.toBinaryString(n);
        byte ritorno[] = new byte[2];
        //menoS contiene in bit il byte meno significativo
        String menoS=binario.substring(binario.length()-8);
        //piuS contiene in bit il byte più significativo
        String piuS="";
        if (binario.length()-8 < 8){
            for (int i = 0; i < 8-(binario.length()-8); i++)
                piuS+=0;
            piuS+=binario.substring(0,binario.length()-8);
        }else
            piuS+=binario.substring(binario.length()-16,binario.length()-8);
        //valore conterrà il valore intero tradotto dal binario
        int valore=0;
        int contatore=128;
        for (int i = 0; i < 8; i++) {
            int carattere = Integer.parseInt(""+piuS.charAt(i));
            valore+=carattere*contatore;
            contatore/=2;
        }
        ritorno[0]=(byte)valore;
        valore=0;
        contatore=128;
        for (int i = 0; i < 8; i++) {
            int carattere = Integer.parseInt(""+menoS.charAt(i));
            valore+=carattere*contatore;
            contatore/=2;
        }
        ritorno[1]=(byte)valore;


        return ritorno;
    }

    //creazione pacchetto di login
    public static byte[] login(String nome){
        byte name[] = nome.getBytes();
        byte login[] = new byte[name.length+3];
        login[0] = (int)11;
        byte lungezza[] = bytes(nome.length());
        login[1] = lungezza[0];
        login[2] = lungezza[1];
        for (int i = 3; i <login.length ; i++) {
            login[i]=name[i-3];
        }

        return login;
    }

    //creazione pacchetti semplici
    private static byte[] pacchettini(int codice){
        byte pacchetto[] = {(byte)codice , (byte)0 , (byte)0};
        return pacchetto;
    }

    //creazione pacchetto di logout
    public static byte[] logout(){return pacchettini(12);}

    //creazione pacchetto di info
    public static byte[] info(){return pacchettini(40);}

    //creazione pacchetto di richesta della lista utenti
    public static byte[] lista(){return pacchettini(42);}

    //creazione pacchetto di messaggio pubblico
    public static byte[] PublicMessage(String messaggio){
        byte mex[] = messaggio.getBytes();
        byte pacchetto[] = new byte[mex.length+3];
        pacchetto[0] = (int)20;
        byte lungezza[] = bytes(mex.length);
        pacchetto[1] = lungezza[0];
        pacchetto[2] = lungezza[1];
        for (int i = 3; i <pacchetto.length ; i++) {
            pacchetto[i]=mex[i-3];
        }

        return pacchetto;
    }

    //creazione pacchetto di messaggio privato
    public static byte[] PrivateMessage(String destinatario,String messaggio){
        byte mex[] = messaggio.getBytes();
        byte dest[] = destinatario.getBytes();
        byte pacchetto[] = new byte[dest.length+messaggio.length()+4];
        pacchetto[0] = (byte)22;
        byte lunghezza[] = bytes(messaggio.length()+1+destinatario.length());
        pacchetto[1] = lunghezza[0];
        pacchetto[2] = lunghezza[1];
        int i;
        for (i = 0; i <dest.length ; i++)
            pacchetto[i+3]=dest[i];
        pacchetto[i+3]=0;
        for (int j = 0; j < mex.length ; j++)
            pacchetto[j+i+4]=mex[j];
        return pacchetto;
    }

}
