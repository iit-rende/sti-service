package it.linksmt.cts2.plugin.sti.service.entitydescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.CodeSystemReference;
import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference;
import edu.mayo.cts2.framework.model.core.EntityReference;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.core.VersionTagReference;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.entity.EntityDescriptionBase;
import edu.mayo.cts2.framework.model.entity.EntityListEntry;
import edu.mayo.cts2.framework.model.entity.NamedEntityDescription;
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURI;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionReadService;
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetEntityDescriptionEntry;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiQueryServiceException;
import it.linksmt.cts2.plugin.sti.service.util.SessionUtil;

@Component
public class StiEntityDescriptionReadService
	extends AbstractStiService
	implements EntityDescriptionReadService {

	private static Logger log = Logger.getLogger(StiEntityDescriptionReadService.class);

	@Override
	public EntityDescription read(final EntityDescriptionReadId identifier, final ResolvedReadContext readContext) {
		try {
			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();

			String csVersion = identifier.getCodeSystemVersion().getName();
			GetEntityDescriptionEntry rCmd = new GetEntityDescriptionEntry(
					identifier.getEntityName().getName(), csVersion, this);

			EntityDescriptionBase entity = (NamedEntityDescription)
				hibUtil.executeByUser(rCmd, SessionUtil.getLoggedUser());

			return ModelUtils.toEntityDescription(entity);
		}
		catch(Exception qx) {
			throw new StiQueryServiceException(
					"Errore durante l'execuzione della query.", qx);
		}
	}

	@Override
	public boolean exists(final EntityDescriptionReadId identifier, final ResolvedReadContext readContext) {
		return (read(identifier, readContext) != null);
	}

	@Override
	public DirectoryResult<EntityListEntry> readEntityDescriptions(final EntityNameOrURI entityId, final SortCriteria sortCriteria,
			final ResolvedReadContext readContext, final Page page) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityReference availableDescriptions(final EntityNameOrURI entityId, final ResolvedReadContext readContext) {
		return null;
	}

	@Override
	public List<EntityListEntry> readEntityDescriptions(final EntityNameOrURI entityId, final ResolvedReadContext readContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<CodeSystemReference> getKnownCodeSystems() {
		// TODO
		return new ArrayList<CodeSystemReference>();
	}

	@Override
	public List<CodeSystemVersionReference> getKnownCodeSystemVersions() {
		// TODO
		return new ArrayList<CodeSystemVersionReference>();
	}

	@Override
	public List<VersionTagReference> getSupportedVersionTags() {
		//TODO not actually supported, but the framework won't marshall unless this is populated
		return Arrays.asList(new VersionTagReference[] {new VersionTagReference("CURRENT")});
	}

}
