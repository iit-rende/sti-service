package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionEntityMembership;
import it.linksmt.cts2.plugin.sti.importer.icd9cm.Icd9CmFields;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class GetConceptLastVersion extends HibernateCommand {

	private String code = null;
	private String codeSystemName = null;

	public GetConceptLastVersion(final String code, final String codeSystemName) {
		this.code = code;
		this.codeSystemName = StiServiceUtil.trimStr(codeSystemName);
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public CodeSystemConcept execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		if (StiServiceUtil.isNull(code)) {
			return null;
		}

		if (StiServiceUtil.isNull(codeSystemName)) {
			throw new StiHibernateException("Specificare il nome del Code System.");
		}

		List<CodeSystemConcept> candConc = session.createCriteria(CodeSystemConcept.class).add(
				Restrictions.eq("code", StiServiceUtil.trimStr(code).toUpperCase())).list();

		if ( ((candConc == null) || (candConc.size() == 0)) &&
				Icd9CmFields.ICD9_CM_CODE_SYSTEM_NAME.equalsIgnoreCase(
					StiServiceUtil.trimStr(codeSystemName))) {
			candConc = session.createCriteria(CodeSystemConcept.class).add(
					Restrictions.eq("termAbbrevation", StiServiceUtil.trimStr(code).toUpperCase())).list();
		}

		CodeSystemConcept retVal = null;
		Date effectiveDate = null;

		for (int i = 0; i < candConc.size(); i++) {
			CodeSystemConcept curVal = candConc.get(i);

			Set<CodeSystemVersionEntityMembership> memSet = curVal.getCodeSystemEntityVersion()
					.getCodeSystemEntity().getCodeSystemVersionEntityMemberships();

//			if ((memSet == null) || (memSet.size() != 1)) {
//				throw new StiHibernateException("Il sistema attualmente supporta "
//						+ "una singola associazione tra la versione del CS e la Entity.");
//			}
			
			if ((memSet == null) || (memSet.size() > 1)) {
				throw new StiHibernateException("Il sistema attualmente supporta "
						+ "una singola associazione tra la versione del CS e la Entity.");
			}

			if (memSet!=null && memSet.size() > 0) {
				CodeSystemVersion curCsVer = memSet.iterator().next().getCodeSystemVersion();
				if ( (StiConstants.STATUS_CODES.ACTIVE.getCode() != curCsVer.getStatus().intValue() ) ||
					 (!codeSystemName.equalsIgnoreCase(curCsVer.getCodeSystem().getName()))) {
					continue;
				}

				if ( (effectiveDate == null) || effectiveDate.before(curCsVer.getReleaseDate()) ) {
					effectiveDate = curCsVer.getReleaseDate();
					retVal = curVal;
				}
			}
			
		}

		return retVal;
	}

}
