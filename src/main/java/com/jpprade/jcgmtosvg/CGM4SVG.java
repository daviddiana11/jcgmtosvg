package com.jpprade.jcgmtosvg;


import java.io.File;
import java.io.IOException;


import net.sf.jcgm.core.ApplicationStructureAttribute;
import net.sf.jcgm.core.BeginApplicationStructure;
import net.sf.jcgm.core.CGM;
import net.sf.jcgm.core.CGMDisplay;
import net.sf.jcgm.core.Command;

public class CGM4SVG extends CGM {
	
	SVGPainter painter = null;
	
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
				}else {
					c.paint(d);
				}
			
		}
	}


}
