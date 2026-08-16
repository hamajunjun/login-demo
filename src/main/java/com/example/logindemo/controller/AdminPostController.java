package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.Post;
import com.example.logindemo.service.PostService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/post")
public class AdminPostController {

    @Autowired
    private PostService postService;

    @GetMapping("/list")
    public Result<PageInfo<Post>> list(@RequestParam(defaultValue="1") int pageNum,
                                       @RequestParam(defaultValue="10") int pageSize){
        PageInfo<Post> pageInfo=postService.listPosts(pageNum,pageSize);
        return Result.success(pageInfo);
    }
    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long id){
        boolean success=postService.adminDeletePost(id);
        if(success){
            return Result.success("删除成功");
        }
        return Result.error("帖子不存在或删除失败");
    }
}
