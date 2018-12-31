package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService {
	
	@Autowired
	private SolrTemplate solrTemplate;
		
	@Override
	public Map<String, Object> search(Map searchMap) {
		//关键字的空格处理
		String keywords=(String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));//去掉多余的空格
		
		Map<String, Object> map=new HashMap<>();
		
		/*Query query=new SimpleQuery("*:*");
		//添加查询条件 
		Criteria criteria= new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		
		map.put("rows", page.getContent());*/
		//1.查询列表
		map.putAll(searchList(searchMap));
		//2.查询分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		//3.查询品牌和规格列表
		String categoryName = (String) searchMap.get("category");
		if( !"".equals(categoryName) ) {//如果有分类名称 
			map.putAll(searchBrandAndSpecList(categoryName));
		}else {//如果没有分类名称，按照第一个查询 
			if(categoryList.size()>0) {
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			};
		}
		
		
		return map;
		
	}
	
	
	/**
	 * 根据关键字搜索列表 
	 * @param searchMap
	 * @return
	 */
	private Map<String, Object> searchList(Map searchMap) {
		Map<String, Object> map=new HashMap<>();
		
		//高亮显示
		HighlightQuery query= new SimpleHighlightQuery();
		//构建高亮选项对象
		HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//设置高亮的域 
		highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀  
		highlightOptions.setSimplePostfix("</em>");//高亮后缀 
		query.setHighlightOptions(highlightOptions);//设置高亮选项 
		
		//1.1按照关键字查询 
		Criteria criteria= new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		//1.2按照分类查询
		if( !"".equals(searchMap.get("category")) ) {
			Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery= new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		//1.3按照品牌查询
		if( !"".equals(searchMap.get("brand")) ) {
			Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filterQuery= new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		//1.4按照规格查询
		if( searchMap.get("spec")!=null ) {
			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
			for(String key:specMap.keySet()) {
				Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key) );
				FilterQuery filterQuery= new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		
		//1.5按照价格查询
		if( !"".equals(searchMap.get("price")) ) {
			String[] price = ( (String)searchMap.get("price") ).split("-");
			if( !price[0].equals("0") ) {//如果区间起点不等于0
				Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
				FilterQuery filterQuery= new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			if( !price[1].equals("*") ) {//如果区间终点不等于*
				Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
				FilterQuery filterQuery= new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		
		//1.6分页查询
		Integer pageNo = (Integer) searchMap.get("pageNo");//提取页码 
		if(pageNo==null) {
			pageNo=1;//默认第一页
		}
		Integer pageSize = (Integer) searchMap.get("pageSize");//每页记录数
		if(pageSize==null) {
			pageSize=20;//默认20
		}
		query.setOffset( (pageNo-1)*pageSize );//从第几条记录查询
		query.setRows(pageSize);//每页记录数
		
		
		//1.7排序
		String sortValue = (String) searchMap.get("sort");//ASC DESC
		String sortField = (String) searchMap.get("sortField");//排序字段
		if(sortValue!=null && !"".equals(sortValue)) {
			if(sortValue.equals("ASC")) {//升序
				Sort sort= new Sort(Sort.Direction.ASC,"item_"+sortField);
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")) {//降序
				Sort sort= new Sort(Sort.Direction.DESC,"item_"+sortField);
				query.addSort(sort);
			}
		}
		
		
		
		
		//高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		//高亮入口集合（每条记录的高亮入口）
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		for(HighlightEntry<TbItem> entry: entryList ) {
			//获取高亮列表（高亮域的个数）
			List<Highlight> highlightList = entry.getHighlights();
			/*for(Highlight h:highlightList ) {
				List<String> sns = h.getSnipplets();//每个域有可能存储多值
				System.out.println(sns);
			}*/
			if(highlightList.size()>0 && highlightList.get(0).getSnipplets().size()>0) {
				TbItem item = entry.getEntity();
				item.setTitle(highlightList.get(0).getSnipplets().get(0));
			}
		}
		
		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages());//返回总页数
		map.put("total", page.getTotalElements());//返回总记录数
		
		return map;
	}
	
	/**
	 * 查询分类列表
	 * @param searchMap
	 * @return
	 */
	private List searchCategoryList(Map searchMap) {
		List<String> list=new ArrayList<>();
		
		Query query=new SimpleQuery();
		//按照关键字查询 
		Criteria criteria= new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//设置分组选项
		GroupOptions groupOptions=new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		//得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		//根据列得到分组结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//得到分组结果入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//得到分组入口集合
		List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
		for(GroupEntry<TbItem> entry:entryList ) {
			list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中 
		}
		
		return list;
	}
	
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 根据分类名称 查询品牌分规格列表
	 * @param category
	 * @return
	 */
	private Map searchBrandAndSpecList(String category) {
		Map map = new HashMap<>();
		//根据分类名称获取模板Id
		Long typeId=(Long)redisTemplate.boundHashOps("itemCat").get(category);
		if(typeId!=null) {//非空判断
			//根据模板Id查询品牌列表
			List brandList=(List)redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList", brandList);//返回值添加品牌列表
			System.out.println("品牌的长度："+brandList.size());
			//根据模板 ID 查询规格列表 
			List specList=(List)redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList);//返回值添加规格列表
			System.out.println("规格的长度："+specList.size());
		}
		return map;
	}


	/**
	 * 导入数据
	 */
	@Override
	public void importList(List<TbItem> list) {
		for(TbItem item:list){
			//item.getSpec()的数据格式为:{"机身内存":"16G","网络":"联通3G"}
			Map specMap = JSON.parseObject(item.getSpec(), Map.class);//从数据库中提取规格json字符串转换为map
			item.setSpecMap(specMap);
		}
		
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		
	}


	/**
	 * 删除数据
	 */
	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		System.out.println("删除商品ID："+goodsIdList);
		
		Query query = new SimpleQuery("*:*");
		Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);	
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
	
	
}
