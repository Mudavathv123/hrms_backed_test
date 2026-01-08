package com.hrms.hrm.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllowedLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String locationName;
    private Double latitude;
    private Double longitude;
    private Double radiusInMeters;
    private LocationType locationType;

    public enum LocationType {
        OFFICE, REMOTE, HYBRID
    }

}
