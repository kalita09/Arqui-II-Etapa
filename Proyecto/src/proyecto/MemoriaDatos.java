/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto;

/**
 *
 * @author b04732
 */
public class MemoriaDatos {
    BloqueDatos[] memoria;
    int numeroHilos;
    int BLOQUES = 88;
    int bloque = 0;
    int posInicio;
    int inst = 0;
    int length = 0;
        
	public MemoriaDatos() {
		this.memoria = new BloqueDatos[BLOQUES];
           //this.inicializar();
                
                
	}

    
}
