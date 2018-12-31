//基本控制器
app.controller('baseController',function($scope){
	
	//分页控制配置
	$scope.paginationConf = {	
			currentPage: 1,
			totalItems: 10,
			itemsPerPage: 10,
			perPageOptions: [10, 20, 30, 40, 50],
			onChange: function(){
				$scope.reloadList();//重新加载//刷新列表
			}		
	}
	
	
	//重新加载列表 数据
	$scope.reloadList=function(){
		//切换页码
		$scope.search( $scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
	}
	
	

	$scope.selectIds=[];//定义一个空数组,用于存放选中的id的值
	
	//更新复选框
	$scope.updateSelection=function($event,id){
		if($event.target.checked){//如果被选中,就被添加到数组中
			$scope.selectIds.push(id);
		}else{
			var idx = $scope.selectIds.indexOf(id);
			$scope.selectIds.splice(idx,1);//如果不被选中,就不添加到数组中
		}
	}
	
	//提取 json 字符串数据中某个属性，返回拼接字符串 逗号分隔 
	$scope.jsonToString=function(jsonString,key){ 
		
		var json=JSON.parse(jsonString);//将 json 字符串转换为 json 对象
		
		var value="";
		
		for(var i = 0; i<json.length;i++){
			if(i>0){
				value+=","
			}
			value+=json[i][key];
		}
		
		return value;
	
	}
	
	//从集合中按照key查询对象
	$scope.searchObjectByKey=function(list,key,keyValue){
		for(var i=0;i<list.length;i++){
			if(list[i][key]==keyValue){
				return list[i];
			}
		}
		return null;
	}

	
	
})