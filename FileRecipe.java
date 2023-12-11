import java.io.Serializable;
import java.util.ArrayList;

public class FileRecipe implements Serializable {
    private ArrayList<String> fileFingerprintList;
    private int fileSize;
    
    public FileRecipe(){
        this.fileFingerprintList = new ArrayList<String>();
        this.fileSize = 0;
    }

    public void setFileSize(int fileSize){
        this.fileSize = fileSize;
    }

    public void addFingerprintList(ArrayList<String> list){
        this.fileFingerprintList = list;
    }

    public int getFileSize(){
        return this.fileSize;
    }

    public ArrayList<String> getFingerprintList(){
        return this.fileFingerprintList;
    }

}
