package AgentLearning;

/**
 *
 * @author b2026323 - Dave Halperin
 */
// Implement stack of moves as vector
public class TrackInfo {

    int maxCapacity = 10; // Arbitrary size value to start
    int length;
    MoveInfo Stack[] = new MoveInfo[maxCapacity]; // Create a stack of moves

    public TrackInfo() {
        length = 0;
    }

    // Returns true if the stack is empty otherwise false
    public boolean IsEmpty() {
        if (length == 0) {
            return true;
        } else {
            return false;
        }
    }

    // Returns the top item on the stack but doesn't remove it
    public MoveInfo Peek() {
        if (length == 0) {
            return null;
        }
        length--;
        MoveInfo temp = Stack[length];
        length++;
        return temp;
    }

    // Returns the top item on the stack and removes it
    public MoveInfo Pop() {
        if (length == 0) {
            return null;
        } else {
            length--;
            MoveInfo temp = Stack[length];  // Preserve top value
            return temp;
        }
    }

    // Adds a new item to the top of the stack
    public void Push(int y, int x, int move) {
        if (Stack != null) {
            if (length == maxCapacity) {
                maxCapacity = maxCapacity * 2;
                MoveInfo MyNewStack[] = new MoveInfo[maxCapacity];
                System.arraycopy(Stack, 0, MyNewStack, 0, length);
                Stack = MyNewStack;
            }
            Stack[length] = new MoveInfo(y, x, move);
            length++;
        }
    }
}
