package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.Notification;
import com.example.logindemo.entity.User;
import com.example.logindemo.service.NotificationService;
import com.example.logindemo.service.UserService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@Tag(name="消息通知模块",description = "消息通知相关接口")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Operation(summary = "标记通知已读", description = "将指定通知标记为已读，只能标记自己的通知")
    @PostMapping("/markRead")
    public Result<String> markRead(@RequestHeader("Authorization") String token,
                                   @RequestParam Long id){
        User user = userService.getCurrentUser(token);
        notificationService.markRead(user.getId(), id);
        return Result.success("标记已读成功");
    }
    @Operation(summary = "未读通知数", description = "查询当前登录用户的未读通知数量")
    @GetMapping("/unreadCount")
    public Result<Integer> unreadCount(@RequestHeader("Authorization") String token) {
        User user = userService.getCurrentUser(token);
        int count = notificationService.countUnread(user.getId());
        return Result.success(count);
    }
    @Operation(summary="我的通知列表",description="分页查询当前登录用户的通知列表，可按类型筛选")
    @GetMapping("/myList")
    public Result<PageInfo<Notification>> myList(@RequestHeader("Authorization") String token,
                                                 @RequestParam(defaultValue="1") int pageNum,
                                                 @RequestParam(defaultValue="10") int pageSize,
                                                 @RequestParam(required = false) String type){
        User user = userService.getCurrentUser(token);
        return Result.success(notificationService.listMyNotification(user.getId(), pageNum, pageSize, type));

    }
    @Operation(summary = "一键全部已读", description = "将当前登录用户的所有未读通知标记为已读")
    @PostMapping("/markAllRead")
    public Result<String> markAllRead(@RequestHeader("Authorization") String token) {
        User user = userService.getCurrentUser(token);
        notificationService.markAllRead(user.getId());
        return Result.success("全部标记已读成功");
    }
}
