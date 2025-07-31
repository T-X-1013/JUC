package com.day05;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        int[] arr = {1,2,3,4,5,6,7,8,9};
        int target = 9;
        Map<Integer, Integer> map = new HashMap<>();
        int[] res = two(arr,target);
        System.out.println(Arrays.toString(res));
    }


    public static int[] two(int[] arr, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < arr.length; i++) {
            int tmp = target - arr[i];
            if (map.containsKey(tmp)) {
                return new int[]{map.get(tmp), i};
            }
            map.put(arr[i], i);
        }
        return new int[]{-1,-1};
    }
}


