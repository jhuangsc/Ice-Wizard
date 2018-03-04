/// Tooltip
/// 
/// Purpose: Provide a way of displaying text at a specific position that does not
///   utilize Java's built-in objects (like JLabel and JTextArea)
/// 
/// Tooltips can be simplified as having a string to be drawn to the screen, and a
///   fixed maximum width. Text will form lines and wrap to this width, and the Tooltip
///   will also have a translucent black background whenever it is drawn.
///
/// Ability tooltips are shown whenever the player mouses over an ability icon in the game's
///   UI. The name, cost, cooldown and description of the ability is shown in the tooltip. 
/// Effect tooltips are similar, but only show the name and description (since cost/cooldown
///   are not applicable.)
/// Dialog tooltips appear in the centre of the screen at key points in the game, such as
///   starting a new level, dying, losing the game, clearing a level, or winning the game. 
///   They can also be used for story purposes, but this has not yet been implemented. Dialog
///   tooltips have their text aligned to the centre of the tooltip's box.
///
/// Tooltips have a Height which is calculated when the tooltip is created (or when the text
///   is modified). The Width is set permanently when the tooltip is created. The tooltip (as
///   well as its background box) is drawn using coordinates of the BOTTOM LEFT of the text
///   area - NOT the background box, which is always slightly larger for aesthetic purposes.
///
/// Height calculation and text drawing relies on the LineBreakMeasurer and TextLayout classes.
///   A workaround has been implemented to allow support for \n characters in the string, but
///   this does not always work when a string or even a line is too short - be careful.

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.text.*;
import java.awt.geom.AffineTransform;
import java.util.Hashtable;

class Tooltip {
	
	int type = 0;
	final static int TYPE_ABILITY = 1, TYPE_EFFECT = 2, TYPE_DIALOG = 3;
	boolean isVisible = true; /// Used for dialogs only
	
	Game main;
	Ability ownerA;
	Effect ownerE;
	
	int width = 0;
	int height = 0;
	int blx = 0, bly = 0;
	
	/// Text vars for Ability/Effect tooltips
	String name = "";
	String cost = "";
	String cd = "";
	String desc = "";
	double time = 0.0;
	
	/// General tooltips
	String out = "";
	AttributedString out2;
	
	/// Constructor for an Ability tooltip
	public Tooltip(Ability a) {
		main = a.main;
		ownerA = a;
		type = TYPE_ABILITY;
		width = 200;
		
		/// Get strings for name and description
		name = a.name;
		desc = a.description;
		
		/// Get string for HP cost/restored, if any
		if (a.hpcost > 0.0) {
			cost = cost + "HP Cost: "+a.hpcost;
		} else if (a.hpcost < 0.0) {
			cost = cost + "HP Restored: "+Math.abs(a.hpcost);
		}
		
		/// Get string for MP cost/restored, if any
		if (a.mpcost != 0.0) {
			if (cost.length() > 0) cost = cost + "\n";
			
			if (a.mpcost > 0.0) {
				cost = cost + "MP Cost: "+a.mpcost;
			} else if (a.mpcost < 0.0) {
				cost = cost + "MP Restored: "+Math.abs(a.mpcost);
			}
		}
		
		/// Get string for Charge spent/built, if any
		if (a.chargecost != 0.0) {
			if (cost.length() > 0) cost = cost + "\n";
			
			if (a.chargecost > 0.0) {
				cost = cost + "Charge Cost: "+a.chargecost;
			} else if (a.chargecost < 0.0) {
				cost = cost + "Charge Built: "+Math.abs(a.chargecost);
			}
		}
		
		/// Create line for Cooldown length if any
		if (a.cd > 0)
			cd = "Cooldown: " + 2*(Math.round(10.0*a.cd)/10000.0);
		
		initTooltipString(false);
	}
	
	/// Constructor for an Effect tooltip
	public Tooltip(Effect e) {
		main = e.main;
		ownerE = e;
		type = TYPE_EFFECT;
		width = 200;
		
		name = e.name;
		desc = e.description;
		
		initTooltipString(false);
	}
	
	/// Constructor for a Dialog
	public Tooltip(String t, Game p) {
		main = p;
		type = TYPE_DIALOG;
		width = 200;
		out = t;
		initTooltipString(true);
	}
	
	/// Creates the output string in its drawable form and calculates its height
	public void initTooltipString(boolean skipGetOut) {
		Hashtable attributes = new Hashtable();
		attributes.put(TextAttribute.FONT, main.font);
		
		if (!skipGetOut) out = getStringOut();
		out2 = new AttributedString(out, attributes);
		calculateHeight();
		
		if (type == TYPE_DIALOG) {
			blx = (int)(main.sx/2.0 - width/2.0);
			bly = (int)(main.sy/2.0 + height/2.0);
		}
	}
	
	public boolean isVisible() {
		if (type != TYPE_DIALOG) return true;
		return isVisible;
	}
	
	public void setVisible(boolean b) {
		isVisible = b;
	}
	
	public String getText() {
		return out;
	}
	
	public void setText(String s) {
		out = s;
		initTooltipString(true);
	}
	
	public void adjustWidth(int dw) {
		width += dw;
		blx -= dw/2;
	}
	
	public void adjustHeight(int dh) {
		height += dh;
		//bly -= dh/2;
	}
	
	/// Creates the tooltip string from its components
	/// Used only for Ability and Effect tooltips
	public String getStringOut() {
		String outS = "[" + name + "]" + "\n\n";
		
		/// Remake the tooltip's string
		if (type == TYPE_ABILITY) {
			/// Ability tooltip
			if (cost.length() != 0)
				outS = outS + cost + "\n\n";
			if (cd.length() != 0)
				outS = outS + cd + "\n\n";
			/*
			if (time != 0.0)
				outS = outS + "Cooldown Remaining: "+time+" s"+"\n\n";
			else
				outS = outS + "Ready to Use" + "\n\n";
			*/
		} else if (type == TYPE_EFFECT) {
			/// Effect tooltip
			/*
			if (time != 0.0)
				outS = outS + "Time Remaining: "+time+" s"+"\n\n";
			else
				outS = outS + "Expired" + "\n\n";
			*/
		} 
		outS = outS + desc;
		
		return outS;
	}
	
	/// Approximates the height of the string given the font and a fixed width
	/// Must be run after String out has been created
	/// Adapted from code provided by JavaGraphics.blogspot.com
	///   Link: http://javagraphics.blogspot.ca/2008/06/text-height-gui-layout-and-text-boxes.html
	public void calculateHeight() {
		Hashtable attributes = new Hashtable();
		attributes.put(TextAttribute.FONT, main.font);
		FontRenderContext frc = new FontRenderContext(new AffineTransform(),false,false);
		
		String[] lines = out.split("\n");
		int rows = 0;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].length() == 0) {
				rows++;
			
			} else {
				int len = lines[i].length();
				AttributedString ln = new AttributedString(lines[i], attributes);
				LineBreakMeasurer lbm = new LineBreakMeasurer(ln.getIterator(), frc);
				
				int pos = 0;
				while (pos < len) {
					pos = lbm.nextOffset(width);
					lbm.setPosition(pos);
					rows++;
				}
			}
		}
		height = (int)(rows * main.font.getLineMetrics("g",frc).getHeight() + 3);
	}
	
	/// Update the cooldown/duration of the main ability/effect
	public void update() {
		/// Scrapped until I find a more efficient way of implementing dynamic tooltips
		/*
		if (type == TYPE_ABILITY) {
			/// Ability tooltip
			time = 2*(Math.round(10.0*ownerA.cooldown)/10000.0);
			
		} else if (type == TYPE_EFFECT) {
			/// Effect tooltip
			time = 2*(Math.round(10.0*ownerE.duration)/10000.0);
		}
		*/
	}
	
	public void draw(Graphics g, int BLX, int BLY) {
		blx = BLX;
		bly = BLY;
	
		g.setColor(Constants.COLOR_ALPHA_BLACK);
		g.fillRect(blx - 10, bly - height - 10, width + 20, height + 20);
		g.setColor(Constants.COLOR_WHITE);
		
		AttributedCharacterIterator p = out2.getIterator();
		int pStart = p.getBeginIndex();
		int pEnd = p.getEndIndex();
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setFont(main.font);
		
		FontRenderContext frc = g2.getFontRenderContext();
		
		LineBreakMeasurer lnbm = new LineBreakMeasurer(p, frc);
		
		lnbm.setPosition(pStart);
		float y = (float)bly;
		
		while (lnbm.getPosition() < pEnd) {
		
			/// Start new line at each \n character
			int next = lnbm.nextOffset(width);
			int limit = next;
			if (limit < pEnd) {
				for (int i = lnbm.getPosition(); i < next; ++i) {
					char c = out.charAt(i);
					if (c == '\n') {
						limit = i+1;
						break;
					}
				}
			}
			
			/// Draw the line of text
			TextLayout layout = lnbm.nextLayout(width, limit, false);
			y += layout.getAscent();
			
			if (type == TYPE_DIALOG) {
				double dx = (width - layout.getAdvance())/2.0;
				layout.draw(g2, (float)(blx + (int)dx), y - height);
			} else {
				layout.draw(g2, (float)blx, y - height);
			}
			y += layout.getDescent() + layout.getLeading();
		}
	}
	
}