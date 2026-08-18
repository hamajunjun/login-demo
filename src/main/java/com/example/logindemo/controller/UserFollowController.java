package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.User;
import com.example.logindemo.service.UserFollowService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/follow")
@Tag(name = "用户关注模块", description = "用户关注和粉丝相关接口")
public class UserFollowController {

    @Autowired
    private UserFollowService userFollowService;

    @Operation(summary = "关注用户", description = "当前登录用户关注指定用户")
    @PostMapping("/add")
    public Result<String> add(@RequestHeader("Authorization") String token,
                              @RequestParam Long followingId) {
        userFollowService.follow(token, followingId);
        return Result.success("关注成功");
    }

    @Operation(summary = "取消关注", description = "当前登录用户取消关注指定用户")
    @PostMapping("/cancel")
    public Result<String> cancel(@RequestHeader("Authorization") String token,
                                 @RequestParam Long followingId) {
        userFollowService.unfollow(token, followingId);
        return Result.success("取消关注成功");
    }

    @Operation(summary = "判断是否已关注", description = "判断当前登录用户是否已关注指定用户")
    @GetMapping("/isFollowing")
    public Result<Boolean> isFollowing(@RequestHeader("Authorization") String token,
                                       @RequestParam Long followingId) {
        return Result.success(userFollowService.isFollowing(token, followingId));
    }

    @Operation(summary = "我关注的人", description = "分页查询当前登录用户关注的用户列表")
    @GetMapping("/myFollowings")
    public Result<PageInfo<User>> myFollowings(@RequestHeader("Authorization") String token,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(userFollowService.listFollowings(token, pageNum, pageSize));
    }

    @Operation(summary = "我的粉丝", description = "分页查询关注当前登录用户的用户列表")
    @GetMapping("/myFollowers")
    public Result<PageInfo<User>> myFollowers(@RequestHeader("Authorization") String token,
                                              @RequestParam(defaultValue = "1") int pageNum,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(userFollowService.listFollowers(token, pageNum, pageSize));
    }

    @Operation(summary = "关注数/粉丝数", description = "查询当前登录用户的关注数和粉丝数")
    @GetMapping("/count")
    public Result<Map<String, Integer>> count(@RequestHeader("Authorization") String token) {
        Map<String, Integer> map = new HashMap<>();
        map.put("followingCount", userFollowService.countFollowing(token));
        map.put("followerCount", userFollowService.countFollowers(token));
        return Result.success(map);
    }
}