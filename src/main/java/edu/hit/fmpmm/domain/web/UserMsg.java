package edu.hit.fmpmm.domain.web;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@TableName("user_msg")
public class UserMsg implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("user_id")
    private Integer userId;
    @TableField("title")
    private String title;
    @TableField("msg")
    private String msg;
    @TableField("send_time")
    private Timestamp sendTime;
    @TableField("status")
    private Integer status;
    @TableField(exist = false)
    private String signal;  // 代表向前端发送什么信号
}
