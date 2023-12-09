import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import static java.lang.String.*;


public class MyUpload {
    private int minChunk;
    private int avgChunk;
    private int maxChunk;
    private int base;
    private String fileToUpload;
    public MyUpload(int minChunk, int avgChunk, int maxChunk, int base, String fileToUpload){
        this.minChunk = minChunk;
        this.avgChunk = avgChunk;
        this.maxChunk = maxChunk;
        this.base = base;
        this.fileToUpload = fileToUpload; 
    }

    public void upload(){
        try (FileInputStream fis = new FileInputStream(this.fileToUpload)) {
            int content;
            // Check existence of file in file recipe
            // Use Rabin fingerprinting for chunking 
            // Use SHA-1 for calculating index 
            // Check index file for updating & upload container & update file recipe
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
