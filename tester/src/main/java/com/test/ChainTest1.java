package com.test;

import com.processor.chain.Chain;

/**
 * @author dwang
 * @since 05.11.18
 */
public class ChainTest1 {
    @Chain(
            downStreamClass = ChainTest1.class,
            downStreamMethod = "i2s"
    )
    public int doubleIt(int i) {
        return i * 2;
    }

    public String i2s(int i) {
        return String.valueOf(i);
    }
}
