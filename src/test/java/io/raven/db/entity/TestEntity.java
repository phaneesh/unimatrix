package io.raven.db.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Calendar;

@Entity
@Table(name = "test_entity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TestEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @NotNull
  @Column(name = "ext_id")
  private String externalId;

  @Column(name = "text", nullable = false)
  @NotNull
  private String text;

  @Column(name = "amount")
  private BigDecimal amount;

  @Column(name = "partition_id", nullable = false)
  private int partitionId;


  @PrePersist
  public void assignPartition() {
    this.partitionId = Calendar.getInstance().get(Calendar.YEAR);
  }


}
