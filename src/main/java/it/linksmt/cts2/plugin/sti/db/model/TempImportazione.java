package it.linksmt.cts2.plugin.sti.db.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;


/**
 * "temp_importazione" table model.
 * 
 * @author Davide Pastore
 *
 */
public class TempImportazione {

	private Long codTempImportazione;
	private Timestamp startTimeStamp;
	private String nameCodeSystem;
	private String versionCodeSystem;
	private Date effectiveDate;
	private String status;
	private String statusMessage;
	private String oid;
	private String description;
	
	public TempImportazione() {
		
	}
	
	/**
	 * Construct a {@link TempImportazione} object by the given {@link ResultSet} object.
	 * @param rs The {@link ResultSet} from which retrieve the data.
	 * @throws SQLException
	 */
	public TempImportazione(ResultSet rs) throws SQLException{
		codTempImportazione = rs.getLong("cod_temp_importazione");
		startTimeStamp = rs.getTimestamp("start_time_stamp");
		nameCodeSystem = rs.getString("name_code_system");
		versionCodeSystem = rs.getString("version_code_system");
		effectiveDate = rs.getDate("effective_date");
		status = rs.getString("status");
		statusMessage = rs.getString("status_message");
		oid = rs.getString("oid");
		description = rs.getString("description");
	}

	public Long getCodTempImportazione() {
		return codTempImportazione;
	}

	public void setCodTempImportazione(Long codTempImportazione) {
		this.codTempImportazione = codTempImportazione;
	}

	public Timestamp getStartTimeStamp() {
		return startTimeStamp;
	}

	public void setStartTimeStamp(Timestamp startTimeStamp) {
		this.startTimeStamp = startTimeStamp;
	}

	public String getNameCodeSystem() {
		return nameCodeSystem;
	}

	public void setNameCodeSystem(String nameCodeSystem) {
		this.nameCodeSystem = nameCodeSystem;
	}

	public String getVersionCodeSystem() {
		return versionCodeSystem;
	}

	public void setVersionCodeSystem(String versionCodeSystem) {
		this.versionCodeSystem = versionCodeSystem;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

}
