package com.jpprade.jcgmtosvg;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.GenericElementNS;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGSyntax;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGSVGElement;

import com.jpprade.jcgmtosvg.extension.MyExtensionHandler;
import com.jpprade.jcgmtosvg.extension.MyStyleHandler;

import net.sf.jcgm.core.CGMDisplay;


public class JcgmtosvgApplication {

	public static void main(String[] args) {
		
		String file ="E:/x/JA-C0418-I9023-2019-00/illustrations/sources/ICN-JA-A-259602-M-C0418-00003-A-02-1.CGM";
		
		//file = "E:/x/JP-F0210-FOSJP-2019-17/illustrations/sources/ICN-JP-A-601000-M-C0418-02698-A-05-1.cgm";
		//file = "E:/x/JP-F0210-FOSJP-2019-17/illustrations/sources/ICN-JP-A-947501-X-F9111-18038-A-01-1.cgm";
		//file = "E:/x/JP-F0210-FOSJP-2019-17/illustrations/sources/ICN-02-A-121220-R-F0228-00190-A-04-1.cgm";
		
		JcgmtosvgApplication JcgmtosvgApplication = new JcgmtosvgApplication();
		try {
			JcgmtosvgApplication.convert(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void convert(String file) throws IOException {
		

	    // Get a DOMImplementation.
	    //DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
	    DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();

	    // Create an instance of org.w3c.dom.Document.
	    String svgNS = "http://www.w3.org/2000/svg";
	    Document document = domImpl.createDocument(svgNS, "svg", null);

	    SVGPainter svgPainter = new SVGPainter();
	    
	    SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
	    //ctx.setExtensionHandler(new MyExtensionHandler());
	    CDATASection styleSheet = document.createCDATASection("");
	    ctx.setStyleHandler(new MyStyleHandler(svgPainter,styleSheet));
	    
	    // Create an instance of the SVG Generator.
	    //SVGGraphics2D svgGenerator = new SVGGraphics2D(document);  
	    SVGGraphics2D svgGenerator = new SVGGraphics2D(ctx, false);
	    
	    paint2(svgGenerator,file,svgPainter);
	    Element root = createrCss(document, styleSheet, svgGenerator);
	    

	    // Finally, stream out SVG to the standard output using
	    // UTF-8 encoding.
	    boolean useCSS = true; // we want to use CSS style attributes
	    Writer out = new OutputStreamWriter(System.out, "UTF-8");
	    //svgGenerator.stream(out, useCSS);
	    svgGenerator.stream(root,out,useCSS,false);
	    
	}


	private Element createrCss(Document document, CDATASection styleSheet, SVGGraphics2D svgGenerator) {
		
		
		
		
		//Element element = svgGenerator.getRoot();
		//System.out.println(element.getNodeName());
		/*Element gen = document.getElementById(SVGSyntax.ID_PREFIX_GENERIC_DEFS);
		if(gen!=null)
			System.out.println(gen.getNodeName());
		else
			System.out.println("non trouvé");*/
			
		// Add a stylesheet to the definition section.
		
		
		SVGSVGElement root = (SVGSVGElement) svgGenerator.getRoot();
		
		
	    Element defs = root.getElementById(SVGSyntax.ID_PREFIX_GENERIC_DEFS);
	    Element style = document.createElementNS(SVGSyntax.SVG_NAMESPACE_URI, SVGSyntax.SVG_STYLE_TAG);
	    style.setAttributeNS(null, SVGSyntax.SVG_TYPE_ATTRIBUTE, "text/css");
	    style.appendChild(styleSheet);
	    defs.appendChild(style);
	    
	    
	    styleSheet.appendData(".hotspot { cursor: pointer;}");
	    return root;
	}
	
	
	
	
	public static void paint2(Graphics2D g2d,String file,SVGPainter svgPainter) {
		
		
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
