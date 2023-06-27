import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class FTPClient {
    private static final String DEFAULT_DOWNLOAD_DIRECTORY = "C:\\directory\\"; // Defina o diretório padrão para salvar os arquivos recebidos

    public static void main(String[] args) throws UnknownHostException, IOException {
        String serverAddress = "localhost";
        int controlPort = 12384;
        int dataPort = 8888;
        Socket controlSocket = new Socket(serverAddress, controlPort);

        String userInput;
        do {
			System.out.print("ftp> ");
			Scanner ler = new Scanner(System.in);
			userInput =ler.nextLine();
			String[] comando = userInput.split(" "); 
			
			switch (comando[0]) {
			case "LIST": 
				listFiles(controlSocket,serverAddress, controlPort, dataPort);
				break;
			case "RETR": 
				retrieveFile(controlSocket,serverAddress, controlPort, dataPort, comando[1], DEFAULT_DOWNLOAD_DIRECTORY);
				break;
			case "STOR":
				storeFile(controlSocket,serverAddress, controlPort, dataPort, comando[1], comando[2]);
				break;
			case "PWD":
				printWorkingDirectory(controlSocket);
				break;
			case "CWD":
				changeWorkingDirectory(controlSocket, comando[1]);
				break;
			case "CDUP":
				changetoParentDirectory(controlSocket);
				break;
			case "QUIT":
				System.out.println("Goodbye...");
				break;
			default:
				throw new IllegalArgumentException("Comando " + userInput+" não reconhecido.");
			}	
        } while (!userInput.equals("QUIT"));
        try {controlSocket.close();}
        catch (IOException e) {e.printStackTrace();}
        
    }

    public static void retrieveFile(Socket controlSocket, String serverAddress, int controlPort, int dataPort, String filename, String downloadDirectory) {
        try {
            PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
            BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));

            controlWriter.println("RETR " + filename);
            String response = controlReader.readLine();

            if (response.equals("OK")) {
                int serverDataPort = Integer.parseInt(controlReader.readLine());
                Socket dataSocket = new Socket(serverAddress, serverDataPort);
                BufferedInputStream dataReader = new BufferedInputStream(dataSocket.getInputStream());
                FileOutputStream fileWriter = new FileOutputStream(downloadDirectory + filename);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = dataReader.read(buffer)) != -1)
                    fileWriter.write(buffer, 0, bytesRead);
                fileWriter.close();
                dataReader.close();
                dataSocket.close();
                System.out.println("Arquivo '" + filename + "' recebido e salvo em: " + downloadDirectory);
            } else System.out.println("Erro: " + response);
        } catch (IOException e) {e.printStackTrace();}
    }

    public static void listFiles(Socket controlSocket,String serverAddress, int controlPort, int dataPort) {
        try {
            PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
            BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));

            controlWriter.println("LIST");
            String response = controlReader.readLine();
            if (response.equals("OK")) {
                int serverDataPort = Integer.parseInt(controlReader.readLine());
                Socket dataSocket = new Socket(serverAddress, serverDataPort);
                BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

                String line;
                while ((line = dataReader.readLine()) != null)
                    System.out.println(line);
                dataReader.close();
                dataSocket.close();
                System.out.println("Lista de arquivos recebida com sucesso.");
            } else System.out.println("Erro: " + response);
        } catch (IOException e) {e.printStackTrace();}
    }

    public static void printWorkingDirectory(Socket controlSocket) {
        try {
            PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
            BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));

            controlWriter.println("PWD");
            String response = controlReader.readLine();
            if (response.equals("OK")) {
                String currentDirectory = controlReader.readLine();
                System.out.println("Diretório atual: " + currentDirectory);
            } else System.out.println("Erro: " + response);
        } catch (IOException e) {e.printStackTrace();}
    }
    
    public static void changeWorkingDirectory(Socket controlSocket, String comando) {
        try {
            PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
            BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));

            controlWriter.println("CWD "+comando);
            String response = controlReader.readLine();
            if (response.equals("OK")) {
                String currentDirectory = controlReader.readLine();
                System.out.println("Diretório atual: " + currentDirectory);
            } else System.out.println("Erro: " + response);
        } catch (IOException e) {e.printStackTrace();}
    }

    public static void changetoParentDirectory(Socket controlSocket) {
    	  try {
              PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
              BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));

              controlWriter.println("CDUP");
              String response = controlReader.readLine();
              if (response.equals("OK")) {
                  String currentDirectory = controlReader.readLine();
                  System.out.println("Diretório Alterado com sucesso \nDiretório atual: " + currentDirectory);
              } else System.out.println("Erro: " + response);
          } catch (IOException e) {e.printStackTrace();}
    }
    
    
    public static void storeFile(Socket controlSocket, String serverAddress, int controlPort, int dataPort, String filename, String directory) { //TODO: retirar o socket de dentro da função.
        try {
            PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
            BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));

            controlWriter.println("STOR " + filename + " " + directory);
            String response = controlReader.readLine();
            if (response.equals("OK")) {
                // Cria um soquete de dados para enviar o arquivo
            	int serverPort = Integer.parseInt(controlReader.readLine());
                Socket dataSocket = new Socket(serverAddress, serverPort);
                BufferedOutputStream dataWriter = new BufferedOutputStream(dataSocket.getOutputStream());
                File arquivo = new File(DEFAULT_DOWNLOAD_DIRECTORY+filename);
                FileInputStream fileReader = new FileInputStream(arquivo);
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = fileReader.read(buffer)) != -1)
                    dataWriter.write(buffer, 0, bytesRead);
                fileReader.close();
                dataWriter.flush();
                dataWriter.close();
                dataSocket.close();
                System.out.println("Arquivo '" + filename + "' enviado para o servidor");
            } else System.out.println("Erro: " + response);

        } catch (IOException e) {e.printStackTrace();}
    }
}