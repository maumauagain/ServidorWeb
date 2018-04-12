package xti2;

/**
 * ServidorWeb
 * Atividade de Redes de Computadores
 * @version 2.0
 * @author Alex Duarte, Amauri Martins Junior, Julia Nicola Guave
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;
import HttpRequest;

public class ServidorWeb {
	
	public static void main(String args[]) throws IOException {
		//Inicia um endereco proprio para o servidor
		InetAddress endereco = InetAddress.getByName("192.168.137.1");
		//InetAddress endereco = InetAddress.getByName("10.20.146.250");
		//Declara a porta 7777
		int port = 7778;
		
		//Cria um socket para o servidor com base no endereco e port
		ServerSocket servidor = new ServerSocket(port, 5, endereco);
		//ServerSocket servidor = new ServerSocket(port);
		//System.out.println(endereco.toString());
		
		while(true) {
			//Cria um socket ao receber um pedido do servidor
			Socket socket = servidor.accept();
			
			//Inicia uma nova requisicao
			HttpRequest request = new HttpRequest(socket);
			
			//A cada requisicao e criado uma nova thread
			Thread thread = new Thread(request);
			
			//Inicia a thread
			thread.start();
			
		}
		
		
	}

}


