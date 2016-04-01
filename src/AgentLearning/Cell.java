package AgentLearning;

/**
 *
 * @author b2026323 - Dave Halperin
 */
public class Cell {

    public Wall northwall;
    public Wall eastwall;
    public Wall southwall;
    public Wall westwall;
    private boolean explored;
    private boolean isExit;
    public int y;
    public int x;
    public int g;
    public int h;

    // Constructor
    public Cell(int m, int n) {
        this.explored = false;
        this.isExit = false;
        this.northwall = new Wall(m, n, 'N');
        this.eastwall = new Wall(m, n, 'E');
        this.southwall = new Wall(m, n, 'S');
        this.westwall = new Wall(m, n, 'W');
        y = m;
        x = n;
        g = 0; // Will get set during A*
        h = 0;
    }

    public void setCellExplored(boolean newcontent) {
        explored = newcontent;
    }

    public boolean isExplored() {
        if (this.explored == true) {
            return true;
        } else {
            return false;
        }
    }

    public void breakCellWall(char wallchar) {
        switch (wallchar) {
            case 'N':
                northwall.breakWall();
                break;
            case 'E':
                eastwall.breakWall();
                break;
            case 'S':
                southwall.breakWall();
                break;
            case 'W':
                westwall.breakWall();
                break;
        }
    }

    public void setExit() {
        this.isExit = true;
    }

    public boolean isExit() {
        return this.isExit;
    }
}
