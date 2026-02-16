package com.hrms.hrm.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileAttachmentDto {

    private UUID id;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private String downloadUrl;

    private String module;

    private UUID referenceId;

    private Instant uploadedAt;

}
