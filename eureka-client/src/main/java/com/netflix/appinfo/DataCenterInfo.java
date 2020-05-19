package com.netflix.appinfo;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.netflix.discovery.converters.jackson.DataCenterTypeInfoResolver;

/**
 * A simple interface for indicating which <em>datacenter</em> a particular instance belongs.
 *
 * @author Karthik Ranganathan
 *
 */
@JsonRootName("dataCenterInfo")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY, property = "@class")
@JsonTypeIdResolver(DataCenterTypeInfoResolver.class)
public interface DataCenterInfo {
    /**
     * 数据中心名枚举
     */
    enum Name {Netflix, Amazon, MyOwn}
    /**
     * @return 归属的数据中心名
     */
    Name getName();
}
