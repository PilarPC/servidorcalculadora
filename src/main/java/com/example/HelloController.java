package com.example;

import com.example.paquete.Paquete;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HelloController implements Runnable{
    @FXML
    private VBox historialVB;
    @FXML
    private Label idServidor;

    //VARIABLES GLLOBALES
    String resultado;
    public String mns;
    int puertoServidor = 13000;
    int puertoMiddleware = 11000; //a partir de aqui comienzan los puertos que escuchan los servidores

    public void initialize(){
        Thread hilo1 = new Thread(this);
        hilo1.start();
    }
    @Override
public void run() {
        while (true){
            try {
                ServerSocket servidor = new ServerSocket(puertoServidor);
                Platform.runLater(() -> {
                    idServidor.setText("Servidor: "+ puertoServidor);
                });
                String puertoString= puertoServidor+"";
                String huella = generarHuella(puertoString);
                System.out.println("Huella generada por mi puerto actual "+puertoServidor +" "+huella);

                while (true){
                    //recibo
                    Socket misocket = servidor.accept();
                    ObjectInputStream flujoEntrada = new ObjectInputStream(misocket.getInputStream());
                    Paquete paqueteRecibido = (Paquete) flujoEntrada.readObject();

                    System.out.println(paqueteRecibido.getMensaje()+" "+ paqueteRecibido.getPuertoEmisor()+ " "+ paqueteRecibido.getIDdireccion());

                    if (paqueteRecibido.getCodigoOperacion() == 'm'){
                        Platform.runLater(() -> {
                            historialVB.getChildren().add(new Label("Esta información probiene de un servidor"));
                        });
                    }else {
                        //Calcular
                        Platform.runLater(() -> {
                            historialVB.getChildren().add(new Label("Operación: "+paqueteRecibido.getMensaje()));
                        });

                        calcular(paqueteRecibido);
                        paqueteRecibido.setPuertoEmisor(puertoServidor);
                        paqueteRecibido.setMensaje(resultado);
                        paqueteRecibido.setIDdireccion('S');
                        paqueteRecibido.setCodigoOperacion('m');
                        paqueteRecibido.setHuellaServidor(huella);
                        String acuse = generarHuella(paqueteRecibido.getMensaje());
                        //System.out.println("Acuse "+acuse);
                        paqueteRecibido.setAcuse(acuse);


                        //ENVIAR RESULTADO
                        Socket SocketMiddleware = new Socket("127.0.0.1", puertoMiddleware);
                        ObjectOutputStream salida =  new ObjectOutputStream(SocketMiddleware.getOutputStream());
                        salida.writeObject(paqueteRecibido);
                        System.out.println(paqueteRecibido.getMensaje()+" "+ paqueteRecibido.getPuertoEmisor()+ " "+ paqueteRecibido.getIDdireccion()+" Envio al puerto "+puertoMiddleware+" ACUSE "+paqueteRecibido.getAcuse());
                        System.out.println("Enviando"+resultado);
                        SocketMiddleware.close();
                        Platform.runLater(() -> {
                            historialVB.getChildren().add(new Label("resultado: "+paqueteRecibido.getMensaje()+"\n"));
                        });

                    }

                    misocket.close();
                }

            } catch (IOException | ClassNotFoundException | NoSuchFieldException | InvocationTargetException |
                     NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                System.out.println(e);
                puertoMiddleware++;
                puertoServidor++;
            }
        }
}

//-----------------------------------GENERAR HUELLA--------------------
    public String generarHuella(String puerto){
        String tiempo = DateTimeFormatter.ofPattern("dd-MM-yyyy | HH:mm:ss").format(LocalDateTime.now());
        String huella = puerto + tiempo;
        huella = sha1Mensaje(huella);
        return huella;
    }

    public String sha1Mensaje(String mensaje){
        String devuelvesha1="";
        String sha=mensaje;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(sha.getBytes("utf8"));
            devuelvesha1 = String.format("%040x", new BigInteger(1, digest.digest()));
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println();
        return devuelvesha1;
    }

    //--------------------------------------Microservicios--------
    static float cargaDeMicroServicios(String nombreFuncion, String nombreDeClase, float valor1, float valor2,String nombreJar) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException{

        URL[] urls = null;
        File dir = new File("D:"+ File.separator+"IJ"+ File.separator+"proyectos"+ File.separator+"Respaldo Claculadora" + File.separator+nombreJar+".jar"+ File.separator);
        URL url = dir.toURI().toURL();
        urls = new URL[] { url };
        ClassLoader cl = new URLClassLoader(urls);
        Class cls = cl.loadClass(nombreDeClase);
        Method[] mthd = cls.getMethods();
        Class[] parameterTypes=null;
        /*for(Method m:mthd)
        {
            parameterTypes  = m.getParameterTypes();
            break;
        }*/
        Object instancia= cls.getDeclaredConstructor().newInstance();
        Method s=cls.getMethod(nombreFuncion,float.class,float.class);

        float resultado=(float)s.invoke(instancia,valor1,valor2);
        System.out.println(resultado);

        return resultado;
    }
//---------------------------------CALCULAR --------------


    public void calcular(Paquete cadena) throws MalformedURLException, NoSuchFieldException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        float suma=0f,resta=0f,div=0f,mul=0f;
        float n1=0f,n2=0f;
        char operacion='a';
        int cont=0;
        boolean ban=true;
        while(ban){
            if(cadena.getMensaje().charAt(cont)=='/'|cadena.getMensaje().charAt(cont)=='-'|cadena.getMensaje().charAt(cont)=='+'|cadena.getMensaje().charAt(cont)=='x'){
                operacion=cadena.getMensaje().charAt(cont);
                n1=Float.parseFloat(cadena.getMensaje().substring(0,cont));
                ban=false;

            }else{
                //System.out.println(cadena.substring(cont,cont+1));
                cont=cont+1;
                if(cont==cadena.getMensaje().length()){
                    ban=false;
                }
            }
        }
        n2=Float.parseFloat(cadena.getMensaje().substring(cont+1,cadena.getMensaje().length()));
        switch(operacion) {
            case '+':
                suma = cargaDeMicroServicios("sumar","Suma", n1, n2,"suma");
                resultado = suma+"";
                break;
            case '-':
                suma = cargaDeMicroServicios("restar","Resta", n1, n2,"resta");
                resultado = suma+"";
                break;
            case 'x':
                suma = cargaDeMicroServicios("multiplicar","Multi", n1, n2,"multi");
                resultado = suma+"";
                break;
            case '/':
                suma = cargaDeMicroServicios("dividir","Div", n1, n2,"div");
                resultado = suma+"";
                break;
            default:
                System.out.println("Habitación 5");
                break;
        }
    }
}