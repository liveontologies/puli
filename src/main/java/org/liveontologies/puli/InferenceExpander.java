package org.liveontologies.puli;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class InferenceExpander<C> implements Producer<Inference<C>> {

	private final Set<C> derivable_;

	private final InferenceSet<C> inferences_;

	private final ListMultimap<C, Inference<C>> watchInferences_ = ArrayListMultimap
			.create();
	private final ListMultimap<C, Integer> watchPositions_ = ArrayListMultimap
			.create();

	private final Queue<C> newlyDerived_ = new ArrayDeque<C>();

	private final Producer<Inference<C>> producer_;

	InferenceExpander(Set<C> derivable, InferenceSet<C> inferences, C goal,
			Producer<Inference<C>> producer) {
		this.inferences_ = inferences;
		this.derivable_ = derivable;
		this.producer_ = producer;
		process(goal);
	}

	public static <C> void expand(Set<C> derivable, InferenceSet<C> inferences,
			C goal, Producer<Inference<C>> producer) {
		new InferenceExpander<C>(derivable, inferences, goal, producer);
	}

	void process(C goal) {
		InferenceSets.unfoldRecursively(inferences_, goal, this);
	}

	@Override
	public void produce(Inference<C> inf) {
		List<? extends C> premises = inf.getPremises();
		for (int i = 0; i < premises.size(); i++) {
			C premise = premises.get(i);
			if (!derivable_.contains(premise)) {
				watchInferences_.put(premise, inf);
				watchPositions_.put(premise, i);
				return;
			}
		}
		// all premises are derived
		C conclusion = inf.getConclusion();
		if (derivable_.add(conclusion)) {
			producer_.produce(inf);
			newlyDerived_.add(conclusion);
			propagate();
		}
	}

	void propagate() {
		for (;;) {
			C next = newlyDerived_.poll();
			if (next == null) {
				return;
			}
			List<Inference<C>> watch = watchInferences_.removeAll(next);
			List<Integer> positions = watchPositions_.removeAll(next);
			for (int i = 0; i < watch.size(); i++) {
				Inference<C> inf = watch.get(i);
				int pos = positions.get(i);
				List<? extends C> premises = inf.getPremises();
				for (;;) {
					pos++;
					if (pos == premises.size()) {
						// all premises are derived
						C conclusion = inf.getConclusion();
						if (derivable_.add(conclusion)) {
							producer_.produce(inf);
							newlyDerived_.add(conclusion);
						}
						break;
					}
					// else
					C premise = premises.get(pos);
					if (derivable_.contains(premise)) {
						continue;
					}
					// else
					watchInferences_.put(premise, inf);
					watchPositions_.put(premise, pos);
					break;
				}
			}
		}

	}

}
