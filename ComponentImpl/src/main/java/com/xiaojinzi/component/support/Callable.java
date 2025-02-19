package com.xiaojinzi.component.support;

import android.support.annotation.NonNull;

import com.xiaojinzi.component.anno.support.CheckClassName;

/**
 * 懒加载设计
 * time   : 2018/11/27
 *
 * @author : xiaojinzi
 */
@CheckClassName
public interface Callable<T> {

    /**
     * 获取实际的兑现
     *
     * @return 获取实现对象
     */
    @NonNull
    T get();

}
