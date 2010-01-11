package projections.gui;

/** @note this class has no right to exist anymore. 
 * Java contains types that already make sorted lists 
 * or trees, such as TreeSet */


// use this by:
//
// OrderedIntList list = new OrderedIntList();
// list.insert(8);
// list.insert(2);
// list.insert(5);
// 
// int e;
// list.reset();
// while (e=nextElement()) != -1) { 
//   System.out.println(e);
// }
// 
// will print out: 2, 5, 8 on separate lines

public class OrderedIntList
{
    private Link head;
    private Link pre;
    private int len;
    
    private class Link
    {
    	private int data;
    	private Link next;
    	private Link(int d, Link n) {
    		data = d; next = n;
    	}
    }  

    public boolean isEmpty() {
	return (len == 0);
    }
    
    public OrderedIntList copyOf()
    {
	OrderedIntList listcopy = new OrderedIntList();
	reset();
	int e;
	while((e = nextElement()) != -1)
	    listcopy.insert(e);
	
	listcopy.reset();
	return listcopy;
    }   

//    // This is an inefficient union just to get things done. If
//    // efficiency becomes an issue, please implement the merge
//    // algorithm instead.
//    public static OrderedIntList union(OrderedIntList list1,
//    		OrderedIntList list2) {
//    	OrderedIntList returnValue;
//    	OrderedIntList shorterList;
//    	OrderedIntList longerList;
//    	if (list1.size() <= list2.size()) {
//    		shorterList = list1;
//    		longerList = list2;
//    	} else {
//    		shorterList = list2;
//    		longerList = list1;
//    	}
//    	returnValue = longerList.copyOf();
//
//    	shorterList.reset();
//    	while (shorterList.hasMoreElements()) {
//    		returnValue.insert(shorterList.nextElement());
//    	}
//    	returnValue.reset();
//
//    	return returnValue;
//    }

//    public int currentElement()
//    {
//	Link cur = nextLink();
//	if(cur == null)
//	    return -1;
//	else
//	    return cur.data;   
//    }   

    public boolean equals(OrderedIntList otherlist)
    {
	if(otherlist == null)
	    return false;
	
	if(size() != otherlist.size())
	    return false;
	
	reset();
	otherlist.reset();
	
	int e;
	while((e = nextElement()) != -1) {
	    if(otherlist.nextElement() != e)
		return false;
	}
	  
	return true;
    }   

    public boolean hasMoreElements()
    {
	return nextLink() != null;
    }   

//    /**
//       return true if this list contain otherlist
//    */
//    public boolean contains(OrderedIntList otherlist)
//    {
//	if (otherlist == null) return true;
//	if (otherlist.size() > size()) return false;
//	reset();
//	otherlist.reset();
//	int e, me;
//	while((e = otherlist.nextElement()) != -1) {
//	    while (e != (me = nextElement())) {
//		if (me == -1) {
//		    return false;
//		}
//	    }
//	}
//	return true;
//    }

    /**
//       return true if this list has ele
//    */
//    public boolean contains(int eleValue)
//    {
//	int me;
//	reset();
//	while((me = nextElement()) != -1) {
//	    if (me == eleValue) {
//		return true;
//	    }
//	}
//	return false;
//    }

//    public void remove(int eleValue) {
//    	int me;
//    	reset();
//    	Link tmp = nextLink();
//    	while (tmp != null && tmp.data < eleValue) {
//    		pre = tmp;
//    		tmp = nextLink();
//    	}
//
//    	if (tmp != null) {
//    		if (tmp.data == eleValue) {
//    			if (pre == null) {
//    				head = tmp.next;
//    			} else {
//    				pre.next = tmp.next;
//    			}
//    		}
//    	}
//    }

    public void insert(int eleValue)
    {
	Link newLink;
	reset();
	
	Link tmp = nextLink();
	while(tmp != null && tmp.data < eleValue) {
	    pre = tmp;
	    tmp = nextLink();
	}

	if (tmp == null) {
	    newLink = new Link(eleValue, tmp);
	    if (head == null) {
		head = newLink;
	    } else {
		pre.next = newLink;
	    }
	    len++;
	} else if (tmp.data != eleValue) {      
	    newLink = new Link(eleValue, tmp);
	    if (head == tmp) {
		head = newLink;
	    } else {
		pre.next = newLink;
	    }
	    len++;      
	}       
    }   
    
    public String listToString()
    {
	reset();
	int min, max, tmp;
	tmp = nextElement();
	  
	String result = "";
	int interval = -1;
	min = max = -1;
	while(tmp != -1) {
	    if (min == -1) {
		min = tmp;
		tmp = nextElement();
	    } else if (interval == -1) {
		interval = tmp-min;
		max = tmp;
		tmp = nextElement();
	    } else {
		while((tmp - max) == interval) {
		    max= tmp;
		    tmp = nextElement();
		}
		if (max == min+interval) {
		    if (!result.equals("")) {
			result += ",";
		    }
		    result += new String("" + min); 
		    min = max; 
		    interval = -1;
		} else {
		    if (!result.equals("")) {
			result += ",";
		    }
		    result += new String("" + min + "-" + max); 
		    if (interval > 1) {
			result += new String(":" + interval);
		    }
		    min = interval = max = -1;
		}
	    }
	}   
	// handle the leftover
	if (min != -1) {
	    if (!result.equals("")) {
		result += ",";
	    }
	    result += new String("" + min); 
	}
	if (max != -1) {
	    if (!result.equals("")) {
		result += ",";
	    }
	    result += new String("" + max); 
	}
	return result;
    }   

    public int nextElement()
    {
	// move pre pointer
	if (pre == null) {
	    pre = head;
	} else {
	    pre = pre.next;
	}

	// "abuse" pre pointer to get "current" element
	if (pre == null) {
	    return -1;
	} else {
	    return pre.data;   
	}
    }   

    private Link nextLink()
    {
	if (pre == null) {
	    return head;
	} else {
	    return pre.next;
	}
    }   
    
//    public void printList()
//    {
//    	reset();
//    	while (hasMoreElements()) {
//    		System.out.println("" + nextElement());
//    	}
//    	reset();
//    }   

    protected void removeAll()
    { 
	len = 0;
	head = null;
	pre = null;
    }   

    public void reset()
    {
	pre = null;
    }   

    public int size()
    {
	return len;
    }   
}