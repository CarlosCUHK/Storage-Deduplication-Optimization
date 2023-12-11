import java.util.*;
import java.io.*;
import java.lang.ClassNotFoundException;    

public class MetaData {
    private HashMap<String, Chunk> fingerprintList;
    private HashMap<String, FileRecipe> fileStoring;
    private long totalChunks;
    private long uniqueChunks;
    private long totalBytes;
    private long uniqueBytes;
    private int containerNum;
    private int containerIndex;
    private double deduRatio;
    private int uploadedFileNum;
    private static int maxContainerSize = 1024*1024;
    private int containerOffset;

    public MetaData(){
        this.fingerprintList = new HashMap<String, Chunk>(); 
        this.fileStoring = new HashMap<String, FileRecipe>();
        this.totalChunks = 0;
        this.uniqueChunks = 0;
        this.totalBytes = 0;
        this.uniqueBytes = 0;
        this.containerNum = 0;
        this.deduRatio = 0;
        this.containerIndex = 0;
        this.containerOffset = 0;
        this.uploadedFileNum = 0;
    }

    public HashMap<String, FileRecipe> getFileRecipe(){
        return this.fileStoring;
    }

    public HashMap<String, Chunk> getMapping(){
        return this.fingerprintList;
    }

    public void putChunk(String checksumStr, byte[] currentChunk, byte[] container){
        int chunkBytes = 0;
        if ((this.containerOffset + currentChunk.length) < maxContainerSize){
            Chunk chunk = new Chunk(this.containerIndex, this.containerOffset, currentChunk.length);         
            System.arraycopy(currentChunk, 0, container, this.containerOffset, currentChunk.length);
            chunkBytes = currentChunk.length;
            this.containerOffset += currentChunk.length;
            this.fingerprintList.put(checksumStr, chunk);
        }   
        else{
            String filename = "data/container_" + Integer.toString(this.containerIndex);
            this.containerIndex++;
            this.containerOffset = 0;
            this.containerNum++;
            File directory = new File("/data");
            if (!directory.exists()){
                directory.mkdir();
            }
            try (FileOutputStream fos = new FileOutputStream(filename)) {
                ObjectOutputStream out = new ObjectOutputStream(fos);
                out.writeObject(container);
            } catch (IOException e) {
                System.err.println("[ERROR] Error writing data to file: " + e.getMessage());
            }
            chunkBytes = currentChunk.length;
            Chunk chunk = new Chunk(this.containerIndex, this.containerOffset, currentChunk.length);        
            System.arraycopy(currentChunk, 0, container, this.containerOffset, currentChunk.length); 
            this.containerOffset += currentChunk.length;
            this.fingerprintList.put(checksumStr, chunk);
        }
        this.uniqueChunks++;
        this.uniqueBytes += chunkBytes;
    }

    public void putFile(String uploadFileName, ArrayList<String> fingerprintList, int totalBytes){
        this.totalChunks += fingerprintList.size();
        this.totalBytes += totalBytes;
        this.uploadedFileNum++;
        FileRecipe myfile = new FileRecipe();
        myfile.setFileSize(totalBytes);
        myfile.addFingerprintList(fingerprintList);
        this.fileStoring.put(uploadFileName, myfile);
    }

    public boolean hasChunk(String checksumStr){
        return this.fingerprintList.containsKey(checksumStr);
    }

    public void read(String path) throws IOException{
        File file = new File(path); 
        if (file.exists()){
            try{
                ObjectInputStream input = new ObjectInputStream(new FileInputStream(path));
                this.fingerprintList = (HashMap<String, Chunk>) input.readObject();
                this.fileStoring = (HashMap<String, FileRecipe>) input.readObject();
                this.totalChunks = (Long) input.readObject();
                this.uniqueChunks = (Long) input.readObject();
                this.totalBytes = (Long) input.readObject();
                this.uniqueBytes = (Long) input.readObject();
                this.containerNum = (Integer) input.readObject();
                this.deduRatio = (Double) input.readObject();
                this.containerIndex = (Integer) input.readObject();
                this.containerOffset = (Integer) input.readObject();
                this.uploadedFileNum = (Integer) input.readObject();
                input.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        } else {
            file.createNewFile();
            System.out.println("Created mydedup.index");
        }
    } 

    public void write(String path, byte[] container) throws IOException, ClassNotFoundException{
        String filename = "data/container_" + Integer.toString(this.containerIndex);
        File directory = new File("data");
        if (!directory.exists()){
            directory.mkdir();
        }
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(container);
        } catch (IOException e) {
            System.err.println("[ERROR] Error writing data to file: " + e.getMessage());
        }
        this.containerIndex++;
        this.containerOffset = 0;
        this.containerNum++;
        this.deduRatio = (double)this.totalBytes/(double)this.uniqueBytes;
        
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
        out.writeObject(this.fingerprintList);
        out.writeObject(this.fileStoring);
        out.writeObject(this.totalChunks);
        out.writeObject(this.uniqueChunks);
        out.writeObject(this.totalBytes);
        out.writeObject(this.uniqueBytes);
        out.writeObject(this.containerNum);
        out.writeObject(this.deduRatio);
        out.writeObject(this.containerIndex);
        out.writeObject(this.containerOffset);
        out.writeObject(this.uploadedFileNum);
        out.close();
    }

    public void reportStat(){     
        System.out.println("Report Output:");
        System.out.println("Total number of files that have been stored: " + Integer.toString(this.uploadedFileNum));
        System.out.println("Total number of pre-deduplicated chunks in storage: " + Long.toString(this.totalChunks));
        System.out.println("Total number of unique chunks in storage: " + Long.toString(this.uniqueChunks));
        System.out.println("Total number of bytes of pre-deduplicated chunks in storage: " + Long.toString(this.totalBytes));
        System.out.println("Total number of bytes of unique chunks in storage: " + Long.toString(this.uniqueBytes));
        System.out.println("Total number of containers in storage: " + Integer.toString(this.containerNum));
        System.out.println("Deduplication ratio: " + Double.toString(this.deduRatio));
    }
}
