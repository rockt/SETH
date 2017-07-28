package de.hu.berlin.wbi.stuff.xml;

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
	final Set<String> mutations;
		
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
		this.mutations.addAll(mutations);
	}
	@Override
	public String toString() {
		return "PSM [aaLoc=" + aaLoc + ", entrez=" + entrez + ", reference="
				+ wildtype + ", mutations=" + mutations + "]";
	}
}
