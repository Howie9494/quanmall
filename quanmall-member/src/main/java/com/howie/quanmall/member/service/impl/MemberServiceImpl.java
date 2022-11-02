package com.howie.quanmall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.howie.common.utils.HttpUtils;
import com.howie.quanmall.member.dao.MemberLevelDao;
import com.howie.quanmall.member.entity.MemberLevelEntity;
import com.howie.quanmall.member.exception.PhoneExistException;
import com.howie.quanmall.member.exception.UsernameExistException;
import com.howie.quanmall.member.vo.MemberLoginVo;
import com.howie.quanmall.member.vo.MemberRegistVo;
import com.howie.quanmall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.asn1.cms.OtherRecipientInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.howie.common.utils.PageUtils;
import com.howie.common.utils.Query;

import com.howie.quanmall.member.dao.MemberDao;
import com.howie.quanmall.member.entity.MemberEntity;
import com.howie.quanmall.member.service.MemberService;

import javax.jws.soap.SOAPBinding;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberEntity memberEntity = new MemberEntity();
        MemberDao memberDao = this.baseMapper;
        //设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefualtLevel();
        memberEntity.setLevelId(levelEntity.getId());

        //检查用户名和手机号是否唯一
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());
        memberEntity.setNickname(vo.getUserName());

        //密码进行加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        memberDao.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException{
        MemberDao memberDao = this.baseMapper;
        Integer mobile = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile>0){
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserNameUnique(String userName) throws UsernameExistException {
        MemberDao memberDao = this.baseMapper;
        Integer username = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (username>0){
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginAcct = vo.getLoginAcct();
        String password = vo.getPassword();

        MemberDao memberDao = this.baseMapper;
        MemberEntity entity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginAcct)
                .or().eq("mobile", loginAcct));
        if (entity == null){
            return null;
        }else {
            String passwordDB = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, passwordDB);
            if (matches){
                return entity;
            }else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception{
        String uid = socialUser.getUid();
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity!=null){
            //用户已经注册过
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());

            memberDao.updateById(update);
            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            return memberEntity;
        }else {
            //没有登录过需要注册
            MemberEntity regist = new MemberEntity();
            try {
                HashMap<String, String> query = new HashMap<>();
                query.put("access_token",socialUser.getAccess_token());
                query.put("uid",socialUser.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), query);
                if (response.getStatusLine().getStatusCode()==200){
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);

                    String name = jsonObject.getString("name");
                    regist.setNickname(name);

                    String gender = jsonObject.getString("gender");
                    regist.setGender("m".equals(gender)?1:0);
                }
            }catch (Exception e){

            }
            regist.setSocialUid(socialUser.getUid());
            regist.setAccessToken(socialUser.getAccess_token());
            regist.setExpiresIn(socialUser.getExpires_in());

            memberDao.insert(regist);
            return regist;
        }
    }
}