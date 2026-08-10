package peony.util;

/**
 * An iterator over a sequence of unboxed int values
 */
public interface IntIterator {

    /**
     * Test whether there are any more integers in the sequence
     */

    public boolean hasNext();

    /**
     * Return the next integer in the sequence. The result is undefined unless hasNext() has been called
     * and has returned true.
     */

    public int next();
}

