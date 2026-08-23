package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.PrivateMessage;
import com.example.logindemo.entity.User;
import com.example.logindemo.service.PrivateMessageService;
import com.example.logindemo.service.UserService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
@Tag(name = "私信模块", description = "用户私信相关接口")
public class PrivateMessageController {
    @Autowired
    private PrivateMessageService privateMessageService;

    @Autowired
    private UserService userService;

    @Operation(summary = "发送私信", description = "当前登录用户向指定用户发送私信")
    @PostMapping("/send")
    public Result<String> send(@RequestHeader("Authorization") String token,
                               @RequestParam Long receiverId,
                               @RequestParam String content){
        User user = userService.getCurrentUser(token);
        privateMessageService.sendMessage(user.getId(), receiverId, content);
        return Result.success("发送成功");
    }
    @Operation(summary = "收件箱", description = "分页查询当前登录用户收到的私信")
    @GetMapping("/inbox")
    public Result<PageInfo<PrivateMessage>> inbox(@RequestHeader("Authorization") String token,
                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        User user = userService.getCurrentUser(token);
        return Result.success(privateMessageService.listInbox(user.getId(), pageNum, pageSize));
    }

    @Operation(summary = "发件箱", description = "分页查询当前登录用户发送的私信")
    @GetMapping("/outbox")
    public Result<PageInfo<PrivateMessage>> outbox(@RequestHeader("Authorization") String token,
                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        User user = userService.getCurrentUser(token);
        return Result.success(privateMessageService.listOutbox(user.getId(), pageNum, pageSize));
    }
    @Operation(summary = "未读私信数", description = "查询当前登录用户的未读私信数量")
    @GetMapping("/unreadCount")
    public Result<Integer> unreadCount(@RequestHeader("Authorization") String token) {
        User user = userService.getCurrentUser(token);
        return Result.success(privateMessageService.countUnread(user.getId()));
    }

    @Operation(summary = "标记私信已读", description = "将指定私信标记为已读，只能标记自己收到的")
    @PostMapping("/markRead")
    public Result<String> markRead(@RequestHeader("Authorization") String token,
                                   @RequestParam Long id) {
        User user = userService.getCurrentUser(token);
        privateMessageService.markRead(user.getId(), id);
        return Result.success("标记已读成功");
    }
}
