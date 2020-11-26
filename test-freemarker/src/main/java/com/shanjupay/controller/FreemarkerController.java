package com.shanjupay.controller;

import com.shanjupay.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @program: shanjupay
 * @author: wangxin
 * @create: 2020-11-20 08:45
 **/

@Controller
public class FreemarkerController {

    @Autowired
    RestTemplate restTemplate;


    @GetMapping(value = "freemarker")
    public String freemarker(Map<String, Object> map, HttpServletRequest request) {
        map.put("name", "world");
        return "test1";
    }


    @RequestMapping("/test2")
    public String freemarker(Map<String, Object> map) {
        //向数据模型放数据
        ArrayList arrayList = new ArrayList<Student>();
        for (int i = 0; i < 3; i++) {
            Student student = new Student();
            student.setId(i);
            student.setName("stu"+i);
            student.setAge(10+i);
            student.setMoney(1000f+i);
            arrayList.add(student);
        }
        map.put("list",arrayList);

        HashMap<String, List> stumap = new HashMap<>();
        stumap.put("list",arrayList);
        map.put("stuMap",stumap);


        //返回模板文件名称
        return "test2";
    }




}