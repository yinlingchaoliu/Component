package com.xiaojinzi.component.support;

import android.support.annotation.NonNull;

import com.xiaojinzi.component.anno.support.CheckClassName;

/**
 * 表示一个接受一个参数的接口
 *
 * @param <T>
 */
@CheckClassName
public interface Consumer<T> {

    /**
     * 接受一个参数的方法,允许抛出异常
     *
     * @param t 方法的参数值
     * @throws Exception 如果发生异常的时候允许抛出异常
     */
    void accept(@NonNull T t) throws Exception;

}