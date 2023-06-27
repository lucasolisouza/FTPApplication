import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class FTPServer {
    public static final String DEFAULT_DIRECTORY = "\\diretory\\"; // Defina o diretório padrão para os arquivos

    public static void main(String[] args) {
        int controlPort = 12384;
        int dataPort = 8888;

        try {
            ServerSocket controlSocket = new ServerSocket(controlPort);
            System.out.println("Servidor FTP iniciado.");
            System.out.println("Aguardando conexões na porta " + controlPort + "...");

            while (true) {
                Socket clientSocket = controlSocket.accept();
                Thread clientThread = new Thread(new ClientHandler(clientSocket, dataPort));
                clientThread.start();
            }
        } catch (IOException e) {e.printStackTrace();}
    }
}

class ClientHandler implements Runnable {
    private Socket controlSocket;
    private int dataPort;
    public String currentDirectory;

    public ClientHandler(Socket controlSocket, int dataPort) {
        this.controlSocket = controlSocket;
        this.dataPort = dataPort;
        this.currentDirectory = FTPServer.DEFAULT_DIRECTORY;
    }

    public void run() {
        try {
            String clientAddress = controlSocket.getInetAddress().getHostAddress();
            System.out.println("Conexão estabelecida com " + clientAddress);
            File dirAtual = new File(FTPServer.DEFAULT_DIRECTORY);
            BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
            
            boolean connectionClose = false;
            while (!connectionClose) {
	            String command = controlReader.readLine();
	            String[] commandParts = command.split(" ");
	            String line;
	            
	            if (commandParts[0].equals("RETR")) {
	                String filename = commandParts[1];
	                File file = new File(dirAtual+"\\"+ filename);
	                if (file.exists()) {
	                    controlWriter.println("OK");
	                    // Cria um soquete de dados para o cliente
	                    ServerSocket dataSocket = new ServerSocket(dataPort);
	                    controlWriter.println(dataPort);
	                    Socket dataConnection = dataSocket.accept();
	                    BufferedOutputStream dataWriter = new BufferedOutputStream(dataConnection.getOutputStream());
	                    FileInputStream fileReader = new FileInputStream(file);
	                    byte[] buffer = new byte[1024];
	                    int bytesRead;
	                    while ((bytesRead = fileReader.read(buffer)) != -1)
	                        dataWriter.write(buffer, 0, bytesRead);
	                    fileReader.close();
	                    dataWriter.flush();
	                    dataWriter.close();
	                    dataConnection.close();
	                    dataSocket.close();
	                    System.out.println("Arquivo '" + filename + "' enviado para " + clientAddress);
	                } else controlWriter.println("ERROR: Arquivo não encontrado");
	            }else if (commandParts[0].equals("LIST")) {
	                controlWriter.println("OK");
	                // Cria um soquete de dados para o cliente
	                ServerSocket dataSocket = new ServerSocket(dataPort);
	                controlWriter.println(dataPort);
	                Socket dataConnection = dataSocket.accept();
	                PrintWriter dataWriter = new PrintWriter(dataConnection.getOutputStream(), true);
	                File[] files = dirAtual.listFiles();
	                for (File file : files)
	                    dataWriter.println(file.getName());
	                dataWriter.close();
	                dataConnection.close();
	                dataSocket.close();
	                System.out.println("Lista de arquivos enviada para " + clientAddress);
	            }else if(commandParts[0].equals("STOR")) {
	                controlWriter.println("OK");
	                File file = new File(commandParts[2] + "\\" + commandParts[1]);
	                System.out.println(file.toString());
	                // Cria um soquete de dados para o cliente
	                ServerSocket dataSocket = new ServerSocket(dataPort);
	                controlWriter.println(dataPort);
	                Socket dataConnection = dataSocket.accept();
	                BufferedInputStream dataReader = new BufferedInputStream(dataConnection.getInputStream());
	                FileOutputStream fileWriter = new FileOutputStream(file);
	                byte[] buffer = new byte[1024];
	                int bytesRead;
	                while ((bytesRead = dataReader.read(buffer)) != -1) 
	                    fileWriter.write(buffer, 0, bytesRead);
	                fileWriter.close();
	                dataReader.close();
	                dataConnection.close();
	                dataSocket.close();
	                System.out.println("Arquivo '" + commandParts[1] + "' recebido e salvo em: " + file.getAbsolutePath());
	            }else if (commandParts[0].equals("PWD")) {
	                controlWriter.println("OK");
	                String currentDirectory = dirAtual.getAbsolutePath();
	                controlWriter.println(currentDirectory);
	            }else if (commandParts[0].equals("CWD")) {
	            	String[] arquivos = dirAtual.list();
	            	List listaArquivo = Arrays.asList(arquivos);
	            	if (listaArquivo.contains(commandParts[1])) {
						File novoDir = new File(dirAtual + "\\" + commandParts[1]);
						String caminhoNovo = novoDir.getAbsolutePath();
						if (novoDir.isDirectory())
							dirAtual = novoDir;
	            	}
	                controlWriter.println("OK");
	                controlWriter.println(dirAtual); 
	            }else if (commandParts[0].equals("CDUP")) {
					File novoDir = new File(dirAtual.getParent());
					dirAtual = novoDir;
					controlWriter.println("OK");
					controlWriter.println(dirAtual); 
	            }else if (commandParts[0].equals("QUIT")) connectionClose = true;
	            else controlWriter.println("ERROR: Comando inválido");
            }
            controlSocket.close();
        } catch (IOException e) {e.printStackTrace();} 
   }
}
