package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.Post;
import com.example.logindemo.service.PostFavoriteService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite")
@Tag(name = "帖子收藏模块", description = "帖子收藏相关接口")
public class PostFavoriteController {

    @Autowired
    private PostFavoriteService postFavoriteService;

    @Operation(summary = "收藏帖子", description = "当前登录用户收藏指定帖子")
    @PostMapping("/add")
    public Result<String> addFavorite(
            @RequestHeader("Authorization") String token,
            @RequestParam Long postId) {
        postFavoriteService.addFavorite(token, postId);
        return Result.success("收藏成功");
    }

    @Operation(summary = "取消收藏", description = "当前登录用户取消收藏指定帖子")
    @PostMapping("/cancel")
    public Result<String> cancelFavorite(
            @RequestHeader("Authorization") String token,
            @RequestParam Long postId) {
        postFavoriteService.cancelFavorite(token, postId);
        return Result.success("取消收藏成功");
    }

    @Operation(summary = "判断是否已收藏", description = "判断当前登录用户是否收藏了指定帖子")
    @GetMapping("/isFavorite")
    public Result<Boolean> isFavorite(
            @RequestHeader("Authorization") String token,
            @RequestParam Long postId) {
        return Result.success(postFavoriteService.isFavorite(token, postId));
    }

    @Operation(summary = "我的收藏列表", description = "分页查询当前登录用户收藏的帖子列表")
    @GetMapping("/myList")
    public Result<PageInfo<Post>> listMyFavorites(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(postFavoriteService.listMyFavorites(token, pageNum, pageSize));
    }
}