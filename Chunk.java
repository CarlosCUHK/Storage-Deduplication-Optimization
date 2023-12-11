import java.util.*;
import java.io.*;
import java.lang.ClassNotFoundException;

public class Chunk implements Serializable {
    private int containerIndex;
    private int offset;
    private int chunkLength;
    public Chunk(int containerIndex, int offset, int chunkLength){
        this.containerIndex = containerIndex;
        this.offset = offset;
        this.chunkLength = chunkLength;
    }

    public int getContainerIndex(){
        return this.containerIndex;
    }

    public int getOffset(){
        return this.offset;
    }

    public int getChunkLength(){
        return this.chunkLength;
    }
}
