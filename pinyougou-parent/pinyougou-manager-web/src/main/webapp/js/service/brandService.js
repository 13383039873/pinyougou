//定义service
		app.service('brandService',function($http){
			
			//查询所有
			this.findAll=function(){
				return $http.get('../brand/findAll.do');
			}
			
			//分页查询
			this.findPage=function(page,rows){
				return $http.get('../brand/findPage.do?page='+page+'&rows='+rows);
			}
			
			
			//查询一个实体
			this.findOne=function(id){
				return $http.get('../brand/findOne.do?id='+id);
			}
			
			//新增
			this.add=function(entity){
				return $http.post('../brand/add.do',entity);
			}
			
			//修改
			this.update=function(entity){
				return $http.post('../brand/update.do',entity);
			}
			
			//删除
			this.dele=function(ids){
				return $http.post('../brand/delete.do?ids='+ids);
			}
			
			//根据条件查询
			this.search=function(page,rows,searchEntity){
				return $http.post('../brand/search.do?page='+page+'&rows='+rows, searchEntity);
			}
			
			//下拉列表
			this.selectOptionList=function(){
				return $http.get('../brand/selectOptionList.do');
			}
			
			
		});
		