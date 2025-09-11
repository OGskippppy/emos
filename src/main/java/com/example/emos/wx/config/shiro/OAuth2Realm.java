package com.example.emos.wx.config.shiro;

import com.example.emos.wx.pojo.TbUser;
import com.example.emos.wx.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Slf4j
@Component
public class OAuth2Realm extends AuthorizingRealm {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    /**
     * 授权
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection collection) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        //获取用户权限列表
        TbUser user = (TbUser) collection.getPrimaryPrincipal();
        Integer id = user.getId();
        Set<String> permSet = userService.searchUserPermissions(id);
        // 把权限列表添加到info中
        info.setStringPermissions(permSet);
        return info;
    }


    /**
     * 认证（验证登录时调用）
     * @param token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        // 从令牌中获取userid，判断用户是否冻结
        String accessToken = (String) token.getPrincipal();
        Integer userId = jwtUtil.getUserId(accessToken);
        
        if (userId == null) {
            throw new AuthenticationException("無效的token");
        }
        
        TbUser user = userService.searchById(userId);

        if (user == null){
            throw new LockedAccountException("用户已被锁定，请联系管理员");
        }

        // 向info中添加用户信息和token
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, accessToken, getName());

        return info;
    }
}
