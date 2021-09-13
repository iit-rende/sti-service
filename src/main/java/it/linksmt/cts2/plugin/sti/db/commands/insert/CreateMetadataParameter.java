package it.linksmt.cts2.plugin.sti.db.commands.insert;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.MetadataParameter;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class CreateMetadataParameter extends HibernateCommand {

	private String paramName;
	private CodeSystem codeSystem;
	private String paramDatatype;
	private String metadataParameterType;
	private String languageCd;
	private String description;
	private String paramNameDisplay;
	private Integer maxLength;
	private Integer position;


	public CreateMetadataParameter(String paramName, CodeSystem codeSystem,
			String paramDatatype, String metadataParameterType,
			String languageCd, String description, String paramNameDisplay,
			Integer maxLength,Integer position) {
		super();
		this.paramName = paramName;
		this.codeSystem = codeSystem;
		this.paramDatatype = paramDatatype;
		this.metadataParameterType = metadataParameterType;
		this.languageCd = languageCd;
		this.description = description;
		this.paramNameDisplay = paramNameDisplay;
		this.maxLength = maxLength;
		this.position = position;
	}

	@Override
	public void checkPermission(Session session)
			throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public MetadataParameter execute(Session session) throws StiAuthorizationException,
	StiHibernateException {
		
//		Criteria critMeta = session.createCriteria(MetadataParameter.class)
//				.add(Restrictions.eq("paramName", paramName.trim().toUpperCase()))
//				.add(Restrictions.eq("codeSystem.id", codeSystem.getId().longValue()));
		
		
		
		paramName = StiServiceUtil.paramNameToUpperCaseAndClean(paramName);
		
		Criteria critMeta = session.createCriteria(MetadataParameter.class)
				.add(Restrictions.eq("paramName", paramName))
				.add(Restrictions.eq("codeSystem.id", codeSystem.getId().longValue()));
		
		
		
		
		if(null != languageCd && !"".equals(languageCd)){
			critMeta.add(Restrictions.eq("languageCd", languageCd.toUpperCase()));
		}
		
		//TODO BISOGNA INTERVENIRE QUI PER L'ERRORE (AGGIUNGERE IL FILTRO SULLA LINGUA SE VIENE PASSATA COME PARAMETRO)
		//		"VERIFICARE PERCHE VA IN ERRORE SE NEI 2 CSV (ITA,ENG) C'è UN CAMPO CON LO STESSO NOME"
		MetadataParameter metaParam = (MetadataParameter)critMeta.uniqueResult();
		if (metaParam == null) {
			metaParam = new MetadataParameter();

			metaParam.setCodeSystem(codeSystem);
			metaParam.setDescription(description);
			metaParam.setLanguageCd(languageCd);
			metaParam.setMaxLength(maxLength);
			metaParam.setParamDatatype(paramDatatype);
			metaParam.setMetadataParameterType(metadataParameterType);
			metaParam.setParamName(paramName.toUpperCase());
			metaParam.setParamNameDisplay(paramNameDisplay);
			metaParam.setPosition(position);
			session.save(metaParam);
			session.flush();
			session.refresh(metaParam);
		}
		
		return metaParam;
	}
	
	

}
