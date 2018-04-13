/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorweb;


import java.io.*;
import java.net.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpRequest implements Runnable {

	//Texto apresentavel em caso de erro 404
	static String html404 = "<!doctype html>\n<html>\n<body>\n<h1>ERRO 404 : Pagina nao encontrada</h1>\n</body>\n</html>";

	//Caminho do arquivo requisitado pelo cliente
	static Path caminho;
	
	//Declaracao de socket
	Socket socket;

	//Inicia a classe
	public HttpRequest(Socket socket) {
		this.socket = socket;
	}

	//Funcao da implementacao do Runnable
	public void run() {
		try {
			//Inicia a funcao a qual recebe o pedido e devolve ao cliente
			processRequest();
		} catch (Exception e) {
			System.err.println("ERROR RUN: " + e.toString());
			return;
		}

	}

	private void processRequest() throws IOException {

		//Atraves do socket, "is" recebe o pedido
		InputStream is = socket.getInputStream();
		
		//Declara "os", que e a saida para o cliente
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());

		//Cria um BufferedReader a partir do InputStream do cliente
		BufferedReader br;
		br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		//Le a primeira linha contem as informacoes da requisicao
		String linha = br.readLine();
		//Quebra a string pelo espaco em branco
		String[] dadosReq = linha.split(" ");
		//Paga o caminho do arquivo
		String caminhoArquivo = dadosReq[1];
		//Pega o protocolo
		String protocolo = dadosReq[2];
		//Enquanto a linha nao for vazia
                System.out.println("Protocolo: " + protocolo);
                System.out.println("Caminho: " + caminhoArquivo);
                
                String host = br.readLine().substring(6);

		//Se o caminho foi igual a / entao deve pegar o /index.html
		if (caminhoArquivo.equals("/")) {
			caminhoArquivo = "\\index.html";
		}

		//Identifica o nome do arquivo requisitado
		String arquivo = caminhoArquivo.substring(1);

		//Identifica a pasta em que o arquivo do projeto esta rodando
		Path path = Paths.get(System.getProperty("user.dir"));

		//Retorna true se o arquivo requisitado pelo cliente foi encontrado nas pastas
		boolean arquivoEncontrado = procurarArquivo(path, arquivo);

		String html = "";
		String status = "";

		//Condicao na qual se o arquivo for encontrado retorna-se status 200 OK,
		//caso nao for encontrado deve retornar status 404 Not Found
		if (arquivoEncontrado) {
			status = protocolo + " 200 OK\r\n";
			String line = null;
			
			//Transfere o arquivo encontrado para a string "html"
			try (BufferedReader reader = Files.newBufferedReader(caminho)) {
				while ((line = reader.readLine()) != null) {
					html += line;
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("ERROR: " + e.toString());

			}
		} else {
			status = protocolo + " 404 Not Found\r\n";
			html = html404;
		}

		//Cria formato de data padrao http
		SimpleDateFormat formatador = new SimpleDateFormat("E, dd MMM yyyy hh:mm:ss", Locale.ENGLISH);
		formatador.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date data = new Date();
		//Formata a data para o padrao
		String dataFormatada = formatador.format(data) + " GMT";
		//Cabecalho padrao da resposta HTTP
		String header = status + "Location: http://"+ host + "\r\n" + "Date: " + dataFormatada + "\r\n"
				+ "Server: MeuServidor/1.0\r\n" + "Content-Type: text/html\r\n" + "Content-Length: " + html.length()
				+ "\r\n" + "Connection: close\r\n" + "\r\n";

		os.writeBytes(header);
		os.writeBytes(html);
		os.flush();
		os.close();

	}

	//A funcaoo usa metodo recursivo para encontrar o arquivo requisitado dentro das pastas do protejo Java
	public static boolean procurarArquivo(Path path, String arquivo) {
		if (Files.isRegularFile(path)) {

			if (path.toAbsolutePath().endsWith(arquivo)) {
				caminho = path;
				return true;
			}
		} else {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
				for (Path p : stream) {
					// System.out.println(p.toAbsolutePath());
					boolean encontrou = procurarArquivo(p, arquivo);
					if (encontrou) {
						return true;
					}
				}
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		return false;
	}
}
