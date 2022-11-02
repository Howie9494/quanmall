package com.howie.quanmall.search.service;

import com.howie.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @Author Howie
 * @Date 2022/1/26 14:06
 * @Version 1.0
 */

public interface ProductSaveService {

    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
