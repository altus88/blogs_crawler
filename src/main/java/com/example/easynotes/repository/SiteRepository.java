package com.example.easynotes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.easynotes.model.Site;

/**
 * Created by anush on 18.07.18.
 */
@Repository
public interface SiteRepository extends JpaRepository<Site, Long>
{

}