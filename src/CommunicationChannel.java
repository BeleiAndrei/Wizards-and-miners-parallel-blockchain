import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * Class that implements the channel used by wizards and miners to communicate.
 */
public class CommunicationChannel {
	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
	//Message minerMessage;
	//Message wizardMessage;
	BlockingQueue<Message> minerMessage = new LinkedBlockingQueue<Message>();
	BlockingQueue<Message> wizardMessage = new LinkedBlockingQueue<Message>();
	static ConcurrentHashMap<String, Integer> rooms = new ConcurrentHashMap<String, Integer>();
	public static Semaphore readSemaphore = new Semaphore(4);
	class Entry {
		Long id;
		Integer parent;
		
		public Entry (Long id, Integer parent) {
			this.id = id;
			this.parent = parent;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Entry)) { 
	            return false; 
	        } else return id == ((Entry)o).id;
		}
		
	}
	Vector<Entry> parents = new Vector<Entry>(10, 10);

	public CommunicationChannel() {
	}

	/**
	 * Puts a message on the miner channel (i.e., where miners write to and wizards
	 * read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	
	public boolean wizHasMes() {
		return wizardMessage.size() != 0;
	}
	
	public void putMessageMinerChannel(Message message) {
		if (message.getCurrentRoom() == -1) {
		    System.out.println("Miner " + Thread.currentThread().getId() + " wanted to send room " + message.getData());
		    System.exit(1);
		}
		minerMessage.add(message);
	}

	/**
	 * Gets a message from the miner channel (i.e., where miners write to and
	 * wizards read from).
	 * 
	 * @return message from the miner channel
	 */
	public Message getMessageMinerChannel() {
		try {
			return minerMessage.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Puts a message on the wizard channel (i.e., where wizards write to and miners
	 * read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageWizardChannel(Message message) {
		
		if (message != null && (message.getData() == Wizard.END || message.getData() == "NO_PARENT" || message.getData() == Wizard.EXIT || rooms.putIfAbsent(message.getData(), message.getCurrentRoom()) == null)) {
			System.out.println("Wizard " + Thread.currentThread().getId() + " sends  " + " " + message.getParentRoom() + " " + message.getCurrentRoom() + " " + message.getData());		
			int index = parents.indexOf(new Entry(Thread.currentThread().getId(), 0));
			if (index == -1) {
				parents.add(new Entry(Thread.currentThread().getId(), message.getCurrentRoom()));
			} else if (message.getData() == "NO_PARENT") {
				parents.set(index, new Entry(Thread.currentThread().getId(), message.getCurrentRoom()));
			} else {
				wizardMessage.add(new Message(parents.elementAt(index).parent, message.getCurrentRoom(), message.getData()));
			}
		}
	}

	/**
	 * Gets a message from the wizard channel (i.e., where wizards write to and
	 * miners read from).
	 * 
	 * @return message from the miner channel
	 */
	public Message getMessageWizardChannel() {
		try {
			return wizardMessage.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
