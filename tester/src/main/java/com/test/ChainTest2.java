package com.test;

import com.processor.chain.Chain;

/**
 * @author dwang
 * @since 06.11.18
 */
public class ChainTest2 {
    @Chain(
            downStreamClass = ChainTest1.class,
            downStreamMethod = "doubleIt"
    )
    public int addOne(int i) {
        return i + 1;
    }

}
