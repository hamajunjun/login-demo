package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.Post;
import com.example.logindemo.entity.User;
import com.example.logindemo.service.PostService;
import com.example.logindemo.service.UserService;
import com.example.logindemo.util.JwtUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/post")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public Result<String> create(@RequestHeader("Authorization") String token,
                                 @RequestParam String title,
                                 @RequestParam String content,
                                 @RequestParam Integer rating,
                                 @RequestParam Long communityId){
        // 1. 从 token 中获取当前登录用户名
        String username =JwtUtil.getUsername(token);
        // 2. 根据用户名查询用户，获取 userId
        User user=userService.findByUsername(username);
        if(user == null){
            return Result.error("用户不存在");
        }
        // 3. 创建帖子
        boolean success=postService.createPost(title, content, username, user.getId(),communityId,rating);
        // 4. 返回结果
        if(success){
            return Result.success("发帖成功");
        }
        return Result.error("发帖失败");
    }

    @GetMapping("/list")
    public Result<PageInfo<Post>> list(@RequestParam(defaultValue="1") int pageNum,
                                       @RequestParam(defaultValue="10") int pageSize){
        PageInfo<Post> pageInfo=postService.listPosts(pageNum,pageSize);
        return Result.success(pageInfo);
    }

    @GetMapping("/{id}")
    public Result<Post> getById(@PathVariable Long id){
        Post post = postService.getPostById(id);
        if(post == null){
            return Result.error("帖子不存在");
        }
        return Result.success(post);
    }

    @PostMapping("/update")
    public Result<String> update(@RequestHeader("Authorization") String token,
                                 @RequestParam Long id,
                                 @RequestParam String title,
                                 @RequestParam String content){
        // 1. 从 token 获取当前用户名
        String username = JwtUtil.getUsername(token);
        // 2. 调用 service 更新
        boolean success = postService.updatePost(id,title,content,username);
        //3. 返回结果
        if(success){
            return Result.success("修改成功");
        }
        return Result.error("帖子不存在或无权修改");
    }

    @PostMapping("/delete")
    public Result<String> delete(@RequestHeader("Authorization") String token,
                                 @RequestParam Long id){
        // 1. 从 token 获取当前用户名
        String username=JwtUtil.getUsername(token);
        // 2. 调用 service 删除
        boolean success = postService.deletePost(id,username);
        // 3. 返回结果
        if(success){
            return Result.success("删除成功");
        }
        return Result.error("帖子不存在或无权删除");
    }
    @GetMapping("/listByCommunity")
    public Result<PageInfo<Post>> listByCommunity(@RequestParam long communityId,
                                                  @RequestParam(defaultValue="1") int pageNum,
                                                  @RequestParam(defaultValue="10") int pageSize){
        PageInfo<Post> pageInfo=postService.listByCommunityId(communityId,pageNum,pageSize);
        return Result.success(pageInfo);
    }

    @GetMapping("/myList")
    public Result<PageInfo<Post>> myList(@RequestHeader("Authorization") String token,
                                         @RequestParam(defaultValue="1") int pageNum,
                                         @RequestParam(defaultValue="10") int pageSize){
        String username=JwtUtil.getUsername(token);

        User user=userService.findByUsername(username);
        if(user==null){
            return Result.error("用户不存在");
        }

        PageInfo<Post> pageInfo = postService.listByUserId(user.getId(),pageNum,pageSize);

        return Result.success(pageInfo);
    }

    @GetMapping("/search")
    public Result<PageInfo<Post>> search(@RequestParam String keyword,
                                         @RequestParam(defaultValue="1") int pageNum,
                                         @RequestParam(defaultValue="10") int pageSize){
        PageInfo<Post> pageInfo=postService.findByKeyword(keyword,pageNum,pageSize);
        return Result.success(pageInfo);
    }
}




















