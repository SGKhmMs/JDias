/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgkhmjaes.jdias.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sgkhmjaes.jdias.domain.Tag;
import com.sgkhmjaes.jdias.service.mapper.AutoMapping;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author andrey
 */
public class ProfileDTO implements AutoMapping{

    private List<Tag> tags;

    private Long id;

    private LocalDate birthday;

    private String bio;

    private String location;

    private String gender;

    @JsonProperty("avatar")
    private AvatarDTO avatarDTO;

    private Boolean searchable;

    public ProfileDTO() {
    }

    @Override
    public String toString() {
        return "ProfileDTO: { id="+id+", AvatarDTO="+avatarDTO;
    }
    
    

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public AvatarDTO getAvatarDTO() {
        return avatarDTO;
    }

    public void setAvatarDTO(AvatarDTO avatarDTO) {
        this.avatarDTO = avatarDTO;
    }

    public Boolean getSearchable() {
        return searchable;
    }

    public void setSearchable(Boolean searchable) {
        this.searchable = searchable;
    }
    
    
}