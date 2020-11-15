package com.bookstore.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @description:
 * @author: Tian
 * @time: 2020/7/26 17:23
 */


public interface IFileService {

    public String upload(MultipartFile file , String path);
}
