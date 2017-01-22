package io.jpower.sgf.common.tuple;

/**
 * @author zheng.sun
 */
public class Tuple {

    public static <A, B> TwoTuple<A, B> tuple(A first, B second) {
        return new TwoTuple<A, B>(first, second);
    }

}
