package com.jpprade.jcgmtosvg;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.List;

import org.apache.batik.svggen.SVGGraphics2D;

import net.sf.jcgm.core.ApplicationStructureAttribute;
import net.sf.jcgm.core.CGMDisplay;
import net.sf.jcgm.core.Member;
import net.sf.jcgm.core.StructuredDataRecord;

public class SVGPainter {

	
	
	public void paint(ApplicationStructureAttribute aps,CGMDisplay d) {
		SVGGraphics2D graphic = (SVGGraphics2D) d.getGraphics2D();
		 String attributeType= aps.getAttributeType();
		 StructuredDataRecord structuredDataRecord = aps.getStructuredDataRecord();
		
		if("region".equals(attributeType)) {
			 List<Member> members = structuredDataRecord.getMembers();
			 
			if(members!=null && members.size() ==2) {
				if(members.get(0).getCount() > 0) {
					if(members.get(0).getData().get(0).toString().equals("4")) {
						List<Double> objects = (List<Double>)(Object)members.get(1).getData();

						int n = (objects.size()-2)/6;

						Point2D.Double[] p1;
						Point2D.Double[] p2;
						Point2D.Double[] p3;
						Point2D.Double[] p4;

						p1 = new Point2D.Double[n];
						p2 = new Point2D.Double[n];
						p3 = new Point2D.Double[n];
						p4 = new Point2D.Double[n];

						int point = 0;
						int pos =0;
						while (point < n) {
							if (point == 0) {
								p1[point] = new Point2D.Double(objects.get(pos), objects.get(pos+1));
								pos+=2;
							}
							else {
								p1[point] = p4[point-1];
							}
							p2[point] = new Point2D.Double(objects.get(pos), objects.get(pos+1));
							pos+=2;
							p3[point] = new Point2D.Double(objects.get(pos), objects.get(pos+1));
							pos+=2;
							p4[point] = new Point2D.Double(objects.get(pos), objects.get(pos+1));
							pos+=2;
							point++;
						}


						Graphics2D g2d = d.getGraphics2D();
						g2d.setStroke(d.getLineStroke());
						g2d.setColor(d.getLineColor());



						//svgGenerator.getTopLevelGroup().setAttribute("id", "whatever")

						GeneralPath gp = new GeneralPath(); 

						for (int i = 0; i < p1.length; i++) {
							if(i==0) {
								gp.moveTo(p1[i].x, p1[i].y);								
							}

							gp.curveTo(p2[i].x, p2[i].y, p3[i].x, p3[i].y, p4[i].x, p4[i].y);

							if(i==p1.length-1) {
								gp.closePath();	
							}
						}
						g2d.draw(gp);
					}
				}
			}
		}else if("name".equals(attributeType)) {
			List<Member> members = structuredDataRecord.getMembers();
			if(members!=null && members.size() ==1) {
				if(members.get(0).getCount() > 0) {
					if(members.get(0).getData().get(0).toString().equalsIgnoreCase("Hotspot")) {
						d.setLineWidth(0);
					}
				}
			}
		}
	}
}
