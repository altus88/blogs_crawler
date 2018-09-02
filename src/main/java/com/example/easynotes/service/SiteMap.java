package com.example.easynotes.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by anush on 02.08.18.
 */
public class SiteMap
{
	String id;
	List<String> startUrl;
	List<Selector> selectors;

	private Selector getSelector(String id)
	{
		return selectors.stream().filter( s -> s.id.equals(id)).collect(Collectors.toList()).get(0);
	}


	@Override
	public String toString()
	{
		return "{id:"+id+" startUrl:"+startUrl+" selectors:"+selectors+"}";
	}

	public Map<String, List<Selector>> buildTree()
	{
		Map<String, List<Selector>> selectorIdToChildrenSelectors = new HashMap<>();

		for (Selector selector : selectors)
		{
			for (String parentSelectorId: selector.parentSelectors)
			{
				if (!selectorIdToChildrenSelectors.containsKey(parentSelectorId))
				{
					selectorIdToChildrenSelectors.put(parentSelectorId, new ArrayList<>());
				}
				selectorIdToChildrenSelectors.get(parentSelectorId).add(selector);
			}
		}

		return selectorIdToChildrenSelectors;
	}
}
