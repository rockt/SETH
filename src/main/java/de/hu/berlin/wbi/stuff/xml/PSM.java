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
import java.util.HashSet;
import java.util.Set;

/**
 * Class represents a protein sequence mutation
 * @author Philippe Thomas 
 *
 */
public class PSM {
    private int entrez;
    private int aaLoc;
    private String wildtype;
	Set<String> mutations;
		
	public PSM(int entrez, int aaLoc, String reference, String mutations) {
		super();
		this.entrez = entrez;
		this.aaLoc = aaLoc;
		this.wildtype = reference;
		this.mutations = new HashSet<String>(1);
		if(mutations != null)
			this.mutations.add(mutations);
	}
	
	/**
	 * Checks if the PSM object is valid.
	 * It has to contain a entrez-gene Id, location, and a wildtype, and a mutation
	 * Not given for SPNs like rs9341281
	 * @return  true if SNP is valid
	 */
	public boolean isValid(){
		if (entrez < 0)
			return false;
		
		if(aaLoc < 0)
			return false;
		
		if(wildtype == null)
			return false;
		
		if(mutations == null || mutations.size() == 0)
			return false;
		
		return true;
	}
	
	public int getEntrez() {
		return entrez;
	}
	public void setEntrez(int entrez) {
		this.entrez = entrez;
	}
	public int getAaLoc() {
		return aaLoc;
	}
	public void setAaLoc(int aaLoc) {
		this.aaLoc = aaLoc;
	}
	public String getWildtype() {
		return wildtype;
	}
	public void setWildtype(String wildtype) {
		this.wildtype = wildtype;
	}
	public String getMutations(){
		StringBuilder sb = new StringBuilder();
		for(String mutation : mutations){
			sb.append(mutation);
			sb.append("|");			
		}

		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	public void addMutation(String mutation) {
		if(mutation != null)
			this.mutations.add(mutation);
	}
	public void addMutations(Set<String> mutations) {
		for(String mutation : mutations)
			this.mutations.add(mutation);
	}
	@Override
	public String toString() {
		return "PSM [aaLoc=" + aaLoc + ", entrez=" + entrez + ", reference="
				+ wildtype + ", mutations=" + mutations + "]";
	}
}
