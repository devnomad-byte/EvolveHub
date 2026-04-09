package org.evolve.auth.user.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * @className UsersEntity
 * @description 用户实体类
 * @author zhao
 * @date 2026/4/9 14:26
 * @version v1.0
 **/
@Getter
@Setter
@TableName("t_user")
public class UsersEntity extends BaseEntity {

    /**
     * 用户名（登录账号，唯一）
     */
    private String username;

    /**
     * 密码（BCrypt 加密存储）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 所属部门 ID
     */
    private Long deptId;

    /**
     * 状态（0-禁用 1-正常）
     */
    private Integer status;
}
