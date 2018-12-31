app.controller('searchController',function($scope,$location,searchService){
	
	//定义搜索对象
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':'' };
	
	
	//搜索 
	$scope.search=function(){
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);//将String转为Integer
		searchService.search($scope.searchMap).success(
				function(response){
					$scope.resultMap=response;//搜索返回的结果 
					
					buildPageLabel();//调用 分页标签
				}
		)
	}
	
	//构建分页标签(totalPages 为总页数) 
	buildPageLabel=function(){
		//构建分页栏
		$scope.pageLabel=[];
		
		var firstPage=1;//开始页面
		var lastPage=$scope.resultMap.totalPages;//截至页面
		
		$scope.firstDot=true;//前面有点
		$scope.lastDot=true;//后面有点
		
		if($scope.resultMap.totalPages>5){//如果总页面大于5页，显示部分页码
			if($scope.searchMap.pageNo<=3){//如果当前页小于等于3
				lastPage=5;//前5页
				$scope.firstDot=false;//前面无点
			}else if($scope.searchMap.pageNo >= lastPage-2){//如果当前页面大于等于（截止页-2）
				firstPage=$scope.resultMap.totalPages-4;//后5页
				$scope.lastDot=false;//后面无点
			}else{//显示当前页为中心的5页
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
		}else{//小于5页  前后都无点
			$scope.firstDot=false;//前面无点
			$scope.lastDot=false;//后面无点
		}
		//循环产生页码标签  
		for( var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);
		}
		
	}
	
	//添加搜索项
	$scope.addSearchItem=function(key,value){
		if(key=='category' || key=='brand' || key=='price'){//如果点击的是分类或者是品牌或者价格
			$scope.searchMap[key]=value;
		}else{//如果点击的是规格
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//执行搜索
	}
	
	//移除复合搜索条件 
	$scope.removeSearchItem=function(key){
		if(key=='category' || key=='brand' || key=='price'){//如果点击的是分类或者是品牌或者价格
			$scope.searchMap[key]="";
		}else{//如果点击的是规格
			delete $scope.searchMap.spec[key];
		}
		$scope.search();//执行搜索
	}
	
	//根据页码查询
	$scope.queryByPage=function(pageNo){
		//页码验证 
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return;//直接返回不处理
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();//执行搜索
	}
	
	//判断当前页为第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	
	//判断当前页为最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}
	}
	
	//设置排序规则
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sort=sort;
		$scope.searchMap.sortField=sortField;
		$scope.search();//执行搜索
	}
	
	//判断关键字是不是品牌
	$scope.keywordsIsBrand=function(){
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0 ){//如果包含
				return true;
			}
		}
		return false;
	}
	
	//加载查询字符串
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords = $location.search()['keywords'];
		$scope.search();//执行搜索
	}
	
	
	
});