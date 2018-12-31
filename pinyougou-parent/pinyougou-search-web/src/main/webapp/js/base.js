//定义模块
var app=angular.module('pinyougou',[]);
/*$sce 服务写成过滤器*/ // 定义过滤器
app.filter('trustHtml',['$sce',function($sce){ //第一个'$sce'代表引入$sce服务,第二个$sce表明在该过滤器中注入该服务
	return function(data){ //data代表要过滤的数据
		return $sce.trustAsHtml(data);//返回过滤后的数据
	}
}])
