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
public class CacheDatos {
    int BLOQUES;
    BloqueDatos[] cacheDatos;	
    public CacheDatos(){
        this.cacheDatos = new BloqueDatos[BLOQUES];
        
        for(int i=0; i<BLOQUES; i++) {
            cacheDatos[i] = new BloqueDatos(-1); //-1 para distinguir estos bloques "vacios"
            cacheDatos[i].inicializarCacheDatos();
            
	}
        
    }
    /*
    public boolean contenerBloque(int direccion) {
        
            //this.bloqueInicio + (this.colaEspera[1][this.hiloActual](Posicion de inicio del hilo(0,1,2 o 3)) + this.PC)/4 da la posicion del bloque en memoria
            if(cacheInstrucciones[(this.bloqueInicio%this.BLOQUES + ((this.colaEspera[1][this.hiloActual] + this.PC)/4)%this.BLOQUES)%this.BLOQUES].getID() == (this.bloqueInicio +
                            (this.colaEspera[1][this.hiloActual] + this.PC)/4)) { // PC/4 nos da el numero de bloque



                    return true;
            }
            return false;
    }
    */
    
}
