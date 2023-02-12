package com.chen.controller;

import com.chen.common.CustomException;
import com.chen.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import java.io.*;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController extends BaseController {

    @Value("${qingguo.path}")
    private String basePath;
    /*
    * 文件上传
    * */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //file 是一个临时文件，要转存，否则请求完成后会删除
        log.info(file.toString());
        //获得原始文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID重新生存文件名，防止覆盖
        String fileName = UUID.randomUUID().toString()+suffix;

        File dir = new File(basePath);
        //判断目录是否存在
        if (!dir.exists()){
            //不存在 ,创建目录
            dir.mkdirs();
        }
        try {
            file.transferTo(new File(basePath+fileName));

        } catch (IOException e) {
            log.error("图片上传失败 错误："+e.getMessage());
            throw new CustomException("图片上传失败");
        }
        return R.success(fileName);
    }

    //回显图片
    @GetMapping("/download/{name}")
    public void download(@PathVariable String name) {

        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //输出流，通过输出流将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            log.info("图片回显失败");
        }
    }

    }
