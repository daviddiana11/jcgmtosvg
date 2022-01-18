package com.jpprade.jcgmtosvg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.util.DocumentFactory;
import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGSVGElement;
import org.xml.sax.SAXException;

public class SVGUtils {

	
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
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(sourceF);

			//doc.getDocumentElement().normalize();

			System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
			System.out.println("------");

			

			XPath xPath = XPathFactory.newInstance().newXPath();
			
			
			//HS SDET au premier plan
			{
				String expressionG = "//svg/g/g[position()=last()]";
				Element lastg = (Element) xPath.compile(expressionG).evaluate(doc, XPathConstants.NODE);
				
				String expression = "//*[@class='hotspot' and starts-with(@apsname,'SDET')]";
				NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
	
				for (int n = 1; n < nodeList.getLength() ; n++) {
					Node node = nodeList.item(n);
					Element hotspot = (Element)node;
					hotspot.getParentNode().removeChild(hotspot);
					
					lastg.appendChild(hotspot);
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
			FileWriter writer = new FileWriter(destination);
			StreamResult result = new StreamResult(writer);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
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


	public static void main(String[] args) {
		String sdetsvg = "C:/Users/jpprade/Documents/vrac/ICN-JP-A-ZZH203-U-F0057-52012-A-04-1.svg";
		String sdetsvgDest = "C:/Users/jpprade/Documents/vrac/ICN-JP-A-ZZH203-U-F0057-52012-A-04-1-opti.svg";
		String sdetsvgDest2 = "C:/Users/jpprade/Documents/vrac/ICN-JP-A-ZZH203-U-F0057-52012-A-04-1-opti2.svg";

		SVGUtils hsm= new SVGUtils();

		


		hsm.moveHotspotToRightLayer(new File(sdetsvg), new File(sdetsvgDest2));

	}

}
