package com.shanjupay.transaction;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class testApp {


    @Test
    public void test(){
        Integer a1 = new Integer(40);
        Integer a2 = new Integer(40);
        Integer a3 = new Integer(0);
        System.out.println(a1 == 40);
        System.out.println(a1 == a2);
        System.out.println(a1 == a2 + a3);
        System.out.println(40 == a2 + a3);
    }
}
