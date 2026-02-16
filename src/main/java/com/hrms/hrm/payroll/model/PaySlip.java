package com.hrms.hrm.payroll.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "payslip")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaySlip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "payroll_id", nullable = false, unique = true)
    private Payroll payroll;

    /**
     * S3 URL or local file path for the PDF
     * Format: s3://bucket-name/payslips/uuid.pdf or /payslips/uuid.pdf for local
     */
    @Column(name = "pdf_url", nullable = false, length = 500)
    private String pdfUrl;

    /**
     * S3 object key for direct access (without presigned URL)
     */
    @Column(name = "s3_key", length = 500)
    private String s3Key;

    /**
     * File size in bytes
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * File MIME type
     */
    @Column(name = "file_type", length = 50)
    private String fileType;

    /**
     * Timestamp when payslip was generated
     */
    @CreationTimestamp
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    /**
     * Timestamp when payslip was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Status: PENDING, GENERATED, SENT, VIEWED
     */
    @Column(name = "status", columnDefinition = "VARCHAR(50) DEFAULT 'GENERATED'")
    private String status;

    /**
     * Email sent timestamp
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * Employee viewed timestamp
     */
    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;
}

