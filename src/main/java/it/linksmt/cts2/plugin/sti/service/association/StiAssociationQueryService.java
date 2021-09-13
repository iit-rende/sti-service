package it.linksmt.cts2.plugin.sti.service.association;

import java.util.List;

import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.association.AssociationDirectoryEntry;
import edu.mayo.cts2.framework.model.association.GraphNode;
import edu.mayo.cts2.framework.model.association.types.GraphDirection;
import edu.mayo.cts2.framework.model.association.types.GraphFocus;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.service.command.restriction.AssociationQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.profile.association.AssociationQuery;
import edu.mayo.cts2.framework.service.profile.association.AssociationQueryService;
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetAssociationsInMapSet;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetAssociationsToValidate;
import it.linksmt.cts2.plugin.sti.db.commands.search.SearchAssociations;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiQueryService;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.exception.StiQueryServiceException;
import it.linksmt.cts2.plugin.sti.service.util.SessionUtil;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

@Component
public class StiAssociationQueryService
	extends AbstractStiQueryService
	implements AssociationQueryService {

	@Override
	public DirectoryResult<AssociationDirectoryEntry> getResourceSummaries(final AssociationQuery query,
			final SortCriteria sortCriteria, final Page page) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DirectoryResult<Association> getResourceList(final AssociationQuery query, final SortCriteria sortCriteria, final Page page) {

		try {
			List<Association> searchRes = getAssociations(query);
			DirectoryResult<Association> retVal =
					new DirectoryResult<Association>(searchRes, true);

			return retVal;
		}
		catch(Exception qx) {
			throw new StiQueryServiceException(
					"Errore durante l'execuzione della query.", qx);
		}
	}

	@Override
	public int count(final AssociationQuery query) {
		try {
			List<Association> searchRes = getAssociations(query);
			return searchRes.size();
		}
		catch(Exception qx) {
			throw new StiQueryServiceException(
					"Errore durante l'execuzione della query.", qx);
		}
	}

	@Override
	public DirectoryResult<GraphNode> getAssociationGraph(final GraphFocus focusType, final EntityDescriptionReadId focusEntity,
			final GraphDirection direction, final long depth) {
		throw new UnsupportedOperationException();
	}

	private List<Association> getAssociations(final AssociationQuery query)
			throws StiHibernateException, StiAuthorizationException {

		AssociationQueryServiceRestrictions filters = query.getRestrictions();
		String codeSystemVersion = null;

		String sourceEntityId = null;
		String targetEntityId = null;
		String sourceOrTargetEntity = null;

		String mapSetVersion = query.getReadContext().getChangeSetContextUri();

		if (filters.getCodeSystemVersion() != null) {
			codeSystemVersion = StiServiceUtil.trimStr(
					filters.getCodeSystemVersion().getName());
		}
		if (filters.getSourceEntity() != null) {
			sourceEntityId = StiServiceUtil.trimStr(
					filters.getSourceEntity().getEntityName().getName());
		}
		if (filters.getPredicate() != null) {
			targetEntityId = StiServiceUtil.trimStr(
					filters.getPredicate().getEntityName().getName());
		}
		if (filters.getTargetEntity() != null) {
			targetEntityId = StiServiceUtil.trimStr(
					filters.getTargetEntity().getEntityName().getName());
		}
		if (filters.getSourceOrTargetEntity() != null) {
			sourceOrTargetEntity = StiServiceUtil.trimStr(
					filters.getSourceOrTargetEntity().getEntityName().getName());
		}

		HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
		List<Association> searchRes = null;

		if ("TO_VALIDATE".equalsIgnoreCase(sourceOrTargetEntity)) {
			searchRes = (List<Association>) hibUtil.executeByUser(
					new GetAssociationsToValidate(this),
					SessionUtil.getLoggedUser());
		}
		else if (!StiServiceUtil.isNull(mapSetVersion)) {
			searchRes = (List<Association>) hibUtil.executeByUser(
					new GetAssociationsInMapSet(mapSetVersion, this),
					SessionUtil.getLoggedUser());
		}
		else {
			searchRes = (List<Association>) hibUtil.executeByUser(
					new SearchAssociations(codeSystemVersion, sourceEntityId,
							targetEntityId, sourceOrTargetEntity, this),
					SessionUtil.getLoggedUser());
		}

		return searchRes;
	}
}
