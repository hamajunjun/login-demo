package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.Comment;
import com.example.logindemo.entity.User;
import com.example.logindemo.service.CommentService;
import com.example.logindemo.service.UserService;
import com.example.logindemo.util.JwtUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    // 发表评论
    @PostMapping("/add")
    public Result<String> add(@RequestHeader("Authorization") String token,
                              @RequestParam Long postId,
                              @RequestParam String content,
                              @RequestParam(required = false) Long parentId) {
        String username = JwtUtil.getUsername(token);
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }

        boolean success = commentService.addComment(postId, user.getId(), username, content, parentId);
        if (success) {
            return Result.success("评论成功");
        }
        return Result.error("评论失败，内容不能为空");
    }

    // 查询某个帖子的评论列表（公开接口）
    @GetMapping("/list")
    public Result<PageInfo<Comment>> list(@RequestParam Long postId,
                                          @RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        PageInfo<Comment> pageInfo = commentService.listByPostId(postId, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    // 删除评论
    @PostMapping("/delete")
    public Result<String> delete(@RequestHeader("Authorization") String token,
                                 @RequestParam Long id) {
        String username = JwtUtil.getUsername(token);
        String role = JwtUtil.getRole(token);

        boolean success = commentService.deleteComment(id, username, role);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("评论不存在或无权删除");
    }
}