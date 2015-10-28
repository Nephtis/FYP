package AgentLearning;

/**
 *
 * @author b2026323 - Dave Halperin
 */
public class WallList {
    private Wall walldata;    // Wall's data
    private WallList next;  // Next wall in list  
    
    // Constructor
    public WallList(Wall current, WallList next)
    {
        this.walldata = current;
        this.next = next;
    }
    public Wall GetDataItem()
    {
        return this.walldata;
    }
    public WallList GetNextItem()
    {
        return this.next;
    }
    public int GetWallListCount()
    {
        if (this.next == null)
            {
                return 1;
            }
        else return 1 + next.GetWallListCount();    // 1 + tail each time  
    }
    public WallList GetWallByIndex(int index)
    {
        if (index == 0) // If at the bottom/end (here 0 is first index)
        {
            return this;
        }
        else 
        {
            if (next == null)   // If at the end of the list - prevent null pointer exception
                return null;
            return next.GetWallByIndex(index-1);
        }
    }
    public void AddWallToList(Wall wall)
    {
           if (this.next == null)
           {
               WallList newWall = new WallList(wall, null);
               this.next = newWall;
           }
           else
           {
               next.AddWallToList(wall);
           }
    }				// add to the end
    public char DeleteWallFromList(int index)
    {
        char ret;
        Wall current = this.GetWallByIndex(index).GetDataItem();
        ret = current.getDirection();
        WallList temp = this.GetWallByIndex(index-1);
        temp.next = this.GetWallByIndex(index+1);
        return ret;
    }
    // Peek at a wall without deleting it
    public WallList PeekWallFromList(int index)
    {
        WallList ret;
        ret = GetWallByIndex(index);
        return ret;
    }
}