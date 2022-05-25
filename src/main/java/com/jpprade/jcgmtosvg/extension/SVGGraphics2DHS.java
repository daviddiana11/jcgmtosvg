package com.jpprade.jcgmtosvg.extension;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.Map;

import org.apache.batik.svggen.DOMGroupManager;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Element;

public class SVGGraphics2DHS extends SVGGraphics2D {

	public SVGGraphics2DHS(SVGGeneratorContext generatorCtx,
			boolean textAsShapes) {
		super(generatorCtx,textAsShapes);
	}



	public void drawHotSpot(Shape s,String apsId,String apsName) {
		// Only BasicStroke can be converted to an SVG attribute equivalent.
		// If the GraphicContext's Stroke is not an instance of BasicStroke,
		// then the stroked outline is filled.
		Stroke stroke = gc.getStroke();
		if (stroke instanceof BasicStroke) {
			Element svgShape = shapeConverter.toSVG(s);
			if (svgShape != null) {
				enrichHS(svgShape,apsId,apsName);
				domGroupManager.addElement(svgShape, DOMGroupManager.DRAW);
			}
		} else {
			Shape strokedShape = stroke.createStrokedShape(s);
			fill(strokedShape);
		}
	}
	
	public void fillTDET(Shape s,String apsId,String apsName) {
        Element svgShape = shapeConverter.toSVG(s);
        if (svgShape != null) {
        	enrichTDET(svgShape,apsId,apsName);
            domGroupManager.addElement(svgShape, DOMGroupManager.FILL);
        }
    }
	
	
	public void drawTDET(Shape s,String apsId,String apsName) {
		// Only BasicStroke can be converted to an SVG attribute equivalent.
		// If the GraphicContext's Stroke is not an instance of BasicStroke,
		// then the stroked outline is filled.
		Stroke stroke = gc.getStroke();
		if (stroke instanceof BasicStroke) {
			Element svgShape = shapeConverter.toSVG(s);
			if (svgShape != null) {
				enrichTDET(svgShape,apsId,apsName);
				domGroupManager.addElement(svgShape, DOMGroupManager.DRAW);
			}
		} else {
			Shape strokedShape = stroke.createStrokedShape(s);
			fill(strokedShape);
		}
	}

	
	private void enrichHS(Element svgShape, String apsId,String apsName) {
		svgShape.setAttributeNS(null, "id", apsId);
		svgShape.setAttributeNS(null, "apsname", apsName);
		svgShape.setAttributeNS(null, "apsid", apsId);
		svgShape.setAttributeNS(null, "fill-rule", "evenodd");
		svgShape.setAttributeNS(null, "fill", "transparent");
		svgShape.setAttributeNS(null, "class", "hotspot");
		svgShape.setAttributeNS(null, "onclick", "clickHS('"+apsId+"')");
		svgShape.setAttributeNS(null, "stroke-width", "0");
	}
	
	private void enrichTDET(Element svgShape, String apsId,String apsName) {
		svgShape.setAttributeNS(null, "apsname", apsName);
		svgShape.setAttributeNS(null, "apsid", apsId);
		svgShape.setAttributeNS(null, "class", "tdet");
	}



	@Override
	public void drawString(String str, int x, int y) {
		
		//str = StringEscapeUtils.escapeHtml4(str);
		super.drawString(str, x, y);
	}
	
	
	
	
}
