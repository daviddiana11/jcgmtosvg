package com.jpprade.jcgmtosvg;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import net.sf.jcgm.core.CGM;
import net.sf.jcgm.core.CGMDisplay;
import net.sf.jcgm.core.HatchIndex.HatchType;

public class CGMDisplay4SVG extends CGMDisplay{

	public CGMDisplay4SVG(CGM cgm) {
		super(cgm);
	}

	protected void drawHatch(Shape s) {
		// remember the clip and the stroke since we're overwriting them here
		Shape previousClippingArea = this.getG2d().getClip();
		Stroke previousStroke = this.getG2d().getStroke();

		Rectangle2D bounds = s.getBounds2D();
		this.getG2d().setClip(s);

		this.getG2d().setStroke(new BasicStroke(0.2f));

		this.getG2d().setColor(getFillColor());

		final double stepX = 1.41;
		final double stepY = 1.41;
		final double slopeStep = stepX * 1.41; // sqrt(2) since the sloped lines are at 45 degree

		if (HatchType.HORIZONTAL_LINES.equals(getHatchType())) {
			drawHorizontalLines(bounds, stepY);
		}
		else if (HatchType.VERTICAL_LINES.equals(getHatchType())) {
			drawVerticalLines(bounds, stepX);
		}
		else if (HatchType.POSITIVE_SLOPE_LINES.equals(getHatchType())) {
			drawPositiveSlopeLines(bounds, slopeStep);
		}
		else if (HatchType.NEGATIVE_SLOPE_LINES.equals(getHatchType())) {
			drawNegativeSlopeLines(bounds, slopeStep);
		}
		else if (HatchType.HORIZONTAL_VERTICAL_CROSSHATCH.equals(getHatchType())) {
			drawHorizontalLines(bounds, stepY);
			drawVerticalLines(bounds, stepX);
		}
		else if (HatchType.POSITIVE_NEGATIVE_CROSSHATCH.equals(getHatchType())) {
			drawPositiveSlopeLines(bounds, slopeStep);
			drawNegativeSlopeLines(bounds, slopeStep);
		}

		// restore the previous clipping area and stroke
		this.getG2d().setClip(previousClippingArea);
		this.getG2d().setStroke(previousStroke);
	}
}
