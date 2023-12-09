import java.io.File;
import java.io.IOException;
import java.util.*;
import static java.lang.String.*;

public class MyDownload {
    String fileToDownload;
    String localFileName;
    public MyDownload(String fileToDownload, String localFileName){
        this.fileToDownload = fileToDownload;
        this.localFileName = localFileName;
    }
}
