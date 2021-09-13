package it.linksmt.cts2.plugin.sti.db.commands.search;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

public class GetCodeSystemConceptAlt extends HibernateCommand {

	private String code;
	private Long entityVersionId;
	
	public GetCodeSystemConceptAlt(String code, Long entityVersionId) {
		this.code = code;
		this.entityVersionId = entityVersionId;
	}

	@Override
	public void checkPermission(Session session)
			throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public CodeSystemConcept execute(Session session) throws StiAuthorizationException,
			StiHibernateException {


		CodeSystemConcept csc = null;
		
		if( StringUtils.isNotBlank(code)  && null != entityVersionId) {
			Criteria c = session.createCriteria(CodeSystemConcept.class)
					.createAlias("codeSystemEntityVersion", "codeSystemEntityVersion")
					.add(Restrictions.eq("codeSystemEntityVersion.versionId", entityVersionId))
					.add(Restrictions.eq("code", code));
			
			csc = (CodeSystemConcept) c.uniqueResult();
		}
		
		return csc;
	}

}
