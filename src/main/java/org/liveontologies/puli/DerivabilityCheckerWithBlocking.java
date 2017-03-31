package org.liveontologies.puli;

import java.util.Set;

/**
 * A {@link DerivabilityChecker} that can check if a conclusion is derivable
 * without using a given set of "blocked" conclusions. That is,
 * {@link #isDerivable(Object)} returns {@code true} if there exists a
 * derivation for the conclusion that does not used the conclusions in
 * {@link #getBlockedConclusions()}
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 */
public interface DerivabilityCheckerWithBlocking<C>
		extends DerivabilityChecker<C> {

	/**
	 * @return the set of conclusions that cannot be used in derivations when
	 *         checking for
	 */
	public Set<C> getBlockedConclusions();

	/**
	 * Prevent the given conclusion from being used in derivations
	 * 
	 * @param conclusion
	 */
	public void block(C conclusion);

	/**
	 * Allow the given conclusion to be used in derivations
	 * 
	 * @param conclusion
	 */
	public void unblock(C conclusion);

}
