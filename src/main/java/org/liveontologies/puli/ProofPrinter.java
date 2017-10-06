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
package org.liveontologies.puli;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * A simple pretty printer for proofs using ASCII characters. Due to potential
 * cycles, inferences for every conclusion are printed only once upon their
 * first occurrence in the proof. Every following occurrence of the same
 * conclusion is labeled by {@code *}.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <I>
 *            the type of the inferences returned by the proof
 * @param <A>
 *            the type of the axioms in the justification of inferences
 */
public class ProofPrinter<I extends Inference<?>, A> {

	/**
	 * the set of inferences from which the proofs are formed
	 */
	private final Proof<? extends I> proof_;

	/**
	 * provides justifications for inferences
	 */
	private final InferenceJustifier<I, ? extends Set<? extends A>> justifier_;

	/**
	 * the current positions of iterators over inferences for conclusions
	 */
	private final Deque<Iterator<? extends I>> inferenceStack_ = new LinkedList<Iterator<? extends I>>();

	/**
	 * the current positions of iterators over conclusions for inferences
	 */
	private final Deque<Iterator<?>> conclusionStack_ = new LinkedList<Iterator<?>>();

	/**
	 * the current positions of iterators over justifications for inferences
	 */
	private final Deque<Iterator<? extends A>> justificationStack_ = new LinkedList<Iterator<? extends A>>();

	/**
	 * accumulates the printed conclusions to avoid repetitions
	 */
	private final Set<Object> printed_ = new HashSet<Object>();

	/**
	 * where the output is written
	 */
	private final BufferedWriter writer_;

	protected ProofPrinter(final Proof<? extends I> proof,
			final InferenceJustifier<I, ? extends Set<? extends A>> justifier,
			BufferedWriter writer) {
		this.proof_ = proof;
		this.justifier_ = justifier;
		this.writer_ = writer;
	}

	protected ProofPrinter(final Proof<? extends I> proof,
			final InferenceJustifier<I, ? extends Set<? extends A>> justifier) {
		this(proof, justifier,
				new BufferedWriter(new OutputStreamWriter(System.out)));
	}

	public void printProof(Object conclusion) throws IOException {
		process(conclusion);
		process();
		writer_.flush();
	}

	public static <I extends Inference<?>, A> void print(
			final Proof<? extends I> proof,
			final InferenceJustifier<I, ? extends Set<? extends A>> justifier,
			Object goal) throws IOException {
		ProofPrinter<I, A> pp = new ProofPrinter<I, A>(proof, justifier);
		pp.printProof(goal);
	}

	public static <I extends Inference<?>> void print(
			final Proof<? extends I> proof, Object goal) throws IOException {
		print(proof,
				new BaseInferenceJustifier<I, Set<Void>>(
						Collections.<I, Set<Void>> emptyMap(),
						Collections.<Void> emptySet()),
				goal);
	}

	protected BufferedWriter getWriter() {
		return writer_;
	}

	protected void writeConclusion(Object conclusion) throws IOException {
		// can be overridden
		writer_.write(conclusion.toString());
	}

	private boolean process(Object conclusion) throws IOException {
		writePrefix();
		writeConclusion(conclusion);
		boolean newConclusion = printed_.add(conclusion);
		if (newConclusion) {
			inferenceStack_.push(proof_.getInferences(conclusion).iterator());
		} else {
			// block conclusions appeared earlier in the proof
			writer_.write(" *");
		}
		writer_.newLine();
		return newConclusion;
	}

	private void print(A just) throws IOException {
		writePrefix();
		writer_.write(just.toString());
		writer_.newLine();
	}

	private void process() throws IOException {
		for (;;) {
			// processing inferences
			Iterator<? extends I> infIter = inferenceStack_.peek();
			if (infIter == null) {
				return;
			}
			// else
			if (infIter.hasNext()) {
				I inf = infIter.next();
				conclusionStack_.push(inf.getPremises().iterator());
				justificationStack_
						.push(justifier_.getJustification(inf).iterator());
			} else {
				inferenceStack_.pop();
			}
			// processing conclusions
			Iterator<?> conclIter = conclusionStack_.peek();
			if (conclIter == null) {
				return;
			}
			// else
			for (;;) {
				if (conclIter.hasNext()) {
					if (process(conclIter.next())) {
						break;
					}
					// else
					continue;
				}
				// else
				// processing justifications
				Iterator<? extends A> justIter = justificationStack_.peek();
				if (justIter == null) {
					return;
				}
				// else
				while (justIter.hasNext()) {
					print(justIter.next());
				}
				conclusionStack_.pop();
				justificationStack_.pop();
				break;
			}
		}
	}

	private void writePrefix() throws IOException {
		Iterator<Iterator<? extends I>> inferStackItr = inferenceStack_
				.descendingIterator();
		Iterator<Iterator<?>> conclStackItr = conclusionStack_
				.descendingIterator();
		Iterator<Iterator<? extends A>> justStackItr = justificationStack_
				.descendingIterator();
		while (inferStackItr.hasNext()) {
			Iterator<? extends I> inferIter = inferStackItr.next();
			Iterator<?> conclIter = conclStackItr.next();
			Iterator<? extends A> justIter = justStackItr.next();
			boolean hasNextPremise = conclIter.hasNext() || justIter.hasNext();
			if (conclStackItr.hasNext() || justStackItr.hasNext()) {
				writer_.write(hasNextPremise ? "|  "
						: inferIter.hasNext() ? ":  " : "   ");
			} else {
				writer_.write(hasNextPremise ? "+- " : "\\- ");
			}
		}
	}

}
