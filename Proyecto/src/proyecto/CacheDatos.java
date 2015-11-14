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
    //tamano bloque en memoria principal de datos
    int TAMANOBLOQUE =16;
    int BLOQUES = 8;
    int INCREMENTO=4;
    int PALABRASBLOQUE=4;
    BloqueDatos[] cacheDatos;	
    public CacheDatos(){
        this.cacheDatos = new BloqueDatos[BLOQUES];
        
        for(int i=0; i<BLOQUES; i++) {
            cacheDatos[i] = new BloqueDatos(-1); //-1 para distinguir estos bloques "vacios"
            cacheDatos[i].inicializarCacheDatos();
            
	}
        
    }
    
    public boolean contenerBloque(int direccion) {
        
        
        if(this.cacheDatos[((direccion-640)/this.TAMANOBLOQUE)%this.BLOQUES].ID==((direccion-640)/this.TAMANOBLOQUE)){
            
            //acierto, hit
            return true;
        }else{
            return false;
        }
     
    }
    public int getDato(int direccion) {
        
        BloqueDatos bloque = this.cacheDatos[((direccion-640)/this.TAMANOBLOQUE)%this.BLOQUES];
        
        return bloque.datos[((direccion-640)/INCREMENTO)%PALABRASBLOQUE];
        
    }
    
    public void setBloque(BloqueDatos bloque) {
     
        
        this.cacheDatos[bloque.ID%this.BLOQUES]= bloque;
   
    }
    public BloqueDatos getBloque(int direccion) {

        return this.cacheDatos[((direccion-640)/this.TAMANOBLOQUE)%this.BLOQUES];

    }
    
}
