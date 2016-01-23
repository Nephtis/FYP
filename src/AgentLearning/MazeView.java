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

    private static final int WIDTH = 900;
    private static final int HEIGHT = 800;
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
    public int enemyspawn = 0; // Deciding where the enemy spawns
    
    public boolean running = true; // Determines whether the game is "running" - i.e. whether actions happen in game (will be false when game ends)
    public boolean shouldreset = false; // Determines whether agents should reset their positions
    public boolean alertmode = false;
    public boolean searchmode = false;
    
    private long starttime;
    private long endtime;
    
    BufferedImage jeep = getImage("images/jeep.png"),
            snakeup = getImage("images/snakeup.png"),
            snakedown = getImage("images/snakedown.png"),
            snakeleft = getImage("images/snakeleft.png"),
            snakeright = getImage("images/snakeright.png"),
            guardup = getImage("images/guardup.png"),
            guarddown = getImage("images/guarddown.png"),
            guardleft = getImage("images/guardleft.png"),
            guardright = getImage("images/guardright.png"),
            guardupALERT = getImage("images/guardupALERT.png"),
            guarddownALERT = getImage("images/guarddownALERT.png"),
            guardleftALERT = getImage("images/guardleftALERT.png"),
            guardrightALERT = getImage("images/guardrightALERT.png"),
            guardupQUESTION = getImage("images/guardupQUESTION.png"),
            guarddownQUESTION = getImage("images/guarddownQUESTION.png"),
            guardleftQUESTION = getImage("images/guardleftQUESTION.png"),
            guardrightQUESTION = getImage("images/guardrightQUESTION.png"),
            bossup = getImage("images/spec_enemyup.png"),
            bossdown = getImage("images/spec_enemydown.png"),
            bossleft = getImage("images/spec_enemyleft.png"),
            bossright = getImage("images/spec_enemyright.png"),
            walltile = getImage("images/walltile.png"),
            //mazefloor = getImage("images/mazefloor.png"), // Not currently used
            blank = getImage ("images/blank.png");

    public MazeView(PrimMazeInfo info) throws InterruptedException {
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
            /*AgentController ac = mainContainer.createNewAgent("enemy1", 
            "AgentLearning.Enemy", params); // With the params declared earlier
            ac.start();*/
            /*AgentController ac2 = mainContainer.createNewAgent("enemy2", "AgentLearning.Enemy", params);
            ac2.start();
            AgentController ac3 = mainContainer.createNewAgent("enemy3", "AgentLearning.Enemy", params);
            ac3.start();
            AgentController ac4 = mainContainer.createNewAgent("enemy4", "AgentLearning.Enemy", params);
            ac4.start();
            AgentController ac5 = mainContainer.createNewAgent("enemy5", "AgentLearning.Enemy", params);
            ac5.start();*/
            AgentController ac = mainContainer.createNewAgent("boss", 
            "AgentLearning.Boss", params);
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
        paintPlayer(getGraphics());
        PrintGUIMessage("normal");
        
        starttime = System.currentTimeMillis();
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
    public final void paintBG(Graphics g) throws InterruptedException{
        g.setColor(Color.LIGHT_GRAY); // Screen background
        Thread.sleep(100); // No idea why but for some reason it doesn't draw properly unless there's a small delay
        g.fillRect(0, 0, getWidth(), getHeight());
        // Draw a 'floor' background (currently just using a solid colour so I can paint over it)
        //g.drawImage(mazefloor, 0, 0, getWidth(), getHeight(), null);
        // Draw the Walls of the maze
        for (int j = 0; j < this.height; j++, y += scaley) {
            x = 20; // Reset x every time (?)
            for (int i = 0; i < this.width; i++, x += scalex) {
                if (!(maze[j][i].northwall.isBroken())) // If the north wall isn't broken
                {
                    g.drawImage(walltile, x, y, scalex, scaley / 5, null); // Draw a wall there (image, xpos, ypos, width, height, observer)
                }
                if (!(maze[j][i].eastwall.isBroken())) // etc
                {
                    g.drawImage(walltile, x + scalex, y, scalex / 5, scaley, null);
                }
                if (!(maze[j][i].southwall.isBroken())) {
                    g.drawImage(walltile, x, y + scaley, scalex, scaley / 5, null);
                }
                if (!(maze[j][i].westwall.isBroken())) {
                    g.drawImage(walltile, x, y, scalex / 5, scaley, null);
                }
                // Draw a red square to show where Snake has 'seen'
//                if (mazeinfo.seen[j][i]) {
//                    g.setColor(Color.red);
//                    g.fillRect(x + (scalex / 2), y + (scaley / 2), cx, cy);
//                }
                if ((j == mazeinfo.getTargetM()) && (i == mazeinfo.getTargetN())) {
                    // Draw the exit
                    g.drawImage(jeep, x + (scalex / 2), y + (scaley / 2), cx, cy, null);
                    //g.setColor(Color.LIGHT_GRAY);
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
                if (alertmode){
                    g.drawImage(guardupALERT, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                } else if (searchmode) {
                    g.drawImage(guardupQUESTION, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                } else {
                    g.drawImage(guardup, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                }
            } else if (current.move == MoveInfo.EAST) {
                if (alertmode){
                    g.drawImage(guardrightALERT, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                } else if (searchmode) {
                    g.drawImage(guardrightQUESTION, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                } else {
                    g.drawImage(guardright, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                }
            } else if (current.move == MoveInfo.SOUTH) {
                if (alertmode){
                    g.drawImage(guarddownALERT, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                } else if (searchmode) {
                    g.drawImage(guarddownQUESTION, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                } else {
                    g.drawImage(guarddown, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                }
            } else if (current.move == MoveInfo.WEST) {
                if (alertmode){
                    g.drawImage(guardleftALERT, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                } else if (searchmode) {
                    g.drawImage(guardleftQUESTION, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                } else {
                    g.drawImage(guardleft, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                }
            } else if (current.move == MoveInfo.NONE) {
                if (alertmode){
                    g.drawImage(guardupALERT, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                } else if (searchmode) {
                    g.drawImage(guardupQUESTION, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                } else {
                    g.drawImage(guardup, x + (scalex / 2), y + (scaley / 2), mx, my, null);
                }
            }
        }
    }
    
    public final void paintBoss(MoveInfo current, MazeMove mazemove, PrimMazeInfo mazeinfo){
        Graphics g = getGraphics(); // needed?
        int mx = (scalex * 4) / 8;
        int my = (scaley * 4) / 8;
        MoveInfo previous;
        previous = mazemove.GetPreviousLocation();
        if (previous != null){
            x = (previous.x * scalex) + 20;
            y = TOP + 20 + previous.y * scaley;
            g.drawImage(blank, x + (scalex / 2), y + (scaley / 2), mx, my, null);
        }
        
        if (current != null) {
            x = (current.x * scalex) + 20;
            y = TOP + 20 + current.y * scaley;
            if (current.move == MoveInfo.NORTH) {
                g.drawImage(bossup, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            } else if (current.move == MoveInfo.EAST) {
                g.drawImage(bossright, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            } else if (current.move == MoveInfo.SOUTH) {
                g.drawImage(bossdown, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            } else if (current.move == MoveInfo.WEST) {
                g.drawImage(bossleft, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            } else if (current.move == MoveInfo.NONE) {
                g.drawImage(bossup, x + (scalex / 2), y + (scaley / 2), mx, my, null);
            }
        }
        
        //System.out.println("Costs:");
        for (int i = 0; i < mazeinfo.costs.length; i++){
            for (int j = 0; j < mazeinfo.costs.length; j++){ // i and j are the same (costs has the same width and height)
                System.out.print(mazeinfo.costs[i][j] + " ");
            }
            System.out.println(""); // new line for every row
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
        if (playercurrent.y == mazeinfo.getTargetM() && playercurrent.x == mazeinfo.getTargetN()) { // Player's coords are the exit coords
            player.setDone();
            EndGame("win");
        } 
    }
    
    // End the game (and write output to file?)
    public final void EndGame(String cond){
        Graphics g = getGraphics();
        endtime = System.currentTimeMillis();
        long totaltime = endtime - starttime;
        long seconds = (totaltime / 1000) % 60;
        long minutes = ((totaltime / 1000) - seconds) / 60;
        if (cond.equalsIgnoreCase("win")){ // Player has escaped
            running = false; // "Stop" the game
            g.setFont(new Font("", 0, 60));
            g.setColor(Color.GREEN);
            g.drawString("GAME", 740, 300);
            g.drawString("OVER", 740, 350);
            g.setFont(new Font("", 0, 40));
            g.drawString("You have", 740, 400);
            g.drawString("escaped!", 740, 450);
            g.setFont(new Font("", 0, 20));
            g.drawString("Total time:", 740, 550);
            g.drawString(String.valueOf(minutes) + " minutes " + String.valueOf(seconds) + " seconds", 740, 600);
            // Need to make sure agents terminate... Done inside Enemy class
            // or:
            /*try{
                mainContainer.kill();
            } catch (StaleProxyException e){
            System.out.println("StaleProxyException caught...");
            }*/
        } else if (cond.equalsIgnoreCase("lose")){ // Player was caught
            running = false;
            g.setFont(new Font("", 0, 60));
            g.setColor(Color.RED);
            g.drawString("GAME", 740, 300);
            g.drawString("OVER", 740, 350);
            g.setFont(new Font("", 0, 40));
            g.drawString("You were", 740, 400);
            g.drawString("caught!", 740, 450);
            g.setFont(new Font("", 0, 20));
            g.drawString("Total time:", 740, 550);
            g.drawString(String.valueOf(minutes) + " minutes " + String.valueOf(seconds) + " seconds", 740, 600);
        }
    }
    
    public final void PrintGUIMessage(String message){
        // Paint a message on the screen to give info to the player (and help me debug...)
        Graphics g = getGraphics();
        g.setFont(new Font("", 0, 40));
        g.drawString("Enemy status:", 740, 80);
        g.setFont(new Font("", 0, 60));
        switch (message){
            case "normal": 
                g.drawImage(blank, 740, 100, 200, 200, null); // Paint over the previous message
                g.setColor(Color.BLACK);
                g.drawString("Normal", 740, 160);
                break;
            case "alert":
                g.drawImage(blank, 740, 100, 200, 200, null);
                g.setColor(Color.RED);
                g.drawString("ALERT", 740, 160);
                g.setColor(Color.BLACK); // Reset the colour?
                break;
            case "search":
                g.drawImage(blank, 740, 100, 200, 200, null);
                g.setColor(Color.ORANGE);
                g.drawString("Search", 740, 160);
                g.setColor(Color.BLACK);
                break;
            case "caution":
                g.drawImage(blank, 740, 100, 200, 200, null);
                g.setColor(Color.YELLOW);
                g.drawString("Caution", 740, 160);
                g.setColor(Color.BLACK);
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    // Only move when player releases a key (prevents them from holding it down and flying across the screen, also means I don't have to resort to using sleeps between key presses)
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_UP:    // Up arrow key
                if (running) {
                    player.Move(1); // Move North
                }
                paintPlayer(getGraphics());
                break;
            case KeyEvent.VK_DOWN:
                if (running) {
                    player.Move(3);
                }
                paintPlayer(getGraphics());
                break;
            case KeyEvent.VK_LEFT:
                if (running) {
                    player.Move(4);
                }
                paintPlayer(getGraphics());
                break;
            case KeyEvent.VK_RIGHT:
                if (running) {
                    player.Move(2);
                }
                paintPlayer(getGraphics());
                break;
            case KeyEvent.VK_R:    // "R" for reset
                shouldreset = true; // If agent 'sees' this, reset pos
                try {
                    Thread.sleep(1000); // Longer, so it should happen after agent processing cycle has finished
                    shouldreset = false; // Don't keep resetting forever
                    break;
                } catch (InterruptedException ex) {
                    Logger.getLogger(MazeView.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }
}