package com.apass.zufang.schedule;

import com.apass.zufang.domain.entity.ZfangSpiderHouseEntity;
import com.apass.zufang.service.spider.HouseSpiderService;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author xiaohai
 * @date 2018/4/19.
 */
@Component
@Configurable
@EnableScheduling
@Controller
@RequestMapping("/noauth/spider")
public class SpiderHouseListTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpiderHouseListTask.class);
    /**
     * 蘑菇房源列表页根路径
     */
    private static final String BASE_URLLIST = "http://www.mogoroom.com/list";

    /**
     * 蘑菇房源详情页根路径
     */
    public static final String BASE_URLDETAIL = "http://www.mogoroom.com";

    /**
     * 要爬的页面
     */
    private static final Integer PAGENUM = 50;

    @Autowired
    private HouseSpiderService houseSpiderService;

    /**
     * 每天00:10分跑此job
     * 思路：1，根据url和page去查对应页数数据，放入数据库存
     * 2，放之前判断是否已经存在，根据
     */
    @Scheduled(cron = "0 10 0 * * *")
    public void initExtHouseList(){
        try{
            for (int i=0; i<PAGENUM; i++){
                houseSpiderService.spiderMogoroomPageList(BASE_URLLIST,i);
            }

        }catch (Exception e){
            LOGGER.error("获取数据失败！------Exception=====>{}",e);
        }
    }
    @RequestMapping("/houseList2")
    public void initExtHouseList2(){
        try{
            for (int i=0; i<PAGENUM; i++){
                houseSpiderService.spiderMogoroomPageList(BASE_URLLIST,i);
            }

        }catch (Exception e){
            LOGGER.error("获取数据失败！------Exception=====>{}",e);
        }
    }

    /**
     * 每天04:10跑
     * 初如何蘑菇租房房源详情表
     * 思路：1，从t_zfang_spider_house表中获取所有未被删除的url，
     * 2，拼接BASE_URLDETAIL爬取相关数据，插入t_zfang_house表中
     * 3，插入成功后，删除t_zfang_spider_house表中对应数据
     */
    @Scheduled(cron = "0 10 4 * * *")
    public void initExtHouseDetail(){
        List<String> urls = Lists.newArrayList();
        try{
            //去查询spider表，获取其中中的url放入urls中
            List<ZfangSpiderHouseEntity> list = houseSpiderService.listAllExtHouse();
            if(CollectionUtils.isNotEmpty(list)){
                for(ZfangSpiderHouseEntity entity : list){
                    urls.add(BASE_URLDETAIL+entity.getUrl());
                }
                houseSpiderService.batchParseMogoroomHouse(urls);
            }

        }catch (Exception e){
            LOGGER.error("获取数据失败！------Exception=====>{}",e);
        }
    }


    @RequestMapping("/initExtHouseDetail2")
    public void initExtHouseDetail2(){
        List<String> urls = Lists.newArrayList();
        try{
            //去查询spider表，获取其中中的url放入urls中
            List<ZfangSpiderHouseEntity> list = houseSpiderService.listAllExtHouse();
            if(CollectionUtils.isNotEmpty(list)){
                for(ZfangSpiderHouseEntity entity : list){
                    urls.add(BASE_URLDETAIL+entity.getUrl());
                }

                houseSpiderService.batchParseMogoroomHouse(urls);
            }

        }catch (Exception e){
            LOGGER.error("获取数据失败------Exception=====>{}",e);
        }
    }
}