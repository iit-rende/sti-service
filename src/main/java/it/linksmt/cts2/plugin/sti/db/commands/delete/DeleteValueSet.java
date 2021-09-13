package it.linksmt.cts2.plugin.sti.db.commands.delete;

import java.util.List;

import it.linksmt.cts2.plugin.sti.db.commands.search.GetValueSetVersionsByVSIdAndStatus;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.ValueSet;
import it.linksmt.cts2.plugin.sti.db.model.ValueSetVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class DeleteValueSet extends HibernateCommand {

	private static Logger log = Logger.getLogger(DeleteValueSet.class);

	private String name;

	public DeleteValueSet(final String name) {
		this.name = name;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public ValueSet execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		if (StiServiceUtil.isNull(name)) {
			throw new StiHibernateException("Occorre specificare il nome del valueset.");
		}

		// Check versione esistente
		ValueSet valueSet = (ValueSet) session.createCriteria(ValueSet.class).add(Restrictions.eq("name", StiServiceUtil.trimStr(name))).uniqueResult();

		if ( valueSet==null ) {
			throw new StiHibernateException("Errore di lettura della risorsa di valueset. name="+name);
		}


		if (valueSet != null) {
			
			List<ValueSetVersion> valueSetVersionList = new GetValueSetVersionsByVSIdAndStatus(valueSet.getId(), null).execute(session);
			if(valueSetVersionList!=null){
				for (ValueSetVersion valueSetVersion : valueSetVersionList) {
					session.delete(valueSetVersion);
					session.flush();
				}
			}
			
			session.delete(valueSet);
			session.flush();
//			session.clear();
//			session.getTransaction().commit();
		}

		session.flush();
		return valueSet;
	}
}
