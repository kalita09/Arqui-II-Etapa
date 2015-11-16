/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto;

import java.util.concurrent.CyclicBarrier;
import javax.swing.JOptionPane;

/**
 *
 * @author b04732 & a96097
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Ventana ventana = new Ventana ();
        ventana.setVisible(true);
        int m = Integer.parseInt(JOptionPane.showInputDialog("Escribe el valor de m"));
        int b = Integer.parseInt(JOptionPane.showInputDialog("Escribe el valor de b"));
        int quantum = Integer.parseInt(JOptionPane.showInputDialog("Escribe el valor del quantum"));
        int dialogResult = JOptionPane.showConfirmDialog (null, "¿Desea activar el modo lento?");
        Controlador controlador;
        if(dialogResult == JOptionPane.YES_OPTION){
            controlador = new Controlador(6, quantum,m,b,true,ventana);
        }else{
            controlador = new Controlador(6, quantum,m,b,false,ventana);
        }
        
        controlador.iniciar();
       

        

        

    }
}
