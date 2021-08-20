package com.jpprade.jcgmtosvg.commands;

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
import java.text.DecimalFormat;

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

public class PolyBezierV2 extends ExtendedCommand{
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
		this.paint(d, figure, null, null, null, null, null,null,null);
	}
	
	public void paint(CGMDisplay d,BeginFigure figure, FillColour currentFC, EdgeColour currentEC, EdgeWidth currentEW, LineColour currentLC, LineWidth currentLW, String apsid, String apsname) {


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
					//g2d.draw(this.curves[i]);
					drawCustom(g2d, this.curves[i], apsid, apsname);
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
			
			DecimalFormat df = new DecimalFormat("#.###");
			String fm = df.format(this.p1[0].x);
			/*if("82,329".equals(fm) || "57,975".equals(fm)) {
				System.out.println(this.p1[0].x);
			}*/
			/*if("216,097".equals(fm)) {
				System.out.println(this.p1[0].x);
			}*/

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
			
			/*if (currentFC!=null) {			
				Color fColor = d.getFillColor();
				g2d.setPaint(fColor);
				
			}*/
			//M82.3294 112.488 C82.3294 112.488
			Style s = d.getInteriorStyle();
			if(currentFC!=null) {
				
				
				if(InteriorStyle.Style.HATCH.equals(s)) {
					//drawHatch(gp,g2d,currentFC.,null);
					//currentFC.
					drawHatch(gp,g2d,d.getFillColor(), d.getHatchType());
					drawCustom(g2d, gp, apsid, apsname);
				}else if(InteriorStyle.Style.EMPTY.equals(s)) {
					drawCustom(g2d, gp, apsid, apsname);
				}else {
					Color fColor = d.getFillColor();
					g2d.setPaint(fColor);
					g2d.fill(gp);
					if (d.drawEdge()) {
			    		g2d.setColor(d.getEdgeColor());
			    		g2d.setStroke(d.getEdgeStroke());
			    		drawCustom(g2d, gp, apsid, apsname);
			    	}
				}
				
			}else {
				if(InteriorStyle.Style.SOLID.equals(s)) {
					Color fColor = d.getFillColor();
					g2d.setPaint(fColor);					
					g2d.fill(gp);
					g2d.setStroke(d.getLineStroke());
					g2d.setColor(d.getLineColor());
					drawCustom(g2d, gp, apsid, apsname);
					
				}else {
					drawCustom(g2d, gp, apsid, apsname);
				}
			}
			
			
		}

	}



	private void drawCustom(SVGGraphics2DHS g2d, Shape gp,String apsid, String apsname) {
		/*if(apsid==null)
			g2d.draw(gp);
		else
			g2d.drawHotSpot(gp, apsid, apsname);*/
		g2d.draw(gp);
	}



	public int getContinuityIndicator() {
		return continuityIndicator;
	}



	public void setContinuityIndicator(int continuityIndicator) {
		this.continuityIndicator = continuityIndicator;
	}



	public CubicCurve2D.Double[] getCurves() {
		return curves;
	}



	public void setCurves(CubicCurve2D.Double[] curves) {
		this.curves = curves;
	}



	public Point2D.Double[] getP1() {
		return p1;
	}



	public void setP1(Point2D.Double[] p1) {
		this.p1 = p1;
	}



	public Point2D.Double[] getP2() {
		return p2;
	}



	public void setP2(Point2D.Double[] p2) {
		this.p2 = p2;
	}



	public Point2D.Double[] getP3() {
		return p3;
	}



	public void setP3(Point2D.Double[] p3) {
		this.p3 = p3;
	}



	public Point2D.Double[] getP4() {
		return p4;
	}



	public void setP4(Point2D.Double[] p4) {
		this.p4 = p4;
	}

	private Point2D.Double[] concatArray(Point2D.Double[] a1,Point2D.Double[] a2){
		Point2D.Double[] ret = new Point2D.Double[a1.length+a2.length];
		int index = 0;
		for(int i =0;i<a1.length;i++) {
			ret[index]=a1[i];
			index++;
		}
		for(int i =0;i<a2.length;i++) {
			ret[index]=a2[i];
			index++;
		}
		return ret;
	}


	public void mergeShape(PolyBezierV2 polyBezierV2) {
		this.p1 = concatArray(p1,polyBezierV2.getP1());
		this.p2 = concatArray(p2,polyBezierV2.getP2());
		this.p3 = concatArray(p3,polyBezierV2.getP3());
		this.p4 = concatArray(p3,polyBezierV2.getP4());
		
	}
	
	
	
}
