package com.jpprade.jcgmtosvg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.lang.reflect.Field;

import com.jpprade.jcgmtosvg.extension.SVGGraphics2DHS;

import net.sf.jcgm.core.BeginApplicationStructure;
import net.sf.jcgm.core.BeginFigure;
import net.sf.jcgm.core.CGMDisplay;
import net.sf.jcgm.core.Command;
import net.sf.jcgm.core.EdgeColour;
import net.sf.jcgm.core.EdgeWidth;
import net.sf.jcgm.core.FillColour;
import net.sf.jcgm.core.HatchIndex.HatchType;
import net.sf.jcgm.core.InteriorStyle;
import net.sf.jcgm.core.InteriorStyle.Style;
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

	public void paint(CGMDisplay d,BeginFigure figure) {
		this.paint(d, figure, null, null, null, null, null);
	}
	
	public void paint(CGMDisplay d,BeginFigure figure, FillColour currentFC, EdgeColour currentEC, EdgeWidth currentEW, LineColour currentLC, LineWidth currentLW) {


		int mode = figure == null?0:1;
		if(mode==0) {
			if (this.curves == null)
				initCurves();

			//Graphics2D g2d = d.getGraphics2D();
			SVGGraphics2DHS g2d =  (SVGGraphics2DHS) d.getGraphics2D();
			g2d.setStroke(d.getLineStroke());
			g2d.setColor(d.getLineColor());
			//g2d.setStroke(d.getEdgeStroke());

			//d.fill(this.curves);

			for (int i = 0; i < this.curves.length; i++) {
					g2d.draw(this.curves[i]);
			}
		}else {



			//Graphics2D g2d = d.getGraphics2D();
			SVGGraphics2DHS g2d =  (SVGGraphics2DHS) d.getGraphics2D();
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
				
				Style s = d.getInteriorStyle();
				if(InteriorStyle.Style.HATCH.equals(s)) {
					//drawHatch(gp,g2d,currentFC.,null);
					//currentFC.
					drawHatch(gp,g2d,d.getFillColor(), d.getHatchType());
					g2d.draw(gp);
				}else {
					g2d.fill(gp);
				}
				
			}else {				
				g2d.draw(gp);	
			}
			
			
		}

	}
	
	
	private void drawHatch(Shape s,SVGGraphics2DHS g2d,Color fillColor,HatchType hatchType) {
		// remember the clip and the stroke since we're overwriting them here
		Shape previousClippingArea = g2d.getClip();
		Stroke previousStroke = g2d.getStroke();

		Rectangle2D bounds = s.getBounds2D();
		g2d.setClip(s);

		g2d.setStroke(new BasicStroke(0.2f));

		g2d.setColor(fillColor);

		final double stepX = 1.41;
		final double stepY = 1.41;
		final double slopeStep = stepX * 1.41;

		if (HatchType.HORIZONTAL_LINES.equals(hatchType)) {
			drawHorizontalLines(bounds, stepY,g2d);
		}
		else if (HatchType.VERTICAL_LINES.equals(hatchType)) {
			drawVerticalLines(bounds, stepX,g2d);
		}
		else if (HatchType.POSITIVE_SLOPE_LINES.equals(hatchType)) {
			drawPositiveSlopeLines(bounds, slopeStep,g2d);
		}
		else if (HatchType.NEGATIVE_SLOPE_LINES.equals(hatchType)) {
			drawNegativeSlopeLines(bounds, slopeStep,g2d);
		}
		else if (HatchType.HORIZONTAL_VERTICAL_CROSSHATCH.equals(hatchType)) {
			drawHorizontalLines(bounds, stepY,g2d);
			drawVerticalLines(bounds, stepX,g2d);
		}
		else if (HatchType.POSITIVE_NEGATIVE_CROSSHATCH.equals(hatchType)) {
			drawPositiveSlopeLines(bounds, slopeStep,g2d);
			drawNegativeSlopeLines(bounds, slopeStep,g2d);
		}

		// restore the previous clipping area and stroke
		g2d.setClip(previousClippingArea);
		g2d.setStroke(previousStroke);
	}
	
	
	
	
	
	private void drawVerticalLines(Rectangle2D bounds, final double stepX,SVGGraphics2DHS g2d) {
		for (double x = bounds.getX(); x < bounds.getX() + bounds.getWidth(); x += stepX) {
			g2d.draw(new Line2D.Double(x, bounds.getY(), x, bounds.getY() + bounds.getHeight()));
		}
	}

	private void drawHorizontalLines(Rectangle2D bounds, final double stepY,SVGGraphics2DHS g2d) {
		for (double y = bounds.getY(); y < bounds.getY() + bounds.getHeight(); y += stepY) {
			g2d.draw(new Line2D.Double(bounds.getX(), y, bounds.getX() + bounds.getWidth(), y));
		}
	}

	private void drawPositiveSlopeLines(Rectangle2D bounds, final double slopeStep,SVGGraphics2DHS g2d) {
		Point2D.Double currentBegin = new Point2D.Double(bounds.getX(), bounds.getY() + bounds.getHeight());
		Point2D.Double currentEnd = currentBegin;

		boolean done = false;
		while (!done) {
			// move begin
			if (currentBegin.y > bounds.getY()) {
				// move the begin down the Y axis
				currentBegin = new Point2D.Double(currentBegin.x, currentBegin.y - slopeStep);
			}
			else {
				// move the begin right the X axis
				currentBegin = new Point2D.Double(currentBegin.x + slopeStep, currentBegin.y);
			}

			// move end
			if (currentEnd.x < bounds.getX() + bounds.getWidth()) {
				// move end right the X axis
				currentEnd = new Point2D.Double(currentEnd.x + slopeStep, currentEnd.y);
			}
			else {
				// move end down the Y axis
				currentEnd = new Point2D.Double(currentEnd.x, currentEnd.y - slopeStep);
			}

			g2d.draw(new Line2D.Double(currentBegin.x, currentBegin.y, currentEnd.x, currentEnd.y));

			if (currentBegin.x > bounds.getX() + bounds.getWidth() || currentEnd.getY() < bounds.getY()) {
				done = true;
			}
		}
	}

	private void drawNegativeSlopeLines(Rectangle2D bounds, final double slopeStep,SVGGraphics2DHS g2d) {
		Point2D.Double currentBegin = new Point2D.Double(bounds.getX(), bounds.getY());
		Point2D.Double currentEnd = currentBegin;

		boolean done = false;
		while (!done) {
			// move begin
			if (currentBegin.y < bounds.getY() + bounds.getHeight()) {
				// move the begin up the Y axis
				currentBegin = new Point2D.Double(currentBegin.x, currentBegin.y + slopeStep);
			}
			else {
				// move the begin right the X axis
				currentBegin = new Point2D.Double(currentBegin.x + slopeStep, currentBegin.y);
			}

			// move end
			if (currentEnd.x < bounds.getX() + bounds.getWidth()) {
				// move end right the X axis
				currentEnd = new Point2D.Double(currentEnd.x + slopeStep, currentEnd.y);
			}
			else {
				// move end up the Y axis
				currentEnd = new Point2D.Double(currentEnd.x, currentEnd.y + slopeStep);
			}

			g2d.draw(new Line2D.Double(currentBegin.x, currentBegin.y, currentEnd.x, currentEnd.y));

			if (currentBegin.x > bounds.getX() + bounds.getWidth() || currentEnd.getY() < bounds.getY()) {
				done = true;
			}
		}
	}
}
