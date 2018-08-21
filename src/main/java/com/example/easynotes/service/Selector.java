package com.example.easynotes.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anush on 02.08.18.
 */
public class Selector
{
	String id;
	String type;
	String selector;
	List<String> parentSelectors;

	boolean multiple;

	@Override
	public String toString()
	{
		StringBuilder strBld = new StringBuilder();
		strBld.append("children = {");
		for (Selector child : children)
		{
			strBld.append(child).append("\n");

		}
		strBld.append("}");
		return "{id:"+id+" type:"+type+" selector:"+selector+" parentSelectors:"+parentSelectors+" multiple:"+multiple+"} \n \t\t"+strBld.toString();
	}

	public String toString(String indent)
	{
		StringBuilder strBld = new StringBuilder();
		strBld.append(indent).append("{id:"+id+" type:"+type+" selector:"+selector+" parentSelectors:"+parentSelectors+" multiple:"+multiple+"} \n ");

		for (Selector child : children)
		{
			strBld.append(child.toString(indent+"\t")).append("\n");

		}
		return strBld.toString();
	}


	List<Selector> parents  = new ArrayList<>();
	List<Selector> children  = new ArrayList<>();
}
