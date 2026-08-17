package com.example.logindemo.controller;

import com.example.logindemo.common.Result;
import com.example.logindemo.entity.User;
import com.example.logindemo.service.UserService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/user")
@Tag(name = "管理员-用户模块", description = "管理员对用户的管理接口")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "根据ID查询用户", description = "管理员根据用户ID查询用户详情")
    @GetMapping("/{id}")
    public Result<User> findById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "用户列表", description = "管理员分页查询所有用户列表")
    @GetMapping("/list")
    public Result<PageInfo<User>> list(@RequestParam(defaultValue="1") int pageNum,
                                       @RequestParam(defaultValue="10") int pageSize){
        PageInfo<User> pageInfo = userService.listUsers(pageNum,pageSize);
        return Result.success(pageInfo);

    }

    @Operation(summary = "删除用户", description = "管理员根据用户ID删除指定用户")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id){
        boolean success=userService.deleteUserById(id);
        if(success){
            return Result.success("删除成功");
        }
        return Result.error("用户不存在或删除失败");
    }

    @Operation(summary = "搜索用户", description = "管理员根据用户名搜索用户")
    @GetMapping("/search")
    public Result<List<User>> search(@RequestParam String username){
        List<User> list = userService.searchUsersByUsername(username);
        return Result.success(list);
    }

    @Operation(summary = "更新用户信息", description = "管理员更新指定用户的基本信息")
    @GetMapping("/update")
    public Result<String> update(@RequestParam Long id,
                                 @RequestParam String username,
                                 @RequestParam String email){
        boolean success=userService.updateUserInfo(id,username,email);
        if(success){
            return Result.success("更新成功");
        }
        return Result.error("用户不存在或更新失败");
    }

    @Operation(summary = "更新用户状态", description = "管理员更新指定用户的状态，状态值只能是 0 或 1")
    @PostMapping("/updateStatus")
    public Result<String> updateStatus(@RequestParam Long id,
                                       @RequestParam Integer status){
        if(status !=0 && status!=1){
            return Result.error("状态值只能是 0 或 1");
        }
        boolean success=userService.updateUserStatus(id,status);
        if(success){
            return Result.success("状态更新成功");
        }
        return Result.error("用户不存在或更新失败");
    }

    @Operation(summary = "重置用户密码", description = "管理员重置指定用户的密码为默认密码")
    @PostMapping("/resetPassword")
    public Result<String> resetPassword(@RequestParam Long id){
        String defaultPassword="123456";
        boolean success=userService.resetUserPassword(id,defaultPassword);
        if(success){
            return Result.success("密码已重置为：" + defaultPassword);
        }
        return Result.error("用户不存在或重置失败");
    }
}
