/*

Copyright (C) 2006 Giovanni Di Mingo

    This program is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software 
Foundation; either version 2 of the License, or any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT 
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
A PARTICULAR PURPOSE. See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along with 
this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, 
Suite 330, Boston, MA 02111-1307 USA

Author: Giovanni Di Mingo
Email:  giovanni@dimingo.com

*/

/*
 * WriteBytesToRS232.java
 *
 * Created on March 26, 2005, 8:27 PM
 *
 * This class is just for testing the different ways of controlling Serial Ports in java
 */

package com.dimingo.usbbutton;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

//import javax.comm.*;

/**
 *
 * @author Giovanni Di Mingo
 */
public class WriteBytesToRS232 {
    
    static Enumeration	      portList;
    static CommPortIdentifier portId;
    static byte[]	      bytesToRS232 = {39};
    static SerialPort	      serialPort;
    static OutputStream       outputStream;
    static boolean	      outputBufferEmptyFlag = false;
    /**
     * Method declaration
     *
     *
     * @param args
     *
     * @see
     */
    public static void main(String[] args) {

	boolean portFound = false;
	String  defaultPort = "/dev/tts/USB1";

	if (args.length > 0) {
	    defaultPort = args[0];
	} 
	portList = CommPortIdentifier.getPortIdentifiers();
	while (portList.hasMoreElements()) {
	    portId = (CommPortIdentifier) portList.nextElement();
	    if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
		if (portId.getName().equals(defaultPort)) {
		    System.out.println("Found port " + defaultPort);
		    portFound = true;
		    try {
			serialPort = (SerialPort) portId.open("SimpleWrite", 2000);
		    } catch (PortInUseException e) {
			System.out.println("Port in use.");
			continue;
		    } 
		    try {
			outputStream = serialPort.getOutputStream();
		    } catch (IOException e) {}
		    try {
			serialPort.setSerialPortParams(9600, 
						       SerialPort.DATABITS_8, 
						       SerialPort.STOPBITS_1, 
						       SerialPort.PARITY_NONE);
		    } catch (UnsupportedCommOperationException e) {}
		    try {
		    	serialPort.notifyOnOutputEmpty(true);
		    } catch (Exception e) {
			System.out.println("Error setting event notification");
			System.out.println(e.toString());
			System.exit(-1);
		    }
                    System.out.println(
                    "Writing \""+bytesToRS232[0]+"\" hexadecimal to "
                    +serialPort.getName());
		    try {
			outputStream.write(bytesToRS232[0]);
		    } catch (IOException e) {}
		    try {
		       Thread.sleep(2000);  // Be sure data is xferred before closing
		    } catch (Exception e) {}
		    serialPort.close();
		    System.exit(1);
		} 
	    } 
	} 

	if (!portFound) {
	    System.out.println("port " + defaultPort + " not found.");
	} 

    /*

    Runtime rt = Runtime.getRuntime();
    Process p = null;
    String portname = "com5:";
    // for Win95 : c:\\windows\\command.com
    //             c:\\windows\\command\\mode.com   
    String cmd[] = {
       "c:\\windows\\system32\\cmd.exe", "/c",
       "start", "/min",
       "c:\\windows\\system32\\mode.com", portname,
       "baud=9600", "parity=n", "data=8",
       "stop=1"
    };
    try {
        p = rt.exec( cmd );
        if( p.waitFor() != 0 ) {
            System.out.println("Error executing command: " + cmd );
            System.exit( -1 );
        }
        /*
        byte data[] = {39};
        //  "Writing a byte stream out of a serial port.".getBytes();
        FileOutputStream fos = new FileOutputStream( portname );
        BufferedOutputStream bos = new BufferedOutputStream( fos );
        fos.write( data, 0, data.length );
        fos.close();
         *
    }
    catch ( Exception e ) {
       e.printStackTrace();
    }
    
    try {
            RandomAccessFile serial  = new RandomAccessFile("COM5", "rw");
            byte[] a = {20};
            serial.write(a);
            serial.close();
        }
        catch (IOException e) {
            System.err.println("Random Access File Error");
            System.exit(1);
        }
         */
    }

}
