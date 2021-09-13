package it.linksmt.cts2.plugin.sti.service.codesystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntryListEntry;
import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntrySummary;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.service.profile.ResourceQuery;
import edu.mayo.cts2.framework.service.profile.codesystem.CodeSystemQueryService;
import it.linksmt.cts2.plugin.sti.db.commands.search.SearchCodeSystems;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.search.util.DbTransformUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiQueryService;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiQueryServiceException;
import it.linksmt.cts2.plugin.sti.service.util.SessionUtil;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

@Component
public class StiCodeSystemQueryService
	extends AbstractStiQueryService
	implements CodeSystemQueryService  {

	@Override
	public DirectoryResult<CodeSystemCatalogEntrySummary> getResourceSummaries(final ResourceQuery query,
			final SortCriteria sortCriteria, final Page page) {

		List<CodeSystem> resList = searchCodeSystems(query);
		List<CodeSystemCatalogEntrySummary> searchRes = new ArrayList<CodeSystemCatalogEntrySummary>();

		if (resList != null) {
			try {
				for (int i = 0; i < resList.size(); i++) {
					CodeSystem csCur = resList.get(i);
					if (csCur.getCurrentVersionId() != null) {
						searchRes.add(DbTransformUtil.codeSystemToCatalogEntry(
								csCur, this));
					}
				}
			}
			catch(Exception qx) {
				throw new StiQueryServiceException(
						"Errore durante l'execuzione della query.", qx);
			}
		}

		DirectoryResult<CodeSystemCatalogEntrySummary> retVal =
				new DirectoryResult<CodeSystemCatalogEntrySummary>(searchRes, true);

		return retVal;
	}

	@Override
	public DirectoryResult<CodeSystemCatalogEntryListEntry> getResourceList(final ResourceQuery query,
			final SortCriteria sortCriteria, final Page page) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int count(final ResourceQuery query) {
		List<CodeSystem> resList = searchCodeSystems(query);

		if (resList != null) {
			return resList.size();
		}

		return 0;
	}

	private static List<CodeSystem> searchCodeSystems(final ResourceQuery query) {

		Set<ResolvedFilter> filterComp = query.getFilterComponent();
		if ( (filterComp == null) || (filterComp.size() > 1) ) {
			throw new StiQueryServiceException("Idicare un valore come criterio di ricerca.");
		}

		String matchValueStr = null;
		if (filterComp.size() == 1) {
			matchValueStr = StiServiceUtil.trimStr(filterComp.iterator().next().getMatchValue());
		}

		try {
			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
			return (List<CodeSystem>)hibUtil.executeByUser(
					new SearchCodeSystems(matchValueStr, false, null), SessionUtil.getLoggedUser());
		}
		catch(Exception qx) {
			throw new StiQueryServiceException(
					"Errore durante l'execuzione della query.", qx);
		}
	}
}
