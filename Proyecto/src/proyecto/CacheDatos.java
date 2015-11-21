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
        //Busca el bloque de datos correspondiente en la cache
        BloqueDatos b = this.cacheDatos[((direccion-640)/this.TAMANOBLOQUE)%this.BLOQUES];
        if((b.ID == ((direccion-640)/this.TAMANOBLOQUE)) && (b.estado != 'I')){
            
            //acierto, hit
            return true;
        }else{
            return false;
        }
     
    }
    
    public boolean reeplazarBloqueModificado(int direccion) {
    	//Busca el bloque de datos correspondiente en la cache
        BloqueDatos b = this.cacheDatos[((direccion-640)/this.TAMANOBLOQUE)%this.BLOQUES];
        if(b.estado == 'M') {
        	return true;
        } else {
        	return false;
        }
    }
    
    public int getDato(int direccion) {
        
        BloqueDatos bloque = this.cacheDatos[((direccion-640)/this.TAMANOBLOQUE)%this.BLOQUES];
        
        return bloque.datos[((direccion-640)/INCREMENTO)%PALABRASBLOQUE];
        
    }
    
	public void setDato(int direccion, int dato) {
	        
        BloqueDatos bloque = this.cacheDatos[((direccion-640)/this.TAMANOBLOQUE)%this.BLOQUES];
	        
        bloque.datos[((direccion-640)/INCREMENTO)%PALABRASBLOQUE] = dato;
	        
	    }
    
    public void setBloque(BloqueDatos bloque) {
        this.cacheDatos[bloque.ID%this.BLOQUES].estado = bloque.estado;
    	this.cacheDatos[bloque.ID%this.BLOQUES].ID = bloque.ID;
        this.cacheDatos[bloque.ID%this.BLOQUES].datos= bloque.datos;
   
    }
    public BloqueDatos getBloque(int direccion) {

        return this.cacheDatos[((direccion-640)/this.TAMANOBLOQUE)%this.BLOQUES];

    }
    
    public String imprimir(){
        String cadena;
        cadena = "";
        for(int i=0;i<this.BLOQUES;i++){
            cadena+=cacheDatos[i].imprimir();

        }
        
        return cadena;
    }
    
}
