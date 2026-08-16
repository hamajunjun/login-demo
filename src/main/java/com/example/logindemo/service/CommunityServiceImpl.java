package com.example.logindemo.service;

import com.example.logindemo.entity.Community;
import com.example.logindemo.mapper.CommunityMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommunityServiceImpl implements CommunityService{
    @Autowired
    private CommunityMapper communityMapper;

    @Override
    public boolean addCommunity(Community community){
        int result = communityMapper.insertCommunity(community);
        return result >0;
    }

    @Override
    public PageInfo<Community> listCommunities(int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Community> list=communityMapper.findAll();
        return new PageInfo<>(list);
    }

}
