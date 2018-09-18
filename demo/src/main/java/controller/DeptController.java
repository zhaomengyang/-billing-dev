package controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Throwables;
import com.pactera.service.common.DeptResponse;
import com.pactera.service.common.JsonResponse;
import com.pactera.service.data.DeptInfo;
import com.pactera.service.data.DeptInfoLog;
import com.pactera.service.service.DeptService;
import com.pactera.service.service.LogDeptService;
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
 * @Author zhaomengyang
 * @DateTime 2018-09-5 16:14
 * @Function 部门, 员工
 */
@RestController
@Api(value = "DeptController", description = "部门相关api")
public class DeptController {

    @Autowired
    private DeptService deptService;
    @Autowired
    private UserService userService;

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Autowired
    private LogDeptService logDeptService;

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public DeptResponse messageNotReadable(HttpMessageNotReadableException exception, HttpServletResponse response) {
        DeptResponse deptResponse = new DeptResponse();
        deptResponse.setErr(exception.getMessage());
        deptResponse.setData("参数异常");
        deptResponse.setStatus(JsonResponse.notOk("").getStatus());
        return deptResponse;
    }

    /**
     * 部门添加,修改-添加时为null
     */
    @ApiOperation(value = "添加部门", notes = "添加部门", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PostMapping("add_deptInfo/v1.0")
    public DeptResponse addDept(@ApiParam(name = "deptInfo",
            value = "部门对象", required = true) @RequestBody DeptInfo deptInfo) {
        DeptInfoLog deptInfoLog = new DeptInfoLog();
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
        try {
            JSONObject jsons = (JSONObject) JSONObject.toJSON(deptInfo);
            deptInfoLog.setDeptRequest(jsons.toString());
            if (deptInfo.getDeptName().isEmpty()) {
                return DeptResponse.nullerr("deptName为空");
            }
            System.out.println(deptInfo.getDeptId().toString().length());
            if (deptInfo.getDeptId() == null) {
                return DeptResponse.nullerr("deptId异常");
            }
            if (deptInfo.getParentId() == null) {
                return DeptResponse.nullerr("parentId为空");
            }
            if (deptInfo.getSortId().isEmpty()) {
                return DeptResponse.nullerr("sortId为空");
            }
            if (deptInfo.getChiefleader().isEmpty()) {
                return DeptResponse.nullerr("chiefleader为空");
            }
            if (deptInfo.getStatus() == null) {
                return DeptResponse.nullerr("status为空");
            }
            if (deptInfo.getOrgType() == null) {
                return DeptResponse.nullerr("orgType为空");
            }


            DeptInfo olddept = deptService.findBydeptId(deptInfo.getDeptId());
            if (olddept != null) {
                return DeptResponse.notOk("部门id重复-请重试");
            }
            deptInfo.setCreationTime(dateFormat.format(date));
            deptInfo.setLastUpdateTime(dateFormat.format(date));
            deptInfo.setPushTime(dateFormat.format(date));
            deptService.insertDeptInfo(deptInfo);
            JSONObject json = (JSONObject) JSONObject.toJSON(deptInfo);
            json.put("StatusType", 0);
            this.rabbitTemplate.convertAndSend("ztj-deptinfo", json.toJSONString());
            this.rabbitTemplate.convertAndSend("ztj-dingding-deptinfo", json.toJSONString());
            this.rabbitTemplate.convertAndSend("ztj-yiruan-deptinfo", json.toJSONString());
            json.put("StatusType", 1);
            this.rabbitTemplate.convertAndSend("ztj-deptinfo", json.toJSONString());
            /**
             * log添加
             */
            deptInfoLog.setId(UUID.randomUUID().toString());
            deptInfoLog.setDeptId(String.valueOf(deptInfo.getDeptId()));
            deptInfoLog.setDeptReponse(DeptResponse.ok("success").toString());
            deptInfoLog.setIsSuccess(DeptResponse.ok("success").isSuccess().toString());
            deptInfoLog.setInterfaceName("/api/add_deptInfo/v1.0");
            deptInfoLog.setStartTime(dateFormat.format(date));
            logDeptService.insertDeptInfo(deptInfoLog);
            return DeptResponse.ok("success");
        } catch (Exception e) {
            /**
             * log添加
             */
            deptInfoLog.setId(UUID.randomUUID().toString());
            deptInfoLog.setDeptId(String.valueOf(deptInfo.getDeptId()));
            deptInfoLog.setDeptReponse(DeptResponse.notOk(Throwables.getStackTraceAsString(e)).toString());
            deptInfoLog.setIsSuccess(DeptResponse.ok("success").isSuccess().toString());
            deptInfoLog.setInterfaceName("/api/add_deptInfo/v1.0");
            deptInfoLog.setStartTime(dateFormat.format(date));
            logDeptService.insertDeptInfo(deptInfoLog);
            return DeptResponse.notOk("网络异常");
        }
    }

    /**
     * 部门添加,修改-添加时为null
     *
     * @return
     */
    @ApiOperation(value = "修改部门", notes = "修改部门", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PostMapping("update_deptInfo/v1.0")
    public DeptResponse updateDept(@ApiParam(name = "deptInfo",
            value = "部门对象", required = true) @RequestBody DeptInfo deptInfo) {
        DeptInfoLog deptInfoLog = new DeptInfoLog();
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
        try {
            JSONObject jsons = (JSONObject) JSONObject.toJSON(deptInfo);
            deptInfoLog.setDeptRequest(jsons.toString());
            if (deptInfo.getDeptName().isEmpty()) {
                return DeptResponse.nullerr("deptName为空");
            }
            if (deptInfo.getDeptId() == null) {
                return DeptResponse.nullerr("deptId异常");
            }
            if (deptInfo.getParentId() == null) {
                return DeptResponse.nullerr("parentId为空");
            }
            if (deptInfo.getSortId().isEmpty()) {
                return DeptResponse.nullerr("sortId为空");
            }
            if (deptInfo.getChiefleader().isEmpty()) {
                return DeptResponse.nullerr("chiefleader为空");
            }
            if (deptInfo.getStatus() == null) {
                return DeptResponse.nullerr("status为空");
            }
            if (deptInfo.getOrgType() == null) {
                return DeptResponse.nullerr("orgType为空");
            }

            DeptInfo olddept = deptService.findBydeptId(deptInfo.getDeptId());
            if (olddept == null) {
                return DeptResponse.notOk("部门不存在");
            }
            deptInfo.setCreationTime(olddept.getCreationTime());
            deptInfo.setLastUpdateTime(dateFormat.format(date));
            deptInfo.setPushTime(dateFormat.format(date));
            deptService.insertDeptInfo(deptInfo);
            JSONObject json = (JSONObject) JSONObject.toJSON(deptInfo);
            json.put("StatusType", 1);
            //json.remove()
            this.rabbitTemplate.convertAndSend("ztj-deptinfo", json.toJSONString());
            this.rabbitTemplate.convertAndSend("ztj-dingding-deptinfo", json.toJSONString());
            this.rabbitTemplate.convertAndSend("ztj-yiruan-deptinfo", json.toJSONString());
            /**
             * log添加
             */
            deptInfoLog.setId(UUID.randomUUID().toString());
            deptInfoLog.setDeptId(String.valueOf(deptInfo.getDeptId()));
            deptInfoLog.setDeptReponse(DeptResponse.ok("success").toString());
            deptInfoLog.setIsSuccess(DeptResponse.ok("success").isSuccess().toString());
            deptInfoLog.setInterfaceName("/api/update_deptInfo/v1.0");
            deptInfoLog.setStartTime(dateFormat.format(date));
            logDeptService.insertDeptInfo(deptInfoLog);
            return DeptResponse.ok("success");
        } catch (Exception e) {
            /**
             * log添加
             */
            deptInfoLog.setId(UUID.randomUUID().toString());
            deptInfoLog.setDeptId(String.valueOf(deptInfo.getDeptId()));
            deptInfoLog.setDeptReponse(DeptResponse.notOk(Throwables.getStackTraceAsString(e)).toString());
            deptInfoLog.setIsSuccess(DeptResponse.ok("success").isSuccess().toString());
            deptInfoLog.setInterfaceName("/api/update_deptInfo/v1.0");
            deptInfoLog.setStartTime(dateFormat.format(date));
            logDeptService.insertDeptInfo(deptInfoLog);
            return DeptResponse.notOk("网络异常");
        }
    }


}
