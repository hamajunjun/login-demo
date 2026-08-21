package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.Post;
import com.example.logindemo.entity.User;
import com.example.logindemo.service.PostService;
import com.example.logindemo.service.UserService;
import com.example.logindemo.util.JwtUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/post")
@Tag(name = "帖子模块", description = "帖子发布、查询、修改、删除相关接口")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Operation(summary = "发布帖子", description = "当前登录用户发布新帖子，包含标题、内容、评分和小区信息")
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

    @Operation(summary = "帖子列表", description = "分页查询所有帖子列表")
    @GetMapping("/list")
    public Result<PageInfo<Post>> list(@RequestParam(defaultValue="1") int pageNum,
                                       @RequestParam(defaultValue="10") int pageSize){
        PageInfo<Post> pageInfo=postService.listPosts(pageNum,pageSize);
        return Result.success(pageInfo);
    }

    @Operation(summary = "查询帖子详情", description = "根据帖子ID查询帖子详细信息")
    @GetMapping("/{id}")
    public Result<Post> getById(@PathVariable Long id){
        Post post = postService.getPostById(id);
        if(post == null){
            return Result.error("帖子不存在");
        }
        return Result.success(post);
    }

    @Operation(summary = "修改帖子", description = "当前登录用户修改自己发布的帖子标题和内容")
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

    @Operation(summary = "删除帖子", description = "当前登录用户删除自己发布的帖子")
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
    @Operation(summary = "小区帖子列表", description = "根据小区ID分页查询该小区下的帖子列表")
    @GetMapping("/listByCommunity")
    public Result<PageInfo<Post>> listByCommunity(@RequestParam long communityId,
                                                  @RequestParam(defaultValue="1") int pageNum,
                                                  @RequestParam(defaultValue="10") int pageSize){
        PageInfo<Post> pageInfo=postService.listByCommunityId(communityId,pageNum,pageSize);
        return Result.success(pageInfo);
    }

    @Operation(summary = "我的帖子列表", description = "分页查询当前登录用户发布的帖子列表")
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

    @Operation(summary = "搜索帖子", description = "根据关键词分页搜索帖子标题或内容")
    @GetMapping("/search")
    public Result<PageInfo<Post>> search(@RequestParam String keyword,
                                         @RequestParam(defaultValue="1") int pageNum,
                                         @RequestParam(defaultValue="10") int pageSize){
        PageInfo<Post> pageInfo=postService.findByKeyword(keyword,pageNum,pageSize);
        return Result.success(pageInfo);
    }
    @Operation(summary= "热门帖子列表",description = "按浏览量倒序分页查询热门帖子")
    @GetMapping("/hostList")
    public Result<PageInfo<Post>> hotList(@RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize){
        PageInfo<Post> pageInfo=postService.getHotList(pageNum,pageSize);
        return Result.success(pageInfo);
    }
}




















