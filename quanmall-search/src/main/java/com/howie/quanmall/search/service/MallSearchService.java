package com.howie.quanmall.search.service;

import com.howie.quanmall.search.vo.SearchParam;
import com.howie.quanmall.search.vo.SearchResult;

/**
 * @Author Howie
 * @Date 2022/1/28 14:59
 * @Version 1.0
 */
public interface MallSearchService {
    //根据检索的参数返回结果
    SearchResult search(SearchParam param);
}
