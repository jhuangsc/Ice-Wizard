/// Camera
/// by Jonathan Huang
/// First created April 26, 2013
/// 
/// Object used to determine what area of a Map to render.
/// A Camera defines a "focus box" within the map area that will be rendered.
/// The Camera should not be able to see farther than the map's edge.
///   (except when the map is smaller than the camera region)
/// The Camera will move so that it is as centered as possible to the player character.
/// 
/// Usage
/// 
/// Attributes
///   int cx,cy - Position of the camera's centre on the Map in pixels.
///   int cw,ch - Size of the camera view in pixels.
/// 
/// Methods
/// 
/// 

import java.awt.*;

public class Camera {

   Level map;
   double cx, cy; // Current centre point on a map
   double w, h; // Upper bound on size, set at initialization
   double cw, ch; // Current size
   
   public Camera(Level m) {
      this(m,0,0,800,600);
   }
   
   public Camera(Level m, int CW, int CH) {
      this(m,0,0,CW,CH);
   }
   
   public Camera(Level m, int CX, int CY, int CW, int CH) {
      this.cx = CX;
      this.cy = CY;
      this.w = CW;
      this.h = CH;
      this.cw = CW;
      this.ch = CH;
      
      if (m != null) {
         this.setMap(m);
         this.setFocus(CX,CY);
      }
   }
   
   public void setMap(Level m) {
      this.map = m;
      if (this.cw > m.w) this.cw = m.w;
      else this.cw = this.w;
      if (this.ch > m.h) this.ch = m.h;
      else this.ch = this.h;
   }
   
   public void setFocus(double x, double y) {
      this.cx = x;
      this.cy = y;
      
      double[] bounds = getCameraBounds();
      
      // Adjust the camera in the x-axis
      int xdir = (bounds[0] < 0 && bounds[1] > map.w? 0 : bounds[0] < 0? -1 : bounds[1] > map.w? 1 : 0);
      if (xdir == -1) cx = cw/2;
      else if (xdir == 1) cx = map.w - cw/2;
      
      // Adjust the camera in the y-axis
      int ydir = (bounds[2] < 0 && bounds[3] > map.h? 0 : bounds[2] < 0? -1 : bounds[3] > map.h? 1 : 0);
      if (ydir == -1) cy = ch/2;
      else if (ydir == 1) cy = map.h - ch/2;
   }
   
   // Returns the camera's boundaries as [left_x, right_x, top_y, bottom_y]
   public double[] getCameraBounds() {
      double[] out = new double[4];
      out[0] = cx - cw/2;
      out[1] = cx + cw/2;
      out[2] = cy - ch/2;
      out[3] = cy + ch/2;
      return out;
   }
   
   public Rectangle getCameraRegion() {
      return new Rectangle((int)(this.cx - this.cw/2), (int)(this.cy - this.ch/2), (int)this.cw, (int)this.ch);
   }
}