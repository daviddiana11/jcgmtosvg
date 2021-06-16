package com.jpprade.jcgmtosvg;

import net.sf.jcgm.core.EdgeColour;
import net.sf.jcgm.core.EdgeWidth;
import net.sf.jcgm.core.FillColour;
import net.sf.jcgm.core.LineColour;
import net.sf.jcgm.core.LineWidth;

public class PaintHolder {
	
	 
		private FillColour currentFC = null;
		
		private EdgeColour currentEC = null;
		
		private EdgeWidth currentEW = null;		
		
		private LineColour currentLC = null;
		
		private LineWidth currentLW = null;

		public FillColour getCurrentFC() {
			return currentFC;
		}

		public void setCurrentFC(FillColour currentFC) {
			this.currentFC = currentFC;
		}

		public EdgeColour getCurrentEC() {
			return currentEC;
		}

		public void setCurrentEC(EdgeColour currentEC) {
			this.currentEC = currentEC;
		}

		public EdgeWidth getCurrentEW() {
			return currentEW;
		}

		public void setCurrentEW(EdgeWidth currentEW) {
			this.currentEW = currentEW;
		}

		public LineColour getCurrentLC() {
			return currentLC;
		}

		public void setCurrentLC(LineColour currentLC) {
			this.currentLC = currentLC;
		}

		public LineWidth getCurrentLW() {
			return currentLW;
		}

		public void setCurrentLW(LineWidth currentLW) {
			this.currentLW = currentLW;
		}
		
		

}
