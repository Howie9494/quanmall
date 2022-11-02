package com.howie.quanmall.member.dao;

import com.howie.quanmall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-18 19:19:44
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
