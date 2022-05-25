package com.jpprade.jcgmtosvg;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import com.jpprade.jcgmtosvg.commands.PolyBezierV2;

import net.sf.jcgm.core.ApplicationStructureAttribute;
import net.sf.jcgm.core.BeginApplicationStructure;
import net.sf.jcgm.core.BeginFigure;
import net.sf.jcgm.core.CGM;
import net.sf.jcgm.core.CGMDisplay;
import net.sf.jcgm.core.Command;
import net.sf.jcgm.core.EdgeColour;
import net.sf.jcgm.core.EdgeWidth;
import net.sf.jcgm.core.EndApplicationStructure;
import net.sf.jcgm.core.EndFigure;
import net.sf.jcgm.core.FillColour;
import net.sf.jcgm.core.LineColour;
import net.sf.jcgm.core.LineWidth;
import net.sf.jcgm.core.PolyBezier;
import net.sf.jcgm.core.RestrictedText;

public class CGM4SVG extends CGM {
	
	SVGPainter painter = null;
	
	private BeginApplicationStructure currentAPS = null;
	
	private Stack<BeginApplicationStructure> basStack = new Stack<BeginApplicationStructure>();
	
	private HashMap<BeginApplicationStructure,PaintHolder> mapping = new HashMap<>();
	
	private BeginFigure currentFigure =null;
	 
	private FillColour currentFC = null;
	
	private EdgeColour currentEC = null;
	
	private EdgeWidth currentEW = null;
	
	
	private LineColour currentLC = null;
	
	private LineWidth currentLW = null;	
	
	private Command lastCommand = null;
	
	private boolean hotspotDrawn = false;
	
	
	
	private ConcurrentHashMap<BeginFigure, List<PolyBezierV2>> figurePolyBezier = new ConcurrentHashMap<>();
	
	public CGM4SVG(File cgmFile,SVGPainter painter) throws IOException {
		super(cgmFile);
		this.painter= painter;
	}

	@Override
	public void paint(CGMDisplay d) {		
		for (Command c : getCommands()) {
				if(c instanceof ApplicationStructureAttribute) {
					
					BeginApplicationStructure top = basStack.peek();
					
					mapping.get(top).addAPS((ApplicationStructureAttribute)c);
					
					ApplicationStructureAttribute regionaps = mapping.get(top).getRegionAPS();
					if(regionaps!=null) {					
						painter.paint(regionaps,d,mapping.get(top),getSize());
						hotspotDrawn=true;
					}else {
						ApplicationStructureAttribute vcaps = mapping.get(top).getVCAPS();
						if(vcaps!=null) {					
							painter.paint(vcaps,d,mapping.get(top),getSize());
							hotspotDrawn=true;
						}
					}
				}else if(c instanceof BeginApplicationStructure) {// structure parente					
					this.currentAPS = (BeginApplicationStructure)c;
					basStack.add(currentAPS);
					PaintHolder ph = new PaintHolder();
					ph.setApsid(this.currentAPS.getIdentifier());
					mapping.put(currentAPS, ph);
					
					painter.paint((BeginApplicationStructure)c,d,ph);
					if(currentAPS.getIdentifier()!=null && currentAPS.getIdentifier().startsWith("HOT")) {
						hotspotDrawn=false;//ICN-JP-A-130100-M-C0418-04982-A-04-1.cgm
					}else {
						hotspotDrawn=true;//on n'est pas dans une couche HS
					}
					
				}else if(c instanceof BeginFigure) {
					c.paint(d);
					this.currentFigure = (BeginFigure)c;
					figurePolyBezier.put(currentFigure, new ArrayList<>());
				}else if(c instanceof EndApplicationStructure) {
					basStack.pop();
					
					if(basStack.empty()) {					
						this.currentAPS = null;
						this.currentFC = null;
						this.currentEC = null;
						this.currentEW = null;
						this.currentLC = null;
						this.currentLW = null;
					}else {
						BeginApplicationStructure top = basStack.peek();
						this.currentAPS = top;
						this.currentFC = mapping.get(top).getCurrentFC();
						this.currentEC = mapping.get(top).getCurrentEC();
						this.currentEW = mapping.get(top).getCurrentEW();
						this.currentLC = mapping.get(top).getCurrentLC();
						this.currentLW = mapping.get(top).getCurrentLW();
					}
				}else if(c instanceof EndFigure) {
					List<PolyBezierV2> toPaint = figurePolyBezier.get(currentFigure);
					
					if(!toPaint.isEmpty()) {
						PolyBezierV2 c2 = mergePB(toPaint);					
						
						String apsname = null;
						String apsid = null;
						if(!basStack.isEmpty()) {
							
							BeginApplicationStructure top = basStack.peek();						
							if(hotspotDrawn==false) {
								apsname = mapping.get(top).getName();
								apsid = top.getIdentifier();
							}							
							
							c2.paint(d,currentFigure,
								mapping.get(top).getCurrentFC(),
								mapping.get(top).getCurrentEC(),
								mapping.get(top).getCurrentEW(),
								mapping.get(top).getCurrentLC(),
								mapping.get(top).getCurrentLW(),apsid,apsname);
							
						}else {
							c2.paint(d,currentFigure);
							
						}
					}
					this.currentFigure = null;
				}else {
					if(c instanceof PolyBezier) {
						PolyBezierV2 c2 = new PolyBezierV2((PolyBezier)c);
						String apsname = null;
						String apsid = null;
						if(!basStack.isEmpty()) {
							
							BeginApplicationStructure top = basStack.peek();
							if(hotspotDrawn==false) {
								apsname = mapping.get(top).getName();
								apsid = top.getIdentifier();
							}
							
							if(currentFigure==null) {
								
								c2.paint(d,currentFigure,
									mapping.get(top).getCurrentFC(),
									mapping.get(top).getCurrentEC(),
									mapping.get(top).getCurrentEW(),
									mapping.get(top).getCurrentLC(),
									mapping.get(top).getCurrentLW(),apsid,apsname);
							}else {
								figurePolyBezier.get(currentFigure).add(c2);//tous les polybeziers seront mergé en 1 seule shape
							}
						}else {
							if(currentFigure==null) {								
								c2.paint(d,currentFigure);
							}else {
								figurePolyBezier.get(currentFigure).add(c2);
							}
						}
						
						
						
					}else if(c instanceof FillColour) {
						if(!basStack.isEmpty()) {
							BeginApplicationStructure top = basStack.peek();						
							mapping.get(top).setCurrentFC((FillColour)c);
						}
						this.currentFC = (FillColour) c;
						this.lastCommand=c;
						c.paint(d);
					}else if(c instanceof EdgeColour) {
						if(!basStack.isEmpty()) {
							BeginApplicationStructure top = basStack.peek();						
							mapping.get(top).setCurrentEC((EdgeColour)c);
						}
						this.currentEC = (EdgeColour) c;
						this.lastCommand=c;
						c.paint(d);
					}else if(c instanceof EdgeWidth) {
						if(!basStack.isEmpty()) {
							BeginApplicationStructure top = basStack.peek();						
							mapping.get(top).setCurrentEW((EdgeWidth)c);
						}
						this.currentEW = (EdgeWidth) c;
						this.lastCommand=c;
						c.paint(d);
					}else if(c instanceof LineColour) {
						if(!basStack.isEmpty()) {
							BeginApplicationStructure top = basStack.peek();						
							mapping.get(top).setCurrentLC((LineColour)c);
						}
						this.currentLC = (LineColour) c;
						this.lastCommand=c;
						c.paint(d);
					}else if(c instanceof LineWidth) {
						if(!basStack.isEmpty()) {
							BeginApplicationStructure top = basStack.peek();
							mapping.get(top).setCurrentLW((LineWidth)c);
						}
						this.currentLW = (LineWidth) c;
						this.lastCommand=c;
						c.paint(d);
					}else {
						//if(c instanceof RestrictedText) {
							c.paint(d);
						//}
						
					}
					
				}
			
		}
	}

	private PolyBezierV2 mergePB(List<PolyBezierV2> tomerge) {
		PolyBezierV2 ret = tomerge.get(0);
		if(tomerge.size() ==1 ) {
			return ret;
		}
		for(int i = 1;i<tomerge.size();i++) {
			ret.mergeShape(tomerge.get(i));
		}
		return ret;
	}


}
