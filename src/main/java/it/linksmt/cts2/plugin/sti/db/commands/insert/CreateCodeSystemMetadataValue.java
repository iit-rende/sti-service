package it.linksmt.cts2.plugin.sti.db.commands.insert;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemMetadataValue;
import it.linksmt.cts2.plugin.sti.db.model.MetadataParameter;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import org.hibernate.Session;

public class CreateCodeSystemMetadataValue extends HibernateCommand{
	
	private String value;
	private MetadataParameter metaParam;
	private CodeSystemEntityVersion csEntityVers;
	
	public CreateCodeSystemMetadataValue(String value,MetadataParameter metaParam,CodeSystemEntityVersion csEntityVers) {
		super();
		this.value = value;
		this.metaParam = metaParam;
		this.csEntityVers = csEntityVers;
	}
	
	
	
	@Override
	public void checkPermission(Session session)
			throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}
	
	

	@Override
	public CodeSystemMetadataValue execute(Session session) throws StiAuthorizationException, StiHibernateException {

		CodeSystemMetadataValue metaVal = new CodeSystemMetadataValue();
		metaVal.setParameterValue(StiServiceUtil.trimStr(value));
		metaVal.setMetadataParameter(metaParam);
		metaVal.setCodeSystemEntityVersion(csEntityVers);

		session.save(metaVal);
		session.flush();
		session.refresh(metaVal);
		
		return metaVal;
	}

}
