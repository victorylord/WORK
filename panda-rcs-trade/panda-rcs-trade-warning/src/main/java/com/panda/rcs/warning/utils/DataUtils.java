package com.panda.rcs.warning.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class DataUtils {
    /**
     * 获取集合中最接近的数
     *
     * @param number  需要查找的数字
     * @param numbers 数字集合
     * @param flag    如果有两个相近的数据   true:选择大数  false:选择小数
     * @param <T>     必须为数字类型
     * @return 相近结果
     */
    public static <T extends Number> T getSimilarNumber(T number, Collection<T> numbers, Boolean flag) {
        if (null == numbers || numbers.isEmpty()) {
            throw new RuntimeException("数字集合不能为空");
        }
        // 如果集合包含需要查找的数字,直接返回
        if (numbers.contains(number)) {
            return number;
        }
        // 判断集合是否为List
        if (numbers instanceof List) {
            // 因为List为重复集合,转成Set去重
            numbers = new HashSet<>(numbers);
        }
        // 把需要查找的数字添加到集合中
        numbers.add(number);
        // 对集合进行排序,转换成有序集合
        List<T> numList = numbers.stream().sorted().collect(Collectors.toList());
        // 获取集合的长度
        int size = numList.size();
        // 获取需要查找的数字所在位置
        int index = numList.indexOf(number);
        // 如果所在位置为最前面,表示第一位及为临近数
        if (index <= 0) {
            return numList.get(1);
        }
        // 如果所在位置为最后一位,表示在它前面的一位为临近数
        if (index >= size - 1) {
            return numList.get(size - 2);
        }
        // 前一个数据
        T before = numList.get(index - 1);
        // 后一个数据
        T after = numList.get(index + 1);
        // 需要查找的数字和前一个数字相差值
        double beforeDifference = number.doubleValue() - before.doubleValue();
        // 需要查找的数字和后一个数字相差值
        double afterDifference = after.doubleValue() - number.doubleValue();
        // 如果两个值相同
        if (beforeDifference == afterDifference) {
            // 判断取大值,还是取小值
            return flag ? after : before;
        }
        // 取最相近的值
        return beforeDifference < afterDifference ? before : after;
    }
}
