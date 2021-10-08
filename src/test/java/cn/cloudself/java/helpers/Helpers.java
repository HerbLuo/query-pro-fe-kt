package cn.cloudself.java.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Helpers {
    @SafeVarargs
    public static <T> List<T> listOf(T ...objs) {
        List<T> res = new ArrayList<>(objs.length);
        Collections.addAll(res, objs);
        return res;
    }
}
