package com.shanjupay.uaa;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Base64;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestApp {




    public static void main(String[] args) {
        byte[] bytes = Base64.getDecoder().decode("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
        System.out.println(new String(bytes));
    }
}
