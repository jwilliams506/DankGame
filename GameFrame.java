


import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jdk.nashorn.internal.runtime.regexp.joni.ast.BackRefNode;
import statics.Platform;
import characters.Gnome;
import characters.Player;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.FPSAnimator;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

@SuppressWarnings("serial")
class GameFrame extends JFrame implements GLEventListener, KeyListener, MouseListener, MouseMotionListener, ActionListener {
    class objModel {
        public FloatBuffer vertexBuffer;
        public IntBuffer faceBuffer;
        public FloatBuffer normalBuffer;
        public Point3f center;
        public int num_verts;		// number of vertices
        public int num_faces;		// number of triangle faces
        ArrayList<Vector2f> tex_coords = null;
        ArrayList<Vector3f> tex_loc = null;
        ArrayList<Point3f> input_verts = null;
        ArrayList<Vector3f> input_faces = null;
        ArrayList<Vector3f> input_norms = null;
        float minx, miny, minz;
        float maxx, maxy, maxz;

        public void Draw(Texture tex) {
            vertexBuffer.rewind();
            normalBuffer.rewind();
            faceBuffer.rewind();
            if(!tex_coords.isEmpty()){
                tex.bind();
                tex.enable();
                for(int i = 0; i < input_faces.size(); i++){
                    gl.glMatrixMode(GL.GL_TEXTURE);
                    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
                    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
                    gl.glEnable(GL.GL_LIGHTING);
                    gl.glBegin(GL.GL_TRIANGLES);
                    gl.glNormal3f(input_norms.get((int)input_faces.get(i).x).x,input_norms.get((int)input_faces.get(i).x).y,input_norms.get((int)input_faces.get(i).x).z);
                    gl.glTexCoord2f(tex_coords.get((int)tex_loc.get(i).x).x, tex_coords.get((int)tex_loc.get(i).x).y);
                    gl.glVertex3f(input_verts.get((int)input_faces.get(i).x).x, input_verts.get((int)input_faces.get(i).x).y, input_verts.get((int)input_faces.get(i).x).z);

                    gl.glNormal3f(input_norms.get((int)input_faces.get(i).y).x,input_norms.get((int)input_faces.get(i).y).y,input_norms.get((int)input_faces.get(i).y).z);
                    gl.glTexCoord2f(tex_coords.get((int)tex_loc.get(i).y).x, tex_coords.get((int)tex_loc.get(i).y).y);
                    gl.glVertex3f(input_verts.get((int)input_faces.get(i).y).x, input_verts.get((int)input_faces.get(i).y).y, input_verts.get((int)input_faces.get(i).y).z);

                    gl.glNormal3f(input_norms.get((int)input_faces.get(i).z).x,input_norms.get((int)input_faces.get(i).z).y,input_norms.get((int)input_faces.get(i).z).z);
                    gl.glTexCoord2f(tex_coords.get((int)tex_loc.get(i).z).x, tex_coords.get((int)tex_loc.get(i).z).y);
                    gl.glVertex3f(input_verts.get((int)input_faces.get(i).z).x, input_verts.get((int)input_faces.get(i).z).y, input_verts.get((int)input_faces.get(i).z).z);

                    gl.glEnd();
                    gl.glMatrixMode(GL.GL_MODELVIEW);
                }
                tex.disable();
            }
            else{

                gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
                gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
                gl.glEnableClientState(GL.GL_COLOR_ARRAY);

                gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertexBuffer);
                gl.glNormalPointer(GL.GL_FLOAT, 0, normalBuffer);

                gl.glDrawElements(GL.GL_TRIANGLES, num_faces*3, GL.GL_UNSIGNED_INT, faceBuffer);

                float lmodel_ambient[] = { 1, 0, 0, 1 };
                gl.glLightModelfv( GL.GL_LIGHT_MODEL_COLOR_CONTROL, lmodel_ambient, 0);
                gl.glLightModeli( GL.GL_LIGHT_MODEL_TWO_SIDE, 1 );

                gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
                gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
                gl.glDisableClientState(GL.GL_COLOR_ARRAY);
            }
        }

        public objModel(String filename) {
            /* load a triangular mesh model from a .obj file */
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(filename));
            } catch (IOException e) {
                System.out.println("Error reading from file " + filename);
                System.exit(0);
            }

            center = new Point3f();			
            float x, y, z;
            int v1, v2, v3;
            float bbx, bby, bbz;
            minx = miny = minz = 10000.f;
            maxx = maxy = maxz = -10000.f;

            String line;
            String[] tokens;
            input_verts = new ArrayList<Point3f> ();
            input_faces = new ArrayList<Vector3f> ();
            input_norms = new ArrayList<Vector3f> ();
            tex_coords = new ArrayList<Vector2f>();
            tex_loc = new ArrayList<Vector3f>();
            try {
                while ((line = in.readLine()) != null) {
                    if (line.length() == 0)
                        continue;
                    switch(line.charAt(0)) {
                    case 'v':
                        tokens = line.split("[ ]+");
                        x = Float.valueOf(tokens[1]);
                        y = Float.valueOf(tokens[2]);
                        z = Float.valueOf(tokens[3]);
                        minx = Math.min(minx, x);
                        miny = Math.min(miny, y);
                        minz = Math.min(minz, z);
                        maxx = Math.max(maxx, x);
                        maxy = Math.max(maxy, y);
                        maxz = Math.max(maxz, z);
                        input_verts.add(new Point3f(x, y, z));
                        center.add(new Point3f(x, y, z));
                        break;
                    case 't':
                        tokens = line.split("[ ]+");
                        x = Float.valueOf(tokens[1]);
                        y = Float.valueOf(tokens[2]);
                        tex_coords.add(new Vector2f(x,1-y));
                        break;
                    case 'f':
                        if(line.contains("/")){
                            tokens = line.split(" ");
                            String[] temp = tokens[1].split("/");
                            v1 = Integer.valueOf(temp[0])-1;
                            x = Integer.valueOf(temp[1])-1;
                            temp = tokens[2].split("/");
                            v2 = Integer.valueOf(temp[0])-1;
                            y = Integer.valueOf(temp[1])-1;
                            temp = tokens[3].split("/");
                            v3 = Integer.valueOf(temp[0])-1;
                            z = Integer.valueOf(temp[1])-1;
                            input_faces.add(new Vector3f(v1,v2,v3));
                            tex_loc.add(new Vector3f(x,y,z));
                            break;
                        }
                        else{
                            tokens = line.split("[ ]+");
                            v1 = Integer.valueOf(tokens[1])-1;
                            v2 = Integer.valueOf(tokens[2])-1;
                            v3 = Integer.valueOf(tokens[3])-1;
                            input_faces.add(new Vector3f(v1,v2,v3));
                            break;
                        }
                    default:
                        continue;
                    }
                }
                in.close();	
            } catch(IOException e) {
                System.out.println("Unhandled error while reading input file.");
            }

            System.out.println("Read " + input_verts.size() +
                    " vertices and " + input_faces.size() + " faces.");

            center.scale(1.f / (float) input_verts.size());

            bbx = maxx - minx;
            bby = maxy - miny;
            bbz = maxz - minz;
            float bbmax = Math.max(bbx, Math.max(bby, bbz));

            for (Point3f p : input_verts) {

                p.x = (p.x - center.x) / bbmax;
                p.y = (p.y - center.y) / bbmax;
                p.z = (p.z - center.z) / bbmax;
            }
            center.x = center.y = center.z = 0.f;

            /* estimate per vertex average normal */
            int i;
            for (i = 0; i < input_verts.size(); i ++) {
                input_norms.add(new Vector3f());
            }

            Vector3f e1 = new Vector3f();
            Vector3f e2 = new Vector3f();
            Vector3f tn = new Vector3f();
            for (i = 0; i < input_faces.size(); i ++) {
                v1 = (int)input_faces.get(i).x;
                v2 = (int)input_faces.get(i).y;
                v3 = (int)input_faces.get(i).z;

                e1.sub(input_verts.get(v2), input_verts.get(v1));
                e2.sub(input_verts.get(v3), input_verts.get(v1));
                tn.cross(e1, e2);
                input_norms.get(v1).add(tn);

                e1.sub(input_verts.get(v3), input_verts.get(v2));
                e2.sub(input_verts.get(v1), input_verts.get(v2));
                tn.cross(e1, e2);
                input_norms.get(v2).add(tn);

                e1.sub(input_verts.get(v1), input_verts.get(v3));
                e2.sub(input_verts.get(v2), input_verts.get(v3));
                tn.cross(e1, e2);
                input_norms.get(v3).add(tn);			
            }

            /* convert to buffers to improve display speed */
            for (i = 0; i < input_verts.size(); i ++) {
                input_norms.get(i).normalize();
            }

            vertexBuffer = BufferUtil.newFloatBuffer(input_verts.size()*3);
            normalBuffer = BufferUtil.newFloatBuffer(input_verts.size()*3);
            faceBuffer = BufferUtil.newIntBuffer(input_faces.size()*3);

            for (i = 0; i < input_verts.size(); i ++) {
                vertexBuffer.put(input_verts.get(i).x);
                vertexBuffer.put(input_verts.get(i).y);
                vertexBuffer.put(input_verts.get(i).z);
                normalBuffer.put(input_norms.get(i).x);
                normalBuffer.put(input_norms.get(i).y);
                normalBuffer.put(input_norms.get(i).z);			
            }

            for (i = 0; i < input_faces.size(); i ++) {
                faceBuffer.put((int) input_faces.get(i).x);
                faceBuffer.put((int) input_faces.get(i).y);	
                faceBuffer.put((int) input_faces.get(i).z);	
            }			
            num_verts = input_verts.size();
            num_faces = input_faces.size()/3;
        }		
    }

    /* GL, display, model transformation, and mouse control variables */
    private final GLCanvas canvas;
    private GL gl;
    private final GLU glu = new GLU();	
    private FPSAnimator animator;

    private Player player = new Player(new Vector3f(2,6,0), .1f, .4f);

    private int winW = 1000, winH = 800;
    private boolean wireframe = false;
    private boolean cullface = true;
    private boolean flatshade = false;	
    private boolean jump = false;
    private boolean isMoving = true;
    private boolean rightHeld = false;
    private boolean leftHeld = false;
    private boolean justHitLeft = false;
    private boolean justHitRight = false;

    private float xpos = 0, ypos = -1, zpos = 0;
    private float charx = 0, chary = 2, charz = 0;
    private float centerx = 7f, centery = -5f, centerz = -5f;
    private float roth = 0, rotv = 0;
    private float znear, zfar;
    private int mouseX, mouseY, mouseButton;
    private float motionSpeed, rotateSpeed;
    private float animation_speed = 1.0f;
    int shadowMapWidth = 1024; int shadowMapHeight = 1024;

    private objModel background = new objModel("platform.obj");
    private Platform backgroundShape = new Platform(new Vector3f(0f,0f,0f));
    private objModel hero = new objModel("hero.obj");
    private objModel flag = new objModel("flag.obj");
    private objModel livesObj = new objModel("lives.obj");
    private objModel gnome = new objModel("gnomeTex.obj");

    private float heroRad = (Math.abs(hero.maxx)+Math.abs(hero.minx))/2;

    private float xmin = -1f, ymin = -1f, zmin = -1f;
    private float xmax = 1f, ymax = 1f, zmax = 1f;	
    private float direction = 1;
    private int backSize = 320;
    private int lives = 2;
    float deathArea;
    float startX, startY;
    ArrayList<Vector4f> objects = null;
    ArrayList<Vector4f> enemies = null;
    Vector4f goal = new Vector4f(9999,9999,9999,9999);

    // Loaded lists of objects to be drawn
    // Position lists
    private List<Platform> platforms = new ArrayList<Platform>();
    private List<Gnome> gnomes = new ArrayList<Gnome>();
    // Object model lists
    private List<objModel> platform = new ArrayList<objModel>();
    private List<objModel> gnomeShapes = new ArrayList<objModel>();

    // Menu Items
    private JMenuItem load, exitStart, exitGame;
    private JMenuBar menuBar;
    private JMenu file;
    private boolean goFast = true;

    public void display(GLAutoDrawable drawable) {
        try{
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, wireframe ? GL.GL_LINE : GL.GL_FILL);	
            gl.glShadeModel(flatshade ? GL.GL_FLAT : GL.GL_SMOOTH);		
            if (cullface)
                gl.glEnable(GL.GL_CULL_FACE);
            else
                gl.glDisable(GL.GL_CULL_FACE);		

            gl.glLoadIdentity();

            player.setVelY(player.getVelY()-player.getGravity());

            if(isMoving && (rightHeld || leftHeld)){
                if(approxZero(player.getVelX()))
                    player.setVelX(.06f*direction);
                else if(player.getVelX() > 0){
                    if(leftHeld)
                        player.setVelX(player.getVelX()*.2f);
                    else
                        if(player.getVelX()*1.03 < player.getMoveSpeed())
                            player.setVelX(player.getVelX()*1.06f);
                }
                else if(player.getVelX() < 0){
                    if(rightHeld)
                        player.setVelX(player.getVelX()*.2f);
                    else
                        if(player.getVelX()*1.03 > -player.getMoveSpeed())
                            player.setVelX(player.getVelX()*1.06f);
                }
            }
            else if(isMoving)
                player.setVelX(player.getVelX()*.93f);
            isCollision();
            drawObjects();

            setCamera(gl, glu, 16);
        }
        catch(GLException e){

        }
    }	

    float[] black = {0,0,0};
    float[] dim = {.2f,.2f,.2f};
    float[] white = {1,1,1};

    private boolean approxZero(float vel){
        if(.00001 > vel && -.00001 < vel)
            return true;
        return false;
    }

    private void drawObjects(){
        gl.glPushMatrix();

        float[] rgba = {1f, 1f, 1f};
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, rgba, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, rgba, 0);
        gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, 0.5f);

        // Draw the level boxes
        for (int i = 0; i < platforms.size(); i++) {
            gl.glLoadIdentity();
            gl.glTranslatef(objects.get(i).x+2.5f, objects.get(i).w - .2f,0);
            gl.glScalef(5, 2.5f, 1);
            platform.get(i).Draw(sand);
        }

        for (int i = 0; i < enemies.size(); i++) {
            gl.glLoadIdentity();
            gl.glTranslatef(enemies.get(i).x, objects.get(i).w+1,0);
            gl.glRotatef(-50, 0, 1, 0);
            gnome.Draw(gnomeTexture);
        }

        for (float i = 0; i <= lives; i++){
            gl.glLoadIdentity();
            gl.glTranslatef(player.getX()+4, player.getY()+3, 0);
            gl.glRotatef(180f, 0f, 0f, 0f);
            gl.glRotatef(-22.5f, 1f, 0f, 0f);
            gl.glEnable(GL.GL_TEXTURE_2D);

            gl.glBindTexture(GL.GL_TEXTURE_2D, livesTex.getTextureObject());

            gl.glBegin(GL.GL_QUADS);

            gl.glTexCoord2f(0.0f, 0.0f);
            gl.glVertex2f(i/4, 0);       // (0,0)

            gl.glTexCoord2f(1.0f, 0.0f);
            gl.glVertex2f((i+1)/4, 0);     // (1,0) 

            gl.glTexCoord2f(1.0f, 1.0f);
            gl.glVertex2f((i+1)/4, 1f/4f);    // (1,1)

            gl.glTexCoord2f(0.0f, 1.0f);
            gl.glVertex2f(i/4, 1f/4f);   // (0,1)
            gl.glEnd();

            gl.glDisable(GL.GL_TEXTURE_2D);

            gl.glLoadIdentity();
        }

        gl.glLoadIdentity();
        gl.glTranslatef(goal.x, goal.w - 1.5f,0);
        gl.glScalef(5, 2.5f, 1);
        flag.Draw(flagTex);

        gl.glLoadIdentity();
        gl.glTranslatef(player.getX(), player.getY(), player.getZ());
        gl.glRotatef(125, 0, 1f, 0);
        gl.glRotatef(180,1f,0,0);
        gl.glRotatef(15,0,0,1f);
        hero.Draw(heroTex);

        gl.glLoadIdentity();

        gl.glTranslatef(player.getX()+240, player.getY()+23, backgroundShape.getZ());
        gl.glRotatef(180f, 0f, 0f, 0f);
        gl.glRotatef(-22.5f, 1f, 0f, 0f);
        gl.glEnable(GL.GL_TEXTURE_2D);

        gl.glBindTexture(GL.GL_TEXTURE_2D, backTex.getTextureObject());

        gl.glBegin(GL.GL_QUADS);

        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex2f(0, 0);       // (0,0)

        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex2f(backSize, 0);     // (1,0) 

        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex2f(backSize, backSize);    // (1,1)

        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex2f(0, backSize);   // (0,1)
        gl.glEnd();

        gl.glDisable(GL.GL_TEXTURE_2D);

        gl.glLoadIdentity();

        gl.glTranslatef(player.getX()+200, player.getY()+25, backgroundShape.getZ()-10);
        gl.glRotatef(180f, 0f, 0f, 0f);
        gl.glRotatef(-22.5f, 1f, 0f, 0f);
        gl.glEnable(GL.GL_TEXTURE_2D);

        gl.glBindTexture(GL.GL_TEXTURE_2D, staticBackTex.getTextureObject());

        gl.glBegin(GL.GL_QUADS);

        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex2f(0f, 0f);       // (0,0)

        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex2f(400, 0);     // (1,0) 

        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex2f(400,200);    // (1,1)

        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex2f(0, 200);   // (0,1)
        gl.glEnd();

        gl.glDisable(GL.GL_TEXTURE_2D);

        gl.glFlush();

    }

    private void endGame(){
        if (lives == 2)
            playSound("NEVER DONE THAT.wav", false);
        if (lives == 1)
            playSound("WhatchaSay.wav", false);
        if (lives == 0)
            playSound("Oh_Baby_A_Triple.wav", false);
        if(lives > 0){
            playSound("HITMARKER.wav", false);
            lives--;
            player.setX(startX);
            player.setY(startY);
            player.setVelX(0);
            player.setVelY(0);
        } 
        else{
            playSound("2SED4AIRHORN.wav", false);
            JOptionPane.showMessageDialog(new JFrame(),
                    "GAME OVER",
                    "2 sad 5 me",
                    JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
    }

    private void isCollision(){
        float minXCol = player.getX()+player.getVelX()-heroRad/2;
        float maxXCol = player.getX()+player.getVelX()*direction+heroRad/2;
        float minYCol = player.getY()+player.getVelY()- heroRad/2;
        float maxYCol = player.getY()+player.getVelY()+heroRad/2;
        boolean noX = false;
        boolean noY = false;
        float xmin;
        float xmax;
        float ymin; 
        float ymax;

        if((maxXCol > goal.x && maxXCol < goal.y)){ //|| (minXCol < enemies.get(i).y && minXCol > enemies.get(i).x))
            if(player.getY() > goal.z && player.getY() < goal.w){
                playSound("MOM GET THE CAMERA.wav", false);
                playSound("wow-.wav", true);
                JOptionPane.showMessageDialog(new JFrame(),
                        "YOU WIN. YOU ARE TRUE MLG QUICKSCOPER",
                        "w00t",
                        JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }
        }

        for(int i = 0; i < enemies.size(); i ++){
            if((maxXCol > enemies.get(i).x && maxXCol < enemies.get(i).y)){ //|| (minXCol < enemies.get(i).y && minXCol > enemies.get(i).x))
                if(player.getY() > enemies.get(i).z && player.getY() < enemies.get(i).w){
                    endGame();
                }
            }
        }
        for(int i = 0; i < objects.size(); i ++){
            xmin = objects.get(i).x;
            xmax = objects.get(i).y;
            ymin = objects.get(i).z;
            ymax = objects.get(i).w;
            if((minXCol > xmin && maxXCol < xmax)){ //|| ((minXCol > xmin && minXCol < xmax) || (maxXCol > xmin && maxXCol < xmax))){
                if( minYCol > ymin && minYCol < ymax){
                    //bottom
                    //if(ymax - .3 < minYCol){
                    player.setVelY(0);
                    player.setY(ymax+heroRad/2);
                    //noY = true;}
                }
                else if(maxYCol < ymax && maxYCol > ymin){
                    //top
                    player.setVelY(0);
                    player.setY(ymin-heroRad/2);
                    noY = true;
                }
            }
        }
        for(int i = 0; i < objects.size(); i ++){
            xmin = objects.get(i).x;
            xmax = objects.get(i).y;
            ymin = objects.get(i).z;
            ymax = objects.get(i).w;
            if(player.getY() > ymin && player.getY() < ymax){
                if(direction == 1){
                    if(maxXCol < xmax && maxXCol > xmin){
                        //right
                        player.setVelX(0);
                        player.setX(xmin-heroRad/2);
                        noX = true;
                    }
                }
                else{
                    if(minXCol > xmin && minXCol  < xmax){
                        //left
                        player.setVelX(0);
                        player.setX(xmax+heroRad/2);
                        noX = true;
                    }
                }
            }
        }

        if(minYCol<deathArea)
            endGame();

        if(!noY)
            player.setY(player.getY()+player.getVelY());
        if(!noX)
            player.setX(player.getX() + player.getVelX());
    }

    private void load(boolean init) {
        gnomes.clear();
        gnomeShapes.clear();
        platforms.clear();
        platform.clear();

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

        if (init)
            load = fileNames[0];
        try {
            Scanner scan = new Scanner(loadLevel);
            float x, y; 
            float xf, yf;
            float bx = -1, by = -1, sx = 1801, sy = 721;

            while(scan.hasNext()) {
                String line = scan.nextLine();
                StringBuilder lineStr = new StringBuilder(line); 
                StringBuilder xy = new StringBuilder(lineStr.substring(lineStr.indexOf("[")+1, lineStr.indexOf("]")));

                x = Float.parseFloat(xy.substring(0, xy.indexOf(",")))/10;
                y = Float.parseFloat(xy.substring(xy.indexOf(",") + 1))/10;

                if (x < sx)
                    sx = x;
                if (x > bx)
                    bx = x;
                if (y < sy)
                    sy = y;
                if (y > by)
                    by = y;

                if (line.contains("p")) {//TODO: make not suck, put scales back in
                    Platform tempPlatform = new Platform(new Vector3f(x, y, 0f));
                    platforms.add(tempPlatform);
                    platform.add(new objModel("platform.obj"));
                    objects.add(new Vector4f(x-3f, x+3f, y-1.25f-3, y+1.25f-3));
                } else if (line.contains("s")) {
                    startX = x;
                    startY = y;
                    player = new Player(new Vector3f(x, y, 0), .1f, .4f);
                } else if (line.contains("e")) {
                    goal = new Vector4f(x-2,x+2,y,y+5);
                } else if (line.contains("g")) {
                    Gnome gnome = new Gnome(new Vector3f(x, y, 0));
                    gnomes.add(gnome);
                    gnomeShapes.add(new objModel("gnomeTex.obj"));
                    enemies.add(new Vector4f(x-3, x-2, y, y+2));
                }   
            }
            deathArea = sy-10;
            scan.close();

            backgroundShape = new Platform(new Vector3f((( bx + sx ) / 2), (( by + sy ) / 2), -150));

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    final ActionListener fileMenu = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == load) {
                File files = new File("levels/");
                int num = files.listFiles().length;
                if (num == 0) {
                    JOptionPane.showMessageDialog(new JFrame(), "No saved levels!", "Load Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    load(false);
                }
            } else if(e.getSource() == exitStart) {
                dispose();
                new MainFrame();
            } else if(e.getSource() == exitGame) {
                System.exit(0);
            }
        }
    };

    IntBuffer frameBuffer; IntBuffer depthTex;
    ByteBuffer pixels;

    private void makeShadowMap(){
        //does nothing atm, book
        FloatBuffer tmp = BufferUtil.newFloatBuffer(16);
        IntBuffer viewport = BufferUtil.newIntBuffer(4);
        FloatBuffer light = BufferUtil.newFloatBuffer(4);
        gl.glGetLightfv(GL.GL_LIGHT0, GL.GL_POSITION, light);
        gl.glGetIntegerv(GL.GL_VIEWPORT, viewport);//lights viewport?
        gl.glViewport(0, 0, shadowMapWidth, shadowMapHeight);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluPerspective(45, winH/winW, 1, 1000);
        gl.glMatrixMode(GL.GL_MODELVIEW);

        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluLookAt(light.get(0), light.get(1), light.get(2), charx, chary, charz, 0, 1, 0);
        drawObjects();
        gl.glPopMatrix();

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);

        gl.glCopyTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_DEPTH_COMPONENT, 0, 0, shadowMapWidth, shadowMapHeight, 0);
        gl.glViewport(viewport.get(0),viewport.get(1),viewport.get(2),viewport.get(3));

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glTranslatef(.5f, .5f, 0f);
        gl.glScalef(.5f,.5f,1f);
        glu.gluPerspective(45, winH/winW, 1, 1000);
        glu.gluLookAt(light.get(0), light.get(1), light.get(2), charx, chary, charz, 0, 1, 0);
        gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, tmp);
        gl.glPopMatrix();
        //transposeMatrix(tmp);
        gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR);
        gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR);
        gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR);
        gl.glTexGeni(GL.GL_Q, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR);

        FloatBuffer test = BufferUtil.newFloatBuffer(4);
        for(int i = 0; i < 4; i++)
            test.put(i, tmp.get(i));
        gl.glTexGenfv(GL.GL_S, GL.GL_OBJECT_PLANE, test);
        for(int i = 0; i < 4; i++)
            test.put(i, tmp.get(i+4));
        gl.glTexGenfv(GL.GL_T, GL.GL_OBJECT_PLANE, test);
        for(int i = 0; i < 4; i++)
            test.put(i, tmp.get(i+8));
        gl.glTexGenfv(GL.GL_R, GL.GL_OBJECT_PLANE, test);
        for(int i = 0; i < 4; i++)
            test.put(i, tmp.get(i+12));
        gl.glTexGenfv(GL.GL_Q, GL.GL_OBJECT_PLANE, test);

        gl.glEnable(GL.GL_TEXTURE_GEN_S);
        gl.glEnable(GL.GL_TEXTURE_GEN_T);
        gl.glEnable(GL.GL_TEXTURE_GEN_R);
        gl.glEnable(GL.GL_TEXTURE_GEN_Q);

        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_COMPARE_FUNC, GL.GL_LEQUAL);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_DEPTH_TEXTURE_MODE, GL.GL_LUMINANCE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_COMPARE_MODE, GL.GL_COMPARE_R_TO_TEXTURE);
        gl.glEnable(GL.GL_TEXTURE_2D);

        gl.glDisable(GL.GL_TEXTURE_GEN_S);
        gl.glDisable(GL.GL_TEXTURE_GEN_T);
        gl.glDisable(GL.GL_TEXTURE_GEN_R);
        gl.glDisable(GL.GL_TEXTURE_GEN_Q);
    }

    private void setCamera(GL gl, GLU glu, float distance) {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

        float widthHeightRatio = (float) getWidth() / (float) getHeight();
        glu.gluPerspective(60, widthHeightRatio, 1, 1000);


        glu.gluLookAt(player.getX(), distance/6+player.getY(), distance/2.5+player.getZ(), player.getX(), player.getY(), player.getZ(), 0, 1, 0);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public GameFrame() {
        super("390 Final");
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setUndecorated(true);
        canvas = new GLCanvas();
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        animator = new FPSAnimator(canvas, 60);
        getContentPane().add(canvas);
        setSize(winW, winH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.jpg")));

        // Build the menu bar
        menuBar = new JMenuBar();

        // File
        file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        menuBar.add(file);

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

        // Set Action Listeners
        load.addActionListener(fileMenu);
        exitStart.addActionListener(fileMenu);
        exitGame.addActionListener(fileMenu);

        setJMenuBar(menuBar);

        animator.start();
        canvas.requestFocus();
        playSound("music.wav", true);
    }

    float light0_position[] = { 10, 20, 20};
    FloatBuffer lightPos;
    Texture gnomeTexture, sand, heroTex, backTex, staticBackTex, flagTex, livesTex;
    public void init(GLAutoDrawable drawable) {
        gl = drawable.getGL();

        initViewParameters();
        gl.glClearColor(.1f, .1f, .1f, 1f);
        gl.glClearDepth(1.0f);

        // white light at the eye
        lightPos = BufferUtil.newFloatBuffer(3);
        lightPos.put(-10);
        lightPos.put(20);
        lightPos.put(10);
        float light0_diffuse[] = { 1, 1, 1, 1 };
        float light0_specular[] = { 1, 1, 1, 1 };
        gl.glLightfv( GL.GL_LIGHT0, GL.GL_POSITION, light0_position, 0);
        gl.glLightfv( GL.GL_LIGHT0, GL.GL_DIFFUSE, light0_diffuse, 0);
        gl.glLightfv( GL.GL_LIGHT0, GL.GL_SPECULAR, light0_specular, 0);

        //material
        float mat_ambient[] = { 0, 0, 0, 1 };
        float mat_specular[] = { .8f, .8f, .8f, 1 };
        float mat_diffuse[] = { .4f, .4f, .4f, 1 };
        float mat_shininess[] = { 128 };
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_AMBIENT, mat_ambient, 0);
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_SPECULAR, mat_specular, 0);
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_DIFFUSE, mat_diffuse, 0);
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_SHININESS, mat_shininess, 0);

        float bmat_ambient[] = { 0, 0, 0, 1 };
        float bmat_specular[] = { 0, .8f, .8f, 1 };
        float bmat_diffuse[] = { 0, .4f, .4f, 1 };
        float bmat_shininess[] = { 128 };
        gl.glMaterialfv( GL.GL_BACK, GL.GL_AMBIENT, bmat_ambient, 0);
        gl.glMaterialfv( GL.GL_BACK, GL.GL_SPECULAR, bmat_specular, 0);
        gl.glMaterialfv( GL.GL_BACK, GL.GL_DIFFUSE, bmat_diffuse, 0);
        gl.glMaterialfv( GL.GL_BACK, GL.GL_SHININESS, bmat_shininess, 0);

        float lmodel_ambient[] = { 0, 0, 0, 1 };
        gl.glLightModelfv( GL.GL_LIGHT_MODEL_AMBIENT, lmodel_ambient, 0);
        gl.glLightModeli( GL.GL_LIGHT_MODEL_TWO_SIDE, 1 );

        gl.glEnable( GL.GL_NORMALIZE );
        gl.glEnable( GL.GL_LIGHTING );
        gl.glEnable( GL.GL_LIGHT0 );
        gl.glEnable( GL.GL_LIGHT1 );
        gl.glEnable( GL.GL_LIGHT2 );

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LESS);
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
        gl.glCullFace(GL.GL_BACK);
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glShadeModel(GL.GL_SMOOTH);	

        try{
            InputStream stream = getClass().getResourceAsStream("makegnome.png");
            TextureData data = TextureIO.newTextureData(stream, false, "png");
            gnomeTexture = TextureIO.newTexture(data);

            stream = getClass().getResourceAsStream("sand.png");
            data = TextureIO.newTextureData(stream, false, "png");
            sand = TextureIO.newTexture(data);

            stream = getClass().getResourceAsStream("hero.png");
            data = TextureIO.newTextureData(stream, false, "png");
            heroTex = TextureIO.newTexture(data);

            stream = getClass().getResourceAsStream("background.png");
            data = TextureIO.newTextureData(stream, false, "png");
            backTex = TextureIO.newTexture(data);

            stream = getClass().getResourceAsStream("static_background.png");
            data = TextureIO.newTextureData(stream, false, "png");
            staticBackTex = TextureIO.newTexture(data);

            stream = getClass().getResourceAsStream("flag.png");
            data = TextureIO.newTextureData(stream, false, "png");
            flagTex = TextureIO.newTexture(data);

            stream = getClass().getResourceAsStream("lives.png");
            data = TextureIO.newTextureData(stream, false, "png");
            livesTex = TextureIO.newTexture(data);
        }
        catch(IOException i){
            i.printStackTrace();
        }

        gl.glEnable(GL.GL_TEXTURE_2D);
        objects = new ArrayList<Vector4f>();
        enemies = new ArrayList<Vector4f>();

        load(true);

        player.setVelY(0);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        winW = width;
        winH = height;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.f, (float)width/(float)height, znear, zfar);
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    public static synchronized void playSound(final String sound, boolean loop) {

        Runnable x = new Runnable() {
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(getClass().getResourceAsStream(sound));
                    clip.open(inputStream);
                    if (loop) {
                        clip.loop(Clip.LOOP_CONTINUOUSLY);
                    }
                    clip.start(); 
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        };         Thread thread = new Thread(x);
        thread.start();
    }

    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
        case KeyEvent.VK_Q:
            System.exit(0);
            break;      
        case KeyEvent.VK_RIGHT:
            //		    playSound("HITMARKER.wav", false);
            rightHeld = true;
            isMoving = true;
            direction = 1;
            break;      
        case KeyEvent.VK_LEFT:
            //	        playSound("HITMARKER.wav", false);
            leftHeld = true;
            isMoving = true;
            direction = -1;
            break;
        case KeyEvent.VK_SPACE:
        case KeyEvent.VK_UP:
            if(player.getVelY() == 0){
                playSound("airhorn.wav", false);
                jump = true;
                player.setVelY(player.getJumpSpeed());
            }
            break;
        default:
            break;
        }
        canvas.display();
    }

    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()){
        case KeyEvent.VK_RIGHT:
            rightHeld = false;
            break;      
        case KeyEvent.VK_LEFT:
            leftHeld = false;
            break;
        }
    }

    public void mousePressed(MouseEvent e) {	
        mouseX = e.getX();
        mouseY = e.getY();
        mouseButton = e.getButton();
        canvas.display();
    }

    public void mouseReleased(MouseEvent e) {
        mouseButton = MouseEvent.NOBUTTON;
        canvas.display();
    }	

    public void mouseDragged(MouseEvent e) {

    }


    /* computes optimal transformation parameters for OpenGL rendering.
     * this is based on an estimate of the scene's bounding box
     */	
    void initViewParameters()
    {
        roth = rotv = 0;

        float ball_r = (float) Math.sqrt((xmax-xmin)*(xmax-xmin)
                + (ymax-ymin)*(ymax-ymin)
                + (zmax-zmin)*(zmax-zmin)) * 0.707f;

        centerx = (xmax+xmin)/2.f;
        centery = (ymax+ymin)/2.f;
        centerz = (zmax+zmin)/2.f;
        xpos = centerx;
        ypos = centery;
        zpos = ball_r/(float) Math.sin(45.f*Math.PI/180.f)+centerz;

        znear = 0.01f;
        zfar  = 1000.f;

        motionSpeed = 0.002f * ball_r;
        rotateSpeed = 0.1f;

    }	

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) { }
    public void keyTyped(KeyEvent e) { }
    public void mouseMoved(MouseEvent e) { }
    public void actionPerformed(ActionEvent e) { }
    public void mouseClicked(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) {	}	
}
