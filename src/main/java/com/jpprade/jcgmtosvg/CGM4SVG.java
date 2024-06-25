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


public class CGM4SVG extends CGM {
	
	SVGPainter painter;
	
	private final Stack<BeginApplicationStructure> basStack = new Stack<>();
	
	private final HashMap<BeginApplicationStructure, PaintHolder> mapping = new HashMap<>();
	
	private BeginFigure currentFigure = null;
	
	private final ConcurrentHashMap<BeginFigure, List<PolyBezierV2>> figurePolyBezier = new ConcurrentHashMap<>();
	
	public CGM4SVG(File cgmFile, SVGPainter painter) throws IOException {
		super(cgmFile);
		this.painter = painter;
	}
	
	@Override
	public void paint(CGMDisplay d) {
		for (Command c : getCommands()) {
			BeginApplicationStructure currentAPS;
			if (c instanceof ApplicationStructureAttribute asa) {
				
				BeginApplicationStructure top = this.basStack.peek();
				
				this.mapping.get(top).addAPS(asa);
				
				ApplicationStructureAttribute regionaps = this.mapping.get(top).getRegionAPS();
				if (regionaps != null) {
					this.painter.paint(regionaps, d, this.mapping.get(top), getSize());
				} else {
					ApplicationStructureAttribute vcaps = this.mapping.get(top).getVCAPS();
					if (vcaps != null) {
						this.painter.paint(vcaps, d, this.mapping.get(top), getSize());
					}
				}
			} else if (c instanceof BeginApplicationStructure bas) {
				currentAPS = bas;
				this.basStack.add(currentAPS);
				PaintHolder ph = new PaintHolder();
				ph.setApsid(currentAPS.getIdentifier());
				this.mapping.put(currentAPS, ph);
				
			} else if (c instanceof BeginFigure bf) {
				c.paint(d);
				this.currentFigure = bf;
				this.figurePolyBezier.put(this.currentFigure, new ArrayList<>());
			} else if (c instanceof EndApplicationStructure) {
				this.basStack.pop();
				if (!this.basStack.empty()) {
					this.basStack.peek();
				}
			} else if (c instanceof EndFigure) {
				List<PolyBezierV2> toPaint = this.figurePolyBezier.get(this.currentFigure);
				
				if (!toPaint.isEmpty()) {
					PolyBezierV2 c2 = mergePB(toPaint);
					
					if (!this.basStack.isEmpty()) {
						BeginApplicationStructure top = this.basStack.peek();
						
						c2.paint(d, this.currentFigure,
								this.mapping.get(top).getCurrentFC(),
								this.mapping.get(top).getCurrentEC(),
								this.mapping.get(top).getCurrentEW());
						
					} else {
						c2.paint(d, this.currentFigure);
						
					}
				}
				this.currentFigure = null;
			} else {
				if (c instanceof PolyBezier pb) {
					PolyBezierV2 c2 = new PolyBezierV2(pb);
					if (!this.basStack.isEmpty()) {
						BeginApplicationStructure top = this.basStack.peek();
						if (this.currentFigure == null) {
							c2.paint(d, null,
									this.mapping.get(top).getCurrentFC(),
									this.mapping.get(top).getCurrentEC(),
									this.mapping.get(top).getCurrentEW());
						} else {
							this.figurePolyBezier.get(this.currentFigure).add(c2); // all polybezier will be merged into a single shape
						}
					} else {
						if (this.currentFigure == null) {
							c2.paint(d, null);
						} else {
							this.figurePolyBezier.get(this.currentFigure).add(c2);
						}
					}
				} else if (c instanceof FillColour fc) {
					if (!this.basStack.isEmpty()) {
						BeginApplicationStructure top = this.basStack.peek();
						this.mapping.get(top).setCurrentFC(fc);
					}
					c.paint(d);
				} else if (c instanceof EdgeColour ec) {
					if (!this.basStack.isEmpty()) {
						BeginApplicationStructure top = this.basStack.peek();
						this.mapping.get(top).setCurrentEC(ec);
					}
					c.paint(d);
				} else if (c instanceof EdgeWidth ew) {
					if (!this.basStack.isEmpty()) {
						BeginApplicationStructure top = this.basStack.peek();
						this.mapping.get(top).setCurrentEW(ew);
					}
					c.paint(d);
				} else if (c instanceof LineColour lc) {
					if (!this.basStack.isEmpty()) {
						BeginApplicationStructure top = this.basStack.peek();
						this.mapping.get(top).setCurrentLC(lc);
					}
					c.paint(d);
				} else if (c instanceof LineWidth lw) {
					if (!this.basStack.isEmpty()) {
						BeginApplicationStructure top = this.basStack.peek();
						this.mapping.get(top).setCurrentLW(lw);
					}
					c.paint(d);
				} else {
					c.paint(d);
				}
			}
		}
	}
	
	private PolyBezierV2 mergePB(List<PolyBezierV2> tomerge) {
		PolyBezierV2 ret = tomerge.get(0);
		if (tomerge.size() == 1) {
			return ret;
		}
		for (int i = 1; i < tomerge.size(); i++) {
			ret.mergeShape(tomerge.get(i));
		}
		return ret;
	}
	
}
