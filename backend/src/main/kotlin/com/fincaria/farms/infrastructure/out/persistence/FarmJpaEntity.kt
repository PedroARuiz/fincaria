package com.fincaria.farms.infrastructure.out.persistence

import com.fincaria.farms.domain.CropType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize
import java.util.UUID

@Entity
@Table(name = "farms")
class FarmJpaEntity(
    @Id
    var id: UUID,
    @Column(nullable = false)
    var userId: UUID,
    @Column(nullable = false, unique = true, length = 20)
    var cadastralReference: String,
    @ElementCollection(targetClass = CropType::class)
    @CollectionTable(
        name = "farm_crop_types",
        joinColumns = [JoinColumn(name = "farm_id")],
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "crop_type", nullable = false, length = 50)
    // Batch-load this collection for up to 50 farms per extra query, so paging a
    // list of farms costs O(page/50) collection queries instead of one per farm
    // (the N+1). A fetch join would collapse it to a single query but cannot be
    // combined with database-side pagination.
    @BatchSize(size = 50)
    var cropTypes: MutableSet<CropType>,
    @Column(name = "verification_status", nullable = false, length = 20)
    var verificationStatus: String,
    @Column(name = "cadastral_municipality")
    var cadastralMunicipality: String?,
    @Column(name = "cadastral_province")
    var cadastralProvince: String?,
    @Column(name = "cadastral_area_hectares")
    var cadastralAreaHectares: Double?,
)
