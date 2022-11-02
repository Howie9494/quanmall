package com.howie.quanmall.search.controller;

import com.howie.quanmall.search.service.MallSearchService;
import com.howie.quanmall.search.vo.SearchParam;
import com.howie.quanmall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author Howie
 * @Date 2022/1/28 13:56
 * @Version 1.0
 */

@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){
        param.set_queryString(request.getQueryString());
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }

}
