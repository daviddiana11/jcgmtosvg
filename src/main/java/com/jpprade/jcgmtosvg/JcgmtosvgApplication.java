package com.jpprade.jcgmtosvg;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import net.sf.jcgm.core.CGMDisplay;


public class JcgmtosvgApplication {

	public static void main(String[] args) {
		
		String file ="E:\\x\\JA-C0418-I9023-2019-00\\illustrations\\sources\\ICN-JA-A-259602-M-C0418-00003-A-02-1.CGM";
		
		file = "E:/x/JP-F0210-FOSJP-2019-17/illustrations/sources/ICN-JP-A-601000-M-C0418-02698-A-05-1.cgm";
		file = "E:/x/JP-F0210-FOSJP-2019-17/illustrations/sources/ICN-JP-A-947501-X-F9111-18038-A-01-1.cgm";
		
		JcgmtosvgApplication JcgmtosvgApplication = new JcgmtosvgApplication();
		try {
			JcgmtosvgApplication.convert(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void convert(String file) throws IOException {
		

	    // Get a DOMImplementation.
	    DOMImplementation domImpl =
	      GenericDOMImplementation.getDOMImplementation();

	    // Create an instance of org.w3c.dom.Document.
	    String svgNS = "http://www.w3.org/2000/svg";
	    Document document = domImpl.createDocument(svgNS, "svg", null);

	    // Create an instance of the SVG Generator.
	    SVGGraphics2D svgGenerator = new SVGGraphics2D(document);  

	    
	    paint2(svgGenerator,file);

	    // Finally, stream out SVG to the standard output using
	    // UTF-8 encoding.
	    boolean useCSS = true; // we want to use CSS style attributes
	    Writer out = new OutputStreamWriter(System.out, "UTF-8");
	    svgGenerator.stream(out, useCSS);
	    
	}
	
	
	
	
	public static void paint2(Graphics2D g2d,String file) {
		SVGPainter svgPainter = new SVGPainter();
		
		File cgmFile = new File(file); 
		CGM4SVG cgm;

		
			try {
				cgm = new CGM4SVG(cgmFile,svgPainter);
				final CGMDisplay display = new CGMDisplay(cgm);
				//cgm.paint(cgmDisplay);
				Dimension size = cgm.getSize();				
				int width = size.width;
				int height = size.height;
				display.scale(g2d, width, height);
				display.paint(g2d);
				
			
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
		
		

	}

}
