package de.hu.berlin.wbi.objects;

import java.io.Serializable;

/**
 * Represents the text span of entity mentions ({@link MutationMention}, {@link Gene}, etc.).
 * 
 * @author Philippe Thomas
 * 
 */
public class EntityOffset implements Serializable {

	/** First character, inclusive, zero-based. */
	private int start;

	/** Last character, exclusive, zero-based. */
	private int stop;

	/**
	 * Constructor with start and end offset based on text
	 * @param start start position
	 * @param stop  end position
	 */
	public EntityOffset(int start, int stop) {
		super();
		if (start < 0 || start > stop)
			throw new IllegalArgumentException("Invalid span: [" + start +"," + stop +")");
		
		this.start = start;
		this.stop = stop;
	}

	/**
	 * Get start character 
	 * @return position of entity start
	 */
	public int getStart() {
		return start;
	}
	
	/**
	 * Get end character position 
	 * @return position of entity end
	 */
	public int getStop() {
		return stop;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EntityOffset [start=" + start + ", stop=" + stop + "]";
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + start;
		result = prime * result + stop;
		return result;
	}

	/**
	 * (non-Javadoc)
	 * 
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
		EntityOffset other = (EntityOffset) obj;
		if (start != other.start)
			return false;
		if (stop != other.stop)
			return false;
		return true;
	}

}
