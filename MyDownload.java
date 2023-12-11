import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import static java.lang.String.*;

public class MyDownload {
    private String fileToDownload;
    private String localFileName;
    private MetaData metadata;


    public MyDownload(String fileToDownload, String localFileName) throws IOException{
        this.fileToDownload = fileToDownload;
        this.localFileName = localFileName;
        this.metadata = new MetaData();
        File file = new File("mydedup.index"); 
        if (!file.exists()){
            System.err.println("[ERROR] Cannot download an nonexistent file!!!");
            System.exit(1);
        } 
        this.metadata.read("mydedup.index");
    }

    public void download() throws FileNotFoundException, IOException, ClassNotFoundException{
        ObjectInputStream input;
        String containerPath;
        byte[] myContent;
        String[] tmp = this.fileToDownload.split("/");
        String fileName = tmp[tmp.length -1];
        
        if (this.metadata.getFileRecipe().containsKey(fileName)){
            FileRecipe fileRecipe = this.metadata.getFileRecipe().get(fileName);
            HashMap<String, Chunk> mapping = this.metadata.getMapping();
            ArrayList<String> fingerprintList = fileRecipe.getFingerprintList();
            myContent = new byte[fileRecipe.getFileSize()];
            byte[] container;
            Chunk currentChunk;
            int containerOffset;
            int chunkLength;
            int contentStartingOffset = 0;
            Set<String> containerSet = new HashSet<>();
            for (int i = 0; i < fingerprintList.size(); i++){
                currentChunk = mapping.get(fingerprintList.get(i));
                int tmpLength = currentChunk.getChunkLength();
                int tmpOffset = contentStartingOffset;
                if (!containerSet.contains(currentChunk.getContainerIndex())){
                    containerSet.add(Integer.toString(currentChunk.getContainerIndex()));
                    containerPath = "data/container_" + Integer.toString(currentChunk.getContainerIndex());
                    input = new ObjectInputStream(new FileInputStream(containerPath));
                    container = (byte[]) input.readObject();
                    int tmpContainerIndex = currentChunk.getContainerIndex();
                    for (int j = i; j < fingerprintList.size(); j++){
                        currentChunk = mapping.get(fingerprintList.get(j));
                        if (currentChunk.getContainerIndex() == tmpContainerIndex){
                            containerOffset = currentChunk.getOffset();
                            chunkLength = currentChunk.getChunkLength();
                            System.arraycopy(container, containerOffset, myContent, tmpOffset, chunkLength);
                        }
                        tmpOffset += currentChunk.getChunkLength();
                    }
                }
                contentStartingOffset += tmpLength;
            }
            try (FileOutputStream fos = new FileOutputStream(this.localFileName)) {
                fos.write(myContent); 
                System.out.println("[INFO] " + this.fileToDownload + " has been downloaded successfully!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            System.err.println("[ERROR] Cannot download an nonexistent file!!!");
            System.exit(1);
        }
    }
}
