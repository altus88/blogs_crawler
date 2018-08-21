package com.example.easynotes.service;

import java.util.ArrayList;
import java.util.List;
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

	public Selector buildTree()
	{
		Selector rootSelector = new Selector();

		for (Selector selector : selectors)
		{

			for (String parentSelectorId : selector.parentSelectors)
			{
				if (parentSelectorId.equalsIgnoreCase("_root"))
				{
					selector.parents.add(rootSelector);
					rootSelector.children.add(selector);
				}
				else
				{
					Selector parentSelector = getSelector(parentSelectorId);
					if(!parentSelector.equals(selector))
					{
						selector.parents.add(parentSelector);
						parentSelector.children.add(selector);
					}
				}
			}
		}

		return rootSelector;
	}
}
