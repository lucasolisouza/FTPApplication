import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPServer {
    public static void main(String[] args) {
        int port = 12384;
        
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
                    File directory = new File("C:\\Users\\lucas\\Documents\\Sistemais Operacionais"); // Diretório a ser listado
                    File[] files = directory.listFiles();
                    
                    for (File file : files) {
                        if (file.isFile()) {
                            writer.write(file.getName() + "\r\n");
                        }
                    }
                    
                    writer.write("226 Listagem de diretório concluída.\r\n");
                    writer.flush();
                }else if (line.startsWith("RETR")){
                    // Lógica para processar o comando RETR
                    String[] parts = line.split(" ");
                    String filename = parts[1]; // Extrai o nome do arquivo a ser baixado
                    
                    File file = new File("C:\\Users\\lucas\\Documents\\Sistemais Operacionais\\FTPApplication\\" + filename); // Caminho completo para o arquivo
                    
                    if (file.exists() && file.isFile()) {
                        writer.write("150 Iniciando transferência do arquivo.\r\n");
                        writer.flush();
                        
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            clientSocket.getOutputStream().write(buffer, 0, bytesRead);
                        }
                        
                        fileInputStream.close();
                        
                        writer.write("226 Transferência de arquivo concluída.\r\n");
                        writer.flush();
                    } else {
                        writer.write("550 Arquivo não encontrado.\r\n");
                        writer.flush();
                    } 
                }else if (line.startsWith("STOR")) {
                    // Lógica para processar o comando STOR
                    String[] parts = line.split(" ");
                    String filename = parts[1]; // Extrai o nome do arquivo a ser armazenado
                    
                    writer.write("150 Iniciando transferência do arquivo.\r\n");
                    writer.flush();
                    
                    FileOutputStream fileOutputStream = new FileOutputStream("/path/to/directory/" + filename); // Caminho completo para o arquivo
                    
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    
                    while ((bytesRead = clientSocket.getInputStream().read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                    
                    fileOutputStream.close();
                    
                    writer.write("226 Transferência de arquivo concluída.\r\n");
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
