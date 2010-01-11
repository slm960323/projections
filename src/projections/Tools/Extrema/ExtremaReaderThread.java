package projections.Tools.Extrema;

import java.io.EOFException;
import java.io.IOException;

import projections.analysis.GenericLogReader;
import projections.analysis.ProjDefs;
import projections.gui.Analysis;
import projections.gui.MainWindow;
import projections.misc.LogEntryData;


/** The reader threads for Time Profile tool. This class ought to be generalized for all the other tools needing similar functionality. */
class ExtremaReaderThread extends Thread  {

	// Temporary hardcode. This variable will be assigned appropriate
	// meaning in future versions of Projections that support multiple
	// runs.
	private static int myRun = 0;
	
	private int pe;
	private long startTime;
	private long endTime;
	private int numActivities;
	private int numActivityPlusSpecial;
	
	private int selectedActivity;
	private int selectedAttribute;
	
	double[] myData;

	
	protected ExtremaReaderThread(int pe, long startTime2, long endTime2, int numActivities, int numActivityPlusSpecial, int selectedActivity, int selectedAttribute){
		this.pe = pe;
		this.startTime = startTime2;
		this.endTime = endTime2;
		this.numActivities = numActivities;
		this.numActivityPlusSpecial = numActivityPlusSpecial;
		this.selectedActivity = selectedActivity;
		this.selectedAttribute = selectedAttribute;
	}

	
	public void run() { 

		myData = new double[numActivityPlusSpecial];

		
		// Construct tempData (read) array hereColorSelectable
		//
		// **NOTE** We really need a generic interface to a "data"
		// object. Re-writing the reading code each time for each
		// tool is starting to get really painful.
		//
		// Right now, we restrict ourselves to reading logs (since
		// we wanna support User Events, the nature of which 
		// unfortunately requires us to write a different read loop
		// for it.
		GenericLogReader reader = 
			new GenericLogReader(pe, MainWindow.runObject[myRun].getVersion());
		
		
		try {
			if (selectedActivity == Analysis.USER_EVENTS) {
				LogEntryData logData;
				LogEntryData logDataEnd;

				logData = reader.nextEventOfType(ProjDefs.USER_EVENT_PAIR);
				logDataEnd = reader.nextEventOfType(ProjDefs.USER_EVENT_PAIR );

				while (logData.time < startTime) {
					logData = reader.nextEventOfType(ProjDefs.USER_EVENT_PAIR);
					logDataEnd = reader.nextEventOfType(ProjDefs.USER_EVENT_PAIR );
				}
				int eventIndex = 0;
				while (true) {
					// process pair read previously
					eventIndex = 
						MainWindow.runObject[myRun].getUserDefinedEventIndex(logData.userEventID);
					myData[eventIndex] += logDataEnd.time - logData.time;
					logData = reader.nextEventOfType(ProjDefs.USER_EVENT_PAIR);
					logDataEnd = reader.nextEventOfType(ProjDefs.USER_EVENT_PAIR );
					if (logDataEnd.time > endTime) {
						break;
					}
				}
			} else {
				LogEntryData logData;
				// dealing with book-keeping events
				boolean isFirstEvent = true;
				// Jump to the first valid event
				boolean markedBegin = false;
				boolean markedIdle = false;
				long beginBlockTime = startTime;
				logData = reader.nextEventOnOrAfter(startTime);
				while (logData.time <= endTime) {
					LogEntryData BE = reader.getLastBE();
					switch (logData.type) {
					case ProjDefs.CREATION:
						if (isFirstEvent) {
							if ((BE != null) && 
									(BE.type == ProjDefs.BEGIN_PROCESSING)) {
								beginBlockTime = startTime;
								markedBegin = true;
							}
						}
						if (markedBegin) {
							int eventIndex = logData.entry;
							if (selectedAttribute == 2) {
								myData[eventIndex]++;
							} else if (selectedAttribute == 3) {
								myData[eventIndex] += logData.msglen;
							}
						}
						break;
					case ProjDefs.BEGIN_PROCESSING:
						isFirstEvent = false;
						// check pairing
						if (!markedBegin) {
							markedBegin = true;
						}
						if (selectedAttribute == 0 || selectedAttribute == 1 || selectedAttribute == 4|| selectedAttribute == 5|| selectedAttribute == 6|| selectedAttribute == 7) {
							// even if a previous begin is found, just
							// overwrite the begin time, we're
							// not expecting nesting here.
							beginBlockTime = logData.time;
						}
						break;
					case ProjDefs.END_PROCESSING:
						if (isFirstEvent) {
							markedBegin = true;
							beginBlockTime = startTime;
						}
						isFirstEvent = false;
						if (markedBegin) {
							markedBegin = false;
							if (selectedAttribute == 0 || selectedAttribute == 1 || selectedAttribute == 4||  selectedAttribute == 5||selectedAttribute == 6|| selectedAttribute == 7) {
								myData[logData.entry] += logData.time - beginBlockTime;
							}
						}
						break;
					case ProjDefs.BEGIN_IDLE:
						isFirstEvent = false;
						// check pairing
						if (!markedIdle) {
							markedIdle = true;
						}
						// NOTE: This code assumes that IDLEs cannot
						// possibly be nested inside of PROCESSING
						// blocks (which should be true).
						if (selectedAttribute == 0 || selectedAttribute == 1 || selectedAttribute == 4||  selectedAttribute == 5|| selectedAttribute == 6 || selectedAttribute == 7) {
							beginBlockTime = logData.time;
						}
						break;
					case ProjDefs.END_IDLE:
						if (isFirstEvent) {
							markedIdle = true;
							beginBlockTime = startTime;
						}
						// check pairing
						if (markedIdle) {
							markedIdle = false;
							if (selectedAttribute == 0 || selectedAttribute == 1 || selectedAttribute == 4||  selectedAttribute == 5||selectedAttribute == 6 || selectedAttribute == 7) {
								myData[numActivities] += logData.time - beginBlockTime;
							}
						}
						break;
					}
					logData = reader.nextEvent();
				}
				LogEntryData beginEvent = reader.getLastBE();
				// Now handle the tail case.
				switch (logData.type) {
				case ProjDefs.END_PROCESSING:
					// lastBE is empty by design in this case, so
					// use beginBlockTime recorded from previously.
					if (selectedAttribute == 0 || selectedAttribute == 1 || selectedAttribute == 4||  selectedAttribute == 5||selectedAttribute == 6 || selectedAttribute == 7) {
						myData[logData.entry] +=
							endTime - beginBlockTime;
					}
					break;
				case ProjDefs.END_IDLE:
					// lastBE is empty by design in this case, so
					// use beginBlockTime recorded from previously.
					if (selectedAttribute == 0 || selectedAttribute == 1 || selectedAttribute == 4||  selectedAttribute == 5||selectedAttribute == 6 || selectedAttribute == 7) {
						myData[numActivities] += endTime - beginBlockTime;
					}
					break;
				default:
					// all other cases. Ignore if no beginEvent is
					// found. Otherwise, use it's begin time if
					// it is greater than startTime, otherwise, use
					// startTime.
					if (beginEvent != null) {
						if (beginEvent.time > startTime) {
							beginBlockTime = beginEvent.time;
						} else {
							beginBlockTime = startTime;
						}
						switch (beginEvent.type) {
						case ProjDefs.BEGIN_PROCESSING:
							if (selectedAttribute == 0 || selectedAttribute == 1 || selectedAttribute == 4||  selectedAttribute == 5||selectedAttribute == 6 || selectedAttribute == 7) {
								myData[beginEvent.entry] +=
									endTime - beginBlockTime;
							}
							break;
						case ProjDefs.BEGIN_IDLE:
							if (selectedAttribute == 0 || selectedAttribute == 1 || selectedAttribute == 4|| selectedAttribute == 5|| selectedAttribute == 6 || selectedAttribute == 7) {
								myData[numActivities] +=
									endTime - beginBlockTime;
							}
							break;
						}
					}
				}
				reader.close();
			}
		} catch (EOFException e) {
			// close the reader and let the external loop continue.
			try {
				reader.close();
			} catch (IOException evt) {
				System.err.println("Outlier Analysis: Error in closing "+
						"file for processor " + pe);
				System.err.println(evt);
			}
		} catch (IOException e) {
			System.err.println("Outlier Analysis: Error in reading log "+
					"data for processor " + pe);
			System.err.println(e);
		}
		
		
		// compute overhead time
		myData[numActivities+1] = endTime - startTime;
		for(int e=0; e<numActivities+1; e++){
			myData[numActivities+1] -= myData[e];
		}
		
		
		if(selectedAttribute == ExtremaWindow.ATTR_LEASTIDLE || selectedAttribute == ExtremaWindow.ATTR_MOSTIDLE){
			// Scale raw data into percentages
			for(int e=0; e< myData.length; e++){
				myData[e] = myData[e] * 100.0 / (double)(endTime - startTime);
			}
		}
		
	}
	

}




