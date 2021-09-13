package it.linksmt.cts2.plugin.sti.db.commands.insert;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemMetadataValue;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.MetadataParameter;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class ImportLocalMetadata extends HibernateCommand {

	private static Logger log = Logger.getLogger(ImportLocalMetadata.class);

	private long csVersionId = -1;
	private String csLocalName = null;
	private String loincNum = null;
	private Map<String, String> localData = null;

	public ImportLocalMetadata(
			final long csVersionId, final String csLocalName,
			final String loincNum,
			final Map<String, String> localData) {
		this.csVersionId = csVersionId;
		this.loincNum = loincNum;
		this.localData = localData;
		this.csLocalName = csLocalName;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public CodeSystemConcept execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		if (localData == null) {
			return null;
		}

		CodeSystemConcept csConc = new GetCodeSystemConcept(
				loincNum, csVersionId).execute(session);

		if (csConc == null) {
			log.warn("Impossibile inserire il mapping locale: " + loincNum);
			return null;
		}

		CodeSystemVersion csVers = (CodeSystemVersion)session.get(
				CodeSystemVersion.class, csVersionId);

		for (String metaName : localData.keySet()) {
			Criteria critMeta = session.createCriteria(MetadataParameter.class)
					.add(Restrictions.eq("paramName", StiServiceUtil.trimStr(metaName).toUpperCase()))
					.add(Restrictions.eq("languageCd", StiConstants.LOCAL_LANGUAGE_CD))
					.add(Restrictions.eq("codeSystem.id",
							csVers.getCodeSystem().getId().longValue()));

			MetadataParameter metaParam = (MetadataParameter)critMeta.uniqueResult();
			if (metaParam == null) {
				throw new StiHibernateException("Impossibile trovare la definizione del metadato: " + metaName);
			}

			inserOrUpdateMetadata(session, metaParam.getId().longValue(),
					csConc.getCodeSystemEntityVersion().getVersionId().longValue(),
					localData.get(metaName));
		}

		return csConc;
	}

	private void inserOrUpdateMetadata(final Session session,final long metaParameterId, final long csEntityVersionId,final String parameterValue) {

		CodeSystemMetadataValue metaVal = null;
		List<CodeSystemMetadataValue> checkList =
				session.createCriteria(CodeSystemMetadataValue.class)
				.add(Restrictions.eq("metadataParameter.id", metaParameterId))
				.add(Restrictions.eq("codeSystemEntityVersion.versionId", csEntityVersionId))
				.list();

//		String localPrefix = WordUtils.capitalize(StiServiceUtil.trimStr(csLocalName)) + StiConstants.LOCAL_VALUE_SEPARATOR;
		String localPrefix = WordUtils.capitalize(StiServiceUtil.trimStr(csLocalName).toLowerCase()) + StiConstants.LOCAL_VALUE_SEPARATOR;

		if (checkList != null) {
			for (int i = 0; i < checkList.size(); i++) {
				CodeSystemMetadataValue curVal = checkList.get(i);

				if (StiServiceUtil.trimStr(curVal.getParameterValue()).startsWith(localPrefix)) {
					metaVal = curVal;
					break;
				}
			}
		}

		if (metaVal == null) {
			metaVal = new CodeSystemMetadataValue();
			metaVal.setMetadataParameter((MetadataParameter)session.get(MetadataParameter.class, metaParameterId));

			metaVal.setCodeSystemEntityVersion((CodeSystemEntityVersion)session.get(CodeSystemEntityVersion.class, csEntityVersionId));
		}

		metaVal.setParameterValue(localPrefix + StiServiceUtil.trimStr(parameterValue));

		session.save(metaVal);
		session.flush();
		session.refresh(metaVal);
	}
}
