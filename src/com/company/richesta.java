package com.company;

import java.io.IOException;
import java.net.*;

public class richesta {
    public static void main(String[] args) {
        try {
            String ip = "230.0.0.0";
                int port = 4321;
                byte[] buffer = new byte[1024];
                MulticastSocket socket = new MulticastSocket(port);
                InetAddress mcastaddr = InetAddress.getByName(ip);
                InetSocketAddress group = new InetSocketAddress(mcastaddr, port);

                socket.joinGroup(group, NetworkInterface.getByName("ethernet"));
                System.out.println(socket.getInterface());

                while (true) {
                    System.out.println("Waiting for multicast message...");
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    System.out.println("[Multicast UDP message received] >> " + msg);
                    if ("OK".equals(msg)) {
                        System.out.println("No more message. Exiting : " + msg);
                        break;
                    }
                }

                socket.leaveGroup(group, NetworkInterface.getByName("ethernet"));

                socket.close();

            } catch (Exception ex) {
                System.out.println(ex);
                ex.printStackTrace();
            }

    }
}
