package com.example;

import com.example.paquete.Paquete;
import com.example.paquete.PuertosSN;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class EnvioRespuestas extends Thread {
    int puertos_nodos[]={11000, 11001, 11002, 11003, 11004};
    PuertosSN puertosSN;

    public void run(){
        while(true){
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {

            }
            try {
                Paquete inicio=new Paquete(" ", puertosSN.puertoServidor, 'O','x'," "," ");
                inicio.setCodigoOperacion(' ');
                inicio.setIDdireccion('N');
                Socket enviaReceptor=new Socket("127.0.0.1",puertosSN.nodo);
                ObjectOutputStream paqueteReenvio=new ObjectOutputStream(enviaReceptor.getOutputStream());
                paqueteReenvio.writeObject(inicio);
                paqueteReenvio.close();
                enviaReceptor.close();
            } catch(IOException e) {
                boolean ban=false;
                for(int nodoLista:puertos_nodos){
                    if(nodoLista!=puertosSN.nodo){
                        ban=conectarseANodoNuevo(nodoLista);
                    }
                    if(ban){
                        puertosSN.nodo=nodoLista;
                        break;
                    }
                }
            }


        }
    }
    boolean conectarseANodoNuevo(int nuevoNodo){
        try {
            Paquete datos=new Paquete(" ", puertosSN.puertoServidor, 'O','x'," "," ");
            Socket enviaReceptor=new Socket("127.0.0.1",nuevoNodo);
            ObjectOutputStream paqueteReenvio=new ObjectOutputStream(enviaReceptor.getOutputStream());
            datos.setCodigoOperacion('x');
            datos.setIDdireccion('N');
            paqueteReenvio.writeObject(datos);
            paqueteReenvio.close();
            enviaReceptor.close();
            return true;
        } catch(IOException e) {
            //System.out.println(e);
            // System.out.println("servidor apagado: "+puerto);
            return false;
        }
    }
}
