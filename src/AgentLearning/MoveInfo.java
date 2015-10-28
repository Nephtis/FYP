package AgentLearning;

/**
 *
 * @author b2026323 - Dave Halperin
 */
// A record of the current x and y coords as well as current direction
public class MoveInfo {
  public static final int NONE=0;
  public static final int NORTH=1;
  public static final int EAST=2;
  public static final int SOUTH=3;
  public static final int WEST=4;

  public int x, y, move;
  
  public MoveInfo(int y, int x, int move) {
    this.y=y;
    this.x=x;
    this.move=move;
  }       
}