package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConceptTranslation;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionEntityMembership;
import it.linksmt.cts2.plugin.sti.search.util.CommonFields;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;

import com.google.gson.JsonObject;

import edu.mayo.cts2.framework.model.core.ScopedEntityName;
import edu.mayo.cts2.framework.model.core.URIAndEntityName;
import edu.mayo.cts2.framework.model.util.ModelUtils;

public class GetParentList extends HibernateCommand {

	private CodeSystemConcept concept;
	private AbstractStiService service;

	public GetParentList(final CodeSystemConcept concept, final AbstractStiService service) {
		this.concept = concept;
		this.service = service;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public List<URIAndEntityName> execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		List<URIAndEntityName> retVal = new ArrayList<URIAndEntityName>();

		CodeSystemConcept parent = concept;
		while(parent != null) {
			parent = new GetParentConcept(parent).execute(session);

			if (parent == null) {
				break;
			}

			CodeSystemEntityVersion entity = parent.getCodeSystemEntityVersion();

			// Source Code System
			Set<CodeSystemVersionEntityMembership> memSetSrc = entity.getCodeSystemEntity()
					.getCodeSystemVersionEntityMemberships();

			if ((memSetSrc == null) || (memSetSrc.size() != 1)) {
				throw new StiHibernateException("Il sistema attualmente supporta "
						+ "una singola associazione tra la versione del CS e la Entity.");
			}

			CodeSystemVersion csVersSrc = memSetSrc.iterator().next().getCodeSystemVersion();
			String csNameSrc = csVersSrc.getCodeSystem().getName();

			String entityIdSrc = parent.getCode();
			String csVersionNameSrc = csVersSrc.getName();

			ScopedEntityName sNameSrc = ModelUtils
					.createScopedEntityName(entityIdSrc, csNameSrc);

			URIAndEntityName subjectRef = new URIAndEntityName();
			subjectRef.setNamespace(csNameSrc);
			subjectRef.setName(entityIdSrc);

			subjectRef.setUri(service.getUrlConstructor().createEntityUrl(sNameSrc));
			subjectRef.setHref(service.getUrlConstructor().createEntityUrl(
					csNameSrc, csVersionNameSrc, sNameSrc));

			String nameIt = "";
			String nameEn = StiServiceUtil.trimStr(parent.getTerm());

			Set<CodeSystemConceptTranslation> trSrc = parent.getCodeSystemConceptTranslations();
			if ((trSrc != null) && (trSrc.size() > 0)) {
				CodeSystemConceptTranslation translSrc = trSrc.iterator().next();
				nameIt = StiServiceUtil.trimStr(translSrc.getTerm());
			}

			if (StiServiceUtil.isNull(nameIt)){
				nameIt = nameEn;
			}
			if (StiServiceUtil.isNull(nameEn)){
				nameEn = nameIt;
			}
			
			//Replace the it name if it contains "capitolo"
			if(nameIt.toLowerCase().contains("capitolo")){
				for (CodeSystemConceptTranslation codeSystemConceptTranslation : trSrc) {
					nameIt = codeSystemConceptTranslation.getDescription();
				}
			}
			

			JsonObject desObj = new JsonObject();

			desObj.addProperty(CommonFields.NAME + "_it", nameIt);
			desObj.addProperty(CommonFields.NAME + "_en", nameEn);

			subjectRef.setDesignation(desObj.toString());
			retVal.add(subjectRef);
		}

		return retVal;
	}

}
