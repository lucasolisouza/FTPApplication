import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPServer {
    public static void main(String[] args) {
        int port = 21;
        
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor FTP iniciado no porto " + port);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova conexão estabelecida com o cliente " + clientSocket.getInetAddress());
                
                // Lidar com a conexão do cliente em uma thread separada
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            
            writer.write("220 Servidor FTP pronto.\r\n");
            writer.flush();
            
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Comando recebido do cliente " + clientSocket.getInetAddress() + ": " + line);
                
                if (line.startsWith("USER")) {
                    // Lógica para processar o comando USER
                    writer.write("331 Senha necessária para o usuário.\r\n");
                    writer.flush();
                } else if (line.startsWith("PASS")) {
                    // Lógica para processar o comando PASS
                    writer.write("230 Login efetuado com sucesso.\r\n");
                    writer.flush();
                } else if (line.startsWith("LIST")) {
                    // Lógica para processar o comando LIST
                    writer.write("150 Iniciando listagem do diretório.\r\n");
                    writer.flush();
                    
                    // Enviar a lista de diretório para o cliente
                    writer.write("Arquivo1.txt\r\n");
                    writer.write("Arquivo2.txt\r\n");
                    
                    writer.write("226 Listagem de diretório concluída.\r\n");
                    writer.flush();
                } else if (line.startsWith("QUIT")) {
                    // Lógica para processar o comando QUIT
                    writer.write("221 Adeus.\r\n");
                    writer.flush();
                    break;
                } else {
                    writer.write("502 Comando não implementado.\r\n");
                    writer.flush();
                }
            }
            
            clientSocket.close();
            System.out.println("Conexão com o cliente " + clientSocket.getInetAddress() + " encerrada.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
