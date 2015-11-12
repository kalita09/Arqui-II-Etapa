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
    int BLOQUES = 8;
    BloqueDatos[] cacheDatos;	
    public CacheDatos(){
        this.cacheDatos = new BloqueDatos[BLOQUES];
        
        for(int i=0; i<BLOQUES; i++) {
            cacheDatos[i] = new BloqueDatos(-1); //-1 para distinguir estos bloques "vacios"
            cacheDatos[i].inicializarCacheDatos();
            
	}
        
    }
    
    public boolean contenerBloque(int id) {
        
        if(this.cacheDatos[id%this.BLOQUES].ID==id){
            //acierto, hit
            return true;
        }else{
            return false;
        }
     
    }
    public void setBloque(BloqueDatos bloque) {
        
        this.cacheDatos[bloque.ID%this.BLOQUES]= bloque;
   
    }
    
    
}
