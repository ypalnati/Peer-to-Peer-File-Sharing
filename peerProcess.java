import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


//This class will have all the information related to the current peer
//It maintains peer id, hostname, portNum, is it having complete file or not , its bitfield and how many pieces it has.
class PeerInfo extends Thread{
    private int peer_ID;
    private String host_Name;
    private int port_Number;
    private int have_File;
    private int[] bit_field;
    private int num_Of_Pieces = 0;


    public void update_Num_Of_Pieces() {
        this.num_Of_Pieces++;
        if(this.num_Of_Pieces == bit_field.length)
            this.have_File = 1;
    }

    public int get_Num_Of_Pieces() { return num_Of_Pieces; }

    public int get_Peer_ID() {return peer_ID;}

    public void set_Peer_ID(int peer_ID) {this.peer_ID = peer_ID;}

    public String get_Host_Name() { return host_Name;}

    public void set_Host_Name(String host_Name) {this.host_Name = host_Name;}

    public int get_Port_Number() {return port_Number;}

    public void set_Port_Number(int port_Number) {this.port_Number = port_Number;}

    public int get_Have_File() {return have_File;}

    public void set_Have_File(int have_File) {this.have_File = have_File;}

    public int[] get_Bit_field() {return bit_field;}

    public void set_Bit_field(int[] bit_field) {this.bit_field = bit_field;}

    public void update_Bit_field(int index){ bit_field[index] = 1;}
}


public class peerProcess {
    private static final char CHOKE = '0';
    private static final char UNCHOKE = '1';
    private static final char INTERESTED = '2';
    private static final char NOT_INTERESTED = '3';
    private static final char HAVE = '4';
    private static final char BIT_FIELD = '5';
    private static final char REQUEST = '6';
    private static final char PIECE = '7';
    private static int host_ID;
    private static LinkedHashMap<Integer, PeerInfo> peers;
    private static byte[][] filePieces;
    private static Messages msg = new Messages();
    private static Logs logs;
    private static ConcurrentHashMap<Integer, Peer_Connection> peer_Connections;
    private static PeerInfo thisPeer;
    private static CommonInfo config_Info;
    private static int completed_Peers = 0;
    private static File directory;

    /*
        This method reads the PeerInfo.cfg file and parses each line
        Parses peer_id, host_name, port_num, have_file and puts the peer objects in linkedHashMap
    */
    static void get_Peer_Info_File_Information() throws IOException
    {
        BufferedReader peer_Info_file = new BufferedReader(new FileReader("PeerInfo.cfg"));
        peers = new LinkedHashMap<>();
        for (Object line : peer_Info_file.lines().toArray()) {
            String[] fields  = ((String) line).split(" ");
            PeerInfo peer = new PeerInfo();
            peer.set_Peer_ID(Integer.parseInt(fields[0]));
            peer.set_Host_Name(fields[1]);
            peer.set_Port_Number(Integer.parseInt(fields[2]));
            peer.set_Have_File(Integer.parseInt(fields[3]));
            peers.put(peer.get_Peer_ID(), peer);
        }
        peer_Info_file.close();
    }


    /*
        This method reads the Common.cfg file and parses each field
        and stores the value in corresponding variables of configInfo Object
    */
    static void get_Common_File_Information() throws IOException
    {
        BufferedReader config_Info_Reader = new BufferedReader(new FileReader("Common.cfg"));
        config_Info = new CommonInfo();
        Object[] common_Info_Lines = config_Info_Reader.lines().toArray();
        config_Info.set_Number_Of_Preferred_Neighbors(Integer.parseInt(((String) common_Info_Lines[0]).split(" ")[1]));
        config_Info.set_Unchoking_Interval(Integer.parseInt(((String) common_Info_Lines[1]).split(" ")[1]));
        config_Info.set_Optimistic_Unchoking_Interval(Integer.parseInt(((String) common_Info_Lines[2]).split(" ")[1]));
        config_Info.set_File_Name(((String) common_Info_Lines[3]).split(" ")[1]);
        config_Info.set_File_Size(Integer.parseInt(((String) common_Info_Lines[4]).split(" ")[1]));
        config_Info.set_Piece_Size(Integer.parseInt(((String) common_Info_Lines[5]).split(" ")[1]));
        config_Info_Reader.close();
    }

    /*
        It sets the bitfield for this peer
        If peer has the file it sets all bits in bitfield as 1 or else 0.
    */
    static void set_Bit_FieldInformation()throws IOException
    {
        thisPeer = peers.get(host_ID);
        int file_Size = config_Info.get_File_Size();
        int piece_Size = config_Info.get_Piece_Size();
        int num_Of_Pieces = (int) Math.ceil((double) file_Size / piece_Size);
        filePieces = new byte[num_Of_Pieces][];
        int[] bit_field = new int[num_Of_Pieces];
        if (thisPeer.get_Have_File() == 1) {
            completed_Peers++;
            Arrays.fill(bit_field, 1);
            thisPeer.set_Bit_field(bit_field);
            //Dividing File into pieces and storing them into array of pieces.
            BufferedInputStream file = new BufferedInputStream(new FileInputStream(directory.getAbsolutePath() + "/" + config_Info.get_File_Name()));
            byte[] file_Bytes = new byte[file_Size];
            file.read(file_Bytes);
            file.close();
            int part = 0;

            for (int counter = 0; counter < file_Size; counter += piece_Size) {
                if (counter + piece_Size <= file_Size)
                    filePieces[part] = Arrays.copyOfRange(file_Bytes, counter, counter + piece_Size);
                else
                    filePieces[part] = Arrays.copyOfRange(file_Bytes, counter, file_Size);

                part++;
                thisPeer.update_Num_Of_Pieces();
            }
        } else {
            Arrays.fill(bit_field, 0);
            thisPeer.set_Bit_field(bit_field);
        }

    }
    public static void main(String[] args) {
        File log_file;
        host_ID = Integer.parseInt(args[0]);
        try {
            /*
             *   Create the file directory corresponding to this peer.
             */
            directory = new File("peer_" + host_ID);
            if (!directory.exists()) {
                directory.mkdir();
            }
            log_file = new File(System.getProperty("user.dir") + "/" + "log_peer_" + host_ID + ".log");
            if (!log_file.exists())
                log_file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(log_file.getAbsolutePath(), true));
            writer.flush();
            logs = new Logs(writer);

            get_Peer_Info_File_Information();
            get_Common_File_Information();
            set_Bit_FieldInformation();

            peer_Connections = new ConcurrentHashMap<>();
            Send_Connections send_Connections = new Send_Connections();
            send_Connections.start();
            Receive_Connections receive_Connections = new Receive_Connections();
            receive_Connections.start();
            Unchoke_Peers unchoke_Peers = new Unchoke_Peers();
            unchoke_Peers.start();
            Optimistic_UnchokePeer optimistic_UnchokePeer = new Optimistic_UnchokePeer();
            optimistic_UnchokePeer.start();
        } catch (Exception e) {
        }
    }

    private static class Unchoke_Peers extends Thread{
        @Override
        public void run(){
            while(completed_Peers < peers.size()){
                ArrayList<Integer> connections = new ArrayList<>(peer_Connections.keySet());
                int[] preferred_Neighbors = new int[config_Info.get_Number_Of_Preferred_Neighbors()];
                if(thisPeer.get_Have_File() != 1) {
                    //if we don't have the complete file, we unchoke the neighbours who has the pieces which we need and has highest download rate
                    ArrayList<Integer> interested_Peers = new ArrayList<>();
                    int counter = 0;
                    for (int peer : connections) {
                        if(peer_Connections.get(peer).is_Interested() && peer_Connections.get(peer).get_DownloadRate() >= 0)//finding peers
                            interested_Peers.add(peer);
                    }
                    if(interested_Peers.size() <= config_Info.get_Number_Of_Preferred_Neighbors()){
                        for(int peer : interested_Peers){
                            preferred_Neighbors[counter++] = peer;
                            if(peer_Connections.get(peer).is_Choked()){
                                peer_Connections.get(peer).unchoke();
                                peer_Connections.get(peer).send_Message(UNCHOKE);
                            }
                        }
                    }
                    else {
                        for (int i = 0; i < config_Info.get_Number_Of_Preferred_Neighbors(); i++) {//if we get more than no.of preferred neighbours we choose the ones with greater download rate
                            int max = interested_Peers.get(0);
                            for(int j = 1; j < interested_Peers.size(); j++){
                                if(peer_Connections.get(max).get_DownloadRate() <= peer_Connections.get(interested_Peers.get(j)).get_DownloadRate()){
                                    max = interested_Peers.get(j);
                                }
                            }
                            if(peer_Connections.get(max).is_Choked()) {
                                peer_Connections.get(max).unchoke();
                                peer_Connections.get(max).send_Message(UNCHOKE);
                            }
                            preferred_Neighbors[i] = max;
                            interested_Peers.remove(Integer.valueOf(max));
                        }
                        for (Integer peer : interested_Peers) {
                            if(!peer_Connections.get(peer).is_Choked() && !peer_Connections.get(peer).is_Optimistically_Unchoked()){
                                peer_Connections.get(peer).choke();
                                peer_Connections.get(peer).send_Message(CHOKE);
                            }
                        }
                    }
                }
                else{
                    //if we have the complete file and still other piece haven't completed
                    //then we randomly find interested peers and try to establish connection and send data
                    ArrayList<Integer> interested_Peers = new ArrayList<>();
                    for (int peer : connections) {
                        if(peer_Connections.get(peer).is_Interested())
                            interested_Peers.add(peer);
                    }
                    if (interested_Peers.size() > 0) {
                        if (interested_Peers.size() <= config_Info.get_Number_Of_Preferred_Neighbors()) {
                            for (Integer peer : interested_Peers) {
                                if(peer_Connections.get(peer).is_Choked()){
                                    peer_Connections.get(peer).unchoke();
                                    peer_Connections.get(peer).send_Message(UNCHOKE);
                                }
                            }
                        } else {
                            Random r = new Random();
                            for (int i = 0; i < config_Info.get_Number_Of_Preferred_Neighbors(); i++) {
                                preferred_Neighbors[i] = (interested_Peers.remove(Math.abs(r.nextInt() % interested_Peers.size())));
                            }
                            for (int peer : preferred_Neighbors) {
                                if(peer_Connections.get(peer).is_Choked()){
                                    peer_Connections.get(peer).unchoke();
                                    peer_Connections.get(peer).send_Message(UNCHOKE);
                                }
                            }
                            //we choke all the remaining interested peers
                            for (Integer peer : interested_Peers) {
                                if(!peer_Connections.get(peer).is_Choked() && !peer_Connections.get(peer).is_Optimistically_Unchoked()){
                                    peer_Connections.get(peer).choke();
                                    peer_Connections.get(peer).send_Message(CHOKE);
                                }
                            }
                        }
                    }
                }


                logs.log_Preferred_Neighbors(thisPeer.get_Peer_ID(), preferred_Neighbors);
                try{
                    Thread.sleep(config_Info.get_Unchoking_Interval() * 1000);
                }
                catch(Exception e){
                }
            }
            try{
                Thread.sleep(5000);
            }
            catch(Exception e){

            }
            System.exit(0);
        }
    }
    private static class Optimistic_UnchokePeer extends Thread{
        @Override
        public void run(){
            while (completed_Peers < peers.size()) {
                //in order to find optimistic unchoke peer we first find out all the interested peers and randomly select a peer and unchoke that peer
                ArrayList<Integer> connections = new ArrayList<>(peer_Connections.keySet());
                ArrayList<Integer> interested = new ArrayList<>();
                for(int connection : connections){
                    if(peer_Connections.get(connection).is_Interested()){
                        interested.add(connection);
                    }
                }
                if(interested.size() > 0){
                    Random r = new Random();
                    int randomNumber = Math.abs(r.nextInt() % interested.size());
                    int connection = interested.get(randomNumber);
                    peer_Connections.get(connection).unchoke();
                    peer_Connections.get(connection).send_Message(UNCHOKE);
                    peer_Connections.get(connection).optimistically_Unchoke();
                    logs.log_Optimistically_Unchoked_Neighbor(thisPeer.get_Peer_ID(), peer_Connections.get(connection).get_Peer_ID());
                    try {
                        Thread.sleep(config_Info.get_Optimistic_Unchoking_Interval() * 1000);
                        peer_Connections.get(connection).optimisticallyChoke();
                    }
                    catch(Exception e){
                    }
                }
            }
            try{
                Thread.sleep(5000);
            }
            catch(Exception e){

            }
            System.exit(0);
        }
    }

    //If our no of peer connections is one less tha  peer size , till then we accept all the incoming requests
    //we take the peerid from the message and add it to our peer list and we send the handshake message to the sender.
    private static class Receive_Connections extends Thread{
        @Override
        public void run(){
            byte[] buffer = new byte[32];
            try{
                ServerSocket server_Socket = new ServerSocket(thisPeer.get_Port_Number());
                while(peer_Connections.size() < peers.size() - 1){
                    Socket connection = server_Socket.accept();
                    DataInputStream data_Input_Stream = new DataInputStream(connection.getInputStream());
                    data_Input_Stream.readFully(buffer);
                    int peer_ID = ByteBuffer.wrap(Arrays.copyOfRange(buffer, 28, 32)).getInt();
                    logs.connectionFrom(host_ID, peer_ID);

                    String hand_shake_Msg = new String(Arrays.copyOfRange(buffer, 0, 28))+  String.valueOf(peer_ID);
                    System.out.println(hand_shake_Msg);
                    peer_Connections.put(peer_ID, new Peer_Connection(connection, peer_ID));

                    //send handshake message to sender
                    DataOutputStream data_Output_Stream = new DataOutputStream(connection.getOutputStream());
                    data_Output_Stream.flush();
                    data_Output_Stream.write(msg.get_Handshake_Message(host_ID));
                }
            }
            catch(Exception e){
            }
        }
    }

    //we send the connection request (handshake message) to all the peers and if we hear back from them we add them to our connections list
    private static class Send_Connections extends Thread{
        @Override
        public void run(){
            byte[] buffer = new byte[32];
            try {
                for (int id : peers.keySet()) {
                    if (id != host_ID)
                    {
                        PeerInfo connPeer = peers.get(id);
                        Socket connection = new Socket(connPeer.get_Host_Name(), connPeer.get_Port_Number());
                        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                        dataOutputStream.flush();
                        dataOutputStream.write(msg.get_Handshake_Message(host_ID));
                        dataOutputStream.flush();
                        DataInputStream dataInputStream = new DataInputStream(connection.getInputStream());
                        dataInputStream.readFully(buffer);
                        int peer_ID = ByteBuffer.wrap(Arrays.copyOfRange(buffer, 28, 32)).getInt();
                        if (peer_ID == id) {
                            logs.connectionTo(host_ID, id);
                            String hand_shake_Msg = new String(Arrays.copyOfRange(buffer, 0, 28)) + String.valueOf(peer_ID);
                            System.out.println(hand_shake_Msg);
                            peer_Connections.put(id, new Peer_Connection(connection, id));
                        } else
                            connection.close();;
                    }
                    else
                        break;

                }
            }
            catch(Exception e){
            }
        }
    }

    //this class stores all the properties required for each of the peer connection information like the connected peerId,
    // and if it is intrested to take from it, it is choked or unchoked, and its download rate
    private static class Peer_Connection{
        private Socket connection;
        private int peer_ID;
        private boolean interested = false;
        private boolean choked = true;
        private boolean optimistically_Unchoked = false;
        private double downloadRate = 0;

        public Peer_Connection(Socket conn, int id){
            connection = conn;
            peer_ID = id;
            (new ReaderThread(this)).start();
        }


        public double get_DownloadRate() {
            return downloadRate;
        }

        public void set_DownloadRate(double rate) {
            this.downloadRate = rate;
        }

        public boolean is_Optimistically_Unchoked() {
            return optimistically_Unchoked;
        }

        public void optimistically_Unchoke() {
            optimistically_Unchoked = true;
        }

        public void optimisticallyChoke(){
            optimistically_Unchoked = false;
        }

        public boolean is_Interested() {
            return interested;
        }

        public void set_Interested() {
            interested = true;
        }
        public void set_NotInterested(){
            interested = false;
        }

        public boolean is_Choked() {
            return choked;
        }

        public void choke() {
            choked = true;
        }
        public void unchoke(){
            choked = false;
        }

        public int get_Peer_ID(){
            return peer_ID;
        }

        public Socket get_Connection() {
            return connection;
        }


        public void send_Message(char type){
            try{
                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.flush();
                switch (type){
                    case CHOKE:
                        dataOutputStream.write(msg.get_Choke_Message());
                        break;
                    case UNCHOKE:
                        dataOutputStream.write(msg.get_Unchoke_Message());
                        break;
                    case INTERESTED:
                        dataOutputStream.write(msg.get_Interested_Message());
                        break;
                    case NOT_INTERESTED:
                        dataOutputStream.write(msg.get_Not_InterestedMessage());
                        break;
                    case BIT_FIELD:
                        dataOutputStream.write(msg.get_Bitfield_Message(thisPeer.get_Bit_field()));
                        break;
                    default:
                        break;
                }
                dataOutputStream.flush();
            }
            catch(Exception e){
            }
        }

        public void send_Message(char type, int index){
            try{
                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.flush();
                switch (type){
                    case HAVE:
                        dataOutputStream.write(msg.get_Have_Message(index));
                        break;
                    case REQUEST:
                        dataOutputStream.write(msg.get_Request_Message(index));
                        break;
                    case PIECE:
                        dataOutputStream.write(msg.get_Piece_Message(index, filePieces[index]));
                        break;
                    default:
                        break;
                }
                dataOutputStream.flush();
            }
            catch(Exception e){
            }
        }

        //We will check if the connected peer has the piece required by us, if yes we mark this peer as interested
        public void compare_Bit_field(int[] thisPeerBit_field, int[] connectedPeerBit_field, int len){
            int i;
            for(i = 0; i < len; i++){
                if(thisPeerBit_field[i] == 0 && connectedPeerBit_field[i] == 1){
                    send_Message(INTERESTED);
                    break;
                }
            }
            if(i == len)
                send_Message(NOT_INTERESTED);
        }

        //Filtering all the required pieces and we request one random piece from it
        public void get_Piece_Index(int[] this_PeerBit_field, int[] connected_PeerBit_field, int len){
            ArrayList<Integer> indices = new ArrayList<>();
            int i;
            for(i = 0; i < len; i++){
                if(this_PeerBit_field[i] == 0 && connected_PeerBit_field[i] == 1){
                    indices.add(i);
                }
            }
            Random r = new Random();
            if(indices.size() > 0){
                int index = indices.get(Math.abs(r.nextInt() % indices.size()));
                send_Message(REQUEST, index);
            }
        }

        //If  we get all the bits we mark as download completed
        public void download_Completed_Check(){
            int counter = 0;
            for(int bit : thisPeer.get_Bit_field()){
                if(bit == 1)
                    counter++;
            }

            if(thisPeer.get_Bit_field().length == counter){
                //if we have complete file we try to merge all the pieces and write to output stream by creating a file
                logs.download_Completed(thisPeer.get_Peer_ID());
                counter = 0;
                byte[] merge = new byte[config_Info.get_File_Size()];
                for(byte[] piece : filePieces){
                    for(byte b : piece){
                        merge[counter] = b;
                        counter++;
                    }
                }
                try {
                    FileOutputStream file = new FileOutputStream(directory.getAbsolutePath() + "/" + config_Info.get_File_Name());
                    BufferedOutputStream bos = new BufferedOutputStream(file);
                    bos.write(merge);
                    bos.close();
                    file.close();
                    System.out.println("File Download Completed.");
                    thisPeer.set_Have_File(1);
                    completed_Peers++;
                } catch (IOException e) {
                }
            }
        }

        //we perform the appropriate behaviour based on the request
        private static class ReaderThread extends Thread{
            private Peer_Connection peer;

            public ReaderThread(Peer_Connection peer){
                this.peer = peer;
            }

            @Override
            public void run(){
                double start_Time;
                double end_Time;
                synchronized (this)
                {
                    try{
                        DataInputStream data_Input_Stream = new DataInputStream(peer.get_Connection().getInputStream());
                        peer.send_Message(BIT_FIELD);
                        while(completed_Peers < peers.size()){
                            int msgLength = data_Input_Stream.readInt();
                            byte[] buffer = new byte[msgLength];
                            start_Time = (double)System.nanoTime() / 100000000;
                            data_Input_Stream.readFully(buffer);
                            end_Time = (double)System.nanoTime() / 100000000;//we store time to get download rate
                            char msgType = (char)buffer[0];
                            byte[] msg = new byte[msgLength - 1];
                            int counter = 0;
                            for(int i = 1; i < msgLength; i++){
                                msg[counter] = buffer[i];
                                counter++;
                            }
                            int index;
                            int bits;
                            switch (msgType){
                                case CHOKE:
                                    logs.choked(thisPeer.get_Peer_ID(), peer.peer_ID);
                                    peer.choke();
                                    break;
                                case UNCHOKE:
                                    peer.unchoke();
                                    logs.unchoked(thisPeer.get_Peer_ID(), peer.peer_ID);
                                    peer.get_Piece_Index(thisPeer.get_Bit_field(), peers.get(peer.peer_ID).get_Bit_field(), thisPeer.get_Bit_field().length);
                                    break;
                                case INTERESTED:
                                    logs.receive_Interested(thisPeer.get_Peer_ID(), peer.peer_ID);
                                    peer.set_Interested();
                                    break;
                                case NOT_INTERESTED:
                                    logs.receive_Not_Interested(thisPeer.get_Peer_ID(), peer.peer_ID);
                                    peer.set_NotInterested();
                                    if(!peer.is_Choked()){
                                        peer.choke();
                                        peer.send_Message(CHOKE);
                                    }
                                    break;
                                case HAVE:
                                    index = ByteBuffer.wrap(msg).getInt();
                                    peers.get(peer.get_Peer_ID()).update_Bit_field(index);
                                    bits = 0;
                                    for(int x : peers.get(peer.get_Peer_ID()).get_Bit_field()){
                                        if(x == 1)
                                            bits++;
                                    }
                                    if(bits == thisPeer.get_Bit_field().length){
                                        peers.get(peer.get_Peer_ID()).set_Have_File(1);
                                        completed_Peers++;
                                    }
                                    peer.compare_Bit_field(thisPeer.get_Bit_field(), peers.get(peer.get_Peer_ID()).get_Bit_field(), thisPeer.get_Bit_field().length);
                                    logs.receive_Have(thisPeer.get_Peer_ID(), peer.get_Peer_ID(), index);
                                    break;
                                case BIT_FIELD:
                                    int[] bit_field = new int[msg.length/4];
                                    counter = 0;
                                    for(int i = 0; i < msg.length; i += 4){
                                        bit_field[counter] = ByteBuffer.wrap(Arrays.copyOfRange(msg, i, i + 4)).getInt();
                                        counter++;
                                    }
                                    peers.get(peer.peer_ID).set_Bit_field(bit_field);
                                    bits = 0;
                                    for(int x : peers.get(peer.get_Peer_ID()).get_Bit_field()){
                                        if(x == 1)
                                            bits++;
                                    }
                                    if(bits == thisPeer.get_Bit_field().length){
                                        peers.get(peer.get_Peer_ID()).set_Have_File(1);
                                        completed_Peers++;
                                    }
                                    else{
                                        peers.get(peer.get_Peer_ID()).set_Have_File(0);
                                    }
                                    peer.compare_Bit_field(thisPeer.get_Bit_field(), bit_field, bit_field.length);
                                    break;
                                case REQUEST:
                                    peer.send_Message(PIECE, ByteBuffer.wrap(msg).getInt());
                                    break;
                                case PIECE:
                                    index = ByteBuffer.wrap(Arrays.copyOfRange(msg, 0, 4)).getInt();
                                    counter = 0;
                                    filePieces[index] = new byte[msg.length - 4];
                                    for(int i = 4; i < msg.length; i++){
                                        filePieces[index][counter] = msg[i];
                                        counter++;
                                    }
                                    thisPeer.update_Bit_field(index);
                                    thisPeer.update_Num_Of_Pieces();
                                    if(!peer.is_Choked()){
                                        peer.get_Piece_Index(thisPeer.get_Bit_field(), peers.get(peer.peer_ID).get_Bit_field(), thisPeer.get_Bit_field().length);
                                    }
                                    double rate = ((double)(msg.length + 5) / (end_Time - start_Time));
                                    if(peers.get(peer.get_Peer_ID()).get_Have_File() == 1){
                                        peer.set_DownloadRate(-1);
                                    }
                                    else{
                                        peer.set_DownloadRate(rate);
                                    }
                                    logs.downloading_Piece(thisPeer.get_Peer_ID(), peer.get_Peer_ID(), index, thisPeer.get_Num_Of_Pieces());
                                    int downloaded = (thisPeer.get_Num_Of_Pieces() * 100) / (int)Math.ceil((double)config_Info.get_File_Size()/config_Info.get_Piece_Size());
                                    String sb = "\r" + "Downloaded: " + downloaded + "%" + " Number of Pieces: " + thisPeer.get_Num_Of_Pieces();
                                    System.out.println(sb);
                                    peer.download_Completed_Check();
                                    for(int connection : peer_Connections.keySet()){
                                        peer_Connections.get(connection).send_Message(HAVE, index);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                        Thread.sleep(5000);
                        //if all the peers completed downloading file we exit the process.
                        System.exit(0);
                    }
                    catch(Exception e){
                    }
                }
            }
        }
    }
}