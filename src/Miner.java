import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Semaphore;

/**
 * Class for a miner.
 */
public class Miner extends Thread {
	/**
	 * Creates a {@code Miner} object.
	 * 
	 * @param hashCount
	 *            number of times that a miner repeats the hash operation when
	 *            solving a puzzle.
	 * @param solved
	 *            set containing the IDs of the solved rooms
	 * @param channel
	 *            communication channel between the miners and the wizards
	 */
	Integer hashCount;
	Set<Integer> solved;
	CommunicationChannel channel;
	Set<String> to_solve;
	Message mess;
	int currentRoom;
	int parentRoom;
	String messData;

	public Miner(Integer hashCount, Set<Integer> solved, CommunicationChannel channel) {
		this.hashCount = hashCount;
		this.solved = solved;
		this.channel = channel;
	}
	
    private static String encryptThisString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // convert to string
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
            String hex = Integer.toHexString(0xff & messageDigest[i]);
            if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
    
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static String encryptMultipleTimes(String input, Integer count) {
        String hashed = input;
        for (int i = 0; i < count; ++i) {
            hashed = encryptThisString(hashed);
        }

        return hashed;
    }

	@Override
	public void run() {

		while(true) {
			
			if(!channel.wizHasMes()) {
				continue;
			} else {
				mess = channel.getMessageWizardChannel();
				currentRoom = mess.getCurrentRoom();
				parentRoom = mess.getParentRoom();
				messData = mess.getData();
				if (messData == Wizard.EXIT) {
					return;
				}
				if (mess.getData() != "END") {
					System.out.println("Miner " + Thread.currentThread().getId() + " receives " + mess.getParentRoom() + " " + mess.getCurrentRoom() + " " + mess.getData());
					String ret = encryptMultipleTimes(mess.getData(), hashCount);
					Message reply = new Message(parentRoom, currentRoom, ret);
					System.out.println("Miner " + Thread.currentThread().getId() + " sends " + reply.getCurrentRoom() + " " + reply.getParentRoom() + " " + reply.getData());
					channel.putMessageMinerChannel(reply);
				}
			}		
		}

	}
}
