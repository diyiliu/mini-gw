package com.tiza.util.task.impl;

import com.tiza.util.CommonUtil;
import com.tiza.util.bean.CanPackage;
import com.tiza.util.bean.NodeItem;
import com.tiza.util.cache.ICache;
import com.tiza.util.dao.CanDao;
import com.tiza.util.entity.CanInfo;
import com.tiza.util.task.ITask;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;

/**
 * Description: RefreshCanTask
 * Author: DIYILIU
 * Update: 2016-04-21 10:31
 */

public class RefreshCanTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICache canCacheProvider;

    @Resource
    private CanDao canDao;

    @Override
    public void execute() {
        logger.info("刷新功能集配置...");

        List<CanInfo> canInfoList = canDao.selectCan();

        refresh(canInfoList, canCacheProvider);
    }

    private void refresh(List<CanInfo> canInfoList, ICache canCache) {

        if (canInfoList == null || canInfoList.size() < 1) {
            logger.warn("功能集数据为空！");
            return;
        }

        Set oldKeys = canCacheProvider.getKeys();
        Set tempKeys = new HashSet<>(canInfoList.size());

        for (CanInfo canInfo : canInfoList) {
            dealCan(canInfo);
            canCache.put(canInfo.getSoftVersion(), canInfo);
            tempKeys.add(canInfo.getSoftVersion());
        }

        Collection subKeys = CollectionUtils.subtract(oldKeys, tempKeys);

        subKeys.forEach(key -> canCache.remove(key));
    }

    private void dealCan(CanInfo canInfo) {
        String xml = canInfo.getFunctionXml();
        if (CommonUtil.isEmpty(xml)) {
            return;
        }

        try {
            Document document = DocumentHelper.parseText(xml);
            List<Node> rootPackageNodes  = document.selectNodes("root/can/package");

            Map<String, CanPackage> canPackages = new HashedMap(rootPackageNodes.size());
            Map emptyValues = new HashedMap();

            int pidLength = 0;
            for (Node node: rootPackageNodes){
                CanPackage canPackage = dealPackage(node);
                canPackages.put(canPackage.getPackageId(), canPackage);

                emptyValues.putAll(canPackage.getEmptyValues());
                pidLength = canPackage.getIdLength();
            }
            canInfo.setCanPackages(canPackages);

            canInfo.setPidLength(pidLength);
            canInfo.setEmptyValues(emptyValues);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private CanPackage dealPackage(Node packageNode){
        String packageId = packageNode.valueOf("@id");
        int length = Integer.parseInt(packageNode.valueOf("@length"));

        CanPackage canPackage = new CanPackage(packageId, length);

        List<Node> nodeItems = packageNode.selectNodes("item");

        List<NodeItem> itemList = new ArrayList<>(nodeItems.size());
        Map emptyValues = new HashedMap(nodeItems.size());
        for (Node node: nodeItems){
            NodeItem nodeItem= dealItem(node);
            itemList.add(nodeItem);
            emptyValues.put(nodeItem.getField().toUpperCase(), null);
        }

        canPackage.setItemList(itemList);
        canPackage.setEmptyValues(emptyValues);

        return canPackage;
    }


    private NodeItem dealItem(Node itemNode) {
        NodeItem itemBean = new NodeItem();
        String nameKey = itemNode.selectSingleNode("nameKey").getText();
        String name = itemNode.selectSingleNode("name").getText();
        String type = itemNode.selectSingleNode("type").getText();
        String endian = itemNode.selectSingleNode("endian") == null ? "big" : itemNode.selectSingleNode("endian").getText();
        Node position = itemNode.selectSingleNode("position");
        Node byteNode = position.selectSingleNode("byte");
        Node bitNode = byteNode.selectSingleNode("bit");
        String byteStart = byteNode.valueOf("@start");
        String byteLen = byteNode.valueOf("@length");
        if (null == bitNode) {
            itemBean.setOnlyByte(true);
        } else {
            itemBean.setOnlyByte(false);
            String bitStart = bitNode.valueOf("@start");
            String bitLen = bitNode.valueOf("@length");
            itemBean.setBitStart(Integer.parseInt(bitStart));
            itemBean.setBitLen(Integer.parseInt(bitLen));
        }
        String expression = itemNode.selectSingleNode("expression").getText();
        String field = itemNode.selectSingleNode("field").getText();
        itemBean.setNameKey(nameKey);
        itemBean.setName(name);
        itemBean.setType(type);
        itemBean.setEndian(endian);
        itemBean.setByteStart(Integer.parseInt(byteStart));
        itemBean.setByteLen(Integer.parseInt(byteLen));
        itemBean.setExpression(expression);
        itemBean.setField(field);

        return itemBean;
    }

}
