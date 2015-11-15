/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author b04732 & a96097
 */

public class Nucleo implements Runnable {
    //Contexto
	int PC;
    int[] registros;
    int numInstruccion; 
    String IR;
	Bloque[] cacheInstrucciones;	
        CacheDatos cacheDatos;
	int BLOQUES;
	int apuntadorCache;
	int apuntadorBloques;
	String nombreNucleo;
    private CyclicBarrier barrier;
    int pruebaHilo;
    Memoria memoria;
    MemoriaDatos memoriaDatos;
    int bloqueInicio;
    int bloqueFin;
    int posFin;
    boolean seguir;
    boolean terminado;
    static int quantum;
    static int ciclosReloj;
    static Semaphore busInstrucciones;
    static Semaphore busDatos;
    static Semaphore bloqueoCacheDatos;
    int direccion;
    int pcFin;
    boolean falloCache;
    int contCiclosFallo;
    boolean esperandoBus;
    boolean apagado;
    boolean desactivado;
    int colaEspera[][];
    int hiloActual;
    //booleanos para comunicacion con el controlador sobre coherencia de datos
    boolean revisarOtraCacheLW;
    boolean revisarOtraCacheSW;
    boolean bloqueoCachePropia;
    
    
        
	public Nucleo(String nombre, CyclicBarrier barrier,Memoria memoria,MemoriaDatos memoriaDatos,int bloqueInicio,int pcFin,
		int quantum,int ciclosReloj, Semaphore busInstrucciones, Semaphore busDatos,Semaphore bloqueoCacheDatos,int[][] colaEspera, int hiloActual) {
            this.nombreNucleo = nombre;
            this.barrier = barrier;
            this.registros = new int[32];
            this.BLOQUES = 8;
            this.apuntadorCache = 0;
            this.apuntadorBloques = 0;
            this.cacheInstrucciones = new Bloque[BLOQUES];
            this.cacheDatos = new CacheDatos();
            this.pruebaHilo = 1;

            this.memoria = memoria;
            this.memoriaDatos = memoriaDatos;
            this.inicializarCaches();
            this.bloqueInicio = bloqueInicio;
            this.pcFin = pcFin;
            this.seguir = true;
            this.terminado = false;
            Nucleo.quantum = quantum;
            Nucleo.ciclosReloj = ciclosReloj;
            Nucleo.busInstrucciones = busInstrucciones;
            Nucleo.busDatos = busDatos;
            Nucleo.bloqueoCacheDatos = bloqueoCacheDatos;
            this.direccion = -999;
            this.falloCache = false;
            this.contCiclosFallo = 1;
            this.esperandoBus = false;
            this.apagado = false;
            this.desactivado = false;
            this.colaEspera = colaEspera;
            this.hiloActual = hiloActual;
            
            revisarOtraCacheLW = false;
            revisarOtraCacheSW = false;
            bloqueoCachePropia = false;
	}
	
	private void inicializarCaches() {
		for(int i=0; i<BLOQUES; i++) {
			cacheInstrucciones[i] = new Bloque(-1); //-1 para distinguir estos bloques "vacios"
			cacheInstrucciones[i].inicializarCache();
		}
	}
	
	public void cargarBloque(Bloque b) {
		cacheInstrucciones[b.ID%this.BLOQUES] = b;
                
	}
        void setPC(int miPC){
            this.PC = miPC;
        }
        int getPC(){
            return this.PC;
        } 
        //El nucleo termino el hilo anterior ahora es el length del nuevo hilo
        void setPCFin(int mipcFin){
            this.pcFin = mipcFin;
        }
	
	public String imprimirCache(){
            String cadena;
            cadena = "";
            for(int bloque = 0; bloque < 8; bloque++ ){
               // System.out.print("BLoque "+bloque +" ");
                 cadena +=this.cacheInstrucciones[bloque].imprimir();

            }
        return cadena;
        }

	public String imprimirRegistros(){
        String cadena;
        cadena = "";
        for(int registro = 0; registro < 32; registro++ ){
            System.out.print("Registro "+registro +" "+this.registros[registro]);
             cadena +="Registro "+Integer.toString(registro) +" "+Integer.toString(this.registros[registro])+"\n";

        }
        cadena += "\n";
        return cadena;
    }
	
	public boolean contenerBloque() {
		//this.bloqueInicio + (this.colaEspera[1][this.hiloActual](Posicion de inicio del hilo(0,1,2 o 3)) + this.PC)/4 da la posicion del bloque en memoria
		if(cacheInstrucciones[(this.bloqueInicio%this.BLOQUES + ((this.colaEspera[1][this.hiloActual] + this.PC)/4)%this.BLOQUES)%this.BLOQUES].getID() == (this.bloqueInicio +
				(this.colaEspera[1][this.hiloActual] + this.PC)/4)) { // PC/4 nos da el numero de bloque
				

				
			return true;
		}
		return false;
	}
	
	public void setPrueba(int num){
        this.pruebaHilo = num;
        
        }
        public void setContexto(int nuevoPC,int[] nuevoRegistros){
            this.PC = nuevoPC;
            this.registros = nuevoRegistros;
        }

    @Override
    public void run() {
    	while(!apagado) {
	        while(this.seguir) {
	        	if(nombreNucleo.equals("Nucleo 2") && (registros[4]==964)) {
	        		System.out.println("a");
	        	}
	        	System.out.println("En ejecucion el: "+this.nombreNucleo);
	            try {
	            System.out.print("pc"+this.PC+"bloque inicio "+this.bloqueInicio);
	            //fallo de cache nucleo 1 (falta el bus)
	
	            if(this.contenerBloque() && !this.falloCache) {
	                this.ejecutarInstruccion();
	                
	            } else if(this.falloCache) {
	            	contCiclosFallo--;
	            	if(contCiclosFallo<=1) {
	            		falloCache = false;
	            		esperandoBus = false;
	            		contCiclosFallo = 1;
	            		busInstrucciones.release();
	            		this.ejecutarInstruccion();
	            	}
	            } else {
	            	esperandoBus = true;
	                if(busInstrucciones.tryAcquire()){
                            try {
                                this.barrier.await();
                            } catch (BrokenBarrierException ex) {
                                Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
                            } 
                            
                            
	                	Bloque b1 = this.memoria.getBloque((this.bloqueInicio + (this.colaEspera[1][this.hiloActual] + this.PC)/4));                	
	                	cargarBloque(b1);
	                	System.out.print("cargando bloque: "+this.bloqueInicio+" PC:"+this.PC/4+". ");
	                	System.out.print("hay fallo");
	                	falloCache = true;
	                    contCiclosFallo--;
	                    System.out.print("cargando bloque: "+this.bloqueInicio+" PC: "+this.PC/4+".");
	                    System.out.print("hay fallo");   
	                }
	            }
	            
	            try {
	            	this.barrier.await();
	            } catch(BrokenBarrierException ex) {}
	            
	            } catch (InterruptedException ex) {
	                Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
	            }
	            
	            
	        }
        
            try {
                this.barrier.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BrokenBarrierException ex) {
                Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
            }
    	}
    }
	
	public void ejecutarInstruccion() {
        //Bloque donde se encuentra la instruccion apuntada por el PC actual, previamente cargada
		
		Bloque b = cacheInstrucciones[(this.bloqueInicio%this.BLOQUES + ((this.colaEspera[1][this.hiloActual]+this.PC)/4)%this.BLOQUES)%this.BLOQUES];
        //Intruccion del bloque (0|1|2|3) 
                
		//Pido instruccion al cache la guardo en el IR  
		IR = b.getInstruccion((this.colaEspera[1][this.hiloActual]+PC)%4);
             
		String[] codificacion = IR.split(" ");
		System.out.println(codificacion[0]);
		
		this.PC++;
                
        //Verifica cual operacion es
		switch(codificacion[0]) {
			case "8": //DADDI
				if(registros[25]==-1) {
					@SuppressWarnings("unused")
					int d = 1;
				}
				registros[Integer.parseInt(codificacion[2])] =
						registros[Integer.parseInt(codificacion[1])] + Integer.parseInt(codificacion[3]);
			break;
			
			case "32": //DADD
				registros[Integer.parseInt(codificacion[3])] =
						registros[Integer.parseInt(codificacion[1])] + registros[Integer.parseInt(codificacion[2])];
			break;
			
			case "34": //DSUB
				registros[Integer.parseInt(codificacion[3])] =
						registros[Integer.parseInt(codificacion[1])] - registros[Integer.parseInt(codificacion[2])];
			break;
			
			case "12": //DMUL
				registros[Integer.parseInt(codificacion[3])] =
						registros[Integer.parseInt(codificacion[1])] * registros[Integer.parseInt(codificacion[2])];
			break;
			
			case "14": //DDIV
				registros[Integer.parseInt(codificacion[3])] =
						registros[Integer.parseInt(codificacion[1])] / registros[Integer.parseInt(codificacion[2])];
			break;
			
			case "4": //BEQZ
				if(registros[Integer.parseInt(codificacion[1])] == 0) {
					this.PC += Integer.parseInt(codificacion[3]); //multiplicado*4???????????????
				}
			break;
			
			case "5": //BNEZ
				if(registros[Integer.parseInt(codificacion[1])] != 0) {
					this.PC += Integer.parseInt(codificacion[3]); //multiplicado*4???????????????
				}
			break;
			
			case "3": //JAL
				registros[31] = this.PC;
				this.PC += Integer.parseInt(codificacion[3])/4;
			break;
			
			case "2": //JR
				this.PC = registros[Integer.parseInt(codificacion[1])];
			break;
			
			case "63": //JR
				this.terminado = true;
				this.seguir = false;
			break;
                            
                        case "35": //LW
                            int direccion = Integer.parseInt(codificacion[3])+registros[Integer.parseInt(codificacion[1])];
                            //registros[Integer.parseInt(codificacion[2])] = this.loadWord(direccion);
		}

	}
        public  int loadWord(int direccion){
            boolean terminado;
            terminado = false;
            
            while(!terminado&&!bloqueoCachePropia){
         
                if(bloqueoCacheDatos.tryAcquire()){
                    if((this.cacheDatos.contenerBloque(direccion)) ){
                        return this.cacheDatos.getDato(direccion);   
                        
                    //fallo    
                    }else{
                        if(busDatos.tryAcquire()){
                            //buscar el 
                            this.direccion = direccion;
                            this.revisarOtraCacheLW = true;
                            this.revisarOtraCacheSW = true;
                            try {
                                this.barrier.await();
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (BrokenBarrierException ex) {
                                Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            terminado = true;
                            return this.cacheDatos.getDato(direccion);
                            
                        
                        }else{
                            bloqueoCacheDatos.release();
                            
                        }
                    
                    }

                }  
            
            }
            direccion = -999;
            return this.cacheDatos.getDato(direccion);
           
         
        }
	
}

