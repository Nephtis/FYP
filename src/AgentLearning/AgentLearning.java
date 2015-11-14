package AgentLearning;

import jade.core.Agent;
import java.util.Scanner;

/**
 *
 * @author b2026323 - Dave Halperin
 */
public class AgentLearning {

    public static void main(String[] args) {

        int m;  // Rows
        int n;  // Columns
        GenerateMaze maze;
        
        // Removed ability to specify maze size, not really needed here...
        /*System.out.println("Enter the dimensions of the maze (m rows by n columns):");
        Scanner sc = new Scanner(System.in);
        System.out.print("m: ");
        m = sc.nextInt();
        Scanner sc2 = new Scanner(System.in);
        System.out.print("n: ");
        n = sc2.nextInt();
        System.out.println("Now generating...");
        */
        maze = new GenerateMaze(6, 6);
        System.out.println("Maze generated successfully.");
    }
}