import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import static java.lang.String.*;
import java.math.BigInteger;

public class MyUpload {
    private int minChunk;
    private int avgChunk;
    private int maxChunk;
    private int base;
    private String fileToUpload;
    private int anchorMask;
    private byte[] container;

    public MyUpload(int minChunk, int avgChunk, int maxChunk, int base, String fileToUpload){
        this.minChunk = minChunk;
        this.avgChunk = avgChunk;
        this.maxChunk = maxChunk;
        this.base = base;
        this.fileToUpload = fileToUpload;
        this.anchorMask = (1<<31)-1; 
        this.container = new byte[1024*1024];
    }

    public static int myPower(int base, int exponent, int modulus) {
        int value = 1;
        int num = base;
        for (; exponent > 0; exponent >>= 1) {
            if ((exponent & 1) != 0)
                value = (value * num) % modulus;
            num = (num * num) % modulus;
        }
        return value;
    }

    public ArrayList<Integer> chunking(byte[] fileContent, int length){
        ArrayList<Integer> anchorList = new ArrayList<>();
        int endIndex = 0;
        int chunkSize = 0;
        long p = 0;
        while(true){
            if (endIndex == length){
                anchorList.add(endIndex);
                break;
            }
            if (((anchorMask & p) == 0) && endIndex != 0){
                anchorList.add(endIndex);
                chunkSize = 0;
            }
            if (chunkSize == maxChunk){
                anchorList.add(endIndex);
                chunkSize = 0;
            }
            if (chunkSize == 0){
                int startIndex = endIndex;
                for (; endIndex < startIndex+this.minChunk; endIndex++){
                    if (endIndex == length){
                        anchorList.add(endIndex);
                        break;
                    }
                    else{
                        p += ((fileContent[endIndex]%this.avgChunk)*myPower(this.base, startIndex+this.minChunk-endIndex-1, this.avgChunk))%this.avgChunk;
                        chunkSize += 1;
                    }
                }
            }
            else{
                p = (this.base * (((p - (myPower(this.base, this.minChunk-1, this.avgChunk)*fileContent[endIndex-this.minChunk])%this.avgChunk))%this.avgChunk) + fileContent[endIndex])%this.avgChunk; 
                endIndex++;
                chunkSize++;
            }
        }
        return anchorList;
    }

    public void uploadFile(ArrayList<Integer> anchorList, byte[] fileContent) throws IOException, NoSuchAlgorithmException, ClassNotFoundException{
        String fingerprintIndexFile = "mydedup.index";
        MetaData metadata = new MetaData();
        ArrayList<String> fileInfo = new ArrayList<String>();
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        metadata.read(fingerprintIndexFile);
        
        for (int i = 0; i < anchorList.size(); i++) {
            if (i == 0){
                md.update(fileContent, 0, anchorList.get(0));
            } 
            else{
                md.update(fileContent, anchorList.get(i-1), anchorList.get(i)-anchorList.get(i-1));
            }
            byte[] checkSumBytes = md.digest();
            String checkSumStr = new BigInteger(checkSumBytes).toString();
            fileInfo.add(checkSumStr);
            if (i == 0){
                if (!metadata.hasChunk(checkSumStr)){
                    byte[] currentChunk = Arrays.copyOfRange(fileContent, 0, anchorList.get(0));
                    metadata.putChunk(checkSumStr, currentChunk, this.container);
                }
            }
            else{
                if (!metadata.hasChunk(checkSumStr)){
                    byte[] currentChunk = Arrays.copyOfRange(fileContent, anchorList.get(i-1), anchorList.get(i));
                    metadata.putChunk(checkSumStr, currentChunk, this.container);
                }
            }
        }
        metadata.putFile(this.fileToUpload, fileInfo, fileContent.length);
        metadata.write(fingerprintIndexFile, this.container);
        metadata.reportStat();
    }

    public void upload() throws NoSuchAlgorithmException, ClassNotFoundException{
        File file = new File(this.fileToUpload);
        ArrayList<Integer> anchorList;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileContent = new byte[(int) file.length()];
            fis.read(fileContent);
            fis.close();
            anchorList = this.chunking(fileContent, (int) file.length());
            this.uploadFile(anchorList, fileContent);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean fileExistence(String filename){
        File file = new File(filename);
        boolean existence = file.exists();
        return existence;
    }
}