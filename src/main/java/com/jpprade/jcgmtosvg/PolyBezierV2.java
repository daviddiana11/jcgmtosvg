package com.jpprade.jcgmtosvg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.lang.reflect.Field;

import net.sf.jcgm.core.BeginApplicationStructure;
import net.sf.jcgm.core.BeginFigure;
import net.sf.jcgm.core.CGMDisplay;
import net.sf.jcgm.core.Command;
import net.sf.jcgm.core.EdgeColour;
import net.sf.jcgm.core.EdgeWidth;
import net.sf.jcgm.core.FillColour;
import net.sf.jcgm.core.LineColour;
import net.sf.jcgm.core.LineWidth;
import net.sf.jcgm.core.PolyBezier;

public class PolyBezierV2 {
	/**
	 * 1: discontinuous, 2: continuous, >2: reserved for registered values
	 */
	private int continuityIndicator = 0;

	/**
	 * The scales curves to draw
	 */
	private CubicCurve2D.Double[] curves;

	private Point2D.Double[] p1;
	private Point2D.Double[] p2;
	private Point2D.Double[] p3;
	private Point2D.Double[] p4;

	public PolyBezierV2(PolyBezier polyBezier) {

		Class  aClass = PolyBezier.class;
		
		
		try {
			Field field1 = aClass.getDeclaredField("p1");
		
			field1.setAccessible(true);
			Point2D.Double[] p1 = (Double[]) field1.get(polyBezier);
			
			Field field2 = aClass.getDeclaredField("p2");
			field2.setAccessible(true);
			Point2D.Double[] p2 = (Double[]) field2.get(polyBezier);
			
			Field field3 = aClass.getDeclaredField("p3");
			field3.setAccessible(true);
			Point2D.Double[] p3 = (Double[]) field3.get(polyBezier);
			
			Field field4 = aClass.getDeclaredField("p4");
			field4.setAccessible(true);
			Point2D.Double[] p4 = (Double[]) field4.get(polyBezier);
			
			
			Field fieldCI = aClass.getDeclaredField("continuityIndicator");
			fieldCI.setAccessible(true);
			int continuityIndicator = (int) fieldCI.get(polyBezier);
			
			this.p1=p1;
			this.p2=p2;
			this.p3=p3;
			this.p4=p4;
			this.continuityIndicator = continuityIndicator;
		
		
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	public void initCurves() {
		this.curves = new CubicCurve2D.Double[this.p1.length];

		for (int i = 0; i < this.p1.length; i++) {
			CubicCurve2D.Double c = new CubicCurve2D.Double();
			c.setCurve(this.p1[i].x, this.p1[i].y,
					this.p2[i].x, this.p2[i].y, this.p3[i].x, this.p3[i].y, this.p4[i].x, this.p4[i].y);
			this.curves[i] = c;
		}
	}

	
	public void paint(CGMDisplay d,BeginFigure figure, FillColour currentFC, EdgeColour currentEC, EdgeWidth currentEW, LineColour currentLC, LineWidth currentLW) {


		int mode = figure == null?0:1;
		if(mode==0) {
			if (this.curves == null)
				initCurves();

			Graphics2D g2d = d.getGraphics2D();
			g2d.setStroke(d.getLineStroke());
			g2d.setColor(d.getLineColor());
			//g2d.setStroke(d.getEdgeStroke());

			//d.fill(this.curves);

			for (int i = 0; i < this.curves.length; i++) {
				//d.fill(this.curves[i]);
				g2d.draw(this.curves[i]);

			}
		}else {



			Graphics2D g2d = d.getGraphics2D();
			if(currentLC!=null || currentLW!=null) {
			//if(lastCommand instanceof LineColour || lastCommand instanceof LineWidth) {
				g2d.setStroke(d.getLineStroke());			
				g2d.setColor(d.getLineColor());
			}else if(currentEC!=null || currentEW!=null) {
			//}else if(lastCommand instanceof EdgeColour || lastCommand instanceof EdgeWidth) {
				g2d.setStroke(d.getEdgeStroke());
				g2d.setColor(d.getEdgeColor());
			}else {
				g2d.setStroke(d.getLineStroke());			
				g2d.setColor(d.getLineColor());
			}
			
			

			GeneralPath gp = new GeneralPath();

			for (int i = 0; i < this.p1.length; i++) {
				if(i==0) {
					gp.moveTo(this.p1[i].x, this.p1[i].y);								
				}

				gp.curveTo(this.p2[i].x, this.p2[i].y, this.p3[i].x, this.p3[i].y, this.p4[i].x, this.p4[i].y);

				if(i==this.p1.length-1) {
					gp.closePath();	
				}
			}
			
			if (currentFC!=null) {			
				Color fColor = d.getFillColor();
				g2d.setPaint(fColor);
				
			}
			//
			if(currentFC!=null) {
				g2d.fill(gp);
			}else {
				g2d.draw(gp);
				//g2d.fill(gp);
			}
			
			/*
				
			}else {
				
			}*/
		}

	}
}
