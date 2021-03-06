package projections.Tools.Timeline;

import projections.misc.MiscUtil;


public class TimelineMessage implements Comparable
{
	/** Message send time */
	protected long Time;
	
	protected int Entry;
	
	/** Message Length */
	protected int MsgLen;
	
	protected int EventID;
	
//	public int SenderEventID;

	private int destPEs[];
	private int numPEs;

	private int srcPE;

	/** A messages sent from srcPE, with eventid EventID */

	/** Single message constructor */
	public TimelineMessage(int srcPE, long t,int e,int mlen,int EventID) {
		this(srcPE, t, e, mlen, EventID, null);
	}

	/** Multicast Constructor */
	public TimelineMessage(int srcPE, long t, int e, int mlen, int EventID, int destPEs[]) {
		this.srcPE = srcPE;
		Time=t;
		Entry=e;
		MsgLen=mlen;
		this.EventID = EventID;
		if (destPEs != null) {
			this.numPEs = destPEs.length;
		} else {
			this.numPEs = 0;
		}
		this.destPEs = destPEs;
	}

	/** Broadcast Constructor */
	public TimelineMessage(int srcPE, long t, int e, int mlen, int EventID, int numPEs) {
		Time=t;
		this.srcPE = srcPE;
		Entry=e;
		MsgLen=mlen;
		this.EventID = EventID;
		this.numPEs = numPEs;
		this.destPEs = null;
	}

	/** compare two timeline messages based on their source pe and their EventID */
	@SuppressWarnings("ucd")
	public int compareTo(Object o) {
		TimelineMessage other = (TimelineMessage)o;

		if(srcPE == other.srcPE){
			return MiscUtil.sign(EventID - other.EventID);
		} else {
			return MiscUtil.sign(srcPE-other.srcPE);
		}
	}
	
	protected String destination(int totalPE){
		if(isMulticast()){
			String ds = "";
			for(int i=0;i<numPEs;i++){
				ds = ds + destPEs[i];
				if(i<numPEs-1)
					ds = ds + ",";
			}
			return "Multicast to " +numPEs + " PEs: " + ds;
		}
		else if(isBroadcast()) {

			if(numPEs == totalPE){
				return "Group Broadcast"; 
			} else {
				return "NodeGroup Broadcast"; 
			}
			
		} else {
			return "Unicast to unknown";
		}
				
	}

	public boolean isBroadcast(){
		return (numPEs>0 && destPEs == null);
	}

	public boolean isMulticast(){
		return (numPEs>0 && destPEs != null);
	}

	public boolean isUnicast(){
		return (numPEs==0);
	}

	protected void shiftTimesBy(long shift) {
		Time += shift;
	}
	
	
}
