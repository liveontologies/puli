package org.liveontologies.puli;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PrunedInferenceSet<C>
		extends DelegatingInferenceSet<C, InferenceSet<C>>
		implements InferenceSet<C>, Producer<Inference<C>> {

	private final Map<C, Inference<C>> expanded_ = new HashMap<C, Inference<C>>();

	public PrunedInferenceSet(InferenceSet<C> delegate, C goal,
			Set<C> asserted) {
		super(delegate);
		InferenceSet<C> extended = InferenceSets.addAssertedInferences(delegate,
				asserted);
		Set<C> essential = InferenceSets.getEssentialConclusions(extended,
				goal);
		InferenceSets.expand(essential, delegate, goal, this);
	}

	@Override
	public void produce(Inference<C> inf) {
		expanded_.put(inf.getConclusion(), inf);
	}

	@Override
	public Collection<? extends Inference<C>> getInferences(C conclusion) {
		Inference<C> inf = expanded_.get(conclusion);
		if (inf == null) {
			return super.getInferences(conclusion);
		}
		// else
		return Collections.singleton(inf);
	}

}
