package com.jpprade.jcgmtosvg;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.batik.anim.dom.AbstractSVGAnimatedLength;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGSyntax;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGSVGElement;

import com.jpprade.jcgmtosvg.extension.SVGGraphics2DHS;

import net.sf.jcgm.core.BeginTileArray;
import net.sf.jcgm.core.BitonalTile;
import net.sf.jcgm.core.CGMDisplay;
import net.sf.jcgm.core.Command;
import net.sf.jcgm.core.CompressionType;
import net.sf.jcgm.core.ScalingMode;


public class JcgmtosvgApplication {
	
	private static Logger logger = LoggerFactory.getLogger(JcgmtosvgApplication.class);
	
	
	
	public void optimizeHotspot(File svgFile,double scale, boolean isMosaic) {
		SVGUtils svgu = new SVGUtils();		
		svgu.moveHotspotToRightLayer(svgFile, svgFile);
		if(scale> 0 && scale < 0.0001 || isMosaic) {
			svgu.applyTransformation(svgFile, svgFile);
			logger.info("Scalling very large illustration : " + svgFile.getAbsolutePath());
		}
		
		
	}

	public boolean isJPEG(String file) {
		boolean isjpeg = false;
		File inputfile = new File(file);
		InputStream bin;
		try {
			bin = new FileInputStream(inputfile);
			byte[] buffer = new byte[2];
			bin.read(buffer);
			int first = unsignedToBytes(buffer[0]);
			int second= unsignedToBytes(buffer[1]);
			if(first==0xFF && second==0xD8) {//les jpg commencent par FF D8 (JFIF ou JPEG) 
				isjpeg = true;
			}
			
			bin.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isjpeg;
		
		
	}
	
	public static int unsignedToBytes(byte b) {
		return b & 0xFF;
	}

	
	
	
	
	public File convert(String fileInput,String directoryOutput) throws IOException {
		return this.convert(fileInput,  directoryOutput,new HashMap<String,Object>(), true);
	}
	
	public File convert(String fileInput,String directoryOutput, boolean optimize) throws IOException {
		return this.convert(fileInput,  directoryOutput,new HashMap<String,Object>(), optimize);
	}
	
	public File convert(String fileInput,String directoryOutput,Map<String,Object> info) throws IOException {
		return this.convert(fileInput,  directoryOutput,info, true);
	}

	public File convert(String fileInput,String directoryOutput,Map<String,Object> info,boolean optimize) throws IOException {
		logger.info("Converting CGM file to SVG :" + fileInput + " optimize = " +optimize);
		// Get a DOMImplementation.
		//DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();

		// Create an instance of org.w3c.dom.Document.
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);

		SVGPainter svgPainter = new SVGPainter();

		SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
		
		CGM4SVG cgm =loadCgm(fileInput, svgPainter);
		
		double scale = findScale(cgm);
		boolean isMosaic = findMosaic(cgm);
		isMosaic=false;
		info.put("Scale", scale);
		if(scale> 0 && scale <= 0.0001) {
			ctx.setPrecision(8);			
			logger.info("Precision 8 " + fileInput + " " + scale);
		}else if(scale > 0.0001 && scale <0.01) {
			ctx.setPrecision(4);
			logger.info("Precision 4 " + fileInput + " " + scale);
		}else {
			ctx.setPrecision(4);
		}
		
		boolean isT6 = findT6(cgm);
		info.put("isT6", isT6);
		
		
		//ctx.setExtensionHandler(new MyExtensionHandler());
		CDATASection styleSheet = document.createCDATASection("");
		//ctx.setStyleHandler(new MyStyleHandler(svgPainter,styleSheet));

		// Create an instance of the SVG Generator.
		//SVGGraphics2D svgGenerator = new SVGGraphics2D(document);  
		SVGGraphics2D svgGenerator = new SVGGraphics2DHS(ctx, false);

		paint2(svgGenerator,cgm);
		
		
		
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
		Writer out = new OutputStreamWriter(fos, "ISO-8859-1");
		//svgGenerator.stream(out, useCSS);
		svgGenerator.stream(root,out,useCSS,false);
		
		if(optimize) {
			this.optimizeHotspot(outf,scale,isMosaic);
		}
		
		return outf;


	}
	
	public static boolean findMosaic(CGM4SVG cgm) {
		List<Command> commands = cgm.getCommands();
		for(Command c : commands) {
			if (BeginTileArray.class.isInstance(c)) {
				BeginTileArray bta = (BeginTileArray)c;
				if(bta.getnTilesInLineDirection() > 1 || bta.getnTilesInPathDirection() > 1) {
					return true;
				}
			}
		}
		return false;
	}

	public static double findScale(CGM4SVG cgm) {
		List<Command> commands = cgm.getCommands();
		for(Command c : commands) {
			if (ScalingMode.class.isInstance(c)) {
				ScalingMode sm = (ScalingMode)c;
				double sf = sm.getMetricScalingFactor();
				return sf;
			}
		}
		return 0;
	}
	
	public static boolean findT6(CGM4SVG cgm) {
		List<Command> commands = cgm.getCommands();
		for(Command c : commands) {
			if (BitonalTile.class.isInstance(c)) {
				BitonalTile bt = (BitonalTile)c;				
				CompressionType ct = bt.getCompressionType();				
				if(ct == CompressionType.T6) {
					return true;
				}
			}
		}
		return false;
	}

	public void convertTojpg(String fileInput,String directoryOutput) throws IOException {
		String fname = getFilenameWithoutExtension(new File(fileInput));
		File dout = new File(directoryOutput);
		File outf = new File(dout.getAbsolutePath()+"/"+fname+".svg");

		File outfjpg = new File(dout.getAbsolutePath()+"/"+fname+".jpg");		 
		convert2(outf,outfjpg);
	}
	
	public boolean convertTojpgTimeout(String fileInput,String directoryOutput) throws IOException {
		ExecutorService executor = Executors.newCachedThreadPool();
		
		Callable<Boolean> task = new Callable<Boolean>() {
			public Boolean call() throws IOException {
				String fname = getFilenameWithoutExtension(new File(fileInput));
				File dout = new File(directoryOutput);
				File outf = new File(dout.getAbsolutePath()+"/"+fname+".svg");

				File outfjpg = new File(dout.getAbsolutePath()+"/"+fname+".jpg");		 
				convert2(outf,outfjpg);
				return true;
			}
		};
		Future<Boolean> future = executor.submit(task);
		try {
			Boolean result = future.get(12, TimeUnit.SECONDS);
			return result;
		} catch (TimeoutException ex) {
			return false;
		} catch (InterruptedException e) {
			return false;
		} catch (ExecutionException e) {
			return false;
		} finally {
			future.cancel(true); // may or may not desire this
		}
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
	/*public boolean convert(File source, File destination) {
		logger.info("Converting CGM file to SVG " + source);
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
	}*/


	public boolean convert2(File source, File destination) {

		JPEGTranscoder t = new JPEGTranscoder();

		// Set the transcoding hints.
		t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,new Float(1));

		FileInputStream fis;
		try {
			fis = new FileInputStream(source);
			
			//On supprime les hostpost
			Document document = getDocument(fis);

			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "//*[@class='hotspot']";
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);

			for (int n = nodeList.getLength() - 1; n >= 0; n--) {
				Node node = nodeList.item(n);
				Element hotspot = (Element)node;	
				hotspot.getParentNode().removeChild(hotspot);
			}
			
			boolean resized = setMaxResolution(document);
			
			


			TranscoderInput input = new TranscoderInput(document);

			// Create the transcoder output.
			OutputStream ostream = new FileOutputStream(destination);
			TranscoderOutput output = new TranscoderOutput(ostream);

			// Save the image.
			t.transcode(input, output);

			// Flush and close the stream.
			ostream.flush();
			ostream.close();
			
			/*if(resized) {
				trimImage(destination);
			}*/
			return true;

		} catch (FileNotFoundException e) {
			System.out.println("Error1 " + source );
			e.printStackTrace();
			System.exit(0);
			return false;
		} catch (TranscoderException e) {
			System.out.println("Error2 " + source );
			e.printStackTrace();
			System.exit(0);
			return false;
		} catch (IOException e) {
			System.out.println("Error3 " + source );
			e.printStackTrace();
			System.exit(0);
			return false;
		} catch (XPathExpressionException e) {
			System.out.println("Error4 " + source );			
			e.printStackTrace();
			System.exit(0);
			return false;
		}catch(Exception e) {
			System.out.println("Error5 " + source );			
			e.printStackTrace();
			System.exit(0);
			return false;
		}
	}

	private void trimImage(File destination) {
		BufferedImage img = null;
		try {
		    img = ImageIO.read(destination);
		    
		    int maxW=0;
		    int maxH=0;
		    
		    for(int i = 0; i< img.getWidth();i++) {
		    	for(int j = 0; j< img.getHeight();j++) {
		    		int argb = img.getRGB(i, j);
		    		
		    		int r = (argb>>16)&0xFF;
		    		int g = (argb>>8)&0xFF;
		    		int b = (argb>>0)&0xFF;
		    		if(r!= 255 || g!=255 || b!=255) {
		    			if(i>maxW) {
		    				maxW=i;
		    			}
		    			if(j>maxH) {
		    				maxH=j;
		    			}
		    			continue;
		    		}
		    		
		    	}
		    }
		    System.out.println("taille to trim=" + maxW + " * " +maxH );
		    
		    img = img.getSubimage(0, 0, maxW + 10, maxH + 10);//petite marge
		    
		    ImageIO.write(img, "JPG", destination);
		    
		} catch (IOException e) {
			System.out.println("Error6 " + destination.getAbsolutePath() );			
			e.printStackTrace();
		}
		
	}
	
	public static BufferedImage crop(BufferedImage image) {
	    int minY = 0, maxY = 0, minX = Integer.MAX_VALUE, maxX = 0;
	    boolean isBlank, minYIsDefined = false;
	    Raster raster = image.getRaster();

	    for (int y = 0; y < image.getHeight(); y++) {
	        isBlank = true;

	        for (int x = 0; x < image.getWidth(); x++) {
	            //Change condition to (raster.getSample(x, y, 3) != 0) 
	            //for better performance
	            if (raster.getPixel(x, y, (int[]) null)[3] != 0) {
	                isBlank = false;

	                if (x < minX) minX = x;
	                if (x > maxX) maxX = x;
	            }
	        }

	        if (!isBlank) {
	            if (!minYIsDefined) {
	                minY = y;
	                minYIsDefined = true;
	            } else {
	                if (y > maxY) maxY = y;
	            }
	        }
	    }

	    return image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
	}
	

	private boolean setMaxResolution(Document document) {
		
		BridgeContext ctx = new BridgeContext(new UserAgentAdapter());
		GVTBuilder builder = new GVTBuilder();
		GraphicsNode gvtRoot = builder.build(ctx, document);
		Rectangle2D rc = gvtRoot.getSensitiveBounds();
		
		System.out.println(rc.getWidth() );
		System.out.println(rc.getHeight() );
		System.out.println(rc.getX() );
		System.out.println(rc.getY() );
		
		//on ajoute une marge de 100px autour
		int margin = 100;
		double width = rc.getWidth() + 2 * margin;
		double height = rc.getHeight() + 2 * margin;
		double x = rc.getX() - margin;
		double y = rc.getY() - margin ;
		
		//on verifie la taille du canvas, on la limite si elle est trop grande
		Element e = document.getDocumentElement();		
		
		SVGOMSVGElement svgElement = (SVGOMSVGElement) e;
		
		
		if(StringUtils.isEmpty(e.getAttribute("width")) || StringUtils.isEmpty(e.getAttribute("height"))) {
			return false;
		}
		
		// 'width' attribute - default is 100%
		AbstractSVGAnimatedLength _width = (AbstractSVGAnimatedLength) svgElement.getWidth();
		float w = _width.getCheckedValue();

		// 'height' attribute - default is 100%
		AbstractSVGAnimatedLength _height =(AbstractSVGAnimatedLength) svgElement.getHeight();
		float h = _height.getCheckedValue();
		
		
		float ratio = w/h;		
		if(w > 3000) {
			/*int newW = 3000;
			int newH = (int)(newW / ratio);
			
			e.setAttribute("width", String.valueOf(newW));
			e.setAttribute("height", String.valueOf(newH));*/
			
			e.setAttribute("width", String.valueOf(width));
			e.setAttribute("height", String.valueOf(height));
			
			e.setAttribute("viewBox", String.valueOf((int)x) 
					+ " " + String.valueOf((int)y)
					+ " " + String.valueOf((int)width)
					+ " " + String.valueOf((int)height));
			return true;
		}
		//todo sauvegarder la modif de viewbox et viewport dans le svg pour éviter le scroll
		//todo 2 faire ca aussi pour les svg de taille normale ?
		
		return false;
	}

	public Document getDocument(InputStream is) throws IOException {
		Document document = null;

		String parserClassname = XMLResourceDescriptor.getXMLParserClassName();
		String namespaceURI = SVGConstants.SVG_NAMESPACE_URI;
		String documentElement = SVGConstants.SVG_SVG_TAG;
		DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();


		// parse the XML document
		SAXDocumentFactory f = new SAXDocumentFactory(domImpl, parserClassname);
		//f.setFeature("http://xml.org/sax/features/external-general-entities", false);

		f.setValidating(false);
		

			document = f.createDocument(namespaceURI,
					documentElement,
					null,
					is);
			return document;

		

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
		styleSheet.appendData("svg { fill-rule: evenodd;pointer-events: none;}");
		
		styleSheet.appendData(".hotspot { cursor: pointer;pointer-events: all;}");
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
	
	public static CGM4SVG loadCgm(String file,SVGPainter svgPainter) {
		File cgmFile = new File(file); 
		CGM4SVG cgm;
		try {
			cgm = new CGM4SVG(cgmFile,svgPainter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return cgm;
	}



	public static void paint2(Graphics2D g2d,CGM4SVG cgm) {

		final CGMDisplay display = new CGMDisplay4SVG(cgm);
		//cgm.paint(cgmDisplay);
		Dimension size = cgm.getSize();				
		int width = size.width;
		int height = size.height;
		display.scale(g2d, width, height);
		display.paint(g2d);


	}

	static String getFilenameWithoutExtension(File file) throws IOException {
		String filename = file.getCanonicalPath();
		String filenameWithoutExtension;
		if (filename.contains("."))
			filenameWithoutExtension = filename.substring(filename.lastIndexOf(System.getProperty("file.separator"))+1, filename.lastIndexOf('.'));
		else
			filenameWithoutExtension = filename.substring(filename.lastIndexOf(System.getProperty("file.separator"))+1);

		return filenameWithoutExtension;
	}

}

