/// VisualEffect
///
/// Purpose: Show something like an impact at certain points in the game:
///   for example, when getting hit, or using certain spells, or when certain
///   buffs/debuffs are active.
///
/// VisualEffects are generally linked to an Ability or Effect, and their duration
///   either depends on the length of the effect or the duration set by the Ability.
/// 
/// VisualEffects are either an AURA type (follows its owner) or BURST type (stays
///   wherever it was fired.
///
/// Planning to change VFX to images soon

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;

class VisualEffect {

	static final int TYPE_AURA = 0, TYPE_ZONEBURST = 1;

	Game main;
	IWObject owner;
	String id = "";
	Color color;
	double x = 0.0, y = 0.0, radius = 10.0;
	int type = 0, pattern = 0;
	int red = 0, green = 0, blue = 0, alpha = 0;
	int duration = 0, durationMax = 0;	/// in milliseconds
	boolean expired = false, fade = false;

	public VisualEffect(String ID, Game p, IWObject e, int durMS) {
		main = p;
		owner = e;
		id = ID;
		
		durationMax = durMS;
		duration = durationMax;
		
		initData();
		/// Get initial location of the vfx
		try {
			this.x = owner.x;
			this.y = owner.y;
		} catch (NullPointerException exc) { }
	}
	
	/// Read data from data\visualeffects\*.dat file
	/// Fields that use missing lines will instead use default values of 0, or in
	///   some cases, they will use values set by their owner
	public void initData() {
		BufferedReader file;
		try {
			file = new BufferedReader(new FileReader("data\\visualeffects\\"+id+".dat"));
			while (file.ready()) {
				String ln = file.readLine();
				if (ln.indexOf("=") < 0) throw new IOException();
				String tag = ln.substring(0,ln.indexOf("="));
				String val = ln.substring(ln.indexOf("=")+1);
				
				if (tag.equals("name")) {
					// do nothing for now
				} else if (tag.equals("duration")) {
					try {
						durationMax = Integer.parseInt(val);
						duration = durationMax;
					} catch (NumberFormatException e) {
						System.out.println("Error while creating vfx "+this.id+": Invalid input for field DURATION");
					}
					
				} else if (tag.equals("type")) {
					try {
						type = Integer.parseInt(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating vfx "+this.id+": Invalid input for field TYPE");
					}
					
				} else if (tag.equals("radius")) {
					try {
						radius = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating vfx "+this.id+": Invalid input for field RADIUS");
					}
					
				} else if (tag.equals("fade")) {
					try {
						fade = (val.equals("1")? true : false);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating vfx "+this.id+": Invalid input for field FADE");
					}
					
				} else if (tag.equals("pattern")) {
					try {
						pattern = Integer.parseInt(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating vfx "+this.id+": Invalid input for field PATTERN");
					}
					
				} else if (tag.equals("red")) {
					try {
						red = Integer.parseInt(val);
						if (red < 0) red = 0;
						if (red > 255) red = 255;
					} catch (NumberFormatException e) {
						System.out.println("Error while creating vfx "+this.id+": Invalid input for field RED");
					}
					
				} else if (tag.equals("green")) {
					try {
						green = Integer.parseInt(val);
						if (green < 0) green = 0;
						if (green > 255) green = 255;
					} catch (NumberFormatException e) {
						System.out.println("Error while creating vfx "+this.id+": Invalid input for field GREEN");
					}
					
				} else if (tag.equals("blue")) {
					try {
						blue = Integer.parseInt(val);
						if (blue < 0) blue = 0;
						if (blue > 255) blue = 255;
					} catch (NumberFormatException e) {
						System.out.println("Error while creating vfx "+this.id+": Invalid input for field BLUE");
					}
					
				} else if (tag.equals("alpha")) {
					try {
						alpha = Integer.parseInt(val);
						if (alpha < 0) alpha = 0;
						if (alpha > 255) alpha = 255;
					} catch (NumberFormatException e) {
						System.out.println("Error while creating vfx "+this.id+": Invalid input for field ALPHA");
					}
				} 
			}
			
		} catch (IOException e) {
			System.out.println("Error: data\\visualeffects\\"+id+".dat could not be found");
		}
		color = new Color(red,green,blue,alpha);
	}
	
	public void draw(Graphics g) {
		if (expired) return;
		
		g.setColor(color);
		
		/// Only update position if this is an AURA vfx ('attached' to a potentially moving entity)
		if (type == TYPE_AURA)
			try {
				this.x = owner.x;
				this.y = owner.y;
			} catch (NullPointerException e) { }
      
      double[] cb = main.camera.getCameraBounds();
      int tlx = (int)(this.x - cb[0] - this.radius);
      int tly = (int)(this.y - cb[2] - this.radius);
      int r2 = (int)(this.radius*2.0);
		
		switch(pattern) {
			case 0: /// plain circle
				g.drawOval(tlx, tly, r2, r2);
				break;
			case 1: /// plain square
				g.drawRect(tlx, tly, r2, r2);
				break;
			case 2: /// plain square, 3 px wide
				for (int i = -1; i < 2; i++)
					g.drawRect(tlx + i, (int)(tly + i), (int)(r2 + 2*i), (int)(r2 + 2*i));
				break;
			case 3: /// 'cage' with 3 bars
				for (int i = 0; i < 3; i++) 
					g.drawRect(tlx + (int)(0.66*i*this.radius), tly, (int)(0.66*this.radius), (int)(2.0*this.radius));
				break;
			case 4: /// plain filled circle
				g.fillOval(tlx, tly, r2, r2);
				break;
		}
	}
	
	/// Updates lifespan of the VFX object
	/// Also updates transparency, if applicable
	/// Should be called on the tdelay timer of Game
	public void update() {
		if (expired) return;
		
		if (durationMax > 0) {
			duration -= main.timerdelay;
			if (duration <= 0) { expired = true; return; }
			
			if (fade && getPercentExpended() > 50.0)
				color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha*( (150.0 - getPercentExpended())/100.0) ) );
		}
	}
	
	/// Resets the lifespan of the VFX object
	/// Also resets the transparency, if applicable
	public void refresh() {
		duration = durationMax;
		if (fade)
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
	
	public boolean isExpired() {
		return expired;
	}
	
	/// Returns the percentage of the VFX's lifespan that has passed
	public double getPercentExpended() {
		if (durationMax == 0) return 0.0;
		return 100.0 - Math.round(10000*(1.0*duration/durationMax))/100.0;
	}
}