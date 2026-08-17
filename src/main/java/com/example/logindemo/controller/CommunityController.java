package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.Community;
import com.example.logindemo.service.CommunityService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community")
@Tag(name = "小区模块", description = "小区添加、查询相关接口")
public class CommunityController{
    @Autowired
    private CommunityService communityService;

    @Operation(summary = "添加小区", description = "添加新的小区信息")
    @PostMapping("/add")
    public Result<String> add(@RequestBody Community community){
        boolean success = communityService.addCommunity(community);
        if(success){
            return Result.success("添加小区成功");
        }
        return Result.error("添加小区失败");
    }
    @Operation(summary = "小区列表", description = "分页查询所有小区列表")
    @GetMapping("/list")
    public Result<PageInfo<Community>> list(@RequestParam(defaultValue="1") int pageNum,
                                            @RequestParam(defaultValue="10") int pageSize){
        PageInfo<Community> pageInfo=communityService.listCommunities(pageNum,pageSize);
        return Result.success(pageInfo);
    }


}