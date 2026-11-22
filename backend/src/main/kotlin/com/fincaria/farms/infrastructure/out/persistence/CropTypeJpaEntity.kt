package com.fincaria.farms.infrastructure.out.persistence

import com.fincaria.farms.domain.CropType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "crop_types")
class CropTypeJpaEntity(
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "code", length = 50)
    var code: CropType,
    @Column(nullable = false)
    var label: String,
    @Column
    var description: String?,
    @Column(name = "display_order", nullable = false)
    var displayOrder: Int,
)
