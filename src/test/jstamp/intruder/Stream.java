package jstamp.intruder;
import transactionLib.LinkedList;
import transactionLib.Queue;
import transactionLib.TXLibExceptions;

public class Stream {
	int percentAttack;
	Random randomPtr;
	Vector_t allocVectorPtr;
	Queue packetQueuePtr;
	RBTree attackMapPtr;

	public Stream(int percentAttack) {
		this.percentAttack = percentAttack;
		randomPtr = new Random();
		allocVectorPtr = new Vector_t(1);
		packetQueuePtr = new Queue();
		attackMapPtr = new RBTree(0);
	}

	/* splintIntoPackets
	 * -- Packets will be equal-size chunks except for last one, which will have
	 *    all extra bytes
	 */
	private void splitIntoPackets(byte[] str, int flowId, Random randomPtr,
			Vector_t allocVectorPtr, Queue packetQueuePtr) {
		int numByte = str.length;
		int numPacket = randomPtr.random_generate() % numByte + 1;
		int numDataByte = numByte / numPacket;
		int i;
		int p;
		boolean status;
		int beginIndex = 0;
		int endIndex;
		int z;
		for (p = 0; p < (numPacket - 1); p++) {
			Packet bytes = new Packet(numDataByte);
			status = allocVectorPtr.vector_pushBack(bytes);
			bytes.flowId = flowId;
			bytes.fragmentId = p;
			bytes.numFragment = numPacket;
			bytes.length = numDataByte;
			endIndex = beginIndex + numDataByte;
			for (i = beginIndex, z = 0; i < endIndex; z++, i++) {
				bytes.data[z] = str[i];
			}
			packetQueuePtr.enqueue(bytes);
			beginIndex = endIndex;
		}
		int lastNumDataByte = numDataByte + numByte % numPacket;
		Packet bytes = new Packet(lastNumDataByte);
		bytes.flowId = flowId;
		bytes.fragmentId = p;
		bytes.numFragment = numPacket;
		bytes.length = lastNumDataByte;
		endIndex = numByte;
		for (i = beginIndex, z = 0; i < endIndex; z++, i++) {
			bytes.data[z] = str[i];
		}
		packetQueuePtr.enqueue(bytes);
	}

	/*==================================================
	/* stream_generate 
	 * -- Returns number of attacks generated
	/*==================================================*/
	public int generate(Dictionary dictionaryPtr, int numFlow, int seed,
			int maxLength) {
		int numAttack = 0;
		ERROR error = new ERROR();
		Detector detectorPtr = new Detector();
		detectorPtr.addPreprocessor(2); // preprocessor_toLower
		randomPtr.random_seed(seed);
		while (!packetQueuePtr.isEmpty()) {
			try {
				packetQueuePtr.dequeue();
			}
			catch (TXLibExceptions.QueueIsEmptyException e) {
				System.out.println(e.getMessage());
			}
			catch (TXLibExceptions.AbortException e) {
				System.out.println(e.getMessage());
			}
		}
		int range = '~' - ' ' + 1;
		int f;
		boolean status;
		for (f = 1; f <= numFlow; f++) {
			byte[] c;
			if ((randomPtr.random_generate() % 100) < percentAttack) {
				int s = randomPtr.random_generate()
						% dictionaryPtr.global_numDefaultSignature;
				String str = dictionaryPtr.get(s);
				c = str.getBytes();
				status = attackMapPtr.insert(f, c);
				numAttack++;
			} else {
				/* Create random string */
				int length = (randomPtr.random_generate() % maxLength) + 1;
				int l;
				c = new byte[length + 1];
				for (l = 0; l < length; l++) {
					c[l] = (byte) (' ' + (byte) (randomPtr.random_generate() % range));
				}
				status = allocVectorPtr.vector_pushBack(c);
				int err = detectorPtr.process(c);
				if (err == error.SIGNATURE) {
					status = attackMapPtr.insert(f, c);
					System.out.println("Never here");
					numAttack++;
				}
			}
			splitIntoPackets(c, f, randomPtr, allocVectorPtr, packetQueuePtr);
		}
		queue_shuffle(packetQueuePtr);
		return numAttack;
	}

	void queue_shuffle (Queue packetQueuePtr) {
		LinkedList dequeuedNodes;
		dequeuedNodes = new LinkedList();
		int count=0;
		int i=0;
		while (!packetQueuePtr.isEmpty()) {
			try {
				dequeuedNodes.put(count, packetQueuePtr.dequeue());
			}
			catch (TXLibExceptions.QueueIsEmptyException e) {
				System.out.println(e.getMessage());
			}
			catch (TXLibExceptions.AbortException e) {
				System.out.println(e.getMessage());
			}
			count++;
		}
		for (i = 0; i < count; i++) {
			int r1 = (int) (randomPtr.random_generate() % count);
			int r2 = (int) (randomPtr.random_generate() % count);
			Object tmp = dequeuedNodes.get(r1);
			dequeuedNodes.put(r1,dequeuedNodes.get(r2));
			dequeuedNodes.put(r2,tmp);
		}
		for (i=0; i<count; i++) {
			packetQueuePtr.enqueue(dequeuedNodes.get(i));
		}
	}

	/*========================================================
	 * stream_getPacket
	 * -- If none, returns null
	 *  ======================================================
	 */
	Packet getPacket() throws TXLibExceptions.QueueIsEmptyException {
			return (Packet) packetQueuePtr.dequeue();
	}

	/* =======================================================
	 * stream_isAttack
	 * =======================================================
	 */
	boolean isAttack(int flowId) {
		return attackMapPtr.contains(flowId);
	}
}
