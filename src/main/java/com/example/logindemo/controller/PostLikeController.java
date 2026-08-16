package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.User;
import com.example.logindemo.service.PostLikeService;
import com.example.logindemo.service.UserService;
import com.example.logindemo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/postLike")
public class PostLikeController {

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private UserService userService;

    @PostMapping("/like")
    public Result<String> like(@RequestHeader("Authorizaton") String token,
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

    @PostMapping("/unlike")
    public Result<String> unlike(@RequestHeader("Authorization") String token,
                                 @RequestParam Long postId){
        String username = JwtUtil.getUsername(token);
        User user=userService.findByUsername(token);
        if(user==null){
            return Result.error("用户不存在");
        }
        boolean success=postLikeService.unlike(postId,user.getId());
        if(success){
            return Result.success("取消点赞成功");
        }
        return Result.error("取消点赞失败");
    }
    @GetMapping("/count")
    public Result<Integer> count(@RequestParam Long postId){
        return Result.success(postLikeService.getLikeCount(postId));
    }

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



















