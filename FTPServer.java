import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class FTPServer {
    public static final String DEFAULT_DIRECTORY = "C:\\Users\\lucas\\Documents\\RedesDeComputadoresI\\Server\\"; // Defina o diretório padrão para os arquivos

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket controlSocket;
    private int dataPort;
    private String currentDirectory;

    public ClientHandler(Socket controlSocket, int dataPort) {
        this.controlSocket = controlSocket;
        this.dataPort = dataPort;
        this.currentDirectory = FTPServer.DEFAULT_DIRECTORY;
    }

    public void run() {
        try {
            String clientAddress = controlSocket.getInetAddress().getHostAddress();
            System.out.println("Conexão estabelecida com " + clientAddress);

            BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);

            String command = controlReader.readLine();
            String[] commandParts = command.split(" ");

            if (commandParts[0].equals("RETR")) {
                String filename = commandParts[1];
                File file = new File(FTPServer.DEFAULT_DIRECTORY + filename);

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

                    while ((bytesRead = fileReader.read(buffer)) != -1) {
                        dataWriter.write(buffer, 0, bytesRead);
                    }

                    fileReader.close();
                    dataWriter.flush();
                    dataWriter.close();
                    dataConnection.close();
                    dataSocket.close();

                    System.out.println("Arquivo '" + filename + "' enviado para " + clientAddress);
                } else {
                    controlWriter.println("ERROR: Arquivo não encontrado");
                }
            } else if (commandParts[0].equals("LIST")) {
                controlWriter.println("OK");

                // Cria um soquete de dados para o cliente
                ServerSocket dataSocket = new ServerSocket(dataPort);
                controlWriter.println(dataPort);

                Socket dataConnection = dataSocket.accept();
                PrintWriter dataWriter = new PrintWriter(dataConnection.getOutputStream(), true);

                File directory = new File(currentDirectory);
                File[] files = directory.listFiles();

                for (File file : files) {
                     dataWriter.println(file.getName());
                }

                dataWriter.close();
                dataConnection.close();
                dataSocket.close();

                System.out.println("Lista de arquivos enviada para " + clientAddress);
            } else if (commandParts[0].equals("PWD")) {
                controlWriter.println("OK");  //TODO: refazer a lógica do PWD.

                String currentDirectory = getCurrentDirectory();
                controlWriter.println(currentDirectory); 
            } else if (commandParts[0].equals("STOR")) {
                String filename = commandParts[1];
                controlWriter.println("OK");

                String directory = commandParts[2];
                File file = new File(directory + "\\" + filename);

                // Cria um soquete de dados para o cliente
                ServerSocket dataSocket = new ServerSocket(dataPort);
                controlWriter.println(dataPort);

                Socket dataConnection = dataSocket.accept();
                BufferedInputStream dataReader = new BufferedInputStream(dataConnection.getInputStream());

                FileOutputStream fileWriter = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = dataReader.read(buffer)) != -1) {
                    fileWriter.write(buffer, 0, bytesRead);
                }

                fileWriter.close();
                dataReader.close();
                dataConnection.close();
                dataSocket.close();

                System.out.println("Arquivo '" + filename + "' recebido e salvo em: " + file.getAbsolutePath());
            } else {
                controlWriter.println("ERROR: Comando inválido");
            }

            controlSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentDirectory() {
        Path currentPath = Paths.get("");
        return currentPath.toAbsolutePath().toString();
    }
}
