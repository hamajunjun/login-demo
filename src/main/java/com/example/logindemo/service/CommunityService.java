package com.example.logindemo.service;

import com.example.logindemo.entity.Community;
import com.github.pagehelper.PageInfo;

public interface CommunityService {

    public boolean addCommunity(Community community);

    PageInfo<Community> listCommunities(int pageNum, int pageSize);
}
