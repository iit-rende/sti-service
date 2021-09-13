package it.linksmt.cts2.plugin.sti.service;

import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import edu.mayo.cts2.framework.core.config.ConfigConstants;
import edu.mayo.cts2.framework.core.config.RefreshableServerContext;
import edu.mayo.cts2.framework.core.plugin.PluginConfigManager;
import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.model.core.CodeSystemReference;
import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference;
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference;
import edu.mayo.cts2.framework.model.core.OpaqueData;
import edu.mayo.cts2.framework.model.core.SourceReference;
import edu.mayo.cts2.framework.model.service.core.DocumentedNamespaceReference;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.service.profile.BaseService;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public abstract class AbstractStiService implements BaseService, InitializingBean {

	private static Logger log = Logger.getLogger(AbstractStiService.class);

	private static final String LINKS = "Links MT";

	protected String buildVersion 	= "1.0";
	protected String buildName		= "STI - CTS-2";
	protected String buildDescription = "Implementazione CTS per il Servizio Terminologico";

	@javax.annotation.Resource
	private PluginConfigManager pluginConfigManager;

	private UrlConstructor urlConstructor;

	public UrlConstructor getUrlConstructor() {
		return urlConstructor;
	}

	public CodeSystemVersionReference buildCodeSystemVersionReference(
			final String codeSystemName, final String codeSystemVersionName){

		CodeSystemVersionReference ref = new CodeSystemVersionReference();

		ref.setCodeSystem(this.buildCodeSystemReference(codeSystemName,
				getUrlConstructor().createCodeSystemUrl(codeSystemName)));

		NameAndMeaningReference version = new NameAndMeaningReference();
		version.setContent(codeSystemVersionName);
		version.setUri(getUrlConstructor().createCodeSystemVersionUrl(codeSystemName, codeSystemVersionName));
		version.setHref(getUrlConstructor().createCodeSystemVersionUrl(codeSystemName, codeSystemVersionName));

		ref.setVersion(version);

		return ref;
	}

	public CodeSystemReference buildCodeSystemReference(final String codeSystemName, final String codeSystemURI){
		CodeSystemReference codeSystemReference = new CodeSystemReference();
		String codeSystemPath = getUrlConstructor().createCodeSystemUrl(codeSystemName);

		codeSystemReference.setContent(codeSystemName);
		codeSystemReference.setHref(codeSystemPath);
		codeSystemReference.setUri(codeSystemURI);

		return codeSystemReference;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		// Override Server Root
		String serverRoot = StiServiceUtil.trimStr(StiAppConfig.getProperty(
				StiServiceConfiguration.CTS2_STI_SERVER_ADDRESS));

		if ((!StiServiceUtil.isNull(serverRoot)) &&
				(this.pluginConfigManager.getServerContext() instanceof RefreshableServerContext)) {

			RefreshableServerContext rc = (RefreshableServerContext)
					this.pluginConfigManager.getServerContext();

			Hashtable<String, String> rs = new Hashtable<String, String>();
			rs.put(ConfigConstants.SERVER_ROOT_PROPERTY, serverRoot);
			rc.updated(rs);

			this.urlConstructor = new UrlConstructor(rc);
		}
		else {
			this.urlConstructor = new UrlConstructor(this.pluginConfigManager.getServerContext());
		}
	}

	@Override
	public String getServiceVersion() {
		return buildVersion;
	}

	@Override
	public SourceReference getServiceProvider() {
		SourceReference ref = new SourceReference();
		ref.setContent(LINKS);

		return ref;
	}

	@Override
	public OpaqueData getServiceDescription() {
		return ModelUtils.createOpaqueData(buildDescription);
	}

	@Override
	public String getServiceName() {
		return this.getClass().getSimpleName() + " - " + buildName;
	}

	@Override
	public List<DocumentedNamespaceReference> getKnownNamespaceList() {
		return null;
	}
}
