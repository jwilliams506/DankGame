import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 
 * @author John Williams
 *
 */
@SuppressWarnings("serial")
public class EditorFrame extends JFrame {

    private JMenuBar menuBar;
    private JMenu file, edit, selectShape, selectEnemy;
    private JMenuItem save, load, exitStart, exitGame, undo, redo, background;
    private JRadioButtonMenuItem platform, player, goal, gnome;

    private final JPanel shapesPanel;

    private List<Shape> shapes = new ArrayList<Shape>();
    private Shape undoShape;
    private String undoString;
    private Shape playerShape;
    private Shape goalShape;
    private boolean undoCalled = false;
    private final int platformWidth = 50;
    private final int platformHeight = 25;
    private final int circleRadius = 25;
    private final int ovalHeight = 50;
    
    StringBuilder shapeStr = new StringBuilder();
    String playerStartStr = new String("");
    String goalStr = new String("");
    String selectedFileString = "Default";
    JFileChooser fileChooser = new JFileChooser();
    private JMenuItem mosaic;

    public EditorFrame() {
        // Initialize Frame
        super("Level Editor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setSize(1800, 720);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.jpg")));
        

        // Build the menu bar
        menuBar = new JMenuBar();

        // File
        file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        menuBar.add(file);

        // File - Save
        save = new JMenuItem("Save", KeyEvent.VK_S);
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        file.add(save);

        // File - Load
        load = new JMenuItem("Load", KeyEvent.VK_L);
        load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        file.add(load);

        file.addSeparator();

        // File - Exit to Start
        exitStart = new JMenuItem("Exit to Start", KeyEvent.VK_W);
        exitStart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        file.add(exitStart);

        // File - Exit Game
        exitGame = new JMenuItem("Exit Game", KeyEvent.VK_Q);
        exitGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        file.add(exitGame);

        // Edit
        edit = new JMenu("Edit");
        edit.setMnemonic(KeyEvent.VK_E);
        menuBar.add(edit);

        // Edit - Select Shape
        selectShape = new JMenu("Select Shape");

        // Edit - Select Shape - Square
        ButtonGroup shapeGroup = new ButtonGroup();
        platform = new JRadioButtonMenuItem("Square");
        platform.setSelected(true);
        shapeGroup.add(platform);
        selectShape.add(platform);
        
        // Edit - Select Shape - Player Start
        player = new JRadioButtonMenuItem("Player Start");
        player.setSelected(false);
        shapeGroup.add(player);
        selectShape.add(player);
        
        // Edit - Select Shape - Goal
        goal = new JRadioButtonMenuItem("Goal");
        goal.setSelected(false);
        shapeGroup.add(goal);
        selectShape.add(goal);
        
        // Edit - Select Shape - Enemies
        selectEnemy = new JMenu("Enemies");
        
        // Edit - Select Shape - Enemies - Gnome
        gnome = new JRadioButtonMenuItem("Gnome");
        gnome.setSelected(false);
        shapeGroup.add(gnome);
        selectEnemy.add(gnome);
        selectShape.add(selectEnemy);
        
        edit.add(selectShape);
        
        edit.addSeparator();
        
         // Edit - Select Background
        background = new JMenuItem("Choose Background Image");
        background.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        edit.add(background);
        
        mosaic = new JCheckBoxMenuItem("Make Background Mosaic");
        edit.add(mosaic);
       
        edit.addSeparator();

        // Edit - Undo
        undo = new JMenuItem("Undo");
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        edit.add(undo);
        
        // Edit - Redo
        redo = new JMenuItem("Redo");
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        edit.add(redo);
        
        setJMenuBar(menuBar);

        final Container contentPane = getContentPane();

        shapesPanel = new JPanel(){
            @Override
            public void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics;
                super.paintComponent(g);
                if(playerShape != null)
                    g.draw(playerShape);
                if(goalShape != null)
                    g.draw(goalShape);
                for (Shape shape: shapes) {
                    if (shape != null) 
                        g.draw(shape);
                }
            }

        };
        contentPane.add(shapesPanel, BorderLayout.CENTER);

        final ActionListener editMenu = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == undo && !shapes.isEmpty() && shapes.size() > 1) {
                    undoShape = shapes.get(shapes.size()-1);
                    shapes.remove(shapes.size()-1);
                    StringBuilder stb = new StringBuilder(shapeStr.substring(shapeStr.lastIndexOf("[")-2, shapeStr.length()-1));
                    undoString = stb.toString();
                    stb = new StringBuilder(shapeStr.substring(1, shapeStr.lastIndexOf("[")-2));
                    shapeStr = stb;
                    undoCalled = true;
                    shapesPanel.repaint();
                } else if (e.getSource() == redo && undoShape != null && undoCalled) {
                    shapes.add(undoShape);
                    shapeStr.append(undoString);
                    undoShape = null;
                    undoCalled = false;
                    shapesPanel.repaint();
                } else if (e.getSource() == background) {
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG File", "png");
                    fileChooser.addChoosableFileFilter(filter);
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    int returnVal = fileChooser.showOpenDialog(new JFrame());
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File selectFile = fileChooser.getSelectedFile();
                        File tmpFile = new File(selectFile.getAbsolutePath());
                        BufferedImage background, resizeImage;
                        File outFile, backup;
                        try {
                            background = ImageIO.read(tmpFile);
                            resizeImage = resizeImage(background, background.getType());
                            outFile = new File("background.png");
                            backup = new File("backup.png");
                            AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
                            transform.translate(-resizeImage.getWidth(null), 0);
                            AffineTransformOp transformOP = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                            resizeImage = transformOP.filter(resizeImage, null);
                            try {
                                ImageIO.write(resizeImage, "png", outFile);
                                ImageIO.write(resizeImage, "png", backup);
                            } catch(IOException exception) {
                            }
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                    }
                } else if (e.getSource() == mosaic) {
                    if (mosaic.isSelected()) {
                        File in = new File(getClass().getResource("background.png").getFile());
                        BufferedImage background, outImage;
                        try {
                            background = ImageIO.read(in);
                            Mosaic(background, background, "/memes");
                            File outfile = new File("background.png");
                            outImage = background;
                            ImageIO.write(outImage, "png", outfile);
                        } catch(IOException exception) {
                        }
                    } else {
                        BufferedImage backup;
                        File in = new File(getClass().getResource("backup.png").getFile());
                        File outfile = new File("background.png");
                        try {
                            backup = ImageIO.read(in);
                            ImageIO.write(backup, "png", outfile);
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                }
            }
        };
        
        final ActionListener fileMenu = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == save) {
                    if (playerStartStr.equals("")) {
                        JOptionPane.showMessageDialog(new JFrame(), "No player starting point selected!", "Save Error", JOptionPane.ERROR_MESSAGE);
                    } else if (goalStr.equals("")) {
                        JOptionPane.showMessageDialog(new JFrame(), "No goal point selected!", "Save Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        saveToLevelFormat();
                    }
                } else if (e.getSource() == load) {
                    File files = new File("levels/");
                    int num = files.listFiles().length;
                    if (num == 0) {
                        JOptionPane.showMessageDialog(new JFrame(), "No saved levels!", "Load Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        load();
                    }
                } else if(e.getSource() == exitStart) {
                    dispose();
                    new MainFrame();
                } else if(e.getSource() == exitGame) {
                    System.exit(0);
                }
            }
        };

        // Set ActionListeners
        
        // File
        save.addActionListener(fileMenu);
        load.addActionListener(fileMenu);
        exitStart.addActionListener(fileMenu);
        exitGame.addActionListener(fileMenu);
        
        // Edit
        mosaic.addActionListener(editMenu);
        background.addActionListener(editMenu);
        undo.addActionListener(editMenu);
        redo.addActionListener(editMenu);
       
        contentPane.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                int x = e.getX();
                int y = e.getY();

                if (platform.isSelected()){
                    Shape tempShape = new Rectangle2D.Double(x, y, platformWidth, platformHeight);
                    shapes.add(tempShape);
                    undoCalled = false;
                    shapeStr.append("p" + "[" + x + "," + y + "]");
                    shapeStr.append("\n");
                } else if (player.isSelected()) {
                    playerShape = new Ellipse2D.Double(x, y, circleRadius, circleRadius);
                    playerStartStr = "s" + "[" + x + "," + y + "]" + "\n";
                } else if (goal.isSelected()) {
                    goalShape = new Ellipse2D.Double(x, y, circleRadius, ovalHeight);
                    goalStr = "e" + "[" + x + "," + y + "]" + "\n";
                } else if (gnome.isSelected()) {
                    Shape tempShape = new Polygon(new int[]{x, x+10, x+5}, new int[]{y, y-10, y+5}, 3);
                    shapes.add(tempShape);
                    undoCalled = false;
                    shapes.add(tempShape);
                    shapeStr.append("g" + "[" + x + "," + y + "]" + "\n");
                    
                }
                shapesPanel.repaint();
            }
        });
    }
    
    private static BufferedImage resizeImage(BufferedImage originalImage, int type){
        BufferedImage resizedImage = new BufferedImage(2160, 1440, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, 1080, 720, null);
        g.dispose();

        return resizedImage;
    }
    
    private void saveToLevelFormat() {
        try {
            PrintWriter out = new PrintWriter("levels/level_" + numberOfLevels() + ".mlg");
            out.print(playerStartStr.toString());
            out.print(goalStr.toString());
            out.print(shapeStr.toString());
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void load() {
        File[] files;
        File fileList = new File("levels/");
        files = fileList.listFiles();
        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileNames[i] = files[i].getPath().substring(7);
        }
        String load = (String) JOptionPane.showInputDialog(new JFrame(), "Select Level:\n", "Load",
                JOptionPane.WARNING_MESSAGE, null, fileNames, fileNames[0]);
        
        File loadLevel = new File("levels/" + load);
        try {
            Scanner scan = new Scanner(loadLevel);
            shapes.clear();
            shapeStr = new StringBuilder();
            playerStartStr = "";
            goalStr = "";
            int x, y; 
            
            while(scan.hasNext()) {
                String line = scan.nextLine();
                StringBuilder lineStr = new StringBuilder(line); 
                StringBuilder xy = new StringBuilder(lineStr.substring(lineStr.indexOf("[")+1, lineStr.indexOf("]")));
                
                x = Integer.parseInt(xy.substring(0, xy.indexOf(",")));
                y = Integer.parseInt(xy.substring(xy.indexOf(",") + 1));
                
                if (line.contains("p")) {
                    Shape tempShape = new Rectangle2D.Double(x, y, platformWidth, platformHeight);
                    shapes.add(tempShape);
                    undoCalled = false;
                    shapeStr.append("p" + "[" + x + "," + y + "]");
                    shapeStr.append("\n");
                } else if (line.contains("s")) {
                    playerShape = new Ellipse2D.Double(x, y, circleRadius, circleRadius);
                    playerStartStr = "s" + "[" + x + "," + y + "]" + "\n";
                } else if (line.contains("e")) {
                    goalShape = new Ellipse2D.Double(x, y, circleRadius, ovalHeight);
                    goalStr = "e" + "[" + x + "," + y + "]" + "\n";
                } else if (line.contains("g")) {
                    Shape tempShape = new Polygon(new int[]{x, x+10, x+5}, new int[]{y, y-10, y+5}, 3);
                    shapes.add(tempShape);
                    undoCalled = false;
                    shapes.add(tempShape);
                    shapeStr.append("g" + "[" + x + "," + y + "]" + "\n");
                }
                shapesPanel.repaint();
            }
            scan.close();
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void Mosaic(BufferedImage src, BufferedImage dst, String mosaicfolder) {

        int width = src.getWidth();
        int height = src.getHeight();

        // load all mosaic images from the specified subfolder
        File folder = new File(ClassLoader.class.getResource(mosaicfolder).getFile());
        File files[] = folder.listFiles();

        int i;
        int w = 0, h = 0;
        int num = files.length;

        // mpixels stores the pixels of each mosaic image read from a disk file
        int[][] mpixels = new int[num][];

        for (i = 0; i < files.length; i ++) {
            if (!files[i].isFile()) continue;
            BufferedImage mosaic = null;
            try {
                mosaic = ImageIO.read(files[i]);
            } catch (IOException e) {
            }
            if (w == 0) {
                w = mosaic.getWidth();
                h = mosaic.getHeight();
            } else {
                if (mosaic.getWidth() != w || mosaic.getHeight() != h) {
                    System.out.println("mosaic images must be of the same size.");
                    System.exit(1);
                }
            }
            mpixels[i] = new int[w*h];

            // get pixels from the buffered image
            mosaic.getRGB(0, 0, w, h, mpixels[i], 0, w);
        }
        int[] pixels = new int[width * height];

        // get the pixels of the source image   
        src.getRGB(0, 0, width, height, pixels, 0, width);

        System.out.println("" + num + " mosaic images (" + w + "," + h + ") loaded.");
        float d_r = 0, d_g = 0, d_b = 0, d, a_r = 0, a_g = 0, a_b = 0,
                d_r_denom = 0, d_g_denom = 0, d_b_denom = 0,
                a_r_denom = 0, a_g_denom = 0, a_b_denom = 0;

        for (int x = 0; x < width; x += w) {
            for (int y = 0; y < height; y += h) {
                float dSmall = 1000000;
                int savedK = 0;
                float savedA_r = 0, savedA_g = 0, savedA_b = 0;
                int r, g, b, a;
                for (int k = 0; k < num; k++) {
                    d=0;
                    d_r = 0;
                    d_g = 0;
                    d_b = 0;
                    a_r = 0; 
                    a_g = 0; 
                    a_b = 0;
                    d_r_denom = 0; 
                    d_g_denom = 0; 
                    d_b_denom = 0;
                    a_r_denom = 0; 
                    a_g_denom = 0;
                    a_b_denom = 0;
                    for (i = 0; i < h; i++) {
                        for (int j = 0; j < w; j++) {
                            if (y*width+i*width+x+j < height*width) {
                                Color rgb = new Color(pixels[y*width+i*width+x+j]);
                                Color rgbm = new Color(mpixels[k][j*w+i]);

                                d_r       += rgb.getRed()    * rgbm.getRed();
                                d_r_denom += rgbm.getRed()   * rgbm.getRed();
                                d_g       += rgb.getGreen()  * rgbm.getGreen();
                                d_g_denom += rgbm.getGreen() * rgbm.getGreen();
                                d_b       += rgb.getBlue()   * rgbm.getBlue();
                                d_b_denom += rgbm.getBlue()  * rgbm.getBlue();

                                a_r       += d_r;
                                a_r_denom += d_r_denom;
                                a_g       += d_g;
                                a_g_denom += d_g_denom;
                                a_b       += d_b;
                                a_b_denom += d_b_denom;
                            }
                        }
                    }                       
                    d_r = -1 * d_r * d_r / d_r_denom;
                    d_g = -1 * d_g * d_g / d_g_denom;
                    d_b = -1 * d_b * d_b / d_b_denom;

                    a_r = a_r / a_r_denom;
                    a_g = a_g / a_g_denom;
                    a_b = a_b / a_b_denom;

                    d = d_r + d_g + d_b;

                    d *= (1 + (float)Math.random() * 2);
                    if (d < dSmall) {
                        dSmall = d;
                        savedK = k;
                        savedA_r = a_r;
                        savedA_g = a_g;
                        savedA_b = a_b;
                    }
                }
                for (i = 0; i < h; i++) {
                    for (int j = 0; j < w; j++) {
                        if (y*width+i*width+x+j < height*width) {
                            Color rgbm = new Color(mpixels[savedK][j*w+i]);

                            a = rgbm.getAlpha();
                            r = rgbm.getRed();
                            g = rgbm.getGreen();
                            b = rgbm.getBlue();
                            r = (int)(savedA_r > 1.0 ? 1.0 * r : r * savedA_r);
                            g = (int)(savedA_g > 1.0 ? 1.0 * g : g * savedA_g);
                            b = (int)(savedA_b > 1.0 ? 1.0 * b : b * savedA_b);
                            pixels[y*width+i*width+x+j] = new Color(r, g, b, a).getRGB(); 
                        }
                    }
                }
            }
        }
        dst.setRGB(0, 0, width, height, pixels, 0, width);
    }

    private int numberOfLevels() {
        File files = new File("levels/");
        int num = files.listFiles().length;
        return num;
    }


}