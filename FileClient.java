import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class FileClient {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Change this to the server's address
        int serverPort = 4148; // Change this to the server's port number

        try (Socket socket = new Socket(serverAddress, serverPort);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            Scanner scanner = new Scanner(System.in);

            System.out.println("Welcome to the File Server!");
            while (true) {
                System.out.println("\nChoose an option:");
                System.out.println("1. Upload a file");
                System.out.println("2. Download a file");
                System.out.println("3. Quit");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline

                if (choice == 1) {
                    System.out.print("Enter the path of the file to upload: ");
                    String filePath = scanner.nextLine();
                    uploadFile(filePath, out);
                } else if (choice == 2) {
                    System.out.print("Enter the file name to download: ");
                    String fileName = scanner.nextLine();
                    downloadFile(fileName, out, in);
                } else if (choice == 3) {
                    System.out.println("Goodbye!");
                    break;
                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void uploadFile(String filePath, OutputStream out) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("File not found.");
            return;
        }

        out.write("UPLOAD\n".getBytes());
        out.write((file.getName() + "\n").getBytes());
        out.write((file.length() + "\n").getBytes());

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = fis.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }

        fis.close();
        System.out.println("File uploaded successfully.");
    }

    private static void downloadFile(String fileName, OutputStream out, InputStream in) throws IOException {
        PrintWriter requestWriter = new PrintWriter(out, true);
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(in));
    
        // Send the download request to the server
        requestWriter.println("DOWNLOAD");
        requestWriter.println(fileName);
    
        // Read the file size response from the server
        String fileSizeStr = responseReader.readLine();
        
        if (fileSizeStr == null || fileSizeStr.equals("-1")) {
            System.out.println("File not found on the server.");
            return;
        }
    
        // Parse the file size string to a long
        long fileSize = Long.parseLong(fileSizeStr);
    
        FileOutputStream fos = new FileOutputStream(fileName);
        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalBytesReceived = 0;
    
        while ((bytesRead = in.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
            totalBytesReceived += bytesRead;
            if (totalBytesReceived >= fileSize) {
                break;
            }
        }
    
        fos.close();
        System.out.println("File downloaded successfully.");
    }
    
}
