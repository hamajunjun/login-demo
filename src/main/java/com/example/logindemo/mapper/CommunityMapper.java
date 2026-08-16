package com.example.logindemo.mapper;

import com.example.logindemo.entity.Community;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommunityMapper{
    @Insert("INSERT INTO community (name, city, district, address, price, rating) " +
            "VALUES (#{name}, #{city}, #{district}, #{address}, #{price}, #{rating})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCommunity(Community community);

    @Select("SELECT * FROM community ORDER BY create_time DESC")
    List<Community> findAll();

    @Update("UPDATE community SET rating = (SELECT AVG(rating) FROM post WHERE community_id=#{communityId})"+
            " WHERE id=#{communityId}")
    int updateRating(@Param("communityId") Long communityId);
}