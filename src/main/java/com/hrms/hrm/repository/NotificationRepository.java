package com.hrms.hrm.repository;

import com.hrms.hrm.model.Notification;
import com.hrms.hrm.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n " +
           "LEFT JOIN FETCH n.sender " +
           "LEFT JOIN FETCH n.receiver " +
           "WHERE n.receiver = :receiver " +
           "ORDER BY n.createdDate DESC")
    List<Notification> findByReceiver(@Param("receiver") Employee receiver);

    @Query("SELECT n FROM Notification n " +
           "LEFT JOIN FETCH n.sender " +
           "LEFT JOIN FETCH n.receiver " +
           "WHERE n.receiver.id = :receiverId AND n.read = false " +
           "ORDER BY n.createdDate DESC")
    List<Notification> findByReceiverIdAndReadFalse(@Param("receiverId") UUID receiverId);

    @Query("SELECT n FROM Notification n " +
           "WHERE n.receiver.id = :receiverId AND n.read = false " +
           "ORDER BY n.createdDate DESC")
    Page<Notification> findByReceiverIdAndReadFalse(@Param("receiverId") UUID receiverId, Pageable pageable);
    

    @Query("SELECT n FROM Notification n " +
           "WHERE n.receiver.id = :receiverId " +
           "ORDER BY n.createdDate DESC")
    Page<Notification> findByReceiverIdWithPagination(@Param("receiverId") UUID receiverId, Pageable pageable);

    @Query("SELECT n FROM Notification n " +
           "LEFT JOIN FETCH n.receiver " +
           "WHERE n.sender.id = :senderId " +
           "ORDER BY n.createdDate DESC")
    List<Notification> findBySenderId(@Param("senderId") UUID senderId);

    @Query("SELECT COUNT(n) FROM Notification n " +
           "WHERE n.receiver.id = :receiverId AND n.read = false")
    long countUnreadByReceiverId(@Param("receiverId") UUID receiverId);
    

    @Query("SELECT n FROM Notification n " +
           "WHERE n.receiver.id = :receiverId AND n.createdDate > :since " +
           "ORDER BY n.createdDate DESC")
    List<Notification> findRecentNotifications(@Param("receiverId") UUID receiverId, @Param("since") LocalDateTime since);
    

    @Query("SELECT n FROM Notification n " +
           "WHERE n.targetRole = :targetRole " +
           "ORDER BY n.createdDate DESC")
    List<Notification> findByTargetRole(@Param("targetRole") Notification.TargetRole targetRole);
}
