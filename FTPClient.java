import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Path;

public class FTPClient {
    private static final String DEFAULT_DOWNLOAD_DIRECTORY = "C:\\Users\\lucas\\Documents\\RedesDeComputadoresI\\Client\\"; // Defina o diretório padrão para salvar os arquivos recebidos

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int controlPort = 12384;
        int dataPort = 8888;

        // Laço para receber e executar funções
        try{
            Socket controlSocket = new Socket(serverAddress, controlPort);
            PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
            BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));

            boolean running = true;
            while (running) {
                BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("ftp> ");
                String input = consoleReader.readLine();
                String[] commandParts = input.split(" ");
                String command = commandParts[0];

                switch (command) {
                    case "RETR":
                        if (commandParts.length > 1) {
                            String filename = commandParts[1];
                            retrieveFile(controlWriter, controlReader, dataPort, filename, 
                                        serverAddress, controlPort, DEFAULT_DOWNLOAD_DIRECTORY);
                        } else {
                            System.out.println("Comando RETR requer um nome de arquivo.");
                        }
                        break;

                    case "LIST":
                        listFiles(controlWriter, controlReader, dataPort, serverAddress, controlPort);
                        //listFiles(serverAddress, controlPort, dataPort);
                        break;

                    case "PWD":
                        printWorkingDirectory(serverAddress, controlPort);
                        break;

                    case "STOR":
                        if (commandParts.length > 2) {
                            String filename = commandParts[1];
                            String directory = commandParts[2];
                            storeFile(serverAddress, controlPort, dataPort, filename, directory);
                        } else {
                            System.out.println("Comando STOR requer um nome de arquivo e um diretório de destino.");
                        }
                        break;
                    
                    case "CDW":
                        break;

                    case "QUIT":
                        running = false;
                        break;

                    default:
                        System.out.println("Comando inválido.");
                }
            }
            controlSocket.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void retrieveFile(PrintWriter controlWriter, BufferedReader controlReader, int dataPort, String filename,
                                    String serverAddress, int controlPort, String downloadDirectory ) {
        try {
            controlWriter.println("RETR " + filename);
            String response = controlReader.readLine();

            if (response.equals("OK")) {
                int serverDataPort = Integer.parseInt(controlReader.readLine());

                Socket dataSocket = new Socket(serverAddress, serverDataPort);
                BufferedInputStream dataReader = new BufferedInputStream(dataSocket.getInputStream());
                FileOutputStream fileWriter = new FileOutputStream(downloadDirectory + filename);

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = dataReader.read(buffer)) != -1) {
                    fileWriter.write(buffer, 0, bytesRead);
                }

                fileWriter.close();
                dataReader.close();
                dataSocket.close();

                System.out.println("Arquivo '" + filename + "' recebido e salvo em: " + downloadDirectory);
            } else {
                System.out.println("Erro: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void listFiles(PrintWriter controlWriter, BufferedReader controlReader, int dataPort,String serverAddress, int controlPort) {
        try {
            controlWriter.println("LIST");
            String response = controlReader.readLine();

            if (response.equals("OK")) {
                int serverDataPort = Integer.parseInt(controlReader.readLine());

                Socket dataSocket = new Socket(serverAddress, serverDataPort);
                BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

                String line;
                while ((line = dataReader.readLine()) != null) {
                    System.out.println(line);
                }

                dataReader.close();
                dataSocket.close();

                System.out.println("Lista de arquivos recebida com sucesso.");
            } else {
                System.out.println("Erro: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printWorkingDirectory(String serverAddress, int controlPort) {  //TODO: retirar o socket de dentro da função.
        try {
            Socket controlSocket = new Socket(serverAddress, controlPort);
            PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
            BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));

            controlWriter.println("PWD");
            String response = controlReader.readLine();

            if (response.equals("OK")) {
                String currentDirectory = controlReader.readLine();
                System.out.println("Diretório atual: " + currentDirectory);
            } else {
                System.out.println("Erro: " + response);
            }

            controlSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void storeFile(String serverAddress, int controlPort, int dataPort, String filename, String directory) { //TODO: retirar o socket de dentro da função.
        try {
            Socket controlSocket = new Socket(serverAddress, controlPort);
            PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
            BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));

            controlWriter.println("STOR " + filename + " " + directory);
            String response = controlReader.readLine();

            if (response.equals("OK")) {
                // Cria um soquete de dados para enviar o arquivo
                Socket dataSocket = new Socket(serverAddress, dataPort);
                BufferedOutputStream dataWriter = new BufferedOutputStream(dataSocket.getOutputStream());

                FileInputStream fileReader = new FileInputStream(filename);
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = fileReader.read(buffer)) != -1) {
                    dataWriter.write(buffer, 0, bytesRead);
                }

                fileReader.close();
                dataWriter.flush();
                dataWriter.close();
                dataSocket.close();

                System.out.println("Arquivo '" + filename + "' enviado para o servidor");
            } else {
                System.out.println("Erro: " + response);
            }

            controlSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
