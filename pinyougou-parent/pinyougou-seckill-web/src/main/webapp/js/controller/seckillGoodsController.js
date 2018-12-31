//控制层
app.controller('seckillGoodsController' ,function($scope,$location,seckillGoodsService,$interval){ 
	//读取列表数据绑定到表单中
	$scope.findList=function(){
		seckillGoodsService.findList().success(
				function(response){
					$scope.list=response;
				} 
		);
	}
	
	//查询实体（一个）
	$scope.findOne=function(){ 
		seckillGoodsService.findOne($location.search()['id']).success(
				function(response){
					$scope.entity= response;
					
					//倒计时开始
					//获取从结束时间到当前时间的秒数
					allsecond = Math.floor( ( new Date($scope.entity.endTime).getTime()-(new Date().getTime()) )/1000 );//总秒数
					time = $interval(function(){
						if(allsecond>0){
							allsecond=allsecond-1;
							$scope.timeString = convertTimeString(allsecond);
						}else{
							$interval.cancel(time);
							alert("秒杀服务结束");
						}
					},1000);
				
				}
		);
	}
	
	
	//转换秒为 天小时分钟秒格式 XXX 天 10:22:33
	convertTimeString=function(allsecond){
		var days=Math.floor( allsecond/(60*60*24) );//天数
		var hours=Math.floor( (allsecond-days*60*60*24)/(60*60) );//小时数
		var minutes=Math.floor( (allsecond-days*60*60*24-hours*60*60)/60 );//分钟数
		var seconds= allsecond-days*60*60*24-hours*60*60-minutes*60;//秒数
		var timeString="";
		if(days>0){
			timeString+=days+"天";
		}
		return timeString+hours+":"+minutes+":"+seconds;
	}
	
	
	//提交订单
	$scope.submitOrder=function(){
		seckillGoodsService.submitOrder($scope.entity.id).success(
				function(response){
					if(response.success){
						alert("下单成功，请在 5 分钟内完成支付");
						location.href="pay.html";
					}else{
						alert(response.message);
					}
				}
		);
	
	}
	
});