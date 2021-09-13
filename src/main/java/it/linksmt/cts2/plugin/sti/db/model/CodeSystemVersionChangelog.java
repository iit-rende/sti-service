package it.linksmt.cts2.plugin.sti.db.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author Luigi Pasca
 *
 * Rappresenta i cambiamenti tra una nuova versione
 * di un codeSystem e la precedente versione.
 * newRows il numero di righe aggiunte rispetto alla precedente versione
 * deletedRows il numero di righe rimosse rispetto alla precedente versione
 * changedCodes i codici che hanno subito cambiamenti rispetto alla precedente versione
 * version la versione attuale
 * previousVersion la versione precedente
 * 
 * articleId id del journal article (liferay)
 */
@Entity
@Table(name="extra_codesystem_version_changelog")
public class CodeSystemVersionChangelog {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name="new_rows")
	private int newRows;
	
	@Column(name="deleted_rows")
	private int deletedRows;
	
	@Column(name="changed_codes")
	private String changedCodes;
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="code_system_id")
	private CodeSystem codeSystem;
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="version_id")
	private CodeSystemVersion version;
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="previous_version_id")
	private CodeSystemVersion previousVersion;

	@Column(name="article_id")
	private Long articleId;
	
	@Column(name="date_insert")
	private Date dateInsert;
	
	@Column(name="type")
	private String type;

	
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="code_system_id_to")
	private CodeSystem codeSystemTo;
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="version_id_to")
	private CodeSystemVersion versionTo;
	
	
	
	public CodeSystemVersionChangelog(int newRows, int deletedRows,
			String changedCodes, CodeSystem codeSystem,
			CodeSystemVersion version, CodeSystemVersion previousVersion,
			Date dateInsert, String type, CodeSystem codeSystemTo, CodeSystemVersion versionTo) {
		super();
		this.newRows = newRows;
		this.deletedRows = deletedRows;
		this.changedCodes = changedCodes;
		this.codeSystem = codeSystem;
		this.version = version;
		this.previousVersion = previousVersion;
		this.dateInsert = dateInsert;
		this.type = type;
		this.codeSystemTo = codeSystemTo;
		this.versionTo = versionTo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getNewRows() {
		return newRows;
	}

	public void setNewRows(int newRows) {
		this.newRows = newRows;
	}

	public int getDeletedRows() {
		return deletedRows;
	}

	public void setDeletedRows(int deletedRows) {
		this.deletedRows = deletedRows;
	}

	public String getChangedCodes() {
		return changedCodes;
	}

	public void setChangedCodes(String changedCodes) {
		this.changedCodes = changedCodes;
	}

	public CodeSystemVersion getVersion() {
		return version;
	}

	public void setVersion(CodeSystemVersion version) {
		this.version = version;
	}

	public CodeSystemVersion getPreviousVersion() {
		return previousVersion;
	}

	public void setPreviousVersion(CodeSystemVersion previousVersion) {
		this.previousVersion = previousVersion;
	}

	public Long getArticleId() {
		return articleId;
	}

	public void setArticleId(Long articleId) {
		this.articleId = articleId;
	}
	
	public Date getDateInsert() {
		return dateInsert;
	}
	
	public void setDateInsert(Date dateInsert) {
		this.dateInsert = dateInsert;
	}

	public CodeSystem getCodeSystem() {
		return codeSystem;
	}

	public void setCodeSystem(CodeSystem codeSystem) {
		this.codeSystem = codeSystem;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public CodeSystem getCodeSystemTo() {
		return codeSystemTo;
	}

	public void setCodeSystemTo(CodeSystem codeSystemTo) {
		this.codeSystemTo = codeSystemTo;
	}

	public CodeSystemVersion getVersionTo() {
		return versionTo;
	}

	public void setVersionTo(CodeSystemVersion versionTo) {
		this.versionTo = versionTo;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CodeSystemVersionChangelog [id=");
		builder.append(id);
		builder.append(", newRows=");
		builder.append(newRows);
		builder.append(", deletedRows=");
		builder.append(deletedRows);
		builder.append(", changedCodes=");
		builder.append(changedCodes);
		builder.append(", codeSystem=");
		builder.append(codeSystem);
		builder.append(", version=");
		builder.append(version);
		builder.append(", previousVersion=");
		builder.append(previousVersion);
		builder.append(", articleId=");
		builder.append(articleId);
		builder.append(", dateInsert=");
		builder.append(dateInsert);
		builder.append(", type=");
		builder.append(type);
		builder.append(", codeSystemTo=");
		builder.append(codeSystemTo);
		builder.append(", versionTo=");
		builder.append(versionTo);
		builder.append("]");
		return builder.toString();
	}


}
