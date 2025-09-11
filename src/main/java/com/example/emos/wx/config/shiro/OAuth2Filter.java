package com.example.emos.wx.config.shiro;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.DecodeException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
@Slf4j
public class OAuth2Filter extends AuthenticatingFilter {



    @Value("${emos.jwt.cache-expire}")
    private Integer cacheExpire;

    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TreadLocalToken treadLocalToken;


    /**
     * 创建token
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws Exception
     * 返回给shiro的 token
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = getRequestToken(request);
        if (StrUtil.isBlank(token)){
            return null;
        }

        return new OAuth2Token(token);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest request1 = (HttpServletRequest) request;
        if (request1.getMethod().equals(RequestMethod.OPTIONS)) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        //强转request和response
        HttpServletRequest request1 = (HttpServletRequest) request;
        HttpServletResponse response1 = (HttpServletResponse) response;
        response1.setCharacterEncoding("UTF-8");
        response1.setContentType("application/json;charset=UTF-8");
        response1.setHeader("Access-Control-Allow-Origin", request1.getHeader("Origin"));
        response1.setHeader("Access-Control-Allow-Credentials", "true");
        AuthenticationToken token = createToken(request1, response1);

        treadLocalToken.remove();

        String token1 = getRequestToken(request1);
        //如果token为空
        if (StrUtil.isBlank(token1)) {
            response1.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response1.getWriter().print("无效的令牌");
            return false;
        }
        //解析 token
        try {
            jwtUtil.verify(token1);
        }//如果是过期异常
        catch (TokenExpiredException e) {
           //分两种情况，1.客户端的token过期，2.redis和客户端的token过期
            //判断redis中的token是否过期
            //redis中的token还在
            if(redisTemplate.hasKey(token1)){
                redisTemplate.delete(token1);
                //获取新的 token
                token1 = jwtUtil.createToken(jwtUtil.getUserId(token1));
                //存进redis中
                redisTemplate.opsForValue().set(token1, jwtUtil.getUserId(token1),cacheExpire, TimeUnit.DAYS);
                //存进treadLocal中
                treadLocalToken.set(token1);
            }//redis中的token过期,重新登录
            else{
                response1.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response1.getWriter().print("令牌已过期");
                return false;
            }
        }//如果是签名异常
        catch (Exception e ){
            response1.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response1.getWriter().print("无效的签名");
            return false;

        }
        //给realm处理
        boolean bool = executeLogin(request, response);
        return bool;
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletRequest request1 = (HttpServletRequest) request;
        HttpServletResponse response1 = (HttpServletResponse) response;
        response1.setCharacterEncoding("UTF-8");
        response1.setContentType("application/json;charset=UTF-8");
        response1.setHeader("Access-Control-Allow-Origin", request1.getHeader("Origin"));
        response1.setHeader("Access-Control-Allow-Credentials", "true");
        response1.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        try {
            response1.getWriter().print("无效的令牌");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return false;
    }

    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        super.doFilterInternal(request, response, chain);
    }

    public String getRequestToken(HttpServletRequest request)
    {
        // 从请求头中获取token
        String token = request.getHeader("token");
        if (token == null || token.length() == 0) {
            // 如果没有在请求头中获取，则从参数中获取
            token = request.getParameter("token");
        }
        return token;

    }
}
