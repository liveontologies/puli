/*-
 * #%L
 * Proof Utility Library
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2017 Live Ontologies Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.liveontologies.puli.pinpointing;

import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.liveontologies.puli.Delegator;
import org.liveontologies.puli.Inference;
import org.liveontologies.puli.InferenceJustifier;
import org.liveontologies.puli.Proof;
import org.liveontologies.puli.collections.BloomTrieCollection2;
import org.liveontologies.puli.collections.Collection2;
import org.liveontologies.puli.pinpointing.MinimalSubsetEnumerator.Listener;
import org.liveontologies.puli.statistics.NestedStats;
import org.liveontologies.puli.statistics.ResetStats;
import org.liveontologies.puli.statistics.Stat;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

/**
 * Computing justifications by resolving inferences. An inference X can be
 * resolved with an inference Y if the conclusion of X is one of the premises of
 * Y; the resulting inference Z will have the conclusion of Y, all premises of X
 * and Y except for the resolved one and all justificaitons of X and Y.
 * 
 * @author Peter Skocovsky
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusion and premises used by the inferences
 * @param <A>
 *            the type of axioms used by the inferences
 */
public class ResolutionJustificationComputation<C, A>
		extends MinimalSubsetsFromProofs<C, A> {

	private static final ResolutionJustificationComputation.Factory<?, ?> FACTORY_ = new Factory<Object, Object>();

	@SuppressWarnings("unchecked")
	public static <C, A> Factory<C, A> getFactory() {
		return (Factory<C, A>) FACTORY_;
	}

	public enum SelectionType {
		TOP_DOWN, BOTTOM_UP, THRESHOLD
		// TODO: switch to class hierarchy to support threshold with parameter
	}

	private final SelectionType selectionType_;

	/**
	 * Conclusions for which computation of justifications has been initialized
	 */
	private final Set<C> initialized_ = new HashSet<C>();

	private final IdMap<C> conclusionIds_ = HashIdMap.create();

	private final IdMap<A> axiomIds_ = HashIdMap.create();

	/**
	 * a structure used to check inferences for minimality; an inference is
	 * minimal if there was no other inference with the same conclusion, subset
	 * of premises and subset of justirication produced
	 */
	private final Map<Integer, Collection2<DerivedInference>> minimalInferencesByConclusionIds_ = new HashMap<Integer, Collection2<DerivedInference>>();

	private final ListMultimap<Integer, DerivedInference>
	// inferences whose conclusions are selected, indexed by this conclusion
	inferencesBySelectedConclusionIds_ = ArrayListMultimap.create(),
			// inferences whose premise is selected, indexed by this premise
			inferencesBySelectedPremiseIds_ = ArrayListMultimap.create();

	/**
	 * inferences that are not necessary for computing the justifications for
	 * the current query; these are (possibly minimal) inferences whose
	 * justification is a superset of a justification for the query
	 */
	private Queue<DerivedInference> blockedInferences_ = new ArrayDeque<DerivedInference>();

	// Statistics
	private int producedInferenceCount_ = 0, minimalInferenceCount_ = 0;

	private ResolutionJustificationComputation(final Proof<C> proof,
			final InferenceJustifier<C, ? extends Set<? extends A>> justifier,
			final InterruptMonitor monitor, final SelectionType selectionType) {
		super(proof, justifier, monitor);
		this.selectionType_ = selectionType;
	}

	private Collection2<DerivedInference> getMinimalInferences(
			Integer conclusionId) {
		Collection2<DerivedInference> result = minimalInferencesByConclusionIds_
				.get(conclusionId);
		if (result == null) {
			result = new BloomTrieCollection2<DerivedInference>();
			minimalInferencesByConclusionIds_.put(conclusionId, result);
		}
		return result;
	}

	@Override
	public MinimalSubsetEnumerator<A> newEnumerator(C query) {
		return new JustificationEnumerator(query);
	}

	@Stat
	public int nProducedInferences() {
		return producedInferenceCount_;
	}

	@Stat
	public int nMinimalInferences() {
		return minimalInferenceCount_;
	}

	@ResetStats
	public void resetStats() {
		producedInferenceCount_ = 0;
		minimalInferenceCount_ = 0;
	}

	@NestedStats
	public static Class<?> getNestedStats() {
		return BloomTrieCollection2.class;
	}

	static int[] getIds(Collection<? extends Integer> set) {
		return SortedIdSet.getIds(set);
	}

	Set<A> getJustification(int[] ids) {
		return new SortedIdSet<A>(ids, axiomIds_);
	}

	/**
	 * A derived inference obtained from either original inferences or
	 * resolution between two inferences on the conclusion and a premise.
	 * 
	 * @author Peter Skocovsky
	 * @author Yevgeny Kazakov
	 */
	static class DerivedInference extends AbstractSet<DerivedInferenceMember> {

		private final int conclusionId_;
		private final int[] premiseIds_;
		private final int[] justificationIds_;
		/**
		 * {@code true} if the inference was checked for minimality
		 */
		boolean isMinimal_ = false;

		protected DerivedInference(final int conclusionId,
				final int[] premiseIds, final int[] justificationIds) {
			this.conclusionId_ = conclusionId;
			this.premiseIds_ = premiseIds;
			this.justificationIds_ = justificationIds;
		}

		public Set<Integer> getPremises() {
			return new SortedIntSet(premiseIds_);
		}

		public Set<Integer> getJustification() {
			return new SortedIntSet(justificationIds_);
		}

		public boolean isATautology() {
			return Arrays.binarySearch(premiseIds_, conclusionId_) >= 0;
		}

		@Override
		public Iterator<DerivedInferenceMember> iterator() {
			return Iterators.<DerivedInferenceMember> concat(
					Iterators.singletonIterator(new Conclusion(conclusionId_)),
					Iterators.transform(Ints.asList(premiseIds_).iterator(),
							new Function<Integer, Premise>() {
								@Override
								public Premise apply(Integer id) {
									return new Premise(id);
								}

							}),
					Iterators.transform(
							Ints.asList(justificationIds_).iterator(),
							new Function<Integer, Axiom>() {

								@Override
								public Axiom apply(Integer id) {
									return new Axiom(id);
								}

							}));
		}

		@Override
		public boolean containsAll(final Collection<?> c) {
			if (c instanceof ResolutionJustificationComputation.DerivedInference) {
				final DerivedInference other = (DerivedInference) c;
				return conclusionId_ == other.conclusionId_
						&& SortedIdSet.containsAll(premiseIds_,
								other.premiseIds_)
						&& SortedIdSet.containsAll(justificationIds_,
								other.justificationIds_);
			}
			// else
			return super.containsAll(c);
		}

		private boolean contains(DerivedInferenceMember other) {
			return other.accept(new DerivedInferenceMember.Visitor<Boolean>() {

				@Override
				public Boolean visit(Axiom axiom) {
					return Arrays.binarySearch(justificationIds_,
							axiom.getDelegate()) >= 0;
				}

				@Override
				public Boolean visit(Conclusion conclusion) {
					return conclusionId_ == conclusion.getDelegate();
				}

				@Override
				public Boolean visit(Premise premise) {
					return Arrays.binarySearch(premiseIds_,
							premise.getDelegate()) >= 0;
				}

			});

		}

		@Override
		public boolean contains(final Object o) {
			if (o instanceof DerivedInferenceMember) {
				return contains((DerivedInferenceMember) o);
			}
			// else
			return false;
		}

		@Override
		public int size() {
			return getPremises().size() + getJustification().size() + 1;
		}

		@Override
		public String toString() {
			return String.valueOf(conclusionId_) + " -| "
					+ Arrays.toString(premiseIds_) + ": "
					+ Arrays.toString(justificationIds_);
		}

	}

	interface DerivedInferenceMember {

		<O> O accept(Visitor<O> visitor);

		interface Visitor<O> {

			O visit(Axiom axiom);

			O visit(Conclusion conclusion);

			O visit(Premise premise);
		}

	}

	static final class Axiom extends Delegator<Integer>
			implements DerivedInferenceMember {

		public Axiom(Integer id) {
			super(id);
		}

		@Override
		public <O> O accept(DerivedInferenceMember.Visitor<O> visitor) {
			return visitor.visit(this);
		}

	}

	static final class Conclusion extends Delegator<Integer>
			implements DerivedInferenceMember {

		public Conclusion(Integer id) {
			super(id);
		}

		@Override
		public <O> O accept(DerivedInferenceMember.Visitor<O> visitor) {
			return visitor.visit(this);
		}

	}

	static final class Premise extends Delegator<Integer>
			implements DerivedInferenceMember {

		public Premise(Integer id) {
			super(id);
		}

		@Override
		public <O> O accept(DerivedInferenceMember.Visitor<O> visitor) {
			return visitor.visit(this);
		}

	}

	class JustificationEnumerator extends AbstractMinimalSubsetEnumerator<A> {

		/**
		 * the conclusion for which to enumerate justifications
		 */
		private final C query_;

		public JustificationEnumerator(C query) {
			this.query_ = query;
		}

		@Override
		public void enumerate(final Listener<A> listener,
				final PriorityComparator<? super Set<A>, ?> priorityComparator) {
			Preconditions.checkNotNull(listener);
			if (priorityComparator == null) {
				enumerate(listener);
				return;
			}
			// else
			JustificationProcessor<?> p = createProcessor(listener,
					priorityComparator);
			p.initialize();
			p.unblockJobs();
			p.changeSelection();
			p.process();
		}

		<P> JustificationProcessor<P> createProcessor(Listener<A> listener,
				PriorityComparator<? super Set<A>, P> priorityComparator) {
			return new JustificationProcessor<P>(query_, listener,
					priorityComparator);
		}

	}

	class JustificationProcessor<P> {

		/**
		 * the conclusion for which to enumerate justifications
		 */
		private final C query_;

		private final int queryId_;

		/**
		 * a function used for selecting conclusions in inferences on which to
		 * resolve
		 */
		private final SelectionFunction selectionFunction_;

		/**
		 * to check minimality of justifications
		 */
		private final Collection2<Set<Integer>> minimalJustifications_ = new BloomTrieCollection2<Set<Integer>>();

		/**
		 * a temporary queue used to initialize computation of justifications
		 * for conclusions that are not yet {@link #initialized_}
		 */
		private final Queue<C> toInitialize_ = new ArrayDeque<C>();

		/**
		 * the listener through which to report the justifications
		 */
		private Listener<A> listener_;

		private final PriorityComparator<? super Set<A>, P> priorityComparator_;

		/**
		 * newly computed inferences to be resolved upon
		 */
		private final Queue<UnprocessedInference<P>> unprocessedInferences_;

		private final InferenceProcessor<P> resolver_;

		JustificationProcessor(C query, final Listener<A> listener,
				PriorityComparator<? super Set<A>, P> priorityComparator) {
			this.query_ = query;
			this.queryId_ = conclusionIds_.getId(query);
			this.priorityComparator_ = priorityComparator;
			this.selectionFunction_ = getSelectionFunction(selectionType_);
			this.listener_ = listener;
			this.resolver_ = new InferenceProcessor<P>(priorityComparator);
			this.unprocessedInferences_ = new PriorityQueue<UnprocessedInference<P>>(
					256,
					new UnprocessedInferenceCompatator<P>(priorityComparator));
		}

		Proof<C> getProof() {
			return ResolutionJustificationComputation.this.getProof();
		}

		IdMap<C> getConclusionIds() {
			return conclusionIds_;
		}

		private void toInitialize(C conclusion) {
			if (initialized_.add(conclusion)) {
				toInitialize_.add(conclusion);
			}
		}

		private void block(DerivedInference inf) {
			blockedInferences_.add(inf);
		}

		private void initialize() {
			toInitialize(query_);
			for (;;) {
				C next = toInitialize_.poll();
				if (next == null) {
					return;
				}
				for (final Inference<C> inf : getInferences(next)) {
					produce(newDerivedInference(inf, getInferenceJustifier()));
					for (C premise : inf.getPremises()) {
						toInitialize(premise);
					}
				}
			}
		}

		private void unblockJobs() {
			for (;;) {
				DerivedInference inf = blockedInferences_.poll();
				if (inf == null) {
					return;
				}
				// else
				produce(newDerivedInference(inf));
			}
		}

		private void changeSelection() {
			// selection for inferences with selected query must change
			for (DerivedInference inf : inferencesBySelectedConclusionIds_
					.removeAll(query_)) {
				produce(newDerivedInference(inf));
			}
		}

		private void process() {
			for (;;) {
				if (isInterrupted()) {
					break;
				}
				UnprocessedInference<P> next = unprocessedInferences_.poll();
				if (next == null) {
					break;
				}
				DerivedInference inf = next.accept(resolver_);
				if (!minimalJustifications_.isMinimal(inf.getJustification())) {
					block(inf);
					continue;
				}
				// else
				if (inf.premiseIds_.length == 0
						&& queryId_ == inf.conclusionId_) {
					minimalJustifications_.add(inf.getJustification());
					listener_.newMinimalSubset(
							getJustification(inf.justificationIds_));
					block(inf);
					continue;
				}
				// else
				if (!inf.isMinimal_) {
					Collection2<DerivedInference> minimalInferences = getMinimalInferences(
							inf.conclusionId_);
					if (!minimalInferences.isMinimal(inf)) {
						continue;
					}
					// else
					inf.isMinimal_ = true;
					minimalInferences.add(inf);
					minimalInferenceCount_++;
				}
				Integer selected = selectionFunction_.getResolvingAtomId(inf);
				if (selected == null) {
					// resolve on the conclusions
					selected = inf.conclusionId_;
					if (query_.equals(selected)) {
						throw new RuntimeException(
								"Goal conclusion cannot be selected if the inference has premises: "
										+ inf);
					}
					inferencesBySelectedConclusionIds_.put(selected, inf);
					for (DerivedInference other : inferencesBySelectedPremiseIds_
							.get(selected)) {
						produce(newResolvent(inf, other));
					}
				} else {
					// resolve on the selected premise
					inferencesBySelectedPremiseIds_.put(selected, inf);
					for (DerivedInference other : inferencesBySelectedConclusionIds_
							.get(selected)) {
						produce(newResolvent(other, inf));
					}
				}
			}

		}

		private void produce(final UnprocessedInference<P> resolvent) {
			if (resolvent.isATautology()) {
				// skip tautologies
				return;
			}
			producedInferenceCount_++;
			unprocessedInferences_.add(resolvent);
		}

		UnprocessedInference<P> newDerivedInference(
				DerivedInference inference) {
			return new InitialInference<P>(inference.conclusionId_,
					inference.premiseIds_, inference.justificationIds_,
					priorityComparator_.getPriority(
							getJustification(inference.justificationIds_)),
					inference.isMinimal_);
		}

		UnprocessedInference<P> newDerivedInference(Inference<C> inference,
				InferenceJustifier<C, ? extends Set<? extends A>> justifier) {
			int[] justificationIds = getAxiomIds(
					justifier.getJustification(inference));
			return new InitialInference<P>(
					conclusionIds_.getId(inference.getConclusion()),
					getConclusionIds(inference.getPremises()), justificationIds,
					priorityComparator_
							.getPriority(getJustification(justificationIds)));
		}

		UnprocessedInference<P> newResolvent(
				final DerivedInference firstInference,
				final DerivedInference secondInference) {
			return new Resolvent<P>(firstInference, secondInference,
					priorityComparator_.getPriority(getJustification(
							SortedIdSet.union(firstInference.justificationIds_,
									secondInference.justificationIds_))));
		}

		int[] getConclusionIds(Collection<? extends C> conclusions) {
			return SortedIdSet.getIds(conclusions, conclusionIds_);
		}

		int[] getAxiomIds(Collection<? extends A> axioms) {
			return SortedIdSet.getIds(axioms, axiomIds_);
		}

		SelectionFunction getSelectionFunction(SelectionType type) {
			switch (type) {
			case BOTTOM_UP:
				return new SelectionFunction() {

					@Override
					public Integer getResolvingAtomId(
							DerivedInference inference) {
						// select the premise that is derived by the fewest
						// inferences;
						// if there are no premises, select the conclusion
						Integer result = null;
						int minInferenceCount = Integer.MAX_VALUE;
						for (int premiseId : inference.premiseIds_) {
							int inferenceCount = getProof().getInferences(
									conclusionIds_.getElement(premiseId))
									.size();
							if (inferenceCount < minInferenceCount) {
								result = premiseId;
								minInferenceCount = inferenceCount;
							}
						}
						return result;
					}
				};
			case THRESHOLD:
				return new SelectionFunction() {

					static final int THRESHOLD_ = 2;

					@Override
					public Integer getResolvingAtomId(
							DerivedInference inference) {
						// select the premise derived by the fewest inferences
						// unless the number of such inferences is larger than
						// the give threshold and the conclusion is not the
						// query; in this case select the conclusion
						int minInferenceCount = Integer.MAX_VALUE;
						Integer result = null;
						for (int premiseId : inference.premiseIds_) {
							int inferenceCount = getProof().getInferences(
									conclusionIds_.getElement(premiseId))
									.size();
							if (inferenceCount < minInferenceCount) {
								result = premiseId;
								minInferenceCount = inferenceCount;
							}
						}
						if (minInferenceCount > THRESHOLD_
								&& queryId_ != inference.conclusionId_) {
							// resolve on the conclusion
							result = null;
						}
						return result;
					}

				};

			case TOP_DOWN:
				return new SelectionFunction() {

					@Override
					public Integer getResolvingAtomId(
							DerivedInference inference) {
						// select the conclusion, unless it is the query
						// conclusion and there are premises, in which case
						// select the premise derived by the fewest inferences
						Integer result = null;
						if (queryId_ == inference.conclusionId_) {
							int minInferenceCount = Integer.MAX_VALUE;
							for (int premiseId : inference.premiseIds_) {
								int inferenceCount = getProof().getInferences(
										conclusionIds_.getElement(premiseId))
										.size();
								if (inferenceCount < minInferenceCount) {
									result = premiseId;
									minInferenceCount = inferenceCount;
								}
							}
						}
						return result;
					}
				};

			default:
				throw new RuntimeException(
						"Unsupported selection type: " + type);
			}
		}

	}

	/**
	 * An object representing unprocessed {@link DerivedInference}
	 * 
	 * @author Yevgeny Kazakov
	 *
	 * @param <P>
	 */
	interface UnprocessedInference<P> {

		/**
		 * @return the priority in which this inference should be processed
		 */
		P getPriority();

		/**
		 * @return the number of premises of for the inference represented by
		 *         this element
		 */
		int getPremiseCount();

		/**
		 * @return {@code true} if the inference represented by this element is
		 *         a tautology, i.e. its conclusion is one of the premises.
		 */
		boolean isATautology();

		<O> O accept(UnprocessedInference.Visitor<P, O> visitor);

		interface Visitor<P, O> {

			O visit(InitialInference<P> inference);

			O visit(Resolvent<P> inference);

		}

	}

	/**
	 * An {@link UnprocessedInference} representing the initial inference, from
	 * which the resolution computation starts
	 * 
	 * @author Yevgeny Kazakov
	 *
	 * @param <P>
	 */
	static class InitialInference<P> extends DerivedInference
			implements UnprocessedInference<P> {

		private final P priority_;

		private InitialInference(int conclusionId, int[] premiseIds,
				int[] justificationIds, P priority) {
			super(conclusionId, premiseIds, justificationIds);
			this.priority_ = priority;
		}

		private InitialInference(int conclusionId, int[] premiseIds,
				int[] justificationIds, P priority, boolean isMinimal) {
			this(conclusionId, premiseIds, justificationIds, priority);
			this.isMinimal_ = isMinimal;
		}

		@Override
		public int getPremiseCount() {
			return getPremises().size();
		}

		@Override
		public P getPriority() {
			return priority_;
		}

		@Override
		public <O> O accept(UnprocessedInference.Visitor<P, O> visitor) {
			return visitor.visit(this);
		}

	}

	/**
	 * The result of resolution applied to two {@link DerivedInference}s. The
	 * resulting inference is computed only when this
	 * {@link UnprocessedInference} is processed to prevent unnecessary memory
	 * consumption when this object is stored in the queue.
	 * 
	 * @author Yevgeny Kazakov
	 *
	 */
	static class Resolvent<P> implements UnprocessedInference<P> {

		private final DerivedInference firstInference_, secondInference_;

		private final P priority_;

		private final int premiseCount_;

		Resolvent(DerivedInference firstInference,
				DerivedInference secondInference, P priority) {
			if (firstInference.isATautology()
					|| secondInference.isATautology()) {
				throw new IllegalArgumentException(
						"Cannot resolve on tautologies!");
			}
			this.firstInference_ = firstInference;
			this.secondInference_ = secondInference;
			this.priority_ = priority;
			this.premiseCount_ = Sets.union(firstInference_.getPremises(),
					secondInference_.getPremises()).size() - 1;
		}

		@Override
		public int getPremiseCount() {
			return premiseCount_;
		}

		@Override
		public boolean isATautology() {
			// correct when the second inference is not a tautology
			return Arrays.binarySearch(firstInference_.premiseIds_,
					secondInference_.conclusionId_) >= 0;
		}

		@Override
		public P getPriority() {
			return priority_;
		}

		@Override
		public <O> O accept(UnprocessedInference.Visitor<P, O> visitor) {
			return visitor.visit(this);
		}

	}

	static class UnprocessedInferenceCompatator<P>
			implements Comparator<UnprocessedInference<P>> {

		private final Comparator<P> priorityComparator_;

		UnprocessedInferenceCompatator(Comparator<P> priorityComparator) {
			this.priorityComparator_ = priorityComparator;
		}

		@Override
		public int compare(UnprocessedInference<P> first,
				UnprocessedInference<P> second) {
			final int result = priorityComparator_.compare(first.getPriority(),
					second.getPriority());
			if (result != 0) {
				return result;
			}
			// else
			final int firstPremiseCount = first.getPremiseCount();
			final int secondPremiseCount = second.getPremiseCount();
			return (firstPremiseCount < secondPremiseCount) ? -1
					: ((firstPremiseCount == secondPremiseCount) ? 0 : 1);
		}

	};

	/**
	 * Converts {@link UnprocessedInference}s to {@link DerivedInference}s
	 * 
	 * @author Yevgeny Kazakov
	 *
	 * @param <P>
	 */
	class InferenceProcessor<P>
			implements UnprocessedInference.Visitor<P, DerivedInference> {

		private final PriorityComparator<? super Set<A>, P> priorityComparator_;

		InferenceProcessor(
				PriorityComparator<? super Set<A>, P> priorityComparator) {
			this.priorityComparator_ = priorityComparator;
		}

		@Override
		public DerivedInference visit(InitialInference<P> inference) {
			return inference;
		}

		@Override
		public DerivedInference visit(Resolvent<P> inference) {
			int[] newPremiseIds;
			DerivedInference first = inference.firstInference_;
			DerivedInference second = inference.secondInference_;
			if (second.getPremises().size() == 1) {
				newPremiseIds = first.premiseIds_;
			} else {
				newPremiseIds = getIds(Sets.union(first.getPremises(),
						Sets.difference(second.getPremises(),
								Collections.singleton(first.conclusionId_))));
			}
			int[] newJustificationIds = SortedIdSet
					.union(first.justificationIds_, second.justificationIds_);
			return new InitialInference<P>(second.conclusionId_, newPremiseIds,
					newJustificationIds, priorityComparator_.getPriority(
							getJustification(newJustificationIds)));
		}

	}

	public interface SelectionFunction {

		/**
		 * @param inference
		 * @return the id of the premise of the inference on which the
		 *         resolution rule should be applied or {@code null} if the
		 *         resolution rule should be applied on the conclusion of the
		 *         inference
		 */
		Integer getResolvingAtomId(DerivedInference inference);

	}

	/**
	 * The factory for creating computations
	 * 
	 * @author Peter Skocovsky
	 *
	 * @param <C>
	 *            the type of conclusion and premises used by the inferences
	 * @param <A>
	 *            the type of axioms used by the inferences
	 */
	public static class Factory<C, A>
			implements MinimalSubsetsFromProofs.Factory<C, A> {

		@Override
		public MinimalSubsetEnumerator.Factory<C, A> create(
				final Proof<C> proof,
				final InferenceJustifier<C, ? extends Set<? extends A>> justifier,
				final InterruptMonitor monitor) {
			return create(proof, justifier, monitor, SelectionType.THRESHOLD);
		}

		public MinimalSubsetEnumerator.Factory<C, A> create(
				final Proof<C> proof,
				final InferenceJustifier<C, ? extends Set<? extends A>> justifier,
				final InterruptMonitor monitor, final SelectionType selection) {
			return new ResolutionJustificationComputation<C, A>(proof,
					justifier, monitor, selection);
		}

	}

}
