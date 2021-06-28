package com.jpprade.jcgmtosvg;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVG12DOMImplementation;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.util.DocumentFactory;
import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.GenericElementNS;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGSyntax;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;
import org.xml.sax.SAXException;

import com.jpprade.jcgmtosvg.extension.MyExtensionHandler;
import com.jpprade.jcgmtosvg.extension.MyStyleHandler;
import com.jpprade.jcgmtosvg.extension.SVGGraphics2DHS;

import net.sf.jcgm.core.CGMDisplay;


public class JcgmtosvgApplication {

	
	
	public static Set<Integer> getRandom(int max,int number){
		HashSet<Integer> result = new HashSet<Integer>(); 
		Random rn = new Random();
		for(int i =0;i<number;i++) {
			result.add( rn.nextInt(max));			
		}
		return result;	
		
	}

	public void convert(String fileInput,String directoryOutput) throws IOException {

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
		//ctx.setStyleHandler(new MyStyleHandler(svgPainter,styleSheet));

		// Create an instance of the SVG Generator.
		//SVGGraphics2D svgGenerator = new SVGGraphics2D(document);  
		SVGGraphics2D svgGenerator = new SVGGraphics2DHS(ctx, false);

		CGM4SVG cgm = paint2(svgGenerator,fileInput,svgPainter);
		svgGenerator.setSVGCanvasSize(cgm.getSize());

		Element root = createrCss(document, styleSheet, svgGenerator);


		// Finally, stream out SVG to the standard output using
		// UTF-8 encoding.
		boolean useCSS = true; // we want to use CSS style attributes

		String fname = getFilenameWithoutExtension(new File(fileInput));
		File dout = new File(directoryOutput);
		File outf = new File(dout.getAbsolutePath()+"/"+fname+".svg");
		FileOutputStream fos = new FileOutputStream(outf);
		//Writer out = new OutputStreamWriter(System.out, "UTF-8");
		Writer out = new OutputStreamWriter(fos, "UTF-8");
		//svgGenerator.stream(out, useCSS);
		svgGenerator.stream(root,out,useCSS,false);


	}

	public void convertTojpg(String fileInput,String directoryOutput) throws IOException {
		String fname = getFilenameWithoutExtension(new File(fileInput));
		File dout = new File(directoryOutput);
		File outf = new File(dout.getAbsolutePath()+"/"+fname+".svg");

		File outfjpg = new File(dout.getAbsolutePath()+"/"+fname+".jpg");		 
		convert2(outf,outfjpg);
	}


	public void imgToSvg(String fileInput,String fileoutput) {

		DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();

		// Create an instance of org.w3c.dom.Document.
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);	    

		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		//svgGenerator.drawImage(img, x, y, observer)

	}
	/*
	 * svg -> jpg
	 */
	public boolean convert(File source, File destination) {

		JPEGTranscoder t = new JPEGTranscoder();

		// Set the transcoding hints.
		t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,new Float(.8));

		FileInputStream fis;
		try {
			fis = new FileInputStream(source);

			TranscoderInput input = new TranscoderInput(fis);

			// Create the transcoder output.
			OutputStream ostream = new FileOutputStream(destination);
			TranscoderOutput output = new TranscoderOutput(ostream);

			// Save the image.
			t.transcode(input, output);

			// Flush and close the stream.
			ostream.flush();
			ostream.close();
			return true;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (TranscoderException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}


	public boolean convert2(File source, File destination) {

		JPEGTranscoder t = new JPEGTranscoder();

		// Set the transcoding hints.
		t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,new Float(.9));

		FileInputStream fis;
		try {
			fis = new FileInputStream(source);


			/*DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
			//an instance of builder to parse the specified xml file  
			DocumentBuilder db = dbf.newDocumentBuilder();  
			Document document = db.parse(source);*/
			Document document = getDocument(fis);

			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "//*[@class='hotspot']";
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);

			for (int n = nodeList.getLength() - 1; n >= 0; n--) {
				Node node = nodeList.item(n);
				Element hotspot = (Element)node;	
				hotspot.getParentNode().removeChild(hotspot);
			}


			TranscoderInput input = new TranscoderInput(document);

			// Create the transcoder output.
			OutputStream ostream = new FileOutputStream(destination);
			TranscoderOutput output = new TranscoderOutput(ostream);

			// Save the image.
			t.transcode(input, output);

			// Flush and close the stream.
			ostream.flush();
			ostream.close();
			return true;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (TranscoderException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Document getDocument(InputStream is) {
		Document document = null;

		String parserClassname = XMLResourceDescriptor.getXMLParserClassName();
		String namespaceURI = SVGConstants.SVG_NAMESPACE_URI;
		String documentElement = SVGConstants.SVG_SVG_TAG;
		DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();


		// parse the XML document
		SAXDocumentFactory f = new SAXDocumentFactory(domImpl, parserClassname);
		//f.setFeature("http://xml.org/sax/features/external-general-entities", false);

		f.setValidating(false);
		try {

			document = f.createDocument(namespaceURI,
					documentElement,
					null,
					is);
			return document;

		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}

	}


	/*public void convert(String file) throws IOException {


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
	    //ctx.setStyleHandler(new MyStyleHandler(svgPainter,styleSheet));

	    // Create an instance of the SVG Generator.
	    //SVGGraphics2D svgGenerator = new SVGGraphics2D(document);  
	    SVGGraphics2D svgGenerator = new SVGGraphics2DHS(ctx, false);

	    CGM4SVG cgm = paint2(svgGenerator,file,svgPainter);
	    svgGenerator.setSVGCanvasSize(cgm.getSize());

	    Element root = createrCss(document, styleSheet, svgGenerator);


	    // Finally, stream out SVG to the standard output using
	    // UTF-8 encoding.
	    boolean useCSS = true; // we want to use CSS style attributes

	    String fname = getFilenameWithoutExtension(new File(file));
	    //File outf = new File("c:/Users/jpprade/Documents/vrac/JP-F0210-FOSJP-2019-17/"+fname+".svg");
	    File outf = new File("c:/Users/jpprade/Documents/vrac/"+fname+".svg");
	    FileOutputStream fos = new FileOutputStream(outf);
	    //Writer out = new OutputStreamWriter(System.out, "UTF-8");
	    Writer out = new OutputStreamWriter(fos, "UTF-8");
	    //svgGenerator.stream(out, useCSS);
	    svgGenerator.stream(root,out,useCSS,false);

	}*/


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
		//styleSheet.appendData("svg { height: 101%;}");
		styleSheet.appendData(".hotspot { cursor: pointer;}");
		styleSheet.appendData("@keyframes blink {100%,0% {fill: transparent;}60% {fill: #f00;}}.hotspotBlink {animation: blink 0.25s 3;}");

		//-----------JS

		//Element javascript = document.createElementNS(SVGSyntax.SVG_NAMESPACE_URI, SVGSyntax.SVG_SCRIPT_TAG);
		Element javascript = document.createElement(SVGSyntax.SVG_SCRIPT_TAG);
		javascript.setAttribute("id", "nativeJSHS");
		defs.appendChild(javascript);

		CDATASection javascriptData = document.createCDATASection("");
		javascript.appendChild(javascriptData);

		javascriptData.appendData("function clickHS(apsid){var apselement = document.getElementById(apsid);apselement.classList.add('hotspotBlink');setTimeout(function(){apselement.classList.remove('hotspotBlink');},750);}");


		return root;
	}




	public static CGM4SVG paint2(Graphics2D g2d,String file,SVGPainter svgPainter) {


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


			return cgm;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		

	}

	private String getFilenameWithoutExtension(File file) throws IOException {
		String filename = file.getCanonicalPath();
		String filenameWithoutExtension;
		if (filename.contains("."))
			filenameWithoutExtension = filename.substring(filename.lastIndexOf(System.getProperty("file.separator"))+1, filename.lastIndexOf('.'));
		else
			filenameWithoutExtension = filename.substring(filename.lastIndexOf(System.getProperty("file.separator"))+1);

		return filenameWithoutExtension;
	}

}
