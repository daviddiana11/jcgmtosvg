package com.jpprade.jcgmtosvg.extension;

import java.util.Map;

import org.apache.batik.svggen.DefaultStyleHandler;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.StyleHandler;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;

import com.jpprade.jcgmtosvg.SVGPainter;

public class MyStyleHandler extends DefaultStyleHandler {
	
	// The CDATA section that holds the CSS stylesheet.
	private CDATASection styleSheet;
	private SVGPainter svgPainter;
	
	Element lastElement = null;
	
	public MyStyleHandler(SVGPainter svgPainter,CDATASection styleSheet) {
	    this.styleSheet = styleSheet;
	    this.svgPainter=svgPainter;
	  }


	public void setStyle(Element element, Map styleMap, SVGGeneratorContext generatorContext) {
		
		
		if(svgPainter.hasPaintedHS()) {
			String apsId = getSvgPainter().getCurrentApsId();
			String apsName = getSvgPainter().getCurrentApsName();
			lastElement.setAttributeNS(null, "id", apsId);
			lastElement.setAttributeNS(null, "apsname", apsName);
			lastElement.setAttributeNS(null, "apsid", apsId);
			lastElement.setAttributeNS(null, "fill-rule", "evenodd");
			lastElement.setAttributeNS(null, "fill", "transparent");
			lastElement.setAttributeNS(null, "class", "hotspot");
			lastElement.setAttributeNS(null, "onclick", "clickHS('"+apsId+"')");
			lastElement.setAttributeNS(null, "stroke-width", "0");
			
			svgPainter.hotspotDrawn();
		}
		lastElement=element;
		
		super.setStyle(element, styleMap, generatorContext);
		/*Iterator iter = styleMap.keySet().iterator();

	    // Create a new class in the style sheet.
	    String id = generatorContext.getIDGenerator().generateID("C");
	    styleSheet.appendData("."+ id +" {");

	    // Append each key/value pair.
	    while (iter.hasNext()) {
	      String key = (String) iter.next();
	      String value = (String) styleMap.get(key);
	      styleSheet.appendData(key + ":" + value + ";");
	    }

	    styleSheet.appendData("}\n");

	    // Reference the stylesheet class on the element to be styled.
	    element.setAttributeNS(null, "class", id);*/
	}

	public SVGPainter getSvgPainter() {
		return svgPainter;
	}

	public void setSvgPainter(SVGPainter svgPainter) {
		this.svgPainter = svgPainter;
	}

}
