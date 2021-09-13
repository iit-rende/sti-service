package it.linksmt.cts2.plugin.sti.service.codesystem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntry;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.EntryDescription;
import edu.mayo.cts2.framework.model.core.Property;
import edu.mayo.cts2.framework.model.core.types.EntryState;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.service.profile.codesystem.CodeSystemReadService;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionById;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersions;
import it.linksmt.cts2.plugin.sti.db.commands.search.SearchCodeSystems;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.search.util.DbTransformUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.util.SessionUtil;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

@Component
public class StiCodeSystemReadService
	extends AbstractStiService
	implements CodeSystemReadService {

	private static Logger log = Logger.getLogger(StiCodeSystemReadService.class);

	private static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

	@Override
	public CodeSystemCatalogEntry read(final NameOrURI identifier, final ResolvedReadContext readContext) {
		//NameOrURI can either be a CodeSystenName or its URI. For this example, assume the request is 'by name'.
		String codeSystemName = StiServiceUtil.trimStr(identifier.getName());

		if(StiServiceUtil.isNull(codeSystemName)){
			throw new UnsupportedOperationException("Only resolution by name is supported.");
		}

		try {

			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
			List<CodeSystem> result = (List<CodeSystem>)hibUtil.executeByUser(
					new SearchCodeSystems(codeSystemName, false, null), SessionUtil.getLoggedUser());

			if (result.size() == 0) {
				return null;
			}
			if (result.size() > 1) {
				throw new Exception("Non esiste un match univoco per il Code System");
			}

			CodeSystem cs = result.get(0);
			if (cs.getCurrentVersionId() == null) {
				return null;
			}

			CodeSystemCatalogEntry entry = new CodeSystemCatalogEntry();
			entry.setCodeSystemName(StiServiceUtil.trimStr(cs.getName()));

			// Versioni del Code System
			List<CodeSystemVersion> csVersList = (List<CodeSystemVersion>) hibUtil.executeBySystem(
					new GetCodeSystemVersions(codeSystemName, StiConstants.STATUS_CODES.ACTIVE));

			JsonArray arrVers = new JsonArray();
			if (csVersList != null) {
				for (int i = csVersList.size(); i > 0; i--) {
					CodeSystemVersion vers = csVersList.get(i-1);
					arrVers.add(new JsonPrimitive(
							StiServiceUtil.trimStr(vers.getName())));
				}
			}

			entry.setVersions(arrVers.toString());

			// Versione Corrente
			CodeSystemVersion curVers = (CodeSystemVersion) hibUtil
					.executeByUser(new GetCodeSystemVersionById(
					cs.getCurrentVersionId()), SessionUtil.getLoggedUser());

			// Informazioni della versione
			List<Property> propVers = new ArrayList<Property>();
			DbTransformUtil.addPropertyToList(propVers, "OID",
					StiServiceUtil.trimStr(curVers.getOid()));

			if (curVers.getReleaseDate() != null) {
				DbTransformUtil.addPropertyToList(propVers, "RELEASE_DATE",
						ISO_DATE_FORMAT.format(curVers.getReleaseDate()));
			}

			if ( (curVers.getStatus() != null) && curVers.getStatus().intValue() !=
					StiConstants.STATUS_CODES.ACTIVE.getCode()) {
				entry.setEntryState(EntryState.INACTIVE);
			}
			else {
				entry.setEntryState(EntryState.ACTIVE);
			}

			entry.setProperty(propVers);


			// Link al dettaglio del code System
			entry.setAbout(buildCodeSystemReference(codeSystemName,
					StiServiceUtil.trimStr(curVers.getName())).getHref());

			EntryDescription ed = new EntryDescription();
			ed.setValue(ModelUtils.toTsAnyType(curVers.getDescription()));

			entry.setResourceSynopsis(ed);

			return entry;
		}
		catch(Exception re) {
			log.error("Errore durante la lettura dei dati del Code System: " + codeSystemName, re);
			throw new RuntimeException("Errore durante la lettura dei dati del Code System: " + codeSystemName, re);
		}
	}

	@Override
	public boolean exists(final NameOrURI identifier, final ResolvedReadContext readContext) {
		return (read(identifier, readContext) != null);
	}
}