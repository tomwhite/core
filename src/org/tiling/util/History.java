package org.tiling.util;

import java.util.*;

/**
 * I look for patterns in sequences of actions and try to predict the next likely candidate.
 * I use a simple Markov-style prediction algorithm.
 */
public class History {

	static class Pair {

		Object a, b;
		
		public Pair(Object a, Object b) {
			this.a = a;
			this.b = b;
		}

		public boolean equals(Object o) {
			if (o instanceof History.Pair) {
				History.Pair p = (History.Pair) o;
				return p.a.equals(a) && p.b.equals(b);
			}
			return false;
		}

		public int hashCode() {
			return a.hashCode() ^ b.hashCode();
		}	

		public String toString() {
			return "(" + a.toString() + ", " + b.toString() + ")";
		}

	}
	
	private List history = new ArrayList();

	private Set singletons = new HashSet();
	private Map singletonsToOccurences = new HashMap();
	private Map pairsToOccurences = new HashMap();
	public History() {
	}
	private int getPairsToOccurences(History.Pair o) {
		Integer occurences = (Integer) pairsToOccurences.get(o);
		return occurences == null ? 0 : occurences.intValue();
	}
	private int getSingletonsToOccurences(Object o) {
		Integer occurences = (Integer) singletonsToOccurences.get(o);
		return occurences == null ? 0 : occurences.intValue();
	}
	private void incrementPairsToOccurences(History.Pair o) {
		int occurences = getPairsToOccurences(o);
		pairsToOccurences.put(o, new Integer(occurences + 1));
	}
	private void incrementSingletonsToOccurences(Object o) {
		int occurences = getSingletonsToOccurences(o);
		singletonsToOccurences.put(o, new Integer(occurences + 1));
	}
	public boolean isEmpty() {
		return history.isEmpty();
	}
	public Object last() {
		if (isEmpty()) {
			return null;
		} else {
			return history.get(history.size() - 1);
		}
	}
	public Object predict() {
		if (isEmpty()) {
			return null;
		} else {
			Object last = last();
			double lastOccurences = getSingletonsToOccurences(last);
			if (lastOccurences == 0.0) {
				return last;
			} else {
				Object next = null;
				double maxP = 0.0;
				for(Iterator i = singletons.iterator(); i.hasNext(); ) {
					Object o = i.next();
					double p = getPairsToOccurences(new Pair(last, o)) / lastOccurences;
					if (p > maxP) {
						maxP = p;
						next = o;
					}
				}
				return next;
			}
		}
	}
	public void remember(Object o) {
		if (!isEmpty()) {
			Object last = last();
			singletons.add(o);
			incrementSingletonsToOccurences(last);
			incrementPairsToOccurences(new Pair(last, o));
		}
		history.add(o);
	}
}
