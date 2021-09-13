package it.linksmt.cts2.plugin.sti.dtos;

import java.util.List;

public class OutputDto{
	
	private Long numFound;
	private List<?> entry;
	
	public Long getNumFound() {
		return numFound;
	}
	public void setNumFound(Long numFound) {
		this.numFound = numFound;
	}
	public List<?> getEntry() {
		return entry;
	}
	public void setEntry(List<?> entry) {
		this.entry = entry;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OutputDto [numFound=");
		builder.append(numFound);
		builder.append(", entry=");
		builder.append(entry);
		builder.append("]");
		return builder.toString();
	}
}
