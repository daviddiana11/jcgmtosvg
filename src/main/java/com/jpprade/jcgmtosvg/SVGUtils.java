package com.jpprade.jcgmtosvg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SVGUtils {
	
	private static Logger logger = LoggerFactory.getLogger(SVGUtils.class);
	
	private DecimalFormat df;
	
	private DecimalFormat dfCoo;

	
	public SVGUtils() {
		df = new DecimalFormat("0.00");
		dfCoo = new DecimalFormat("0.0000");
		
	    DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
	    sym.setDecimalSeparator('.');
	    df.setDecimalFormatSymbols(sym);
	    dfCoo.setDecimalFormatSymbols(sym);
	}

	/**
	 * Déplace les hotspot SDET au premier plan
	 * 
	 * Peut aussi améliorer les hotspots "normaux" afin que le clignotement ne se mette pas par dessus
	 * 
	 * @param sourceF
	 * @param destination
	 */
	public void moveHotspotToRightLayer(File sourceF, File destination) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db;
		try {
			dbf.setValidating(false);
			dbf.setNamespaceAware(true);
			dbf.setFeature("http://xml.org/sax/features/namespaces", false);
			dbf.setFeature("http://xml.org/sax/features/validation", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			
			
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(sourceF);

		

			XPath xPath = XPathFactory.newInstance().newXPath();


			//HS SDET au premier plan
			{
				/*String expressionG = "//svg/g/g[position()=last()]";
				Element lastg = (Element) xPath.compile(expressionG).evaluate(doc, XPathConstants.NODE);*/
				String expressionG = "//svg/g/g";
				NodeList nodeGList = (NodeList) xPath.compile(expressionG).evaluate(doc, XPathConstants.NODESET);

				//String expression = "//*[@class='hotspot' and starts-with(@apsname,'SDET')]";
				String expression = "//*[@class='hotspot']";
				NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
				
				if( nodeList.getLength() ==0) {
					return;
				}
				
				

				for (int n = 0; n < nodeList.getLength() ; n++) {
					Node node = nodeList.item(n);
					Element hotspot = (Element)node;
					Element gParent = findParentG(hotspot);
					hotspot.getParentNode().removeChild(hotspot);
					
					Element lastG = findLastMatchingG(gParent,nodeGList);

					lastG.appendChild(hotspot);
				}
			}

			//HS normaux au dernier plan
			//Décommenter pour que les HS soit à l'arrière plan et que le clignotement ne se mette pas par dessus
			/*{
				String expressionfG = "//svg/g/g[2]";
				Element firstg = (Element) xPath.compile(expressionfG).evaluate(doc, XPathConstants.NODE);

				String expressionHS = "//*[@class='hotspot' and not(starts-with(@apsname,'SDET'))]";
				NodeList nodeListHS = (NodeList) xPath.compile(expressionHS).evaluate(doc, XPathConstants.NODESET);

				for (int n = 1; n < nodeListHS.getLength() ; n++) {
					Node node = nodeListHS.item(n);
					Element hotspot = (Element)node;
					hotspot.getParentNode().removeChild(hotspot);

					firstg.appendChild(hotspot);
				}
			}*/


			
			
			DOMSource source = new DOMSource(doc);
			FileOutputStream fos = new FileOutputStream(destination);
			Writer out = new OutputStreamWriter(fos, "UTF-8");
			StreamResult result = new StreamResult(out);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.transform(source, result);
		


		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage(),e);
		} catch (SAXException e) {
			logger.error(e.getMessage(),e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		} catch (TransformerConfigurationException e) {
			logger.error(e.getMessage(),e);
		} catch (TransformerException e) {
			logger.error(e.getMessage(),e);
		} catch (XPathExpressionException e) {
			logger.error(e.getMessage(),e);
		}


	}
	
	private Element findLastMatchingG(Element gParent, NodeList nodeGList) {
		String parrentTransfo = gParent.getAttribute("transform");
		for (int n = nodeGList.getLength() -1; n >= 0 ; n--) {
			Element g = (Element)nodeGList.item(n);			
			String currentTransfo = g.getAttribute("transform");
			if(parrentTransfo.equalsIgnoreCase(currentTransfo)) {
				return g;
			}
		}
		return gParent;
	}

	private Element findParentG(Node n) {
		Node parent = n.getParentNode();
		if("g".equalsIgnoreCase(parent.getNodeName())){
			return (Element)parent;
		}
		return findParentG(parent);
	}
	
	

	/**
	 * all possible transformation (found in ietp 19)
	 * 
	 *   2980 matrix()
	 *      1 matrix() scale()
	 * 151948 matrix() scale()
	 * 109909 matrix() translate() scale()
	 * 
	 * 
	 * @param sourceF
	 * @param destination
	 */
	public void applyTransformation(File sourceF, File destination) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			dbf.setValidating(false);
			dbf.setNamespaceAware(true);
			dbf.setFeature("http://xml.org/sax/features/namespaces", false);
			dbf.setFeature("http://xml.org/sax/features/validation", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(sourceF);

			XPath xPath = XPathFactory.newInstance().newXPath();


			//
			{
				String expressionG = "//g[@transform]";
				NodeList nodeList = (NodeList) xPath.compile(expressionG).evaluate(doc, XPathConstants.NODESET);

				for (int n = 0; n < nodeList.getLength() ; n++) {
					Node node = nodeList.item(n);
					Element g = (Element)node;
					Matrix[] matrixs = parseTransform(g.getAttribute("transform"));
					Matrix matrix =calculateMatrix(matrixs);					
					g.setAttribute("transform", "");
					
					updateStyle(g,matrix.m00);
					
					
					//NodeList childs = g.getChildNodes();
					
					NodeList childs = g.getElementsByTagName("*");
					
					for (int m = 0; m < childs.getLength() ; m++) {
						Node childNode = childs.item(m);						
						Element child = (Element)childNode;
						
						if(child.getAttribute("transform") !=null && !child.getAttribute("transform").isEmpty()) {
							Matrix[] subMatrixs = parseTransform(child.getAttribute("transform"));
							matrix =calculateMatrix(matrix,subMatrixs);							
							child.setAttribute("transform", "");
							
						}
						
						if("path".equalsIgnoreCase(child.getNodeName())) {
							recalculatePath(child,matrix);
							updateStyle(child,matrix.m00);
						}else if("circle".equalsIgnoreCase(child.getNodeName())) {
							recalculateCircle(child,matrix);
							updateStyle(child,matrix.m00);
						}else if("text".equalsIgnoreCase(child.getNodeName())) {
							recalculateText(child,matrix);
							updateStyle(child,matrix.m00);
						}else if("rect".equalsIgnoreCase(child.getNodeName())) {
							recalculateRect(child,matrix);
							updateStyle(child,matrix.m00);
						}else{
							System.out.println("ERROR SHAPE NON GERE" + child.getNodeName());
						}
					}
					
					
				}
			}
			
			//transformation des images
			{
				String expressionG = "//image[@transform]";
				NodeList nodeList = (NodeList) xPath.compile(expressionG).evaluate(doc, XPathConstants.NODESET);

				for (int n = 0; n < nodeList.getLength() ; n++) {
					Node node = nodeList.item(n);
					Element image = (Element)node;
					Matrix[] matrixs = parseTransform(image.getAttribute("transform"));
					Matrix matrix =calculateMatrix(matrixs);					
					image.setAttribute("transform", "");
					
					updateStyle(image,matrix.m00);
					
					recalculateImage(image,matrix);
					
					
					
				}
			}

			


			DOMSource source = new DOMSource(doc);
			FileOutputStream fos = new FileOutputStream(destination);
			Writer out = new OutputStreamWriter(fos, "UTF-8");
			StreamResult result = new StreamResult(out);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.transform(source, result);



		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	private void recalculateCircle(Element child, Matrix matrix) {
		float rayon = Float.valueOf(child.getAttribute("r"));
		String cx = child.getAttribute("cx");
		String cy = child.getAttribute("cy");
		
		Point center = new Point(Float.valueOf(cx),Float.valueOf(cy));
		
		Point newcenter = matrix.apply(center);
		
		child.setAttribute("cx", dfCoo.format(newcenter.x));
		child.setAttribute("cy", dfCoo.format(newcenter.y));
		child.setAttribute("r", dfCoo.format(rayon * matrix.m00));
		
	}
	
	private void recalculateText(Element child, Matrix matrix) {
		String x = child.getAttribute("x");
		String y = child.getAttribute("y");
		
		Point positon = new Point(Float.valueOf(x),Float.valueOf(y));
		
		Point newpositon = matrix.apply(positon);
		
		child.setAttribute("x", dfCoo.format(newpositon.x));
		child.setAttribute("y", dfCoo.format(newpositon.y));
		
		
	}
	
	private void recalculateRect(Element child, Matrix matrix) {
		String x = child.getAttribute("x");
		String y = child.getAttribute("y");
		
		float width = Float.valueOf(child.getAttribute("width"));
		float height = Float.valueOf(child.getAttribute("height"));
		
		Point positon = new Point(Float.valueOf(x),Float.valueOf(y));
		
		Point newpositon = matrix.apply(positon);
		
		child.setAttribute("x", dfCoo.format(newpositon.x));
		child.setAttribute("y", dfCoo.format(newpositon.y));
		
		
		child.setAttribute("width", dfCoo.format(width * matrix.m00));
		child.setAttribute("height", dfCoo.format(height * matrix.m00));
		
		
	}
	
	private void recalculateImage(Element image, Matrix matrix) {
		String x = image.getAttribute("x");
		String y = image.getAttribute("y");
		
		float width = Float.valueOf(image.getAttribute("width"));
		float height = Float.valueOf(image.getAttribute("height"));
		
		Point positon = new Point(Float.valueOf(x),Float.valueOf(y));
		
		Point newpositon = matrix.apply(positon);
		
		image.setAttribute("x", String.valueOf(Math.round(newpositon.x)));
		image.setAttribute("y", String.valueOf(Math.round(newpositon.y)));
		
		
		image.setAttribute("width" , String.valueOf(Math.round(Math.abs(width * matrix.m00))));
		image.setAttribute("height", String.valueOf(Math.round(Math.abs(height * matrix.m11))));
		
		
	}
	

	private void recalculatePath(Element child, Matrix matrix) {
		String d = child.getAttribute("d").trim();
		

		StringBuffer newD = new StringBuffer();

		/*if("M-68.40625".equals(coos[0])){
			System.out.println("found");
		}*/


		final String regex = "([MLQTCSAZVH])([^MLQTCSAZVH]*)";		

		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(d);

		boolean first = true;
		while (matcher.find()) {
			//System.out.println("Full match: " + matcher.group(0));
			if(!first) {
				newD.append(" ");
			}else {
				first = false;
			}
			String letter = matcher.group(1);
			String coordinates = matcher.group(2);
			newD.append(letter);
			if("A".equals(letter)) {
				String[] coos = coordinates.split(" ");
				float rx = Float.parseFloat(coos[0]);
				float ry = Float.parseFloat(coos[1]);
				float xaxis = Float.parseFloat(coos[2]);
				int laf = Integer.parseInt(coos[3]);
				int sf = Integer.parseInt(coos[4]);
				float x = Float.parseFloat(coos[5]);
				float y = Float.parseFloat(coos[6]);				
				Point rxy = new Point(rx,ry);
				Point xy = new Point(x,y);
				
				rxy = matrix.scale(rxy);
				xy = matrix.apply(xy);
				newD.append(rxy.toString() + " ");
				newD.append(xaxis + " ");
				newD.append(laf + " ");				
				//newD.append(sf + " ");
				newD.append("1 ");//il faut inverser tout les arc
				newD.append(xy.toString());
				
			}else if("Z".equals(letter)) {
				//rien
			}else {
				String[] coos = coordinates.split(" ");
				for(int i=0;i<coos.length;i=i+2) {
					if(i>0) {
						newD.append(" ");
					}
					float x = Float.parseFloat(coos[i]);
					float y = Float.parseFloat(coos[i+1]);
					Point xy = new Point(x,y);
					xy = matrix.apply(xy);
					newD.append(xy.toString());
					
				}
			}
				
		}

		/*System.out.println(d);
		System.out.println(newD);*/

		child.setAttribute("d", newD.toString());

	}


	private void recalculatePathV2(Element child, Matrix matrix) {
		String d = child.getAttribute("d").trim();
		String[] coos = d.split(" ");
		
		StringBuffer newD = new StringBuffer();
		
		if("M-68.40625".equals(coos[0])){
			System.out.println("found");
		}
  
		
		for(int i =0;i<coos.length;i++) {
			if(i>0) {
				newD.append(" ");
			}
			if(i + 1 < coos.length ) {
				String x = coos[i];
				String y = coos[i+1];
				
				ShapePoint spx = getShapePoint(x,y);
				
				spx = matrix.apply(spx);
				newD.append(spx.toString());
				
				i++;
			}else {
				newD.append(coos[i]);
			}
		}
		
		/*System.out.println(d);
		System.out.println(newD);*/
		
		child.setAttribute("d", newD.toString());
		
	}
	
	
	private ShapePoint getShapePoint(String valueX,String valueY) {		
		final String regex = "([a-zA-Z]{0,2})?([+-]?\\d*\\.?\\d+)";
        final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(valueX);
		
		final String regexY = "([+-]?\\d*\\.?\\d+)";
        final Pattern patternY = Pattern.compile(regexY);
		final Matcher matcherY = patternY.matcher(valueY);
		
		ShapePoint sp = null;
		
		if (matcher.find() && matcherY.find()) {
            String letter = matcher.group(1);
            float floatvalueX = Float.parseFloat(matcher.group(2));
            float floatvalueY = Float.parseFloat(matcherY.group(1));
            sp = new ShapePoint(letter, floatvalueX,floatvalueY);
            return sp;
        }else {
        	System.out.println("ERR : non match "+ valueX );
        }
		return new ShapePoint(null, 0,0);
	}
	

	private Matrix[] parseTransform(String transform) {
		String[] transforms = transform.split(" ");

		final String regex = "(matrix|scale|translate|rotate)(\\(.*\\))";
		
		Matrix matrix = null;
		Scale scale = null;
		Translate translate =null;
		Rotate rotate =null;
		
		Matrix[] matrixs = new Matrix[transforms.length];

		for(int i =0;i<transforms.length;i++) {
			final Pattern pattern = Pattern.compile(regex);
			final Matcher matcher = pattern.matcher(transforms[i]);

			while (matcher.find()) {

				String type = matcher.group(1);
				String value = matcher.group(2);
				//System.out.println("Full match: " + matcher.group(0));

				if("matrix".contentEquals(type)) {
					matrix = parseMatrix(value);
					matrixs[i]=matrix;
				}
				if("scale".contentEquals(type)) {
					scale = parseScale(value);
					matrixs[i]=scale;
				}
				if("translate".contentEquals(type)) {
					translate = parseTranslate(value);
					matrixs[i]=translate;
				}
				
				if("rotate".contentEquals(type)) {
					rotate = parseRotate(value);
					matrixs[i]=rotate;
				}
			}
		}
		
		
		return matrixs; 

	}

	public Matrix parseMatrix(String value) {
		final String regex = "\\(([+-]?\\d*\\.?\\d+),([+-]?\\d*\\.?\\d+),([+-]?\\d*\\.?\\d+),([+-]?\\d*\\.?\\d+),([+-]?\\d*\\.?\\d+),([+-]?\\d*\\.?\\d+)\\)";


		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(value);

		try {
			while (matcher.find()) {
				//System.out.println("Full match: " + matcher.group(0));

				float m00 = Float.valueOf(matcher.group(1));
				float m10 = Float.valueOf(matcher.group(2));
				float m01 = Float.valueOf(matcher.group(3));
				float m11 = Float.valueOf(matcher.group(4));
				float m02 = Float.valueOf(matcher.group(5));
				float m12 = Float.valueOf(matcher.group(6));

				Matrix matrix =new Matrix(m00, m10, m01, m11, m02, m12);            
				return matrix;
			}
			
			Matrix matrix =new Matrix(1, 0, 0, 1, 0, 0);
			return matrix;
		}catch (Exception e) {
			Matrix matrix =new Matrix(1, 0, 0, 1, 0, 0);
			return matrix;
		}

	}

	public Scale parseScale(String value) {
		final String regex = "\\(([+-]?\\d*\\.?\\d+),([+-]?\\d*\\.?\\d+)\\)";


		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(value);

		try {
			while (matcher.find()) {
				//System.out.println("Full match: " + matcher.group(0));

				float sx = Float.valueOf(matcher.group(1));
				float sy = Float.valueOf(matcher.group(2));

				Scale scale =new Scale(sx, sy);            
				return scale;
			}
			
			Scale scale =new Scale(1, 1);            
			return scale;
		}catch (Exception e) {
			Scale sacle =new Scale(1, 1);             
			return sacle;
		}
	}

	public Translate parseTranslate(String value) {
		final String regex = "\\(([+-]?\\d*\\.?\\d+),([+-]?\\d*\\.?\\d+)\\)";


		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(value);

		try {
			while (matcher.find()) {
				//System.out.println("Full match: " + matcher.group(0));

				float tx = Float.valueOf(matcher.group(1));
				float ty = Float.valueOf(matcher.group(2));

				Translate translate =new Translate(tx, ty);            
				return translate;
			}
			
			Translate sacle =new Translate(0, 0);            
			return sacle;
		}catch (Exception e) {
			Translate sacle =new Translate(0, 0);             
			return sacle;
		}
	}
	
	public Rotate parseRotate(String value) {
		final String regex = "\\(([+-]?\\d*\\.?\\d+)\\)";


		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(value);
		
		try {
			while (matcher.find()) {
				//System.out.println("Full match: " + matcher.group(0));

				float a = Float.valueOf(matcher.group(1));
				

				Rotate angle =new Rotate(a);            
				return angle;
			}
			
			Rotate angle =new Rotate(0);            
			return angle;
		}catch (Exception e) {
			Rotate angle =new Rotate(0);            
			return angle;
		}
		
	}
	
	private void updateStyle(Element g, float m00) {
		String style = g.getAttribute("style");		
		if(style!=null && style.length() > 0) {
			//System.out.println("AVANT = " + style);
			
			style = parseStrokeWidth(style, m00);
			style = parseFontSize(style, m00);
			//System.out.println("Pares = " + style);
			g.setAttribute("style", style);
		}
		
	}
	
	public String parseFontSize(String style,float scalling) {
		return parseCssProperty(style, scalling,"font-size");
	}

	public String parseStrokeWidth(String style,float scalling) {
		return parseCssProperty(style, scalling,"stroke-width");
	}
	
	public String parseCssProperty(String style,float scalling,String cssProperty) {
		final String regex = cssProperty+":(\\d*\\.?\\d+)";        
        
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(style);
        
        if (!matcher.find()) return style; // pattern not met, may also throw an exception here
        
        float strokeWith = Float.valueOf(matcher.group(1));
        
        strokeWith = strokeWith * scalling;
        
        strokeWith = Math.max(strokeWith, 0.01f);
		
		return new StringBuilder(style)
				.replace(matcher.start(1), matcher.end(1), dfCoo.format(strokeWith))
				.toString();
	}
	
	

	
	private Matrix calculateMatrix(Matrix[] matrixs) {
		Matrix base = matrixs[0];
		if(matrixs.length==1) {
			return base;
		}else {
			for(int i=1;i<matrixs.length;i++) {
				base = base.multiply(matrixs[i]);
			}
			return base;
		}
			
	}
	
	private Matrix calculateMatrix(Matrix matrix, Matrix[] subMatrixs) {
		Matrix[] matrixsInc= new Matrix[subMatrixs.length+1];
		matrixsInc[0]=matrix;
		for( int i=0;i<subMatrixs.length;i++) {
			matrixsInc[i+1]=subMatrixs[i];
		}
		return calculateMatrix(matrixsInc);
	}

	


	/*
	 [ x']   [  m00  m01  m02  ] [ x ]   [ m00x + m01y + m02 ]
	 [ y'] = [  m10  m11  m12  ] [ y ] = [ m10x + m11y + m12 ]
	 [ 1 ]   [   0    0    1   ] [ 1 ]   [         1         ]
	 */
	class Matrix{
		private float m00;
		private float m10;
		private float m01;
		private float m11;
		private float m02;
		private float m12;


		public Matrix(float m00, float m10, float m01, float m11, float m02, float m12){
			this.m00=m00;
			this.m10=m10;
			this.m01=m01;
			this.m11=m11;
			this.m02=m02;
			this.m12=m12;

		}

		public Matrix multiply(Matrix subMatrix) {
			Matrix m = new Matrix(m00*subMatrix.m00 + m01*subMatrix.m10,
					m10*subMatrix.m00 + m11*subMatrix.m10,
					m00*subMatrix.m01 + m01*subMatrix.m11,
					m10*subMatrix.m01 + m11*subMatrix.m11,
					m00*subMatrix.m02 + m01*subMatrix.m12 + m02,
					m10*subMatrix.m02 + m11*subMatrix.m12 + m12
					);
			return m;
		}

		/*public void apply(Scale s) {
			this.m00 = m00 * s.sx;
			this.m11 = m11 * s.sy;
		}

		public void apply(Translate t) {
			this.m02 = m02 + t.tx;
			this.m12 = m12 + t.ty;
		}*/

		public ShapePoint apply(ShapePoint p) {
			float nx = m00 * p.x + m01 * p.y + m02;
			float ny = m10 * p.x + m11 * p.y + m12;

			ShapePoint np = new ShapePoint(p.letter,nx,ny);
			return np;
		}
		
		public Point apply(Point p) {
			float nx = m00 * p.x + m01 * p.y + m02;
			float ny = m10 * p.x + m11 * p.y + m12;

			Point np = new Point(nx,ny);
			return np;
		}
		
		public Point scale(Point p) {
			float nx = Math.abs(m00 * p.x + m01 * p.y );
			float ny = Math.abs(m10 * p.x + m11 * p.y );

			Point np = new Point(nx,ny);
			return np;
		}

		@Override
		public String toString() {
			String l1 =  "[ " + m00 + " " + m01 + " " + m02 +" ]\n";
			String l2 =  "[ " + m10 + " " + m11 + " " + m12 +" ]\n";
			String l3 =  "[ 0 0 1 ]";
			
			return l1 + l2 + l3;
					
		}
		
		
	}

	class Scale extends Matrix{

		public Scale(float sx,float sy) {
			super(sx,0,0,sy,0,0);
			
		}

	}
	
	class Rotate extends Matrix{

		public Rotate(float angle) {
			super((float)Math.cos(angle), (float)Math.sin(angle), (float) (-1*Math.sin(angle)), (float) Math.cos(angle),0,0);
			
		}

	}

	class Translate extends Matrix{
		private float tx;
		private float ty;

		public Translate(float tx,float ty) {
			super(1,0,0,1,tx,ty);
		}

	}

	class Point{
		protected float x;
		protected float y;

		public Point(float x,float y) {
			this.x=x;
			this.y=y;
		}
		
		public String toString() {			
			return dfCoo.format(x) + " " +  dfCoo.format(y);
		}

	}
	
	
	class ShapePoint extends Point{
		private String letter;
		

		public ShapePoint(String letter,float x,float y) {
			super(x,y);
			this.letter=letter;
		}
		
		public String toString() {
			if(letter!=null) {
				return letter + dfCoo.format(x) + " " +  dfCoo.format(y);
			}else {
				return dfCoo.format(x) + " " +  dfCoo.format(y);
			}
		}

	}
	
	
	public static void main(String[] args) {
		String sdetsvg = "C:/Users/jpprade/Documents/vrac/ICN-JP-A-ZZH203-U-F0057-52012-A-04-1.svg";
		String sdetsvgDest = "C:/Users/jpprade/Documents/vrac/ICN-JP-A-ZZH203-U-F0057-52012-A-04-1-opti.svg";
		String sdetsvgDest2 = "C:/Users/jpprade/Documents/vrac/ICN-JP-A-ZZH203-U-F0057-52012-A-04-1-opti2.svg";

		String sdetsvgDestMillion = "C:/Users/jpprade/Documents/vrac/ICN-02-B-725000-R-8338B-31185-A-02-1.svg";
		
		String sdetsvgSrcApply = "E:/x/JP-F0210-FOSJP-2021-24/illustrations/sources/ICN-02-A-050000-R-D3309-20016-A-03-1.svg";
		//String sdetsvgSrcApply = "E:/x/JP-F0210-FOSJP-2021-24/illustrations/sources/ICN-02-A-121220-R-F0228-00188-A-01-1.svg";
		//String sdetsvgSrcApply = "C:/Users/jpprade/Documents/vrac/ICN-02-B-725000-R-8338B-31185-A-02-1.svg";
		//String sdetsvgSrcApply = "C:/Users/jpprade/Documents/vrac/ICN-02-B-725000-R-8338B-31185-A-02-1-mini.svg";
		
		String sdetsvgDestApply = "C:/Users/jpprade/Documents/vrac/ICN-02-A-050000-R-D3309-20016-A-03-1-notransform.svg";
		//String sdetsvgDestApply = "C:/Users/jpprade/Documents/vrac/ICN-02-B-725000-R-8338B-31185-A-02-1-notransform.svg";


		SVGUtils hsm= new SVGUtils();


		//hsm.moveHotspotToRightLayer(new File(sdetsvg), new File(sdetsvgDest2));
		
		
		//Matrix[] matrixs = hsm.parseTransform("matrix(3.7791,0,0,3.7791,613.066,277.5855) scale(0.1764,0.1127)");
		
		
		hsm.applyTransformation(new File(sdetsvgSrcApply), new File(sdetsvgDestApply));
		
		//System.out.println(matrix);

	}

}
