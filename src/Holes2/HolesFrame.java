package Holes2;

import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.swing.Timer;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.table.DefaultTableModel;

//Group: Massi, Rachel and Brevin

//phaseOneFrame class extends JFrame and adds the component which contains most of the work in the frame
public class HolesFrame extends JFrame
{
    //A panel is created here to contain the buttons and the HolesComponent
    private JPanel buttonPanel;
    public HolesFrame()
    {
        //Buttons are created.
        JButton hallOfFameBtn = new JButton("Hall of Fame");
        hallOfFameBtn.setBackground(Color.red);
        JButton saveBtn = new JButton("Save");
        JButton muteBtn = new JButton("Mute");
        buttonPanel = new JPanel();
        
        //The panel's layout is set to BorderLayout to contain the buttons and the HoleComponent 3-1 layout
        buttonPanel.setLayout(new BorderLayout());
        HolesComponent holescomponent = new HolesComponent();
        
        //A table model is created so that it can take input from the text file
        DefaultTableModel model = new DefaultTableModel(); 
        JTable dataTable = new JTable(model); 

        // Create a couple of columns 
        model.addColumn("Name"); 
        model.addColumn("Score");
        
        // Creates the file and assigns it to the Scanner
        File file = new File("hall_of_fame.txt"); 
        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException ex) {
        }
        
        //Sets the ";" as delimiter and adds each word from the text file to the ArrayList
        sc.useDelimiter(";");
        ArrayList<String> dataList = new ArrayList<String>();
        while (sc.hasNext()){
            dataList.add((sc.next()).trim());
        }
        sc.close();
        
        Map<String, Integer> unsortedDataMap = new HashMap<String, Integer>();
        for (int i=0; i < dataList.size() - 1; i= i + 2) {
            int intNumber = Integer.parseInt(dataList.get(i+1));
            unsortedDataMap.put(dataList.get(i), intNumber); 
        }

        Map<String, Integer> sortedDataMap = sortByValue(unsortedDataMap);
        
        //Takes each item from the sortedDataMap and adds it as a row to the table
        modelMap(sortedDataMap, model);
        
        //All buttons and the component are added to the panel and then panel is added to the frame.
        buttonPanel.add(hallOfFameBtn, BorderLayout.WEST);
        buttonPanel.add(muteBtn, BorderLayout.CENTER);
        buttonPanel.add(saveBtn, BorderLayout.EAST);
        buttonPanel.add(holescomponent, BorderLayout.NORTH);
        
        add(buttonPanel);
        
        //The ActionListener for our save button: it uses BufferedWriter to write the name and the score once Save Button is clicked
        class SaveAction implements ActionListener {
            public void actionPerformed(ActionEvent event){
                try {
                    BufferedWriter tofile = new BufferedWriter(new FileWriter ("hall_of_fame.txt", true));
                    PrintWriter printwriter = new PrintWriter(tofile);
                    printwriter.println(holescomponent.name + "; " + Integer.toString(holescomponent.score) + "; ");
                    printwriter.close();
                    PrintWriter printwriterCurrent = new PrintWriter("current_game.txt");
                    printwriterCurrent.print(holescomponent.name + ";" + holescomponent.score + ";" + holescomponent.level + ";" + holescomponent.lives + ";" + holescomponent.score_multiplier + ";" + holescomponent.interval + ";" + holescomponent.rows_number + ";" + holescomponent.cols_number + ";");
                    printwriterCurrent.close();
                }   
                catch (IOException IOError) {}
                }
            }
 
        //Adds the above ActionListener to the save button
        SaveAction saveaction = new SaveAction();
        saveBtn.addActionListener(saveaction);        
        
        //ActionListener for mute button: it sets the mute off once clicked
        class MuteAction implements ActionListener {
            public void actionPerformed(ActionEvent event) {
                if (holescomponent.mute == false) {
                    holescomponent.mute = true;
            }
                else {
                    holescomponent.mute = false;
                }
            }        
        }
        
        //Adds the above ActionListener to the mute button
        MuteAction muteaction = new MuteAction();
        muteBtn.addActionListener(muteaction);        
        
        //ActionListener for the Hall of Fame button: it shows the JTable on the North of the layout
        class HallOfFameAction implements ActionListener {
            
            @Override
             public void actionPerformed(ActionEvent event){
                buttonPanel.remove(holescomponent);
                buttonPanel.add(dataTable, BorderLayout.NORTH);
                setSize(500, 350);
                buttonPanel.revalidate();
                buttonPanel.repaint();
                }
            }
        
        //Adds the above ActionListener to the HallOfFame button
        HallOfFameAction tableAction = new HallOfFameAction();
        hallOfFameBtn.addActionListener(tableAction);  
        
        pack();
    }
    
    //The method to sort a given map in descending order
    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortedMap) 
    {
        // Convert Map to List of Map
        LinkedList<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortedMap.entrySet());

        // Sort list with Collections.sort(), providing a Custom comparator for sorting it in descending order
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // Loop the sorted list and put it into a new insertion order map
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
    
    public <K, V> void modelMap(Map<K, V> map, DefaultTableModel model) {
        
        for (Map.Entry<K, V> entry : map.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }
}

//This class holds the component which does most of the GUI implementation
class HolesComponent extends JComponent implements KeyListener
{
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 500;
    public static int rows_number;
    public static int cols_number;
    private ArrayList<Ellipse2D> myEllipses = new ArrayList<>();
    private Ellipse2D myCurrentEllipse;
    public int score = 0;
    private String score_result = "Score: " + score;
    public int lives = 5;
    public int interval;
    private String lives_result = "Lives: " + lives;
    public int level = 1;
    private String level_result = "Level: " + level;
    public String name;
    private Timer timer;
    Graphics2D g2;
    public boolean mute = false;
    private boolean gameover = false;
    public String restoreState;
    public int score_multiplier = 1;
    
    //The ctor of the Component class adds the MouseListener to the component and takes the input from user
    //which later decides the number of rows and columns. If an integer equal or less than 0 or a non-integer
    //is entered, error will raise. 
    
    public HolesComponent()
    {
        addMouseListener(new MouseHandler());
        Scanner restoreInput = new Scanner(System.in);
        System.out.println("Would you like to restore from the last save game? (y/n)");
        restoreState = restoreInput.nextLine();
        if (restoreState.equals("y")) {
            File file = new File("current_game.txt");
            Scanner scCurrent = null;
            try {
                scCurrent = new Scanner(file);
            } catch (FileNotFoundException ex) {}
            scCurrent.useDelimiter(";");
            ArrayList<String> dataList = new ArrayList<String>();
            while (scCurrent.hasNext()) {
                dataList.add(scCurrent.next());
            }
            scCurrent.close();
            name = dataList.get(0);
            score = Integer.parseInt(dataList.get(1));
            level = Integer.parseInt(dataList.get(2));
            lives = Integer.parseInt(dataList.get(3));
            score_multiplier = Integer.parseInt(dataList.get(4));
            interval = Integer.parseInt(dataList.get(5));
            rows_number = Integer.parseInt(dataList.get(6));
            cols_number = Integer.parseInt(dataList.get(7));
            score_result = "Score: " + score;
            level_result = "Level: " + level;
            lives_result = "Lives: " + lives;
        }
        else {
        Scanner rowInput = new Scanner(System.in);
        System.out.println("Enter the number of rows (in positive integer)");
        String rows = rowInput.nextLine();
        Scanner colInput = new Scanner(System.in);
        System.out.println("Enter the number of columns (in positive integer)");
        String cols = colInput.nextLine();
        Scanner nameInput = new Scanner(System.in);
        System.out.println("What is your name?");
        name = nameInput.nextLine();
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        try {       
            rows_number = Integer.parseInt(rows);
            cols_number = Integer.parseInt(cols);
            if (rows_number <= 0 || cols_number <= 0){
                System.err.println("The number must be above 0.");
                rows_number = 0;
                cols_number = 0;              
            }
        } catch (NumberFormatException e) {
            System.err.println("It must be a valid positive integer.");
            }
        
        //It takes the input from the user about the interval in seconds
        Scanner intervalInput = new Scanner(System.in);
        System.out.println("Enter your desired interval (in seconds)");
        interval = intervalInput.nextInt() * 1000;
        }
        //It sets the timer with the number of seconds and the actionlistener as parameters
        timer = new Timer(interval ,new taskPerformer());
        timer.start();
        
        //Create the hall_of_fame.txt, if it's not there
        File file = new File("hall_of_fame.txt");
        try {
            
        if (file.createNewFile()) {
           file.createNewFile(); 
        }
                }
       
        catch (IOException IOError) {
        }
        File fileCurrent = new File("current_game.txt");
        try {
            if (fileCurrent.createNewFile()); {
                fileCurrent.createNewFile();
        }
        }
        catch (IOException IOError) {
                }
        }
    
    
    //The paintComponent method which override the Paint method draws the ellipses based on the number
    //of rows and columns entered. Each ellipse is also colored and placed in the ArrayList which stores all ellipses.
    //Also it paints the string which keeps count of the score and changes as we play the game. 
    //The width and height of each ellipse is linked the width and height of the window.
    @Override
    public void paintComponent(Graphics g)
    {
        myEllipses.clear();
        g2 = (Graphics2D) g; 
        Ellipse2D ellipse = new Ellipse2D.Double(0, 0, getWidth()/cols_number, getHeight()/rows_number);
        for (int i = 1; i <= rows_number; i++){
            for (int j = 1; j <= cols_number; j++){
                myEllipses.add(ellipse);
                g2.setPaint(Color.BLACK);
                g2.draw(ellipse);
                g2.fill(ellipse);
                ellipse = new Ellipse2D.Double(ellipse.getMaxX(), ellipse.getMinY(), ellipse.getWidth(), ellipse.getHeight());
            }
            ellipse = new Ellipse2D.Double(0, ellipse.getMaxY(), ellipse.getWidth(), ellipse.getHeight());
        }
        Font f = new Font("Serif", Font.BOLD, (getWidth() + getHeight())/30);
        g2.setFont(f);
        g2.setColor(Color.GRAY);
        g2.drawString(score_result, 25, 50);
        g2.drawString(level_result, getWidth() / 2 - 50, 50);
        g2.drawString(lives_result, getWidth() - 150, 50);
        if (gameover == false)
        {
            colorRandomEllipse(myEllipses);
        }
        if(lives == 0)
        {
            g2.setFont(new Font("serif", Font.BOLD, 30));
            g2.drawString("Game Over", getWidth() / 2 - 80, getHeight() / 2);
            g2.setFont(new Font("serif", Font.BOLD, 20));
            g2.drawString("Press enter to restart the game", getWidth() / 2 - 140, getHeight() / 2 + 50);
            gameover = true;
        }
    }
    
    //This function looks at the arrayList which contains all ellipses and picks one randomly. Then, it paints that, when invoked. 
    public void colorRandomEllipse(ArrayList<Ellipse2D> ellipses) 
    { 
        Random rand = new Random(); 
        Ellipse2D randEllipse = ellipses.get(rand.nextInt(ellipses.size()));
        g2.setPaint(Color.RED);
        g2.fill(randEllipse);
        myCurrentEllipse = randEllipse;  
    }
    
    //PointChecker checks if the current ellipse (the red one) contains the mouse pointer's coordinates. If so, returns true. 
    private boolean pointChecker(Ellipse2D redEllipse, Point2D mousePoint)
    {
        return redEllipse.contains(mousePoint);
    }  
    
    //It is the actionlistener that invokes the ColorRandomEllipse method whenever called
    class taskPerformer implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            colorRandomEllipse(myEllipses);
            repaint();
      }
    }
    //This is a key listener that is used to listen for the keyevent "enter" to restart the game after a gameover
    @Override
    public void keyTyped(KeyEvent e){}
    @Override
    public void keyReleased(KeyEvent e){}
    @Override
    public void keyPressed(KeyEvent e) 
    {
        if(e.getKeyCode() == KeyEvent.VK_ENTER)
        {
            if(gameover == true)
            {
                gameover = false;
                score = 0;
                score_result = "Score: " + score;
                lives = 5;
                lives_result = "Lives: " + lives;
                level = 1;
                level_result = "Level: " + level;
                repaint();
            }
        }
    }
    
    //This fuction takes an audio file path and creates an inputstream object to play sounds for successful and unsuccessful clicks.
    public void playSoundEffect(String filepath)
    {
        InputStream path = getClass().getResourceAsStream(filepath);
        try{
                Clip soud = AudioSystem.getClip();
                soud.open(AudioSystem.getAudioInputStream(path));
                soud.start();
        }
        catch(Exception error)
        {
            System.out.println("Error");
        }
    }
    
   
    
    
    
    //MouseHandler class extends MouseAdapter because we will use only mouseClicked method of the interface. 
    private class MouseHandler extends MouseAdapter
    {
        //Whenever the user clicks the mouse, first it will check if the click happened on the red circle.
        //If so, it will invoke the ColorRandomEllipse function and raise the score. 
        //Besides it repaints because both score and the red ellipse changes. 
        //Else, it will lower the lives.
        @Override
        public void mouseClicked(MouseEvent event)
        {
            if (pointChecker(myCurrentEllipse, (Point2D) event.getPoint()) == true)
            {
                if(gameover == false)
                {
                    colorRandomEllipse(myEllipses);
                    score += 5 * score_multiplier;
                    score_result = "Score: " + score;
                }
                
        
                //Checks if the mute field is true or false, which is connected to the MuteButton ActionListener
                if (mute == false) 
                    playSoundEffect("Sounds/Ding - Sound Effects YouTube.wav");
                repaint(); 
    
                //The timer is restarted whenever it is clicked
                timer.restart();
            }
            else
            {
                if(gameover == false)
                {
                    lives -= 1;
                    lives_result = "Lives: " + lives;
                    if (mute == false)
                        playSoundEffect("Sounds/Basketball Buzzer-SoundBible.com-1863250611.wav");
                }
                repaint();
            }
            if (score % 50 == 0 && score != 0)
            {
                level +=1;
                level_result = "Level: " + level;
                score_multiplier += 1;
                interval = interval / 2;
                timer = new Timer(interval, new taskPerformer());
                level_result = "Level: " + level;
                if (mute == false)
                    playSoundEffect("Sounds/level-up.wav");
                repaint();
            }

        }
        
    }
    
    public Dimension getPreferredSize()
    {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
           
}

