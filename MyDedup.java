import java.io.File;
import java.io.IOException;
import java.util.*;
import static java.lang.String.*;
import java.nio.file.*;

public class MyDedup {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            helpInfo();
            System.exit(1);
        }

        String operation = args[0];
        try {
            switch (operation) {
                case "upload":
                    if (args.length != 6) {
                        helpInfo();
                        System.exit(1);
                    }
                    int minChunk = Integer.parseInt(args[1]);
                    int avgChunk = Integer.parseInt(args[2]);
                    int maxChunk = Integer.parseInt(args[3]);
                    int base = Integer.parseInt(args[4]);
                    String fileToUpload = args[5];
                    if (!fileExistence(fileToUpload)) {
                        System.err.println("[ERROR] The file to upload does does not exist!!!");
                        System.exit(1);
                    }
                    File directory = new File("data");
                    if (!directory.exists()) {
                        directory.mkdir();
                    }
                    MyUpload myUpload = new MyUpload(minChunk, avgChunk, maxChunk, base, fileToUpload);
                    myUpload.upload();
                    break;
                case "download":
                    if (args.length != 3) {
                        helpInfo();
                        System.exit(1);
                    }
                    String fileToDownload = args[1];
                    String localFileName = args[2];
                    MyDownload myDownload = new MyDownload(fileToDownload, localFileName);
                    myDownload.download();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void helpInfo() {
        System.out.println("Sample Input/Output Format: ");
        System.out
                .println("    Upload: java MyDedup upload <min_chunk> <avg_chunk> <max_chunk> <base> <file_to_upload>");
        System.out.println("    Download: java MyDedup download <file_to_download> <local_file_name>");
    }

    public static boolean fileExistence(String filename) {
        File file = new File(filename);
        boolean existence = file.exists();
        return existence;
    }
}
