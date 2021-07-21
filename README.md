CNT5160C COMPUTER NETWORKS			
Peer to Peer File Sharing 


Project Overview :
In this project we are showing peer to peer file sharing software similar to bit torrent protocol in JAVA. 
BitTorrent is a popular P2P protocol for file distribution. Among its interesting features, you are asked to implement the choking-unchoking mechanism which is one of the most important features of BitTorrent. In the following Protocol Description section, you can read the protocol description, which has been modified a little bit from the original BitTorrent protocol. After reading the protocol description carefully, you must follow the implementation specifics shown in the Implementation Specifics section.

File Structure:
1)	Logs.java
      It contains the logs class which handles all the log messages.
2)	Messages.java
    It handles to build the message to send to peers for all the operations (like choke, unchoke, interested, not interested, have, bitfield, request, piece, handshake)

Methods :
public byte[] get_Choke_Message()
public byte[] get_Unchoke_Message()
public byte[] get_Interested_Message()
public byte[] get_Not_InterestedMessage()
public byte[] get_Have_Message()
public byte[] get_Bitfield_Message()
public byte[] get_Request_Message()
public byte[] get_Piece_Message()
public byte[] get_Handshake_Message()

3) CommonInfo.java
      CommonInfo class handles the information retrieved from Config.cfg
Methods:
public int get_Number_Of_Preferred_Neighbors()
public void set_Number_Of_Preferred_Neighbors()
public int get_Unchoking_Interval()
public void set_Unchoking_Interval()
public int get_Optimistic_Unchoking_Interval()
public void set_Optimistic_Unchoking_Interval()
public String get_File_Name()
public void set_File_Name()
public int get_File_Size()
public void set_File_Size()
public int get_Piece_Size()
public void set_Piece_Size()


4)	peerProcess.java
     	It contains different classes to handle the file transfer process.


Methods:
static void get_Peer_Info_File_Information() 
static void get_Common_File_Information() 
static void set_Bit_FieldInformation()
send_Connections.start();
receive_Connections.start();
unchoke_Peers.start();
optimistic_UnchokePeer.start();


Private Classes:

•	private static class Unchoke_Peers
         In this class we unchoke the neighbours who has the pieces which we need and has highest download rate if we dont have the complete file.
if we get more than no.of preferred neighbours we choose the ones with greater download rate

•	private static class Optimistic_UnchokePeer 
    In this class we find the optimistic unchoke peer. In order to find optimistic unchoke peer we first find out all the interested peers and randomly select a peer and unchoke that peer

•	private static class Receive_Connections
  In this class we will read all the incoming handshake messages to connect with peers and makes the connection by sending the handshake messages as response.
         If no of peer connections is one less than peer size, till then we accept all the incoming requests
         we take the peer id from the message and add it to our peer list and we send the handshake message to the sender.

•	private static class Send_Connections
          In this class, we try making connections with peers by sending the handshake messages.
 we send the connection request (handshake message) to all the peers and if we hear back from them, we add them to our connections list

•	private static class Peer_Connection
	This class stores all the properties required for each of the peer connection information like the connected peerId, and if it is interested to take from it, it is choked or unchoked, and its download rate


Methods:

public void send_Message()

public void compare_Bit_field() - We will check if the connected peer has the piece required by us, if yes we mark this peer as interested
 
public void get_Piece_Index() - Filtering all the required pieces and we request one random piece from it

public void download_Completed_Check()- If  we get all the bits we mark as download completed

private static class ReaderThread - we perform the appropriate behaviour based on the request
   

PeerProcess:
We take input arguments as peer_id and we check for peer_id directory if not present we will create a directory for it.
Later we get peers information from peer.config file.
 Peer.config  contains information about peers id, hostname, port number and information regarding if it having file or not and we parse the data and store in memory.
Later we get the common file information which contains information regarding :  NumberOfPreferredNeighbors, UnchokingInterval ,OptimisticUnchokingInterval , FileName, FileSize, PieceSize. And we parse the data and store in memory.
Based on file owner or not, we set the bits [ for owner we set the bits as 1 which indicates it that chunk else 0] 
Later we call the threads to send the pieces, request the pieces, to unchoke the peers, to optimistic unchoke peer. 






