package it.linksmt.cts2.plugin.sti.db.commands.insert;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import java.util.Date;

import org.hibernate.Session;

public class CreateCodeSystem extends HibernateCommand {

	private String name;
	private String description;
	private Date insertDate;
	private String codeSystemType;
	
	public CreateCodeSystem(String name, String description, Date insertDate,String codeSystemType) {
		this.name = name;
		this.description = description;
		this.insertDate = insertDate;
		this.codeSystemType = codeSystemType;
	}
	
	
	@Override
	public void checkPermission(Session session)
			throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public CodeSystem execute(Session session) throws StiAuthorizationException,
			StiHibernateException {

		CodeSystem cs = new CodeSystem(null, name, description, insertDate, null, codeSystemType, null, null);
		session.save(cs);
		session.flush();
		session.refresh(cs);
		
		return cs;
	}

}
