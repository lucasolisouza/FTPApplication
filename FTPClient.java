import java.io.*;
import java.net.Socket;

public class FTPClient {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 12384;
        
        try {
            Socket socket = new Socket(serverAddress, serverPort);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            String response = reader.readLine();
            System.out.println(response);
            
            // Enviar comando de login
            String username = "username";
            String password = "password";
            sendCommand(writer, "USER " + username);
            response = reader.readLine();
            System.out.println(response);
            
            sendCommand(writer, "PASS " + password);
            response = reader.readLine();
            System.out.println(response);
            
            // Enviar comando de lista de diretório
            sendCommand(writer, "LIST");
            
            // Ler a resposta do servidor
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            
            // Fechar conexão
            sendCommand(writer, "QUIT");
            response = reader.readLine();
            System.out.println(response);
            
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void sendCommand(BufferedWriter writer, String command) throws IOException {
        writer.write(command);
        writer.newLine();
        writer.flush();
    }
}
