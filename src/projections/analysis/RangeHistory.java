package projections.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Vector;

import projections.gui.U;

public class RangeHistory
{
    public static final int MAX_ENTRIES = 10;

    private String filename;
    private int numEntries;
    private Vector rangeSet;

    private Vector historyStringVector;

    public RangeHistory(String logDirectory) 
    {
	this.filename = logDirectory + "ranges.hst";
	if (!(new File(this.filename)).exists()) {
	    rangeSet = new Vector();
	    historyStringVector = new Vector();
	} else {
	    try {
		loadRanges();
		historyStringVector = new Vector();
		for (int i=0; i<rangeSet.size()/2; i++) {
		    String historyString = 
			U.humanReadableString(((Long)rangeSet.elementAt(i*2)).longValue()) + 
			" to " + 
			U.humanReadableString(((Long)rangeSet.elementAt(i*2+1)).longValue());
		    historyStringVector.add(historyString);
		}
	    } catch (IOException e) {
		System.err.println("Error: " + e.toString());
	    }
	}
    }

    private void loadRanges() 
	throws IOException
    {
	rangeSet = new Vector();
	String line;
	StringTokenizer st;
	BufferedReader reader = 
	    new BufferedReader(new FileReader(filename));

	numEntries = 0;
	
	while ((line = reader.readLine()) != null) {
	    st = new StringTokenizer(line);
	    String s1 = st.nextToken();
	    if (s1.equals("ENTRY")) {
		if (numEntries >= MAX_ENTRIES) {
		    throw new IOException("Range history overflow!");
		}
		rangeSet.add(Long.valueOf(st.nextToken()));
		rangeSet.add(Long.valueOf(st.nextToken()));
		numEntries++;
	    }
	}
	reader.close();
    }

    public void save() 
	throws IOException
    {
	PrintWriter writer =
	    new PrintWriter(new FileWriter(filename), true);

	// Data and File is stored in proper order (latest on top)
	for (int i=0; i<numEntries; i++) {
	    writer.print("ENTRY ");
	    writer.print(((Long)rangeSet.elementAt(i*2)).longValue());
	    writer.print(" ");
	    writer.println(((Long)rangeSet.elementAt(i*2+1)).longValue());
	}
    }

    // adds a new entry and displaces any old entry beyond MAX_ENTRIES
    public void add(long start, long end) {
	if (numEntries == MAX_ENTRIES) {
	    // history is full, take off the rear entry.
	    rangeSet.remove(MAX_ENTRIES);
	    rangeSet.remove(MAX_ENTRIES-1);
	    numEntries--;
	}
	// added in reverse order starting from the front of the vector.
	rangeSet.add(0, new Long(end));
	rangeSet.add(0, new Long(start));
	numEntries++;
    }

    public void remove(int index) {
	if ((index < 0) ||
	    (index >= numEntries)) {
	    System.err.println("Internal Error: Attempt to remove " +
			       "invalid index " + 
			       index + ". Max number of " +
			       "histories is " + numEntries +
			       ". Please report to developers!");
	    System.exit(-1);
	}
	// remove the "same" index twice because the first remove
	// has the side effect of changing the index.
	rangeSet.remove(index*2);
	rangeSet.remove(index*2); 
	numEntries--;
    }

    // NOTE: history string is only used at the start of initializing
    // the history list in the GUI. Any further updates to this string
    // is quite meaningless.
    public Vector getHistoryStrings() {
	return historyStringVector;
    }

    public long getStartValue(int index) {
	if ((index < 0) ||
	    (index >= numEntries)) {
	    System.err.println("Internal Error: Requested history index " + 
			       index + " is invalid. Max number of " +
			       "histories is " + numEntries +
			       ". Please report to developers!");
	    System.exit(-1);
	}
	return ((Long)rangeSet.elementAt(index*2)).longValue();
    }

    public long getEndValue(int index) {
	if ((index < 0) ||
	    (index >= numEntries)) {
	    System.err.println("Internal Error: Requested history index " + 
			       index + " is invalid. Max number of " +
			       "histories is " + numEntries +
			       ". Please report to developers!");
	    System.exit(-1);
	}
	return ((Long)rangeSet.elementAt(index*2+1)).longValue();
    }
}