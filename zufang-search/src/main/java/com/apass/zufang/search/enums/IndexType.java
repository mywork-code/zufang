package com.apass.zufang.search.enums;

/**
 * Created by Administrator on 2017/5/23.
 */


import com.apass.zufang.search.entity.HouseEs;
import com.apass.zufang.search.entity.IdAble;
import com.apass.zufang.search.entity.UpdatedObject;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * Created by xianzhi.wang on 2017/5/22.
 */
public enum IndexType {
    HOUSE("house") {
        @Override
        public String getMapper() {
            return "/esmapper/houseMapper.txt";
        }

        @Override
        public Class<? extends IdAble> getTypeClass() {
            return HouseEs.class;
        }
    };
    private String dataName;

    IndexType(String name) {
        this.dataName = name;
    }

    public String getDataName() {
        return dataName;
    }

    abstract public String getMapper();

    abstract public Class getTypeClass();

    /**
     * 生产者会一直阻塞直到所添加到队列的元素被某一个消费者所消费
     */
    private TransferQueue<UpdatedObject<? extends IdAble>> transferQueue = new LinkedTransferQueue<>();

    public UpdatedObject<? extends IdAble> takeQueue() throws InterruptedException {
        return transferQueue.take();
    }

    public boolean offerQueue(UpdatedObject<? extends IdAble> updatedObject) {
        if (transferQueue.tryTransfer(updatedObject)) {
            return true;
        }
        return transferQueue.offer(updatedObject);
    }
}
