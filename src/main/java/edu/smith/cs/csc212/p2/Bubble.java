package edu.smith.cs.csc212.p2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class Bubble extends WorldObject {
	


    boolean bubbleExists;

	public Bubble(World world) {
		super(world);
		bubbleExists = true;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(new Color(1f,1f,1f,0.5f));
		g.fill(new Ellipse2D.Double(-0.6, -0.6, 1.2, 1.2));
		
	}

	@Override
	public void step() {
		

	}

}
