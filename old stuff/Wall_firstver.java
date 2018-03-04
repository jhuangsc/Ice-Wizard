/**
	WALL entity - for Ice Wizard game
		by Jonathan Huang - Apr 2012
*/
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import javax.imageio.*;

class Wall {
	
	final static int WALL_OBSTACLE = 0, WALL_DESTRUCTIBLE = 1;
	
	Game parent;
	int type;
	double xLeft, xRight, yTop, yBottom;
	double width, height, cx, cy;
	double hp, hpmax;
	boolean invulnerable, dead = false;
	
	public Wall(double xT, double yT, double w, double h, int TYPE, Game p) {
		this.type = TYPE;
		this.parent = p;
		
		this.xLeft = xT;
		this.yTop = yT;
		this.xRight = xT + w;
		this.yBottom = yT + h;
		this.width = w;
		this.height = h;
		this.cx = this.xLeft + this.width/2.0;
		this.cy = this.yTop + this.height/2.0;
		
		switch(type) {
			case WALL_OBSTACLE:		this.hp = 1000; this.hpmax = 1000; this.invulnerable = true; break;
			case WALL_DESTRUCTIBLE:	this.hp = 1000; this.hpmax = 1000; this.invulnerable = false; break;
		}
	}
	
	public boolean isDestroyed() {
		return dead;
	}
	
	public void destroyed() {
		dead = true;
	}
	
	public void receiveDamage(double dmg) {
		if (this.invulnerable) return;
	
		if (this.hp > dmg) hp -= dmg;
		else hp = 0.0;
		
		if (hp <= 0.0) this.destroyed();
	}
	
	public double getHPPercent() {
		return Math.round(10000*(this.hp/this.hpmax)/100.0);
	}
	
	public void draw(Graphics g) {
		switch(type) {
			case WALL_OBSTACLE: 	g.setColor(new Color(140,140,255)); break;
			case WALL_DESTRUCTIBLE: g.setColor(new Color(140,140,140)); break;
		}
		g.fillRect((int)(xLeft), (int)(yTop), (int)(width), (int)(height));
	}
	
	public void drawHPBar(Graphics g) {
		if (getHPPercent() < 100.0 && this.type != WALL_OBSTACLE) {
			int xTL = (int)(this.cx - 50);
			int yTL = (int)(this.cy - this.height/2.0 - 15);
			g.setColor(new Color(0,0,0,180));
			g.fillRect(xTL, yTL, 100, 4);
			g.setColor(new Color(80,127,80,180));
			g.fillRect(xTL, yTL, (int)getHPPercent(), 4);
		}
	}
}