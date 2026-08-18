package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.User;
import com.example.logindemo.service.PostLikeService;
import com.example.logindemo.service.UserService;
import com.example.logindemo.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/postLike")
@Tag(name = "帖子点赞模块", description = "帖子点赞、取消点赞相关接口")
public class PostLikeController {

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private UserService userService;

    @Operation(summary = "点赞帖子", description = "当前登录用户对指定帖子点赞")
    @PostMapping("/like")
    public Result<String> like(@RequestHeader("Authorization") String token,
                               @RequestParam Long postId){
        String username=JwtUtil.getUsername(token);
        User user=userService.findByUsername(username);
        if(user==null){
            return Result.error("用户不存在");
        }
        boolean success=postLikeService.like(postId,user.getId());
        if(success){
            return Result.success("点赞成功");
        }
        return Result.error("点赞失败，可能已点赞");
    }

    @Operation(summary = "取消点赞", description = "当前登录用户取消对指定帖子的点赞")
    @PostMapping("/unlike")
    public Result<String> unlike(@RequestHeader("Authorization") String token,
                                 @RequestParam Long postId){
        String username = JwtUtil.getUsername(token);
        User user=userService.findByUsername(username);
        if(user==null){
            return Result.error("用户不存在");
        }
        boolean success=postLikeService.unlike(postId,user.getId());
        if(success){
            return Result.success("取消点赞成功");
        }
        return Result.error("取消点赞失败");
    }
    @Operation(summary = "查询点赞数", description = "查询指定帖子的点赞总数")
    @GetMapping("/count")
    public Result<Integer> count(@RequestParam Long postId){
        return Result.success(postLikeService.getLikeCount(postId));
    }

    @Operation(summary = "判断是否已点赞", description = "判断当前登录用户是否对指定帖子已点赞")
    @GetMapping("/hasLiked")
    public Result<Boolean> hasLiked(@RequestHeader("Authorization") String token,
                                    @RequestParam Long postId) {
        String username = JwtUtil.getUsername(token);
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.success(false);
        }
        return Result.success(postLikeService.hasLiked(postId, user.getId()));
    }
}



















