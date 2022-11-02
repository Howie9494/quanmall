package com.howie.quanmall.member.dao;

import com.howie.quanmall.member.entity.MemberLoginLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员登录记录
 * 
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-18 19:19:44
 */
@Mapper
public interface MemberLoginLogDao extends BaseMapper<MemberLoginLogEntity> {
	
}
