app.controller("contentController",function($scope,$location,contentService){
	
	
	$scope.contentList=[];//广告集合 
	//根据分类 ID 查询广告列表 
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(
			function(response){
				$scope.contentList[categoryId] = response;
			}	
		);
	}
	
	//搜索（传递参数）
	$scope.search=function(){
		location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
	}
	
	
})
