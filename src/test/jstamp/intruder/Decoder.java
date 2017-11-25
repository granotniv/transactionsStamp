package jstamp.intruder;

/* =============================================================================
 *
 * decoder.java
 *
 * =============================================================================
 *
 * Copyright (C) Stanford University, 2006.  All Rights Reserved.
 * Author: Chi Cao Minh
 *
 * =============================================================================
 *
 * For the license of bayes/sort.h and bayes/sort.c, please see the header
 * of the files.
 * 
 * ------------------------------------------------------------------------
 * 
 * For the license of kmeans, please see kmeans/LICENSE.kmeans
 * 
 * ------------------------------------------------------------------------
 * 
 * For the license of ssca2, please see ssca2/COPYRIGHT
 * 
 * ------------------------------------------------------------------------
 * 
 * For the license of lib/mt19937ar.c and lib/mt19937ar.h, please see the
 * header of the files.
 * 
 * ------------------------------------------------------------------------
 * 
 * For the license of lib/rbtree.h and lib/rbtree.c, please see
 * lib/LEGALNOTICE.rbtree and lib/LICENSE.rbtree
 * 
 * ------------------------------------------------------------------------
 * 
 * Unless otherwise noted, the following license applies to STAMP files:
 * 
 * Copyright (c) 2007, Stanford University
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 * 
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 * 
 *     * Neither the name of Stanford University nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY STANFORD UNIVERSITY ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL STANFORD UNIVERSITY BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 * =============================================================================
 */

import transactionLib.LinkedList;
import transactionLib.Queue;
import transactionLib.TXLibExceptions;

public class Decoder {
	LinkedList fragmentedMapPtr; /* contains list of packet_t* */
	Queue decodedQueuePtr; /* contains decoded_t*  */
	int cnt;

	/* =============================================================================
	 * decoder_alloc
	 * =============================================================================
	 decoder_t* decoder_alloc ();
	  */

	public Decoder() {
		fragmentedMapPtr = new LinkedList();
		decodedQueuePtr = new Queue();
	}

	/* =============================================================================
	 * decoder_free
	 * =============================================================================
	 void decoder_free (decoder_t* decoderPtr);
	 */
	/* =============================================================================
	 * decoder_process
	 * =============================================================================
	 er_t decoder_process (decoder_t* decoderPtr, char* bytes, long numByte);
	 */
	public int process(Packet packetPtr, int numByte) {
		boolean status;
		ERROR er = new ERROR();
		/*
		 * Basic error checking
		 */
		if (numByte < 0) {
			return er.SHORT;
		}
		int flowId = packetPtr.flowId;
		int fragmentId = packetPtr.fragmentId;
		int numFragment = packetPtr.numFragment;
		int length = packetPtr.length;
		if (flowId < 0) {
			return er.FLOWID;
		}
		if ((fragmentId < 0) || (fragmentId >= numFragment)) {
			return er.FRAGMENTID;
		}
		if (length < 0) {
			return er.LENGTH;
		}
		/*
		 * Add to fragmented map for reassembling
		 */
		if (numFragment > 1) {
			LinkedList fragmentListPtr = (LinkedList) fragmentedMapPtr.get(flowId);
			if (fragmentListPtr == null) {
				fragmentListPtr = new LinkedList(); // packet_compareFragmentId
				fragmentListPtr.put(fragmentId,packetPtr);
				fragmentedMapPtr.put(flowId, fragmentListPtr);
			} else {
				//***********************************************************************//
				// Next check was made (as we understand) to make sure that we don't
				// put a fragment in the wrong list (checking that the num of fragments
				// to expect is the same as the num of fragments as written in the first
				// fragment in the list.
				//***********************************************************************//
				//List_Iter it = new List_Iter();
				//it.reset(fragmentListPtr);
				//System.out.print("");
				//Packet firstFragmentPtr = (Packet) it.next(fragmentListPtr);
				//int expectedNumFragment = firstFragmentPtr.numFragment;
				//if (numFragment != expectedNumFragment) {
				//	fragmentedMapPtr.remove(flowId);
				//	return er.NUMFRAGMENT;
				//}
				fragmentListPtr.put(fragmentId,packetPtr);
				/*
				 * If we have all the fragments we can reassemble them
				 */
				int currFragment;
				for (currFragment=0; currFragment<numFragment; currFragment++) {
					if (fragmentListPtr.get(currFragment)==null)
						break;
				}
				if (currFragment==numFragment) {
					int numBytes = 0;
					int i = 0;
					//****************************************************************//
					// Unnecessary check - already checked in previous loop in our
					// implementation
					//****************************************************************//
					//it.reset(fragmentListPtr);
//					while (it.hasNext(fragmentListPtr)) {
//						Packet fragmentPtr = (Packet) it.next(fragmentListPtr);
//						if (fragmentPtr.fragmentId != i) {
//							fragmentedMapPtr.remove(flowId);
//							return er.INCOMPLETE; /* should be sequential */
//						}
					Packet fragmentPtr = null;
					for (currFragment=0; currFragment<numFragment; currFragment++) {
						fragmentPtr = (Packet) fragmentListPtr.get(currFragment);
						numBytes = numBytes + fragmentPtr.length;
						//i++;
					}
					byte[] data = new byte[numBytes];
					//it.reset(fragmentListPtr);
					fragmentPtr=null;
					int index = 0;
					//while (it.hasNext(fragmentListPtr)) {
					for (currFragment=0; currFragment<numFragment; currFragment++) {
						fragmentPtr = (Packet) fragmentListPtr.get(currFragment);
						for (i = 0; i < fragmentPtr.length; i++) {
							data[index++] = fragmentPtr.data[i];
						}
					}
					Decoded decodedPtr = new Decoded();

					decodedPtr.flowId = flowId;
					decodedPtr.data = data;
					decodedQueuePtr.enqueue(decodedPtr);
					fragmentedMapPtr.remove(flowId);
				}
			}
		} else {
			/*
			 * This is the only fragment, so it is ready
			 */
			if (fragmentId != 0) {
				return er.FRAGMENTID;
			}
			byte[] data = packetPtr.data;
			Decoded decodedPtr = new Decoded();

			decodedPtr.flowId = flowId;
			decodedPtr.data = data;
			decodedQueuePtr.enqueue(decodedPtr);
		}
		return er.NONE;
	}

	/* =============================================================================
	 * TMdecoder_process
	 * =============================================================================
	 er_t TMdecoder_process (TM_ARGDECL  decoder_t* decoderPtr, char* bytes, long numByte);
	 */
	/* =============================================================================
	 * decoder_getComplete
	 * -- If none, returns NULL
	 * =============================================================================
	 char* decoder_getComplete (decoder_t* decoderPtr, long* decodedFlowIdPtr); */
	public byte[] getComplete(int[] decodedFlowId) throws TXLibExceptions.QueueIsEmptyException {
		byte[] data = null;
			Decoded decodedPtr = (Decoded) decodedQueuePtr.dequeue();
			if (decodedPtr != null) {

				decodedFlowId[0] = decodedPtr.flowId;
				data = decodedPtr.data;
			} else {
				decodedFlowId[0] = -1;
				data = null;
			}
		return data;
	}
}
/* =============================================================================
 * TMdecoder_getComplete
 * -- If none, returns NULL
 * =============================================================================
 char* TMdecoder_getComplete (TM_ARGDECL  decoder_t* decoderPtr, long* decodedFlowIdPtr);
 */
/* =============================================================================
 *
 * End of decoder.java
 *
 * =============================================================================
 */
