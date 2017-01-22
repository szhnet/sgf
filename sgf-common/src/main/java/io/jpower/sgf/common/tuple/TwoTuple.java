package io.jpower.sgf.common.tuple;

/**
 * 二元组
 *
 * @param <A>
 * @param <B>
 * @author zheng.sun
 */
public class TwoTuple<A, B> {

    private final A first;

    private final B second;

    public TwoTuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        TwoTuple<A, B> ot = (TwoTuple<A, B>) o;
        if (first != ot.first && !(first != null && first.equals(ot.first))) {
            return false;
        }
        if (second != ot.second && !(second != null && second.equals(ot.second))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 17;
        result = prime * result + (first == null ? 0 : first.hashCode());
        result = prime * result + (second == null ? 0 : second.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

}
