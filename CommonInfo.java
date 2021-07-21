class CommonInfo
{
    private int numberOfPreferredNeighbors;
    private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String fileName;
    private int fileSize;
    private int pieceSize;

    public int get_Number_Of_Preferred_Neighbors(){
        return numberOfPreferredNeighbors;
    }
    public void set_Number_Of_Preferred_Neighbors(int k){
        numberOfPreferredNeighbors = k;
    }
    public int get_Unchoking_Interval(){
        return unchokingInterval;
    }
    public void set_Unchoking_Interval(int u){
        unchokingInterval = u;
    }
    public int get_Optimistic_Unchoking_Interval(){
        return optimisticUnchokingInterval;
    }
    public void set_Optimistic_Unchoking_Interval(int o){
        optimisticUnchokingInterval = o;
    }
    public String get_File_Name(){
        return fileName;
    }
    public void set_File_Name(String filename){
        this.fileName = filename;
    }
    public int get_File_Size(){
        return fileSize;
    }
    public void set_File_Size(int size){
        fileSize = size;
    }
    public int get_Piece_Size(){
        return pieceSize;
    }
    public void set_Piece_Size(int piece_size){
        pieceSize = piece_size;
    }

}