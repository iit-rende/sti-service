package it.linksmt.cts2.plugin.sti.service.association;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.core.OpaqueData;
import edu.mayo.cts2.framework.model.core.Property;
import edu.mayo.cts2.framework.model.extension.LocalIdAssociation;
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest;
import edu.mayo.cts2.framework.service.profile.association.AssociationMaintenanceService;
import edu.mayo.cts2.framework.service.profile.association.name.AssociationReadId;
import it.linksmt.cts2.plugin.sti.db.commands.insert.InsertAssociation;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiQueryServiceException;
import it.linksmt.cts2.plugin.sti.service.util.SessionUtil;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

@Component
public class StiAssociationMaintenanceService
	extends AbstractStiService
	implements AssociationMaintenanceService {

	private static Logger log = Logger.getLogger(StiAssociationMaintenanceService.class);

	@Override
	public void updateChangeableMetadata(final AssociationReadId identifier, final UpdateChangeableMetadataRequest request) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateResource(final LocalIdAssociation resource) {
		throw new UnsupportedOperationException();
	}

	@Override
	public LocalIdAssociation createResource(final Association resource) {

		try {
			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();

			String sourceHref = resource.getSubject().getHref();
			String targetHref = resource.getPredicate().getHref();

			String sourceEntityId = resource.getSubject().getName();
			String targetEntityId = resource.getPredicate().getName();

			String forwardName = null;
			String reverseName = null;

			StiConstants.ASSOCIATION_KIND associationKind = null;

			Property[] assQualif = resource.getAssociationQualifier();
			if (assQualif != null) {
				for (int i = 0; i < assQualif.length; i++) {

					Property curProp = assQualif[i];
					if (StiServiceUtil.trimStr(curProp.getPredicate().getName())
							.equalsIgnoreCase("forwardName")) {

						OpaqueData dataVal = curProp.getValue()[0].getLiteral();
						forwardName = StiServiceUtil.trimStr(dataVal.getValue().getContent());
					}
					else if (StiServiceUtil.trimStr(curProp.getPredicate().getName())
							.equalsIgnoreCase("reverseName")) {

						OpaqueData dataVal = curProp.getValue()[0].getLiteral();
						reverseName = StiServiceUtil.trimStr(dataVal.getValue().getContent());
					}
					else if (StiServiceUtil.trimStr(curProp.getPredicate().getName())
							.equalsIgnoreCase("associationKind")) {

						OpaqueData dataVal = curProp.getValue()[0].getLiteral();
						String kindVal = StiServiceUtil.trimStr(dataVal.getValue().getContent());

						if (kindVal.equals("3")) {
							associationKind = StiConstants.ASSOCIATION_KIND.CROSS_MAPPING;
						}
						else if (kindVal.equals("4")) {
							associationKind = StiConstants.ASSOCIATION_KIND.LINK;
						}
					}
				}
			}

			InsertAssociation insCmd = new InsertAssociation(
					getCodeSystemVersionFromHref(sourceHref), sourceEntityId,
					getCodeSystemVersionFromHref(targetHref), targetEntityId,
					forwardName, reverseName,
					associationKind, this);

			Association assoc = (Association) hibUtil
					.executeByUser(insCmd, SessionUtil.getLoggedUser());

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
	public void deleteResource(final AssociationReadId identifier, final String changeSetUri) {
		throw new UnsupportedOperationException();
	}


	private String getCodeSystemVersionFromHref(final String valHref) {

		int versIdx = valHref.indexOf("/version/");
		String retVal = null;

		if (versIdx > 0) {
			retVal = valHref.substring(versIdx+9);

			int endIdx = retVal.indexOf("/");
			if (endIdx > 0) {
				retVal = retVal.substring(0, endIdx);
			}
		}

		return retVal;
	}

}
