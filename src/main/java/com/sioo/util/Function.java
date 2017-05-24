package com.sioo.util;

/**
 * 类似js的回调函数
 *
 * @param <E> 输入类型
 * @param <K> 输出类型
 */
public interface Function<E, K> {

     K callBack(E e);

}
