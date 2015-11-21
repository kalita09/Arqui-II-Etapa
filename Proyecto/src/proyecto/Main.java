/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto;

import java.util.concurrent.CyclicBarrier;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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
            controlador = new Controlador(7, quantum,m,b,true,ventana);
        }else{
            controlador = new Controlador(7, quantum,m,b,false,ventana);
        }
        
        JButton open = new JButton();
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new java.io.File("."));
        fc.setDialogTitle("Seleccione la carpeta que contenga los hilos");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(fc.showOpenDialog(open)==JFileChooser.APPROVE_OPTION){
        }
        
        JOptionPane.showMessageDialog(null, fc.getSelectedFile().getAbsolutePath());
       
        controlador.iniciar(fc.getSelectedFile().getAbsolutePath()+"\\");
       

        

        

    }
}
