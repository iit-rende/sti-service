package it.linksmt.cts2.plugin.sti.dtos;

import java.io.Serializable;

public class ChangelogDto implements Serializable {

	private static final long serialVersionUID = 4730144413615516874L;

	private String codeSystem;
	private String version;
	private String previousVersion;
	private String dateCreate;
	private int newCodes;
	private int deletedCodes;
	private int importedRow;
	private String changedCodes;
	private String type;
	private String languages;
	private String title;
	
	private String codeSystemTo;
	private String versionTo;
	
	public String getCodeSystem() {
		return codeSystem;
	}

	public void setCodeSystem(String codeSystem) {
		this.codeSystem = codeSystem;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPreviousVersion() {
		return previousVersion;
	}

	public void setPreviousVersion(String previousVersion) {
		this.previousVersion = previousVersion;
	}

	public String getDateCreate() {
		return dateCreate;
	}

	public void setDateCreate(String dateCreate) {
		this.dateCreate = dateCreate;
	}

	public int getNewCodes() {
		return newCodes;
	}

	public void setNewCodes(int newCodes) {
		this.newCodes = newCodes;
	}

	public int getDeletedCodes() {
		return deletedCodes;
	}

	public void setDeletedCodes(int deletedCodes) {
		this.deletedCodes = deletedCodes;
	}

	public String getChangedCodes() {
		return changedCodes;
	}

	public void setChangedCodes(String changedCodes) {
		this.changedCodes = changedCodes;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLanguages() {
		return languages;
	}

	public void setLanguages(String languages) {
		this.languages = languages;
	}
	
	public int getImportedRow() {
		return importedRow;
	}

	public void setImportedRow(int importedRow) {
		this.importedRow = importedRow;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCodeSystemTo() {
		return codeSystemTo;
	}

	public void setCodeSystemTo(String codeSystemTo) {
		this.codeSystemTo = codeSystemTo;
	}

	public String getVersionTo() {
		return versionTo;
	}

	public void setVersionTo(String versionTo) {
		this.versionTo = versionTo;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChangelogDto [codeSystem=");
		builder.append(codeSystem);
		builder.append(", version=");
		builder.append(version);
		builder.append(", previousVersion=");
		builder.append(previousVersion);
		builder.append(", dateCreate=");
		builder.append(dateCreate);
		builder.append(", newCodes=");
		builder.append(newCodes);
		builder.append(", deletedCodes=");
		builder.append(deletedCodes);
		builder.append(", importedRow=");
		builder.append(importedRow);
		builder.append(", changedCodes=");
		builder.append(changedCodes);
		builder.append(", type=");
		builder.append(type);
		builder.append(", languages=");
		builder.append(languages);
		builder.append(", title=");
		builder.append(title);
		builder.append(", codeSystemTo=");
		builder.append(codeSystemTo);
		builder.append(", versionTo=");
		builder.append(versionTo);
		builder.append("]");
		return builder.toString();
	}

}
