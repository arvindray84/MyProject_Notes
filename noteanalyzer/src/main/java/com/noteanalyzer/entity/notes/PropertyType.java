package com.noteanalyzer.entity.notes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.noteanalyzer.entity.AbstractEntity;

import lombok.ToString;

@Entity
@Table(name="PROPERTY_TYPE")
@ToString(callSuper = true)
@NamedQueries({ @NamedQuery(name = "GET_PROPERTY_TYPE_BY_TYPE",query="select p from PropertyType p  where p.propertyType =:propertyType")})
public class PropertyType extends AbstractEntity { 
	
	private static final long serialVersionUID = 3343766906297480204L;
	
	public static final String GET_PROPERTY_TYPE_BY_TYPE = "GET_PROPERTY_TYPE_BY_TYPE";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="P_TYPE_ID")
    private Integer P_TYPE_ID;
	
    @Column(name="PROPERTY_TYPE")
	private String propertyType;
	
    @Column(name="DESCRIPTION")
	private String description;

	/**
	 * @return the propertyType
	 */
	public String getPropertyType() {
		return propertyType;
	}

	/**
	 * @param propertyType the propertyType to set
	 */
	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
    
    
}
