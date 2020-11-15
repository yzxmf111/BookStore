package com.bookstore.service.impl;

import com.google.common.collect.Lists;
import com.bookstore.service.IFileService;
import com.bookstore.utils.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/26 17:24
 */

@Service("iFileService")
public class IFileServiceImpl implements IFileService {

    Logger logger = LoggerFactory.getLogger(IFileServiceImpl.class);
    @Override
    public String upload(MultipartFile file , String path) {
        //获取原始文件名称
        String filename = file.getOriginalFilename();
        //获取文件扩展名,且为了防止重名,给与新的名字
        String extensionName =  filename.substring(filename.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString() + "." + extensionName;
        logger.info("开始上传文件,上传文件名:{},上传的路径:{},上传后的文件名:{}",filename,path,uploadFileName);
        //创建文件夹
        File fileDir = new File(path);
        if (!fileDir.exists()){
            //tomcat的权限设置
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName);
        try {
            //文件上传到webapp里边
            file.transferTo(targetFile);
            //文件已经从本地上传到webapp目录下成功了,上传到ftp服务器,删除webapp下 的文件,文件夹可以保留
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            targetFile.delete();
        } catch (IOException e) {
          logger.error("文件上传异常",e);
            return null;

        }
        //controller最终要返回的是uri和url
        return targetFile.getName();
    }
}
