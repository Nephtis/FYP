package AgentLearning;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

// JADE

import jade.core.Runtime; 
import jade.core.Profile; 
import jade.core.ProfileImpl; 
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author b2026323 - Dave Halperin
 */
public class MazeView extends JFrame implements KeyListener {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1000;
    private static final int TOP = 30;
    private int width, height;
    private Cell[][] maze;
    private PrimMazeInfo mazeinfo;
    private PlayerMazeMove player;  // The player, who can move freely  
    private MoveInfo playercurrent;
    private MoveInfo playerprevious;
    private Object[] params = new Object[4];
    // For maze scaling:
    private int scalex;
    private int scaley;
    private int x;
    private int y;
    private int cx; // These are for scaling images within the maze (the exit)
    private int cy;

    BufferedImage jeep = getImage("images/jeep.png"),
            snakeup = getImage("images/snakeup.png"),
            snakedown = getImage("images/snakedown.png"),
            snakeleft = getImage("images/snakeleft.png"),
            snakeright = getImage("images/snakeright.png"),
            guardup = getImage("images/guardup.png"),
            guarddown = getImage("images/guarddown.png"),
            guardleft = getImage("images/guardleft.png"),
            guardright = getImage("images/guardright.png"),
            walltile = getImage("images/walltile.png"),
            //mazefloor = getImage("images/mazefloor.png"), // Not currently used
            blank = getImage ("images/blank.png");

    public MazeView(PrimMazeInfo info) {
        mazeinfo = info;
        this.maze = mazeinfo.maze;
        this.width = mazeinfo.getWidth();
        this.height = mazeinfo.getHeight();

        // Add the player
        player = new PlayerMazeMove(maze, mazeinfo);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addKeyListener(this);
        setTitle("Escape");
        setSize(WIDTH+200, TOP + HEIGHT);
        setVisible(true);
        
        // To be passed to agent
        params[0] = maze;
        params[1] = mazeinfo;
        params[2] = this;
        params[3] = player;
        
        // Create a runtime, container, and agent(s)
        Runtime rt = Runtime.instance(); 
        Profile p = new ProfileImpl(); 
        ContainerController mainContainer = rt.createMainContainer(p); 
        try{
            AgentController ac = mainContainer.createNewAgent("enemy1", 
            "AgentLearning.Enemy", params); // With the params declared earlier
            ac.start(); 
        } catch (StaleProxyException e){
            System.out.println("StaleProxyException caught...");
        }
        
        // Draw the background (once)
        this.scalex = WIDTH / this.width - 20; // Need to make this more 'dynamic'... Was / 2, need some calc?
        this.scaley = HEIGHT / this.height - 20;
        this.x = 20;
        this.y = TOP + 20;
        this.cx = (scalex * 4) / 10; 
        this.cy = (scaley * 4) / 10;
        paintBG(getGraphics()/*, scalex, scaley, x, y, cx, cy*/);
        
        // Draw the initial objects
        paintPlayer(getGraphics());
        PrintGUIMessage("normal");
}

    private BufferedImage getImage(String filename) {
        try {
            InputStream in = getClass().getResourceAsStream(filename);
            return ImageIO.read(in);
        } catch (IOException e) {
            System.out.println("The image was not loaded. Is it there? Is the filepath correct?");
            System.exit(1);
        }
        return null;
    }

    // The 'maze' itself is just a data structure that exists in code.
    // This draws the maze on the screen so that it can be seen and interacted with more easily.
    public final void paintBG(Graphics g){
        g.setColor(Color.LIGHT_GRAY); // Screen background
        g.fillRect(0, 0, getWidth(), getHeight());
        // Draw a 'floor' background (currently just using a solid colour so I can paint over it)
        //g.drawImage(mazefloor, 0, 0, getWidth(), getHeight(), null);
        // Draw the Walls of the maze
        for (int j = 0; j < this.height; j++, y += scaley) {
            x = 20; // Reset x every time (?)
            for (int i = 0; i < this.width; i++, x += scalex) {
                if (!(maze[j][i].northwall.isBroken())) // If the north wall isn't broken
                {
                    g.drawImage(walltile, x, y, scalex, scaley / 4, null); // Draw a wall there (image, xpos, ypos, width, height, observer)
                }
                if (!(maze[j][i].eastwall.isBroken())) // etc
                {
                    g.drawImage(walltile, x + scalex, y, scalex / 4, scaley, null);
                }
                if (!(maze[j][i].southwall.isBroken())) {
                    g.drawImage(walltile, x, y + scaley, scalex, scaley / 4, null);
                }
                if (!(maze[j][i].westwall.isBroken())) {
                    g.drawImage(walltile, x, y, scalex / 4, scaley, null);
                }
                // Draw a red square to show where Snake has 'seen'
//                if (mazeinfo.seen[j][i]) {
//                    g.setColor(Color.red);
//                    g.fillRect(x + (scalex / 2), y + (scaley / 2), cx, cy);
//                }
                if ((j == mazeinfo.getTargetM()) && (i == mazeinfo.getTargetN())) {
                    // Draw the exit
                    g.drawImage(jeep, x + (scalex / 2), y + (scaley / 2), cx, cy, null);
                    g.setColor(Color.LIGHT_GRAY);
                    if (maze[j][i].northwall.isEdge()) {
                        // Paint over the edge creating a 'way out'
                        g.fillRect(x, y, scalex, scaley / 4);
                    } else if (maze[j][i].eastwall.isEdge()) {
                        g.fillRect(x + scalex, y, scalex / 4, scaley);
                    } else if (maze[j][i].southwall.isEdge()) {
                        g.fillRect(x, y + scaley, scalex, scaley / 4);
                    } else if (maze[j][i].westwall.isEdge()) {
                        g.fillRect(x, y, scalex / 4, scaley);
                    }
                }
            }
        }
    }
    
    // For debugging purposes (will probably be removed in final release)
    /*
    public final void paintLineOfSight(MazeMove mazemove){
        Graphics g = getGraphics();
        int mx = (scalex * 4) / 8;
        int my = (scaley * 4) / 8;
        MoveInfo[] lineofsight = mazemove.getLineOfSight();
        if (lineofsight != null && lineofsight.length != 0){
            for (int i=0; i<lineofsight.length; i++){
                if (lineofsight[i] != null){
                    //System.out.println("Drawing line of sight, i is " + i);
                    x = (lineofsight[i].x * scalex) + 20;
                    y = TOP + 20 + lineofsight[i].y * scaley;
                    g.drawImage(jeep, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                }
            }
        }
    }
    */
    
    // Draws the enemies
    public final void paintEnemy(MoveInfo current, MazeMove mazemove){
        Graphics g = getGraphics(); // needed?
        int mx = (scalex * 4) / 8;
        int my = (scaley * 4) / 8;
        MoveInfo previous;
        // Draw the enemy agents (put this in a for loop for multiple agents)
        // (this will also let the player decide how many enemies they want e.g. from 1-10, will need a minimum maze size)
        // e.g. in a 5x5 maze you might not be able to win with 10 agents, etc.
        // could just do minEnemies = mazesize / 3?)
        // mazemove will need to be an array of all the enemies' mazemoves, which is then looped through and painted
        previous = mazemove.GetPreviousLocation();
        if (previous != null){
            //System.out.println("Drawing enemy blank");
            x = (previous.x * scalex) + 20;
            y = TOP + 20 + previous.y * scaley;
            g.drawImage(blank, x + (scalex / 2), y + (scaley / 2), mx, my, null);
        }
        
        if (current != null) {
            x = (current.x * scalex) + 20;
            y = TOP + 20 + current.y * scaley;
            if (current.move == MoveInfo.NORTH) {
                g.drawImage(guardup, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            } else if (current.move == MoveInfo.EAST) {
                g.drawImage(guardright, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            } else if (current.move == MoveInfo.SOUTH) {
                g.drawImage(guarddown, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            } else if (current.move == MoveInfo.WEST) {
                g.drawImage(guardleft, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            } else if (current.move == MoveInfo.NONE) {
                g.drawImage(guardup, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            }
        }
    }
    
    // Draws the player
    // Previously I just re-painted the entire GUI, background and all, but that
    // led to an annoying 'flickering' every time it updated.
    public final void paintPlayer(Graphics g) {
        //MoveInfo current = enemy1.moves.GetLocation(); // Enemy's current pos and direction
        int mx = (scalex * 4) / 8; // Different scale for player + Enemy
        int my = (scaley * 4) / 8;
        
        // Paint over the player's previous location with a blank tile to avoid 'afterimages'
        playerprevious = player.GetPreviousLocation();
        if (playerprevious != null){
            //System.out.println("Drawing blank");
            x = (playerprevious.x * scalex) + 20;
            y = TOP + 20 + playerprevious.y * scaley;
            g.drawImage(blank, x + (scalex / 2), y + (scaley / 2), mx, my, null);
        }
        
        // Then draw the player
        playercurrent = player.GetLocation();
        if (playercurrent != null) {
            x = (playercurrent.x * scalex) + 20;
            y = TOP + 20 +playercurrent.y * scaley;
            if (playercurrent.move == MoveInfo.NORTH) {
                g.drawImage(snakeup, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            } else if (playercurrent.move == MoveInfo.EAST) {
                g.drawImage(snakeright, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            } else if (playercurrent.move == MoveInfo.SOUTH) {
                g.drawImage(snakedown, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            } else if (playercurrent.move == MoveInfo.WEST) {
                g.drawImage(snakeleft, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            } else if (playercurrent.move == MoveInfo.NONE) {
                g.drawImage(snakeup, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            }
        }
        
        // Check for 'stop conditions'
//        if (current.y == mazeinfo.getTargetM() && current.x == mazeinfo.getTargetN()) { // Snake's coords are the exit coords
//            snake.setDone();
//            player.setDone();
//            g.drawString("The prisoner got away...", 450, 700);
//            g.drawString("Found exit at " + current.y + ", " + current.x, 480, 730);
//        } else if (playercurrent.y == current.y && playercurrent.x == current.x) {  // Player's coords are Snake's coords
//            snake.setDone();
//            player.setDone();
//            g.drawString("Caught the prisoner! ", 450, 700);
//            g.drawString("Caught at " + current.y + ", " + current.x, 480, 730);
//        }
    }
    
    public final void PrintGUIMessage(String message){
        // Paint a message on the screen to give info to the player (and help me debug...)
        Graphics g = getGraphics();
        g.setFont(new Font("", 0, 40));
        g.drawString("Enemy status:", 940, 80);
        g.setFont(new Font("", 0, 60));
        switch (message){
            case "normal": 
                g.drawImage(blank, 940, 100, 200, 200, null); // Paint over the previous message
                g.setColor(Color.BLACK);
                g.drawString("Normal", 940, 160);
                break;
            case "alert":
                g.drawImage(blank, 940, 100, 200, 200, null);
                g.setColor(Color.RED);
                g.drawString("ALERT", 940, 160);
                g.setColor(Color.BLACK); // Reset the colour?
                break;
            case "search":
                g.drawImage(blank, 940, 100, 200, 200, null);
                g.setColor(Color.ORANGE);
                g.drawString("Search", 940, 160);
                g.setColor(Color.BLACK);
                break;
            case "caution":
                g.drawImage(blank, 940, 100, 200, 200, null);
                g.setColor(Color.YELLOW);
                g.drawString("Caution", 940, 160);
                g.setColor(Color.BLACK);
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
//        boolean start = false;
//        
//        while (!(start)){
//        if (keyCode == KeyEvent.VK_X){
//                try {
//                    paint(getGraphics());
//                    Thread.sleep(500);
//                    start = true;
//                } catch(InterruptedException ex) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//        }
        switch (keyCode) {
            case KeyEvent.VK_UP:    // Up arrow key
                if (player.HasMoreMoves()) {
                    player.Move(1); // Move North
                }
//                if (snake.HasMoreMoves() && !snake.ExitFound()) {
//                    snake.SearchForExit();
                    paintPlayer(getGraphics());
                // So the player can't hold down the arrow key and fly across the screen, force them to wait between inputs
                    // BUT this leads to problems if you do hold it down, moves end up in a 'queue'...
                try {
                    Thread.sleep(150);
                    break;
                } catch (InterruptedException ex) {
                    Logger.getLogger(MazeView.class.getName()).log(Level.SEVERE, null, ex);
                }
//                }
                break;
            case KeyEvent.VK_DOWN:
                if (player.HasMoreMoves()) {
                    player.Move(3);
                }
//                if (snake.HasMoreMoves() && !snake.ExitFound()) {
//                    snake.SearchForExit();
                    paintPlayer(getGraphics());
                    try {
                    Thread.sleep(150);
                    break;
                } catch (InterruptedException ex) {
                    Logger.getLogger(MazeView.class.getName()).log(Level.SEVERE, null, ex);
                }
//                }
                break;
            case KeyEvent.VK_LEFT:
                if (player.HasMoreMoves()) {
                    player.Move(4);
                }
//                if (snake.HasMoreMoves() && !snake.ExitFound()) {
//                    snake.SearchForExit();
                    paintPlayer(getGraphics());
                    try {
                    Thread.sleep(150);
                    break;
                } catch (InterruptedException ex) {
                    Logger.getLogger(MazeView.class.getName()).log(Level.SEVERE, null, ex);
                }
//                }
                break;
            case KeyEvent.VK_RIGHT:
                if (player.HasMoreMoves()) {
                    player.Move(2);
                }
//                if (snake.HasMoreMoves() && !snake.ExitFound()) {
//                    snake.SearchForExit();
                    paintPlayer(getGraphics());
                    try {
                    Thread.sleep(150);
                    break;
                } catch (InterruptedException ex) {
                    Logger.getLogger(MazeView.class.getName()).log(Level.SEVERE, null, ex);
                }
//                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}