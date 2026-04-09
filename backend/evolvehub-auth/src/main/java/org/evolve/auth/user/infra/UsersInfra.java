package org.evolve.auth.user.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.auth.user.model.UsersEntity;
import org.springframework.stereotype.Repository;

/**
 * 用户数据访问层
 * <p>
 * 封装 t_user 表的所有数据库操作，对外提供业务语义化的查询和写入方法，
 * 上层 Manager 只通过本类访问用户数据，不直接接触 SQL / MyBatis API。
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Repository
public class UsersInfra extends ServiceImpl<UsersInfra.UsersMapper, UsersEntity> {

    @Mapper
    interface UsersMapper extends BaseMapper<UsersEntity> {}

    // ==================== 单条查询 ====================

    /**
     * 根据用户名查询用户（登录 / 唯一性校验）
     *
     * @param username 用户名
     * @return 用户实体，不存在返回 null
     */
    public UsersEntity getByUsername(String username) {
        return this.lambdaQuery().eq(UsersEntity::getUsername, username).one();
    }

    /**
     * 根据邮箱查询用户（唯一性校验）
     *
     * @param email 邮箱地址
     * @return 用户实体，不存在返回 null
     */
    public UsersEntity getByEmail(String email) {
        return this.lambdaQuery().eq(UsersEntity::getEmail, email).one();
    }

    /**
     * 根据手机号查询用户（唯一性校验）
     *
     * @param phone 手机号
     * @return 用户实体，不存在返回 null
     */
    public UsersEntity getByPhone(String phone) {
        return this.lambdaQuery().eq(UsersEntity::getPhone, phone).one();
    }

    /**
     * 根据主键查询用户
     *
     * @param id 用户 ID
     * @return 用户实体，不存在返回 null
     */
    public UsersEntity getUserById(Long id) {
        return this.getById(id);
    }

    // ==================== 统计 ====================

    /**
     * 统计指定部门下的用户数量（删除部门前的关联检查）
     *
     * @param deptId 部门 ID
     * @return 该部门下的用户数
     */
    public long countByDeptId(Long deptId) {
        return this.lambdaQuery().eq(UsersEntity::getDeptId, deptId).count();
    }

    // ==================== 写入 ====================

    /**
     * 新增用户
     *
     * @param entity 用户实体（需预先设好所有字段）
     */
    public void createUser(UsersEntity entity) {
        this.save(entity);
    }

    /**
     * 按主键更新用户（仅更新非 null 字段）
     *
     * @param entity 用户实体（id 必填，其余字段按需设值）
     */
    public void updateUser(UsersEntity entity) {
        this.updateById(entity);
    }

    /**
     * 逻辑删除用户
     *
     * @param id 用户 ID
     */
    public void deleteUser(Long id) {
        this.removeById(id);
    }

    // ==================== 分页 ====================

    /**
     * 分页查询用户列表
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public Page<UsersEntity> listPage(int pageNum, int pageSize) {
        return this.page(new Page<>(pageNum, pageSize));
    }
}
