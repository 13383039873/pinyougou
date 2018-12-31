package com.pinyougou.seckill.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;
import util.IdWorker;


@RestController
@RequestMapping("/pay")
public class PayController {
	
	@Reference(timeout=8000)
	private WeixinPayService weixinPayService;
	
	@Reference(timeout=8000)
	private SeckillOrderService seckillOrderService;
	
	/**
	 * 生成二维码
	 * @return
	 */
	@RequestMapping("/createNative")
	public Map createNative(){ 
		//1.获取当前用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		//2.获取秒杀订单（从redis）
		TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(username);
		//3.调用微信支付接口
		if(seckillOrder!=null) {
			return weixinPayService.createNative(seckillOrder.getId()+"", (long)(seckillOrder.getMoney().doubleValue()*100)+"" );
		}else {
			return new HashMap<>();
		}
		
	}
	
	
	/**
	 * 查询支付状态
	 * @param out_trade_no
	 * @return
	 */
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no){
		//1.获取当前用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Result result=null;
		int x=0;
		while(true) {
			//调用查询接口
			Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);
			//System.out.println(map.get("trade_state"));
			if(map==null) {//出错
				result=new Result(false, "支付出错");
				break;
			}
			if("SUCCESS".equals(map.get("trade_state"))){
				result=new Result(true, "支付成功");
				//保存订单到数据库
				seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no), map.get("transaction_id"));
				break;
			}
			
			try {
				Thread.sleep(3000);//间隔三秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//为了不让循环无休止地运行，我们定义一个循环变量，如果这个变量超过了这个值则退出循环，设置时间为 5 分钟
			x++;
			if(x>=100) {
				result=new Result(false, "二维码超时");
				//1.调用微信的关闭订单接口
				Map<String,String> payResult = weixinPayService.closePay(out_trade_no);
				if(payResult!=null && "FAIL".equals(payResult.get("return_code"))) {
					if("ORDERPAID".equals(payResult.get("err_code"))) {
						result=new Result(true, "支付成功");
						//保存订单到数据库
						seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no), map.get("transaction_id"));
					}
				}
				//删除订单
				if(result.isSuccess()==false) {	
					seckillOrderService.deleteOrderFromRedis(username, Long.valueOf(out_trade_no));
				}
				break;
				
			}
		}
		
		return result;
	}

}
