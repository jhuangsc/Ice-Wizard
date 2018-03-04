/**
	ENTITY - for Ice Wizard game
		by Jonathan Huang - Apr 2012
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

class Entity {

	static int OBJ_PLAYER = 0, OBJ_PROJECTILE = 1, OBJ_NPC = 2, OBJ_OBJ = 3, OBJ_WALL = 4;
	static int PLAYER = 10;
	static int PROJECTILE_SNOWFLAKE = 100, PROJECTILE_ICESHARD = 101, PROJECTILE_FIREBALL = 102;
	static int ENEMY_FIREBALL = 200;
	
	static int FRICTION_UP = 1, FRICTION_LEFT = 2, FRICTION_DOWN = 3, FRICTION_RIGHT = 4;
	
	Game parent;
	ArrayList<Vector> forces;

	int hp, type, id;
	double radius, mass, baseSpeed, angleFacing; // radius in pixels, mass in kg
	double x, y, dx, dy;
	double xspeed, xaccel, yspeed, yaccel; // Should all be positive values
	boolean garbage = false, ignoresFriction = false;
	boolean movingUp = false, movingDown = false, movingLeft = false, movingRight = false;
	boolean movingX = false, movingY = false;
	boolean firing = false;
	Timer timer;
	
	public Entity(double X, double Y, int TYPE, int ID, Game PARENT) {
		this.parent = PARENT;
		this.x = X;
		this.y = Y;
		
		forces = new ArrayList();
		this.baseSpeed = 100.0;
		this.angleFacing = 0.0;
		this.xspeed = 0.0;
		this.yspeed = 0.0;
		this.xaccel = 0.0;
		this.yaccel = 0.0;
		
		this.type = TYPE;
		this.id = ID;
		
		if (type == OBJ_PLAYER) {
			this.hp = 100;
			this.mass = 20.0;
			this.radius = 16.0;
		} else if (type == OBJ_PROJECTILE) {
			this.hp = 10;
			this.mass = 0.5;
			this.radius = 3.0;
			this.ignoresFriction = true;
		} else if (type == OBJ_NPC) {
			this.hp = 50;
			this.mass = 10.0;
			this.radius = 8.0;
		} else if (type == OBJ_OBJ) {
			this.hp = 1000;
			this.mass = 10.0;
			this.radius = 8.0;
		} else if (type == OBJ_WALL) {
			this.hp = 1000000000;
			this.mass = 1000000000.0;
			this.radius = 8.0;
		}
	}
	
	//###################################################
	//## SPEED METHODS ##################################
	//###################################################
	
		/// Modify position based on current net speed
		public void move() {
			if (this.type > OBJ_OBJ) return;
			
			
			if (!movingUp && !movingDown) slowDownY();
			if (!movingLeft && !movingRight) slowDownX();
			
			if (movingLeft && movingRight) {
				movingLeft = false;
				movingRight = false;
			}
			if (movingUp && movingDown) {
				movingUp = false;
				movingDown = false;
			}
			
			accelerate();
			modSpeedX();
			modSpeedY();
			
			if (movingUp) {
				y -= yspeed/Game.fps;
			} else if (movingDown) {
				y += yspeed/Game.fps;
			}
			
			if (movingLeft) {
				x -= xspeed/Game.fps;
			} else if (movingRight) {
				x += xspeed/Game.fps;
			}
		}
		
		public void moveTowards(Entity obj) {
			if (this.type > OBJ_OBJ) return;
			
			if (this.x > obj.x) {
				//moveLeft();
			} else if (this.x < obj.x) {
				//moveRight();
			}
			
			if (this.y > obj.y) {
				//moveUp();
			} else if (this.y < obj.x) {
				//moveDown();
			}
		}
			
		public void applyBaseXSpeed(boolean remove) {
			if (!remove) {
				if (!movingX) {
					movingX = true;
					xspeed += baseSpeed;
				}
			} else {
				if (!movingX) {
					movingX = false;
					xspeed -= baseSpeed;
				}
			}
		}
		
		public void applyBaseYSpeed(boolean remove) {
			if (!remove) {
				if (!movingY) {
					movingY = true;
					yspeed += baseSpeed;
				}
			} else {
				if (!movingY) {
					movingY = false;
					yspeed -= baseSpeed;
				}
			}
		}
	
	//###################################################
	//## PLAYER MOVEMENT METHODS ########################
	//###################################################
	
		public void moveUp() {
			this.movingUp = true;
			applyBaseYSpeed(false);
		}
		
		public void moveDown() {
			this.movingDown = true;
			applyBaseYSpeed(false);
		}
		
		public void moveLeft() {
			this.movingLeft = true;
			applyBaseXSpeed(false);
		}
		
		public void moveRight() {
			this.movingRight = true;
			applyBaseXSpeed(false);
		}
		
		public void stopUp() {
			this.movingUp = false;
			applyBaseYSpeed(true);
		}
		
		public void stopLeft() {
			this.movingLeft = false;
			applyBaseXSpeed(true);
		}
		
		public void stopDown() {
			this.movingDown = false;
			applyBaseYSpeed(true);
		}
		
		public void stopRight() {
			this.movingRight = false;
			applyBaseXSpeed(true);
		}
		
		public void stop() {
			if (movingX) {
				movingX = false;
				xspeed -= baseSpeed;
			}
			if (movingY) {
				movingY = false;
				yspeed -= baseSpeed;
			}
		
			this.movingUp = false;
			this.movingDown = false;
			this.movingLeft = false;
			this.movingRight = false;
		}
		
		public void stopX() {
			if (movingX) {
				movingX = false;
				xspeed -= baseSpeed;
			}
			this.movingLeft = false;
			this.movingRight = false;
		}
		
		public void stopY() {
			if (movingY) {
				movingY = false;
				yspeed -= baseSpeed;
			}
			this.movingUp = false;
			this.movingDown = false;
		}
		
	//###################################################
	//## SPEED METHODS ##################################
	//###################################################
		
		/// No parameter - takes acceleration instead
		/// Should be called every 'tick' of the timer
		public void modSpeed() {
			if (this.type > OBJ_OBJ) return;
			
			xspeed += xaccel/Game.fps;
			yspeed += yaccel/Game.fps;
		}
		
		public void modSpeed(double ds) {
			if (this.type > OBJ_OBJ) return;
			
			this.xspeed += ds;
			this.yspeed += ds;
		}
		
		public void modSpeedX() {
			if (this.type > OBJ_OBJ) return;
			
			this.xspeed += xaccel/Game.fps;
		}
		
		public void modSpeedX(double dsx) {
			if (this.type > OBJ_OBJ) return;
			
			this.xspeed += dsx;
		}
		
		public void modSpeedY() {
			if (this.type > OBJ_OBJ) return;
			
			this.yspeed += yaccel/Game.fps;
		}
		
		public void modSpeedY(double dsy) {
			if (this.type > OBJ_OBJ) return;
			
			this.yspeed += dsy;
		}
		
		public void slowDown() {
			if (xspeed < 0.0) 
				xspeed = 0.0;
			else
				modSpeedX(-1.0*xaccel/Game.fps);
			
			if (yspeed < 0.0)
				yspeed = 0.0;
			else
				modSpeedY(-1*yaccel/Game.fps);
		}
		
		public void slowDownX() {
			if (xspeed < 0.0) 
				xspeed = 0.0;
			else
				modSpeedX(-1.0*xaccel/Game.fps);
		}
		
		public void slowDownY() {
			if (yspeed < 0.0)
				yspeed = 0.0;
			else
				modSpeedY(-1.0*yaccel/Game.fps);
		}
		
	//###################################################
	//## ANGLE METHODS ##################################
	//###################################################
	
		/// Takes angle in radians
		public void rotate(double theta) {
			this.angleFacing += theta;
		}
		
		/// Takes angle in radians
		public void setDirection(double theta) {
			this.angleFacing = theta;
		}
		
		/// Sets base speed of a projectile to get it moving immediately
		public void initProjectile(double mx, double my) {
			
			double speed = 0.0;
			if (id == PROJECTILE_SNOWFLAKE) {
				speed = 300.0;
			} else if (id == PROJECTILE_ICESHARD) {
				speed = 150.0;
			} else if (id == PROJECTILE_FIREBALL) {
				speed = 100.0;
			}
			
			double dx = mx - x;
			double dy = my - y;
			
			this.xspeed = Math.abs(speed * Math.cos(angleFacing));
			this.yspeed = Math.abs(speed * Math.sin(angleFacing));
			//this.x = x + 200*Math.cos(angleFacing);
			//this.y = y + 200*Math.sin(angleFacing);
			
			if (x > mx) movingLeft = true;
			if (x < mx) movingRight = true;
			if (y > my) movingUp = true;
			if (y < my) movingDown = true;
		}
	
	//###################################################
	//## PHYSICS METHODS ################################
	//###################################################
	
		/// Applies force in Newtons in a specific direction
			public void applyForce(double F, double theta) {
				applyForce(F,theta,-1,Vector.DEFAULT);
			}
			
			public void applyForce(double F, double theta, int flag) {
				applyForce(F,theta,-1,flag);
			}
			
			public void applyForce(double F, double theta, int duration, int flag) {
				this.forces.add(new Vector(F,theta,duration,flag));
			}
		
		/// Set acceleration based on forces acting on object
			public void accelerate() {
				if (this.type > OBJ_OBJ) return;
				
				xaccel = 0.0;
				yaccel = 0.0;
				
				for (int i = 0; i < forces.size(); i++) {
					xaccel += forces.get(i).getX();
					yaccel += forces.get(i).getY();
				}
				
				if (xaccel < 0.0) {
					xaccel = Math.abs(xaccel);
					movingLeft = true;
					movingRight = false;
				}
				if (yaccel < 0.0) {
					yaccel = Math.abs(yaccel);
					movingUp = true;
					movingDown = false;
				}
			}
		
		/// Check for collision based on distance between centre point and radii lengths
			public boolean getCollision(Entity obj) {
				// Don't bother checking collision between immobile walls
				if (this.type == OBJ_WALL && obj.type == OBJ_WALL) return false;
			
				// Use Rect collision if one obj is a wall, else use Radius collision
				if (this.type == OBJ_WALL || obj.type == OBJ_WALL) {
				
					return false;
					
				} else {
					double distance = Math.sqrt( Math.pow((this.x - obj.x),2) + Math.pow((this.y - obj.y),2) );
					if (distance - this.radius - obj.radius <= 0.0) return true;
					return false;
				}
			}
		/*
		/// Calculate angle that this object will move at after colliding
		/// Only called if it does collide
			public double getCollisionAngle(Entity obj) {
				if (obj.type == OBJ_WALL) {
				
				}
			}
		*/
	
	//###################################################
	//## DRAWING METHODS ################################
	//###################################################
	
	public void draw(Graphics g) {
		if (id == PLAYER) {
			g.fillOval((int)x, (int)y, 4, 4);
			drawUberflake(g, this.x, this.y, this.radius);
		} else if (id == PROJECTILE_SNOWFLAKE) {
			drawSnowflake(g, this.x, this.y, this.radius);
		} else if (id == PROJECTILE_ICESHARD) {
			drawIceShard(g, this.x, this.y);
		} else if (id == PROJECTILE_FIREBALL) {
		
		} else if (id == ENEMY_FIREBALL) {
		
		} 
	}
	
	public void drawUberflake(Graphics g, double X, double Y, double len1) {
		if (len1 <= 2) return;
		g.setColor(new Color(255,255,255));
		drawSnowflake(g,X,Y,len1);
		drawUberflake(g,X,Y,len1/3.0);
	}

	public void drawSnowflake(Graphics g, double X, double Y, double len) {
		if (len < 3) return;
		
		g.setColor(new Color(255,255,255));
		double r = 0.0 + this.angleFacing;;
			
		for (double theta = r; theta < r + 2.0*Math.PI; theta += 2.0*Math.PI/6.0) {
			double x2 = X + (len * Math.cos(theta));
			double y2 = Y + (len * Math.sin(theta));
			g.drawLine((int)X, (int)Y, (int)x2, (int)y2);
			drawSnowflake(g, x2, y2, len/3.0);
		}
	}
	
	public void drawIceShard(Graphics g, double X, double Y) {
		double x1, y1, x2, y2, x3, y3;
		x1 = X + radius*Math.cos(angleFacing);
		y1 = Y + radius*Math.sin(angleFacing);
		x2 = X + radius*Math.cos(angleFacing+(2.0*Math.PI/3.0));
		y2 = Y + radius*Math.sin(angleFacing+(2.0*Math.PI/3.0));
		x3 = X + radius*Math.cos(angleFacing+(4.0*Math.PI/3.0));
		y3 = Y + radius*Math.sin(angleFacing+(4.0*Math.PI/3.0));
		g.setColor(new Color(240,240,255));
		g.drawLine((int)x1,(int)y1,(int)x2,(int)y2);
		g.drawLine((int)x2,(int)y2,(int)x3,(int)y3);
		g.drawLine((int)x3,(int)y3,(int)x1,(int)y1);
	}
}