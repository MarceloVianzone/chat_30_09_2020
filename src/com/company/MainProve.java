package com.company;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MainProve {
    public static void main(String[] args) {
        JOptionPane.showMessageDialog(null,"Errore! Non esiste nessuno chiamato: "+"destinatario","Destinatario inesistente", JOptionPane.ERROR_MESSAGE);
        String a = "/msg marceloV ciao coglione!";
        System.out.println(a);

        System.out.println(a.substring(0,4));
        String destinatario="",messaggio;
        int i;
        for (i = 5; a.charAt(i)!=' '; i++)
            destinatario+=a.charAt(i);
        System.out.println(destinatario);
        i++;
        messaggio=a.substring(i);
        System.out.println(messaggio);

        boolean b=false;
        System.out.println(b);
    }
}
