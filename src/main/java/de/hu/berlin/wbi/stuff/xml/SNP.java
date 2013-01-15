package de.hu.berlin.wbi.stuff.xml;

/**
Copyright 2010, 2011 Philippe Thomas
This file is part of snp-normalizer.

snp-normalizer is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
any later version.

snp-normalizer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with snp-normalizer.  If not, see <http://www.gnu.org/licenses/>.
*/
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
	private int rsId;
	private Set<PSM>   psms;
	private List<String> hgvs; 
	
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
				if(psm.wildtype != null)
					tmpSNP.setWildtype(psm.wildtype);
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
