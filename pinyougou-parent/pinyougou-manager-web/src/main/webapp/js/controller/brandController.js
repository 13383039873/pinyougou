		//定义controller
		app.controller('brandController',function($scope,$controller,brandService){
			
			$controller('baseController',{$scope:$scope});//继承
			
			//查询品牌列表
			$scope.findAll=function(){
				brandService.findAll().success(
					function(response){
						$scope.list=response;
					}		
				)	
			}
			
		
			//分页
			$scope.findPage=function(page,rows){
				brandService.findPage(page,rows).success(
						function(response){
							$scope.list=response.rows;
							$scope.paginationConf.totalItems=response.total;
						}		
					)	
			}
			
			
			//新增
			$scope.add=function(){
				brandService.add($scope.entity).success(
					function(response){
						if(response){
							$scope.reloadList();//刷新列表
						}else{
							alert(response.message);
						}
					}		
				)	
			}
			
			
			//查询一个实体
			$scope.findOne=function(id){
				brandService.findOne(id).success(
					function(response){
						$scope.entity=response;
					}		
				)
			}
			
			
			//保存
			$scope.save=function(){
				var serviceObject;//服务层对象  
				if($scope.entity.id!=null){//如果有ID
					serviceObject=brandService.update($scope.entity);//修改
				}else{
					serviceObject=brandService.add($scope.entity);//增加
				}
				serviceObject.success(
						function(response){
							if(response.success){
								$scope.reloadList();//刷新
							}else{
								alert(response.message);
							}
						})
			}
			
			
			//批量删除
			$scope.dele=function(){
				//获取选中的框id的数组
				brandService.dele($scope.selectIds).success(
					function(response){
						if(response.success){
							$scope.reloadList();//刷新列表
						}else{
							alert(response.message);
						}
					}	
				);
			}
			
			
			$scope.searchEntity={};//定义搜索对象
			//根据条件查询
			$scope.search=function(page,rows){
				brandService.search(page,rows,$scope.searchEntity).success(
						function(response){
							$scope.list=response.rows;
							$scope.paginationConf.totalItems=response.total;
						}		
					)	
			}
			
		
		});