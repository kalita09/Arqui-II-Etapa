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
    int RL;
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
    boolean terminado; //indica cuando termina un hilo
    static int quantum;
    static int ciclosReloj;
    static Semaphore busInstrucciones;
    static Semaphore busDatos;
    Semaphore bloqueoCacheDatos;
    int direccion;
    boolean falloCache;
    int contCiclosFallo;
    boolean esperandoBus;
    boolean esperandoCache;
    boolean apagado; //se activa cuando ambos nucleos terminaron de ejecutar todos los hilos
    boolean desactivado; //indica que ya no tiene nada mas que ejecutar
    int colaEspera[][];
    int hiloActual;
    //booleanos para comunicacion con el controlador sobre coherencia de datos
    boolean revisarOtraCacheLW;
    boolean revisarOtraCacheSW;
    boolean banderaLL;
    int bloqueLL;
    //boolean bloqueoCachePropia;
    boolean datoCargado;
    boolean leerBloqueOtraCache;
    boolean LLenEsperaDeSC;
    int candado;
    
        
	public Nucleo(String nombre, CyclicBarrier barrier,Memoria memoria,MemoriaDatos memoriaDatos,int bloqueInicio,int pcFin,
		int quantum,int ciclosReloj, Semaphore busInstrucciones, Semaphore busDatos,int[][] colaEspera, int hiloActual) {
            this.nombreNucleo = nombre;
            this.barrier = barrier;
            this.registros = new int[33];
            this.RL = 0;
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
            this.seguir = true;
            this.terminado = false;
            Nucleo.quantum = quantum;
            Nucleo.ciclosReloj = ciclosReloj;
            Nucleo.busInstrucciones = busInstrucciones;
            Nucleo.busDatos = busDatos;
            bloqueoCacheDatos = new Semaphore(1);
            this.direccion = -999;
            this.falloCache = false;
            this.contCiclosFallo = 1;
            this.esperandoBus = false;
            this.esperandoCache = false;
            this.apagado = false;
            this.desactivado = false;
            this.colaEspera = colaEspera;
            this.hiloActual = hiloActual;
            this.datoCargado = false;
            this.leerBloqueOtraCache = false;
            this.LLenEsperaDeSC = false;
            this.bloqueLL = 0;
            this.candado = 0;
			
            revisarOtraCacheLW = false;
            revisarOtraCacheSW = false;
            //bloqueoCachePropia = false;
            this.banderaLL = false;
            this.bloqueLL = -1;
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
        for(int registro = 0; registro < 33; registro++ ){
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
                            //mostrar al usuario el pc correcto
				registros[31] = this.PC*4;
				this.PC += Integer.parseInt(codificacion[3])/4;
			break;
			
			case "2": //JR
                                if(Integer.parseInt(codificacion[1])==31){
                                  this.PC = registros[Integer.parseInt(codificacion[1])];  
                                  this.PC = this.PC/4;
                                }else{
                                  this.PC = registros[Integer.parseInt(codificacion[1])]; 
                                }
				
			break;
			
			case "63": //JR
				this.terminado = true;
				this.seguir = false;
			break;
                            
			case "35": //LW
				this.loadWord(codificacion);
			break;
				
			case "43": //SW
				this.storeWord(codificacion);
			break;
			
			case "50": //LL
				this.loadWord(codificacion);
                //guardo en RL el valor de la direccion
               int direccionLL = Integer.parseInt(codificacion[3])+registros[Integer.parseInt(codificacion[1])];
               registros[32] =  direccionLL;
               //numero de bloque
               this.bloqueLL = (direccion-640)/16;
               //levantar la bandera esperando SC
               this.banderaLL = true;
			break;
			
			case "51": //SC
				if(registros[32] == Integer.parseInt(codificacion[3])+ registros[Integer.parseInt(codificacion[1])]    ){
                    this.storeWord(codificacion);
                    this.banderaLL = false;
                    }else{
                        registros[Integer.parseInt(codificacion[2])] = 0;
                        this.banderaLL = false;
                    }
			break;
                   
		}

	}
	public  void storeWord(String[] codificacion){
    	boolean terminoStore = false;
		direccion = Integer.parseInt(codificacion[3])+registros[Integer.parseInt(codificacion[1])];
		
		//Ciclo hasta que pueda bloquear la cache propia y el bus
		while(!terminoStore) {
			this.esperandoCache = true;
			if(bloqueoCacheDatos.tryAcquire()) {
				
				//Hit y bloque modificado
				if((this.cacheDatos.contenerBloque(direccion)) && (this.cacheDatos.getBloque(direccion).estado == 'M')){
					cacheDatos.setDato(direccion, registros[Integer.parseInt(codificacion[2])]);
					bloqueoCacheDatos.release();
					terminoStore = true;
					this.esperandoCache = false;
						
				//Tengo el bloque compartido o hay fallo
				} else {
					if(busDatos.tryAcquire()) {
	        			try {
		                    this.barrier.await();
		                } catch (InterruptedException ex) {
		                    Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
		                } catch (BrokenBarrierException ex) {
		                    Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
		                }
	        			
	        			//Solicitar la otra cache
	        			this.revisarOtraCacheSW = true;
	        			
	        			this.leerBloqueOtraCache = false;
	        			//Espera hasta que se haya leido el bloque en la otra cache para continuar
	        			while(!leerBloqueOtraCache) {
	        				try {
			                    this.barrier.await();
			                } catch (InterruptedException ex) {
			                    Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
			                } catch (BrokenBarrierException ex) {
			                    Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
			                }
	        			}
	        			
	        			busDatos.release();
						cacheDatos.setDato(direccion, registros[Integer.parseInt(codificacion[2])]);
						bloqueoCacheDatos.release();
						this.revisarOtraCacheSW = false;
	        			terminoStore = true;
	        			this.esperandoCache = false;
	        			
	        		//Bus ocupado	
					}else {
						this.bloqueoCacheDatos.release();
						try {
		                    this.barrier.await();
		                } catch (InterruptedException ex) {
		                    Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
		                } catch (BrokenBarrierException ex) {
		                    Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
		                }
					}
					
				}
												
			//Cache propia ocupada
			} else {
				try {
                    this.barrier.await();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
                } catch (BrokenBarrierException ex) {
                    Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
                }
			}
		}
    }
                  
	public void loadWord(String[] codificacion){
    	boolean terminoLoad = false;
        int palabra;
                while(!terminoLoad) {
                	this.esperandoCache = true;
                    if(bloqueoCacheDatos.tryAcquire()){
                    		direccion = Integer.parseInt(codificacion[3]) + registros[Integer.parseInt(codificacion[1])];
                            
                            //Hit
                            if((this.cacheDatos.contenerBloque(direccion))) {
                                palabra = this.cacheDatos.getDato(direccion);
                                registros[Integer.parseInt(codificacion[2])] = palabra;
                                bloqueoCacheDatos.release();
                                terminoLoad = true;
                                this.esperandoCache = false;

                            //Fallo
                            } else {
                                if(busDatos.tryAcquire()) {
                                    try {
                                    	this.barrier.await();
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (BrokenBarrierException ex) {
                                        Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                        this.revisarOtraCacheLW = true;

                                        this.datoCargado = false;
                                        while(!datoCargado) {
                                            try {
                                            this.barrier.await();
                                            } catch (InterruptedException ex) {
                                                Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
                                            } catch (BrokenBarrierException ex) {
                                                Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }
                                        //Controlador se encarga de bloquear la otra cache

                                        
                                        busDatos.release();
                                        palabra = this.cacheDatos.getDato(direccion);
                                        registros[Integer.parseInt(codificacion[2])] = palabra;
                                        bloqueoCacheDatos.release();
                                        terminoLoad = true;
                                        this.revisarOtraCacheLW = false;
                                        this.esperandoCache = false;

                                } else {
                                    bloqueoCacheDatos.release();
                                    try {
                                    	this.barrier.await();
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (BrokenBarrierException ex) {
                                        Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }


                    } else {
                        try {
                        this.barrier.await();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (BrokenBarrierException ex) {
                            Logger.getLogger(Nucleo.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }
    }   

	
}

