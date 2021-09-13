package it.linksmt.cts2.plugin.sti.db.commands.insert;

import java.util.Date;

import org.hibernate.Session;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionChangelog;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

public class InsertChangelog extends HibernateCommand {

	private int newRows;
	private int deletedRows;
	private String changedCodes;
	private CodeSystemVersion version;
	private CodeSystemVersion previousVersion;
	private Date dateInsert;
	private CodeSystem codeSystem;
	private String type;
	private CodeSystem codeSystemTo;
	private CodeSystemVersion versionTo;
	
	public InsertChangelog(int newRows, int deletedRows, String changedCodes, CodeSystem codeSystem,
			CodeSystemVersion version, CodeSystemVersion previousVersion, Date dateInsert, String type) {
		super();
		this.newRows = newRows;
		this.deletedRows = deletedRows;
		this.changedCodes = changedCodes;
		this.codeSystem = codeSystem;
		this.version = version;
		this.previousVersion = previousVersion;
		this.dateInsert = dateInsert;
		this.type = type;
	}
	
	public InsertChangelog(int newRows, int deletedRows, String changedCodes, CodeSystem codeSystem,
			CodeSystemVersion version, CodeSystemVersion previousVersion, Date dateInsert, String type,CodeSystem codeSystemTo,CodeSystemVersion versionTo) {
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

	@Override
	public void checkPermission(Session session)
			throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public CodeSystemVersionChangelog execute(Session session) throws StiAuthorizationException,
			StiHibernateException {
		
		CodeSystemVersionChangelog changelog = new CodeSystemVersionChangelog(this.newRows, this.deletedRows, this.changedCodes, this.codeSystem, this.version, this.previousVersion, this.dateInsert,this.type,this.codeSystemTo,this.versionTo);
		session.save(changelog);
		session.flush();
		session.refresh(changelog);
		
		return changelog;
	}

}
