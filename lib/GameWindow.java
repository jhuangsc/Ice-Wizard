
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*; 
import java.awt.image.*;
import java.util.ArrayList;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

class GameWindow extends JPanel {

	Game main;
	VisualEffect lololol;
	BufferedImage img = GameImages.get("LIFE");
   double gdx = 0.0, gdy = 0.0;

	public GameWindow(Game g) {
		super();
		main = g;
		setLayout(null);
		setBackground(Constants.COLOR_BLACK);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		/// if the game has started...
		if (main.dialogState != Constants.GAMESTATE_MAINMENU) {
         
         /// draw map image
         g.drawImage(GameData.getLevel(main.level).getMapRenderArea(main.camera), 0, 0, null);
			
			/// draw walls
			for (int i = 0; i < main.walls.size(); i++) 
				main.walls.get(i).draw(g);
			
			/// draw items
			for (int i = 0; i < main.items.size(); i++)
				main.items.get(i).draw(g);
				
			/// draw projectiles
			for (int i = 0; i < main.projectiles.size(); i++)
				main.projectiles.get(i).draw(g);
				
			/// draw mobs
			for (int i = 0; i < main.mobs.size(); i++) 
				main.mobs.get(i).draw(g);
				
			/// draw VFX
			for (int i = 0; i < main.mobs.size(); i++) {
				for (int e = 0; e < main.mobs.get(i).effects.size(); e++) 
					main.mobs.get(i).effects.get(e).draw(g);
				for (int a = 0; a < main.mobs.get(i).ai.abilities.size(); a++)
					main.mobs.get(i).ai.abilities.get(a).draw(g);
			}
			for (int i = 0; i < main.player.effects.size(); i++)
				main.player.effects.get(i).draw(g);
			for (int i = 0; i < main.player.ai.abilities.size(); i++)
				main.player.ai.abilities.get(i).draw(g);
				
				
			/// draw HP/shield/MP bars
			for (int i = 0; i < main.mobs.size(); i++)
				main.mobs.get(i).drawBars(g);
			for (int i = 0; i < main.walls.size(); i++)
				main.walls.get(i).drawBars(g);
			
			/// draw player on top of everything except UI
			main.player.draw(g);
			
			/// draw UI
			g.setColor(Constants.COLOR_ALPHA_BLUE);
			g.fillRect(0, main.sy - 55, main.sx, main.sy);
			main.player.drawBar(g, IWObject.BAR_SHIELD, 5, main.sy - 49, 100, true);
			main.player.drawBar(g, IWObject.BAR_HP, 5, main.sy - 38, 100, true);
			main.player.drawBar(g, IWObject.BAR_MP, 5, main.sy - 27, 100, true);
			main.player.drawBar(g, IWObject.BAR_CHARGE, 5, main.sy - 16, 100, true);
			g.setColor(Constants.COLOR_WHITE);
			//g.setFont(main.font);
			g.drawString((int)main.player.shield+"/"+(int)main.player.shieldmax, 110, main.sy - 39);
			g.drawString((int)main.player.hp+"/"+(int)main.player.hpmax, 110, main.sy - 28);
			g.drawString((int)main.player.mp+"/"+(int)main.player.mpmax, 110, main.sy - 17);
			g.drawString((int)main.player.charge+"/"+(int)main.player.chargemax, 110, main.sy - 6);
			drawLives(g, main.sx - 30, main.sy - 8);
			drawPlayerEffectIcons(g, 160, main.sy - 49);
			drawAbilityIcons(g, 160, main.sy - 25);
			
		} else {
			/// otherwise draw ... whatever else is there
		}
		/// if dialogs are visible, draw them
		for (int i = 0; i < main.dialogs.length; i++)
			if (main.dialogs[i] != null)
            if (main.dialogs[i].isVisible()) 
               main.dialogs[i].draw(g, main.dialogs[i].blx, main.dialogs[i].bly);
	}
	
	public void drawAbilityIcons(Graphics g, int TLX, int TLY) {
		for (int i = 0; i < main.player.ai.abilities.size(); i++) {
			main.player.ai.abilities.get(i).drawIcon(g, TLX + 24*i, TLY);
			if (getMouseIsInIconArea(TLX + 24*i,TLY,main.mx,main.my))
				main.player.ai.abilities.get(i).drawTooltip(g, TLX + 24*i + 10, TLY - 20);
			
		}
	}
	
	public void drawPlayerEffectIcons(Graphics g, int TLX, int TLY) {
		for (int i = 0; i < main.player.effects.size(); i++) {
			main.player.effects.get(i).drawIcon(g, TLX + 24*i, TLY);
			if (getMouseIsInIconArea(TLX + 24*i,TLY,main.mx,main.my))
				main.player.effects.get(i).drawTooltip(g, TLX + 24*i + 10, TLY - 20);
		}
	}
	
	public void drawLives(Graphics g, int BRX, int BRY) {
		for (int i = 0; i < main.lives; i++) {
			g.drawImage(img, BRX - img.getWidth()*(i%10) - 4*(i%10), BRY - img.getHeight() - (i/10)*20, null);
			if (i == 19) break;
		}
		if (main.lives > 20) {
			g.setColor(Constants.COLOR_WHITE);
			g.drawString("x"+main.lives, BRX - 48 - img.getWidth()*10, BRY - 3);
		}
	}
	
	public boolean getMouseIsInIconArea(int TLX, int TLY, double mx, double my) {
		if (TLX < mx && mx < TLX + 20 && TLY < my && my < TLY + 20) return true;
		return false;
	}
   
   public void updateRenderRegion(double ngdx, double ngdy) {
      this.gdx = ngdx;
      this.gdy = ngdy;
   }
}