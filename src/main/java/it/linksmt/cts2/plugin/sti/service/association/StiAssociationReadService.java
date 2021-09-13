package it.linksmt.cts2.plugin.sti.service.association;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.extension.LocalIdAssociation;
import edu.mayo.cts2.framework.service.profile.association.AssociationReadService;
import edu.mayo.cts2.framework.service.profile.association.name.AssociationReadId;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetAssociationById;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiQueryServiceException;
import it.linksmt.cts2.plugin.sti.service.util.SessionUtil;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

@Component
public class StiAssociationReadService
	extends AbstractStiService
	implements AssociationReadService {

	private static Logger log = Logger.getLogger(StiAssociationReadService.class);

	@Override
	public LocalIdAssociation read(final AssociationReadId identifier, final ResolvedReadContext readContext) {

		try {
			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();

			long assocId = -1;
			try {
				assocId = Long.parseLong(StiServiceUtil.trimStr(identifier.getName()));
			}
			catch(Exception ex){
				log.error("Impossibile leggere il valore per Id Associazione: " + identifier.getName());
			}

			Association assoc = (Association) hibUtil.executeByUser(
					new GetAssociationById(assocId, this), SessionUtil.getLoggedUser());

			if (assoc != null) {
				return new LocalIdAssociation(assoc);
			}
		}
		catch(Exception qx) {
			throw new StiQueryServiceException(
					"Errore durante l'execuzione della query.", qx);
		}

		return null;
	}

	@Override
	public boolean exists(final AssociationReadId identifier, final ResolvedReadContext readContext) {
		return (read(identifier, readContext) != null);
	}

	@Override
	public LocalIdAssociation readByExternalStatementId(final String exteralStatementId, final String scopingNamespaceName,
			final ResolvedReadContext readContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean existsByExternalStatementId(final String exteralStatementId, final String scopingNamespaceName,
			final ResolvedReadContext readContext) {
		throw new UnsupportedOperationException();
	}

}
