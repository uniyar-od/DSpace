/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.util;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;

public class CollectionUtils {
    
    private static ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();
    
	public static CollectionsTree getCollectionsTree(Context context, List<Collection> collections, boolean skipCollection)
			throws SQLException {
		
		if (collections == null || collections.isEmpty()) {
			return null;
		}
		
		String skipHandles = configurationService.getProperty("submission.skip.handle");
		//TODO: Should we create a plugin?
		

		Map<Community, List<Collection>> map = new HashMap<Community, List<Collection>>();
		for (Collection col : collections) {
		
			String handle = col.getHandle();
			if (skipCollection && StringUtils.contains(handle, skipHandles)) 
			{
				continue;
			}
			Community com = (Community) col.getCollectionService().getParentObject(context, col);
			if (map.containsKey(com)) 
			{
				map.get(com).add(col);
			} else {
				List<Collection> cols = new ArrayList<>();
				cols.add(col);
				map.put(com, cols);
			}
		}
		
		List<CollectionsTree> treeBranches = new ArrayList<CollectionsTree>();
		
		for (Community com : map.keySet()) 
		{
			CollectionsTree tree = new CollectionsTree();
			tree.setCurrent(com);
			tree.setCollections(map.get(com));
			treeBranches.add(tree);
		}
		
		for (int i = 0; i < treeBranches.size(); i++) 
		{
			CollectionsTree collectionsTree = treeBranches.get(i);
			treeBranches.set(i, buildBranch(context, collectionsTree));
		}
		
		List<CollectionsTree> trees = new ArrayList<CollectionsTree>();
		
		do
		{
			trees.add(mergeBranch(treeBranches));
		} while (treeBranches.size() > 0);
		
		Collections.sort(trees);
		CollectionsTree finalCollectionsTree = new CollectionsTree();
		finalCollectionsTree.setSubTree(trees);
		
		return finalCollectionsTree;
	}
	
	/**
	 * Takes the first branch at position 0 in the provided CollectionsTree List and returns a CollectionsTree containing all trees with the same top community
	 * @see org.dspace.app.util.CollectionUtils.mergeBranchRecursive
	 */
	private static CollectionsTree mergeBranch(List<CollectionsTree> trees) 
	{
		CollectionsTree currTree = trees.get(0);
		trees.remove(0);
			for (int i = 0; i < trees.size(); i++) 
			{			
				CollectionsTree current = trees.get(i);
				if (currTree.getCurrent().getName().equals(current.getCurrent().getName())) 
				{
					mergeBranchRecursive(current, currTree);
					trees.remove(i);
					i--;
				}
			}
		return currTree;
	}
	
	/**
	 * The recursive part of mergeBranch(List<CollectionsTree>)
	 * @param source The CollectionsTree that needs to be merged with target
	 * @param target The CollectionsTree that is being built
	 * @see org.dspace.app.util.CollectionUtils.mergeBranch
	 */
	private static void mergeBranchRecursive(CollectionsTree source, CollectionsTree target) 
	{
		if (source.getSubTree() == null) 
		{
			if (target.getCollections() == null) 
			{
				target.setCollections(source.getCollections());
				return;
			}else
			{
				target.getCollections().addAll(source.getCollections());
				return;
			}
		}else {
			if (target.getSubTree() == null)
			{
				target.setSubTree(source.getSubTree());
				target.getSubTree().sort(null);
				return;
			}
		}
		for (CollectionsTree sourceTree : source.getSubTree()) 
		{
			for (CollectionsTree targetTree : target.getSubTree()) 
			{
				if (sourceTree.getCurrent().getName().equals(targetTree.getCurrent().getName())) 
				{
					mergeBranchRecursive(sourceTree, targetTree);
					return;
				}
			}
		}
		target.getSubTree().addAll(source.getSubTree());
		target.getSubTree().sort(null);
		return;
	}
	
	/**
	 * Builds all single branches of the provided CollectionsTree
	 */
	private static CollectionsTree buildBranch(Context context, CollectionsTree tree) throws SQLException
	{
		Community current = tree.getCurrent();
		Community com = (Community) current.getDSpaceObjectService().getParentObject(context, current);
		if (com != null) 
		{
			CollectionsTree tree2 = new CollectionsTree();
			tree2.setCurrent(com);
			List<CollectionsTree> tempList = new ArrayList<CollectionsTree>();
			tempList.add(tree);
			tree2.setSubTree(tempList);
			return buildBranch(context, tree2);
		}else {
			return tree;
		}
		
	}
	
}