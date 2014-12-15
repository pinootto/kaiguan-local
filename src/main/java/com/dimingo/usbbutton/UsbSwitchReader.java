
package com.dimingo.usbbutton;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TooManyListenersException;

//import javax.comm.*;


/**
 * Class declaration
 *
 *
 * @author Giovanni Di Mingo
 */
public class UsbSwitchReader implements Runnable, SerialPortEventListener {
	
    static CommPortIdentifier 	portId;
    static Enumeration	      	portList;
    InputStream		      		inputStream;
    SerialPort		      		serialPort;
    Thread		      			readThread;
    
    static String 				command;
    

    /**
     * Method declaration
     *
     *
     * @param args
     *
     * @see
     */
    public static void main(String[] args) {
    	
	    boolean		    portFound = false;
	    String		    defaultPort = "/dev/ttyUSB0";
	    
	    boolean			debug = true;
	
	 	if (args.length > 0) {
		    defaultPort = args[0];
		} 
	   
	 	// define the configuration properties
	 	Properties properties = new Properties();
	 	
	 	try {
	 		
	 		// load the properties from the configuration file
			properties.load(new FileInputStream("usbswitch.properties"));
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// get the command to execute
		command = properties.getProperty("command");
		
		System.out.println("command = " + command);
		
		// debug only
		if (debug) {
		
			// test the command
			try {
				
				// execute the command
				Runtime.getRuntime().exec(command);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 	
		} // end if debug
		
		portList = CommPortIdentifier.getPortIdentifiers();
	
		while (portList.hasMoreElements()) {
			
		    portId = (CommPortIdentifier) portList.nextElement();
		    if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (portId.getName().equals(defaultPort)) {
				    System.out.println("Found port: "+defaultPort);
				    portFound = true;
				    UsbSwitchReader reader = new UsbSwitchReader();
				} 
		    } 
		    
		} // end while
		
		if (!portFound) {
		    System.out.println("port " + defaultPort + " not found.");
		} 
 	
    } // end main
    

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public UsbSwitchReader() {
    	
		try {
			
			// open the serial port
		    serialPort = (SerialPort) portId.open("UsbSwitchReader", 2000);
		    
		} catch (PortInUseException e) {}
	
		try {
			
			// get the serial input stream
		    inputStream = serialPort.getInputStream();
		    
		} catch (IOException e) {}
	
		try {
			
			// add the serial event listener
		    serialPort.addEventListener(this);
		    
		} catch (TooManyListenersException e) {}
	
//		serialPort.notifyOnDataAvailable(true);
		serialPort.notifyOnCarrierDetect(true);
//		serialPort.notifyOnCTS(true);
//		serialPort.notifyOnDSR(true);
//		serialPort.notifyOnRingIndicator(true);
	
		try {
			
			// set serial port parameters
		    serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, 
						   SerialPort.STOPBITS_1, 
						   SerialPort.PARITY_NONE);
		    
		} catch (UnsupportedCommOperationException e) {}
	
		// create the reader thread
		readThread = new Thread(this);
	
		// start the reader thread
		readThread.start();
		
    } // end constructor
    

    /**
     * Method declaration
     *
     *
     * @see
     */
    public void run() {
    	
//    	serialPort.setDTR(true);  // pin 4 (positive voltage)
    	
    	// define the flag to indicate whether button is pressed or not
    	boolean isButtonPressed = false;
    	
    	
		try {
			
			// loop forever
            while (true) {
            	
//	            Thread.sleep(20000);
            	
            	// read the status of the carrier detect signal
            	isButtonPressed = serialPort.isCD();
            	
            	System.out.println("isButtonPressed: " + isButtonPressed);
            	
            	Thread.sleep(500);
            	
//            	serialPort.setRTS(true);  // pin 7
//
//                Thread.sleep(200);
//                
//                serialPort.setRTS(false); // pin 7
//                
//                Thread.sleep(200);
//                
//                serialPort.setDTR(true);  // pin 4
//
//                Thread.sleep(200);
//                
//                serialPort.setDTR(false); // pin 4
//                
//                Thread.sleep(200);
                
            } // end while
	            
		} catch (InterruptedException e) {}
		
    } // end run
    

    /**
     * Method declaration
     *
     *
     * @param event
     *
     * @see
     */
    public void serialEvent(SerialPortEvent event) {
    	
    	boolean isButtonPressed = false;
    	
    	boolean isStillButtonPressed = false;
    	
		switch (event.getEventType()) {
	
			case SerialPortEvent.BI:
		
			case SerialPortEvent.OE:
		
			case SerialPortEvent.FE:
		
			case SerialPortEvent.PE:
		
			case SerialPortEvent.CD: 	// pin 1
				
				// read the CD signal value
				isButtonPressed = serialPort.isCD();
				
				System.out.println("Event: CD");
				
				System.out.println("After event, CD is: " + isButtonPressed);
				
				try {
					
					// wait for eventual bouncing
					Thread.sleep(100);
					
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
				// read again the CD signal value
				isStillButtonPressed = serialPort.isCD();
				
				// check if signal is stable
				if (isButtonPressed == isStillButtonPressed) {
					
					try {
						
						// execute the command: switch ON
						Runtime.getRuntime().exec(command + " true");
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				} else { // no button pressed
					
					try {
						
						// execute the command: switch OFF
						Runtime.getRuntime().exec(command + " false");
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			
				break;
		
			case SerialPortEvent.CTS:	// pin 8
				
				System.out.println("Event: CTS");
				
				break;
		
			case SerialPortEvent.DSR:	// pin 6		
		
				System.out.println("Event: DSR");
				
				break;
				
			case SerialPortEvent.RI:	// pin 9

				System.out.println("Event: RI");
				
//				serialPort.setRTS(true);  // pin 7
				
				break;
				
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				
			    break;
		
			case SerialPortEvent.DATA_AVAILABLE:
				
			    byte[] readBuffer = new byte[20];
		
			    try {
					while (inputStream.available() > 0) {
					    int numBytes = inputStream.read(readBuffer);
			                    System.out.print("numero di bytes: " + numBytes + " --> ");
					} 
	                System.out.println(new String(readBuffer));
			    } catch (IOException e) {}
		
			    break;
			    
		} // end switch
		
    } // end serialEvent

} // end class




