package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.Community;
import com.example.logindemo.service.CommunityService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community")
public class CommunityController{
    @Autowired
    private CommunityService communityService;

    @PostMapping("/add")
    public Result<String> add(@RequestBody Community community){
        boolean success = communityService.addCommunity(community);
        if(success){
            return Result.success("添加小区成功");
        }
        return Result.error("添加小区失败");
    }
    @GetMapping("/list")
    public Result<PageInfo<Community>> list(@RequestParam(defaultValue="1") int pageNum,
                                            @RequestParam(defaultValue="10") int pageSize){
        PageInfo<Community> pageInfo=communityService.listCommunities(pageNum,pageSize);
        return Result.success(pageInfo);
    }


}