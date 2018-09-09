package com.example.easynotes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.easynotes.model.Item;

/**
 * Created by anush on 23.08.18.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long>
{
	List<Item> getByTextContaining(String text);

	List<Item> getByContentContaining(String content);

	List<Item> getBySiteId(Long siteId);
}
