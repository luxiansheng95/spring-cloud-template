package com.springboot.druid.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.springboot.druid.annotation.ControllerLogs;
import com.springboot.druid.dao.UserJpaDao;
import com.springboot.druid.exception.InnerException;
import com.springboot.druid.exception.ParamIncorrectException;
import com.springboot.druid.exception.ServiceException;
import com.springboot.druid.ftp.FtpWholeUtils;
import com.springboot.druid.mapper.UserMapper;
import com.springboot.druid.model.base.Paging;
import com.springboot.druid.model.base.Query;
import com.springboot.druid.model.entity.User;
import com.springboot.druid.model.vo.ResponseVO;
import com.springboot.druid.service.UserService;
import com.springboot.druid.util.ApiCode;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Dong.L
 * @Create: 2019-11-15 16:22
 * @Description: java类描述
 */
@Slf4j
@Controller
public class UserController {
    @Autowired
    private UserJpaDao userJpaDao;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

    /**
     * @param: [id]
     * @return: java.lang.String
     * @Author: Dong.L
     * @Date: 2019/11/19 10:28
     * @Description: jpa测试
     */
    @ApiOperation("jpa测试")
    @ControllerLogs
    @ResponseBody
    @RequestMapping(value = "/testJpa/{id}", method = RequestMethod.GET)
    public String jpaUser(@PathVariable("id") Long id) {
        return userJpaDao.findById(id).toString();
    }

    /**
     * @param: [id]
     * @return: java.lang.String
     * @Author: Dong.L
     * @Date: 2019/11/19 10:30
     * @Description: mapper测试
     */
    @ApiOperation("mapper测试")
    @ControllerLogs
    @ResponseBody
    @RequestMapping(value = "/testMap/{id}", method = RequestMethod.GET)
    public String mapperUser(@PathVariable("id") Long id) {
        return userMapper.info(id).toString();
    }

    /**
     * @param: [query]
     * @return: java.lang.String
     * @Author: Dong.L
     * @Date: 2019/11/19 10:30
     * @Description: 翻页测试
     */
    @ApiOperation("翻页测试")
    @ControllerLogs
    @ResponseBody
    @RequestMapping(value = "/testPage", method = RequestMethod.POST)
    public ResponseVO pageUser(@RequestBody Query query) {
        log.info("->> testPage");
        Paging page = query.getPage();
        if (page == null) {
            page = new Paging();
        }
        PageHelper.startPage(page.getPage(), page.getPageSize());
        List<User> users = userMapper.pageAll(query.getCondition(), query.getOrderByClause());
        Page list = (Page) users;
        page.setPage(list.getPageNum());
        page.setPageSize(list.getPageSize());
        page.setTotal(list.getTotal());
        log.info("->> users: {}", users);
        Paging finalPage = page;
        return ResponseVO.newResult(ApiCode.OK, new HashMap<String, Object>(4) {
            private static final long serialVersionUID = -7465514010831408908L;

            {
                put("page", finalPage);
                put("users", users);
            }
        });
    }

    /**
     * @Author: Dong.L
     * @Date: 2019/11/19 15:39
     * @Description: 测试异常
     */
    @ResponseBody
    @ApiOperation("测试异常")
    @ControllerLogs
    @RequestMapping(value = "/testEx/{id}", method = RequestMethod.GET)
    public String testException(@PathVariable("id") Long id) {
        log.info("->> testException");
        log.info("->> id: {}", id);
        if (id == 1L) {
            throw new InnerException(ApiCode.ERROR.getMessage());
        } else if (id == 2L) {
            throw new ParamIncorrectException(ApiCode.CONFIGURED_ERROR);
        } else if (id == 3L) {
            throw new ServiceException(ApiCode.STATUS_CODE_ERROR);
        } else if (id == 9L) {
            userService.testException(id);
        }
        return ApiCode.OK.getMessage();
    }

    /**
     * @param:
     * @return:
     * @Author: Dong.L
     * @Date: 2019/11/19 10:30
     * @Description: 页面访问
     */
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    @ApiOperation("页面访问")
    @ControllerLogs
    public String index() {
        return "index";
    }

    @Autowired
    private RestTemplate restTemplate;

    @ApiOperation("请求下载")
    @ResponseBody
    @RequestMapping(value = "/reqDown", method = RequestMethod.GET)
    public String reqDown() {
        String url = "http://127.0.0.1:7014/testDown";
        ResponseEntity<ResponseVO> responseVO = restTemplate.postForEntity(url, new HashMap<String, Object>(4) {
            private static final long serialVersionUID = -7465514010831408908L;

            {
                put("pathName", "/img");
                put("remoteFileName", "lv2.jpg");
            }
        }, ResponseVO.class);
        log.info("->> {}", responseVO.getBody());
        return responseVO.getBody().toString();
    }

    @ApiOperation("测试下载")
    @ResponseBody
    @RequestMapping(value = "/testDown", method = RequestMethod.POST)
    public ResponseVO testDown(@RequestBody Map<String, Object> params) {
        log.info("->> testDown.params: {}", params);
        FtpWholeUtils ftpWholeUtils = new FtpWholeUtils();
        String ftpBase64 = ftpWholeUtils.readFileToBase64(params.get("pathName").toString(),
                params.get("remoteFileName").toString());
        return ResponseVO.newResult(ApiCode.OK, new HashMap<String, Object>(2) {
            private static final long serialVersionUID = -36854153093669713L;

            {
                put("ftpBase64", ftpBase64);
            }
        });
    }

}
