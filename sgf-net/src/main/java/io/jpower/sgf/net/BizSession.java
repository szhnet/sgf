package io.jpower.sgf.net;

/**
 * 一个标记接口，用来标记业务层的session对象。 这样{@link NetSession#getAttachment()}
 * 返回的对象如果是业务层对象，则框架内部可以实现更好的配合。
 * <p>
 * <ul>
 * <li>更好的日志输出</li>
 * </ul>
 *
 * @author zheng.sun
 */
public interface BizSession {

}
