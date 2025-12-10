package com.hrms.hrm.repository;

import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.Notification;
import com.hrms.hrm.model.Notification.TargetRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByReceiver(Employee employee);

    List<Notification> findByTargetRole(TargetRole role);
    List<Notification> findByReceiverIdAndReadFalse(UUID receiverId);

}
