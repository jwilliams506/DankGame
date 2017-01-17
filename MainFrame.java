import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * 
 * @author John Williams
 *
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame{

    private JButton startGame;
    private JButton levelEditor;
    private JMenuBar menuBar;
    private JMenu file;
    private JMenuItem exitGame;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } 
        catch (UnsupportedLookAndFeelException e) {
        }
        catch (ClassNotFoundException e) {
        }
        catch (InstantiationException e) {
        }
        catch (IllegalAccessException e) {
        }

        new MainFrame();
    }
    public MainFrame()
    {
        super("DANK GAME");

        startGame = new JButton("Start Game");
        startGame.setFont(new Font("Impact", Font.PLAIN, 18));
        startGame.setBackground(Color.CYAN);

        levelEditor = new JButton("Level Editor");
        levelEditor.setFont(new Font("Impact", Font.PLAIN, 18));
        levelEditor.setBackground(Color.PINK);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.jpg")));
        
        // Build the menu bar
        menuBar = new JMenuBar();

        // File
        file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        menuBar.add(file);

        // File - Exit Game
        exitGame = new JMenuItem("Exit Game", KeyEvent.VK_Q);
        exitGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        file.add(exitGame);
        exitGame.addActionListener(handler);

        setJMenuBar(menuBar);

        Container c = getContentPane();
        GridLayout fl = new GridLayout(0, 1, 0, 0);
        c.setLayout(fl);
        setVisible(true);
        c.add (startGame);
        c.add (levelEditor);

        startGame.addActionListener(handler);
        levelEditor.addActionListener(handler);
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    final ActionListener handler = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == startGame) {
                File files = new File("levels/");
                int num = files.listFiles().length;
                if (num == 0) {
                    JOptionPane.showMessageDialog(new JFrame(), "No saved levels!", "Load Error", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    dispose();
                    new GameFrame();
                }
            }
            if (e.getSource() == levelEditor) {
                dispose();
                new EditorFrame();
            }
            if (e.getSource() == exitGame) {
                System.exit(0);
            }
        }
    };
}