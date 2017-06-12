package nl.thehyve.podium.repository;

import nl.thehyve.podium.common.enumeration.Classifier;

/**
 * Summary entry containing a type (status or outcome) and a count.
 */
public class SummaryEntry<T extends Classifier> {

    private T type;
    private long count;

    public SummaryEntry(T type, long count) {
        this.type = type;
        this.count = count;
    }

    public T getType() {
        return type;
    }

    public long getCount() {
        return count;
    }

}
