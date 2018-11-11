package com.test;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;

/**
 * @author dwang
 * @since 06.11.18
 */
public class Application {
    public static void main(String[] args) {
        Map proxyInstance = (Map) Proxy.newProxyInstance(
            Application.class.getClassLoader(),
            new Class[] { Map.class },
            (proxy, method, methodArgs) -> {
                System.out.println(proxy.getClass());
                System.out.println("Invoked method: " + method.getName());
                System.out.println(Arrays.toString(methodArgs));
//                if (method.getName().equals("get")) {
//                    return 42;
//                } else {
//                    throw new UnsupportedOperationException(
//                            "Unsupported method: " + method.getName());
//                }
                return 1;
//                return method.invoke(proxy, methodArgs);
            });

        proxyInstance.put("key", "value");
        System.out.println(proxyInstance.get("key"));


    }
}
