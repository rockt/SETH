package de.hu.berlin.wbi.objects;

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

import edu.uchsc.ccp.nlp.ei.mutation.MutationFinder;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Represents a <a href="http://www.hgvs.org/">HGVS</a> reference, like <tt>NR_027062.1:c.157+25766C>T</tt>.
 * @author Philippe Thomas
 *
 */
public class HGVS {

    /** Type.  (c., p., g., m.) */
    private char type;

    /** Location of the mutation */
    private String location;

    /** Wild-type allele */
    private String wildtype;

    /** Mutated allele */
    private String mutation;

    /** Flag indicating deletion, insertion, etc */
    private boolean insdeldup;

    /**  */
    private static final Pattern locationPattern  = Pattern.compile("[A-Za-z]");
    private static final Pattern mutationMattern = Pattern.compile("([A-Za-z]+)([1-9][0-9]*)([A-Za-z]+|=)");
    /**
     * Construct a HGVS object based on a HGVS string (e.g. "NM_123c.234A>T")
     *
     * @param string  HGVS mention
     */
    public HGVS(String string){
        super();

        try{
            this.type =  string.charAt(0);

            if(type == 'c' || type =='g'){
                String s = string.substring(2);	//String contains a substring of s (minus reference sequence at beginning)

                //Find location
                Matcher locationMatcher = locationPattern.matcher(s);
                locationMatcher.find();
                this.location = s.substring(0,locationMatcher.start());
                //Determine wildtype and mutation
                s = s.substring(locationMatcher.start());

                if(s.contains(">")){
                    this.wildtype = s.substring(0,s.indexOf(">"));
                    this.mutation = s.substring(s.indexOf(">")+1);
                }
                else if(s.startsWith("ins") || s.startsWith("del") || s.startsWith("dup")){
                    this.wildtype = s.substring(0,3);
                    this.mutation = s.substring(3);
                    this.insdeldup = true;
                }
                else{
                    throw new Exception("Unknown HGVS string '" +string +"'");	//In case we observe a new type of variation
                }
            }
            else  if (type =='p'){				//Protein-mentions are extracted from dbSNP directly, but sometimes the XML/database information is incomplete (e.g. rs4684677 contains not the wildtype amino acid)
                Map<String, String> map = MutationFinder.populateAminoAcidThreeToOneLookupMap;
                String s = string.substring(2);	//String contains a substring of s (minus reference sequence at beginning)
                this.type = 'p';
                Matcher m = mutationMattern.matcher(s);
                if(m.find()){

                    //TODO This code currently only supports substitions. But not protein deletions and so on.
                    this.location = m.group(2);
                    this.wildtype = map.get(m.group(1).toUpperCase());
                    if(m.group(3).equals("="))
                        this.mutation = this.wildtype;
                    else
                        this.mutation = map.get(m.group(3).toUpperCase());
                }
            }
            else if (type == 'n' || type == 'm'){
                //Currently [non-coding RNA reference sequence (gene producing an RNA transcript but not a protein)] are ignored.
                //Also mRNA is ignored
            }
            else
                System.err.println("Type " +type +" not covered");
        }
        catch(Exception exception){
            this.type = '-';
            this.location = null;
            this.wildtype = null;
            this.mutation = null;
        }//This can be thrown for SNP's like rs9281649 where the HGVS string from dbSNP is erroneous (NG_000013.2:g.34513)
    }

    /**
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((location == null) ? 0 : location.hashCode());
        result = prime * result
                + ((mutation == null) ? 0 : mutation.hashCode());
        result = prime * result + type;
        result = prime * result
                + ((wildtype == null) ? 0 : wildtype.hashCode());
        return result;
    }

    /**
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HGVS other = (HGVS) obj;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (mutation == null) {
            if (other.mutation != null)
                return false;
        } else if (!mutation.equals(other.mutation))
            return false;
        if (type != other.type)
            return false;
        if (wildtype == null) {
            if (other.wildtype != null)
                return false;
        } else if (!wildtype.equals(other.wildtype))
            return false;
        return true;
    }

    /**
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if(type == 'c' || type =='g')   {
            if(insdeldup)
                return type +"." +location +wildtype +mutation;
            else
                return type +"." +location +wildtype +">" +mutation;
        }

        else
            return type +"." +wildtype +location +mutation;
    }

    /**
     * Gets the mutation type (e.g. c., p.,  or g.)
     *
     * @return the location
     */
    public char getType() {
        return type;
    }

    /**
     * Gets the location.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets the wild-type.
     *
     * @return the wild-type
     */
    public String getWildtype() {
        return wildtype;
    }

    /**
     * Gets the mutation.
     *
     * @return the mutation
     */
    public String getMutation() {
        return mutation;
    }
}
