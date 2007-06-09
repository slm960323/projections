package projections.analysis;

import java.io.*;
import java.util.*;
import projections.misc.*;

/** 
 *  StsReader
 *  Modified by Chee Wai Lee.
 *  4/7/2003
 *  10/26/2005 - moved the functionality for various pre-processing 
 *               analysis of data files to Analysis.java
 *
 *  StsReader provides the necessary abstractions to reason about a
 *  projections .sts file and its associated data files (eg. .log, .sum
 *  .sumd etc).
 * 
 */
public class StsReader extends ProjDefs
{
    private boolean hasPAPI = false;
	
    // Sts header information
    private double version;
    private String Machine;
    private int NumPe;
    private int TotalChares;
    private int EntryCount;
    private int TotalMsgs;

    // Sts data
    private String ClassNames[];    // indexed by chare id
    private Chare ChareList[];
    private long MsgTable[];        // indexed by msg id
    // indexed by (ep id) X (type) where type == 0 -> entry name
    //                               and type == 1 -> class (chare) name
    private String EntryNames[][];  
    
    // User Event information
    private int userEventIndex = 0;
    private Hashtable userEventIndices = new Hashtable();
    // index by Integer, return String name
    private Hashtable userEvents = new Hashtable();  
    private String userEventNames[];

    // AMPI Functions tracing
    private int functionEventIndex = 0;
    private Hashtable functionEventIndices = new Hashtable();
    private Hashtable functionEvents = new Hashtable();
    private String functionEventNames[];
    
    private int numPapiEvents;
    private String papiEventNames[];

    /**
     *  Basically a hack to allow multirun tool to bypass the 
     *  ActivityManager and it's use of Analysis. Hence this
     *  wrapper is used for normal tools.
     */
    public StsReader(String FileName) 
	throws LogLoadException
    {
	this(FileName, false);
    }

    /** 
     *  The StsReader constructor reads the .sts file indicated.
     *  @exception LogLoadException if an error occurs while reading in the
     *      the state file
     *  Pre-condition: FileName is the full pathname of the sts file.
     */
    public StsReader(String FileName, boolean isMultirun) 
	throws LogLoadException   
    {
	try {
	    BufferedReader InFile = 
		new BufferedReader(new InputStreamReader(new FileInputStream(FileName)));
	    int ID,ChareID,MsgID;
	    String Line,Name;
	    while ((Line = InFile.readLine()) != null) {
		StringTokenizer st = new StringTokenizer(Line);
		String s1 = st.nextToken();
		if (s1.equals("VERSION")) {
		    version = Double.parseDouble(st.nextToken());
		} else if (s1.equals("MACHINE")) {
		    Machine = st.nextToken();
		} else if (s1.equals("PROCESSORS")) {
		    NumPe = Integer.parseInt(st.nextToken());
		} else if (s1.equals("TOTAL_CHARES")) {
		    TotalChares = Integer.parseInt(st.nextToken());
		    ChareList   = new Chare[TotalChares];
		    ClassNames  = new String[TotalChares];
		} else if (s1.equals("TOTAL_EPS")) {
		    EntryCount   = Integer.parseInt(st.nextToken());
		    EntryNames   = new String[EntryCount][2];
		} else if (s1.equals("TOTAL_MSGS")) {
		    TotalMsgs = Integer.parseInt(st.nextToken());
		    MsgTable  = new long[TotalMsgs];
		} else if (s1.equals("CHARE") || Line.equals("BOC")) {
		    ID = Integer.parseInt(st.nextToken());
		    ChareList[ID]            = new Chare();
		    ChareList[ID].ChareID    = ID;
		    ChareList[ID].NumEntries = 0;
		    ChareList[ID].Name       = st.nextToken();
		    ChareList[ID].Type       = new String(s1);
		    ClassNames[ID]      = ChareList[ID].Name;
		} else if (s1.equals("ENTRY")) {
			String Type    = st.nextToken();
		    ID      = Integer.parseInt(st.nextToken());
		    StringBuffer nameBuf=new StringBuffer(st.nextToken());
		    Name = nameBuf.toString();
		    if (-1!=Name.indexOf('(') && -1==Name.indexOf(')')) {
			//Parse strings until we find the close-paren
			while (true) {
			    String tmp=st.nextToken();
			    nameBuf.append(" ");
			    nameBuf.append(tmp);
			    if (tmp.endsWith(")"))
				break;
			}
		    }
		    Name    = nameBuf.toString();
		    ChareID = Integer.parseInt(st.nextToken());
		    MsgID   = Integer.parseInt(st.nextToken());
		    
		    EntryNames[ID][0] = Name;
		    EntryNames[ID][1] = ClassNames [ChareID];
		} else if (s1.equals("MESSAGE")) {
		    ID  = Integer.parseInt(st.nextToken());
		    int Size  = Integer.parseInt(st.nextToken());
		    MsgTable[ID] = Size;
		} else if (s1.equals("FUNCTION")) {
		    Integer key = new Integer(st.nextToken());
		    if (!functionEvents.containsKey(key)) {
			// Allow the presence of spaces in the descriptor.
			String functionEventName = "";
			while (st.hasMoreTokens()) {
			    functionEventName += st.nextToken() + " ";
			}
			functionEvents.put(key, functionEventName);
			functionEventNames[functionEventIndex] = 
			    functionEventName;
			functionEventIndices.put(key,
						 new Integer(functionEventIndex));
		    }
		    functionEventIndex++;
		} else if (s1.equals("EVENT")) {
		    Integer key = new Integer(st.nextToken());
		    if (!userEvents.containsKey(key)) {
			String eventName = "";
			while (st.hasMoreTokens()) {
			    eventName = eventName + st.nextToken() + " ";
			}
			userEvents.put(key, eventName);
			userEventNames[userEventIndex] = eventName;
			userEventIndices.put(key, 
					     new Integer(userEventIndex++));
		    }
		} else if (s1.equals("TOTAL_EVENTS")) {
		    // restored by Chee Wai - 7/29/2002
		    userEventNames = 
			new String[Integer.parseInt(st.nextToken())];
		} else if (s1.equals("TOTAL_FUNCTIONS")) {
		    functionEventNames = 
			new String[Integer.parseInt(st.nextToken())];
		} else if (s1.equals ("TOTAL_PAPI_EVENTS")) {
		    hasPAPI = true;
		    numPapiEvents = Integer.parseInt(st.nextToken());
		    papiEventNames =
			new String[numPapiEvents];
		} else if (s1.equals ("PAPI_EVENT")) {
		    hasPAPI = true;
		    papiEventNames[Integer.parseInt(st.nextToken())] =
			st.nextToken();
		} else if (s1.equals ("END")) {
		    break;
		}
	    }
		
	    InFile.close();
	} catch (FileNotFoundException e) {
	    throw new LogLoadException (FileName, LogLoadException.OPEN);
	} catch (IOException e) {
	    throw new LogLoadException (FileName, LogLoadException.READ);
	}
    }

    /** ************** Private working/util Methods ************** */
    
    /** ****************** Accessor Methods ******************* */

    // *** Data accessors ***
    public double getVersion() {
	return version;
    }

    public int getEntryCount() { 
	return EntryCount;
    }   
    
    public int getProcessorCount() {
	return NumPe;
    }   

    public String getMachineName() {
	return Machine;
    }
    
    /** 
     * Gives the user entry points as read in from the .sts file as an 
     * array of two strings:  one for the name of the entry point with 
     * BOC or CHARE prepended to the front and a second containing the 
     * name of its parent BOC or chare.
     * @return a two-dimensional array of Strings containing these records
     */
    public String[][] getEntryNames() {
	return EntryNames;
    }   
    
    // *** user event accessors ***
    public int getNumUserDefinedEvents() {
	return userEvents.size();
    }

    public int getUserEventIndex(int eventID) {
	Integer key = new Integer(eventID);
	return ((Integer)userEventIndices.get(key)).intValue();
    }

    public String getUserEventName(int eventID) { 
	Integer key = new Integer(eventID);
	return (String) userEvents.get(key);
    }

    public String[] getUserEventNames() {
	// gets an array by logical (not user-given) index
	return userEventNames;
    }

    // *** function event accessors ***
    public int getNumFunctionEvents() {
	// **FIXME**
	// ** create a new function called getFunctionArraySize **
	// ** getNumFunctionEvents does not semantically mean the same thing!
	//
	// the +1 is a silly hack because function id counts does not begin
	// from 0. No easy solution is visible in the near-future.
	if (functionEvents.size() == 0) {
	    return 0;
	} else {
	    return functionEvents.size()+1;
	}
    }

    public int getFunctionEventIndex(int eventID) {
	Integer key = new Integer(eventID);
	return ((Integer)functionEventIndices.get(key)).intValue();
    }

    public String getFunctionEventDescriptor(int eventID) {
	Integer key = new Integer(eventID);
	return (String)functionEvents.get(key);
    }

    public String[] getFunctionEventDescriptors() {
	return functionEventNames;
    }

    public int getNumPerfCounts() {
	if (hasPAPI) {
	    return numPapiEvents;
	} else {
	    return 0;
	}
    }

    public String[] getPerfCountNames() {
	if (hasPAPI) {
	    return papiEventNames;
	} else {
	    return null;
	}
    }

    public boolean hasPapi() {
	return hasPAPI;
    }
}

