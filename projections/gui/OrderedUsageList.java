package projections.gui;

public class OrderedUsageList
{
   private Link head;
   private Link tail;
   private Link pre;
   private int len;
   private Link head2;
	private Link tail2;
   class Link
   {
	  int entry;
	  float usage;
	  Link next;
	  Link(float u, int e, Link n) {usage = u; entry = e; next = n;}
   }   
	public void combineLists()
	{
	if ((tail2 != null)&&(tail !=null))
	    {
		tail2.next = head;
		head = head2;
	    }
	else if (tail2 != null)
	    {
		tail = tail2;
		head = head2;
	    }
	head2 = null;
	tail2 = null;
	}
   public int currentEntry()
   {
	  Link cur = nextLink();
	  if(cur == null)
		 return -1;
	  else
		 return cur.entry;
   }   
   public float currentUsage()
   {
	  Link cur = nextLink();
	  if(cur == null)
		 return -1;
	  else 
		 return cur.usage;
   }   
   public boolean hasMoreElements()
   {
	  return nextLink() != null;
   }   
	// ADDED FUNCTION HeadEntryNum()
	// Returns the first entry number and then moves the head 
	// pointer to point to the next element of the list 
	// Basically removing the first element of the list
	public int HeadEntryNum()
	{
	if (head == null)
	    return -1;
	else 
	    { int returnval = head.entry; 
	     if (head.next == null)
		removeAll();
	    else
		head = head.next;
	    return returnval;
	    }
	}
   public void insert(float u, int e)
   {
	  Link newLink;
	  reset();
	  
	  Link tmp = nextLink();
	  while(tmp != null && tmp.usage > u)
	  {
		 pre = tmp;
		 tmp = nextLink();
	  }

	  if(tmp == null)
	  {
		 newLink = new Link(u, e, tmp);
		 if(head == null)
		 {
			head = newLink;
			tail = newLink;
		 }
		 else
		 {
			pre.next = newLink;
			tail = newLink;
		 }
		 len++;
	  }
	  else
	  {      
		 newLink = new Link(u, e, tmp);
		 if(head == tmp)
			head = newLink;
		 else
			pre.next = newLink;
		 len++;       
	  }       
   }   
	public int insert2(float u, int e, int[] DisplayOrder, int listsize)
	{
	   int DisplayIndex = 0;
	   Link curr = head2;
	   Link prev = null;
	   while (DisplayIndex < listsize)
	   { 
	       if ((curr != null) && (curr.entry == DisplayOrder[DisplayIndex]))
		   { prev = curr;
		   curr = curr.next;
		   }
	       else if (e == DisplayOrder[DisplayIndex])
		   {
		       len++;
		       Link newLink;
		       if (head2 == null)
			   { 
			       newLink = new Link(u,e, null);
			       head2 = newLink;
			       tail2 = newLink;
			   }
		       else if (curr == null) 
			   {
			       newLink = new Link(u,e, null);
			       prev.next = newLink;
			       tail2  = newLink;
			   }	      
		       else if (head2 == curr)
			   {
			       newLink = new Link(u, e, head2);
			       newLink.next = head2;
			       head2 = newLink;
			   }

		       else
			   {
			       newLink = new Link(u, e, curr);
			       newLink.next = curr;
			       prev.next = newLink;
			   }
		       return 1;
		   }
	       DisplayIndex++;
	   }
	   return -1;
	}
   public void nextElement()
   {
	  if(pre == null) 
		 pre = head;
	  else
		 pre = pre.next; 
   }   
   private Link nextLink()
   {
	  if(pre == null)
		 return head;
	  else
		 return pre.next;
   }   
   public void removeAll()
   { 
	  pre  = null;
	  head = null;
	  tail = null;
	  len = 0;
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