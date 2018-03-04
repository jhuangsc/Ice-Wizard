/// IWWall
///   Subclass of IWObject
///
/// Main differences: 
///   Has a width and height (has radius too, but this is not used)
///   Collision detection with walls is calculated based on the wall's centre and w,h
///   IWWalls have no MP or charge
///   IWWalls can move, though this is not currently used due to a lack of Waypoint implementation
///   IWWalls can receive but cannot take damage
///   IWWalls have no AI or effects

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*; 
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.io.*;

class IWWall extends IWObject {

	double width = 0.0, height = 0.0;
	
	public IWWall(IWWall orig) {
		super(orig);
		this.width = orig.width;
		this.height = orig.height;
	}
	
	public IWWall(String ID, double X, double Y, Game PARENT) {
		super(ID,X,Y,PARENT,null);
		initStats();
	}
	
	public void initIWWall(double X, double Y) {
		this.setPosition(X,Y);
	}
	
	public void initStats() {
		BufferedReader file;
      String ln, tag, val; int eq;
		try {
			file = new BufferedReader(new FileReader("data\\doodads\\"+id+".dat"));
			while (file.ready()) {
				ln = file.readLine();
            eq = ln.indexOf("=");
				if (eq == -1 || eq == ln.length()-1) continue;
				tag = ln.substring(0,ln.indexOf("="));
				val = ln.substring(ln.indexOf("=")+1);
				
				if (tag.equals("name")) {
					name = val;
				} else if (tag.equals("image")) {
					imgID = val;
					img = GameImages.get(val);
               if (this.img == null) System.out.println("Couldn't get image!");
				} else if (tag.equals("faction")) {
					try {
						faction = new Faction(Integer.parseInt(val));
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field FACTION");
					} catch (NullPointerException e) {
						System.out.println("Error while creating entity "+id+": Invalid or projectile faction and has no owner");
					}
				} else if (tag.equals("health")) {
					try {
						hpmax = Double.parseDouble(val);
						hp = hpmax;
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field HP");
					}
				} else if (tag.equals("shield")) {
					try {
						shieldmax = Double.parseDouble(val);
						shield = shieldmax;
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field SHIELD");
					}
						
				} else if (tag.equals("shieldregen")) {
					try {
						shieldregen = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field SHIELDREGEN");
					}
				} else if (tag.equals("basespeed")) {
					try {
						baseSpeed = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field BASESPEED");
					}
						
				} else if (tag.equals("width")) {
					try {
						width = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field WIDTH");
					}
						
				} else if (tag.equals("height")) {
					try {
						height = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field HEIGHT");
					}
						
				} else if (tag.equals("mass")) {
					try {
						mass = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field MASS");
					}
						
				} else if (tag.equals("flag")) {
					if (val.equals("1")) invulnerable = true;
				} 
				
			} file.close();
			
		} catch (IOException e) {
			System.out.println("Error: data\\walls\\"+id+".dat could not be found");
		}
	}
	
	public boolean isIWBattler() { return false; }
	public boolean isIWProjectile() { return false; }
	public boolean isIWWall() { return true; }
	public boolean isIWPickup() { return false; }
	
	//###################################################
	//## META ###########################################
	//###################################################
	
		public void update() {
			regenerateShield();
		}
		
		/// Get X coordinate of left boundary
		public double getLeftBound(boolean includeTolerance) {
			return x - width/2.0 - (includeTolerance? main.collisionTolerance : 0);
		}
		/// Get X coordinate of right boundary
		public double getRightBound(boolean includeTolerance) {
			return x + width/2.0 + (includeTolerance? main.collisionTolerance : 0);
		}
		/// Get Y coordinate of top boundary
		public double getTopBound(boolean includeTolerance) {
			return y - height/2.0 - (includeTolerance? main.collisionTolerance : 0);
		}
		/// Get Y coordinate of bottom boundary
		public double getBottomBound(boolean includeTolerance) {
			return y + height/2.0 + (includeTolerance? main.collisionTolerance : 0);
		}
	
	//###################################################
	//## SPEED/Constants.VECTOR_VELOCITY METHODS #########################
	//###################################################
   
		public void move() { }
      public void stop() { }
		
		public void moveTowards(double nx, double ny) {
			for (int i = 0; i < velocities.size(); i++)
				if (velocities.get(i).matchID(Constants.VECTOR_OBJMOVEMENT)) velocities.remove(i);
					
			// get angle to target location
			double ntheta = Math.atan2( (ny - y), (nx - x) );
			
			velocities.add(new Vector(Constants.VECTOR_OBJMOVEMENT,baseSpeed,ntheta,-1,main));
		} 
		
		public void moveTowards(double ntheta) {
			for (int i = 0; i < velocities.size(); i++)
				if (velocities.get(i).matchID(Constants.VECTOR_OBJMOVEMENT)) velocities.remove(i);
			
			velocities.add(new Vector(Constants.VECTOR_OBJMOVEMENT,baseSpeed,ntheta,-1,main));
		} 
		
		
		
		public double getNetXVelocity() {
			double s = 0.0; 
			Vector v;
			for (int i = 0; i < velocities.size(); i++) {
				v = velocities.get(i);
				s += v.magnitude*Math.cos(v.direction)*(v.id.equals(Constants.VECTOR_OBJMOVEMENT)? baseSpeedMult : 1.0);
			}
			return s;
		}
		
		public double getNetYVelocity() {
			double s = 0.0; 
			Vector v;
			for (int i = 0; i < velocities.size(); i++) {
				v = velocities.get(i);
				s += v.magnitude*Math.sin(v.direction)*(v.id.equals(Constants.VECTOR_OBJMOVEMENT)? baseSpeedMult : 1.0);
			}
			return s;
		}
	
	//###################################################
	//## GAMEPLAY METHODS ###############################
	//###################################################
	
	public void touched(IWBattler e) { }
	public void touched(IWProjectile e) { }
	public void touched(IWPickup e) { }
	
	public void dead() {
		garbage = true;
	}
	
	public void dealDamageTo(IWBattler e) { }
	
	public void dealDamageTo(IWWall w) { }
	
	public void receiveDamage(double dmg, IWObject source) {
		if (dead || invulnerable) return;
		if (source.faction.friendlyTo(this.faction)) return;
		
		shield -= dmg;
		if (shield < 0.0) {
			hp += shield;
			shield = 0.0;
		}
		
		if (hp <= 0.0) hp = 0.0;
		else if (hp > hpmax) hp = hpmax;
		
		if (hp <= 0.0) dead();
	}
	
	public void spawnIWProjectile(String id, double angleFacing) { }
	
	//###################################################
	//## DRAWING METHODS ################################
	//###################################################
	
	public void draw(Graphics g) {
		try {
         double[] cb = main.camera.getCameraBounds();
         double nx = this.x - cb[0] - this.img.getWidth()/2.0;
         double ny = this.y - cb[2] - this.img.getHeight()/2.0;
         g.drawImage(img, (int)nx, (int)ny, null);
		} catch (NullPointerException exc) {
			System.out.println("Error: IWWall "+id+" at ("+(int)x+","+(int)y+") is missing its image "+imgID);
		}
	}
	
	/// generic drawBars method for drawing bars over enemies, other objects and walls
	public void drawBars(Graphics g) {
      double[] cb = this.main.camera.getCameraBounds();
      double nx = this.x - cb[0];
      double ny = this.y - cb[2] - 15;
      int len = 50;
      //int len = (int)(width < 60? 50 : width < 200? 100 : 200);
		drawBar(g, BAR_SHIELD, nx - len/2.0, ny - 10, len, false);
		drawBar(g, BAR_HP, nx - len/2.0, ny - 5, len, false);
	}
	
	/// more specific drawBar method - use this instead of drawBar(Graphics,int) for player UI
	public void drawBar(Graphics g, int type, double TLX, double TLY, int len, boolean ignoreThisArg) {
		int h = 5;
		switch(type) {
			case BAR_HP: 
				if (hpmax == 0.0 || getHPPercent() == 100.0) return; 
				else {
					g.setColor(Constants.COLOR_RED); 
					g.fillRect((int)TLX, (int)TLY, len, h);
					g.setColor(Constants.COLOR_GREEN);
					g.fillRect((int)TLX, (int)TLY, (int)(len*getHPPercent()/100.0), h);
					drawBarSquares(g,TLX,TLY,len,h);
					break;
				}
			case BAR_SHIELD: 
				if (shieldmax == 0.0 || getShieldPercent() == 100.0) return; 
				else {
					g.setColor(Constants.COLOR_CYAN); 
					g.fillRect((int)TLX, (int)TLY, (int)(len*getShieldPercent()/100.0), h);
					drawBarSquares(g,TLX,TLY,len,h);
					break;
				}
		}
	}
	
	public void drawBarSquares(Graphics g, double TLX, double TLY, int w, int h) {
		g.setColor(new Color(0,0,0,180));
		for (int i = 0; i < w/10; i++)
			g.drawRect((int)TLX+10*i, (int)TLY, 10, h);
	}
	
}