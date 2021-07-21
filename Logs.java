import java.io.BufferedWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

class Logs{
    private DateFormat dateTimeformat;
    private BufferedWriter logWriter;

    public Logs(BufferedWriter logWriter){
        dateTimeformat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        this.logWriter = logWriter;
    }
    public void WriteLog(String log)
    {
        try{
            logWriter.write(log);
            logWriter.flush();
        }
        catch(Exception e){
        }
    }

    public void connectionTo(int id1,int id2){
        Date time = new Date();
        String log = dateTimeformat.format(time) + ":"+" Peer "+id1+ " makes a connection to Peer "+id2+".\n";
        WriteLog(log);
    }

    public void connectionFrom(int id1, int id2){
        Date time = new Date();
        String log = dateTimeformat.format(time) + ":"+" Peer "+id1+ " is connected from Peer "+id2+".\n";
        WriteLog(log);
    }

    public void log_Preferred_Neighbors(int id1, int[] ids){
        Date time = new Date();
        String log = dateTimeformat.format(time)+ ": Peer "+id1 +" has the preferred neighbors "+ Arrays.toString(ids) + ".\n";
        WriteLog(log);
    }

    public void log_Optimistically_Unchoked_Neighbor(int id1, int id2){
        Date time = new Date();
        String log = dateTimeformat.format(time) + ":"+" Peer "+id1+ " has the optimistically unchoked neighbor "+id2+".\n";
        WriteLog(log);
    }

    public void unchoked(int id1, int id2){
        Date time = new Date();
        String log = dateTimeformat.format(time) + ":"+" Peer "+id1+ " is unchoked by "+id2+".\n";
        WriteLog(log);
    }

    public void choked(int id1, int id2){
        Date time = new Date();
        String log = dateTimeformat.format(time) + ":"+" Peer "+id1+" is choked by "+id2+".\n";
        WriteLog(log);
    }

    public void receive_Have(int id1, int id2, int index){
        Date time = new Date();
        String log = dateTimeformat.format(time) + ":"+" Peer "+id1+" received the 'have' message from "+id2+" for the piece " +index+ ".\n";
        WriteLog(log);
    }

    public void receive_Interested(int id1, int id2){
        Date time = new Date();
        String log = dateTimeformat.format(time) + ":"+" Peer "+id1+" received the 'interested' message from "+ id2+ ".\n";
        WriteLog(log);
    }

    public void receive_Not_Interested(int id1, int id2){
        Date time = new Date();
        String log = dateTimeformat.format(time) + ":"+" Peer "+id1+" received the 'not interested' message from "+ id2+ ".\n";
        WriteLog(log);
    }

    public void downloading_Piece(int id1, int id2, int index, int numOfPieces){
        Date time = new Date();
        String log = dateTimeformat.format(time) + ":"+" Peer "+id1+" has downloaded the piece " +index+ " from "+ id2+ ".\n"+
                "Now the number of pieces it has is "+ numOfPieces+ ".\n" ;
        WriteLog(log);
    }

    public void download_Completed(int id1){
        Date time = new Date();
        String log = dateTimeformat.format(time) + ":"+ " Peer "+id1+ " has downloaded the complete file.\n" ;
        WriteLog(log);
    }

}
