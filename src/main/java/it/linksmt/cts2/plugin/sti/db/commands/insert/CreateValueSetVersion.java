package it.linksmt.cts2.plugin.sti.db.commands.insert;

import java.util.Date;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.ValueSet;
import it.linksmt.cts2.plugin.sti.db.model.ValueSetVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import org.hibernate.Session;

public class CreateValueSetVersion extends HibernateCommand {

	private ValueSet valueSet;
	private Integer status;
	private Date statusDate;
	private Date insertTimestamp;
	private Date releaseDate;
	private Long previousVersionId;
	private String oid;
	private String name;
	private Long virtualCodeSystemVersionId;
	private Date lastChangeDate;

	public CreateValueSetVersion(ValueSet valueSet, Integer status,
			Date statusDate, Date insertTimestamp, Date releaseDate,
			Long previousVersionId, String oid, String name,
			Long virtualCodeSystemVersionId, Date lastChangeDate) {
		this.valueSet = valueSet;
		this.status = status;
		this.statusDate = statusDate;
		this.insertTimestamp = insertTimestamp;
		this.releaseDate = releaseDate;
		this.previousVersionId = previousVersionId;
		this.oid = oid;
		this.name = name;
		this.virtualCodeSystemVersionId = virtualCodeSystemVersionId;
		this.lastChangeDate = lastChangeDate;
	}

	@Override
	public void checkPermission(Session session)
			throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public ValueSetVersion execute(Session session) throws StiAuthorizationException,
	StiHibernateException {

		ValueSetVersion vs = new ValueSetVersion(valueSet, status, statusDate, insertTimestamp, releaseDate,
				previousVersionId, oid, name, virtualCodeSystemVersionId, lastChangeDate  ) ;
		session.save(vs);
		session.flush();
		session.refresh(vs);
		return vs;
	}

}
