import java.nio.ByteBuffer;

class Messages{
    private static final char CHOKE = '0';
    private static final char UNCHOKE = '1';
    private static final char INTERESTED = '2';
    private static final char NOT_INTERESTED = '3';
    private static final char HAVE = '4';
    private static final char BITFIELD = '5';
    private static final char REQUEST = '6';
    private static final char PIECE = '7';

    public byte[] build_Message_Without_Payload(int len, char type){
        byte[] msg;
        byte[] lengthBytes;
        byte msgType = (byte)type;
        int index;

        msg = new byte[len + 4];
        lengthBytes = ByteBuffer.allocate(4).putInt(len).array();
        index = 0;
        for(byte i : lengthBytes) {
            msg[index] = i;
            index++;
        }
        msg[index] = msgType;
        return msg;

    }
    public byte[] build_Message_With_Payload(int len, char type, byte[] payload) {
        byte[] msg;
        byte[] lengthBytes;
        byte msgType = (byte) type;
        int index;

        msg = new byte[len + 4];
        lengthBytes = ByteBuffer.allocate(4).putInt(len).array();
        index = 0;
        for (byte i : lengthBytes) {
            msg[index] = i;
            index++;
        }
        msg[index++] = msgType;
        for (byte i : payload) {
            msg[index] = i;
            index++;
        }
        return msg;

    }
    //choke, unchoke, interested and not interested messages have no payload.
    //Length of these 4 messages is 1 bit as it has no payload and only type of message information
    public byte[] get_Choke_Message(){
        return build_Message_Without_Payload(1, CHOKE);
    }

    public byte[] get_Unchoke_Message(){
        return build_Message_Without_Payload(1, UNCHOKE);
    }

    public byte[] get_Interested_Message(){
        return build_Message_Without_Payload(1, INTERESTED);
    }

    public byte[] get_Not_InterestedMessage(){
        return build_Message_Without_Payload(1, NOT_INTERESTED);
    }

    //have messages have a payload that contains a 4-byte piece index field.
    //Length of Have message is 5 [pieceIndex length(4) + msg type (have =1) ]
    public byte[] get_Have_Message(int pieceIndex){
        byte[] payload = ByteBuffer.allocate(4).putInt(pieceIndex).array();
        return build_Message_With_Payload(5, HAVE, payload);
    }

    //bitfield messages have a bitfield as its payload, each byte says if corresponding bit is present in it
    //Length of bitfield message is one less than 4 times the input bit field
    public byte[] get_Bitfield_Message(int[] bitfield){
        int len = 1 + (4 * bitfield.length);
        byte[] payload = new byte[len - 1];
        int index = 0;
        for(int bit : bitfield){
            byte[] bitBytes = ByteBuffer.allocate(4).putInt(bit).array();
            for(byte b : bitBytes){
                payload[index] = b;
                index++;
            }
        }
        return build_Message_With_Payload(len, BITFIELD, payload);
    }

    //request messages have a payload which consists of a 4-byte piece index field
    //Length of request message is 5 [pieceIndex length(4) + msg type (request =1) ]
    public byte[] get_Request_Message(int pieceIndex){
        byte[] payload = ByteBuffer.allocate(4).putInt(pieceIndex).array();
        return build_Message_With_Payload(5, REQUEST, payload);
    }

    //piece messages have a payload which consists of a 4-byte piece index field and the content of the piece.
    //Length of piece message is pieceIndex length(4) + piece length
    public byte[] get_Piece_Message(int pieceIndex, byte[] piece){
        byte[] payload = new byte[4 + piece.length];
        int index = 0;
        byte[] indexBytes = ByteBuffer.allocate(4).putInt(pieceIndex).array();
        for(byte bit : indexBytes){
            payload[index] = bit;
            index++;
        }
        for(byte bit : piece){
            payload[index] = bit;
            index++;
        }
        return build_Message_With_Payload((5 + piece.length), PIECE, payload);
    }

    public byte[] get_Handshake_Message(int peerID){
        byte[] message = new byte[32];
        byte[] header = "P2PFILESHARINGPROJ".getBytes();//18 bits
        byte[] zerobits = "0000000000".getBytes();//10 bits
        byte[] peerIdBits = ByteBuffer.allocate(4).putInt(peerID).array();//peerId 4 bits
        int index = 0;
        for(byte b : header){
            message[index] = b;
            index++;
        }
        for(byte b : zerobits){
            message[index] = b;
            index++;
        }
        for(byte b : peerIdBits){
            message[index] = b;
            index++;
        }
        return message;
    }
}