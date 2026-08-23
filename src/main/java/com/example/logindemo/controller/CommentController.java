package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.Comment;
import com.example.logindemo.entity.User;
import com.example.logindemo.service.CommentService;
import com.example.logindemo.service.UserService;
import com.example.logindemo.util.JwtUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@Tag(name = "评论模块", description = "帖子评论、回复、删除相关接口")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    // 发表评论
    @Operation(summary = "发表评论", description = "当前登录用户对指定帖子发表评论或回复其他评论")
    @PostMapping("/add")
    public Result<String> add(@RequestHeader("Authorization") String token,
                              @RequestParam Long postId,
                              @RequestParam String content,
                              @RequestParam(required = false) Long parentId) {
        User user = userService.getCurrentUser(token);
        commentService.addComment(postId, user.getId(), user.getUsername(), content, parentId);
        return Result.success("评论成功");
    }

    // 查询某个帖子的评论列表（公开接口）
    @Operation(summary = "查询评论列表", description = "根据帖子ID分页查询该帖子下的评论列表")
    @GetMapping("/list")
    public Result<PageInfo<Comment>> list(@RequestParam Long postId,
                                          @RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        PageInfo<Comment> pageInfo = commentService.listByPostId(postId, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    // 删除评论
    @Operation(summary = "删除评论", description = "当前登录用户或管理员删除指定评论")
    @PostMapping("/delete")
    public Result<String> delete(@RequestHeader("Authorization") String token,
                                 @RequestParam Long id) {
        User user = userService.getCurrentUser(token);
        String role = JwtUtil.getRole(token);

        commentService.deleteComment(id, user.getUsername(), role);
        return Result.success("删除成功");
    }
}