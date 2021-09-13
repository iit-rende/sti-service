package it.linksmt.cts2.plugin.sti.service.entitydescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.EntityReferenceList;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.core.VersionTagReference;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.entity.EntityListEntry;
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURI;
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURIList;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.service.command.restriction.TaggedCodeSystemRestriction;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQuery;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQueryService;
import it.linksmt.cts2.plugin.sti.db.commands.search.CountCodeSystemEntities;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemByName;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemLastVersion;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionByName;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetEntityDirectoryEntries;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.enums.CodeSystemType;
import it.linksmt.cts2.plugin.sti.importer.standardlocal.StandardLocalFields;
import it.linksmt.cts2.plugin.sti.importer.valueset.ValueSetFields;
import it.linksmt.cts2.plugin.sti.search.util.SolrQueryUtil;
import it.linksmt.cts2.plugin.sti.search.util.SolrTransformUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiQueryService;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.exception.StiQueryServiceException;
import it.linksmt.cts2.plugin.sti.service.util.SessionUtil;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

@Component
public class StiEntityDescriptionQueryService extends AbstractStiQueryService implements EntityDescriptionQueryService {

	private static Logger log = Logger.getLogger(StiEntityDescriptionQueryService.class);
	
	
	private static String SOLR_HANDLERS  = "/query";

	@Override
	public DirectoryResult<EntityDirectoryEntry> getResourceSummaries(final EntityDescriptionQuery query, final SortCriteria sortCriteria, final Page page) {

		String csVersion = extractCodeSystemVersion(query);
		String csName = extractCodeSystemName(query);

		String solrQuery = extractSolrQuery(query, csVersion);

		if (StiConstants.ALL_ENTITIES_FILTER.equals(solrQuery)) {
			if (StiServiceUtil.isNull(csVersion)) {
				csVersion = getCodeSystemLastVersion(csName);
			}

			return searchIntoDbms(null, csVersion, sortCriteria, page, csName);
		}

		if (solrQuery.startsWith(StiConstants.ENTITY_CHILDREN_FILTER + "=")) {
			if (StiServiceUtil.isNull(csVersion)) {
				csVersion = getCodeSystemLastVersion(csName);
			}

			return searchIntoDbms(solrQuery.substring(solrQuery.indexOf("=") + 1), csVersion, sortCriteria, page, csName);
		}

		if (StiServiceUtil.isNull(csName)) {
			csName = getCodeSystemNameByVersion(csVersion);
		}

		String csType = getCodeSystemTypeByName(csName);

		return searchIntoIndex(csName, csVersion, solrQuery, sortCriteria, page, csType);
	}

	@Override
	public int count(final EntityDescriptionQuery query) {

		String csVersion = extractCodeSystemVersion(query);
		String csName = extractCodeSystemName(query);

		String solrQuery = extractSolrQuery(query, csVersion);

		if (StiConstants.ALL_ENTITIES_FILTER.equals(solrQuery)) {
			if (StiServiceUtil.isNull(csVersion)) {
				csVersion = getCodeSystemLastVersion(csName);
			}

			return countIntoDbms(null, csVersion, csName);
		}

		if (solrQuery.startsWith(StiConstants.ENTITY_CHILDREN_FILTER + "=")) {
			if (StiServiceUtil.isNull(csVersion)) {
				csVersion = getCodeSystemLastVersion(csName);
			}

			return countIntoDbms(solrQuery.substring(solrQuery.indexOf("=") + 1), csVersion, csName);
		}

		if (StiServiceUtil.isNull(csName)) {
			csName = getCodeSystemNameByVersion(csVersion);
		}

		String csType = getCodeSystemTypeByName(csName);

		return countIntoIndex(csName, csVersion, solrQuery, csType);
	}

	@Override
	public DirectoryResult<EntityListEntry> getResourceList(final EntityDescriptionQuery query, final SortCriteria sortCriteria, final Page page) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEntityInSet(final EntityNameOrURI entity, final EntityDescriptionQuery restrictions, final ResolvedReadContext readContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityReferenceList resolveAsEntityReferenceList(final EntityDescriptionQuery restrictions, final ResolvedReadContext readContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityNameOrURIList intersectEntityList(final Set<EntityNameOrURI> entities, final EntityDescriptionQuery restrictions, final ResolvedReadContext readContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<? extends VersionTagReference> getSupportedTags() {
		// TODO not actually supported, but the framework won't marshall unless this is populated
		return new HashSet<VersionTagReference>(Arrays.asList(new VersionTagReference[] { new VersionTagReference("CURRENT") }));
	}

	private DirectoryResult<EntityDirectoryEntry> searchIntoIndex(final String codeSystemName, final String codeSystemVersion, final String matchQuery, final SortCriteria sortCriteria,
			final Page page, String csType) {

		String solrQuery = matchQuery + "&start=" + String.valueOf(page.getStart()) + "&rows=" + String.valueOf(page.getMaxToReturn());

		int numFound = 0;
		boolean atEnd = false;

		JsonArray docs = null;
		try {
			// Naming convention per gli indici di SOLR
			String indexName = codeSystemName;

			if (StringUtils.isNotBlank(csType) && (csType.equalsIgnoreCase(CodeSystemType.LOCAL.getKey()) || csType.equals(CodeSystemType.STANDARD_NATIONAL.getKey()))) {
				indexName = StandardLocalFields.STANDARD_LOACL_CODE_SYSTEM_INDEX_SUFFIX_NAME;
			} else if (StringUtils.isNotBlank(csType) && csType.equalsIgnoreCase(CodeSystemType.VALUE_SET.getKey())) {
				indexName = ValueSetFields.VALUESET_INDEX_SUFFIX_NAME;
			}

			String solrIndexPath = StiServiceUtil.buildCsIndexPath(indexName);

			JsonObject searchRes = SolrQueryUtil.solrQueryResult(solrQuery, solrIndexPath + SOLR_HANDLERS);
			docs = searchRes.getAsJsonArray("docs");

			int start = searchRes.getAsJsonPrimitive("start").getAsInt();
			numFound = searchRes.getAsJsonPrimitive("numFound").getAsInt();

			atEnd = (numFound - start) <= page.getMaxToReturn();
		} catch (Exception qx) {
			throw new StiQueryServiceException("Errore durante l'execuzione della query.", qx);
		}

		ArrayList<EntityDirectoryEntry> entries = new ArrayList<EntityDirectoryEntry>();
		for (int i = 0; i < docs.size(); i++) {
			JsonObject curDoc = (JsonObject) docs.get(i);

			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
			EntityDirectoryEntry entry = SolrTransformUtil.solrDocToEntityEntry(hibUtil, codeSystemName, curDoc, this);

			// TODO: settare in base a score di SOLR ?
			entry.setMatchStrength(Math.min((((double) (page.getEnd() - i)) / (double) numFound), 1.0));
			entries.add(entry);
		}

		DirectoryResult<EntityDirectoryEntry> retVal = new DirectoryResult<EntityDirectoryEntry>(entries, atEnd);

		return retVal;
	}

	private DirectoryResult<EntityDirectoryEntry> searchIntoDbms(final String superClassID, final String csVersion, final SortCriteria sortCriteria, final Page page, String csName) {

		int numFound = 0;
		boolean atEnd = false;

		List<EntityDirectoryEntry> searchRes = null;

		try {
			numFound = countIntoDbms(superClassID, csVersion, csName);
			atEnd = (numFound - page.getStart()) <= page.getMaxToReturn();

			GetEntityDirectoryEntries sCmd = new GetEntityDirectoryEntries(csVersion, superClassID, page.getStart(), page.getMaxToReturn(), this, csName);

			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
			searchRes = (List<EntityDirectoryEntry>) hibUtil.executeByUser(sCmd, SessionUtil.getLoggedUser());
		} catch (Exception qx) {
			throw new StiQueryServiceException("Errore durante l'execuzione della query.", qx);
		}

		DirectoryResult<EntityDirectoryEntry> retVal = new DirectoryResult<EntityDirectoryEntry>(searchRes, atEnd);

		return retVal;
	}

	private int countIntoIndex(final String codeSystemName, final String codeSystemVersion, final String matchQuery, String csType) {

		String solrQuery = StiServiceUtil.trimStr(matchQuery) + "&start=0&rows=1";

		try {
			// Naming convention per gli indici di SOLR
			String indexName = codeSystemName;

			if (StringUtils.isNotBlank(csType) && (csType.equalsIgnoreCase(CodeSystemType.LOCAL.getKey()) || csType.equals(CodeSystemType.STANDARD_NATIONAL.getKey()))) {
				indexName = StandardLocalFields.STANDARD_LOACL_CODE_SYSTEM_INDEX_SUFFIX_NAME;
			} else if (StringUtils.isNotBlank(csType) && csType.equalsIgnoreCase(CodeSystemType.VALUE_SET.getKey())) {
				indexName = ValueSetFields.VALUESET_INDEX_SUFFIX_NAME;
			}

			String solrIndexPath = StiServiceUtil.buildCsIndexPath(indexName);

			JsonObject searchRes = SolrQueryUtil.solrQueryResult(solrQuery, solrIndexPath + SOLR_HANDLERS);
			return searchRes.getAsJsonPrimitive("numFound").getAsInt();
		} catch (Exception qx) {
			throw new StiQueryServiceException("Errore durante l'execuzione della query.", qx);
		}
	}

	private int countIntoDbms(final String superClassID, final String csVersion, String csName) {

		try {
			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
			return ((Integer) hibUtil.executeByUser(new CountCodeSystemEntities(csVersion, superClassID, csName), SessionUtil.getLoggedUser())).intValue();
		} catch (Exception qx) {
			throw new StiQueryServiceException("Errore durante l'execuzione della query.", qx);
		}
	}

	private String extractSolrQuery(final EntityDescriptionQuery query, final String csVersion) {

		Set<ResolvedFilter> filterComp = query.getFilterComponent();
		if ((filterComp == null) || (filterComp.size() != 1)) {
			throw new StiQueryServiceException("Idicare un singolo valore come criterio di ricerca.");
		}

		String matchValueStr = StiServiceUtil.trimStr(filterComp.iterator().next().getMatchValue());
		if (StiConstants.ALL_ENTITIES_FILTER.equalsIgnoreCase(matchValueStr)) {
			return StiConstants.ALL_ENTITIES_FILTER;
		}
		if (matchValueStr.toUpperCase().startsWith(StiConstants.ENTITY_CHILDREN_FILTER + "=")) {
			return matchValueStr;
		}

		String solrQuery = matchValueStr;

		if (!solrQuery.startsWith("q=")) {
			solrQuery = "q=" + solrQuery;
		}
		if (StiServiceUtil.isNull(csVersion)) {
			solrQuery += "&fq=IS_LAST_VERSION:true";
		} else {
			solrQuery += "&fq=VERSION:" + StiServiceUtil.trimStr(csVersion);
		}

		return solrQuery;
	}

	private String extractCodeSystemVersion(final EntityDescriptionQuery query) {

		Set<NameOrURI> resVer = query.getRestrictions().getCodeSystemVersions();

		String csVersion = null;
		if (resVer != null) {
			if (resVer.size() > 1) {
				throw new StiQueryServiceException("Attualmente il sistema supporta il filtro su una versione del Code System.");
			} else if (resVer.size() == 1) {
				csVersion = StiServiceUtil.trimStr(resVer.iterator().next().getName());
			}
		}

		return csVersion;
	}

	private String extractCodeSystemName(final EntityDescriptionQuery query) {

		Set<TaggedCodeSystemRestriction> resCs = query.getRestrictions().getTaggedCodeSystems();

		String csName = null;
		if (resCs != null) {
			if (resCs.size() > 1) {
				throw new StiQueryServiceException("Attualmente il sistema supporta il filtro su un solo Code System.");
			} else if (resCs.size() == 1) {
				csName = StiServiceUtil.trimStr(resCs.iterator().next().getCodeSystem().getName());
			}
		}

		return csName;
	}

	private String getCodeSystemLastVersion(final String csName) {

		if (StiServiceUtil.isNull(csName)) {
			return null;
		}

		try {
			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
			GetCodeSystemLastVersion gCmd = new GetCodeSystemLastVersion(csName);

			CodeSystemVersion csVers = (CodeSystemVersion) hibUtil.executeByUser(gCmd, SessionUtil.getLoggedUser());

			String retVal = null;
			if (csVers != null) {
				retVal = StiServiceUtil.trimStr(csVers.getName());
			}

			return retVal;
		} catch (Exception qx) {
			throw new StiQueryServiceException("Errore durante l'execuzione della query.", qx);
		}
	}

	private String getCodeSystemNameByVersion(final String csVersion) {
		try {
			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();

			List<CodeSystemVersion> setVers = (List<CodeSystemVersion>) hibUtil.executeByUser(new GetCodeSystemVersionByName(csVersion), SessionUtil.getLoggedUser());

			if ((setVers == null) || (setVers.size() != 1)) {
				throw new StiHibernateException("Impossibile leggere i dati " + "della Versione Code System: " + csVersion);
			}

			CodeSystemVersion csVers = setVers.get(0);

			return csVers.getCodeSystem().getName();
		} catch (Exception qx) {
			throw new StiQueryServiceException("Errore durante l'execuzione della query.", qx);
		}
	}

	private String getCodeSystemTypeByName(final String csName) {
		try {
			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();

			List<CodeSystem> css = (List<CodeSystem>) hibUtil.executeByUser(new GetCodeSystemByName(csName), SessionUtil.getLoggedUser());
			String csType = null;
			if (null != css && !css.isEmpty()) {
				csType = css.get(0).getCodeSystemType();
			}
			return csType;
		} catch (Exception qx) {
			throw new StiQueryServiceException("Errore durante l'execuzione della query.", qx);
		}
	}
}
