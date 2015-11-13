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
    int TAMANOBLOQUE =16;
    int INCREMENTO=4;
    int PALABRASBLOQUE=4;
        
	public MemoriaDatos() {
            this.memoria = new BloqueDatos[BLOQUES];
            this.inicializar();
                
	}
        private void inicializar() { 
            
	    for(int i=0; i<BLOQUES; i++) {
	        memoria[i] = new BloqueDatos(i);
	        memoria[i].inicializarMemoriaDatos();
            }
	}
        public BloqueDatos getBloque(int direccion) {
            //numero de bloque (0-87) de la direccion 
            return this.memoria[(direccion-640)/this.TAMANOBLOQUE];
            
        }
        public BloqueDatos getpalabra(int direccion) {
            //numero de palabra en el vector segun la direccion 0/1/2/3
            return this.memoria[((direccion-640)/INCREMENTO)%PALABRASBLOQUE];
            
        }
        

    
}
