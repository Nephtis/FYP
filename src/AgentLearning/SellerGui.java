package AgentLearning;

import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author Dave
 * NOTE: THIS IS AN EXAMPLE CLASS FOR MY OWN REFERENCE AND WILL NOT BE PART OF THE FINAL PRODUCT
 */
public class SellerGui extends JFrame{
    private SecondAgent myAgent;
    
    private JTextField titleField, priceField;
    
    SellerGui(SecondAgent a){
        // super() calls the parent constructor with no arguments?
        // Here it is used with arguments
        super(a.getLocalName());
        
        myAgent = a;
        
        // Draw the GUI
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 2));
        p.add (new JLabel("Title:"));
        titleField = new JTextField(15);
        p.add(titleField);
        p.add(new JLabel("Price:"));
        priceField = new JTextField(15);
        p.add(priceField);
        getContentPane().add(p, BorderLayout.CENTER);
        
        JButton addButton = new JButton("Add");
        // Add a listener to the add button to do something 
        // when an action is performed
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev){
                try{
                    // Get the user-entered data and put it into the catalogue
                    String title = titleField.getText().trim();
                    String price = priceField.getText().trim();
                    myAgent.updateCatalogue(title, Integer.parseInt(price));
                    // Clear the fields ready for more input
                    titleField.setText("");
                    priceField.setText("");
                } catch (Exception e){
                    JOptionPane.showMessageDialog(SellerGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        p = new JPanel();
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);
        
        // Make the agent terminate when the user closes
        // the GUI using the button on the upper right corner
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                myAgent.doDelete();
            }
        });
        setResizable(false);
    }
    
    public void showGui(){
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int) screenSize.getWidth() / 2;
        int centerY = (int) screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }
}
