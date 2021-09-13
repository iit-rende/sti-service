package it.linksmt.cts2.plugin.sti.db.commands.insert;

import java.util.Date;

import org.hibernate.Session;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.ValueSet;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

public class CreateValueSet extends HibernateCommand {

	private String name;
	private String description;
	private Date statusDate;
	private Integer status;
	
	public CreateValueSet(String name, String description, Date statusDate,
			Integer status) {
		super();
		this.name = name;
		this.description = description;
		this.statusDate = statusDate;
		this.status = status;
	}

	@Override
	public void checkPermission(Session session)
			throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public ValueSet execute(Session session) throws StiAuthorizationException,
			StiHibernateException {
		
		ValueSet valueSet = new ValueSet(name, description, 
				 status, statusDate);
		
		session.save(valueSet);
		session.flush();
		session.refresh(valueSet);
		
		return valueSet;
	}

}
