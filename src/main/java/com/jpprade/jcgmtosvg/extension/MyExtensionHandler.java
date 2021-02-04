package com.jpprade.jcgmtosvg.extension;

import java.awt.Composite;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.image.BufferedImageOp;

import org.apache.batik.svggen.DefaultExtensionHandler;
import org.apache.batik.svggen.SVGCompositeDescriptor;
import org.apache.batik.svggen.SVGFilterDescriptor;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPaintDescriptor;

public class MyExtensionHandler extends DefaultExtensionHandler {

	@Override
	public SVGPaintDescriptor handlePaint(Paint paint, SVGGeneratorContext generatorContext) {
		//System.out.println("Painting " + paint.getClass().getName());
		return super.handlePaint(paint, generatorContext);
	}

	@Override
	public SVGCompositeDescriptor handleComposite(Composite composite, SVGGeneratorContext generatorContext) {
		//System.out.println("Composite " + composite.getClass().getName());
		return super.handleComposite(composite, generatorContext);
	}

	@Override
	public SVGFilterDescriptor handleFilter(BufferedImageOp filter, Rectangle filterRect,
			SVGGeneratorContext generatorContext) {
		//System.out.println("BufferedImageOp " + filter.getClass().getName());
		return super.handleFilter(filter, filterRect, generatorContext);
	}
	
	

}
