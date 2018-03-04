/// 
/// 
/// 
/// 

import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import javax.imageio.*;

public class Level {

   int number = 0;
   double w = 800.0, h = 600.0, startx = 400.0, starty = 300.0;
   BufferedImage mapImage;
   ArrayList<String[]> doodads = new ArrayList<String[]>();
   ArrayList<String[]> mobs = new ArrayList<String[]>();
   ArrayList<String[]> pickups = new ArrayList<String[]>();
   
   public Level(File f) {
   
      // Get the level id
      this.number = Integer.parseInt(f.getName().substring(0, f.getName().indexOf(".")));
   
      // Read the level file
      Scanner sc; int eq;
      String line, tag, val;
      
      try {
         sc = new Scanner(f);
         while (sc.hasNext()) {
            line = sc.nextLine();
            eq = line.indexOf("=");
            if (eq == -1 || eq == line.length()-1) continue;
            tag = line.substring(0, eq);
            val = line.substring(eq+1);
            
            if (tag.equals("image")) {
               this.mapImage = GameImages.get(val);
            } else if (tag.equals("startx")) {
               this.startx = Double.parseDouble(val);
            } else if (tag.equals("starty")) {
               this.starty = Double.parseDouble(val);
            } else if (tag.equals("width")) {
               this.w = Double.parseDouble(val);
            } else if (tag.equals("height")) {
               this.h = Double.parseDouble(val);
            } else if (tag.equals("doodad")) {
               doodads.add(val.split(","));
            } else if (tag.equals("item")) {
               pickups.add(val.split(","));
            } else if (tag.equals("mob")) {
               mobs.add(val.split(","));
            }
         }
      } catch (IOException e) { }
   }
   
   // Start the level - spawn objects
   public void start(Game main) {
      String[] ar;
      
      // Spawn doodads
      for (int i = 0; i < doodads.size(); i++) {
         ar = doodads.get(i);
         main.spawnIWWallAt(ar[0], Double.parseDouble(ar[1]), Double.parseDouble(ar[2]));
      }
      // Spawn mobs
      for (int i = 0; i < mobs.size(); i++) {
         ar = mobs.get(i);
         main.spawnIWBattlerAt(ar[0], Double.parseDouble(ar[1]), Double.parseDouble(ar[2]));
      }
      // Spawn items
      for (int i = 0; i < pickups.size(); i++) {
         ar = pickups.get(i);
         main.spawnIWPickupAt(ar[0], Double.parseDouble(ar[1]), Double.parseDouble(ar[2]));
      }
      // Set the player's position
      main.player.setPosition(startx, starty);
      main.resetPlayer();
      main.camera.setFocus(startx, starty);
   }
   
   // Return the image of the currently visible area of the map
   public BufferedImage getMapRenderArea(Camera cam) {
      try {
         Rectangle region = cam.getCameraRegion();
         BufferedImage mapArea = this.mapImage.getSubimage(region.x, region.y, region.width, region.height);
         Graphics2D g = mapArea.createGraphics();
         g.drawImage(mapArea, null, 0, 0);
         return mapArea;
      } catch (RasterFormatException exc) {
         double[] cb = cam.getCameraBounds();
         System.out.println("Error: Couldn't get map region "+cb[0]+"-"+cb[1]+", "+cb[2]+"-"+cb[3]);
         exc.printStackTrace();
         return null;
      }
   }
}