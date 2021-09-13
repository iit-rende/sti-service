package it.linksmt.cts2.plugin.sti.service.mapping;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.map.MapCatalogEntryListEntry;
import edu.mayo.cts2.framework.model.map.MapCatalogEntrySummary;
import edu.mayo.cts2.framework.model.mapversion.MapVersion;
import edu.mayo.cts2.framework.service.profile.map.MapQuery;
import edu.mayo.cts2.framework.service.profile.map.MapQueryService;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetAllMapVersion;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.service.AbstractStiQueryService;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiQueryServiceException;
import it.linksmt.cts2.plugin.sti.service.util.SessionUtil;

@Component
public class StiMapQueryService
	extends AbstractStiQueryService
	implements MapQueryService {

	@Override
	public DirectoryResult<MapCatalogEntrySummary> getResourceSummaries(final MapQuery query, final SortCriteria sortCriteria,
			final Page page) {

		List<MapCatalogEntrySummary> searchRes = new ArrayList<MapCatalogEntrySummary>();

		try {
			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
			List<MapSetVersion> mapList = (List<MapSetVersion>) hibUtil.executeByUser(
					new GetAllMapVersion(), SessionUtil.getLoggedUser());

			for (int i = 0; i < mapList.size(); i++) {
				MapSetVersion curMap = mapList.get(i);

				MapCatalogEntrySummary sumMap = new MapCatalogEntrySummary();
				sumMap.setMapName(curMap.getFullname());

				searchRes.add(sumMap);
			}
		}
		catch(Exception qx) {
			throw new StiQueryServiceException(
					"Errore durante l'execuzione della query.", qx);
		}

		DirectoryResult<MapCatalogEntrySummary> retVal = new DirectoryResult<MapCatalogEntrySummary>(
				searchRes ,true);

		return retVal;
	}

	@Override
	public DirectoryResult<MapCatalogEntryListEntry> getResourceList(final MapQuery query, final SortCriteria sortCriteria,
			final Page page) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int count(final MapQuery query) {

		try {
			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
			List<MapVersion> mapList = (List<MapVersion>) hibUtil.executeByUser(
					new GetAllMapVersion(), SessionUtil.getLoggedUser());

			return mapList.size();
		}
		catch(Exception qx) {
			throw new StiQueryServiceException(
					"Errore durante l'execuzione della query.", qx);
		}
	}
}
