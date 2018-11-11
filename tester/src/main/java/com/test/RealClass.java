package com.test;

import com.processor.log.Logged;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * @author dwang
 * @since 03.11.18
 */
@Logged
public class RealClass {
    public String realMethod(String[] args, int number) {
        System.out.println("Real method called!");
        return Arrays.stream(args)
                .collect(joining(", "));
    }

    public void anotherMethod(List<? extends Iterable> something) {
    }
}
