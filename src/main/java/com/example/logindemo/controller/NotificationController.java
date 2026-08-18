package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.Notification;
import com.example.logindemo.service.NotificationService;
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

    @Operation(summary="我的通知列表",description = "分页查询当前登录用户的通知列表")
    @GetMapping("/myList")
    public Result<PageInfo<Notification>> myList(@RequestHeader("Authorization") String token,
                                                 @RequestParam(defaultValue="1") int pageNum,
                                                 @RequestParam(defaultValue="10") int pageSize){
        return Result.success(notificationService.listMyNotification(token,pageNum,pageSize));
    }
}
