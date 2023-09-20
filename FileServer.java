import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
    public static void main(String[] args) {
        int port = 4148; // Change this to your desired port number
        String uploadDir = "uploads/"; // Directory to store uploaded files

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                Thread clientThread = new Thread(new ClientHandler(clientSocket, uploadDir));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String uploadDir;

    public ClientHandler(Socket clientSocket, String uploadDir) {
        this.clientSocket = clientSocket;
        this.uploadDir = uploadDir;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String request = in.readLine();
            if (request.equals("UPLOAD")) {
                receiveFile(in, out);
            } else if (request.equals("DOWNLOAD")) {
                sendFile(in, out);
            }

           clientSocket.close();
            //System.out.println("Client disconnected: " + clientSocket.getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile(BufferedReader in, PrintWriter out) throws IOException {
        String fileName = in.readLine();
        long fileSize = Long.parseLong(in.readLine());

        File file = new File(uploadDir + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalBytesReceived = 0;

        while ((bytesRead = clientSocket.getInputStream().read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
            totalBytesReceived += bytesRead;
            if (totalBytesReceived >= fileSize) {
                break;
            }
        }

        fos.close();
        out.println("File uploaded successfully.");
    }

    private void sendFile(BufferedReader in, PrintWriter out) throws IOException {
        String fileName = in.readLine();
        File file = new File(uploadDir + fileName);

        if (file.exists() && file.isFile()) {
            long fileSize = file.length();
            out.println(fileSize);

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                clientSocket.getOutputStream().write(buffer, 0, bytesRead);
            }

            fis.close();
            System.out.println("File sent: " + fileName);
        } else {
            out.println("File not found.");
        }
    }
}
