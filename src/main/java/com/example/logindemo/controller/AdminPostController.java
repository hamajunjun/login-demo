package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.Post;
import com.example.logindemo.service.PostService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/post")
@Tag(name = "管理员-帖子模块", description = "管理员对帖子的管理接口")
public class AdminPostController {

    @Autowired
    private PostService postService;

    @Operation(summary = "帖子列表", description = "管理员分页查询所有帖子列表")
    @GetMapping("/list")
    public Result<PageInfo<Post>> list(@RequestParam(defaultValue="1") int pageNum,
                                       @RequestParam(defaultValue="10") int pageSize){
        PageInfo<Post> pageInfo=postService.listPosts(pageNum,pageSize);
        return Result.success(pageInfo);
    }
    @Operation(summary = "删除帖子", description = "管理员根据帖子ID删除指定帖子")
    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long id){
        boolean success=postService.adminDeletePost(id);
        if(success){
            return Result.success("删除成功");
        }
        return Result.error("帖子不存在或删除失败");
    }
}
