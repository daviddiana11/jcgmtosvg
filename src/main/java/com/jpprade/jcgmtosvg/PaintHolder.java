package com.jpprade.jcgmtosvg;

import java.util.ArrayList;
import java.util.List;

import net.sf.jcgm.core.ApplicationStructureAttribute;
import net.sf.jcgm.core.EdgeColour;
import net.sf.jcgm.core.EdgeWidth;
import net.sf.jcgm.core.FillColour;
import net.sf.jcgm.core.LineColour;
import net.sf.jcgm.core.LineWidth;
import net.sf.jcgm.core.Member;
import net.sf.jcgm.core.StructuredDataRecord;

public class PaintHolder {


	private FillColour currentFC = null;

	private EdgeColour currentEC = null;

	private EdgeWidth currentEW = null;		

	private LineColour currentLC = null;

	private LineWidth currentLW = null;

	private List<ApplicationStructureAttribute> curentAPS = new ArrayList<>();

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


	public void addAPS(ApplicationStructureAttribute aps) {
		this.curentAPS.add(aps);
	}

	public String getName() {
		if(curentAPS!=null) {
			for(ApplicationStructureAttribute aps : curentAPS) {
				String attributeType= aps.getAttributeType();
				StructuredDataRecord structuredDataRecord = aps.getStructuredDataRecord();
				if("name".equals(attributeType)) {
					List<Member> members = structuredDataRecord.getMembers();
					if(members!=null && members.size() ==1) {
						if(members.get(0).getCount() > 0) {
							if(members.get(0).getData() != null 
									&& members.get(0).getData().size() > 0
									&& members.get(0).getData().get(0) instanceof String) {
								return (String) members.get(0).getData().get(0);
							}
						}
					}
				}
			}
		}
		return "";
	}



}
