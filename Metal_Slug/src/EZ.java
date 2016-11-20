/*
Copyright (c) 6/23/2014, Dylan Kobayashi
Version: 2/9/2016
Laboratory for Advanced Visualization and Applications, University of Hawaii at Manoa.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
//import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
//import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
//import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputListener;

/**
 * Interaction with the EZ class should be done through the public static methods. EZ extends a JPanel which are among
 * the few things that can be put into the JApplet. They way they work and how to interact with them is significantly
 * different from JFrames.
 * 
 * The standard usage of EZ will involve EZ.initialize() to create a window. Then usage of the add methods to place
 * elements on it. The EZ.refreshScreen() method must be called in order to update the visuals. The standard update rate
 * is 60fps.
 * 
 * Majority of the EZ methods will not work unless EZ has been initialized.
 * 
 * @author Dylan Kobayashi
 *
 */
@SuppressWarnings("serial")
public class EZ extends JPanel {

  /** Used for external referencing. */
  public static EZ app;
  private static ArrayList<JFrame> 	openWindows = new ArrayList<>();
  private static ArrayList<Boolean> openWindowsStatus = new ArrayList<>();
  private static ArrayList<EZ> 		openWindowEz = new ArrayList<>();

  /** Width. This needs to match the values given in the applet properties or there may be visual chopping. */
  private static int WWIDTH;
  /** Height. This needs to match the values given in the applet properties or there may be visual chopping. */
  private static int WHEIGHT;
  /** Background color. */
  private static Color backgroundColor = Color.WHITE;
  /** Time tracker for updates. */
  private static long lastUpdate = System.currentTimeMillis();
  /** Time tracker for updates. */
  private static long timeDelta;

  /** Used for tracking visual elements. */
  protected ArrayList<EZElement> elements = new ArrayList<EZElement>();

  /** Used for frame tracking. */
  private static int currentFrameRate = 60;
  private static long sleepTime = (long) (1000.0 / currentFrameRate); // default is 60fps.
  private static boolean updateASAP = false;
  
  /**Used for silent error tracking.*/
  private static int errorCounter = 0;
  private static String errorMsg = "";

  /**
   * This frustrating variable is necessary to get keyboard input detection working. Determined through trial and error.
   * There doesn't seem to be any documentation why and really it could have been for any other button. Clarification:
   * key binders are necessary if there are multiple JPanels or subcomponents. But even if there is only one, this does
   * something that inits the listeners "correctly".
   */
  private Action xPress = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      // System.out.print("VisualControl: the x button was pressed");
    }
  };

  // **Used as a work around to the applet deadlock on init or start.*/
  // private boolean runOnce = true;

  /**
   * Calling the constructor for the EZ class should never be done manually. You should be using EZ.initialize().
   * Creates an instance of EZ and sets it as primary content pane and initiates values as necessary.
   * 
   * @param w value in pixels of how large to make the width of the inner content area.
   * @param h value in pixels of how large to make the height of the inner content area.
   */
  public EZ(int w, int h) {
    WWIDTH = w;
    WHEIGHT = h;
    this.setPreferredSize(new Dimension(WWIDTH, WHEIGHT));
    app = this;
    lastUpdate = System.currentTimeMillis();

    // setup the input handlers.
    EZInteraction ih = new EZInteraction();
    this.addKeyListener(ih);
    this.addMouseListener(ih);
    this.addMouseMotionListener(ih);
    // Note: difference between input handler and input/action map
    // The below lines are used to "activate the input handler, and I have no clue how or why.
    this.getInputMap().put(KeyStroke.getKeyStroke("pressed X"), "pressed");
    this.getActionMap().put("pressed", xPress);

  } // end constructor

  /**
   * Used to get the window width not including the frames.
   * 
   * @return int value equal to the number of pixels between the frames horizontally.
   */
  public static int getWindowWidth() {
    return WWIDTH;
  }

  /**
   * Used to get the window height not including the frames.
   * 
   * @return int value equal to the number of pixels between the frames vertically.
   */
  public static int getWindowHeight() {
    return WHEIGHT;
  }

  /**
   * Used to get the difference in time since the last refresh. Time is in milliseconds. 1 second == 1000 milliseconds.
   * If you want to change the time between updates setFrameRate() may be what you are looking for.
   * 
   * @return int value of the difference in time. Note: Standard system time counters are usually longs. However for
   * ICS111 int is the most commonly used datatype of that family tree, and should be more than enough for delta time.
   */
  public static int getDeltaTime() {
    return (int) timeDelta;
  }

  /**
   * This method is in charge of painting the screen. But do note that each of the individual elements are in charge of
   * painting themselves. This method should not be called manually. Use EZ.refreshScreen() instead.
   */
  @Override public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    g2.setColor(backgroundColor); // wash the background with specified bg color to prevent ghosting.
    g2.fillRect(0, 0, WWIDTH + 100, WHEIGHT + 100);

    for (int i = 0; i < elements.size(); i++) {
      if(!elements.get(i).hasParent()){
        elements.get(i).paint(g2);
      }
    }

  } // end paint

  /**
   * This method will set the background color to the given color. Don't forget to import the Color when using this.
   * While standard Colors like Color.WHITE or Color.BLUE are available, it is possible to specify an rgb value using:
   * EZ.setBackgroundColor( new Color( r, g, b) ); where r,g,b are int values.
   * 
   * @param c Color to use.
   */
  public static void setBackgroundColor(Color c) {
    backgroundColor = c;
  }
  
  /**
   * Will pause the program for the specified amount of milliseconds.
   * Mechanically this is just an encapsulated sleep.
   * 
   * @param msToPauseFor how many milliseconds to pause for.
   */
  public static void pause(long msToPauseFor) {
    //The try/catch is strictly by requirement. Realistically no error should result from the sleep.
    try { Thread.sleep(msToPauseFor); }
    catch (InterruptedException e) {}
  }

  /**
   * Used to repaint the application. Without this call, any changes made to the elements will not be visibly seen. This
   * will also control the speed of the program updates. By default it is set to 60fps. This can be modified by using
   * setFrameRate().
   */
  public static void refreshScreen() {
    timeDelta = System.currentTimeMillis() - lastUpdate;
    lastUpdate = System.currentTimeMillis();
    app.repaint();
    if (!updateASAP) {
      try {
        if(timeDelta > sleepTime){
          Thread.sleep(sleepTime * 2 - timeDelta); 
        }
        else {
          Thread.sleep(sleepTime);
        }
      }
      catch (Exception e) {
        errorCounter++;
        errorMsg += "EZ.refreshScreen() error with sleep:" + e.getMessage() + "\n";
      }
    }
    closeWindowWithIndex(-9999);
  }// end refresh screen.
  
  /**
   * 
   */
  public static void refreshScreenOfAllActiveWindows(){
	  refreshScreen();
	  for(int i = 0; i < openWindows.size(); i++) {
		  if( openWindowsStatus.get(i) ) {
			  openWindowEz.get(i).repaint();
		  }
	  }
  }
  
  /**
   * Sets the frame rate, which controls how fast the program will attempt to update itself. Note: it is very rarely
   * possible to get an exact match of frames per second due to the time statements take to execute in addition to the
   * fact that the division of time may not be equally distributed for that particular value of fps. Also possible to
   * request a speed that cannot be done due to processing time of other components or hardware limitations. Will not
   * change current fps if given value is 0 or less. A value of 1000 is the same as setting the program to update as
   * quickly as possible, it doesn't guarantee that frame rate.
   * 
   * @param fr an int value specifying the desired frames(updates) per second.
   */
  public static void setFrameRate(int fr) {
    if (fr > 0) {
      sleepTime = (long) (1000.0 / fr);
      currentFrameRate = fr;
    }
  }

  /**
   * Will return the current frame rate that EZ is updating at.
   * 
   * @return int value of the current frame rate.
   */
  public static int getCurrentFrameRate() {
    return currentFrameRate;
  }

  /**
   * Calling this method will tell EZ whether or not it should be updating As Soon As Possible(ASAP). Passing true will
   * bypass the given frame rate values causing updates to occur ASAP. There will be no CPU rest, which for most
   * programs is not necessary. Passing false will revert back to the last passed frame rate value. If none has been
   * given, 60fps is the EZ default.
   * 
   * @param b value true means update ASAP. false means use last specified frame rate.
   */
  public static void setFrameRateASAP(boolean b) {
    updateASAP = b;
  }

  /**
   * Returns whether or not the program will update ASAP.
   * 
   * @return true means it is. false means it is using the specified frame rate. 60fps is the default.
   */
  public static boolean isFrameRateASAP() {
    return updateASAP;
  }

  /**
   * Adds an element for EZ to track. Generally you should not be using this. Use the more specific add methods instead.
   * 
   * @param ve The element to add.
   * @return true or false based on whether or not the element was successfully added.
   */
  public static boolean addElement(EZElement ve) {
    return EZ.app.elements.add(ve);
  }

  /**
   * Adds an element for EZ to track at a specific draw layer. Lower numbers are on lower layers. Generally you should
   * not be using this. Use the more specific add methods instead. The given layer must be valid and within the current
   * range of layers.
   *
   * @param ve The element to add.
   * @param index where the element should be placed.
   * @return true or false based on whether or not the element was successfully added.
   */
  public static boolean addElement(EZElement ve, int index) {
    if (index < 0 || index > EZ.app.elements.size()) {
      System.out.println("ERROR: attempting to add an element into an invalid index.");
      return false;
    }
    EZ.app.elements.add(index, ve);
    return true;
  }

  /**
   * Adds a rectangle to the window. Returns the rectangle for later manipulation. If not immediately assigned to a
   * variable, chances are you will have this element stuck on screen which cannot be removed. As result in most cases
   * you will want to assign it to a variable.
   * 
   * Color must be specified. Don't forget to import Color. The filled parameter will determine whether or not the
   * element will be a solid of the given color. If it is not filled, the inner parts will be fully transparent.
   * 
   * Example usage: EZRectangle r; r = EZ.addRectangle( 200, 200, 30, 10, Color.BLACK, true);
   * 
   * @param x center.
   * @param y center.
   * @param w width.
   * @param h height.
   * @param c color.
   * @param filled true will make the element a solid color. false will outline with the given color.
   * @return the rectangle.
   */
  public static EZRectangle addRectangle(int x, int y, int w, int h, Color c, boolean filled) {
    EZRectangle vr = new EZRectangle(x, y, w, h, c, filled);
    EZ.app.elements.add(vr);
    refreshScreen();
    return vr;
  }

  /**
   * Adds a circle to the window. Returns the circle for later manipulation. If not immediately assigned to a variable,
   * chances are you will have this element stuck on screen which cannot be removed. As result in most cases you will
   * want to assign it to a variable.
   * 
   * Color must be specified. Don't forget to import Color. The filled parameter will determine whether or not the
   * element will be a solid of the given color. If it is not filled, the inner parts will be fully transparent.
   * 
   * Example usage: EZCircle c; c = EZ.addCircle( 200, 200, 30, 10, Color.BLACK, true);
   * 
   * @param x center.
   * @param y center.
   * @param w width.
   * @param h height.
   * @param c color.
   * @param filled true will make the element a solid color. false will outline with the given color.
   * @return the circle.
   */
  public static EZCircle addCircle(int x, int y, int w, int h, Color c, boolean filled) {
    EZCircle vc = new EZCircle(x, y, w, h, c, filled);
    EZ.app.elements.add(vc);
    refreshScreen();
    return vc;
  }

  /**
   * Adds text to the window. Returns the text for later manipulation. If not immediately assigned to a variable,
   * chances are you will have this element stuck on screen which cannot be removed. As result in most cases you will
   * want to assign it to a variable.
   * 
   * It might not be easy to calculate left or right bound until after creation since the x,y values are where the
   * text's center will be placed.
   * 
   * Text cannot have their width and height manually set, that will depend on the content of the text. Using this
   * addText() method will default the text size to 10px.
   * 
   * Color must be specified. Don't forget to import Color.
   * 
   * Example usage: EZText t; t = EZ.addText( 200, 200, Color.BLACK, true);
   * 
   * @param x center.
   * @param y center.
   * @param msg that will be displayed.
   * @param c color of the text
   * @return the circle.
   */
  public static EZText addText(int x, int y, String msg, Color c) {
    return addText(x, y, msg, c, 10);
  }

  /**
   * Adds text to the window. Returns the text for later manipulation. If not immediately assigned to a variable,
   * chances are you will have this element stuck on screen which cannot be removed. As result in most cases you will
   * want to assign it to a variable.
   * 
   * It might not be easy to calculate left or right bound until after creation since the x,y values are where the
   * text's center will be placed.
   * 
   * Text cannot have their width and height manually set, that will depend on the content of the text.
   * 
   * Color must be specified. Don't forget to import Color.
   * 
   * Example usage: EZText t; t = EZ.addText( 200, 200, Color.BLACK, true, 20);
   * 
   * @param x center.
   * @param y center.
   * @param msg that will be displayed.
   * @param c color of the text
   * @param fs size of the font in pixels.
   * @return the circle.
   */
  public static EZText addText(int x, int y, String msg, Color c, int fs) {
    EZText vc = new EZText(x, y, msg, c, fs);
    EZ.app.elements.add(vc);
    refreshScreen();
    return vc;
  }
  
  /**
   * Adds text to the window. Returns the text for later manipulation. If not immediately assigned to a variable,
   * chances are you will have this element stuck on screen which cannot be removed. As result in most cases you will
   * want to assign it to a variable.
   * 
   * It might not be easy to calculate left or right bound until after creation since the x,y values are where the
   * text's center will be placed.
   * 
   * Text cannot have their width and height manually set, that will depend on the content of the text.
   * 
   * Color must be specified. Don't forget to import Color.
   * 
   * Example usage: EZText t; t = EZ.addText("Arial", 200, 200, Color.BLACK, true, 20);
   * 
   * @param fontName to display the msg in. Must be available to the system. A nonexistent font will output a console error, but will not halt the program.
   * @param x center.
   * @param y center.
   * @param msg that will be displayed.
   * @param c color of the text
   * @param fs size of the font in pixels.
   * @return the circle.
   */
  public static EZText addText(String fontName, int x, int y, String msg, Color c, int fs) {
    EZText vc = new EZText(x, y, msg, c, fs);
    vc.setFont(fontName);
    EZ.app.elements.add(vc);
    refreshScreen();
    return vc;
  }


  /**
   * Adds an image to the window. Returns the image for later manipulation. If not immediately assigned to a variable,
   * chances are you will have this element stuck on screen which cannot be removed. As result in most cases you will
   * want to assign it to a variable.
   * 
   * The size(width and height) of the image will be based upon the original attributes of the image file.
   * 
   * Example usage: EZImage i; i = EZ.addImage( "Smile.png", 200, 300);
   * 
   * @param filename of image
   * @param x center.
   * @param y center.
   * @return the image.
   */
  public static EZImage addImage(String filename, int x, int y) {
    EZImage vc = new EZImage(filename, x, y);
    EZ.app.elements.add(vc);
    refreshScreen();
    return vc;
  }

  /**
   * Adds a line to the window. Returns the line for later manipulation. If not immediately assigned to a variable,
   * chances are you will have this element stuck on screen which cannot be removed. As result in most cases you will
   * want to assign it to a variable.
   * 
   * The line must be created with two points. A start and end point. The line itself will then be drawn to connect the
   * two points. By default this method will make the line thickness 1px.
   * 
   * Color must be specified. Don't forget to import Color.
   * 
   * Example usage: EZLine l; l = EZ.addLine( 200, 300, 600, 100, Color.BLACK);
   * 
   * @param x1 The x value of point 1.
   * @param y1 The y value of point 1.
   * @param x2 The x value of point 2.
   * @param y2 The y value of point 2.
   * @param c color to make the line.
   * @return the line.
   */
  public static EZLine addLine(int x1, int y1, int x2, int y2, Color c) {
    return addLine(x1, y1, x2, y2, c, 1);
  }

  /**
   * Adds a line to the window. Returns the line for later manipulation. If not immediately assigned to a variable,
   * chances are you will have this element stuck on screen which cannot be removed. As result in most cases you will
   * want to assign it to a variable.
   * 
   * The line must be created with two points. A start and end point. The line itself will then be drawn to connect the
   * two points. Thickness less than 1 will be automatically increased to 1.
   * 
   * Color must be specified. Don't forget to import Color.
   * 
   * Example usage: EZLine l; l = EZ.addLine( 200, 300, 600, 100, Color.BLACK);
   * 
   * @param x1 The x value of point 1.
   * @param y1 The y value of point 1.
   * @param x2 The x value of point 2.
   * @param y2 The y value of point 2.
   * @param c Color to make the line.
   * @param thickness to make the line.
   * @return the line.
   */
  public static EZLine addLine(int x1, int y1, int x2, int y2, Color c, int thickness) {
    EZLine vl = new EZLine(x1, y1, x2, y2, c, thickness);
    EZ.app.elements.add(vl);
    refreshScreen();
    return vl;
  }

  /**
   * Adds a polygon to the window. Returns the polygon for later manipulation. If not immediately assigned to a
   * variable, chances are you will have this element stuck on screen which cannot be removed. As result in most cases
   * you will want to assign it to a variable.
   * 
   * The polygon must be created with two arrays. One holding a list of x values while the other holds a list of y
   * value. Each index of the arrays refer to a specific point. The order of points matter, as the polygon will be drawn
   * starting from index 0 to the end of the array. The last point will be automatically connected to the first point.
   * 
   * Color must be specified. Don't forget to import Color.
   * 
   * Example usage: EZPolygon p; int[] xp, yp; xp = new int[3]; yp = new int[3]; xp[0] = 100; xp[1] = 150; xp[2] = 200;
   * yp[0] = 100; yp[1] = 200; yp[2] = 100;
   * 
   * p = EZ.addPolygon( xp, yp, Color.BLACK, true);
   * 
   * @param xp int array containing the x values for the points.
   * @param yp int array containing the y values for the points.
   * @param c color.
   * @param filled true will make the element a solid color. false will outline with the given color.
   * @return the polygon.
   */
  public static EZPolygon addPolygon(int[] xp, int[] yp, Color c, boolean filled) {
    EZPolygon vp = new EZPolygon(xp, yp, c, filled);
    EZ.app.elements.add(vp);
    refreshScreen();
    return vp;
  }

  /**
   * Adds a sound to the window. Returns the sound for later manipulation.
   * 
   * You NEED to assign this to a variable otherwise you will not be able to play the sound.
   * 
   * Currently the sound file must be in .wav format to work.
   * 
   * Example usage: EZSound s; s = EZ.addSound("YouGotMail.wav");
   * 
   * @param file name of the sound file including extension.
   * @return the sound.
   */
  public static EZSound addSound(String file) {
    EZSound s = new EZSound(file);
    return s;
  }

  /**
   * Adds a group to the window. Will always start at coordinate 0,0.
   * 
   * You NEED to assign this to a variable otherwise the group will not be accessible.
   * 
   * A group by itself will not do anything. The usage is to add other elements to a group so they can maintain their
   * relative positions and be manipulated as one element.
   * 
   * Example usage: EZGroup g; g = EZ.addGroup();
   * 
   * @return the group.
   */
  public static EZGroup addGroup() {
    EZGroup n = new EZGroup();
    EZ.app.elements.add(n);
    refreshScreen();
    return n;
  }

  /** Clears out all visual elements that EZ is tracking. */
  public static void removeAllEZElements() {
    EZ.app.elements.clear();
  }

  /**
   * Remove one visual element that EZ is tracking.
   * 
   * @param ve the element to remove from EZ.
   */
  public static void removeEZElement(EZElement ve) {
    EZ.app.elements.remove(ve);
  }

  /**
   * Returns the topmost element that contains the point. Will not return an element which is not visible. The topmost
   * element is the one which has been drawn last making it visually appear on top others. Will not ever return an
   * EZGroup, since technically the group itself is comprised of multiple elements. If you want the EZGroup which the
   * given element is apart, crawl up the ancestry using getParent().
   * 
   * Polymorphism knowledge may be needed to use this method.
   * 
   * @param x coordinate of the point.
   * @param y coordinate of the point.
   * @return the top most EZElement that is not a group.
   */
  public static EZElement getTopElementContainingPoint(int x, int y) {
    ArrayList<EZElement> elems = getAllElementsContainingPoint(x, y);
    if (elems.size() > 0) {
      return elems.get(elems.size() - 1);
    }
    return null;
  }

  /**
   * Collects and returns all elements containing the specified point. Will not return an element which is not visible.
   * Will not return an EZGroup. See getTopElementContainingPoint() for explanation.
   * 
   * Polymorphism knowledge may be needed to use this method.
   * 
   * @param x coordinate of the point.
   * @param y coordinate of the point.
   * @return an array containing all EZElements.
   */
  public static ArrayList<EZElement> getAllElementsContainingPoint(int x, int y) {
    ArrayList<EZElement> containingElems = new ArrayList<EZElement>();
    for (int i = 0; i < EZ.app.elements.size(); i++) {
      if (EZ.app.elements.get(i) instanceof EZGroup) {
        ArrayList<EZElement> allGroupChildren = new ArrayList<EZElement>();
        recurseGroupAddingToArrayList((EZGroup) EZ.app.elements.get(i), allGroupChildren);
        for (EZElement child : allGroupChildren) {
          if (child.isShowing() && child.isPointInElement(x, y)) {
            containingElems.add(child);
          }
        }
      }
      else if (EZ.app.elements.get(i).isShowing() && EZ.app.elements.get(i).isPointInElement(x, y)) {
        containingElems.add(EZ.app.elements.get(i));
      }
    }
    return containingElems;
  } // end

  /**
   * Designed as a recursive method to collect all children and add them to the given arraylist. This will not add
   * groups to the ArrayList, but instead will search those groups for elements adding those elements to the ArrayList.
   * 
   * @param group from which to start the downward search
   * @param elems the ArrayList to add all non-EZGroup children to.
   */
  public static void recurseGroupAddingToArrayList(EZGroup group, ArrayList<EZElement> elems) {
    ArrayList<EZElement> children = group.getChildren();
    for (EZElement c : children) {
      if (c instanceof EZGroup) {
        recurseGroupAddingToArrayList((EZGroup) c, elems);
      }
      else {
        elems.add(c);
      }
    }
  }

  /**
   * Given an x and y coordinate, will check if that point is within the given element. This is done with respect to
   * world space. If the element is not showing, will always return false.
   * 
   * @param x coordinate of the point.
   * @param y coordinate of the point.
   * @param ve the element to check if the point is within.
   * @return true if the point is within the element. Otherwise false. Always returns false if the element is not
   * showing.
   */
  public static boolean isElementAtPoint(int x, int y, EZElement ve) {
    if (!ve.isShowing()) {
      return false;
    }
    return ve.isPointInElement(x, y);
  }

  /**
   * Will check if the given element is the top most element at the specified point. Top most is refers to the highest
   * draw layer meaning nothing is visually in front of it. If it is not showing, will always return false. Will not
   * work with EZGroup.
   * 
   * @param x coordinate of the point.
   * @param y coordinate of the point.
   * @param ve element to check if the point is within.
   * @return true if the element is the top point. Otherwise false. Always returns false if the element is not showing.
   */
  public static boolean isTopElementAtPoint(int x, int y, EZElement ve) {
    ArrayList<EZElement> elems = getAllElementsContainingPoint(x, y);
    if (elems.size() > 0) {
      return (elems.get(elems.size() - 1) == ve);
    }
    return false;
  }

  /**
   * Will push the given element to the back of the drawing layer. If the Element is in a group, the element will be
   * pushed to the back of that group's drawing layer.
   * 
   * @param ve element to push back.
   * @return false if the element doesn't exist. Otherwise true.
   */
  public boolean pushToBack(EZElement ve) {
    if (!elements.contains(ve) && !ve.hasParent()) {
      System.out.println("ERROR: attempting to change layer of element not tracked by EZ or part of a group.");
      return false;
    }
    if (ve.hasParent()) {
      ve.getParent().getChildren().remove(ve); // only works because the getChildren returns an editable arraylist.
      ve.getParent().getChildren().add(0, ve); // and will not influence either elements.
    }
    else {
      elements.remove(ve);
      elements.add(0, ve);
    }
    return true;
  } // end visual push to back

  /**
   * Will push the given element back one drawing layer. If the element is in a group, the element will be pushed back
   * once on that group's drawing layer.
   * 
   * @param ve element to push back.
   * @return false if the element doesn't exist. Otherwise true.
   */
  public boolean pushBackOneLayer(EZElement ve) {
    if (!elements.contains(ve) && !ve.hasParent()) {
      System.out.println("ERROR: attempting to change layer of element not tracked by EZ or part of a group.");
      return false;
    }
    if(ve.hasParent()){
      int pos = ve.getParent().getChildren().indexOf(ve);
      if (pos > 0) {
        ve.getParent().getChildren().remove(ve);
        ve.getParent().getChildren().add(pos - 1, ve);
      } // only works because the getChildren returns an editable arraylist.
      // and will not influence either elements.
    }
    else {
      int pos = elements.indexOf(ve);
      if (pos > 0) {
        elements.remove(ve);
        elements.add(pos - 1, ve);
      }
    }
    return true;
  } // end push back one layer

  /**
   * Will pull the given element to the front of the drawing layer. If the element is in a group, the element will be
   * pulled to the front of that group's drawing layer.
   * 
   * @param ve the element to pull.
   * @return false if the element doesn't exist. Otherwise true.
   */
  public boolean pullToFront(EZElement ve) {
    if (!elements.contains(ve) && !ve.hasParent()) {
      System.out.println("ERROR: attempting to change layer of element not tracked by EZ or part of a group.");
      return false;
    }
    if(ve.hasParent()){
      ve.getParent().getChildren().remove(ve); // only works because the getChildren returns an editable arraylist.
      ve.getParent().getChildren().add(ve); // and will not influence either elements.
    }
    else {
      elements.remove(ve);
      elements.add(ve);
    }
    return true;
  } // end visual pull to front

  /**
   * Will pull the given element to the front of the drawing layer. If the element is in a group, the element will be
   * pulled forward once on that group's drawing layer.
   * 
   * @param ve the element to pull.
   * @return false if the element doesn't exist. Otherwise true.
   */
  public boolean pullForwardOneLayer(EZElement ve) {
    if (!elements.contains(ve) && !ve.hasParent() ) {
      System.out.println("ERROR: attempting to change layer of element not tracked by EZ or part of a group.");
      return false;
    }
    if(ve.hasParent()){
      int pos = ve.getParent().getChildren().indexOf(ve);
      if (pos < elements.size() - 1) {
        elements.remove(ve);
        elements.add(pos + 1, ve);
      }// only works because the getChildren returns an editable arraylist.
       // and will not influence either elements.
    }
    else {
      int pos = elements.indexOf(ve);
      if (pos < elements.size() - 1) {
        elements.remove(ve);
        elements.add(pos + 1, ve);
      }
    }
    return true;
  } // end pull forward one layer.
  
  /**
   * Will return the highest layer occupied by an element.
   * @return highest layer occupied by an element.
   */
  public int getHighestLayerOfAllElements() {
    return elements.size() -1;
  }
  
  /**
   * Will return the index of the given element. Lower numbers have least visibility.
   * If the element is part of a group, then it will return the position in the group.
   * 
   * @param ve the element to get the index of.
   * @return index of the element. -1 if it is not a tracked object.
   */
  public int getLayerPosition(EZElement ve) {
    if (!elements.contains(ve) && !ve.hasParent() ) {
      System.out.println("ERROR: element not being tracked by EZ and as result does not have layer.");
      return -1;
    }
    if( ve.hasParent() ) {
      return ve.getParent().getChildren().indexOf(ve);
    }
    else {
      return elements.indexOf(ve);
    }
  } //end getLayerPosition
  
  /**
   * Sets the layer of the given element to the specified layer if possible.
   * The layer must be zero or greater.
   * If the provided layer higher than possible, it will just be set to highest layer. 
   * Moving from a low index to higher index will cause the element at destination to shift left.
   * However, moving from a high index to lower index will cause the element at destination to shift right.
   * Layer values that match the current, will not change anything.
   * 
   * @param ve the element to change layer.
   * @param layer index to move to.
   */
  public void setLayerOfElement(EZElement ve, int layer) {
    if (!elements.contains(ve) && !ve.hasParent() && layer < 0 ) {
      System.out.println("ERROR: element not being tracked by EZ and as result does not have layer.");
      return;
    }
    if ( ve.hasParent() &&  ve.getParent().getChildren().indexOf(ve) != layer) {
      EZGroup p = ve.getParent();
      p.getChildren().remove(ve);
      if( layer <= p.getChildren().size() ) {
        p.getChildren().add(layer, ve);
      }
      else {
        p.getChildren().add(ve);
      }
    } //end if had parent.
    else if ( elements.indexOf(ve) != layer ) {
      elements.remove(ve);
      if( layer <= elements.size() ) {
        elements.add(layer, ve);
      }
      else {
        elements.add(ve);
      }
    } //end else did not have parent.
    
  } //end setLayerOfElement
  
  /**
   * Given two elements, will place the first below the second.
   * This will most likely change the index of other elements.
   * Will not work if they are not in the same container.
   * @param moving object that will be extracted from container then placed below the second.
   * @param above will not be extracted, but the first object will be placed below this one.
   */
  public void setLayerBelow(EZElement moving, EZElement above) {
    if( elements.contains(moving) && elements.contains(above) ) {
      elements.remove(moving);
      elements.add( elements.indexOf(above), moving );
    }
    else if ( moving.hasParent() && above.hasParent() && ( moving.getParent() == above.getParent() ) ) {
      above.getParent().getChildren().remove(moving);
      above.getParent().getChildren().add( above.getParent().getChildren().indexOf(above), moving);
    }
    else {
      System.out.println("ERROR: element not being tracked by EZ and as result does not have layer.");
      return;
    }
  } //setLayerBelow
  

  /**
   * Given two elements, will place the first above the second.
   * This will most likely change the index of other elements.
   * Will not work if they are not in the same container.
   * @param moving object that will be extracted from container then placed above the second.
   * @param below will not be extracted, but the first object will be placed above this one.
   */
  public void setLayerAbove( EZElement moving, EZElement below ) {
    if( elements.contains(moving) && elements.contains(below) ) {
      elements.remove(moving);
      elements.add( elements.indexOf(below) + 1, moving );
    }
    else if ( moving.hasParent() && below.hasParent() && ( moving.getParent() == below.getParent() ) ) {
      below.getParent().getChildren().remove(moving);
      below.getParent().getChildren().add( below.getParent().getChildren().indexOf(below) + 1, moving);
    }
    else {
      System.out.println("ERROR: element not being tracked by EZ and as result does not have layer.");
      return;
    }
  } //setLayerAbove

  /**
   * This will setup EZ for usage. Without calling this method first, none of the other EZ methods will work correctly.
   * Parameters will be used to determine width and height of window. Do not call this method more than once in a
   * program run.
   * 
   * @param width for the content area of the window.
   * @param height for the content area of the window.
   */
  public static int initialize(int width, int height) {
    String windowName = "ICS111";
    JFrame frame = new JFrame(windowName);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Create and set up the content pane.
    EZ newContentPane = new EZ(width, height);
    newContentPane.setOpaque(true); // content panes must be opaque
    frame.setContentPane(newContentPane);

    // Size the frame according to largest element, then display the window.
    frame.setResizable(false);
    frame.pack();
    frame.setVisible(true);
    timeDelta = 0;
    lastUpdate = System.currentTimeMillis();
    
    //account for number of windows
    openWindows.add(frame);
    openWindowsStatus.add(true);
    openWindowEz.add(newContentPane);
    int wIndex = openWindows.size() - 1;
    openWindows.get( wIndex ).setTitle("ICS 111 - Window index:" + wIndex);
    return wIndex;
  }

  /**
   * This will setup EZ for usage. Without calling this method first, none of the other EZ methods will work correctly.
   * Window will default to use the full dimensions of the screen. Do not call this method more than once in a program
   * run.
   */
  public static int initialize() {
    return initialize((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int) Toolkit.getDefaultToolkit()
        .getScreenSize().getHeight());
  }
  
  /**
   * 
   */
  public static void setCurrentWindow(int windowIndex) {
    if( windowIndex > -1 && windowIndex < openWindows.size() && openWindowsStatus.get(windowIndex) ) {
    	app = openWindowEz.get(windowIndex);
    }
  }
  
  /**
   * Will close the specified window. Numbers may change when windows close. This will depend on their order of creation.
   */
  public static void closeWindowWithIndex(int windowIndex) {
    if ( (windowIndex >= 0) && (windowIndex < openWindows.size()) && openWindowsStatus.get(windowIndex) ) {
      //get the window to close, remove from open windows, then dispose of it
      //JFrame windowToClose = openWindows.get(windowIndex);
      //openWindows.remove(windowToClose);
      //windowToClose.dispose();
      openWindows.get(windowIndex).dispose();
      openWindowsStatus.set(windowIndex, false);
    }
    else if( windowIndex != -9999) {
    	System.out.println("Invalid window index given:" + windowIndex + ". Not closing a window.");
    }
//    //window checks: close if no windows. 1 window gets the close app on close. Renumber windows.
//    if(openWindows.size() == 0) { System.exit(0); System.out.println("Closing program, no open windows."); }
//    else if(openWindows.size() == 1) { openWindows.get(0).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); }
//    else {
//      for(int i = 0; i < openWindows.size(); i++) {
//        openWindows.get(i).setTitle("ICS 111 - Window index:" + i);
//        openWindows.get(i).setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//      }
//    }
  } //end closeWindowWithoutClosingApplication
  
  /**
   * Will return the number of open windows.
   */
  public static int getNumberOfOpenWindows() {
	  int count = 0;
	  for(int i = 0; i < openWindows.size(); i++) {
		  if(openWindowsStatus.get(i)) { count++; }
	  }
	  return count;
  }
  
  public static void trackedErrorPrint() {
    System.out.println("Errors tracked:" + EZ.errorCounter);
    System.out.println("====\nErrors\n====\n:" + EZ.errorMsg + "\n====\nEnd\n====\n");
  }

} // end visual control class

/**
 * The EZElement is the parent class of each of the types of elements that can be added to the window. Contains a set of
 * methods that are standard for all inheriting classes. Some methods do not have the same functionality. For example,
 * while the EZImage class does have getColor() and setColor() methods, they do not have any practical usage for the
 * EZImage class.
 * 
 * @author Dylan Kobayashi
 */
abstract class EZElement {

  protected boolean isShowing = true;

  /**
   * The paint method controls how the element draws itself on the screen. You should not be calling this method, it
   * will be handled by EZ.refreshScreen().
   * 
   * @param g2 is the graphics reference to draw to the screen.
   */
  public abstract void paint(Graphics2D g2);

  /**
   * Returns the height of this element with respect to local space.
   * 
   * @return height in pixels.
   */
  public abstract int getHeight();

  /**
   * Returns the width of this element with respect to local space.
   * 
   * @return width in pixels.
   */
  public abstract int getWidth();

  /**
   * Returns the x center of this element with respect to local space.
   * 
   * @return x coordinate.
   */
  public abstract int getXCenter();

  /**
   * Returns the y center of this element with respect to local space.
   * 
   * @return y coordinate.
   */
  public abstract int getYCenter();

  /**
   * Will return this element's x coordinate with respect to world space(After all transformations have been applied).
   * 
   * @return x coordinate on the world space.
   */
  public int getWorldXCenter() {
    return (int) (this.getBounds().getBounds().getCenterX());
  }

  /**
   * Will return this element's y coordinate with respect to world space(After all transformations have been applied).
   * 
   * @return y coordinate on the world space.
   */
  public int getWorldYCenter() {
    return (int) (this.getBounds().getBounds().getCenterY());
  }

  /**
   * Returns the width of the object with respect to the world. This can differ from getWidth() if scale has been
   * applied and if any groups this element resides in has been affected by scale.
   * 
   * @return width in pixels with respect to world space.
   * */
  public int getWorldWidth() {
    double tscale = this.scaleWith1AsOriginal;
    EZElement temp = this;
    while (this.hasParent()) {
      temp = this.getParent();
      tscale *= temp.getScale();
    }
    return (int) (tscale * this.getWidth());
  } // end get world width

  /**
   * Returns the height of the object with respect to the world. This can differ from getHeight() if scale has been
   * applied and if any groups this element resides in has been affected by scale.
   * 
   * @return height in pixels with respect to world space.
   * */
  public int getWorldHeight() {
    double tscale = this.scaleWith1AsOriginal;
    EZElement temp = this;
    while (this.hasParent()) {
      temp = this.getParent();
      tscale *= temp.getScale();
    }
    return (int) (tscale * this.getHeight());
  } // end get world height

  /**
   * Sets the color of this element.
   * 
   * @param c color to set this element to.
   */
  public abstract void setColor(Color c);

  /**
   * Returns the color of this element.
   * 
   * @return color of this element.
   */
  public abstract Color getColor();

  /**
   * Will return whether or not this element is set to be filled.
   * 
   * @return true if it is. false if it isn't.
   */
  public abstract boolean isFilled();

  /**
   * Will set the filled status of this element.
   * 
   * @param f fill status that will be set.
   */
  public abstract void setFilled(boolean f);

  /**
   * Sets the center of the element to given x and y coordinate. Only affects local coordinate location.
   * 
   * @param x center coordinate this element will be set to.
   * @param y center coordinate this element will be set to.
   */
  public abstract void translateTo(double x, double y);

  /**
   * Moves the center of the element by given x and y coordinate. Only affects local coordinate location.
   * 
   * @param x amount this element's center will be shifted by.
   * @param y amount this element's center will be shifted by.
   */
  public abstract void translateBy(double x, double y);

  /**
   * Will move the element forward by the given distance. Forward is determined by the current rotation of the object.
   * For example:<br>
   * a rotation of 0 means forward is to the right side of the screen.<br>
   * a rotation of 90 means forward is to the bottom of the screen.<br>
   * a rotation of -90 means forward is to the top of the screen.<br>
   *
   * @param distance to shift the element forward by.
   */
  public void moveForward(double distance) {
    double dx = Math.cos(Math.toRadians(this.getRotation())) * distance;
    double dy = Math.sin(Math.toRadians(this.getRotation())) * distance;
    this.translateBy(dx, dy);
  }

  /*
   * Will move with respect to the rotation and scale. Only affects local coordinate location. Commented out for now.
   * final public void translate(int x, int y) { AffineTransform at = EZElement.transformHelper(this); at.translate(x,
   * y); Point atCenter = new Point(0,0); at.transform(atCenter, atCenter); this.moveTo(atCenter.x, atCenter.y); } //end
   * translate
   */

  /**
   * This will ensure the element is painted. If the object is already showing, this has no effect. By default objects
   * are showing.
   */
  public abstract void show(); // while the effect is simple some elements need this filled out differently.

  /**
   * This will prevent the element from being painted. An additional effect is that the isPointInElement() will return
   * false if the element is hidden. If the object is already hidden, this has no effect.
   */
  public abstract void hide(); // the intention is to ensure additions take this into account.

  /** 
   * Returns whether or not the element is being painted.
   * This has nothing to do with whether or not the object is on screen.
   * 
   * @return true if this object is being painted. false if not.
   *  */
  public boolean isShowing() {
    return isShowing;
  }

  /**
   * Will push the given element to the back of the drawing layer. If the Element is in a group, the element will be
   * pushed to the back of that group's drawing layer.
   * 
   */
  public void pushToBack() {
    EZ.app.pushToBack(this);
  } // end push to back

  /**
   * Will push the given element to the back one drawing layer. If the Element is in a group, the element will be
   * pushed back once on that group's drawing layer.
   */
  public void pushBackOneLayer() {
    EZ.app.pushBackOneLayer(this);
  } // end push back one

  /**
   * Will pull the given element to the front of the drawing layer. If the Element is in a group, the element will be
   * pulled to the front of that group's drawing layer.
   */
  public void pullToFront() {
    EZ.app.pullToFront(this);
  } // end pull to front

  /**
   * Will pull the given element forward once on the drawing layer. If the Element is in a group, the element will be
   * pulled forward once on that group's drawing layer.
   */
  public void pullForwardOneLayer() {
    EZ.app.pullForwardOneLayer(this);
  } // end pull forward one

  /** Returns the current draw layer this object is one.
   * Lower index will be visibly covered by higher index.
   * @return layer index.
   */
  public int getLayer() {
    return EZ.app.getLayerPosition(this);
  }
  
  /**
   * Sets the draw layer of this. No change if the specified layer matches the current.
   * If moving to a higher index, the elements above will shift lower.
   * If moving to a lower index, the elements will shift higher.
   * 
   * @param layer to move to.
   */
  public void setLayer(int layer) {
    EZ.app.setLayerOfElement(this, layer);
  }
  
  /** 
   * Move this element below the specified element in the draw layer.
   * The placement is done by extraction and insert.
   * Will not work if they do not share the same container.
   * 
   * @param reference element to place this below.
   */
  public void placeBelow(EZElement reference) {
    EZ.app.setLayerBelow(this, reference);
  }
  
  /** 
   * Move this element above the specified element in the draw layer.
   * The placement is done by extraction and insert.
   * Will not work if they do not share the same container.
   * 
   * @param reference element to place above.
   */
  public void placeAbove(EZElement reference) {
    EZ.app.setLayerAbove(this, reference);
  }
  
  
  /**
   * Given a string figures out how long a string is.
   * 
   * @param s the string to calculate the width of.
   * @return int width in pixels.
   */
  protected static int getWidthOf(String s) {
    return EZ.app.getFontMetrics(EZ.app.getFont()).stringWidth(s);
  }

  /**
   * Used to calculated height of a given string.
   * 
   * @param s string to calculate height of.
   * @return int height in pixels.
   */
  protected static int getHeightOf(String s) {
    return EZ.app.getFontMetrics(EZ.app.getFont()).getHeight();
  }

  protected double rotationInDegrees = 0.0;
  protected double scaleWith1AsOriginal = 1.0;

  /**
   * Calling this will reset rotation and scale back to normal values, 0 and 1.0 respectively and
   * local coordinates will be set to 0,0.
   * */
  public abstract void identity();

  /**
   * This will rotate the image by specified degrees. Additive, doesn't override previous degree value. Positive
   * rotation is clockwise. Negative is counter clockwise. Values of 360 and beyond are as though it were degree % 360.
   * 
   * @param degrees to rotate. Positive is clockwise. Negative is counter clockwise.
   */
  public void rotateBy(double degrees) {
    rotationInDegrees += degrees;
  }

  /**
   * This will rotate the image to specified degree. Overrides previous degree value. Positive rotation is clockwise.
   * Negative is counter clockwise. Values of 360 and beyond are as though it were degree % 360.
   * 
   * @param degrees to rotate. Positive is clockwise. Negative is counter clockwise.
   * */
  public void rotateTo(double degrees) {
    rotationInDegrees = degrees;
  }

  /**
   * Will adjust the rotation of the element by turning to the left(counter clockwise) by the specified degrees. Note: if given a negative,
   * will have the opposite effect, will turn right(clockwise) instead.
   * @param degrees to rotate. Positive is left. Negative is right.
   */
  public void turnLeft(double degrees) {
    rotationInDegrees -= degrees;
  }

  /**
   * Will adjust the rotation of the element by turning to the right(clockwise) by the specified degrees. Note: if given a negative,
   * will have the opposite effect, will turn left(counter clockwise) instead.
   * @param degrees to rotate. Positive is right. Negative is left.
   */
  public void turnRight(double degrees) {
    rotationInDegrees += degrees;
  }

  /**
   * This will return the current rotation in degrees, a double value.
   * @return the rotation in degrees.
   * */
  public double getRotation() {
    return rotationInDegrees;
  }

  /**
   * Will scale by given value. Multiplicative over the current scale value. For example, if getScale() returns 2.0 and
   * then scale(3.0) the resulting scale value is 2.0 * 3.0 = 6.0.
   * @param s how much to scale the element by.
   * */
  public void scaleBy(double s) {
    scaleWith1AsOriginal *= s;
  }

  /**
   * Will set the scale to given value. This will replace previous scale value. For example, if getScale() returns 2.0
   * and then scaleTo(3.0) the resulting scale value is 3.0.
   * @param s value to set the scale at.
   * */
  public void scaleTo(double s) {
    scaleWith1AsOriginal = s;
  }

  /**
   * Returns the current scaling value. 1.0 means original scale value.
   * @return the current scale value.
   * */
  public double getScale() {
    return scaleWith1AsOriginal;
  }

  /** Used to store the parent of a Node. */
  private EZGroup parent = null;

  /**
   * Will set the parent to the given group ONLY if it doesn't already have a parent.
   * Generally you should not use this, it will be handled automatically by EZGroup.
   * 
   * @param g group to set the parent as.
   * @return true if successful. Otherwise false.
   * */
  public boolean setParent(EZGroup g) {
    if (parent == null) {
      parent = g;
      return true;
    }
    return false;
  }

  /**
   * Will remove the group parent from this element only if it already has one.
   * Generally you should not use this, it will be handled automatically by EZGroup.
   * 
   * @return true if successful removal. Otherwise false.
   */
  public boolean removeParent() {
    if (parent == null) {
      return false;
    }
    parent = null;
    return true;
  }

  /**
   * If this element has a parent returns true, otherwise false.
   * @return true if has a parent. Otherwise false.
   */
  public boolean hasParent() {
    if (parent == null) {
      return false;
    }
    return true;
  }

  /**
   * Will return the group which this element is located within.
   * 
   * @return EZGroup that this is part of.
   */
  public EZGroup getParent() {
    return parent;
  }

  /**
   * This will return a Shape of the bounds of this element with respect to the world space.
   * This is not a bounding box. This is the shape itself after transformations have been applied.
   * 
   * @return Shape instance of the bounds for this Element.
   */
  public abstract Shape getBounds();

  /**
   * This method will return a Shape which holds the bounds of the given shape that has all of the transformations of
   * the given EZElement applied to it. The transformations will take into account each of the groups that the given
   * EZElement is a part of. In brief, returns the bounds of the shape with respect to the world space.
   * 
   * @param os original shape before transformations.
   * @param oe EZElement which to pull transformations from to calculate bounds.
   * @return Shape which holds the final bounds with respect to world space.
   */
  public static Shape boundHelper(Shape os, EZElement oe) {
    Shape bs = os;
    bs = transformHelper(oe).createTransformedShape(bs);
    return bs;
  } // end boundHelper

  /**
   * Returns the transform before being applied to the shape. The transform is affected by all groups this element is
   * contained in.
   * 
   * @param oe The EZElement which to get the affine transform of.
   * @return the final AffineTransform that will be applied to the shape.
   */
  public static AffineTransform transformHelper(EZElement oe) {
    AffineTransform af = new AffineTransform();
    ArrayList<EZElement> ancestors = new ArrayList<EZElement>();
    EZElement temp;
    temp = oe;

    while (temp.hasParent()) {
      ancestors.add(temp.getParent());
      temp = temp.getParent();
    }

    for (int i = ancestors.size() - 1; i >= 0; i--) {
      temp = ancestors.get(i);
      af.translate(temp.getXCenter(), temp.getYCenter());
      af.scale(temp.getScale(), temp.getScale());
      af.rotate(Math.toRadians(temp.getRotation()));
    }
    af.translate(oe.getXCenter(), oe.getYCenter());
    af.scale(oe.getScale(), oe.getScale());
    af.rotate(Math.toRadians(oe.getRotation()));
    return af;
  } // end boundHelper

  /**
   * Checks if the given x,y coordinates(point) are within the shape of this element.
   * The check is made with respect to world space.
   * 
   * @param x coordinate of the point to check.
   * @param y coordinate of the point to check.
   * @return true if the specified point is within this element. Otherwise returns false.
   */
  public boolean isPointInElement(int x, int y) {
    return this.getBounds().contains(x, y);
  } // end is point in element.

} // end visual element class

/**
 * The EZCircle is used to create an Ellipse type of shape. A perfect circle isn't required and is
 * calculated based upon given width and height. When creating a circle, the given center coordinate, width and height
 * will specify a bounding box for the cirlce. From there, the circle drawn will attempt to make the most usage of the
 * given bounding box ensuring that should a line be placed along the vertical or horizontal axis the opposite sides
 * will be symmetrical.
 * 
 * @author Dylan Kobayashi
 *
 */
class EZCircle extends EZElement {

  /** Used for circle drawing, sizing and positioning. */
  protected Ellipse2D.Double circle;
  protected Ellipse2D.Double tempCircle;
  protected Shape transformCircle;

  /** Determines whether or not shape will be a solid color. */
  protected Boolean filled = false;
  /** Color of shape outline/fill. */
  protected Color color;

  /** Variables used for the center tracking to allow decimal translations. */
  private double xcd, ycd;

  /**
   * Creates a circle with the given specifications.
   * While this constructor is available for usage, it is highly recommended that you do not use this.
   * Instead call EZ.addCircle() method which will perform additional background actions to get the circle to display
   * on the window properly.
   * 
   * @param x center coordinate.
   * @param y center coordinate.
   * @param width of the circle.
   * @param height of the circle.
   * @param color to use when drawing.
   * @param filled status of whether the drawn circle should be a solid of the given color.
   */
  public EZCircle(int x, int y, int width, int height, Color color, boolean filled) {
    circle = new Ellipse2D.Double(x - width / 2, y - height / 2, width, height);
    // transformCircle = new Ellipse2D.Double(-width/2,-height/2,width,height);
    this.color = color;
    this.filled = filled;
    xcd = x;
    ycd = y;
  } // end constructor

  @Override public void paint(Graphics2D g2) {
    if (this.isShowing) {
      g2.setColor(color);

      if (filled) {
        g2.fill(this.getBounds());
      }
      else {
        g2.draw(this.getBounds());
      }

      /*
       * Commented out, unsure which is faster, affine transforms or moving of the g2 plane. g2.setColor(Color.RED);
       * tempCircle = (Ellipse2D.Double) circle.clone(); tempCircle.width = (int) (tempCircle.width *
       * scaleWith1AsOriginal); tempCircle.height = (int) (tempCircle.height * scaleWith1AsOriginal); tempCircle.x =
       * -(tempCircle.width/2); tempCircle.y = -(tempCircle.height/2); transformCircle =
       * AffineTransform.getRotateInstance(Math.toRadians(rotationInDegrees)).createTransformedShape(tempCircle);
       * transformCircle = AffineTransform.getTranslateInstance(circle.x + circle.width/2, circle.y +
       * circle.height/2).createTransformedShape(transformCircle); g2.draw(transformCircle); //
       */
    } // end if visible
  }// end paint

  @Override public void show() {
    isShowing = true;
  }

  @Override public void hide() {
    isShowing = false;
  }

  @Override public int getXCenter() {
    return (int) (circle.x + circle.width / 2);
  }

  /**
   * This will set the x center coordinate of the circle to the given value.
   * Made a private method since the user should be using translateBy and translateTo instead.
   * @param x coordinate the center will be set to.
   */
  private void setXCenter(double x) {
    xcd = x;
    circle.x = (int) xcd - circle.width / 2;
  }
  
  @Override public int getYCenter() {
    return (int) (circle.y + circle.height / 2);
  }

  /**
   * This will set the y center coordinate of the circle to the given value.
   * Made a private method since the user should be using translateBy and translateTo instead.
   * @param y coordinate the center will be set to.
   */
  private void setYCenter(double y) {
    ycd = y;
    circle.y = (int) y - circle.height / 2;
  }

  @Override public void identity() {
    setXCenter(0);
    setYCenter(0);
    rotationInDegrees = 0;
    scaleWith1AsOriginal = 1.0;
  }

  @Override public void translateTo(double x, double y) {
    xcd = x;
    ycd = y;
    circle.x = (int) x - circle.width / 2;
    circle.y = (int) y - circle.height / 2;
  }

  @Override public void translateBy(double x, double y) {
    xcd += x;
    ycd += y;
    circle.x = (int) xcd - circle.width / 2;
    circle.y = (int) ycd - circle.height / 2;
  }
  
  
  @Override public int getHeight() {
    return (int) circle.height;
  }

  /**
   * The circle can have its height changed. Does not affect width.
   * When applied, the center coordinate will not be affected.
   * 
   * @param h new height for the circle.
   */
  public void setHeight(int h) {
    circle.y = (circle.y + circle.height / 2) - h / 2;
    circle.height = h;
  }

  
  @Override public int getWidth() {
    return (int) circle.width;
  }

  /**
   * The circle can have its width changed. Does not affect height.
   * When applied, the center coordinate will not be affected.
   * 
   * @param w new width for the circle.
   */
  public void setWidth(int w) {
    circle.x = circle.x + circle.width / 2 - w / 2;
    circle.width = w;
  }

  
  @Override public Color getColor() {
    return color;
  }
  
  @Override public void setColor(Color c) {
    this.color = c;
  }
  
  @Override public boolean isFilled() {
    return filled;
  }
  
  @Override public void setFilled(boolean f) {
    filled = f;
  }
  
  @Override public Shape getBounds() {
    return EZElement.boundHelper(new Ellipse2D.Double(-circle.width / 2, -circle.height / 2, circle.width,
        circle.height), this);
  } // end get bounds

} // end visual circle class

/**
 * The EZRectangle is used to create an rectangle calculated off center coordinates, width, and height.
 * 
 * @author Dylan Kobayashi
 *
 */
class EZRectangle extends EZElement {

  /** Used for the dimensions of drawing. */
  protected Rectangle rect;
  protected Rectangle temprect;
  protected Shape transformRect;

  /** Determines whether or not shape will be a solid color. */
  protected Boolean filled = false;
  /** Color of shape outline/fill. */
  protected Color color;

  /** Used for tracking center with decimals. */
  private double xcd, ycd;

  /**
   * Creates a rectangle with the given specifications.
   * While this constructor is available for usage, it is highly recommended that you do not use this.
   * Instead call EZ.addRectangle() method which will perform additional background actions to get the rectangle to display
   * on the window properly.
   * 
   * @param x center coordinate.
   * @param y center coordinate.
   * @param width of the rectangle.
   * @param height of the rectangle.
   * @param color to use when drawing.
   * @param filled status of whether the drawn rectangle should be a solid of the given color.
   */
  public EZRectangle(int x, int y, int width, int height, Color color, boolean filled) {
    rect = new Rectangle(x - width / 2, y - height / 2, width, height);
    this.color = color;
    this.filled = filled;
    xcd = x;
    ycd = y;
  } // end constructor
  
  @Override public void paint(Graphics2D g2) {
    if (this.isShowing) {
      g2.setColor(color);

      if (filled) {
        g2.fill(this.getBounds());
      }
      else {
        g2.draw(this.getBounds());
      }
    }// end if visible

  }// end paint

  @Override public void show() {
    isShowing = true;
  }

  @Override public void hide() {
    isShowing = false;
  }

  @Override public int getXCenter() {
    return rect.x + rect.width / 2;
  }


  /**
   * This will set the x center coordinate of the rectangle to the given value.
   * Made a private method since the user should be using translateBy and translateTo instead.
   * @param x coordinate the center will be set to.
   */
  private void setXCenter(double x) {
    xcd = x;
    rect.x = (int) x - rect.width / 2;
  }

  @Override public int getYCenter() {
    return rect.y + rect.height / 2;
  }


  /**
   * This will set the y center coordinate of the rectangle to the given value.
   * Made a private method since the user should be using translateBy and translateTo instead.
   * @param y coordinate the center will be set to.
   */
  private void setYCenter(double y) {
    ycd = y;
    rect.y = (int) y - rect.height / 2;
  }

  @Override public void translateTo(double x, double y) {
    xcd = x;
    ycd = y;
    rect.x = (int) x - rect.width / 2;
    rect.y = (int) y - rect.height / 2;
  }

  @Override public void translateBy(double x, double y) {
    xcd += x;
    ycd += y;
    rect.x = (int) xcd - rect.width / 2;
    rect.y = (int) ycd - rect.height / 2;
  }

  @Override public void identity() {
    setXCenter(0);
    setYCenter(0);
    rotationInDegrees = 0;
    scaleWith1AsOriginal = 1.0;
  }

  @Override public int getHeight() {
    return rect.height;
  }
  
  
  /**
   * The rectangle can have its height changed. Does not affect width.
   * When applied, the center coordinate will not be affected.
   * 
   * @param h new height for the rectangle.
   */
  public void setHeight(int h) {
    rect.y = rect.y + rect.height / 2 - h / 2;
    rect.height = h;
  }

  @Override public int getWidth() {
    return rect.width;
  }
  
  /**
   * The rectangle can have its width changed. Does not affect height.
   * When applied, the center coordinate will not be affected.
   * 
   * @param w new width for the rectangle.
   */
  public void setWidth(int w) {
    rect.x = rect.x + rect.width / 2 - w / 2;
    rect.width = w;
  }

  @Override public Color getColor() {
    return color;
  }

  @Override public void setColor(Color c) {
    this.color = c;
  }

  @Override public boolean isFilled() {
    return filled;
  }

  @Override public void setFilled(boolean f) {
    filled = f;
  }

  @Override public Shape getBounds() {
    return EZElement.boundHelper(new Rectangle(-rect.width / 2, -rect.height / 2, rect.width, rect.height), this);
  }

} // end class rectangle

/**
 * Used to "paint" text on the screen.
 * The following should be taken into consideration when using EZText:<br>
 * -to change the displayed text use setMsg().<br>
 * -width and height cannot be directly set, it is a derivative of the message and text size.<br>
 * -center coordinates will not be influenced when the message is changed.<br>
 * -in order for left alignment to be applied to a text, you must calculate the offset after changing the message.
 * -text is always "filled", using the setFilled() method will not do anything.<br>
 * -text cannot be locally scaled. Use setFontSize() instead. However, text will be affected by group scales.<br>
 * 
 * @author Dylan Kobayashi
 */
class EZText extends EZElement {

  /** What will be displayed. */
  protected String msg;
  /** Color of text. */
  protected Color color;
  /** Used to specify x center. */
  protected double xCenter;
  /** Used to specify y center. */
  protected double yCenter;

  protected int fontSize = 20;
  

  /**
   * Creates text with the given specifications.
   * While this constructor is available for usage, it is highly recommended that you do not use this.
   * Instead call EZ.addText() method which will perform additional background actions to get the text to display
   * on the window properly.
   * 
   * @param x center coordinate for the text.
   * @param y center coordinate for the text.
   * @param msg to display.
   * @param color of the text.
   * @param fSize pixel size that the text will be drawn at.
   */
  public EZText(int x, int y, String msg, Color color, int fSize) {
    this.setXCenter(x);
    this.setYCenter(y);
    this.msg = msg;
    this.color = color;
    this.fontSize = fSize;
    this.fontName = EZ.app.getFont().getName();
    this.dFont = EZ.app.getFont();
  } // end constructor
  
  @Override public void paint(Graphics2D g2) {
    if (this.isShowing) {
      //g2.setFont(new Font(fontName, Font.PLAIN, fontSize));
      g2.setFont(  dFont.deriveFont( (float) fontSize) ); 
      g2.setColor(color);
      // only print if the message has visible characters.
      if (msg.trim().length() > 0) {
        AffineTransform tmp = g2.getTransform();
        g2.setTransform(EZElement.transformHelper(this));
        g2.drawString(msg, -getWidth() / 2, getHeight() / 3);
        g2.setTransform(tmp);
      }
    }
  }// end paint

  @Override public void show() {
    isShowing = true;
  }

  @Override public void hide() {
    isShowing = false;
  }

  /**
   * This will return what text is currently being displayed
   * @return String containing the text currently being displayed.
   */
  public String getMsg() {
    return msg;
  }

  /**
   * Will set the displayed text to the given parameter.
   * This will not render escape character. For example \n does not result in a new line.
   * If you want a new line, you need to create another EZText.
   * @param m String containing text to display.
   */
  public void setMsg(String m) {
    this.msg = m;
  }

  @Override public int getXCenter() {
    return (int) xCenter;
  }

  /**
   * This will set the x center coordinate of the text to the given value.
   * Made a private method since the user should be using translateBy and translateTo instead.
   * @param x coordinate the center will be set to.
   */
  private void setXCenter(double x) {
    this.xCenter = x;
  }

  @Override public int getYCenter() {
    return (int) yCenter;
  }
  
  /**
  * This will set the y center coordinate of the text to the given value.
  * Made a private method since the user should be using translateBy and translateTo instead.
  * @param y coordinate the center will be set to.
  */
  private void setYCenter(double y) {
    this.yCenter = y;
  }

  @Override public void translateTo(double x, double y) {
    this.xCenter = x;
    this.yCenter = y;
  }

  @Override public void translateBy(double x, double y) {
    this.xCenter += x;
    this.yCenter += y;
  }

  @Override public int getHeight() {
    //EZ.app.setFont(new Font(fontName, Font.PLAIN, fontSize));
    EZ.app.setFont( dFont.deriveFont( (float) fontSize) );
    return EZElement.getHeightOf(msg);
  }

  @Override public int getWidth() {
    //EZ.app.setFont(new Font(fontName, Font.PLAIN, fontSize));
    EZ.app.setFont( dFont.deriveFont( (float) fontSize) );
    return EZElement.getWidthOf(msg);
  }

  @Override public Color getColor() {
    return color;
  }

  @Override public void setColor(Color c) {
    this.color = c;
  }

  /**
   * Text is always "filled" as result this will always return true.
   * @return always returns true.*/
  @Override public boolean isFilled() {
    return true;
  }

  /**
   * Text cannot be set to unfilled. This method will not do anything.
   * @param f value will be discarded.
   * */
  @Override public void setFilled(boolean f) { }

  @Override public Shape getBounds() {
    return EZElement.boundHelper(
        new Rectangle(-this.getWidth() / 2, -this.getHeight() / 2, this.getWidth(), this.getHeight()), this);
  }

  @Override public void identity() {
    this.xCenter = 0;
    this.yCenter = 0;
    this.scaleWith1AsOriginal = 1.0;
    this.rotationInDegrees = 0;
  }

  /**
   * Text cannot be scaled. Use setFontSize() instead.
   * @param s will be discarded.
   * */
  @Override public void scaleBy(double s) { }

  /**
   * Text cannot be scalled. Use setFontSize() instead.
   * @param s will be discarded.
   * */
  @Override public void scaleTo(double s) { }

  /**
   * Changes the size of the font.
   * @param f size in pixels.
   * */
  public void setFontSize(int f) {
    this.fontSize = f;
  }

  /** 
   * Returns the size of the font.
   * @return size in pixels.*/
  public int getFontSize() {
    return this.fontSize;
  }

  /**Stores the name of the font used.*/
  private String fontName;
  /**Contains the Font object used for display.*/
  private Font dFont;

  /**Holds the loaded fonts from files.*/
  private static ArrayList<Font> loadedFonts = new ArrayList<Font>();
  /**Holds the loaded file names.*/
  private static ArrayList<String> loadedFiles = new ArrayList<String>();
  /**The font item itself which should be displayed.*/

  /**
   * Will print all available fonts to console.
   * Usage would be done before the program is complete so the use can verify available fonts and choose from among them.
   */
  public static void printAvailableFontsToConsole() {
    String[] names = getAllFontNames();
    for(String s : names){
      System.out.println(s);
    }
  }
  
  /**
   * Will return a String array containing all names of fonts available for usage specific to this machine.
   * While specific to this machine, unless you have tampered with java settings, the font set should the same
   * for other machines.
   * @return String[] containing names of available fonts.
   */
  public static String[] getAllFontNames() {
    return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
  }
  
  
  /**
   * Will attempt to set the font to the specified parameter. If the font is not available on the machine, an error
   * will be output to the console, but the program will not halt.
   * 
   * @param name of the font to use.
   */
  public void setFont(String name) {
    if(name.contains(".ttf") || name.contains(".TTF")){
      if( !checkIfAlreadyLoaded(name) ) {
        tryLoadFontFromFile(name);
      }
    }
    else {
      String[] allNames = getAllFontNames();
      boolean match = false;
      for(String n : allNames) {
        if(n.equals(name)){
          match = true;
          break;
        }
      }
      if(match){
        fontName = name;
        EZ.app.setFont(new Font(fontName, Font.PLAIN, fontSize));
        dFont = EZ.app.getFont();
      }
      else {
        System.out.println("ERROR: EZText is unable to use the specified font because it not on this system:" + name);
        System.out.println("  The change will not be applied.");
      }
    } //end else try look up a system font
  } //end setfont to
  
  /**
   * Will return the name of the font currently being used.
   * @return String containing the name of the font being  used.
   */
  public String getFont() {
    return fontName;
  }
  
  /**
   * Checks if the specified ttf file has already been loaded.
   * If it has will assign fontName and dFont correct value.
   * @param fName ttf file name to check if was already loaded.
   * @return true if it was and able to set the value, otherwise false.
   */
  private boolean checkIfAlreadyLoaded(String fName) {
    for(int i = 0; i < loadedFiles.size(); i++) {
      if( loadedFiles.get(i).equals(fName)){
        fontName = fName;
        dFont = loadedFonts.get(i);
        return true;
      }
    }//end for each loaded file name
    return false;
  }
  
  /**
   * Will try to load a font from the specified ttf file.
   * If successful, will correctly set drawing attributes.
   * Otherwise will output an error, but will not halt the program.
   * @param fName ttf file to load from.
   */
  private void tryLoadFontFromFile(String fName) {
    try {
      Font temp = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(fName));
      fontName = fName;
      dFont = temp;
      loadedFonts.add(temp);
      loadedFiles.add(fName);
    }
    catch (Exception e) {
      e.printStackTrace();
      System.out.println("ERROR: EZText was unable to load a text from the specified file:" + fName);
      System.out.println("  The change will not be applied.");
    }
  }
  
  
} // end visual text class

/**
 * The VisualImage is designed to draw an image from a file.
 * The following should be taken into consideration when using EZImage:<br>
 * -the image file associated on creation cannot be changed.<br>
 * -width and height cannot be directly set, it is based upon the image file itself.<br>
 * -getWidth() and getHeight() will return the values from the image file.<br>
 * -images are always "filled", using the setFilled() method will not do anything.<br>
 * -images cannot have color assigned to them, setColor() will not do anything and getColor() will always return black.<br>
 * -if the image has transparencies, they will work.<br>
 * <br>
 * Mechanical considerations: it is possible to create multiple EZImages that refer to the same image file. But for the sake of
 * memory and efficiency, they will all share the same loaded image data. One major reason for this is say you wanted to use a 1MB image.
 * Then you wanted to tile the image. You need to make one EZImage for each of the tiles. If each EZImage had to load the data
 * you would have 1MB * # of tiles memory usage. This can get very costly in memory very quickly. While there are some
 * negative aspects to this method, it is unlikely they will be encountered in the context of ICS111.
 * 
 * @author Dylan Kobayashi
 *
 */
class EZImage extends EZElement {
  //used for drawing.
  protected double xCenter;
  protected double yCenter;
  //reference to the image file.
  protected BufferedImage img;
  
  protected boolean imgHasFocus;
  protected int xtlf, ytlf, xbrf, ybrf; //x y t(op) or b(ottom) l(eft) or r(ight) f(ocus) values

  // Images are using static values to reduce memory usage. Otherwise, each time something was created there would be
  // memory set for each object using the image equal to the image size.
  protected static ArrayList<String> usedImageNames = new ArrayList<String>();
  protected static ArrayList<BufferedImage> loadedImages = new ArrayList<BufferedImage>();

  /**
   * This will check if the given image name has already been loaded into memory. If so, it will return that
   * BufferedImage, otherwise null.
   * 
   * @param imgName to check if was loaded.
   * @return corresponding BufferedImage or null.
   */
  protected static BufferedImage checkLoadedImages(String imgName) {
    for (int i = 0; i < usedImageNames.size(); i++) {
      if (usedImageNames.get(i).equals(imgName)) {
        return loadedImages.get(i);
      }
    }
    return null;
  } // end checkLoaded Images

  /**
   * Try load image will automatically search opened images before trying to allocate memory for an image. Will return a
   * BufferedImage or null.
   * 
   * @param imgName to try to open.
   * @return the BufferedImage or null.
   */
  protected static BufferedImage tryLoadImage(String imgName) {
    BufferedImage tempImg = checkLoadedImages(imgName);
    if (tempImg == null) {
      try {
        tempImg = ImageIO.read(new File(imgName));
      }
      catch (IOException e) {
        System.out.println("ERROR: Unable to open specified imagefile:" + imgName);
      }
    }
    return tempImg;
  } // end tryloadimage

  /**
   * Creates a visual image with the specifications.
   * While this constructor is available for usage, it is highly recommended that you do not use this.
   * Instead call EZ.addImage() method which will perform additional background actions to get the image to display
   * on the window properly.
   * 
   * @param filename of the image to use.
   * @param x center coordinate.
   * @param y center coordinate.
   */
  public EZImage(String filename, int x, int y) {
    img = tryLoadImage(filename);
    xCenter = x;
    yCenter = y;
  } // end constructor
  
  @Override public void paint(Graphics2D g2) {
    if (this.isShowing) {
      if (img == null) {
        g2.setColor(Color.BLACK);
        String err = "Failed to load image";
        int wos = EZElement.getWidthOf(err);
        int hos = EZElement.getHeightOf(err);
        g2.drawRect((int) xCenter - wos / 2 - 10, (int) yCenter - hos / 2 - 10, wos + 20, hos + 20);
        g2.drawString(err, (int) xCenter - wos / 2, (int) yCenter);
      }
      else {
        AffineTransform tmp = g2.getTransform();
        g2.setTransform(EZElement.transformHelper(this));
        if(imgHasFocus) {
          g2.drawImage(img,
              -(xbrf - xtlf)/2, -(ybrf-ytlf)/2,
              (xbrf - xtlf)/2, (ybrf-ytlf)/2,
              xtlf, ytlf,
              xbrf, ybrf, null);
        }
        else {
          g2.drawImage(img, -img.getWidth() / 2, -img.getHeight() / 2, null);
        }
        g2.setTransform(tmp);
      }
    }
  }// end paint

  @Override public void show() {
    isShowing = true;
  }

  @Override public void hide() {
    isShowing = false;
  }
  
  
  @Override public int getXCenter() {
    return (int) xCenter;
  }

  /**
   * This will set the x center coordinate of the image to the given value.
   * Made a private method since the user should be using translateBy and translateTo instead.
   * @param x coordinate the center will be set to.
   */
  private void setXCenter(double x) {
    xCenter = x;
  }
  
  
  @Override public int getYCenter() {
    return (int) yCenter;
  }


  /**
   * This will set the y center coordinate of the image to the given value.
   * Made a private method since the user should be using translateBy and translateTo instead.
   * @param y coordinate the center will be set to.
   */
  private void setYCenter(double y) {
    yCenter = y;
  }

  /**
   * The color of an image cannot be set.
   * @param c will be discarded.
   */
  @Override public void setColor(Color c) { }

  /**
   * Images do not have one color. It is determined by the contents of image file. Will return BLACK by default.
   * @return always Color.BLACK.
   * */
  @Override public Color getColor() {
    return Color.BLACK;
  }

  /**
   *  Images are technically always filled with their specified image.
   *  @return always true.
   */
  @Override public boolean isFilled() {
    return true;
  }

  /**
   * Images cannot have their fill status changed. This method won't do anything.
   * @param f will be discarded.
   */
  @Override public void setFilled(boolean f) { }

  @Override public void translateTo(double x, double y) {
    xCenter = x;
    yCenter = y;
  }
  
  @Override public void translateBy(double x, double y) {
    xCenter += x;
    yCenter += y;
  }

  @Override public void identity() {
    setXCenter(0);
    setYCenter(0);
    rotationInDegrees = 0;
    scaleWith1AsOriginal = 1.0;
  }
  
  @Override public int getHeight() {
    if(imgHasFocus) { return (ybrf - ytlf);  }
    return img.getHeight();
  }
  
  @Override public int getWidth() {
    if(imgHasFocus) { return (xbrf - xtlf);  } 
    return img.getWidth();
  }

  @Override public Shape getBounds() {
    return EZElement.boundHelper(
        new Rectangle(-getWidth() / 2, -getHeight() / 2, getWidth(), getHeight()), this);
  }
  
  
  /**
   * Will set a focus area on the image that will be displayed instead of the entire image. The focus
   * area is determined by a rectangle shape formed by two points. The first two parameters
   * represent the top left corner of the rectangle while the last two parameters represent the bottom right
   * corner of the rectangle.
   * <br>
   * There are NO restricts on the values given. This allows points which do not exist on the image.
   * If the focus area includes coordinates which are not part of the image, they are assumed to be fully transparent.
   * Swapping the coordinates will flip the image.
   * @param xTopLeftCorner
   * @param yTopLeftCorner
   * @param xBottomRightCorner
   * @param yBottomRightCorner
   */
  public void setFocus(int xTopLeftCorner, int yTopLeftCorner, int xBottomRightCorner, int yBottomRightCorner) {
    xtlf = xTopLeftCorner;
    ytlf = yTopLeftCorner;
    xbrf = xBottomRightCorner;
    ybrf = yBottomRightCorner;
    imgHasFocus = true;
  }
  
  /**
   * If a focus area has been set, it will be released and the entire image will be shown.
   * Otherwise no effect.
   */
  public void releaseFocus() {
    imgHasFocus = false;
  }
  
  /**
   * Returns whether or not the image has a focus area set.
   * @return true if a focus area has been set, otherwise false.
   */
  public boolean hasFocus() { return imgHasFocus; }

} // end visual image class

/**
 * The EZLine is used to draw a line between two specified points.
 * The following should be taken into consideration when using EZLine:<br>
 * -width and height cannot be directly set, it is based upon the points that define the line.<br>
 * -getWidth() and getHeight() do not return the "length" or thickness of the line. Instead, width returns the difference between the x coordinates of the two points, while height returns the difference between the y coordinates of the two points.<br>
 * -getXCenter() and getYCenter() will give the center coordinate of the line.
 * -line rotation is base upon the idea that the line starts off horizontal at that center x,y coordinate. Rotation is the amount necessary to make the line connect the two points pivoting around the center coordinate.
 * -lines are always "filled", using the setFilled() method will not do anything.<br>
 * -lines cannot be locally scaled. To change the size use setThickness(), setPoint1() and setPoint2().<br>
 * <br>
 * 
 * 
 * @author Dylan Kobayashi
 *
 */
class EZLine extends EZElement {
  private Rectangle rsub;

  private int hypot, x1, x2, y1, y2;
  private double originalDegrees;

  protected Color color;

  private double cx, cy;

  /**
   * Creates a line with the specifications. Thickness less than 1 will be automatically increased to 1.
   * 
   * While this constructor is available for usage, it is highly recommended that you do not use this.
   * Instead call EZ.addLine() method which will perform additional background actions to get the line to display
   * on the window properly.
   * 
   * @param x1 coordinate of point 1.
   * @param y1 coordinate of point 1.
   * @param x2 coordinate of point 2.
   * @param y2 coordinate of point 2.
   * @param thickness of the line.
   * @param color to draw the line in.
   */
  public EZLine(int x1, int y1, int x2, int y2, Color color, int thickness) {
    int dx, dy;
    dx = x2 - x1;
    dy = y2 - y1;
    this.cx = dx / 2 + x1;
    this.cy = dy / 2 + y1;
    this.hypot = (int) Math.hypot(dx, dy);
    if (thickness < 1) {
      thickness = 1;
    } // cannot have a thickness less than 1.

    rsub = new Rectangle(-hypot / 2, -thickness / 2, hypot, thickness);

    rotationInDegrees = originalDegrees = Math.toDegrees(Math.atan2(dy, dx));

    this.color = color;

    this.x1 = x1; // used for get methods();
    this.x2 = x2;
    this.y1 = y1;
    this.y2 = y2;

  } // end constructor
  
  
  @Override public void paint(Graphics2D g2) {
    if (this.isShowing) {
      g2.setColor(color);
      g2.fill(this.getBounds());
    }
  }// end paint

  @Override public void show() {
    isShowing = true;
  }

  @Override public void hide() {
    isShowing = false;
  }

  /**
   * Returns the x coordinate of point 1.
   * @return the x coordinate of point 1.
   */
  public int getX1() {
    return x1;
  }


  /**
   * Returns the y coordinate of point 1.
   * @return the y coordinate of point 1.
   */
  public int getY1() {
    return y1;
  }


  /**
   * Returns the x coordinate of point 2.
   * @return the x coordinate of point 2.
   */
  public int getX2() {
    return x2;
  }


  /**
   * Returns the y coordinate of point 2.
   * @return the y coordinate of point 2.
   */
  public int getY2() {
    return y2;
  }

  /**
   * Changes the coordinate of point 1, which will probably result in a change of center and rotation.
   * @param x coordinate point 1 will be set to.
   * @param y coordinate point 1 will be set to.
   */
  public void setPoint1(int x, int y) {
    x1 = x;
    y1 = y;
    int dx = x2 - x1;
    int dy = y2 - y1;
    this.cx = dx / 2 + x1;
    this.cy = dy / 2 + y1;
    this.hypot = (int) Math.hypot(dx, dy);
    rsub.width = hypot;
    rotationInDegrees = originalDegrees = Math.toDegrees(Math.atan2(dy, dx));
  } // end set point 1

  /**
   * Changes the position of point 2, which will probably result in a change of length, center and rotation.
   * @param x coordinate point 2 will be set to.
   * @param y coordinate point 2 will be set to.
   * */
  public void setPoint2(int x, int y) {
    x2 = x;
    y2 = y;
    int dx = x2 - x1;
    int dy = y2 - y1;
    this.cx = dx / 2 + x1;
    this.cy = dy / 2 + y1;
    this.hypot = (int) Math.hypot(dx, dy);
    rsub.width = hypot;
    rotationInDegrees = originalDegrees = Math.toDegrees(Math.atan2(dy, dx));
  } // end set point 1

  /**
   * Will increase the thickness of the line to given parameter.
   * @param t new thickness in pixels.
   */
  public void setThickness(int t) {
    this.rsub.height = t;
  }

  /**
   * Returns the current thickness of the line.
   * @return thickness in pixels.
   */
  public int getThickness() {
    return this.rsub.height;
  }

  
  @Override public Color getColor() {
    return color;
  }

  @Override public void setColor(Color nv) {
    this.color = nv;
  }

  /**
   *  Lines are technically always filled.
   *  @return always true.
   */
  public boolean isFilled() {
    return true;
  }

  /**
   * Lines cannot have their fill status changed. This method won't do anything.
   * @param f will be discarded.
   */
  public void setFilled(boolean f) { }

  /**
   * The value returned is actually the difference between the x coordinates of the two points.
   * The returned value will always be positive, even if point 2 has an x value that is less than point 1.
   * 
   *  @return difference between the two point's x coordinate as a positive value.
   */
  @Override public int getWidth() {
    return (Math.abs(x1 - x2) + 1);
  }


  /**
   * The value returned is actually the difference between the y coordinates of the two points.
   * The returned value will always be positive, even if point 2 has an y value that is less than point 1.
   * 
   *  @return difference between the two point's y coordinate as a positive value.
   */
  @Override public int getHeight() {
    return (Math.abs(y1 - y2) + 1);
  }

  /**
   * This returns the x coordinate of the center of the line.
   * @return x coordinate.
   */
  @Override public int getXCenter() {
    return (int) cx;
  }

  /**
   * This returns the y coordinate of the center of the line.
   * @return y coordinate.
   */
  @Override public int getYCenter() {
    return (int) cy;
  }

  /**
   * Translations will alter both point locations that the line connects while preserving the rotation.
   * @param x coordinate the center will be set to.
   * @param y coordinate the center will be set to.
   */
  @Override public void translateTo(double x, double y) {
    cx = x;
    cy = y;

    int dx = (int) (Math.sin(Math.toRadians(rotationInDegrees)) * rsub.width);
    int dy = (int) (Math.cos(Math.toRadians(rotationInDegrees)) * rsub.width);

    this.x1 = (int) x - dx / 2;
    this.x2 = (int) x + dx / 2;
    this.y1 = (int) y + dy / 2;
    this.y2 = (int) y - dy / 2;
  }


  /**
   * Translations will alter both point locations that the line connects while preserving the rotation.
   * @param x coordinate the center will be shifted by.
   * @param y coordinate the center will be shifted by.
   */
  @Override public void translateBy(double x, double y) {
    cx += x;
    cy += y;

    this.x1 += x;
    this.x2 += x;
    this.y1 += y;
    this.y2 += y;
  }

  @Override public Shape getBounds() {
    return EZElement.boundHelper(new Rectangle(-rsub.width / 2, -rsub.height / 2, rsub.width, rsub.height), this);
  }

  /**
   * Lines cannot be scaled. Use the set point methods.
   * @param s will be discarded.
   * */
  @Override public void scaleBy(double s) { }

  /**
   * Lines cannot be scaled. Use the set point methods.
   * @param s will be discarded.
   * */
  @Override public void scaleTo(double s) { }

  @Override public void identity() {
    this.cx = 0;
    this.cy = 0;
    this.rotationInDegrees = originalDegrees;
    int dx = (int) (Math.sin(Math.toRadians(rotationInDegrees)) * rsub.width);
    int dy = (int) (Math.cos(Math.toRadians(rotationInDegrees)) * rsub.width);

    this.x1 = -dx / 2;
    this.x2 = dx / 2;
    this.y1 = dy / 2;
    this.y2 = -dy / 2;
  }

} // end visual line class

/**
 * The EZPolygon is used to draw a polygon. The shape of a polygon is defined by a series of points. When drawn, lines
 * will be made to connect each of the points. The last point will have a line connecting it to the first point.
 * 
 * @author Dylan Kobayashi
 *
 */
class EZPolygon extends EZElement {

  /** The polygon used to draw the shape and do calculations. */
  protected Polygon drawShape;
  protected Polygon tempShape;
  protected Shape transformShape;
  protected Color color;
  protected boolean filled;

  protected boolean error = false;
  /** Used to track center with decimal values. */
  private double xcd, ycd;
  
  /**
   * Creates a polygon with the specifications.
   * 
   * While this constructor is available for usage, it is highly recommended that you do not use this.
   * Instead call EZ.addPolygon() method which will perform additional background actions to get the polygon to display
   * on the window properly.
   * 
   * @param xp an array containing each of the point's x coordinates in sequence.
   * @param yp an array containing each of the point's y coordinates in sequence.
   * @param c color to draw the polygon in.
   * @param f whether or not the polygon will be filled.
   */
  public EZPolygon(int[] xp, int[] yp, Color c, boolean f) {
    color = c;
    filled = f;
    xcd = ycd = 0;
    try {
      drawShape = new Polygon(xp, yp, xp.length);
      xcd = drawShape.getBounds().getCenterX();
      ycd = drawShape.getBounds().getCenterY();
    }
    catch (Exception e) {
      error = true;
      e.printStackTrace();
    }

    if (xp.length < 3) {
      error = true;
      System.out.println("Polygon creation needs at least 3 points.");
    }

  } // end constructor
  
  @Override public void paint(Graphics2D g2) {
    if (this.isShowing) {
      g2.setColor(color);

      if (!error) {
        if (filled) {
          g2.fill(this.getBounds());
        }
        else {
          g2.draw(this.getBounds());
        }
      }
      else {
        g2.drawString("Error with polygon, see console for more details", 100, 100);
      }
    }
  }// end paint

  @Override public void show() {
    isShowing = true;
  }

  @Override public void hide() {
    isShowing = false;
  }


  @Override public Color getColor() {
    return color;
  }

  @Override public void setColor(Color c) {
    color = c;
  }

  @Override public boolean isFilled() {
    return filled;
  }

  @Override public void setFilled(boolean c) {
    filled = c;
  }

  @Override public int getXCenter() {
    return (int) xcd;
  }

  @Override public int getYCenter() {
    return (int) ycd;
  }

  @Override public int getWidth() {
    return (int) drawShape.getBounds().getWidth();
  }

  @Override public int getHeight() {
    return (int) drawShape.getBounds().getHeight();
  }
  
  /**
   * Sets the center of the polygon to the given value.
   * Made a private method since the user should be using translateBy and translateTo instead.
   * @param cx coordinate the x center will be set to.
   */
  private void setXCenter(double cx) {
    xcd = cx;
    drawShape.translate(-1 * (int) (drawShape.getBounds().getCenterX()), -1
        * (int) (drawShape.getBounds().getCenterY()));
    drawShape.translate((int) xcd, (int) ycd);
  }

  /**
   * Sets the center of the polygon to the given value.
   * Made a private method since the user should be using translateBy and translateTo instead.
   * @param cy coordinate the y center will be set to.
   */
  private void setYCenter(double cy) {
    ycd = cy;
    drawShape.translate(-1 * (int) (drawShape.getBounds().getCenterX()), -1
        * (int) (drawShape.getBounds().getCenterY()));
    drawShape.translate((int) xcd, (int) ycd);
  }

  @Override public void translateTo(double x, double y) {
    this.setXCenter(x);
    this.setYCenter(y);
  }

  @Override public void translateBy(double x, double y) {
    xcd += x;
    ycd += y;
    this.setXCenter(xcd);
    this.setYCenter(ycd);
  }

  @Override public void identity() {
    this.setXCenter(0);
    this.setYCenter(0);
    rotationInDegrees = 0;
    scaleWith1AsOriginal = 1.0;
  }
  
  @Override public Shape getBounds() {
    tempShape = new Polygon(drawShape.xpoints, drawShape.ypoints, drawShape.xpoints.length);
    tempShape.translate(-1 * (tempShape.getBounds().x + tempShape.getBounds().width/2), -1 * (tempShape.getBounds().y + tempShape.getBounds().height/2));
    return EZElement.boundHelper(tempShape, this);
  }

} // end visual polygon class

/**
 * This class is designed to collect keyboard and mouse input for the window.
 * The methods will not work unless EZ.initialize() has been called.
 * 
 * @author Dylan Kobayashi
 *
 */
class EZInteraction implements KeyListener, MouseInputListener {

  /** Used as for external referencing. */
  public static EZInteraction app;

//  /** Used for tracking keys down. */
//  protected ArrayList<Character> keysDown = new ArrayList<Character>();
//  /** Used for tracking key releases. */
//  protected ArrayList<Character> keysReleased = new ArrayList<Character>();
//  /** Used for tracking key presses. */
//  protected ArrayList<Character> keysPressed = new ArrayList<Character>();

  /** Used for tracking keys down. */
  protected Map<String, Integer> keysDown = new HashMap<>();
  /** Used for tracking key releases. */
  protected Map<String, Integer> keysReleased = new HashMap<>();
  /** Used for tracking key presses. */
  protected Map<String, Integer> keysPressed = new HashMap<>();

  /** Used for indexing. */
  protected int keysDownIndex = 0;

  /**
   * Used for tracking mouse clicks. This is not very efficient and currently just used as a means to get click
   * detection that the students can call from an outside location.
   */
  private static int mMoveX;
  private static int mMoveY;
  private static long keyrLastUpdate;
  private static long keypLastUpdate;
  private static boolean keyrCheckInitiated = false;
  private static boolean keypCheckInitiated = false;
  private static long TIMEOUT = 1; // 1 ms storage for clicks.
  private static boolean mlbPressed = false;
  private static boolean mlbDown = false;
  private static boolean mlbReleased = false;
  private static boolean mrbPressed = false;
  private static boolean mrbDown = false;
  private static boolean mrbReleased = false;
  private static long mlbPTime = 0; //used to store their press and release time.
  private static long mlbRTime = 0;
  private static long mrbPTime = 0;
  private static long mrbRTime = 0;

  /**
   * Constructor's job is to associate external public reference.
   * You should not be using this. EZ.initialize() will take care of this in the background.
   * */
  public EZInteraction() {
    app = this;
  }

  
  @Override public void keyPressed(KeyEvent e) {
    keypCheckInitiated = false;
    String keyToUse;
    int valueToUse;
    try {
      if ( e.getKeyCode() == KeyEvent.VK_UP ) { keyToUse = "VK_UP"; valueToUse = e.getKeyCode(); }
      else if ( e.getKeyCode() == KeyEvent.VK_DOWN ) { keyToUse = "VK_DOWN"; valueToUse = e.getKeyCode(); }
      else if ( e.getKeyCode() == KeyEvent.VK_LEFT ) { keyToUse = "VK_LEFT"; valueToUse = e.getKeyCode(); }
      else if ( e.getKeyCode() == KeyEvent.VK_RIGHT ) { keyToUse = "VK_RIGHT"; valueToUse = e.getKeyCode(); }
      else if ( ! Character.isLetterOrDigit( e.getKeyChar() ) ) { keyToUse = "kc" + e.getKeyCode(); valueToUse = e.getKeyCode(); }
      else {
        keyToUse = "" + e.getKeyChar();
        valueToUse = e.getKeyCode();
      }
      keysDown.put(keyToUse, valueToUse);
      keysPressed.put(keyToUse, valueToUse);
    }
    catch (Exception ex) {
      System.out
          .println("Unexpected thread sync conflict in key detection.\n---Problem has been handled, but may have lost key input in the process.");
      ex.printStackTrace();
    }
  } // end keypressed

  
  @Override public void keyReleased(KeyEvent e) {
    keyrCheckInitiated = false;
    String keyToUse;
    int valueToUse;
    
    try {
      if ( e.getKeyCode() == KeyEvent.VK_UP ) { keyToUse = "VK_UP"; valueToUse = e.getKeyCode(); }
      else if ( e.getKeyCode() == KeyEvent.VK_DOWN ) { keyToUse = "VK_DOWN"; valueToUse = e.getKeyCode(); }
      else if ( e.getKeyCode() == KeyEvent.VK_LEFT ) { keyToUse = "VK_LEFT"; valueToUse = e.getKeyCode(); }
      else if ( e.getKeyCode() == KeyEvent.VK_RIGHT ) { keyToUse = "VK_RIGHT"; valueToUse = e.getKeyCode(); }
      else if ( ! Character.isLetterOrDigit( e.getKeyChar() ) ) { keyToUse = "kc" + e.getKeyCode(); valueToUse = e.getKeyCode(); }
      else { keyToUse = "" + e.getKeyChar(); valueToUse = e.getKeyCode(); }
      
      keysReleased.put(keyToUse, valueToUse);
      keysDown.remove(keyToUse);
      
    }// end try
    catch (Exception ex) {
      System.out
          .println("Unexpected thread sync conflict in key detection.\n---Problem has been handled, but may have lost key input in the process.");
      ex.printStackTrace();
    }

  } // end key released

  /**
   * Used for actively checking if a key is down(being pressed).
   * 
   * @param key to check for.
   * @return true if the key is down. Otherwise false.
   */
  public static boolean isKeyDown(String key) {
    try {
      return app.keysDown.containsKey(key);
    }
    catch (Exception e) {
      /*
       * this try catch is for the rare case where the key might start off "down" but then becomes "up" while in the
       * process of going through the map. The error is result of the size starting off "larger" but because a key
       * get's released, the map decreases in size resulting in an error that may crash the thread that calls this
       * method, because thread timing is concurrent.
       * 
       * Anyone reading this, yes the code actually utilizes multiple threads.
       */
    }
    return false;
  } // end is key down
  
  /** Overload command to backwards compatible char call. */
  public static boolean isKeyDown(char c) { return isKeyDown("" + c); }
  /** Overload command to allow keycode checks. */
  public static boolean isKeyDown(int code) { return app.keysDown.containsValue(code); }

  /**
   * Checks if a key was just released. See description for getXMouseClick(), uses the same timing ideology.
   * */
  public static boolean wasKeyReleased(String key) {
    if (!keyrCheckInitiated) {
      keyrCheckInitiated = true;
      keyrLastUpdate = System.currentTimeMillis();
    }
    if (keyrLastUpdate + TIMEOUT < System.currentTimeMillis()) {
      app.keysReleased.clear();
    }
    try {
      return app.keysReleased.containsKey(key);
    }// end try
    catch (Exception e) {
    }
    return false;
  } // end waskey released

  /** Overload command to backwards compatible char call. */
  public static boolean wasKeyReleased(char c) { return wasKeyReleased("" + c); }
  /** Overload command to allow keycode checks. */
  public static boolean wasKeyReleased(int code) { wasKeyReleased(""); return app.keysReleased.containsValue(code); }

  /**
   * Checks if a key was just pressed. See description for getXMouseClick(), uses the same timing ideology.
   * */
  public static boolean wasKeyPressed(String key) {
    try {
      if (!keypCheckInitiated) {
        keypCheckInitiated = true;
        keypLastUpdate = System.currentTimeMillis();
      }
      if (keypLastUpdate + TIMEOUT < System.currentTimeMillis()) {
        app.keysPressed.clear();
      }
      return app.keysPressed.containsKey(key);
    }// end try
    catch (Exception e) {
    }
    return false;
  } // end waskey released

  /** Overload command to backwards compatible char call. */
  public static boolean wasKeyPressed(char c) { return wasKeyPressed("" + c); }
  /** Overload command to allow keycode checks. */
  public static boolean wasKeyPressed(int code) { wasKeyPressed(""); return app.keysPressed.containsValue(code); }

  
  @Override public void mousePressed(MouseEvent me) {
    // System.out.println("Mouse pressed");
    if (me.getButton() == 1) {
      mlbPressed = true;
      mlbDown = true;
      mlbReleased = false;
      mlbPTime = -1;
    }
    else if(me.getButton() == 3) {
      mrbPressed = true;
      mrbDown = true;
      mrbPTime = -1;
      mrbReleased = false;
    }
  }
  
  @Override public void mouseReleased(MouseEvent me) {
    if (me.getButton() == 1) { //left
      mlbPressed = false;
      mlbDown = false;
      mlbReleased = true;
      mlbRTime = -1;
    }
    else if(me.getButton() == 3) { //right
      mrbPressed = false;
      mrbDown = false;
      mrbReleased = true;
      mrbRTime = -1;
    }
  }// System.out.println("Mouse released"); }

  
  /**
   * Updated version of left button click status.
   * Used to detect if the left button was pressed.
   * There is a TIMEOUT associated to allow multiple calls within a particular update.
   * @return true if the left mouse button was just pressed, otherwise false.
   */
  public static boolean wasMouseLeftButtonPressed() {
    if(mlbPTime == -1 && mlbDown) {  mlbPTime = System.currentTimeMillis();  }
    if(mlbPTime + TIMEOUT <= System.currentTimeMillis() ) {
      mlbPressed = false;
    }
    return mlbPressed;
  }
  /**
   * Updated version of right button click status.
   * Used to detect if the right button was pressed.
   * There is a TIMEOUT associated to allow multiple calls within a particular update.
   * @return true if the right mouse button was just pressed, otherwise false.
   */
  public static boolean wasMouseRightButtonPressed() {
    if(mrbPTime == -1 && mrbDown) {  mrbPTime = System.currentTimeMillis();  }
    if(mrbPTime + TIMEOUT <= System.currentTimeMillis() ) {
      mrbPressed = false;
    }
    return mrbPressed;
  }
  
  /**
   * Used to detect if the left button is down.
   * @return true if the left mouse button is down, otherwise false.
   */
  public static boolean isMouseLeftButtonDown() {
    return mlbDown;
  }
  
  /**
   * Used to detect if the right button is down.
   * @return true if the right mouse button is down, otherwise false.
   */
  public static boolean isMouseRightButtonDown() {
    return mrbDown;
  }
  /**
   * Used to detect if the left button was released.
   * There is a TIMEOUT associated to allow multiple calls within a particular update.
   * @return true if the left mouse button was just pressed, otherwise false.
   */
  public static boolean wasMouseLeftButtonReleased() {
    if(mlbRTime == -1 && !mlbDown) {  mlbRTime = System.currentTimeMillis();  }
    if(mlbRTime + TIMEOUT <= System.currentTimeMillis() ) {
      mlbReleased = false;
    }
    return mlbReleased;
  }
  
  /**
   * Used to detect if the right button was released.
   * There is a TIMEOUT associated to allow multiple calls within a particular update.
   * @return true if the right mouse button was just pressed, otherwise false.
   */
  public static boolean wasMouseRightButtonReleased() {
    if(mrbRTime == -1 && !mrbDown) {  mrbRTime = System.currentTimeMillis();  }
    if(mrbRTime + TIMEOUT <= System.currentTimeMillis() ) {
      mrbReleased = false;
    }
    return mrbReleased;
  }
  

  
  @Override public void mouseExited(MouseEvent arg0) {
    mMoveX = -1;
    mMoveY = -1;
  }

  @Override public void mouseMoved(MouseEvent me) {
    mMoveX = me.getX();
    mMoveY = me.getY();
  }

  @Override public void mouseDragged(MouseEvent me) {
    mMoveX = me.getX();
    mMoveY = me.getY();
  }// System.out.println("Mouse drag"); }


  /**
   * Returns the x coordinate of the mouse if it is over the window.
   * @return x coordinate of the mouse if over the window. Otherwise -1.
   */
  public static int getXMouse() {
    return mMoveX;
  }

  /**
   * Returns the y coordinate of the mouse if it is over the window.
   * @return y coordinate of the mouse if over the window. Otherwise -1.
   */
  public static int getYMouse() {
    return mMoveY;
  }

  @Override public void mouseClicked(MouseEvent arg0) {
  }

  @Override public void mouseEntered(MouseEvent arg0) {
  }
  
  @Override public void keyTyped(KeyEvent e) {
    // System.out.println("key typed");
  }

} // end input handler class

/**
 * The EZ is designed to draw an image from a file.
 * The following should be taken into consideration when using EZSound:<br>
 * -ONLY works with .wav files.<br>
 * -the associated sound cannot be changed once created, but that is ok due to storage type the overhead is relatively low. <br>
 * -while the sound is managed by EZ, EZSound is not an EZElement, as it is not draw.<br>
 * -sound files are stored in a static history with the AudioInputStream to reduce memory usage. A process similar to EZImage.<br>
 * 
 * @author Dylan Kobayashi
 */
class EZSound {
  protected static ArrayList<AudioInputStream> aisList = new ArrayList<AudioInputStream>();
  protected static ArrayList<String> aisFile = new ArrayList<String>();

  //protected Clip sound;
  protected Clip sound;
  protected String filename;


  /**
   * Creates a new sound out of the given file. Must be a .wav file.
   * 
   * While this constructor is available for usage, it is highly recommended that you do not use this.
   * Instead call EZ.addSound() method which will perform additional background actions to bind the sound to the window.
   * 
   * @param file of the sound to load.
   * */
  public EZSound(String file) {
    /*
    filename = file;
    sound = tryLoadSound(file);
    if (sound == null) {
      System.out.println("Error loading sound file");
      System.exit(1);
    }
    if (sound == null) {
      reloadClip();
    }
    */
    try {
      AudioInputStream ais = AudioSystem.getAudioInputStream(new File(file).getAbsoluteFile());
      sound = AudioSystem.getClip();
      sound.open(ais);
    }
    catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error loading sound file, it may not exist or another program has a lock on it.");
      System.exit(1);
    }
  }// end constructor


  /**
   * This will play the sound file from wherever the current position is.
   * 
   */
  public void play() {
    if( sound.getFramePosition() == sound.getFrameLength()  || 
        (sound.getFramePosition() != 0 && sound.isRunning()) ) {
      sound.setFramePosition(0);
    }
    sound.start();
  }

  /**
   * This will stop the sound and reset back to the start.
   */
  public void stop() {
    sound.stop();
    sound.setFramePosition(0);
  } // end stop()
  
  /**
   *  Will pause the sound at it's current position. Using play() will resume from this point.
   */
  public void pause() {
    sound.stop();
  }

  /**
   * Will play from the start and loop the sound... again... and again... and again...
   * 
   */
  public void loop() {
    sound.setFramePosition(0);
    sound.loop(Clip.LOOP_CONTINUOUSLY);
  }
  
  /** 
   * Returns how many frames are held within this sound file.
   * @return Positive int value including zero indicating number of frames.
   * Otherwise -1 to indicate that the file's length cannot be determined.
   */
  public int getFrameLength() {
    return sound.getFrameLength();
  }
  
  /**
   * Returns the current frame of the sound file.
   * @return Positive int value including zero indicating the current frame.
   */
  public int getFramePosistion() {
    return sound.getFramePosition();
  }
  
  /**
   * Returns the total length of the sound file in microseconds.
   * @return Positive long value including zero indicating the length of the sound file.
   * Otherwise -1 to indicate the file's length cannot be determined.
   */
  public long getMicroSecondLength() {
    return sound.getMicrosecondLength();
  }
  
  /**
   * Returns the current position in microseconds.
   * @return Positive long value including zero indicating the position in the sound file.
   * Otherwise -1 to indicate the file's position cannot be determined.
   */
  public long getMicroSecondPosition() {
    return sound.getMicrosecondPosition();
  }
  
  /**
   * Sets the position in frames from which to continue playing.
   * This will be overridden if stop() or loop() is called after this(they reset back to start).
   * @param pos frame of the file to start from.
   */
  public void setFramePosition(int pos) {
    sound.setFramePosition(pos);
  }

  
  /**
   * Sets the position in microseconds from which to continue playing.
   * This will be overridden if stop() or loop() is called after this(they reset back to start).
   * Note: the level of precision is based upon ms per frame.
   * @param pos milliseconds of the file to start from.
   */
  public void setMicrosecondPosition(int pos) {
    sound.setMicrosecondPosition(pos);
  }
  
  
} // end class

/**
 * A means to group EZElements together and manipulate them as one element.
 * 
 * Adding an element to a group does the following effects:<br>
 * |-Element center coordinates use the group's center as origin.<br>
 * |-This may cause the elements coordinates to change.<br>
 * |-The element will no longer be tracked by EZ. It will now tracked by the group.<br>
 * |-Adjusting draw layer will be limited to the group's draw layer.<br>
 * |-pushToBack, pushBackOneLayer,pullToFront,pullForwardOneLayer will be limited to the group.<br>
 * 
 * 
 * @author Dylan Kobayashi
 *
 */
class EZGroup extends EZElement {

  /** The x center value of this node. */
  private double xCurrent = 0;
  /** The y center value of this node. */
  private double yCurrent = 0;

  /** This will hold all children which are considered to be part of this node. */
  private ArrayList<EZElement> children = new ArrayList<EZElement>();

  /**
   * Creates a group. Center position starts at 0,0. Rotations will be made around center location.
   * 
   * While this constructor is available for usage, it is highly recommended that you do not use this.
   * Instead call EZ.addGroup() method which will perform additional background actions to get the group to display
   * on the window properly.
   */
  public EZGroup() {
    this.rotationInDegrees = 0;
    this.scaleWith1AsOriginal = 1.0;
  }

  @Override public void identity() {
    this.xCurrent = 0;
    this.yCurrent = 0;
    this.rotationInDegrees = 0;
    this.scaleWith1AsOriginal = 1.0;
  } // end identity
  
  @Override public void paint(Graphics2D g2) {
    if (this.isShowing) {
      for (EZElement e : children) {
        e.paint(g2);
      }
    }
  } // end paint

  /**
   * EZGroups themselves do not have height, the elements they hold have such values.
   * If there are no children, will return 0.
   * If there are children, will return the difference between the top most point and bottom most point.
   * 
   * @return 0 if no children. Otherwise the positive difference between the top most and bottom most point of all the elements within this group.
   */
  @Override public int getHeight() {
    int topMost, bottomMost;
    if(children.size() == 0){ return 0; }
    
    topMost = children.get(0).getYCenter() - children.get(0).getHeight()/2;    
    bottomMost = children.get(0).getYCenter() + children.get(0).getHeight()/2;
    
    for(EZElement e : children) {
      if( e.getYCenter() - e.getHeight()/2 < topMost ) {
        topMost = e.getYCenter() - e.getHeight()/2;
      }
      if( e.getYCenter() + e.getHeight()/2 > bottomMost ) {
        bottomMost = e.getYCenter() + e.getHeight()/2;
      }
    }
    return Math.abs(bottomMost - topMost);
  }

  /**
   * EZGroups themselves do not have width, the elements they hold have such values.
   * If there are no children, will return 0.
   * If there are children, will return the difference between the left most point and right most point regardless if the child is showing or not.
   * 
   * @return 0 if no children. Otherwise the positive difference between the left most and right most point of all the elements within this group.
   */
  @Override public int getWidth() {
    int leftMost, rightMost;
    if(children.size() == 0){ return 0; }
    
    leftMost = children.get(0).getXCenter() - children.get(0).getWidth()/2;    
    rightMost = children.get(0).getXCenter() + children.get(0).getWidth()/2;
    
    for(EZElement e : children) {
      if( e.getXCenter() - e.getWidth()/2 < leftMost ) {
        leftMost = e.getXCenter() - e.getWidth()/2;
      }
      if( e.getXCenter() + e.getWidth()/2 > rightMost ) {
        rightMost = e.getXCenter() + e.getWidth()/2;
      }
    }
    return Math.abs(rightMost - leftMost);
  }

  @Override public int getXCenter() {
    return (int) xCurrent;
  }

  @Override public int getYCenter() {
    return (int) yCurrent;
  }

  /**
   * Groups cannot have color. This doesn't do anything.
   * @param c will be discarded.
   * */
  @Override public void setColor(Color c) { }

  /**
   * Groups cannot have color. This returns BLACK by default.
   * @return Color.BLACK always.
   * */
  @Override public Color getColor() {
    return Color.BLACK;
  }

  /**
   * Groups do not have a "filled" status. Always returns true by default.
   * Perhaps you were looking for isShowing()?
   * @return true always.
   *  */
  @Override public boolean isFilled() {
    return true;
  }

  /**
   * Groups do not have a "filled" status. This method does nothing. Perhaps you were looking for show() or hide()?
   * @param f will be discarded.
   * */
  @Override public void setFilled(boolean f) { }

  
  @Override public void translateTo(double x, double y) {
    xCurrent = x;
    yCurrent = y;
  }

  @Override public void translateBy(double x, double y) {
    xCurrent += x;
    yCurrent += y;
  }

  /**
   * The bounds of a group is determined by the rectangle needed to contain all children, regardless if the child is showing or not. The
   * rectangle will always be aligned with the axis. The returned shape will be with respect to the world space.
   * This is much different from the other EZElements where the getBounds methods will return the shape after all
   * transformations including parent groups have been applied.
   * 
   * @return the shape which is a bounding box containing all elements of this group.
   */
  @Override public Shape getBounds() {
    ArrayList<EZElement> allChildren = new ArrayList<EZElement>();

    EZ.recurseGroupAddingToArrayList(this, allChildren);

    int top, bottom, left, right, temp;
    top = bottom = left = right = 0;

    if (allChildren.size() > 0) {
      top = allChildren.get(0).getBounds().getBounds().y;
      bottom = top + allChildren.get(0).getBounds().getBounds().height;
      left = allChildren.get(0).getBounds().getBounds().x;
      right = left + allChildren.get(0).getBounds().getBounds().width;

      for (EZElement c : allChildren) {
        temp = c.getBounds().getBounds().y;
        if (temp < top) {
          top = temp;
        }
        temp = temp + c.getBounds().getBounds().height;
        if (temp > bottom) {
          bottom = temp;
        }
        temp = c.getBounds().getBounds().x;
        if (temp < left) {
          left = temp;
        }
        temp = temp + c.getBounds().getBounds().width;
        if (temp > right) {
          right = temp;
        }
      } // end for each child

    }// end if there was at least 1 child.

    Rectangle r = new Rectangle(left, top, right - left, bottom - top);

    return r;
  } // end get bounds.

  /**
   * Will search all children and their children(if a group) to see if the point is within the elements.
   * This is different from checking if the point is within the bound of a group because this doesn’t go by the containing
   * rectangle for all elements which may include spaces that are not covered by a child.
   * 
   * Assumes the given point is on world space.
   * 
   * @param x coordinate of the point.
   * @param y coordinate of the point.
   * @return true if the point is within an element of this group. Otherwise false.
   */
  @Override public boolean isPointInElement(int x, int y) {
    for (EZElement child : children) {
      if (child.isPointInElement(x, y)) {
        return true;
      }
    }
    return false;
  } // end is point in element

  /**
   *  Will apply a cascading effect of show() calls on all elements in this group.
   */
  @Override public void show() {
    for (EZElement e : children) {
      e.show();
    }
  }

  /**
   * Will apply a cascading effect of hide() calls on all elements part of this node.
   * */
  @Override public void hide() {
    for (EZElement e : children) {
      e.hide();
    }
  }

  /**
   * Adds an element to the group. Since the element would have had an arbitrary coordinate, that element's coordinate
   * values will be adjusted such that it will be with relation to the distance from this node's center. Visually
   * this will not move the element. When an element is added to the group, the group will attempt to retain the
   * element's current draw layer relations with the other elements. Once the element is within a group, the draw
   * layer manipulation methods will be restricted to the draw layers within the group.
   * 
   * @param e, the element to add.
   * @return true if it was able to add the element. Otherwise false, meaning the element was already part of a group.
   */
  public boolean addElement(EZElement e) {
    if (e.setParent(this)) {
      // Correctly position the element's center with relation to the node's center
      e.translateTo(e.getXCenter() - xCurrent, e.getYCenter() - yCurrent);
      int addindex = -1;
      
      for(int i = 0; i < children.size(); i++){
        if( EZ.app.elements.indexOf(e) < EZ.app.elements.indexOf( children.get(i) )   ){
          addindex = i;
          break;
        }
      }
      if(addindex > -1) { children.add(addindex, e); }
      else { children.add(e); }
      
      return true;
    }
    return false;
  } // end add element

  /**
   * Will attempt to remove the specified element.
   *  The element will have coordinates, scale and rotation adjusted such that visually it will not look like anything has changed.
   * 
   * When an element is removed from a group, it will go back to the draw layer it was at before being added to the group.
   * Any changes to the draw layer that the element received while in the group will be discarded when it is
   * removed from the group.
   * 
   * @param e, the element to attempt to remove.
   * @return true if successful, otherwise false.
   */
  public boolean removeElement(EZElement e) {
    if (children.contains(e)) {
      ArrayList<EZElement> ancestors = new ArrayList<EZElement>();
      EZElement temp;
      temp = e;
      while (temp.hasParent()) {
        ancestors.add(temp.getParent());
        temp = temp.getParent();
      }

      double fRotation, fScale, fcx, fcy;
      fRotation = 0;
      fScale = 1.0;
      for (EZElement anc : ancestors) {
        fRotation += anc.getRotation();
        fScale *= anc.getScale();
      }
      fcx = e.getBounds().getBounds().getCenterX();
      fcy = e.getBounds().getBounds().getCenterY();
      if(e instanceof EZGroup){
        AffineTransform at = EZElement.transformHelper(e);
        fcx = at.getTranslateX();
        fcy = at.getTranslateY();
      }
      
      e.removeParent();
      children.remove(e);
      e.translateTo(fcx, fcy);
      e.scaleTo(fScale);
      e.rotateTo(fRotation);
      return true;
    }
    System.out.println("Unable to remove specified Element of " +
      Thread.currentThread().getStackTrace()[2].getFileName() + ":" +
      Thread.currentThread().getStackTrace()[2].getLineNumber()
      );
    
    return false;
  } // end remove element

  /**
   * Returns an ArrayList of EZElements containing all children of this element.
   * Will not search out sub children. For example, if an EZGroup contains EZGroups with elements, those groups
   * will be part of the ArrayList, not the children of the those groups.
   * 
   * Note: knowledge of polymorphism may be necessary to use this method.
   * 
   * @return an ArrayList of the children of this group.
   */
  public ArrayList<EZElement> getChildren() {
    return children;
  }

} // end EZGroup

