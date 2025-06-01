package cn.mrcode.dtdemo.b.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// StorageService.java
public interface StorageService {
    void decrease(String productId, Integer count);
}
