package com.jpprade.jcgmtosvg;

import com.jpprade.jcgmtosvg.extension.SVGGraphics2DHS;
import net.sf.jcgm.core.BeginTileArray;
import net.sf.jcgm.core.BitonalTile;
import net.sf.jcgm.core.CGMDisplay;
import net.sf.jcgm.core.Command;
import net.sf.jcgm.core.CompressionType;
import net.sf.jcgm.core.ScalingMode;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGSyntax;
import org.apache.batik.util.SVGConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGSVGElement;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JcgmtosvgApplication {
	
	private static final Logger logger = LoggerFactory.getLogger(JcgmtosvgApplication.class);
	
	public void optimizeHotspot(File svgFile, double scale, boolean isMosaic) {
		SVGUtils svgu = new SVGUtils();
		svgu.moveHotspotToRightLayer(svgFile, svgFile);
		if (scale > 0 && scale < 0.0001 || isMosaic) {
			svgu.applyTransformation(svgFile, svgFile);
			logger.info("Scaling very large illustration: {}", svgFile.getAbsolutePath());
		}
	}
	
	/**
	 * Converts a single CGM file to an SVG.
	 *
	 * @param fileInput       path to the CGM file
	 * @param directoryOutput path to the SVG directory output
	 * @return the SVG file
	 * @throws IOException
	 */
	public File convert(String fileInput, String directoryOutput) throws IOException {
		return this.convert(fileInput, directoryOutput, new HashMap<>(), true);
	}
	
	public File convert(String fileInput, String directoryOutput, boolean optimize) throws IOException {
		return this.convert(fileInput, directoryOutput, new HashMap<>(), optimize);
	}
	
	public File convert(String fileInput, String directoryOutput, Map<String, Object> info) throws IOException {
		return this.convert(fileInput, directoryOutput, info, true);
	}
	
	public File convert(String fileInput, String directoryOutput, Map<String, Object> info, boolean optimize) throws IOException {
		logger.info("Converting CGM file to SVG: {} optimize = {}", fileInput, optimize);
		// Get a DOMImplementation.
		DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
		
		// Create an instance of org.w3c.dom.Document.
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);
		
		SVGPainter svgPainter = new SVGPainter();
		
		SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
		
		CGM4SVG cgm;
		try {
			cgm = loadCgm(fileInput, svgPainter);
		} catch (Exception e) {
			logger.error("Error while converting " + fileInput + ", " + e.getMessage(), e);
			throw new JcgmToSvgException("Error while converting the file", e.getCause());
		}
		
		if (cgm == null) {
			throw new JcgmToSvgException("Could not load the CGM");
		}
		
		double scale = findScale(cgm);
		boolean isMosaic = findMosaic(cgm);
		info.put("Scale", scale);
		if (scale > 0 && scale <= 0.0001) {
			ctx.setPrecision(8);
			logger.info("Precision 8 {} {}", fileInput, scale);
		} else if (scale > 0.0001 && scale < 0.01) {
			ctx.setPrecision(4);
			logger.info("Precision 4 {} {}", fileInput, scale);
		} else {
			ctx.setPrecision(4);
		}
		
		boolean isT6 = findT6(cgm);
		info.put("isT6", isT6);
		
		CDATASection styleSheet = document.createCDATASection("");
		
		// Create an instance of the SVG Generator.
		SVGGraphics2D svgGenerator = new SVGGraphics2DHS(ctx, false);
		
		paint2(svgGenerator, cgm);
		
		svgGenerator.setSVGCanvasSize(cgm.getSize());
		
		Element root = createrCss(document, styleSheet, svgGenerator);
		
		// Finally, stream out SVG to the standard output using
		// UTF-8 encoding.
		boolean useCSS = true; // we want to use CSS style attributes
		String fname = getFilenameWithoutExtension(new File(fileInput));
		File dout = new File(directoryOutput);
		File outf = new File(dout.getAbsolutePath() + "/" + fname + ".svg");
		FileOutputStream fos = new FileOutputStream(outf);
		Writer out = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
		svgGenerator.stream(root, out, useCSS, false);
		
		if (optimize) {
			this.optimizeHotspot(outf, scale, isMosaic);
		}
		
		return outf;
		
		
	}
	
	public static boolean findMosaic(CGM4SVG cgm) {
		List<Command> commands = cgm.getCommands();
		for (Command c : commands) {
			if (c instanceof BeginTileArray bta && (bta.getnTilesInLineDirection() > 1 || bta.getnTilesInPathDirection() > 1)) {
					return true;
			}
		}
		return false;
	}
	
	public static double findScale(CGM4SVG cgm) {
		List<Command> commands = cgm.getCommands();
		for (Command c : commands) {
			if (c instanceof ScalingMode sm) {
				return sm.getMetricScalingFactor();
			}
		}
		return 0;
	}
	
	public static boolean findT6(CGM4SVG cgm) {
		List<Command> commands = cgm.getCommands();
		for (Command c : commands) {
			if (c instanceof BitonalTile bt) {
				CompressionType ct = bt.getCompressionType();
				if (ct == CompressionType.T6) {
					return true;
				}
			}
		}
		return false;
	}
	
	private Element createrCss(Document document, CDATASection styleSheet, SVGGraphics2D svgGenerator) {
		// Add a stylesheet to the definition section.
		SVGSVGElement root = (SVGSVGElement) svgGenerator.getRoot();
		
		Element defs = root.getElementById(SVGSyntax.ID_PREFIX_GENERIC_DEFS);
		Element style = document.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_STYLE_TAG);
		style.setAttributeNS(null, SVGConstants.SVG_TYPE_ATTRIBUTE, "text/css");
		style.appendChild(styleSheet);
		defs.appendChild(style);
		styleSheet.appendData("svg { fill-rule: evenodd;pointer-events: none;}");
		
		styleSheet.appendData(".hotspot { cursor: pointer;pointer-events: all;}");
		styleSheet.appendData("@keyframes blink {100%,0% {fill: transparent;}60% {fill: #f00;}}.hotspotBlink {animation: blink 0.25s 3;}");
		//-----------JS
		
		Element javascript = document.createElement(SVGConstants.SVG_SCRIPT_TAG);
		javascript.setAttribute("id", "nativeJSHS");
		defs.appendChild(javascript);
		
		CDATASection javascriptData = document.createCDATASection("");
		javascript.appendChild(javascriptData);
		
		javascriptData.appendData("function clickHS(apsid){var apselement = document.getElementById(apsid);apselement.classList.add('hotspotBlink');setTimeout(function(){apselement.classList.remove('hotspotBlink');},750);}");
		
		return root;
	}
	
	public static CGM4SVG loadCgm(String file, SVGPainter svgPainter) {
		File cgmFile = new File(file);
		CGM4SVG cgm;
		try {
			cgm = new CGM4SVG(cgmFile, svgPainter);
		} catch (IOException e) {
			logger.error("Error while loading the CGM file [" + file + "]: " + e.getMessage(), e);
			return null;
		}
		return cgm;
	}
	
	public static void paint2(Graphics2D g2d, CGM4SVG cgm) {
		final CGMDisplay display = new CGMDisplay4SVG(cgm);
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
			filenameWithoutExtension = filename.substring(filename.lastIndexOf(FileSystems.getDefault().getSeparator()) + 1, filename.lastIndexOf('.'));
		else
			filenameWithoutExtension = filename.substring(filename.lastIndexOf(FileSystems.getDefault().getSeparator()) + 1);
		
		return filenameWithoutExtension;
	}
	
}

