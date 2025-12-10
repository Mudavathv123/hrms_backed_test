package com.hrms.hrm.repository;

import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmployee(Employee employee);

    List<User> findByRole(User.Role role);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts =:attempts WHERE u.username =:username")
    void updateFailedLoginAttempts(@Param("username") String username, @Param("attempts") Integer attempts);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.lastLogin =:loginTime WHERE u.username =:username")
    void lastLogin(@Param("username") String username, @Param("loginTime")LocalDateTime loginTime);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil =:lockedUntil WHERE u.username =:username")
    void lockUser(@Param("username") String username, @Param("lockedUntil") LocalDateTime lockedUntil);
}
