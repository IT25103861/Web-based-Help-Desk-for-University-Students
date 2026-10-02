package com.campus.helpdesk.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Categories")
public class Category implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private int categoryId;

    @Column(name = "category_name", unique = true, nullable = false)
    private String categoryName;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "sla_hours")
    private Integer slaHours;

    public Category() {}

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Integer getSlaHours() { return slaHours; }
    public void setSlaHours(Integer slaHours) { this.slaHours = slaHours; }
}
