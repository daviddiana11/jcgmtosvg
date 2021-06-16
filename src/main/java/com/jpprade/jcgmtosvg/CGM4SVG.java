package com.jpprade.jcgmtosvg;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

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
	
	public CGM4SVG(File cgmFile,SVGPainter painter) throws IOException {
		super(cgmFile);
		this.painter= painter;
	}

	@Override
	public void paint(CGMDisplay d) {		
		for (Command c : getCommands()) {
				if(c instanceof ApplicationStructureAttribute) {
					painter.paint((ApplicationStructureAttribute)c,d);
				}else if(c instanceof BeginApplicationStructure) {
					painter.paint((BeginApplicationStructure)c,d);
					this.currentAPS = (BeginApplicationStructure)c;
					basStack.add(currentAPS);
					PaintHolder ph = new PaintHolder();
					mapping.put(currentAPS, ph);
				}else if(c instanceof BeginFigure) {
					c.paint(d);
					this.currentFigure = (BeginFigure)c;				
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
					this.currentFigure = null;
				}else {
					if(c instanceof PolyBezier) {
						PolyBezierV2 c2 = new PolyBezierV2((PolyBezier)c);
						BeginApplicationStructure top = basStack.peek();
						
						c2.paint(d,currentFigure,
								mapping.get(top).getCurrentFC(),
								mapping.get(top).getCurrentEC(),
								mapping.get(top).getCurrentEW(),
								mapping.get(top).getCurrentLC(),
								mapping.get(top).getCurrentLW());
					}else if(c instanceof FillColour) {
						BeginApplicationStructure top = basStack.peek();
						if(top!=null) {
							mapping.get(top).setCurrentFC((FillColour)c);
						}
						//if(currentAPS!=null) {
							this.currentFC = (FillColour) c;
							this.lastCommand=c;
						//}
						c.paint(d);
					}else if(c instanceof EdgeColour) {
						BeginApplicationStructure top = basStack.peek();
						if(top!=null) {
							mapping.get(top).setCurrentEC((EdgeColour)c);
						}
						//if(currentAPS!=null) {
							this.currentEC = (EdgeColour) c;
							this.lastCommand=c;
						//}
						c.paint(d);
					}else if(c instanceof EdgeWidth) {
						BeginApplicationStructure top = basStack.peek();
						if(top!=null) {
							mapping.get(top).setCurrentEW((EdgeWidth)c);
						}
						//if(currentAPS!=null) {
							this.currentEW = (EdgeWidth) c;
							this.lastCommand=c;
						//}
						c.paint(d);
					}else if(c instanceof LineColour) {
						BeginApplicationStructure top = basStack.peek();
						if(top!=null) {
							mapping.get(top).setCurrentLC((LineColour)c);
						}
						//if(currentAPS!=null) {
							this.currentLC = (LineColour) c;
							this.lastCommand=c;
						//}
						c.paint(d);
					}else if(c instanceof LineWidth) {
						BeginApplicationStructure top = basStack.peek();
						if(top!=null) {
							mapping.get(top).setCurrentLW((LineWidth)c);
						}
						//if(currentAPS!=null) {
							this.currentLW = (LineWidth) c;
							this.lastCommand=c;
						//}
						c.paint(d);
					}else {
						c.paint(d);
					}
					
				}
			
		}
	}


}
