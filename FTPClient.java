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
            
            BufferedReader userInputR = new BufferedReader(new InputStreamReader(System.in));
            String userInput;

            do {
                System.out.print("ftp> ");
                userInput = userInputR.readLine();

                sendCommand(writer, userInput);

                // Ler e imprimir a resposta do servidor
                String serverResponse;
                while ((serverResponse = reader.readLine()) != null) {
                    System.out.println(serverResponse);
                    if (serverResponse.startsWith("226") || serverResponse.startsWith("250")) {
                        break;
                    }
                }
            } while (!userInput.equalsIgnoreCase("QUIT"));
            
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
