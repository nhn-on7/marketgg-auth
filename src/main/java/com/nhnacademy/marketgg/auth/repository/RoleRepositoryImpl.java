package com.nhnacademy.marketgg.auth.repository;

import com.nhnacademy.marketgg.auth.entity.QAuthRole;
import com.nhnacademy.marketgg.auth.entity.QRole;
import com.nhnacademy.marketgg.auth.entity.Role;
import java.util.List;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class RoleRepositoryImpl extends QuerydslRepositorySupport implements RoleRepositoryCustom {

    public RoleRepositoryImpl() {
        super(Role.class);
    }

    @Override
    public List<Role> findRolesByAuthId(Long id) {

        QRole role = QRole.role;
        QAuthRole authRole = QAuthRole.authRole;

        return from(role)
            .innerJoin(authRole).on(role.id.eq(authRole.id.roleId))
            .where(authRole.id.authId.eq(id))
            .fetch();
    }

}
