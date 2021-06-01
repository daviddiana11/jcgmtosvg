package com.jpprade.jcgmtosvg;


import java.io.File;
import java.io.IOException;


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
import net.sf.jcgm.core.PolyBezier;

public class CGM4SVG extends CGM {
	
	SVGPainter painter = null;
	
	private BeginApplicationStructure currentAPS = null;
	
	private BeginFigure currentFigure =null;
	 
	private FillColour currentFC = null;
	
	private EdgeColour currentEC = null;
	
	private EdgeWidth currentEW = null;
	
	
	
	
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
				}else if(c instanceof BeginFigure) {
					c.paint(d);
					this.currentFigure = (BeginFigure)c;				
				}else if(c instanceof EndApplicationStructure) {
					this.currentAPS = null;
					this.currentFC = null;
					this.currentEC = null;
					this.currentEW = null;
				}else if(c instanceof EndFigure) {
					this.currentFigure = null;
				}else {
					if(c instanceof PolyBezier) {
						PolyBezierV2 c2 = new PolyBezierV2((PolyBezier)c);
						c2.paint(d,currentFigure,currentFC,currentEC,currentEW);
					}else if(c instanceof FillColour) {
						if(currentAPS!=null) {
							this.currentFC = (FillColour) c;
						}
						c.paint(d);
					}else if(c instanceof EdgeColour) {
						if(currentAPS!=null) {
							this.currentEC = (EdgeColour) c;
						}
						c.paint(d);
					}else if(c instanceof EdgeWidth) {
						if(currentAPS!=null) {
							this.currentEW = (EdgeWidth) c;
						}
						c.paint(d);
					}else {
						c.paint(d);
					}
					
				}
			
		}
	}


}
