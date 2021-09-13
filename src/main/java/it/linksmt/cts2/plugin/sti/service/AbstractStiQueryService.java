package it.linksmt.cts2.plugin.sti.service;

import java.util.HashSet;
import java.util.Set;

import edu.mayo.cts2.framework.model.core.ComponentReference;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.BaseQueryService;

public abstract class AbstractStiQueryService
	extends AbstractStiService
	implements BaseQueryService {

	@Override
	public Set<? extends MatchAlgorithmReference> getSupportedMatchAlgorithms() {
		Set<MatchAlgorithmReference> returnSet = new HashSet<MatchAlgorithmReference>();

		returnSet.add(StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference());
		return returnSet;
	}

	@Override
	public Set<? extends ComponentReference> getSupportedSearchReferences() {
		Set<ComponentReference> returnSet = new HashSet<ComponentReference>();

		// returnSet.add(StandardModelAttributeReference.RESOURCE_NAME.getComponentReference());
		returnSet.add(StandardModelAttributeReference.RESOURCE_SYNOPSIS.getComponentReference());

		return returnSet;
	}

	@Override
	public Set<? extends ComponentReference> getSupportedSortReferences() {
		return null;
	}

	@Override
	public Set<PredicateReference> getKnownProperties() {
		return null;
	}
}
