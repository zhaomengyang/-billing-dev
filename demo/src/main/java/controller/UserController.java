package controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Throwables;
import com.pactera.service.common.JsonResponse;
import com.pactera.service.data.UserInfo;
import com.pactera.service.data.UserInfoLog;
import com.pactera.service.service.LogUserService;
import com.pactera.service.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @Auther: zhaomengyang
 * @Date: 2018/9/6 17:01
 * @Description:
 */
@RestController
@Api(value = "UserController", description = "用户相关api")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private AmqpTemplate rabbitTemplate;
    @Autowired
    private LogUserService logUserService;

    /**
     * 处理参数异常
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public JsonResponse messageNotReadable(HttpMessageNotReadableException exception, HttpServletResponse response) {
        JsonResponse jsonResponse = new JsonResponse();
        jsonResponse.setErr(exception.getMessage());
        jsonResponse.setData("参数异常");
        jsonResponse.setStatus(JsonResponse.notOk("").getStatus());
        return jsonResponse;
    }

    /**
     * 员工添加
     */
    @ApiOperation(value = "添加用户", notes = "添加用户", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping("/add_userInfo/v1.0")
    public JsonResponse addUser(@ApiParam(name = "userInfo",
            value = "部门对象", required = true) @RequestBody UserInfo userInfo) {
        UserInfoLog userInfoLog = new UserInfoLog();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
        Date date = new Date();
        try {
            JSONObject jsons = (JSONObject) JSONObject.toJSON(userInfo);

            userInfoLog.setUserRequest(jsons.toString());
            if (userInfo.getUserId() == null) {
                return JsonResponse.nullerr("userId异常");
            }


            if (userInfo.getUserName().isEmpty()) {
                return JsonResponse.nullerr("userName为空");
            }
            if (userInfo.getSortId() == null) {
                return JsonResponse.nullerr("sortId为空");
            }
            if (userInfo.getOrgDepartmentId() == null) {
                return JsonResponse.nullerr("orgDepartmentId为空");
            }
            if (userInfo.getLoginName().isEmpty()) {
                return JsonResponse.nullerr("loginName为空");
            }
            if (userInfo.getOrgPostName().isEmpty()) {
                return JsonResponse.nullerr("orgPostName为空");
            }
            if (userInfo.getOrgPostCode() == null) {
                return JsonResponse.nullerr("orgPostCode为空");
            }
            if (userInfo.getEnabled() == null) {
                return JsonResponse.nullerr("enabled为空");
            }
            if (userInfo.getDescription().isEmpty()) {
                return JsonResponse.nullerr("description为空");
            }
            if (userInfo.getTelNumber().isEmpty()) {
                return JsonResponse.nullerr("telNumber为空");
            }
            if (userInfo.getOfficeNumber().isEmpty()) {
                return JsonResponse.nullerr("officeNumber为空");
            }
            if (userInfo.getLocaltion().isEmpty()) {
                return JsonResponse.nullerr("localtion为空");
            }
            if (userInfo.getEmail().isEmpty()) {
                return JsonResponse.nullerr("email为空");
            }
            if (userInfo.getCode().isEmpty()) {
                return JsonResponse.nullerr("code为空");
            }
            if (userInfo.getOrgLevelName().isEmpty()) {
                return JsonResponse.nullerr("orgLevelName为空");
            }
            if (userInfo.getOrgLevelCode().isEmpty()) {
                return JsonResponse.nullerr("orgLevelCode为空");
            }
            if (userInfo.getState() == null) {
                return JsonResponse.nullerr("state为空");
            }
            UserInfo olduser = userService.findByuserId(userInfo.getUserId());
            if (olduser != null) {
                return JsonResponse.ok("用户已存在");
            }

            userInfo.setLastUpdateTime(dateFormat.format(date));
            userInfo.setPushTime(dateFormat.format(date));
            userInfo.setCreationTime(dateFormat.format(date));
            userService.insertUserInfo(userInfo);
            JSONObject json = (JSONObject) JSONObject.toJSON(userInfo);
            json.put("StatusType", 0);
            this.rabbitTemplate.convertAndSend("ztj-userinfo", json.toJSONString());
            this.rabbitTemplate.convertAndSend("ztj-dingding-userinfo", json.toJSONString());
            this.rabbitTemplate.convertAndSend("ztj-yiruan-userinfo", json.toJSONString());
            /**
             * log添加
             */
            userInfoLog.setIsSuccess(JsonResponse.ok("添加成功").isSuccess().toString());
            userInfoLog.setUserReponse(JsonResponse.ok("添加成功").toString());
            userInfoLog.setId(UUID.randomUUID().toString());
            userInfoLog.setStartTime(dateFormat.format(date));
            userInfoLog.setInterFaceName("/api/add_userInfo/v1.0");
            userInfoLog.setUserId(String.valueOf(userInfo.getUserId()));

            logUserService.insertUserInfo(userInfoLog);
            return JsonResponse.ok("添加成功");
        } catch (Exception e) {
            userInfoLog.setUserReponse(JsonResponse.notOk(Throwables.getStackTraceAsString(e)).toString());
            userInfoLog.setIsSuccess(JsonResponse.notOk("添加失败").isSuccess().toString());
            userInfoLog.setInterFaceName("/api/add_userInfo/v1.0");
            userInfoLog.setId(UUID.randomUUID().toString());
            userInfoLog.setUserId(String.valueOf(userInfo.getUserId()));
            userInfoLog.setStartTime(dateFormat.format(date));
            logUserService.insertUserInfo(userInfoLog);
            return JsonResponse.notOk("网络异常");
        }
    }

    /**
     * 员工修改
     */
    @ApiOperation(value = "修改用户", notes = "修改用户", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PostMapping("/update_userInfo/v1.0")
    public JsonResponse updateUser(@ApiParam(name = "userInfo",
            value = "部门对象", required = true) @RequestBody UserInfo userInfo) {
        UserInfoLog userInfoLog = new UserInfoLog();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
        Date date = new Date();
        try {
            JSONObject jsons = (JSONObject) JSONObject.toJSON(userInfo);

            userInfoLog.setUserRequest(jsons.toString());
            if (userInfo.getUserId() == null) {
                return JsonResponse.nullerr("userId异常");
            }


            if (userInfo.getUserName().isEmpty()) {
                return JsonResponse.nullerr("userName为空");
            }
            if (userInfo.getSortId() == null) {
                return JsonResponse.nullerr("sortId为空");
            }
            if (userInfo.getOrgDepartmentId() == null) {
                return JsonResponse.nullerr("orgDepartmentId为空");
            }
            if (userInfo.getLoginName().isEmpty()) {
                return JsonResponse.nullerr("loginName为空");
            }
            if (userInfo.getOrgPostName().isEmpty()) {
                return JsonResponse.nullerr("orgPostName为空");
            }
            if (userInfo.getOrgPostCode() == null) {
                return JsonResponse.nullerr("orgPostCode为空");
            }
            if (userInfo.getEnabled() == null) {
                return JsonResponse.nullerr("enabled为空");
            }
            if (userInfo.getDescription().isEmpty()) {
                return JsonResponse.nullerr("description为空");
            }
            if (userInfo.getTelNumber().isEmpty()) {
                return JsonResponse.nullerr("telNumber为空");
            }
            if (userInfo.getOfficeNumber().isEmpty()) {
                return JsonResponse.nullerr("officeNumber为空");
            }
            if (userInfo.getLocaltion().isEmpty()) {
                return JsonResponse.nullerr("localtion为空");
            }
            if (userInfo.getEmail().isEmpty()) {
                return JsonResponse.nullerr("email为空");
            }
            if (userInfo.getCode().isEmpty()) {
                return JsonResponse.nullerr("code为空");
            }
            if (userInfo.getOrgLevelName().isEmpty()) {
                return JsonResponse.nullerr("orgLevelName为空");
            }
            if (userInfo.getOrgLevelCode().isEmpty()) {
                return JsonResponse.nullerr("orgLevelCode为空");
            }
            if (userInfo.getState() == null) {
                return JsonResponse.nullerr("state为空");
            }
            //UserInfo userInfo = new UserInfo();
            UserInfo olduser = new UserInfo();
            // userInfo.setId(userId);
            userInfo.setLastUpdateTime(dateFormat.format(date));
            userInfo.setPushTime(dateFormat.format(date));
            olduser = userService.findByuserId(userInfo.getUserId());
            if (olduser == null) {
                return JsonResponse.notOk("用户不存在");
            }
            userInfo.setCreationTime(olduser.getCreationTime());
            userService.insertUserInfo(userInfo);
            JSONObject json = (JSONObject) JSONObject.toJSON(userInfo);
            json.put("StatusType", 1);
            this.rabbitTemplate.convertAndSend("ztj-userinfo", json.toJSONString());
            this.rabbitTemplate.convertAndSend("ztj-dingding-userinfo", json.toJSONString());
            this.rabbitTemplate.convertAndSend("ztj-yiruan-userinfo", json.toJSONString());

            /**
             * log添加
             */
            userInfoLog.setIsSuccess(JsonResponse.ok("修改成功").isSuccess().toString());
            userInfoLog.setUserReponse(JsonResponse.ok("修改成功").toString());
            userInfoLog.setId(UUID.randomUUID().toString());
            userInfoLog.setStartTime(dateFormat.format(date));
            userInfoLog.setInterFaceName("/api/update_userInfo/v1.0");
            userInfoLog.setUserId(String.valueOf(userInfo.getUserId()));
            logUserService.insertUserInfo(userInfoLog);
            return JsonResponse.ok("success");
        } catch (Exception e) {
            userInfoLog.setIsSuccess(JsonResponse.notOk(Throwables.getStackTraceAsString(e)).toString());
            userInfoLog.setUserReponse(JsonResponse.ok("修改失败").toString());
            userInfoLog.setId(UUID.randomUUID().toString());
            userInfoLog.setStartTime(dateFormat.format(date));
            userInfoLog.setInterFaceName("/api/update_userInfo/v1.0");
            userInfoLog.setUserId(String.valueOf(userInfo.getUserId()));
            logUserService.insertUserInfo(userInfoLog);
            return JsonResponse.notOk("网络异常");
        }

    }

}
