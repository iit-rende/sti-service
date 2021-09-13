package it.linksmt.cts2.plugin.sti.db.commands.insert;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.ExtraMetadataParameter;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class CreateExtraMetadataParameter extends HibernateCommand {

	private String paramName;
	private String paramDatatype;
	private String paramValue;
	private String description;
	private CodeSystem codeSystem;
	


	public CreateExtraMetadataParameter(String paramName, CodeSystem codeSystem, String paramDatatype, String description, String paramValue) {
		super();
		this.paramName = paramName;
		this.codeSystem = codeSystem;
		this.paramDatatype = paramDatatype;
		this.description = description;
		this.paramValue = paramValue;
	}

	@Override
	public void checkPermission(Session session)
			throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public ExtraMetadataParameter execute(Session session) throws StiAuthorizationException,
	StiHibernateException {
		
		Criteria critMeta = session.createCriteria(ExtraMetadataParameter.class)
				.add(Restrictions.eq("paramName", paramName.trim().toUpperCase()))
				.add(Restrictions.eq("codeSystem.id", codeSystem.getId().longValue()));
		ExtraMetadataParameter metaParam = (ExtraMetadataParameter)critMeta.uniqueResult();
		if (metaParam == null) {
			metaParam = new ExtraMetadataParameter();

			metaParam.setCodeSystem(codeSystem);
			metaParam.setDescription(description);
			metaParam.setParamDatatype(paramDatatype);
			metaParam.setParamName(paramName.toUpperCase());
			metaParam.setParamValue(paramValue);
			session.save(metaParam);
			session.flush();
			session.refresh(metaParam);
		}
		
		return metaParam;
	}

}
