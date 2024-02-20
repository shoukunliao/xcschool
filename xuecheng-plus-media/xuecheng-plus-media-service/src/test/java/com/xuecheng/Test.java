package com.xuecheng;

public class Test {

    @org.junit.jupiter.api.Test
    public void test(){
        try {
            int i  = 1/0;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            System.out.println("finally");
        }
        System.out.println("out");
    }
}
