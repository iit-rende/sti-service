package it.linksmt.cts2.plugin.sti.db.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "extra_metadata_parameter")
public class ExtraMetadataParameter implements java.io.Serializable {

	private Long id;
	private String paramName;
	private String paramDatatype;
	private String paramValue;
	private String description;
	private CodeSystem codeSystem;
	
	
	public ExtraMetadataParameter() {
	}

	public ExtraMetadataParameter(final String paramName, final String paramDatatype, final String paramValue,final String description, final CodeSystem codeSystem) {
		this.paramName = paramName;
		this.paramDatatype = paramDatatype;
		this.paramValue = paramValue;
		this.description = description;
		this.codeSystem = codeSystem;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "codeSystemId")
	public CodeSystem getCodeSystem() {
		return this.codeSystem;
	}

	public void setCodeSystem(final CodeSystem codeSystem) {
		this.codeSystem = codeSystem;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	@Column(name = "paramName", nullable = false, length = 65535)
	public String getParamName() {
		return this.paramName;
	}

	public void setParamName(final String paramName) {
		this.paramName = paramName;
	}

	@Column(name = "paramDatatype", length = 65535)
	public String getParamDatatype() {
		return this.paramDatatype;
	}

	public void setParamDatatype(final String paramDatatype) {
		this.paramDatatype = paramDatatype;
	}

	@Column(name = "description", nullable = true)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	@Column(name = "paramValue", nullable = true)
	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

}
