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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.liveontologies.puli.Delegator;
import org.liveontologies.puli.Inference;
import org.liveontologies.puli.InferenceJustifier;
import org.liveontologies.puli.Proof;
import org.liveontologies.puli.collections.BloomTrieCollection2;
import org.liveontologies.puli.collections.Collection2;
import org.liveontologies.puli.statistics.NestedStats;
import org.liveontologies.puli.statistics.ResetStats;
import org.liveontologies.puli.statistics.Stat;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

/**
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusions used in inferences
 * @param <I>
 *            the type of inferences used in the proof
 * @param <A>
 *            the type of axioms used by the inferences
 */
public class TopDownRepairComputation<C, I extends Inference<? extends C>, A>
		extends MinimalSubsetsFromProofs<C, I, A> {

	private static final TopDownRepairComputation.Factory<?, ?, ?> FACTORY_ = new Factory<Object, Inference<?>, Object>();

	@SuppressWarnings("unchecked")
	public static <C, I extends Inference<? extends C>, A> MinimalSubsetsFromProofs.Factory<C, I, A> getFactory() {
		return (Factory<C, I, A>) FACTORY_;
	}

	// Statistics
	private int producedJobsCount_ = 0;

	private TopDownRepairComputation(final Proof<? extends I> proof,
			final InferenceJustifier<? super I, ? extends Set<? extends A>> justifier,
			final InterruptMonitor monitor) {
		super(proof, justifier, monitor);
	}

	@Override
	public MinimalSubsetEnumerator<A> newEnumerator(final Object query) {
		return new Enumerator(query);
	}

	private class Enumerator extends AbstractMinimalSubsetEnumerator<A> {

		private final Object query_;

		/**
		 * jobs to be processed
		 */
		private Queue<JobFactory<I, A, ?>.Job> toDoJobs_;

		/**
		 * Used to collect the result and prune jobs
		 */
		private final Collection2<Set<A>> minimalRepairs_ = new BloomTrieCollection2<Set<A>>();

		/**
		 * Used to filter out redundant jobs
		 */
		private final Collection2<JobFactory<I, A, ?>.Job> minimalJobs_ = new BloomTrieCollection2<JobFactory<I, A, ?>.Job>();

		private Listener<A> listener_ = null;

		private JobFactory<I, A, ?> jobFactory_ = null;

		Enumerator(final Object query) {
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
			this.listener_ = listener;
			this.jobFactory_ = JobFactory.create(getProof(),
					getInferenceJustifier(), priorityComparator);
			this.toDoJobs_ = new PriorityQueue<JobFactory<I, A, ?>.Job>();
			this.minimalRepairs_.clear();

			initialize(query_);
			process();

			this.listener_ = null;
		}

		private void initialize(final Object goal) {
			produce(jobFactory_.newJob(goal));
		}

		private void process() {
			for (;;) {
				if (isInterrupted()) {
					break;
				}
				final JobFactory<I, A, ?>.Job job = toDoJobs_.poll();
				if (job == null) {
					break;
				}
				// else
				if (!minimalRepairs_.isMinimal(job.repair_)) {
					continue;
				}
				// else
				if (!minimalJobs_.isMinimal(job)) {
					continue;
				}
				// else
				minimalJobs_.add(job);
				final I nextToBreak = chooseToBreak(job.toBreak_);
				if (nextToBreak == null) {
					minimalRepairs_.add(job.repair_);
					if (listener_ != null) {
						listener_.newMinimalSubset(job.repair_);
					}
					continue;
				}
				for (Object premise : nextToBreak.getPremises()) {
					produce(jobFactory_.doBreak(job.repair_, job.toBreak_,
							job.broken_, premise));
				}
				for (A axiom : getJustification(nextToBreak)) {
					produce(jobFactory_.repair(job.repair_, job.toBreak_,
							job.broken_, axiom));
				}
			}
		}

		private I chooseToBreak(final Collection<I> inferences) {
			// select the smallest conclusion according to the comparator
			I result = null;
			for (I inf : inferences) {
				if (result == null
						|| inferenceComparator.compare(inf, result) < 0) {
					result = inf;
				}
			}
			return result;
		}

		private void produce(final JobFactory<I, A, ?>.Job job) {
			producedJobsCount_++;
			toDoJobs_.add(job);
		}

	}

	private static class JobFactory<I extends Inference<?>, A, P> {

		private final Proof<? extends I> proof_;
		private final InferenceJustifier<? super I, ? extends Set<? extends A>> justifier_;
		private final PriorityComparator<? super Set<A>, P> priorityComparator_;

		public JobFactory(final Proof<? extends I> proof,
				final InferenceJustifier<? super I, ? extends Set<? extends A>> justifier,
				final PriorityComparator<? super Set<A>, P> priorityComparator) {
			this.proof_ = proof;
			this.justifier_ = justifier;
			this.priorityComparator_ = priorityComparator;
		}

		public static <I extends Inference<?>, A, P> JobFactory<I, A, P> create(
				final Proof<? extends I> proof,
				final InferenceJustifier<? super I, ? extends Set<? extends A>> justifier,
				final PriorityComparator<? super Set<A>, P> priorityComparator) {
			return new JobFactory<I, A, P>(proof, justifier,
					priorityComparator);
		}

		public Job newJob(final Object conclusion) {
			return doBreak(Collections.<A> emptySet(),
					Collections.<I> emptySet(), Collections.<Object> emptySet(),
					conclusion);
		}

		public Job doBreak(final Set<A> repair, final Collection<I> toBreak,
				final Set<Object> broken, final Object conclusion) {

			final Set<A> newRepair = repair.isEmpty() ? new HashSet<A>(1)
					: new HashSet<A>(repair);
			final Set<I> newToBreak = toBreak.isEmpty() ? new HashSet<I>(3)
					: new HashSet<I>(toBreak.size());
			final Set<Object> newBroken = broken.isEmpty()
					? new HashSet<Object>(1)
					: new HashSet<Object>(broken);

			newBroken.add(conclusion);
			for (final I inf : toBreak) {
				if (!inf.getPremises().contains(conclusion)) {
					newToBreak.add(inf);
				}
			}
			infLoop: for (final I inf : proof_.getInferences(conclusion)) {
				for (final Object premise : inf.getPremises()) {
					if (broken.contains(premise)) {
						continue infLoop;
					}
				}
				for (final A axiom : justifier_.getJustification(inf)) {
					if (repair.contains(axiom)) {
						continue infLoop;
					}
				}
				newToBreak.add(inf);
			}
			return new Job(newRepair, newToBreak, newBroken,
					priorityComparator_.getPriority(newRepair));
		}

		public Job repair(final Set<A> repair, final Collection<I> toBreak,
				final Set<Object> broken, final A axiom) {

			final Set<A> newRepair = new HashSet<A>(repair);
			final Set<I> newToBreak = new HashSet<I>(toBreak.size());
			final Set<Object> newBroken = new HashSet<Object>(broken);

			newRepair.add(axiom);
			for (final I inf : toBreak) {
				if (!justifier_.getJustification(inf).contains(axiom)) {
					newToBreak.add(inf);
				}
			}
			return new Job(newRepair, newToBreak, newBroken,
					priorityComparator_.getPriority(newRepair));
		}

		/**
		 * A simple state for computing a repair;
		 * 
		 * @author Peter Skocovsky
		 * @author Yevgeny Kazakov
		 */
		public class Job extends AbstractSet<JobMember<I, A>>
				implements Comparable<Job> {

			private final Set<A> repair_;
			private final Set<I> toBreak_;
			/**
			 * the cached set of conclusions not derivable without using
			 * {@link #repair_} and {@link #toBreak_}
			 */
			private final Set<Object> broken_;
			private final P priority_;

			private Job(final Set<A> repair, final Set<I> toBreak,
					final Set<Object> broken, final P priority) {
				this.repair_ = repair;
				this.toBreak_ = toBreak;
				this.broken_ = broken;
				this.priority_ = priority;
			}

			@Override
			public boolean containsAll(final Collection<?> c) {
				if (c instanceof JobFactory<?, ?, ?>.Job) {
					final JobFactory<?, ?, ?>.Job other = (JobFactory<?, ?, ?>.Job) c;
					return repair_.containsAll(other.repair_)
							&& toBreak_.containsAll(other.toBreak_);
				}
				// else
				return super.containsAll(c);
			}

			@Override
			public String toString() {
				return repair_.toString() + "; " + broken_.toString() + "; "
						+ toBreak_.toString();
			}

			@Override
			public Iterator<JobMember<I, A>> iterator() {
				return Iterators.<JobMember<I, A>> concat(Iterators.transform(
						repair_.iterator(), new Function<A, Axiom<I, A>>() {

							@Override
							public Axiom<I, A> apply(final A axiom) {
								return new Axiom<I, A>(axiom);
							}

						}), Iterators.transform(toBreak_.iterator(),
								new Function<I, Inf<I, A>>() {

									@Override
									public Inf<I, A> apply(I inf) {
										return new Inf<I, A>(inf);
									}

								}));
			}

			@Override
			public int size() {
				return repair_.size() + toBreak_.size();
			}

			@Override
			public int compareTo(final Job other) {
				final int result = priorityComparator_.compare(priority_,
						other.priority_);
				if (result != 0) {
					return result;
				}
				// else
				return toBreak_.size() - other.toBreak_.size();
			}

		}

	}

	@Stat
	public int nProducedJobs() {
		return producedJobsCount_;
	}

	@ResetStats
	public void resetStats() {
		producedJobsCount_ = 0;
	}

	@NestedStats
	public static Class<?> getNestedStats() {
		return BloomTrieCollection2.class;
	}

	private final Comparator<I> inferenceComparator = new Comparator<I>() {

		@Override
		public int compare(final I inf1, final I inf2) {
			return inf1.getPremises().size() + getJustification(inf1).size()
					- inf2.getPremises().size() - getJustification(inf2).size();
		}

	};

	private interface JobMember<C, A> {

	}

	private final static class Inf<I extends Inference<?>, A>
			extends Delegator<I> implements JobMember<I, A> {

		public Inf(I delegate) {
			super(delegate);
		}

	}

	private final static class Axiom<C, A> extends Delegator<A>
			implements JobMember<C, A> {

		public Axiom(A delegate) {
			super(delegate);
		}

	}

	/**
	 * The factory.
	 * 
	 * @author Peter Skocovsky
	 *
	 * @param <C>
	 *            the type of conclusions used in inferences
	 * @param <I>
	 *            the type of inferences used in the proof
	 * @param <A>
	 *            the type of axioms used by the inferences
	 */
	private static class Factory<C, I extends Inference<? extends C>, A>
			implements MinimalSubsetsFromProofs.Factory<C, I, A> {

		@Override
		public MinimalSubsetEnumerator.Factory<C, A> create(
				final Proof<? extends I> proof,
				final InferenceJustifier<? super I, ? extends Set<? extends A>> justifier,
				final InterruptMonitor monitor) {
			return new TopDownRepairComputation<C, I, A>(proof, justifier,
					monitor);
		}

	}

}
