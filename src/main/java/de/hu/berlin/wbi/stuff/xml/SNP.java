package de.hu.berlin.wbi.stuff.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a SNP 
 *  consisting of dbSNP id
 *  several (at least one) protein sequence mutations
 *  several HGVS representations
 * @author Philippe Thomas 
 *
 */
public class SNP {
	private final int rsId;
	private final Set<PSM>   psms;
	private final List<String> hgvs;
	
	public SNP(int rsId) {
		super();
		this.rsId = rsId;
		psms = new HashSet<PSM>();
		hgvs = new ArrayList<String>();		
	}

	public void addPSM(PSM psm){
		
		boolean add=false;
		loop:for(PSM tmpSNP : psms){
			if(tmpSNP.getAaLoc() == psm.getAaLoc() && tmpSNP.getEntrez() == psm.getEntrez()){
				if(psm.getWildtype() != null)
					tmpSNP.setWildtype(psm.getWildtype());
				else
					tmpSNP.addMutations(psm.mutations);
				add = true;
				break loop;
			}
		}
		
		if(add == false){
			psms.add(psm);
		}
	}
	
	public void addHgvs(String hgvs){
		this.hgvs.add(hgvs);
	}

	public int getRsId() {
		return rsId;
	}

	public Set<PSM> getPsms() {
		return psms;
	}

	public List<String> getHgvs() {
		return hgvs;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("rs");
		sb.append(rsId);
		sb.append(" ");
		
		for(PSM psm : psms){
			sb.append(psm.toString());
			sb.append(";");
		}
		sb.append(" ");
		
		for(String string : hgvs){
			sb.append(string);
			sb.append(";");
		}
		
		return sb.toString();
	}

	
	
	
}
