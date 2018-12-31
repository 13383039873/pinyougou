package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {
	
	@Autowired
	private TbBrandMapper brandMapper;

	//查询所有
	@Override
	public List<TbBrand> findAll() {
		return brandMapper.selectByExample(null);
	}
	
	//查询分页结果
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		
		PageHelper.startPage(pageNum, pageSize);//分页
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	//新增
	@Override
	public void add(TbBrand tbBrand) {
		brandMapper.insert(tbBrand);
	}
	
	//查询
	@Override
	public TbBrand findOne(Long id) {
		return brandMapper.selectByPrimaryKey(id);
	}

	//更新
	@Override
	public void update(TbBrand brand) {
		brandMapper.updateByPrimaryKey(brand);	
	}

	//删除
	@Override
	public void delete(Long[] ids) {
		for(Long id: ids) {
			brandMapper.deleteByPrimaryKey(id);
		}
		
	}

	//根据条件分页查询
	@Override
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);//分页
		TbBrandExample example = new TbBrandExample();
		Criteria criteria = example.createCriteria();
		if(brand!=null) {
			if(brand.getName()!=null && brand.getName().length() > 0) {
				criteria.andNameLike("%"+brand.getName()+"%");
			}
			if(brand.getFirstChar()!=null && brand.getFirstChar().length() >0) {
				//criteria.andFirstCharLike("%"+brand.getFirstChar()+"%");
				criteria.andFirstCharEqualTo(brand.getFirstChar());
			}
		}
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example );
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 下拉列表
	 */
	@Override
	public List<Map> selectOptionList() {
		return brandMapper.selectOptionList();
	}
	
	

}
